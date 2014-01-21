/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.registry.eventing.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.ListenerManager;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.AxisFault;
import org.osgi.service.component.ComponentContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.wso2.carbon.email.verification.util.EmailVerifcationSubscriber;
import org.wso2.carbon.email.verification.util.Util;
import org.wso2.carbon.event.core.EventBroker;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.jdbc.handlers.filters.URLMatcher;
import org.wso2.carbon.registry.core.jdbc.handlers.HandlerLifecycleManager;
import org.wso2.carbon.registry.eventing.RegistryEventDispatcher;
import org.wso2.carbon.registry.eventing.RegistryEventingConstants;
import org.wso2.carbon.registry.eventing.services.EventingService;
import org.wso2.carbon.registry.eventing.services.EventingServiceImpl;
import org.wso2.carbon.registry.eventing.services.SubscriptionEmailVerficationService;
import org.wso2.carbon.registry.eventing.handlers.RegistryEventingHandler;
import org.wso2.carbon.registry.eventing.handlers.SubscriptionManagerHandler;
import org.wso2.carbon.registry.eventing.handlers.erbsm.EmbeddedRegistryBasedSubscriptionManagerResourceRelocateHandler;
import org.wso2.carbon.registry.eventing.exceptions.ActivationException;
import org.wso2.carbon.registry.app.RemoteRegistryService;
import org.wso2.carbon.registry.common.eventing.NotificationService;
import org.wso2.carbon.registry.extensions.jmx.Events;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.NetworkUtils;
import org.wso2.carbon.email.verification.util.EmailVerifierConfig;

import java.io.File;
import java.net.SocketException;

/**
 * @scr.component name="org.wso2.carbon.registry.eventing" immediate="true"
 * @scr.reference name="configuration.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="listener.manager.service"
 * interface="org.apache.axis2.engine.ListenerManager" cardinality="1..1" policy="dynamic"
 * bind="setListenerManager" unbind="unsetListenerManager"
 * @scr.reference name="eventbroker.service"
 * interface="org.wso2.carbon.event.core.EventBroker"
 * cardinality="1..1" policy="dynamic"
 * bind="setEventBroker" unbind="unsetEventBroker"
 * @scr.reference name="email.verification.service"
 * interface="org.wso2.carbon.email.verification.util.EmailVerifcationSubscriber"
 * cardinality="1..1" policy="dynamic"
 * bind="setEmailVerificationSubscriber" unbind="unsetEmailVerificationSubscriber"
 * @scr.reference name="registry.events.jmx.service"
 * interface="org.wso2.carbon.registry.extensions.jmx.Events" cardinality="0..1"
 * policy="dynamic" bind="setEvents" unbind="unsetEvents"
 */
public class RegistryEventingServiceComponent {

    private static Log log = LogFactory.getLog(RegistryEventingServiceComponent.class);

    private boolean configurationDone = false;

    private Registry registry = null;

    private ConfigurationContextService configurationContextService = null;

    private ListenerManager listenerManager = null;

    private boolean initialized = false;

    private String endpoint = null;

    private ServiceRegistration eventingServiceRegistration = null;
    private ServiceRegistration notificationServiceRegistration = null;
    private ServiceRegistration emailVerificationServiceRegistration = null;
    private EventingServiceImpl service = null;

    private BundleContext bundleContext = null;

    private String eventingRoot = "/repository/components/org.wso2.carbon.event/";

    protected void activate(ComponentContext context) {
        bundleContext = context.getBundleContext();
        initialize();
        registerEventingService();
        log.debug("Registry Eventing bundle is activated ");
    }

    private void registerEventingService() {
        if (listenerManager != null && eventingServiceRegistration == null && bundleContext != null) {
            service = new EventingServiceImpl();
            eventingServiceRegistration = bundleContext.registerService(EventingService.class.getName(), service, null);
            notificationServiceRegistration = bundleContext.registerService(NotificationService.class.getName(), service, null);
            emailVerificationServiceRegistration = bundleContext.registerService(
                    SubscriptionEmailVerficationService.class.getName(), service, null);
            Utils.setRegistryEventingService(service);
            log.debug("Successfully setup the Eventing OGSi Service");
        }
    }

    private void unregisterEventingService() {
        if (eventingServiceRegistration != null) {
            Utils.setRegistryEventingService(null);
            eventingServiceRegistration.unregister();
            eventingServiceRegistration = null;
            notificationServiceRegistration.unregister();
            notificationServiceRegistration = null;
            emailVerificationServiceRegistration.unregister();
            emailVerificationServiceRegistration = null;
            service = null;
            log.debug("Successfully unregistered the Eventing OGSi Service");
        }
    }

