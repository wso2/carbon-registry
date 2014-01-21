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
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.registry.common.utils.RegistryUtil" %>
<%
    boolean propertiesFound = CarbonUIUtil.isContextRegistered(config, "/properties/");
    //set the tree view session
    session.setAttribute( "viewType", "std" );
%>
<div id="metadataDiv">
    <jsp:include page="metadata_ajaxprocessor.jsp"/>
</div>

<% if (propertiesFound) {
    String propertiesPath = "../properties/properties-main-ajaxprocessor.jsp?path=" + RegistryUtil.getPath(request).replaceAll("&","%26");
%>
<div id="propertiesDiv">
    <jsp:include page="<%=propertiesPath%>"/>
</div>
<% } %>

<div id="contentDiv">
    <jsp:include page="content_ajaxprocessor.jsp"/>
</div>

<div id="permissionsDiv">
    <jsp:include page="permissions_ajaxprocessor.jsp"/>
</div>