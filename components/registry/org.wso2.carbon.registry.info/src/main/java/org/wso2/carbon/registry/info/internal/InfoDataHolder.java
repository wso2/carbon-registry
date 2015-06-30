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

package org.wso2.carbon.registry.info.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.eventing.services.EventingService;
import org.wso2.carbon.registry.eventing.services.SubscriptionEmailVerficationService;

public class InfoDataHolder {

    private RegistryService registryService;

    private EventingService registryEventingService;

    private SubscriptionEmailVerficationService subscriptionEmailVerficationService;

    private ConfigurationContext configurationContext;

    private static InfoDataHolder holder = new InfoDataHolder();

    private InfoDataHolder() {
    }

    public static InfoDataHolder getInstance() {
        return holder;
    }

    public RegistryService getRegistryService() {
        return registryService;
    }

    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    public EventingService getRegistryEventingService() {
        return registryEventingService;
    }

    public void setRegistryEventingService(EventingService registryEventingService) {
        this.registryEventingService = registryEventingService;
    }

    public SubscriptionEmailVerficationService getSubscriptionEmailVerficationService() {
        return subscriptionEmailVerficationService;
    }

    public void setSubscriptionEmailVerficationService(
            SubscriptionEmailVerficationService subscriptionEmailVerficationService) {
        this.subscriptionEmailVerficationService = subscriptionEmailVerficationService;
    }

    public ConfigurationContext getConfigurationContext() {
        return configurationContext;
    }

    public void setConfigurationContext(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }
}