    protected void deactivate(ComponentContext context) {
        unregisterEventingService();
        log.debug("Registry Eventing bundle is deactivated ");
    }

    protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        log.debug("The Configuration Context Service was set");
        this.configurationContextService  = configurationContextService;
        if (configurationContextService != null) {
            Utils.setConfigurationContext(configurationContextService.getServerConfigContext());
        }
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {
        Utils.setConfigurationContext(null);
    }

    protected void setRegistryService(RegistryService registryService) {
        Utils.setRegistryService(registryService);
    }

    private void setupEmailVerification() {
        if (Utils.getEmailVerificationSubscriber() == null) {
            return;
        }
        EmailVerifierConfig emailVerifierConfig = Utils.getEmailVerifierConfig();
        if (emailVerifierConfig == null) {
            String fileName = CarbonUtils.getCarbonConfigDirPath() + File.separator +
                    "notifications-email-verification.xml";
            if ((new File(fileName)).exists()) {
                emailVerifierConfig = Util.loadeMailVerificationConfig(fileName);
            }
            if (emailVerifierConfig == null) {
                emailVerifierConfig = new EmailVerifierConfig();
            }
            if (emailVerifierConfig.getEmailBody() == null) {
                emailVerifierConfig.setEmailBody("To complete your subscription process, please " +
                        "click on the link below to verify your e-mail address.");
            }
            if (emailVerifierConfig.getEmailFooter() == null) {
                emailVerifierConfig.setEmailFooter("This message is automatically generated " +
                        "by the WSO2 Carbon Registry.");
            }
            if (emailVerifierConfig.getTargetEpr() == null) {
                String registryURL = Utils.getDefaultEventingServiceURL();
                if (registryURL != null && registryURL.indexOf(
                        "/services/RegistryEventingService") > -1) {
                    registryURL = registryURL.substring(0, registryURL.length() -
                            "/services/RegistryEventingService".length()) +
                            "/carbon";
                }
                if (registryURL == null) {
                    log.error("Unable to obtain registry URL");
                    emailVerifierConfig = null;
                    return;
                }
                if (!registryURL.endsWith("/")) {
                    registryURL = registryURL + "/";
                }
                emailVerifierConfig.setTargetEpr(registryURL +
                        "info/subscription-email-verification.jsp");
            }
            if (emailVerifierConfig.getSubject() == null) {
                emailVerifierConfig.setSubject("E-mail Address Verification");
            }
            if (emailVerifierConfig.getRedirectPath() == null) {
                emailVerifierConfig.setRedirectPath("../info/subscription-email-verified.jsp");
            }
            log.debug("The E-mail Verfication Component Configuration has been done.");
            Utils.setEmailVerifierConfig(emailVerifierConfig);
        }
    }

    protected void unsetEmailVerificationSubscriber(EmailVerifcationSubscriber emailVerificationSubscriber) {
        Utils.setEmailVerificationSubscriber(null);
    }

    protected void setEmailVerificationSubscriber(EmailVerifcationSubscriber emailVerificationSubscriber) {
        Utils.setEmailVerificationSubscriber(emailVerificationSubscriber);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        Utils.setRegistryService(null);
    }

    protected void setEventBroker(EventBroker eventBroker) {
        Utils.setRegistryEventBrokerService(eventBroker);
    }

    protected void unsetEventBroker(EventBroker eventBroker) {
        Utils.setRegistryEventBrokerService(null);
    }

    protected void setListenerManager(ListenerManager listenerManager) {
        this.listenerManager = listenerManager;
        //initialize();
        //registerEventingService();
    }

    protected void setEvents(Events notifications) {
        JMXEventsBean implBean = new JMXEventsBean();
        notifications.setImplBean(implBean);
        Utils.setEventsBean(implBean);
    }

    protected void unsetEvents(Events notifications) {
        Utils.setEventsBean(null);
        notifications.setImplBean(null);
    }

    protected void unsetListenerManager(ListenerManager listenerManager) {
        this.listenerManager = null;
        unregisterEventingService();
    }

