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

<%@ page import="org.wso2.carbon.registry.resource.ui.processors.AddUserPermissionProcessor" %>
<%@ page import="org.wso2.carbon.registry.common.ui.UIException" %>
<fmt:bundle basename="org.wso2.carbon.registry.resource.ui.i18n.Resources">

<%
    String errorMessage = null;
    try {
        AddUserPermissionProcessor.process(request, response, config);
    } catch (UIException e) {
        errorMessage = e.getMessage();
        response.setStatus(500);
    }

    if (errorMessage != null) {
%>

    <fmt:message key="error.adding.permission.message"/>.<br/>
<%=errorMessage%>

<% } %>


</fmt:bundle>