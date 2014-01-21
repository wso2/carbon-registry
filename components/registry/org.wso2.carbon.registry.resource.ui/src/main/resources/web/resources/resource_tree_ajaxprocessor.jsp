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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.wso2.carbon.core.RegistryResources" %>
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<%@ page import="org.wso2.carbon.registry.resource.ui.Utils" %>
<%@ page import="org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.Stack" %>
<%@ page import="org.wso2.carbon.registry.core.utils.RegistryUtils" %>
<%@ page import="org.wso2.carbon.registry.resource.stub.beans.xsd.ResourceTreeEntryBean" %>
<%
    String viewMode = Utils.getResourceViewMode(request);
    String resourceConsumer = Utils.getResourceConsumer(request);
    String errorMessage = null;
    String textBoxId = request.getParameter("textBoxId");
    String treeNavigationPath = request.getParameter("displayTreeNavigation");
    String rootName = request.getParameter("rootName");
    boolean relativeRoot = request.getParameter("relativeRoot") != null;
    if (rootName == null || rootName.length() == 0) {
        rootName = "root";
    }
    Object expansionPathObj = session.getAttribute("resource-tree-expansion-path");
    String expansionPath = null;
    if (expansionPathObj != null && expansionPathObj instanceof String) {
        expansionPath = (String)expansionPathObj;
    }
    boolean displayTreeNavigation = treeNavigationPath != null;
    String onOKCallback = request.getParameter("onOKCallback");
    String synapseRegistryMetaDataRootPath =
             RegistryResources.ROOT + "esb"
            + RegistryConstants.PATH_SEPARATOR + "registry";
    String synapseRegistryRoot = "";
    boolean displayResourceTree = false;
    String rootPath = request.getParameter("rootPath");
    if (rootPath == null) {
        rootPath = "/";
    }
    String displayRootPath = request.getParameter("displayRootPath");
    String displayPath;
    if ("true".equals(displayRootPath)) {
    	displayPath = "/_system/governance";
    } else {
    	displayPath = rootPath;
    }
    
    try {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        ResourceServiceClient client = new ResourceServiceClient(cookie, config, session);
        synapseRegistryRoot = client.getProperty(synapseRegistryMetaDataRootPath, "SYNAPSE_REGISTRY_ROOT");
        if (client.getResourceTreeEntry(rootPath) != null) {
            displayResourceTree = true;
        }
    } catch (Exception ignored) {
    }
%>
<!-- other includes -->
<div style="display:none">&nbsp;</div><!-- This div is to fix a ie issue. please dont remove it-->
<jsp:include page="../registry_common/registry_common-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
<script type="text/javascript" src="../registry_common/js/registry_common.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<script src="../global-params.js" type="text/javascript"></script>
<link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>


    <% if (errorMessage != null) { %>

    <script type="text/javascript">
        location.href = '../admin/error.jsp?errorMsg=<%=errorMessage%>';
    </script>

    <% } else {
            if (!displayTreeNavigation) {%>
    <fmt:bundle basename="org.wso2.carbon.registry.resource.ui.i18n.Resources">
    <div class="headding-tree-back">
        <h1 class="headding-tree"><fmt:message key="resource.paths"/></h1>
    </div>
    <div id="local-registry-placeholder" style="display:none"></div>
    <div class="resource-tree-headding">
        <fmt:message key="picked.path"/> : <input type="text" id="pickedPath"
                                                class="picked-path-textbox" <%= (expansionPath != null) ? "value=\"" + expansionPath + "\"" : ""%> style="width:500px;"
                                                onfocus="setResolvedResourcePathOnConsumer('<%=resourceConsumer%>','<%=synapseRegistryRoot%>');" onchange="setResolvedResourcePathOnConsumer('<%=resourceConsumer%>','<%=synapseRegistryRoot%>')"/>
                                            <input type="button" class="button" value="<fmt:message key="ok"/>"
                                                   onclick="if ((typeof(isMediationLocalEntrySelected) == undefined )|| !isMediationLocalEntrySelected) { if (validateResoucePath()) { handle<%= relativeRoot ? "Relative" : "" %>WindowOk(<%= relativeRoot ? "'" + displayPath + "', " : "" %>'<%=textBoxId%>'<%= onOKCallback != null ? ", " + onOKCallback : ""%>); CARBON.closeWindow(); return true; } return false; }; CARBON.closeWindow(); return true;" />
    </div>
    </fmt:bundle>    
    <%      } %>
    <div class="resource-tree-box" <% if (displayTreeNavigation) { %>style='height:100% !important;'<% }%>>
<%
    if (displayResourceTree) {
%>
        <div id="father_<%=rootName%>" class="father-object">
<%
    boolean hideResources = request.getParameter("hideResources") != null;

    if (expansionPath == null) {
%>
            <a onclick="loadSubTree('<%=rootPath%>', '<%=rootName%>', '<%=textBoxId%>', '<%=(hideResources? "true" : "false")%>');">
                <img style="margin-right: 5px;" id="plus_<%=rootName%>" src="../resources/images/icon-tree-plus.jpg"/>
                <img style="display:none; margin-right: 5px;" id="minus_<%=rootName%>" src="../resources/images/icon-tree-minus.jpg"/></a>
            <a onclick="pickPath('<%=rootPath%>', '<%=textBoxId%>', '<%=rootName%>');"><img style="margin-right: 2px;"
                                                                                            src="../resources/images/icon-folder-small.gif"/><%=rootPath%></a>
        </div>
        <div id="child_<%=rootName%>" class="child-objects"></div>
<%
    } else {
        session.removeAttribute("resource-tree-expansion-path");
        ResourceServiceClient client;
        String reference;
        try {
            String cookie =
                    (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            client = new ResourceServiceClient(cookie, config, request.getSession());
            reference = Utils.buildReference(expansionPath, client, rootName);
        } catch (Exception ignored) {
            ignored.printStackTrace();
            return;
        }
        if (!expansionPath.equals("")) {
            Stack<String> pathStack = new Stack<String>();
            String path = expansionPath;
            if (!path.equals("/")) {
                boolean isCollection = false;
                try {
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
            sb.append("loadSubTree('/', '" + rootName +
                    "', '" + textBoxId +"', 'false', function() {");
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
                        temp = rootName;
                    }
                }
                sb.append(temp);
                sb.append("', '" + textBoxId +"', 'false'");
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
            sb.append("});");
%>
        <a onclick="<%=sb.toString()%>">
            <img style="margin-right: 5px;" id="plus_<%=rootName%>" src="../resources/images/icon-tree-plus.jpg"/>
            <img style="display:none; margin-right: 5px;" id="minus_<%=rootName%>" src="../resources/images/icon-tree-minus.jpg"/></a>
        <a onclick="pickPath('<%=rootPath%>', '<%=textBoxId%>', '<%=rootName%>');"><img style="margin-right: 2px;"
                                                                                        src="../resources/images/icon-folder-small.gif"/><%=rootPath%></a>
    </div>
    <div id="child_<%=rootName%>" class="child-objects"></div>

        <%
        }
        }
        }
%>
    </div>
    <% }
    %>
