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
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.sql.SQLException;

public class AddCollectionUtil {

    private static final Log log = LogFactory.getLog(AddCollectionUtil.class);

    public static String process(
            String parentPath, String collectionName, String mediaType, String description, UserRegistry registry)
            throws RegistryException {

        String path;
        while (collectionName != null && collectionName.startsWith(RegistryConstants.PATH_SEPARATOR)) {
            collectionName = collectionName.substring(RegistryConstants.PATH_SEPARATOR.length()); 
        }
        if (parentPath.equals(RegistryConstants.ROOT_PATH)) {
            path = RegistryConstants.ROOT_PATH + collectionName;
        } else {
            path = parentPath + RegistryConstants.PATH_SEPARATOR + collectionName;
        }

        CollectionImpl collection = null;
        try {
            collection = (CollectionImpl) registry.newCollection();
        } catch (RegistryException e) {
            String msg = "Failed to create new collection instance for the collection " +
                    path + ". " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }

        collection.setPath(path);
        if (!mediaType.equals(RegistryConstants.DEFAULT_MEDIA_TYPE)) {
            collection.setMediaType(mediaType);
        }
        collection.setDescription(description);

        try {
            registry.put(path, collection);
        } catch (RegistryException e) {
            String msg = "Failed to add collection " + path + ". " +
                    ((e.getCause() instanceof SQLException) ?
                    "" : e.getMessage());
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }

        return parentPath;
    }
}
