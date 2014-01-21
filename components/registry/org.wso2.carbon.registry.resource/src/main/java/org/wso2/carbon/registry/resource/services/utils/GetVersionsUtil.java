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
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.common.CommonConstants;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourcePath;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.beans.VersionPath;
import org.wso2.carbon.registry.resource.beans.VersionsBean;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.AuthorizationManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;

public class GetVersionsUtil {

    private static final Log log = LogFactory.getLog(GetVersionsUtil.class);

    public static VersionsBean getVersionsBean(UserRegistry userRegistry, String path) throws Exception {

        VersionsBean versionsBean = new VersionsBean();
        versionsBean.setResourcePath(path);

        try {
            Resource currentResource = userRegistry.get(path);
            if (currentResource != null) {
                String isLink = currentResource.getProperty("registry.link");
                String mountPoint = currentResource.getProperty("registry.mountpoint");
                String targetPoint = currentResource.getProperty("registry.targetpoint");
                String actualPath = currentResource.getProperty("registry.actualpath");
                if (CarbonContext.getThreadLocalCarbonContext().getUsername() != null &&
                        !CarbonContext.getThreadLocalCarbonContext().getUsername().equals(currentResource.getProperty(CommonConstants.RETENTION_USERNAME_PROP_NAME)) &&
                        Boolean.parseBoolean(currentResource.getProperty(CommonConstants.RETENTION_WRITE_LOCKED_PROP_NAME))) {

                    versionsBean.setWriteLocked(currentResource.getProperty(CommonConstants.RETENTION_WRITE_LOCKED_PROP_NAME));
                    versionsBean.setDeleteLocked(currentResource.getProperty(CommonConstants.RETENTION_DELETE_LOCKED_PROP_NAME));

                } else {
                    versionsBean.setWriteLocked("false");
                    versionsBean.setDeleteLocked("false");
                }
                if (isLink != null && mountPoint != null && targetPoint != null) {
//                    path = path.replace(mountPoint, targetPoint);
                    // This is a symbolic link
                    currentResource = userRegistry.get(actualPath);
                }
            }
            if (currentResource != null) {
                versionsBean.setPermalink(currentResource.getPermanentPath());
                currentResource.discard();
            }

            String[] versions = userRegistry.getVersions(path);

            List <VersionPath> versionPaths = new ArrayList <VersionPath> ();
            for (String version : versions) {
                VersionPath versionPath = new VersionPath();
                versionPath.setCompleteVersionPath(version);
                versionPath.setActiveResourcePath(path);

                ResourcePath resourcePath = new ResourcePath(version);
                if (!resourcePath.isCurrentVersion()) {
                    long versionNumber = Long.parseLong(resourcePath.
                            getParameterValue(RegistryConstants.VERSION_PARAMETER_NAME));
                    versionPath.setVersionNumber(versionNumber);
                }

                Resource versionResource = userRegistry.get(version);
                if (versionResource != null) {
                    versionPath.setUpdater(versionResource.getLastUpdaterUserName());
                    Calendar versionLastModified = Calendar.getInstance();
                    versionLastModified.setTime(versionResource.getLastModified());
                    versionPath.setUpdatedOn(versionLastModified);
                    versionResource.discard();
                }

                versionPaths.add(versionPath);
            }

            versionsBean.setVersionPaths(
                    versionPaths.toArray(new VersionPath[versionPaths.size()]));

            String userName = userRegistry.getUserName();
            versionsBean.setLoggedIn(!RegistryConstants.ANONYMOUS_USER.equals(userName));

            ResourcePath resourcePath = new ResourcePath(path);
            try {
                AuthorizationManager authorizer = userRegistry.getUserRealm().getAuthorizationManager();
                boolean putAllowed = authorizer.
                        isUserAuthorized(userName, resourcePath.getPath(), ActionConstants.PUT);
                boolean deleteAllowed =
                        authorizer.isUserAuthorized(userName,resourcePath.getPath(),ActionConstants.DELETE);
                versionsBean.setPutAllowed(putAllowed);
                versionsBean.setDeletePermissionAllowed(deleteAllowed);

            } catch (UserStoreException e) {

                String msg = "Failed to check put permissions of user " + userName +
                        " on the resource " + path + ". " + e.getMessage();
                log.error(msg, e);
                throw new RegistryException(msg, e);
            }

        } catch (RegistryException e) {

            String msg = "Failed to get version information of resource " +
                    path + ". " + ((e.getCause() instanceof SQLException) ?
                    "" : e.getMessage());
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }

        return versionsBean;
    }
}
