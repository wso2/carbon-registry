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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourcePath;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.relations.beans.AssociationTreeBean;
import org.wso2.carbon.registry.common.CommonConstants;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class AssociationTreeBeanPopulator {

    private static final Log log = LogFactory.getLog(AssociationTreeBeanPopulator.class);
    private static List paths;

    public static AssociationTreeBean populate(UserRegistry userRegistry, String resourcePath, String associationType) {
        paths = new ArrayList();
        AssociationTreeBean associationTreeBean = new AssociationTreeBean();
        associationTreeBean.setResourcePath(resourcePath);
        associationTreeBean.setAssoType(associationType);
        associationTreeBean.setAssoIndex(0);

        try {
            List fatherAsso = provideChildAssociations(resourcePath, associationType, userRegistry);
            if (fatherAsso != null) {
                Iterator ifatherAssofaterAsso = fatherAsso.iterator();

                while (ifatherAssofaterAsso.hasNext()) {
                    Association asso = (Association) ifatherAssofaterAsso.next();
                    associationTreeBean.setAssociationTree(associationTreeBean.getAssociationTree() +
                            createAssociationTree(
                                    asso, associationTreeBean, userRegistry));

                }
            }

            String [] pathArray = new String [paths.size()];
            for(int i=0; i<paths.size(); i++) {
                pathArray[i] = (String) paths.get(i);
            }
            associationTreeBean.setTreeCache(pathArray);
        } catch (RegistryException e) {
            String msg = "Failed to generate association tree of the resource " +
                    resourcePath + ". " + e.getMessage();
            log.error(msg, e);
            associationTreeBean.setErrorMessage(msg);
        }

        return associationTreeBean;
    }

    private static String createAssociationTree(Association tmpAsso, AssociationTreeBean associationTreeBean, UserRegistry registry) throws RegistryException {

        String path = tmpAsso.getDestinationPath();
        associationTreeBean.setAssoIndex(associationTreeBean.getAssoIndex() + 1);
        StringBuffer associationTreePart = new StringBuffer();

        if (registry.resourceExists(path)) {
            Resource father = registry.get(path);

            String fatherResourceType = father instanceof Collection ? "collection" : "resource";
            List fatherAsso = provideChildAssociations(path, associationTreeBean.getAssoType(), registry);
            paths.add(path);

            boolean foundInfinite = false;

            if (fatherAsso == null) foundInfinite = true;

            associationTreePart.append("<div class=\"father-object\" >");
            if (associationTreeBean.getAssoType().equals(CommonConstants.ASSOCIATION_TYPE01)) {
                associationTreePart.append("<ul class=\"tree-row-object\"><li>");
            } else {
                associationTreePart.append("<ul class=\"tree-row-object\"><li class=\"first\">");
            }
            if (!foundInfinite) {
                if (!fatherAsso.isEmpty()) {
                    boolean showChildren = false;
                    Iterator iFaterAsso = fatherAsso.iterator();
                    while (iFaterAsso.hasNext()) {
                        Association asso = (Association) iFaterAsso.next();

                        // if path is equal to the destination path, it is a backward association. we are only displaying
                        // the forward associations here
                        if (!path.equals(asso.getDestinationPath())) {
                            if (!paths.contains(asso.getDestinationPath())) {
                                showChildren = true;
                                break;
                            }
                        }
                    }
                    if (showChildren) {
                        associationTreePart.append("<a onclick=\"showHideCommon('y_").append(
                                associationTreeBean.getAssoIndex()).append(
                                "');showHideCommon('xminus_").append(
                                associationTreeBean.getAssoIndex()).append(
                                "');showHideCommon('xplus_").append(
                                associationTreeBean.getAssoIndex()).append(
                                "');\">").append(
                                "<img src=\"../resources/images/icon-tree-minus.gif\"").append(
                                "id=\"xminus_").append(associationTreeBean.getAssoIndex()).append(
                                "\" style=\"margin-right:2px;\" />").append(
                                "<img src=\"../resources/images/icon-tree-plus.gif\"").append(
                                "id=\"xplus_").append(associationTreeBean.getAssoIndex()).append(
                                "\" style=\"margin-right:2px;display:none;\" /></a>");
                    }
                } else {
                    associationTreePart.append("<img src=\"../resources/images/spacer.gif\" style=\"width:15px;\" />");
                }
            } else {
                associationTreePart.append("<img src=\"../resources/images/spacer.gif\" style=\"width:15px;\" />");
            }
            if (fatherResourceType.equals("collection")) {
                associationTreePart.append("<img src=\"../resources/images/icon-folder-small.gif\" style=\"margin-right:2px;\" />");
            } else {
                associationTreePart.append("<img src=\"../resources/images/editshred.png\" style=\"margin-right:2px;\" />");
            }

            //associationTreePart += "<a title=\"" + path + "\" href=\"resource.jsp?path=" + calculateRelativePath(path) + "\">";
            String tempPath = path;
            if (tempPath != null) {
                try {
                    tempPath = URLEncoder.encode(tempPath, "UTF-8");
                } catch (UnsupportedEncodingException ignored) {
                }
            }
            associationTreePart.append("<a title=\"").append(path).append(
                    "\" href=\"../resources/resource.jsp?region=region3&item=resource_browser_menu&viewType=std&path=").append(
                    tempPath).append("\">");
            String pathSmall = getShortnedPath(associationTreeBean, path);
            if (foundInfinite) {
                associationTreePart.append(pathSmall).append("(infinite loop..)");
            } else {
                associationTreePart.append(pathSmall);
            }
            associationTreePart.append("</a></li>");
            if (!associationTreeBean.getAssoType().equals(CommonConstants.ASSOCIATION_TYPE01))
                associationTreePart.append("<li class=\"second\">").append(
                        tmpAsso.getAssociationType()).append("</li>");
            associationTreePart.append("</ul></div>");
            if (!foundInfinite && !fatherAsso.isEmpty()) {
                associationTreePart.append("<div class=\"child-objects\" id=\"y_").append(
                        associationTreeBean.getAssoIndex()).append("\">");
                Iterator iFaterAsso = fatherAsso.iterator();
                while (iFaterAsso.hasNext()) {
                    Association asso = (Association) iFaterAsso.next();

                    // if path is equal to the destination path, it is a backward association. we are only displaying
                    // the forward associations here
                    if (!path.equals(asso.getDestinationPath())) {
                        if (!paths.contains(asso.getDestinationPath())) {
                            associationTreePart.append(createAssociationTree(asso, associationTreeBean, registry));
                        }
                    }
                }
                associationTreePart.append("</div>");

            }

            father.discard();
        } else {
            associationTreePart.append("<div class=\"father-object\" >");
            if (associationTreeBean.getAssoType().equals(CommonConstants.ASSOCIATION_TYPE01)) {
                associationTreePart.append("<ul class=\"tree-row-object\"><li>");
            } else {
                associationTreePart.append("<ul class=\"tree-row-object\"><li class=\"first\">");
            }
            associationTreePart.append("<img src=\"../resources/images/spacer.gif\"").append(
                    "style=\"width:15px;\" />");

            String pathSmall = getShortnedPath(associationTreeBean, path);
            associationTreePart.append("<img src=\"../resources/images/goto_url.gif\" style=\"margin-right:2px;\" />");
            associationTreePart.append("<a target=\"_blank\" title=\"").append(path).append("\" href=\"").append(
                    path).append("\">");
           associationTreePart.append(pathSmall).append("</a></li>");
            if (!associationTreeBean.getAssoType().equals(CommonConstants.ASSOCIATION_TYPE01))
                associationTreePart.append("<li class=\"second\">").append(
                        tmpAsso.getAssociationType()).append("</li>");
            associationTreePart.append("</ul></div>");
        }

        //associationTreeBean.setAssociationTree(associationTreeBean.getAssociationTree() + associationTreePart);
        return associationTreePart.toString();

    }

    private static String getShortnedPath(AssociationTreeBean associationTreeBean, String path) {
        String pathSmall = path;
        if (pathSmall != null) {
            if (associationTreeBean.getAssoType().equals(CommonConstants.ASSOCIATION_TYPE01)) {
                if (pathSmall.length() >= 108) {
                    pathSmall = pathSmall.substring(0, 50) + " .... " +
                            pathSmall.substring(pathSmall.length() - 50, pathSmall.length());
                }
            } else {
                if (pathSmall.length() >= 68) {
                    pathSmall = pathSmall.substring(0, 30) + " .... " +
                            pathSmall.substring(pathSmall.length() - 30, pathSmall.length());
                }
            }
        }
        return pathSmall;
    }

    /*private static String calculateRelativePath(String resourcePath) {
        if (resourcePath.startsWith("http://")) {
            return resourcePath;
        }

        String relativePath = resourcePath;

        if (RegistryConstants.ROOT_PATH.equals(resourcePath)) {
            relativePath = "";
        } else {
            if (resourcePath.startsWith(RegistryConstants.PATH_SEPARATOR)) {
                relativePath = resourcePath.substring(1, resourcePath.length());
            }
        }
        return relativePath;
    }*/

    private static List provideChildAssociations(
            String path, String assoType, UserRegistry registry)
            throws RegistryException {

        List tmpAssociations = new ArrayList();
        List associations = new ArrayList();
        ResourcePath resourcePath = new ResourcePath(path);
        Resource resource = registry.get(path);
        Association[] asso = CommonUtil.getAssociations(registry, (resourcePath.isCurrentVersion() ?
                resourcePath.getPath() : resourcePath.getPathWithVersion()));

        tmpAssociations.addAll(Arrays.asList(asso));

        Iterator iAssociations = tmpAssociations.iterator();
        while (iAssociations.hasNext()) {
            Association tmpAsso = (Association) iAssociations.next();
            //Get all associations filtered by it's association type
                if (tmpAsso.getAssociationType().equals(CommonConstants.ASSOCIATION_TYPE01)
                        && assoType.equals(CommonConstants.ASSOCIATION_TYPE01) && tmpAsso.getSourcePath().equals(path))
                    associations.add(tmpAsso);
                if (!tmpAsso.getAssociationType().equals(CommonConstants.ASSOCIATION_TYPE01)
                        && !assoType.equals(CommonConstants.ASSOCIATION_TYPE01) && tmpAsso.getSourcePath().equals(path))
                    associations.add(tmpAsso);
        }

        resource.discard();
        // if path is equal to the destination path, it is a backward association. we are only displaying
        // the forward associations here

        return associations;
    }
}
