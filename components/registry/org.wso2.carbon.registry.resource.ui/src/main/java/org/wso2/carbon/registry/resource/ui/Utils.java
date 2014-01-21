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

package org.wso2.carbon.registry.resource.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.resource.stub.beans.xsd.CollectionContentBean;
import org.wso2.carbon.registry.resource.stub.beans.xsd.ResourceTreeEntryBean;
import org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.registry.relations.stub.beans.xsd.DependenciesBean;
import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationBean;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Utils {

    private static final Log log = LogFactory.getLog(Utils.class);

    public static String[] getSortedChildNodes(CollectionContentBean bean) {
        return getSortedChildNodes(bean.getChildPaths());
    }

    public static String[] getSortedChildNodes(String[] childPaths) {
        if (childPaths == null || childPaths.length == 0) {
            return childPaths;
        }
        Map<String, List<String>> sortedMap = new TreeMap<String, List<String>>();
        for (String childPath : childPaths) {
            List<String> childPathList = sortedMap.get(childPath.toLowerCase());
            if (childPathList == null) {
                childPathList = new LinkedList<String>();
            }
            childPathList.add(childPath);
            sortedMap.put(childPath.toLowerCase(), childPathList);
        }
        List<String> allChildPathList = new LinkedList<String>();
        for (List<String> valueList : sortedMap.values()) {
            for (String value : valueList) {
                allChildPathList.add(value);
            }
        }
        return allChildPathList.toArray(new String[childPaths.length]);
    }

    public static String getResourceContentURL(HttpServletRequest request, String resourcePath) {

        ServletContext context = request.getSession().getServletContext();
        HttpSession session = request.getSession();

        String serverURL = CarbonUIUtil.getServerURL(context, session);
        String serverRoot = serverURL.substring(0, serverURL.length() - "services/".length());
        try {
            resourcePath = URLEncoder.encode(resourcePath, "UTF-8");
        } catch (UnsupportedEncodingException ignored) {
        }
        return serverRoot + "registry/resources?path=" + resourcePath;
    }

    public static String getResourceDownloadURL(HttpServletRequest request, String resourcePath) {

        /*ServletContext context = request.getSession().getServletContext();
        HttpSession session = request.getSession();

        String serverURL = CarbonUIUtil.getServerURL(context, session);
        String serverRoot = serverURL.substring(0, serverURL.length() - "services/".length());
        serverRoot = serverRoot.substring(serverRoot.indexOf("//") + "//".length(), serverRoot.length());
        serverRoot = serverRoot.substring(serverRoot.indexOf("/") + "/".length(), serverRoot.length());
        return "/" + serverRoot + "registry/resourceContent?path=" + resourcePath;*/

        /*String contextRoot = request.getContextPath();

        // We need a context root in the format '/foo/', for this logic to work.
        if (!contextRoot.startsWith(RegistryConstants.PATH_SEPARATOR)) {
            contextRoot = RegistryConstants.PATH_SEPARATOR + contextRoot;
        }
        if (!contextRoot.endsWith(RegistryConstants.PATH_SEPARATOR)) {
            contextRoot = contextRoot + RegistryConstants.PATH_SEPARATOR;
        }

        return contextRoot + "registry/resourceContent?path=" + resourcePath;*/

        // This path needs to be encoded twice so that it is not mis-interpreted in JavaScript.
        resourcePath = resourcePath.replace("&", "%2526");
        return "../../registry/resourceContent?path=" + resourcePath;
    }

    public static String getResourceViewMode(HttpServletRequest request) {
        String mode = request.getParameter("resourceViewMode");
        if (mode == null) {
            mode = "";
        }
        return mode.trim();
    }

    public static String getResourceConsumer(HttpServletRequest request) {
        String consumer = request.getParameter("resourcePathConsumer");
        if (consumer == null) {
            consumer = "";
        }
        return consumer.trim();
    }

    public static String getResourcePath(HttpServletRequest request) {
        String path = request.getParameter("path");
        if (path == null) {
            path = "";
        }
        return path.trim();
    }

    public static String getSynapseRoot(HttpServletRequest request) {
        String path = request.getParameter("synapseroot");
        if (path == null) {
            path = "";
        }
        return path.trim();
    }

    public static String getTargetDivID(HttpServletRequest request) {
        String consumer = request.getParameter("targetDivID");
        if (consumer == null) {
            consumer = "";
        }
        return consumer.trim();
    }

    public static String resolveResourceKey(String completePath, String root) {

        if (completePath == null || "".equals(completePath)) {
            String msg = "Invalid path - Path cannot be null or empty";
            log.error(msg);
            throw new RuntimeException(msg);
        }

        if (root == null || "".equals(root)) {
            return completePath;
        }

        if (!root.endsWith(RegistryConstants.PATH_SEPARATOR)) {
            root += RegistryConstants.PATH_SEPARATOR;
        }

        if (completePath.startsWith(root)) {
            return completePath.substring(root.length(), completePath.length());
        }
        return "";
    }

    public static String buildReference(String resourcePath, ResourceServiceClient client, 
                                        String rootName) {
        if (resourcePath == null || resourcePath.length() == 0 || resourcePath.equals("/")) {
            return rootName;
        }

        if (resourcePath.endsWith("/")) {
            resourcePath = resourcePath.substring(0, resourcePath.length() - 1);
        }

        try {
            String[] parts = resourcePath.split("/");
            parts[0] = "";
            StringBuffer temp = new StringBuffer();
            StringBuffer reference = new StringBuffer(rootName);
            for (int i = 0; i < parts.length - 1; i++) {
                temp.append("/").append(parts[i]);
                ResourceTreeEntryBean resourceEntry = client.getResourceTreeEntry(temp.toString());
                if (resourceEntry.getCollection()) {
                    String childPaths[] = resourceEntry.getChildren();
                    for (int j = 0; j < childPaths.length; j++) {
                        String childName = childPaths[j];
                        if (childName == null || childName.length() == 0) {
                            continue;
                        }
                        if (childName.endsWith("/")) {
                            childName = childName.substring(0, childName.length() - 1);
                        }
                        childName = childName.substring(childName.lastIndexOf("/") + 1);
                        if (childName.equals(parts[i + 1])) {
                            reference.append("_").append(j);
                            break;
                        }
                    }
                } else {
                    return null;
                }
            }
            return reference.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean hasDependencies(DependenciesBean dependenciesBean,String srcPath){
        boolean hasDependencies = false;
            if(srcPath == null){
             return false;
            }

            AssociationBean[] associations = dependenciesBean.getAssociationBeans();
            if(dependenciesBean != null && dependenciesBean.getAssociationBeans().length > 0) {
                for(AssociationBean associationBean: associations) {
                    if("depends".equals(associationBean.getAssociationType())
                            && associationBean.getSourcePath().equals(srcPath) ) {
                        hasDependencies = true;
                        break;
                    }
                }
            }
      return hasDependencies;
    }

    public static String[][] getProperties(HttpServletRequest request) {
        return getProperties(request.getParameter("properties"));
    }

    public static String[][] getProperties(String propertyString) {
        if (propertyString != null && propertyString.trim().length() > 0) {
            String[] keySetWithValues = propertyString.split("\\^\\|\\^");
            String[][] propertyArray = new String[keySetWithValues.length][2];

            for (int i = 0; i < keySetWithValues.length; i++) {
                String keySetWithValue = keySetWithValues[i];
                String[] keyAndValue = keySetWithValue.split("\\^\\^");
                propertyArray[i][0] = keyAndValue[0];
                propertyArray[i][1] = keyAndValue[1];
            }

            return propertyArray;
        }
        return new String[0][];
    }

}
