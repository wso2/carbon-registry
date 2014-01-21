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

package org.wso2.carbon.registry.relations.services.utils;

import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourcePath;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.relations.beans.AssociationBean;
import org.wso2.carbon.registry.relations.beans.DependenciesBean;
import org.wso2.carbon.registry.common.utils.UserUtil;

import java.util.List;

public class DependenciesBeanPopulator {

    public static DependenciesBean populate(UserRegistry userRegistry, String path) {

        DependenciesBean dependenciesBean = new DependenciesBean();
        ResourcePath resourcePath = new ResourcePath(path);
        try {
            Association[] asso = CommonUtil.getAssociations(userRegistry,
                    (resourcePath.isCurrentVersion() ?
                            resourcePath.getPath() : resourcePath.getPathWithVersion()));
            Resource resource = userRegistry.get(path);

            AssociationBean[] beans = new AssociationBean[asso.length];
            for (int i = 0; i < beans.length; i++) {
                Association as = asso[i];
                beans[i] = new AssociationBean(as.getSourcePath(), as.getDestinationPath(),
                        as.getAssociationType());

            }
            dependenciesBean.setAssociationBeans(beans);

            dependenciesBean.setVersionView(!resourcePath.isCurrentVersion());
            dependenciesBean.setPathWithVersion(resourcePath.getPathWithVersion());
            List mountPoints = resource.getPropertyValues("registry.mountpoint");
            List targetPoints = resource.getPropertyValues("registry.targetpoint");
//            List paths = resource.getPropertyValues("registry.path");
            List actualPaths = resource.getPropertyValues("registry.actualpath");
            String user = resource.getProperty("registry.user");
            if (resource.getProperty("registry.link") != null) {

                if(mountPoints != null && targetPoints != null) {
//                    String mountPoint = (String)mountPoints.get(0);
//                    String targetPoint = (String)targetPoints.get(0);
//                        String tempPath;
//                        if (targetPoint.equals(RegistryConstants.PATH_SEPARATOR) && !path.equals(mountPoint)) {
//                            tempPath = ((String)paths.get(0)).substring(mountPoint.length());
//                        } else {
//                            tempPath = targetPoint + ((String)paths.get(0)).substring(mountPoint.length());
//                        }
                    String tempPath = (String)actualPaths.get(0);
                    dependenciesBean.setPutAllowed(
                            UserUtil.isPutAllowed(userRegistry.getUserName(), tempPath, userRegistry));
                } else if (user != null) {
                    if (userRegistry.getUserName().equals(user)) {
                        dependenciesBean.setPutAllowed(true);
                    } else {
                        dependenciesBean.setPutAllowed(UserUtil.isPutAllowed(
                                userRegistry.getUserName(), path, userRegistry));
                    }
                    }
            } else {
                dependenciesBean.setPutAllowed(UserUtil.isPutAllowed(userRegistry.getUserName(), path, userRegistry));
            }
            dependenciesBean.setLoggedIn(!RegistryConstants.ANONYMOUS_USER.equals(userRegistry.getUserName()));
        } catch (RegistryException e) {

            String msg = "Failed to get dependencies of resource " +
                    resourcePath + ". " + e.getMessage();
            dependenciesBean.setErrorMessage(msg);
        }

        return dependenciesBean;
    }
}
