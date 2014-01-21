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
import org.wso2.carbon.registry.common.beans.CommentBean;
import org.wso2.carbon.registry.common.beans.utils.Comment;
import org.wso2.carbon.registry.common.utils.UserUtil;

import java.util.Calendar;
import java.util.List;

public class CommentBeanPopulator {
    public static CommentBean populate(UserRegistry userRegistry, String path) {

        CommentBean commentBean = new CommentBean();

        try {
            Resource resource = userRegistry.get(path);
            org.wso2.carbon.registry.core.Comment[] c = userRegistry.getComments(path);
            Comment [] comments = new Comment [c.length];
            Comment comment;
            for (int i=0; i <c.length; i++) {
                comment = new Comment();
                comment.setAuthorUserName(c[i].getAuthorUserName());
                comment.setCommentPath(c[i].getCommentPath());
                comment.setContent(c[i].getContent());
                Calendar createdDate = Calendar.getInstance();
                createdDate.setTime(c[i].getCreatedTime());
                comment.setCreatedTime(createdDate);
                comment.setDescription(c[i].getDescription());
                Calendar lastModifiedDate = Calendar.getInstance();
                // We don't support editing comments as yet.
                lastModifiedDate.setTime(c[i].getCreatedTime());
                comment.setLastModified(lastModifiedDate);
                comment.setMediaType(c[i].getMediaType());
                comment.setResourcePath(c[i].getResourcePath());
                comment.setText(c[i].getText());
                Calendar calendarTime = Calendar.getInstance();
                calendarTime.setTime(c[i].getTime());
                comment.setTime(calendarTime);
                comment.setUser(c[i].getUser());
                comments[i] = comment;
            }

            commentBean.setComments(comments);

            ResourcePath resourcePath = new ResourcePath(path);
            commentBean.setPathWithVersion(resourcePath.getPathWithVersion());
            commentBean.setVersionView(!resourcePath.isCurrentVersion());
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
                    commentBean.setPutAllowed(
                            UserUtil.isPutAllowed(userRegistry.getUserName(), tempPath, userRegistry));
                } else if (user != null) {
                    if (userRegistry.getUserName().equals(user)) {
                        commentBean.setPutAllowed(true);
                    } else {
                        commentBean.setPutAllowed(UserUtil.isPutAllowed(
                                userRegistry.getUserName(), path, userRegistry));
                    }
                }
            } else {
                commentBean.setPutAllowed(UserUtil.isPutAllowed(userRegistry.getUserName(), path, userRegistry));
            }
            commentBean.setLoggedIn(!RegistryConstants.ANONYMOUS_USER.equals(userRegistry.getUserName()));
        } catch (RegistryException e) {

            String msg = "Failed to get comment information of the resource. " + e.getMessage();

            commentBean.setErrorMessage(msg);
        }

        return commentBean;
    }
}
