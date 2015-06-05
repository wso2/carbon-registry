/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.registry.eventing.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.email.verification.util.EmailVerifcationSubscriber;
import org.wso2.carbon.email.verification.util.EmailVerifierConfig;
import org.wso2.carbon.event.core.EventBroker;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.eventing.services.EventingService;

public class EventingDataHolder {

    private static EventingDataHolder holder = new EventingDataHolder();

    private EventingDataHolder(){
    }

    public static EventingDataHolder getInstance(){
          return holder;
    }

    private RegistryService registryService;

    private String defaultEventingServiceURL;

    private EventingService registryEventingService;

    private EventBroker registryEventBrokerService;

    private ConfigurationContext configurationContext;

    private EmailVerifcationSubscriber emailVerificationSubscriber;

    private EmailVerifierConfig emailVerifierConfig = null;

    private JMXEventsBean eventsBean;

    private NotificationConfig  notificationConfig;

    public RegistryService getRegistryService() {
        return registryService;
    }

    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    public String getDefaultEventingServiceURL() {
        return defaultEventingServiceURL;
    }

    public void setDefaultEventingServiceURL(String defaultEventingServiceURL) {
        this.defaultEventingServiceURL = defaultEventingServiceURL;
    }

    public EventingService getRegistryEventingService() {
        return registryEventingService;
    }

    public void setRegistryEventingService(EventingService registryEventingService) {
        this.registryEventingService = registryEventingService;
    }

    public EventBroker getRegistryEventBrokerService() {
        return registryEventBrokerService;
    }

    public void setRegistryEventBrokerService(EventBroker registryEventBrokerService) {
        this.registryEventBrokerService = registryEventBrokerService;
    }

    public ConfigurationContext getConfigurationContext() {
        return configurationContext;
    }

    public void setConfigurationContext(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

    public EmailVerifcationSubscriber getEmailVerificationSubscriber() {
        return emailVerificationSubscriber;
    }

    public void setEmailVerificationSubscriber(EmailVerifcationSubscriber emailVerificationSubscriber) {
        this.emailVerificationSubscriber = emailVerificationSubscriber;
    }

    public EmailVerifierConfig getEmailVerifierConfig() {
        return emailVerifierConfig;
    }

    public void setEmailVerifierConfig(EmailVerifierConfig emailVerifierConfig) {
        this.emailVerifierConfig = emailVerifierConfig;
    }

    public JMXEventsBean getEventsBean() {
        return eventsBean;
    }

    public void setEventsBean(JMXEventsBean eventsBean) {
        this.eventsBean = eventsBean;
    }

    public NotificationConfig getNotificationConfig() {
        return notificationConfig;
    }

    public void setNotificationConfig(NotificationConfig notificationConfig) {
        this.notificationConfig = notificationConfig;
    }
}
