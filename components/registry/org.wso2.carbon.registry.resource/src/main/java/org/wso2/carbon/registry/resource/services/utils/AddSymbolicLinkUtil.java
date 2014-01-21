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
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.sql.SQLException;

public class AddSymbolicLinkUtil {

    private static final Log log = LogFactory.getLog(AddTextResourceUtil.class);

    public static void addSymbolicLink (
            UserRegistry userRegistry,
            String parentPath,
            String name,
            String targetPath) throws Exception {

        String linkResourcePath;
        if (RegistryConstants.ROOT_PATH.equals(parentPath)) {
            linkResourcePath = RegistryConstants.ROOT_PATH + name;
        } else {
            linkResourcePath = parentPath + RegistryConstants.PATH_SEPARATOR + name;
        }

        if (RegistryConstants.ROOT_PATH.equals(targetPath)) {
            String msg = "Unable to create symbolic link to root.";
            log.error(msg);
            throw new RegistryException(msg);
        }

        try {
            userRegistry.createLink(linkResourcePath, targetPath);

        } catch (RegistryException e) {
            String msg = "Failed to add symbolic link to path " +
                    linkResourcePath + ". " + ((e.getCause() instanceof SQLException) ?
                    "" : e.getMessage());
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }

}
