/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.registry.info;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.eventing.services.EventingService;
import org.wso2.carbon.registry.eventing.services.SubscriptionEmailVerficationService;

public class Utils {
    private static final Log log = LogFactory.getLog(Utils.class);

    private static RegistryService registryService;

    private static EventingService registryEventingService;

    private static SubscriptionEmailVerficationService subscriptionEmailVerficationService;

    private static ConfigurationContext configurationContext;

    public static synchronized void setRegistryService(RegistryService service) {
        registryService = service;
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static EventingService getRegistryEventingService() {
        return registryEventingService;
    }

    public static void setRegistryEventingService(EventingService registryEventingService) {
        Utils.registryEventingService = registryEventingService;
    }

    public static SubscriptionEmailVerficationService getSubscriptionEmailVerficationService() {
        return subscriptionEmailVerficationService;
    }

    public static void setSubscriptionEmailVerficationService(SubscriptionEmailVerficationService subscriptionEmailVerficationService) {
        Utils.subscriptionEmailVerficationService = subscriptionEmailVerficationService;
    }

    public static ConfigurationContext getConfigurationContext() {
        return configurationContext;
    }

    public static void setConfigurationContext(ConfigurationContext configurationContext) {
        Utils.configurationContext = configurationContext;
    }
}
