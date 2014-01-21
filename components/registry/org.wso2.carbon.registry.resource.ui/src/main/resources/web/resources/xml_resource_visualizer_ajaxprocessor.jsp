<%--
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
 --%>
<%@ page contentType="application/xml;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<?xml-stylesheet type="text/xsl"
    href="../resources/xslt/annotated-<%=request.getParameter("type")%>.xsl"?>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient" %>
<%@ page import="org.wso2.carbon.registry.core.utils.RegistryUtils" %>

    <%
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        String textContent;
        try {
            ResourceServiceClient client = new ResourceServiceClient(cookie, config, session);
            textContent = client.getTextContent(request);
        } catch (Exception e) {
            return;
        }
%>
<% 
    textContent = textContent.replace("&", "&amp;").replaceAll("<[?]xml[^?]*[?]>", "").replaceAll(
            "([import|include|redefine|override][^>]*[ ]schemaLocation=[\"])([^\"]*)([\"])", 
            "$1../resources/xml_resource_visualizer_ajaxprocessor.jsp?rootPath=" +
            RegistryUtils.getParentPath(request.getParameter("rootPath")) + "&amp;path=" +
            RegistryUtils.getParentPath(request.getParameter("path")) + "/$2&amp;type=xsd$3").replaceAll(
            "(import[^>]*[ ]location=[\"])([^\"]*)([\"])",
            "$1../resources/xml_resource_visualizer_ajaxprocessor.jsp?rootPath=" +
            RegistryUtils.getParentPath(request.getParameter("rootPath")) + "&amp;path=" +
            RegistryUtils.getParentPath(request.getParameter("path")) + "/$2&amp;type=wsdl$3");
    while (textContent.indexOf("/../") > 0) {
        textContent = textContent.replaceAll("/[^/.]*/[.][.]/", "/");
    }
%>
<%=textContent%>
