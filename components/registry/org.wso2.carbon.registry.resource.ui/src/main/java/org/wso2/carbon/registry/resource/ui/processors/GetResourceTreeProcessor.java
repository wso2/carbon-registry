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

package org.wso2.carbon.registry.resource.ui.processors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.common.ui.UIException;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.ResourceTreeEntryBean;
import org.wso2.carbon.registry.resource.ui.Utils;
import org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient;
import org.wso2.carbon.registry.resource.ui.processors.utils.ResourceTreeData;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GetResourceTreeProcessor {

    private static final Log log = LogFactory.getLog(GetResourceTreeProcessor.class);

    public static String process(
            HttpServletRequest request, HttpServletResponse response, ServletConfig config)
            throws UIException {
        return process(request, response, config, RegistryConstants.ROOT_PATH, null);
    }

    public static String process(HttpServletRequest request, HttpServletResponse response,
                                 ServletConfig config, String resourcePath, String parentId)
            throws UIException {
        String cookie = (String) request.
                getSession().getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        ResourceServiceClient client;
        try {
            client = new ResourceServiceClient(cookie, config, request.getSession());
        } catch (Exception e) {
            String msg = "Failed to initialize the resource service client " +
                    "to get resource tree data. " + e.getMessage();
            log.error(msg, e);
            throw new UIException(msg, e);
        }

        String textBoxId = request.getParameter("textBoxId");
        try {
            ResourceTreeData resourceTreeData = new ResourceTreeData();
            fillSubResourceTree(resourcePath, resourceTreeData, client,textBoxId, parentId,
                    request.getParameter("hideResources") != null);

            String displayHTML = "";
            displayHTML += resourceTreeData.getResourceTree();
            return displayHTML;

        } catch (RegistryException e) {

            String msg = "Failed to generate the resource tree for the resource " +
                    resourcePath + ". " + e.getMessage();
            log.error(msg, e);
            throw new UIException(msg, e);
        }
    }

    private static String getTreeFolderIcon(ResourceTreeEntryBean entryBean) {
        if (entryBean.getSymlink() == null) {
            return "icon-folder-small.gif";
        } else if (entryBean.getSymlink().equals("symlink")) {
            return "collection-extn.gif";
        }
        return "collection-extn-mounted.gif";
    }

    private static String getTreeResourceIcon(ResourceTreeEntryBean entryBean) {
        if (entryBean.getSymlink() == null) {
            return "resource.gif";
        } else if (entryBean.getSymlink().equals("symlink")) {
            return "resource-extn.gif";
        }
        return "resource-extn-mounted.gif";
    }

    private static void fillResourceTree(
            String resourcePath, ResourceTreeData treeData, ResourceServiceClient client,String textBoxId)
            throws RegistryException {

        String[] childPaths = {""};
        String resourceName = "";
        boolean hasChildren = false;


        ResourceTreeEntryBean resourceEntry;
        try {
            resourceEntry = client.getResourceTreeEntry(resourcePath);
        } catch (Exception e) {
            String msg = "Failed to get resource tree entry for resource " +
                    resourcePath + ". " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }

        if (resourceEntry.getCollection()) {
            childPaths = Utils.getSortedChildNodes(resourceEntry.getChildren());
            if (childPaths != null && childPaths.length > 0) hasChildren = true;
        }

        if (resourcePath != null && !resourcePath.equals(RegistryConstants.ROOT_PATH)) {
            String[] parts = resourcePath.split(RegistryConstants.PATH_SEPARATOR);
            resourceName = parts[parts.length - 1];
        } else {
            resourceName = "/";
        }

        treeData.incrementTreeIndex();
        treeData.appendToTree("<div class=\"father-object\">");
        if (resourceEntry.getCollection()) {
            treeData.appendToTree("<a onclick=\"showHideCommon('y_plus_" + treeData.getResourceTreeIndex() + "');showHideCommon('y_minus_" + treeData.getResourceTreeIndex() + "');showHideCommon('z_" + treeData.getResourceTreeIndex() + "');\">");
            if (hasChildren) {
                treeData.appendToTree("<img src=\"../resources/images/icon-tree-plus.jpg\" id=\"y_plus_" + treeData.getResourceTreeIndex() + "\" style=\"display:none;margin-right:5px;\" />" +
                        "<img src=\"../resources/images/icon-tree-minus.jpg\" id=\"y_minus_" + treeData.getResourceTreeIndex() + "\" style=\"margin-right:5px;\" />");
            } else {
                treeData.appendToTree("<img src=\"../resources/images/spacer.gif\" style=\"width:18px;height:10px;\" />");
            }
            treeData.appendToTree("<a onclick=\"pickPath('" + resourcePath + "','" + textBoxId + "')\" title=\"" + resourcePath + "\">" +
                    "<img src=\"../resources/images/" + getTreeFolderIcon(resourceEntry) + "\" style=\"margin-right:2px;\" />" +
                    resourceName +
                    "</a>");
        } else {
            treeData.appendToTree("<img src=\"../resources/images/spacer.gif\" style=\"width:18px;height:10px;\" />");
            treeData.appendToTree("<a class=\"plane-resource\" onclick=\"pickPath('" + resourcePath + "','" + textBoxId + "')\" title=\"" + resourcePath + "\">" + "<img src=\"../resources/images/" + getTreeResourceIcon(resourceEntry) + "\" style=\"margin-right:2px;\" />" + resourceName + "</a>");
        }
        treeData.appendToTree("</div>" + "<div class=\"child-objects\" id=\"z_" + treeData.getResourceTreeIndex() + "\">");
        if (!resourceEntry.getCollection()) {
            treeData.appendToTree("</div>");
            return;
        } else {
            if (!hasChildren) {
                treeData.appendToTree("</div>");
                return;
            } else {

                for (int i = 0; childPaths.length > i; i++) {
                    //Recursive call
                    fillResourceTree(childPaths[i], treeData, client,textBoxId);
                }
            }
        }
        treeData.appendToTree("</div>");
    }

    private static void fillSubResourceTree(
            String resourcePath, ResourceTreeData treeData, ResourceServiceClient client,String textBoxId,
            String parentId, boolean hideResources) throws RegistryException {

        String[] childPaths = {""};
        String resourceName = "";
        boolean hasChildren = false;
        
        ResourceTreeEntryBean resourceEntry;
        try {
            resourceEntry = client.getResourceTreeEntry(resourcePath);
        } catch (Exception e) {
            String msg = "Failed to get resource tree entry for resource " +
                    resourcePath + ". " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
        if (resourceEntry.getCollection()) {
            childPaths = Utils.getSortedChildNodes(resourceEntry.getChildren());
            if (childPaths != null && childPaths.length > 0) hasChildren = true;
        } else if (hideResources) {
            return;
        }
        if (hasChildren) {
            for (int i = 0; childPaths.length > i; i++) {
                String[] parts = childPaths[i].split(RegistryConstants.PATH_SEPARATOR);
                String fatherId = "father_" + parentId + "_" + i;
                String childId = "child_" + parentId + "_" + i;
                if (parts != null && parts.length > 1) {
                    resourceName = parts[parts.length - 1];
                }
                /* get the child entry for the current entry */
                ResourceTreeEntryBean childResouceEntry;
                try {
                    childResouceEntry = client.getResourceTreeEntry(childPaths[i]);
                } catch (Exception e) {
                    String msg = "Failed to get resource tree entry for resource " +
                            childPaths[i] + ". " + e.getMessage();
                    log.warn(msg, e);
                    continue;
                }
                boolean childHasChildren = false;
                if (childResouceEntry.getCollection()) {
                    String []childChildPaths = childResouceEntry.getChildren();
                    if (childChildPaths != null && childChildPaths.length > 0) childHasChildren = true;
                } else if (hideResources) {
                    continue;
                }
                treeData.appendToTree("<div class=\"father-object\" id=\"" + fatherId + "\">");
                /* if this has children we let it expandable */
                if (childResouceEntry.getCollection()) {
                    if (childHasChildren) {
                        treeData.appendToTree("<a onclick=\"loadSubTree('" + childPaths[i] + "', '" + parentId + "_" + i + "', '" + textBoxId + "', '" + (hideResources? "true" : "false") + "')\">");
                        treeData.appendToTree("<img src=\"../resources/images/icon-tree-plus.jpg\" id=\"plus_" + parentId + "_" + i + "\" style=\"margin-right:5px;\"  />" +
                                "<img src=\"../resources/images/icon-tree-minus.jpg\" id=\"minus_" + parentId + "_" + i + "\" style=\"display:none;margin-right:5px;\"/>");
                    } else {
                       treeData.appendToTree("<img src=\"../resources/images/spacer.gif\" style=\"width:18px;height:10px;\" />");
                    }
                    treeData.appendToTree("<a onclick=\"pickPath('" + childPaths[i] + "','" + textBoxId + "', '" + parentId + "_" + i + "');\" title=\"" + childPaths[i] + "\">" +
                            "<img src=\"../resources/images/" + getTreeFolderIcon(childResouceEntry) + "\" style=\"margin-right:2px;\" />" +
                            resourceName +
                            "</a>");
                    treeData.appendToTree("</div>" + "<div class=\"child-objects\" id=\"" + childId + "\"></div>");
                } else {
                    treeData.appendToTree("<img src=\"../resources/images/spacer.gif\" style=\"width:18px;height:10px;\" />");
                    treeData.appendToTree("<a class=\"plane-resource\" onclick=\"pickPath('" + childPaths[i] + "','" + textBoxId + "', '" + parentId + "_" + i + "');\" title=\"" + childPaths[i] + "\">" + "<img src=\"../resources/images/" + getTreeResourceIcon(childResouceEntry) + "\" style=\"margin-right:2px;\"/>" + resourceName + "</a></div>");
                }
            }
        }
    }
}
