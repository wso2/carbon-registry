<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ page import="org.wso2.carbon.registry.core.utils.RegistryUtils" %>
<%@ page import="java.util.Stack" %>
<%@ page import="org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.registry.resource.stub.beans.xsd.ResourceTreeEntryBean" %>
<%@ page import="org.wso2.carbon.registry.resource.ui.Utils" %>
<%
    String treeNavigationPath = request.getParameter("treeNavigationPath");
    String reference = request.getParameter("reference");
    if ("compute".equals(reference) && treeNavigationPath != null) {
        // From detail to tree view
        try {
            String cookie =
                    (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            ResourceServiceClient client =
                    new ResourceServiceClient(cookie, config, request.getSession());
            reference = Utils.buildReference(treeNavigationPath, client, "treeViewRoot");
        } catch (Exception ignored) {
            // We won't expand the collection, if an error occurs.
        }
        session.setAttribute("treeNavigationPath", treeNavigationPath);
        session.setAttribute("reference", reference);
    } else if (treeNavigationPath != null) {
        session.setAttribute("treeNavigationPath", treeNavigationPath);
        session.setAttribute("reference", reference);
        session.setAttribute( "viewType", "std" );
        return;
    } else if (session.getAttribute("treeNavigationPath") != null) {
        treeNavigationPath = (String)session.getAttribute("treeNavigationPath");
        reference = (String)session.getAttribute("reference");
    } else {
        treeNavigationPath = "/";
        reference = "treeViewRoot";
    }
    //set the tree view session
    session.setAttribute( "viewType", "tree" );
%>
<div>
    <jsp:include page="resource_tree_ajaxprocessor.jsp">
        <jsp:param name="displayTreeNavigation" value="<%=treeNavigationPath%>" />
        <jsp:param name="rootName" value="treeViewRoot" />
    </jsp:include>
    <% if (treeNavigationPath != null && !treeNavigationPath.equals("") && reference != null) {
                Stack<String> pathStack = new Stack<String>();
                String path = treeNavigationPath;
                if (!path.equals("/")) {
                    boolean isCollection = false;
                    try {
                        String cookie =
                                (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
                        ResourceServiceClient client =
                                new ResourceServiceClient(cookie, config, request.getSession());
                        ResourceTreeEntryBean bean = client.getResourceTreeEntry(path);
                        isCollection = bean.getCollection();
                    } catch (Exception ignored) {
                        // We won't expand the collection, if an error occurs.
                    }
                    if (isCollection) {
                        pathStack.push(path);
                    } else if (reference.lastIndexOf("_") > 0) {
                        reference = reference.substring(0, reference.lastIndexOf("_"));
                    }
                    path = RegistryUtils.getParentPath(path);
                }
                while (path != null && !path.equals("/")) {
                    pathStack.push(path);
                    path = RegistryUtils.getParentPath(path);
                }
                int count = 0;
                StringBuffer sb = new StringBuffer();
                sb.append("<script type=\"text/javascript\">\n" +
                          "        loadSubTree('/', 'treeViewRoot', 'null', 'false', function() {");
                int depth = pathStack.size();
                while (!pathStack.empty()) {
                    String temp = pathStack.pop();
                    count++;
                    sb.append("loadSubTree('");
                    sb.append(temp);
                    sb.append("', '");
                    temp = reference;
                    for (int i = count; i < depth; i++) {
                        if (temp.lastIndexOf("_") > 0) {
                            temp = temp.substring(0, temp.lastIndexOf("_"));
                        } else {
                            temp = "treeViewRoot";
                        }
                    }
                    sb.append(temp);
                    sb.append("', 'null', 'false'");
                    if (pathStack.empty()) {
                        sb.append(")");
                    } else {
                        sb.append(", function() {");
                    }
                }
                count--;
                while(count > 0) {
                    count--;
                    sb.append("})");
                }
                sb.append("});\n    </script>");
                %><%=sb.toString()%>
    <% } %>
</div>