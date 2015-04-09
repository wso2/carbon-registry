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

package org.wso2.carbon.registry.resource.services.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.common.eventing.NotificationService;
import org.wso2.carbon.registry.common.eventing.RegistryEvent;

public class CommonUtil {

    private static final Log log = LogFactory.getLog(CommonUtil.class);

    private static RegistryService registryService;

    private static NotificationService registryNotificationService;

    public static void setRegistryService(RegistryService service) {
        registryService = service;
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static void setRegistryNotificationService(NotificationService registryNotificationService) {
        CommonUtil.registryNotificationService = registryNotificationService;
    }

    public static void notify(RegistryEvent event, Registry registry, String path)
            throws Exception {
        try {
            if (CommonUtil.registryNotificationService == null) {
                log.debug("Eventing service is unavailable.");
                return;
            }
            if (registry == null || registry.getEventingServiceURL(path) == null) {
                CommonUtil.registryNotificationService.notify(event);
            } else if (registry.getEventingServiceURL(null) == null) {
                log.error("Unable to send notification.");
            } else if (registry.getEventingServiceURL(path).equals(registry.getEventingServiceURL(null))) {
                CommonUtil.registryNotificationService.notify(event);
            } else {
                CommonUtil.registryNotificationService.notify(event, registry.getEventingServiceURL(path));
            }
        } catch (RegistryException e) {
            log.error("Unable to send notification", e);
        }
    }

    /**
     * Return the concatenated full path of the resource where
     * it need to be stored. This will decrease the memory usage.
     * This method use StringBuilder to construct the string.
     * Therefore this method is not thread safe.
     *
     * @param parentPath ex: x /x /x/y x/y x/y/
     * @param resourceName ex: resourceName
     * @return resourcePath ex: /x/resourceName /x/resourceName /x/y/resourceName /x/y/resourceName /x/y/resourceName
     */
    public static String calculatePath(String parentPath, String resourceName) {
        StringBuilder resourcePath = new StringBuilder();
        if (!parentPath.startsWith(RegistryConstants.PATH_SEPARATOR)) {
            parentPath = RegistryConstants.PATH_SEPARATOR + parentPath;
        }
        if (parentPath.endsWith(RegistryConstants.PATH_SEPARATOR)) {
            resourcePath.append(parentPath).append(resourceName);
        } else {
            resourcePath.append(parentPath).append(RegistryConstants.PATH_SEPARATOR).append(resourceName);
        }
        return resourcePath.toString();
    }

    /**
     * Adding new properties to end of the properties array
     *
     * @param properties String[][] of properties
     * @return propertyArray    updated String[][] of properties
     */
    public static String[][] setProperties(String[][] properties, String key, String value) {
        String[][] propertyArray = new String[properties.length + 1][2];
        for (int i = 0; i <= properties.length; i++) {
            if (i < properties.length) {
                propertyArray[i][0] = properties[i][0];
                propertyArray[i][1] = properties[i][1];
            } else {
                propertyArray[properties.length][0] = key;
                propertyArray[properties.length][1] = value;
                return propertyArray;
            }
        }
        return new String[0][];
    }
}