    private void initialize() {
        ConfigurationContext serverConfigurationContext = configurationContextService.getServerConfigContext();
        if (!configurationDone && listenerManager != null && Utils.getRegistryService() != null) {
            String host;
            try {
                host = NetworkUtils.getLocalHostname();
            } catch (SocketException e) {
                host = null;
                log.warn("An error occured while determining server host", e);
            }
            if (host == null) {
                host = System.getProperty("carbon.local.ip");
                log.warn("Unable to obtain server host, using the carbon.local.ip system "
                        + "property to determine the ip address.");
            }
            log.debug("Found Server Host: " + host);
            if (serverConfigurationContext != null) {
                AxisConfiguration config = serverConfigurationContext.getAxisConfiguration();
                if (config != null && config.getTransportIn("https") != null &&
                        config.getTransportIn("https").getReceiver() != null) {
                    try {
                        EndpointReference[] eprArray = config.getTransportIn("https")
                                .getReceiver().getEPRsForService("RegistryEventingService",
                                        host);
                        if (eprArray != null && eprArray[0] != null) {
                            endpoint = eprArray[0].getAddress();
                            if (endpoint != null && endpoint.endsWith("/")) {
                                endpoint = endpoint.substring(0, endpoint.length() - 1);
                            }
                        } else {
                            String msg = "Unable to obtain EPR for service. " +
                                "Attempting to construct EPR based on known parameters.";
                            log.warn(msg);
                        }
                    } catch (AxisFault e) {
                        String msg = "Error occured while obtaining EPR for service. " +
                                "Attempting to construct EPR based on known parameters.";
                        log.warn(msg, e);
                    }
                }
            }
            if (endpoint == null) {
                // Try to an alternate strategy to obtain the endpoint URL based on the ip address
                // and the port.
                StringBuffer epBuf = new StringBuffer("https://");
                String port = null;
                try {
                    port = Integer.toString(CarbonUtils.getTransportPort(serverConfigurationContext, "https"));
                } catch (Exception e) {
                    port = null;
                    log.warn("Unable to get HTTP port from Server Axis Configuration, using system defined value");
                }
                if (port == null) {
                    port = System.getProperty("carbon.https.port");
                }
                log.debug("Found Server Port: " + port);
                if (host != null && port != null) {
                    epBuf.append(host);
                    epBuf.append(':');
                    epBuf.append(port);
                    AxisConfiguration axisConfig = null;
                    if (serverConfigurationContext != null) {
                        axisConfig = serverConfigurationContext.getAxisConfiguration();
                    }
                    if (axisConfig != null) {
                        log.debug("Successfully obtained the Axis Configuration");
                    }
                    if (axisConfig != null && serverConfigurationContext.getContextRoot() != null) {
                        String contextRoot = serverConfigurationContext.getContextRoot();
                        log.debug("Found Context Root: " + contextRoot);
                        epBuf.append(contextRoot);
                        if (!contextRoot.endsWith("/")) {
                            epBuf.append('/');
                        }
                    }
                    if (axisConfig != null && axisConfig.getParameter("servicePath") != null) {
                        log.debug("Found Service Path: " + axisConfig.getParameter("servicePath").getValue());
                        epBuf.append((String) axisConfig.getParameter("servicePath").getValue());
                        epBuf.append('/');
                        endpoint = epBuf.toString() + RegistryEventingConstants.EVENTING_SERVICE_NAME;
                    }
                }
            }
            if (endpoint == null) {
                String msg = "Failed obtaining server configuration";
                log.error(msg);
                throw new ActivationException(msg);
            }
            log.debug("The Registry Eventing Service is available at: " + endpoint);
            try {
                /*Utils.setRemoteTopicHeaderName(Utils.getRegistryEventBrokerService()
                        .getSubscriptionManager().getPropertyValue("topicHeaderName"));
                Utils.setRemoteTopicHeaderNS(Utils.getRegistryEventBrokerService()
                        .getSubscriptionManager().getPropertyValue("topicHeaderNS"));
                eventingRoot = Utils.getRegistryEventBrokerService()
                        .getSubscriptionManager().getPropertyValue("subscriptionStoragePath");
                Utils.setRemoteSubscriptionStoreContext(eventingRoot);*/
                setupHandlers();
                setupDispatchers();
                setupEmailVerification();
                log.debug("Successfully instantiated the Registry Event Source");
            } catch (Exception e) {
                String msg = "Error Instantiating Registry Event Source";
                log.error(msg);
                throw new ActivationException(msg);
            }
            configurationDone = true;
        }
    }

