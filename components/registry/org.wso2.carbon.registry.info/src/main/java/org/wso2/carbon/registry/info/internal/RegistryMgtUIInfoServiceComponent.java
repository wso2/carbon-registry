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
import org.wso2.carbon.registry.event.core.exception.EventBrokerException;
import org.wso2.carbon.registry.event.core.subscription.Subscription;
import org.wso2.carbon.registry.admin.api.jmx.ISubscriptionsService;
import org.wso2.carbon.registry.common.beans.SubscriptionBean;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.eventing.services.EventingService;
import org.wso2.carbon.registry.eventing.services.SubscriptionEmailVerficationService;
import org.wso2.carbon.registry.extensions.jmx.Subscriptions;
import org.wso2.carbon.registry.info.services.utils.SubscriptionBeanPopulator;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import java.util.*;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
         name = "org.wso2.carbon.registry.info", 
         immediate = true)
public class RegistryMgtUIInfoServiceComponent {

    private static Log log = LogFactory.getLog(RegistryMgtUIInfoServiceComponent.class);

    private InfoDataHolder dataHolder = InfoDataHolder.getInstance();

    private ServiceRegistration infoServiceRegistration = null;

    @Activate
    protected void activate(ComponentContext context) {
        // TODO: uncomment when the backend-frontend seperation when running in same vm is completed
        // infoServiceRegistration = context.getBundleContext().registerService(
        // IInfoService.class.getName(), new InfoService(), null);
        log.debug("******* Registry Info Management bundle is activated ******* ");
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        if (infoServiceRegistration != null) {
            infoServiceRegistration.unregister();
            infoServiceRegistration = null;
        }
        log.debug("******* Registry Info UI Management bundle is deactivated ******* ");
    }

    @Reference(
             name = "registry.service", 
             service = org.wso2.carbon.registry.core.service.RegistryService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {
        dataHolder.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        dataHolder.setRegistryService(null);
    }

    @Reference(
             name = "registry.subscriptions.jmx.service", 
             service = org.wso2.carbon.registry.extensions.jmx.Subscriptions.class, 
             cardinality = ReferenceCardinality.OPTIONAL, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetSubscriptions")
    protected void setSubscriptions(Subscriptions eventing) {
        eventing.setImplBean(new ISubscriptionsService() {

            private String[] eventNames = null;

            public String subscribe(String endpoint, boolean isRestEndpoint, String path, String eventName) {
                PrivilegedCarbonContext.startTenantFlow();
                try {
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                    UserRegistry registry = dataHolder.getRegistryService().getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME);
                    if (RegistryUtils.isRegistryReadOnly(registry.getRegistryContext())) {
                        return null;
                    }
                    SubscriptionBean subscriptionBean = SubscriptionBeanPopulator.subscribeAndPopulate(registry, path, endpoint, eventName, isRestEndpoint);
                    if (subscriptionBean != null && subscriptionBean.getSubscriptionInstances() != null && subscriptionBean.getSubscriptionInstances().length > 0)
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
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                    dataHolder.getRegistryEventingService().unsubscribe(id);
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }

            public String[] getEventNames() {
                if (eventNames != null) {
                    return eventNames;
                }
                Set<String> output = new TreeSet<String>();
                Collection values = dataHolder.getRegistryEventingService().getEventTypes().values();
                for (Object value : values) {
                    String[] types = (String[]) value;
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
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                    List<Subscription> subscriptions = dataHolder.getRegistryEventingService().getAllSubscriptions();
                    for (Subscription subscription : subscriptions) {
                        output.add(subscription.getId() + ":" + subscription.getTopicName() + ":" + subscription.getEventSinkURL());
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

    @Reference(
             name = "registry.eventing.service", 
             service = org.wso2.carbon.registry.eventing.services.EventingService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRegistryEventingService")
    protected void setRegistryEventingService(EventingService eventingService) {
        dataHolder.setRegistryEventingService(eventingService);
        log.debug("Successfully set registry eventing service");
    }

    protected void unsetRegistryEventingService(EventingService eventingService) {
        dataHolder.setRegistryEventingService(null);
    }

    @Reference(
             name = "registry.subscription.email.verification.service", 
             service = org.wso2.carbon.registry.eventing.services.SubscriptionEmailVerficationService.class, 
             cardinality = ReferenceCardinality.OPTIONAL, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetSubscriptionEmailVerficationService")
    protected void setSubscriptionEmailVerficationService(SubscriptionEmailVerficationService subscriptionEmailVerficationService) {
        dataHolder.setSubscriptionEmailVerficationService(subscriptionEmailVerficationService);
        log.debug("Successfully set subscription e-mail verification service");
    }

    protected void unsetSubscriptionEmailVerficationService(SubscriptionEmailVerficationService subscriptionEmailVerficationService) {
        dataHolder.setSubscriptionEmailVerficationService(null);
    }

    @Reference(
             name = "configuration.context.service", 
             service = org.wso2.carbon.utils.ConfigurationContextService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        log.debug("The Configuration Context Service was set");
        if (configurationContextService != null) {
            dataHolder.setConfigurationContext(configurationContextService.getServerConfigContext());
        }
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {
        dataHolder.setConfigurationContext(null);
    }
}

