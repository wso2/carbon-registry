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

package org.wso2.carbon.registry.info.services.utils;

import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourcePath;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.common.beans.TagBean;
import org.wso2.carbon.registry.common.beans.utils.Tag;
import org.wso2.carbon.registry.common.utils.UserUtil;

import java.util.List;

public class TagBeanPopulator {
    public static TagBean populate(UserRegistry userRegistry, String path) {
        TagBean tagBean = new TagBean();

        try {
            Resource resource = userRegistry.get(path);
            org.wso2.carbon.registry.core.Tag[] t = userRegistry.getTags(path);
            Tag[] tags = new Tag [t.length];
            Tag tag;
            for(int i=0; i<t.length; i++) {
                tag = new Tag();
                tag.setCategory(t[i].getCategory());
                tag.setTagCount(t[i].getTagCount());
                tag.setTagName(t[i].getTagName());
                tags[i] = tag;
            }
            
            tagBean.setTags(tags);

            ResourcePath resourcePath = new ResourcePath(path);
            tagBean.setPathWithVersion(resourcePath.getPathWithVersion());
            tagBean.setVersionView(!resourcePath.isCurrentVersion());
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
                    tagBean.setPutAllowed(
                            UserUtil.isPutAllowed(userRegistry.getUserName(), tempPath, userRegistry));
                } else if (user != null) {
                    if (userRegistry.getUserName().equals(user)) {
                        tagBean.setPutAllowed(true);
                    } else {
                        tagBean.setPutAllowed(UserUtil.isPutAllowed(
                                userRegistry.getUserName(), path, userRegistry));
                    }
                }
            } else {
                tagBean.setPutAllowed(UserUtil.isPutAllowed(userRegistry.getUserName(), path, userRegistry));
            }
            tagBean.setLoggedIn(!RegistryConstants.ANONYMOUS_USER.equals(userRegistry.getUserName()));

        } catch (RegistryException e) {

            String msg = "Failed to get tagging information of resource . " + e.getMessage();
            tagBean.setErrorMessage(msg);
        }

        return tagBean;
    }
}
