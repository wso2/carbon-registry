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
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import java.sql.SQLException;

public class AddTextResourceUtil {

    private static final Log log = LogFactory.getLog(AddTextResourceUtil.class);

    public static void addTextResource(
            String parentPath,
            String resourceName,
            String mediaType,
            String description,
            String contentString,
            UserRegistry userRegistry) throws Exception {

        String resourcePath;
        if (RegistryConstants.ROOT_PATH.equals(parentPath)) {
            resourcePath = RegistryConstants.ROOT_PATH + resourceName;
        } else {
            resourcePath = parentPath + RegistryConstants.PATH_SEPARATOR + resourceName;
        }

        byte[] content = null;
        if (contentString != null) {
            content = RegistryUtils.encodeString(contentString);
        }

        try {
            Resource resource = userRegistry.newResource();

            resource.setMediaType(mediaType);
            resource.setDescription(description);
            resource.setContent(RegistryUtils.decodeBytes(content));

            userRegistry.put(resourcePath, resource);
            resource.discard();

        } catch (RegistryException e) {
            String msg = "Failed to add resource with text based content to path " +
                    resourcePath + ". " + ((e.getCause() instanceof SQLException) ?
                    "" : e.getMessage());
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }
}
