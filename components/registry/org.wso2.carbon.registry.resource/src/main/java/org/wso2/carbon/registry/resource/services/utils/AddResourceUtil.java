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
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;

import javax.activation.DataHandler;
import java.io.ByteArrayInputStream;
import java.sql.SQLException;

public class AddResourceUtil {

    private static final Log log = LogFactory.getLog(AddResourceUtil.class);

    public static void addResource(
            String path, String mediaType, String description, DataHandler content,
            String symlinkLocation, Registry registry, String[][] properties)
            throws Exception {

        try {
            boolean isNew = !(registry.resourceExists(path));
            ResourceImpl resourceImpl = (ResourceImpl) (isNew ? registry.newResource() :
                    registry.get(path));
            if (resourceImpl.getProperty(RegistryConstants.REGISTRY_LINK) != null &&
                    (CommonConstants.WSDL_MEDIA_TYPE.equals(mediaType) ||
                            CommonConstants.SCHEMA_MEDIA_TYPE.equals(mediaType))) {
                resourceImpl = (ResourceImpl) registry.newResource();
            }
            resourceImpl.setMediaType(mediaType);
            resourceImpl.setDescription(description);
            if (properties != null && properties.length > 0) {
                for (String[] p : properties) {
                    resourceImpl.setProperty(p[0], p[1]);
                }
            }
           //allow to upload a file with empty content
            if (content == null) {
                String temp = "";
                resourceImpl.setContentStream(new ByteArrayInputStream(RegistryUtils.encodeString(temp)));
            } else {
                resourceImpl.setContentStream(content.getInputStream());
            }


            if (symlinkLocation != null && isNew) {
                if (!symlinkLocation.endsWith(RegistryConstants.PATH_SEPARATOR)) {
                    symlinkLocation += RegistryConstants.PATH_SEPARATOR;
                }
                resourceImpl.setProperty(RegistryConstants.SYMLINK_PROPERTY_NAME, symlinkLocation);
                // The symbolic link location is expected to be set only for WSDLs, Schemas and
                // Policies. Therefore, if this has been set, a symbolic link will be created at the
                // given location. However, if the symbolic link already exists, that means that the
                // content is being updated. In such a situation, the symbolic link will first be
                // removed before attempting to do the put operation. This will fix the issue
                // mentioned in CARBON-7350.
                if (registry.resourceExists(path)) {
                    Resource resource = registry.get(path);
                    if (resource != null) {
                        if (resource.getProperty("registry.link") != null) {
                            registry.removeLink(path);
                        }
                    }
                }
            }
            registry.put(path, resourceImpl);
            resourceImpl.discard();

        } catch (Exception e) {

            String msg = "Failed to add resource " + path + ". " + ((e.getCause() instanceof SQLException) ?
                    "" : e.getMessage());
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }
}