    // TODO: This is hack to set the Registry Dispatcher. Get rid of this once message formatting is
    //       available.
    private void setupDispatchers() throws Exception {
        RegistryEventDispatcher dispatcher = new RegistryEventDispatcher();
        dispatcher.init(Utils.getConfigurationContext());
        if (Utils.getRegistryEventBrokerService() != null) {
            Utils.getRegistryEventBrokerService().registerEventDispatcher(
                    RegistryEventingConstants.TOPIC_PREFIX, dispatcher);
        }
    }

    private void setupHandlers() {
        if (Boolean.toString(Boolean.TRUE).equals(System.getProperty("disable.event.handlers"))) {
            initialized = true;
            log.debug("Default Eventing Handlers have been disabled. Events will not be " +
                    "generated unless a custom handler has been configured.");
            return;
        }
        RegistryService registryService = Utils.getRegistryService();
        if (registryService instanceof RemoteRegistryService && !initialized) {
            initialized = true;
            log.warn("Eventing is not available on Remote Registry");
            return;
        }
        if (!initialized && listenerManager != null && registryService != null) {
            initialized = true;
            try {
                // We can't get Registry from Utils, as the MessageContext is not available at
                // activation time.
                Registry systemRegistry = registryService.getConfigSystemRegistry();
                if (registry != null && registry == systemRegistry) {
                    return;
                }
                registry = systemRegistry;
                if (registry == null ||
                        registry.getRegistryContext() == null ||
                        registry.getRegistryContext().getHandlerManager() == null) {
                    String msg = "Error Initializing Registry Eventing Handler";
                    log.error(msg);
                } else {
                    URLMatcher filter = new URLMatcher();
                    filter.setDeletePattern(".*");
                    filter.setPutPattern(".*");
                    filter.setPutChildPattern(".*");
                    filter.setMovePattern(".*");
                    filter.setCopyPattern(".*");
                    filter.setRenamePattern(".*");
                    filter.setCreateVersionPattern(".*");
                    filter.setApplyTagPattern(".*");
                    filter.setRemoveTagPattern(".*");
                    filter.setAddCommentPattern(".*");
                    filter.setAddAssociationPattern(".*");
                    filter.setRemoveAssociationPattern(".*");
                    filter.setRateResourcePattern(".*");
                    filter.setCreateLinkPattern(".*");
                    filter.setRemoveLinkPattern(".*");
                    filter.setRestorePattern(".*");
                    RegistryEventingHandler handler = new RegistryEventingHandler();
                    registry.getRegistryContext().getHandlerManager().addHandler(null, filter,
                            handler, HandlerLifecycleManager.DEFAULT_REPORTING_HANDLER_PHASE);
                    registry.setEventingServiceURL(null, endpoint);
                    Utils.setDefaultEventingServiceURL(endpoint);
                    log.debug("Successfully Initialized the Registry Eventing Handler");

                    /*URLMatcher erbSubManagerMountFilter = new URLMatcher();
                    erbSubManagerMountFilter.setPutPattern(
                            eventingRoot + "/.*");
                    SubscriptionManagerHandler erbSubManagerMountHanlder =
                            new EmbeddedRegistryBasedSubscriptionManagerMountHandler();
                    registry.getRegistryContext().getHandlerManager().addHandler(null,
                            erbSubManagerMountFilter, erbSubManagerMountHanlder);
                    erbSubManagerMountHanlder.init(registry.getRegistryContext(), eventingRoot);
                    log.debug("Successfully Initialized the Subscription Manager Mount Handler");*/


                    URLMatcher erbSubManagerRRFilter = new URLMatcher();
                    erbSubManagerRRFilter.setCopyPattern(".*");
                    erbSubManagerRRFilter.setRenamePattern(".*");
                    erbSubManagerRRFilter.setMovePattern(".*");
                    erbSubManagerRRFilter.setDeletePattern(".*");
                    SubscriptionManagerHandler erbSubManagerRRHanlder =
                            new EmbeddedRegistryBasedSubscriptionManagerResourceRelocateHandler();
                    registry.getRegistryContext().getHandlerManager().addHandler(null,
                            erbSubManagerRRFilter, erbSubManagerRRHanlder,
                            HandlerLifecycleManager.DEFAULT_REPORTING_HANDLER_PHASE);
                    erbSubManagerRRHanlder.init(registry.getRegistryContext(), eventingRoot);
                    log.debug("Successfully Initialized the Subscription Manager Resource Relocate Handler");
                    log.info("Successfully Initialized Eventing on Registry");
                }
            } catch (Exception e) {
                String msg = "Error Initializing Eventing on Registry";
                log.error(msg, e);
                throw new RuntimeException(msg, e);
            }
        }
    }
}

