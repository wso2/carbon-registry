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
import org.wso2.carbon.registry.extensions.utils.CommonConstants;

import java.sql.SQLException;

public class ImportResourceUtil {

    private static final Log log = LogFactory.getLog(ImportResourceUtil.class);

    public static String importResource(
                String parentPath,
                String resourceName,
                String mediaType,
                String description,
                String fetchURL,
                String symlinkLocation,
                UserRegistry userRegistry,
                String[][] properties) throws Exception {

        String resourcePath;
        if (RegistryConstants.ROOT_PATH.equals(parentPath)) {
            resourcePath = RegistryConstants.ROOT_PATH + resourceName;
        } else {
            resourcePath = parentPath + RegistryConstants.PATH_SEPARATOR + resourceName;
        }

        try {
            Resource metadataResource = userRegistry.newResource();

            metadataResource.setMediaType(mediaType);
            metadataResource.setDescription(description);

            if (symlinkLocation != null) {
                if (!symlinkLocation.endsWith(RegistryConstants.PATH_SEPARATOR)) {
                    symlinkLocation += RegistryConstants.PATH_SEPARATOR;
                }
                metadataResource.setProperty(RegistryConstants.SYMLINK_PROPERTY_NAME, symlinkLocation);
            }

            if (properties != null && properties.length > 0) {
                for (String[] p : properties) {
                    metadataResource.setProperty(p[0], p[1]);
                }
            }
            metadataResource.setProperty(CommonConstants.SOURCE_PROPERTY, CommonConstants.SOURCE_ADMIN_CONSOLE);
            String path = userRegistry.importResource(resourcePath, fetchURL, metadataResource);
           /* if (properties != null && properties.length > 0) {
                Resource resource = userRegistry.get(path);
                for (String[] p : properties) {
                    resource.setProperty(p[0], p[1]);
                }
                userRegistry.put(path, resource);
            }*/
            metadataResource.discard();

            return path;

        } catch (RegistryException e) {
            String msg = "Failed to import resource from the URL " + fetchURL + " to path " +
                    resourcePath + ". " + ((e.getCause() instanceof SQLException) ?
                    "" : e.getMessage());
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }

    }
}
