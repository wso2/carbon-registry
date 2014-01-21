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

public class MoveResourceUtil {

    private static final Log log = LogFactory.getLog(MoveResourceUtil.class);

    public static void moveResource(
            UserRegistry registry,String parentPath,
            String oldResourcePath, String destinationPath, String resourceName)
            throws Exception {

        if (!destinationPath.startsWith(RegistryConstants.PATH_SEPARATOR)) {
            destinationPath = RegistryConstants.PATH_SEPARATOR + destinationPath;
        }

        String newResourcePath;
        if (destinationPath.endsWith(RegistryConstants.PATH_SEPARATOR)) {
            newResourcePath = destinationPath + resourceName;
        } else {
            newResourcePath=destinationPath + "/" +resourceName;
        }

        try {
            if (registry.resourceExists(destinationPath) &&
                    !(registry.get(destinationPath) instanceof CollectionImpl)) {
                throw new RegistryException("A resource can't be moved to a resource");
            }
            registry.move(oldResourcePath, newResourcePath);

        } catch (RegistryException e) {
            String msg = "Failed to move the resource: " + oldResourcePath + " to Destination: " +
                    newResourcePath + ". Caused by: " +
                    ((e.getCause() instanceof SQLException) ?
                    "" : e.getMessage());
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }
}
