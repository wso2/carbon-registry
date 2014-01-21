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
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.sql.SQLException;

public class RenameResourceUtil {

    private static final Log log = LogFactory.getLog(RenameResourceUtil.class);

    public static void renameResource(
            String parentPath, String oldResourcePath, String newResourceName, UserRegistry registry)
            throws Exception {

        if (!parentPath.endsWith(RegistryConstants.PATH_SEPARATOR)) {
            parentPath = parentPath + RegistryConstants.PATH_SEPARATOR;
        }

        String newResourcePath;
        if (newResourceName.startsWith(RegistryConstants.ROOT_PATH)) {
            newResourcePath = newResourceName;
        } else {
            newResourcePath = parentPath + newResourceName;
        }

        try {
            registry.rename(oldResourcePath, newResourcePath);

        } catch (RegistryException e) {
            String msg = "Failed to rename the resource: " + oldResourcePath + " to name: " +
                    newResourcePath + ". Caused by: " +
                    ((e.getCause() instanceof SQLException) ?
                    "" : e.getMessage());
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }
}
