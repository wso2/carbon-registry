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

import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.common.CommonConstants;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.beans.MetadataBean;
import org.wso2.carbon.registry.common.WebResourcePath;
import org.wso2.carbon.registry.common.utils.CommonUtil;
import org.wso2.carbon.user.core.AuthorizationManager;


import java.util.ArrayList;
import java.util.List;

public class MetadataPopulator {

    public static MetadataBean populate(String purePath, UserRegistry registry) throws Exception {

        ResourcePath path = new ResourcePath(purePath);
        Resource resource = registry.get(path.getPathWithVersion());
        MetadataBean bean = new MetadataBean();

        bean.setAuthor(resource.getAuthorUserName());
        bean.setCollection(resource instanceof Collection);
        bean.setDescription(resource.getDescription());
        bean.setLastUpdater(resource.getLastUpdaterUserName());
        bean.setMediaType(resource.getMediaType());
        bean.setPath(resource.getPath());
        bean.setFormattedCreatedOn(CommonUtil.formatDate(resource.getCreatedTime()));
        bean.setFormattedLastModified(CommonUtil.formatDate(resource.getLastModified()));

        if (CarbonContext.getThreadLocalCarbonContext().getUsername() != null &&
                !CarbonContext.getThreadLocalCarbonContext().getUsername().equals(resource.getProperty(CommonConstants.RETENTION_USERNAME_PROP_NAME)) &&
                Boolean.parseBoolean(resource.getProperty(CommonConstants.RETENTION_WRITE_LOCKED_PROP_NAME))) {

            bean.setWriteLocked(resource.getProperty(CommonConstants.RETENTION_WRITE_LOCKED_PROP_NAME));
            bean.setDeleteLocked(resource.getProperty(CommonConstants.RETENTION_DELETE_LOCKED_PROP_NAME));

        } else {
            bean.setWriteLocked("false");
            bean.setDeleteLocked("false");
        }

        if (resource instanceof CollectionImpl) {
            bean.setResourceVersion(String.valueOf(((CollectionImpl) resource).getVersionNumber()));
        } else {
            bean.setResourceVersion(String.valueOf(((ResourceImpl) resource).getVersionNumber()));
        }

        bean.setActiveResourcePath(path.getPath());

        if (path.parameterExists("version")) {
            bean.setVersionView(true);
            bean.setPermalink(path.getCompletePath());
        } else {
            bean.setVersionView(false);
            bean.setPermalink(resource.getPermanentPath());
        }
        if ((resource.getProperty("registry.link") != null) || (resource.getProperty("registry.mount") != null)) {
            bean.setPermalink("hide");
         }

        bean.setNavigatablePaths(constructNavigatablePaths(path.getCompletePath()));
        bean.setContentPath(path.getCompletePath());
        bean.setServerBaseURL("carbon registry base URL.");

        String versionPath;
        if (path.parameterExists("version")) {
            versionPath = path.getPath() + RegistryConstants.URL_SEPARATOR + "version" +
                    RegistryConstants.URL_PARAMETER_SEPARATOR + path.getParameterValue("version");
        } else {
            versionPath = path.getPath();
        }
        bean.setPathWithVersion(versionPath);

        String userName = registry.getUserName();
        AuthorizationManager authorizer = registry.getUserRealm().getAuthorizationManager();
        boolean putAllowed = authorizer.
                isUserAuthorized(userName, path.getPath(), ActionConstants.PUT);
        bean.setPutAllowed(putAllowed);

        return bean;
    }

    private static WebResourcePath[] constructNavigatablePaths(String rawPath) {

        List <WebResourcePath> navigatablePaths = new ArrayList<WebResourcePath>();

        if (rawPath.equals(RegistryConstants.ROOT_PATH)) {

            WebResourcePath webPath = new WebResourcePath();
            webPath.setNavigateName("/");
            webPath.setNavigatePath("#");

            navigatablePaths.add(webPath);

        } else {

            // first add the root path
            WebResourcePath rootPath = new WebResourcePath();
            rootPath.setNavigateName("/");
            rootPath.setNavigatePath(RegistryConstants.ROOT_PATH);
            navigatablePaths.add(rootPath);

            String preparedPath = rawPath;
            if (preparedPath.startsWith(RegistryConstants.PATH_SEPARATOR)) {
                preparedPath = preparedPath.substring(RegistryConstants.PATH_SEPARATOR.length());
            }

            if (preparedPath.endsWith(RegistryConstants.PATH_SEPARATOR)) {
                preparedPath = preparedPath.
                        substring(0, preparedPath.length() - RegistryConstants.PATH_SEPARATOR.length());
            }

            String[] parts = preparedPath.split(RegistryConstants.PATH_SEPARATOR);
            for (int i = 0; i < parts.length; i++) {
                WebResourcePath resourcePath = new WebResourcePath();

                if (i == parts.length - 1) {
                    String[] q = parts[i].split(RegistryConstants.URL_SEPARATOR);
                    if (q.length == 2) {
                        if (q[1].startsWith("version:")) {
                            String versionNumber = q[1].substring("version:".length());
                            String navName = q[0] + " (version " + versionNumber + ")";
                            resourcePath.setNavigateName(navName);
                        } else {
                            resourcePath.setNavigateName(parts[i]);
                        }
                    } else {
                        resourcePath.setNavigateName(parts[i]);
                    }
                } else {
                    resourcePath.setNavigateName(parts[i]);
                }

                StringBuffer tempPath = new StringBuffer();
                for (int j = 0; j < i + 1; j++) {
                    tempPath.append(RegistryConstants.PATH_SEPARATOR).append(parts[j]);
                }
                resourcePath.setNavigatePath(tempPath.toString());
                navigatablePaths.add(resourcePath);
            }
        }

        return navigatablePaths.toArray(new WebResourcePath[navigatablePaths.size()]);
    }
}
