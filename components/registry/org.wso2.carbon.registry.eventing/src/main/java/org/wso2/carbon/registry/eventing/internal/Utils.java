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

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.email.verification.util.EmailVerifcationSubscriber;
import org.wso2.carbon.email.verification.util.EmailVerifierConfig;
import org.wso2.carbon.event.core.EventBroker;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.eventing.services.EventingService;

public class Utils {
    private static RegistryService registryService;

    private static String defaultEventingServiceURL;

    private static EventingService registryEventingService;

    private static EventBroker registryEventBrokerService;

    private static ConfigurationContext configurationContext;

    private static EmailVerifcationSubscriber emailVerificationSubscriber;

    private static EmailVerifierConfig emailVerifierConfig = null;

    private static JMXEventsBean eventsBean;

    public static synchronized void setRegistryService(RegistryService service) {
        registryService = service;
    }

    public static synchronized RegistryService getRegistryService() {
        return registryService;
    }

    public static String getDefaultEventingServiceURL() {
        return defaultEventingServiceURL;
    }

    public static void setDefaultEventingServiceURL(String defaultEventingServiceURL) {
        Utils.defaultEventingServiceURL = defaultEventingServiceURL;
    }

    public static EventingService getRegistryEventingService() {
        return registryEventingService;
    }

    public static void setRegistryEventingService(EventingService registryEventingService) {
        Utils.registryEventingService = registryEventingService;
    }

    public static EventBroker getRegistryEventBrokerService() {
        return registryEventBrokerService;
    }

    public static void setRegistryEventBrokerService(EventBroker registryEventBrokerService) {
        Utils.registryEventBrokerService = registryEventBrokerService;
    }

    public static ConfigurationContext getConfigurationContext() {
        return configurationContext;
    }

    public static void setConfigurationContext(ConfigurationContext configurationContext) {
        Utils.configurationContext = configurationContext;
    }

    public static EmailVerifcationSubscriber getEmailVerificationSubscriber() {
        return emailVerificationSubscriber;
    }

    public static void setEmailVerificationSubscriber(
            EmailVerifcationSubscriber emailVerificationSubscriber) {
        Utils.emailVerificationSubscriber = emailVerificationSubscriber;
    }

    public static EmailVerifierConfig getEmailVerifierConfig() {
        return emailVerifierConfig;
    }

    public static void setEmailVerifierConfig(EmailVerifierConfig emailVerifierConfig) {
        Utils.emailVerifierConfig = emailVerifierConfig;
    }

    public static JMXEventsBean getEventsBean() {
        return eventsBean;
    }

    public static void setEventsBean(JMXEventsBean eventsBean) {
        Utils.eventsBean = eventsBean;
    }
}
