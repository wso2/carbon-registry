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
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;

import java.sql.SQLException;
import java.util.Properties;

public class UpdateTextContentUtil {

    private static final Log log = LogFactory.getLog(UpdateTextContentUtil.class);

    public static void updateTextContent(String path, String contentText, Registry registry)
            throws Exception {

        try {
            Resource resource = registry.get(path);
            String mediaType = resource.getMediaType();
            if (resource.getProperty(RegistryConstants.REGISTRY_LINK) != null &&
                    (CommonConstants.WSDL_MEDIA_TYPE.equals(mediaType) ||
                            CommonConstants.SCHEMA_MEDIA_TYPE.equals(mediaType))) {
                String description = resource.getDescription();
                Properties properties = (Properties) resource.getProperties().clone();
                resource = registry.newResource();
                resource.setMediaType(mediaType);
                resource.setDescription(description);
                resource.setProperties(properties);
            }
            resource.setContent(RegistryUtils.encodeString(contentText));
            registry.put(path, resource);
            resource.discard();

        } catch (RegistryException e) {

            String msg = "Could not update the content of the resource " +
                    path + ". Caused by: " + ((e.getCause() instanceof SQLException) ?
                    "" : e.getMessage());
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }
}
