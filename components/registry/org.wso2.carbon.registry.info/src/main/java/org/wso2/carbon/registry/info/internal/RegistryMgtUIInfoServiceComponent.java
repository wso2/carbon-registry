/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.info.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.core.exception.EventBrokerException;
import org.wso2.carbon.event.core.subscription.Subscription;
import org.wso2.carbon.registry.admin.api.jmx.ISubscriptionsService;
import org.wso2.carbon.registry.common.beans.SubscriptionBean;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.eventing.services.EventingService;
import org.wso2.carbon.registry.eventing.services.SubscriptionEmailVerficationService;
import org.wso2.carbon.registry.extensions.jmx.Subscriptions;
import org.wso2.carbon.registry.info.Utils;
import org.wso2.carbon.registry.info.services.utils.SubscriptionBeanPopulator;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.*;

/**
 * @scr.component name="org.wso2.carbon.registry.info" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="registry.eventing.service"
 * interface="org.wso2.carbon.registry.eventing.services.EventingService" cardinality="0..1"
 * policy="dynamic" bind="setRegistryEventingService" unbind="unsetRegistryEventingService"
 * @scr.reference name="registry.subscription.email.verification.service"
 * interface="org.wso2.carbon.registry.eventing.services.SubscriptionEmailVerficationService" cardinality="0..1"
 * policy="dynamic" bind="setSubscriptionEmailVerficationService" unbind="unsetSubscriptionEmailVerficationService"
 * @scr.reference name="registry.subscriptions.jmx.service"
 * interface="org.wso2.carbon.registry.extensions.jmx.Subscriptions" cardinality="0..1"
 * policy="dynamic" bind="setSubscriptions" unbind="unsetSubscriptions"
 * @scr.reference name="configuration.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 */
public class RegistryMgtUIInfoServiceComponent {

    private static Log log = LogFactory.getLog(RegistryMgtUIInfoServiceComponent.class);

    private ServiceRegistration infoServiceRegistration = null;

    protected void activate(ComponentContext context) {
        // TODO: uncomment when the backend-frontend seperation when running in same vm is completed
        // infoServiceRegistration = context.getBundleContext().registerService(
        //        IInfoService.class.getName(), new InfoService(), null);
        log.debug("******* Registry Info Management bundle is activated ******* ");
    }

    protected void deactivate(ComponentContext context) {
        if (infoServiceRegistration != null) {
            infoServiceRegistration.unregister();
            infoServiceRegistration = null;
        }
        log.debug("******* Registry Info UI Management bundle is deactivated ******* ");
    }

    protected void setRegistryService(RegistryService registryService) {
        Utils.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        Utils.setRegistryService(null);
    }

    protected void setSubscriptions(Subscriptions eventing) {
        eventing.setImplBean(new ISubscriptionsService() {
            private String[] eventNames = null;

            public String subscribe(String endpoint, boolean isRestEndpoint, String path,
                                    String eventName) {
                PrivilegedCarbonContext.startTenantFlow();
                try {
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(
                            MultitenantConstants.SUPER_TENANT_ID);
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                    UserRegistry registry = Utils.getRegistryService().getRegistry(
                            CarbonConstants.REGISTRY_SYSTEM_USERNAME);
                    if (RegistryUtils.isRegistryReadOnly(registry.getRegistryContext())) {
                        return null;
                    }

                    SubscriptionBean subscriptionBean =
                            SubscriptionBeanPopulator.subscribeAndPopulate(registry, path, endpoint,
                                    eventName, isRestEndpoint);
                    if (subscriptionBean != null &&
                            subscriptionBean.getSubscriptionInstances() != null &&
                            subscriptionBean.getSubscriptionInstances().length > 0)
                    return subscriptionBean.getSubscriptionInstances()[0].getId();
                } catch (RegistryException e) {
                    log.error("An error occurred while subscribing", e);
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }
                return "";
            }

            public void unsubscribe(String id) {
                PrivilegedCarbonContext.startTenantFlow();
                try {
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(
                            MultitenantConstants.SUPER_TENANT_ID);
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                    Utils.getRegistryEventingService().unsubscribe(id);
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }

            public String[] getEventNames() {
                if (eventNames != null) {
                    return eventNames;
                }
                Set<String> output = new TreeSet<String>();
                Collection values = Utils.getRegistryEventingService().getEventTypes().values();
                for (Object value : values) {
                    String[] types = (String[])value;
                    if (types[0] != null) {
                        output.add(types[0]);
                    }
                    if (types[1] != null) {
                        output.add(types[1]);
                    }
                }
                eventNames = output.toArray(new String[output.size()]);
                return eventNames;
            }

            public String[] getList() {
                List<String> output = new LinkedList<String>();
                PrivilegedCarbonContext.startTenantFlow();
                try {
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(
                            MultitenantConstants.SUPER_TENANT_ID);
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                    List<Subscription> subscriptions =
                            Utils.getRegistryEventingService().getAllSubscriptions();
                    for (Subscription subscription : subscriptions) {
                        output.add(subscription.getId() + ":" + subscription.getTopicName() + ":" +
                                subscription.getEventSinkURL());
                    }
                } catch (EventBrokerException e) {
                    log.error("Unable to retrieve subscriptions", e);
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }
                return output.toArray(new String[output.size()]);
            }
        });
    }

    protected void unsetSubscriptions(Subscriptions eventing) {
        eventing.setImplBean(null);
    }

    protected void setRegistryEventingService(EventingService eventingService) {
        Utils.setRegistryEventingService(eventingService);
        log.debug("Successfully set registry eventing service");
    }

    protected void unsetRegistryEventingService(EventingService eventingService) {
        Utils.setRegistryEventingService(null);
    }

    protected void setSubscriptionEmailVerficationService(SubscriptionEmailVerficationService
            subscriptionEmailVerficationService) {
        Utils.setSubscriptionEmailVerficationService(subscriptionEmailVerficationService);
        log.debug("Successfully set subscription e-mail verification service");
    }

    protected void unsetSubscriptionEmailVerficationService(SubscriptionEmailVerficationService
            subscriptionEmailVerficationService) {
        Utils.setSubscriptionEmailVerficationService(null);
    }

    protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        log.debug("The Configuration Context Service was set");
        if (configurationContextService != null) {
            Utils.setConfigurationContext(configurationContextService.getServerConfigContext());
        }
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {
        Utils.setConfigurationContext(null);
    }
}
