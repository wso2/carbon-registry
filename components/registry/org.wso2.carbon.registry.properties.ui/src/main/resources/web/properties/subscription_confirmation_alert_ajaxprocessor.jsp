<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ page import="org.wso2.carbon.registry.properties.stub.beans.xsd.PropertiesBean" %>
<%@ page import="org.wso2.carbon.registry.properties.stub.utils.xsd.Property" %>
<%@ page import="org.wso2.carbon.registry.properties.ui.clients.PropertiesServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.HashMap" %>


<%
    PropertiesServiceClient client_ = new PropertiesServiceClient(config, session);
    PropertiesBean properties = client_.getProperties(request);
    try {
        if (request.getParameter("path") != null) {
            String email = request.getParameter("email");
            for (Property p : properties.getProperties()) {
                if (p.getValue() != null
                        && email != null
                        && p.getValue().equals(email)) {
                     %>----EmailExists----<%
                    return;
                }
            }
        }
    } catch (Exception e) {
        response.setStatus(500);
%>
<%=e.getMessage()%>
<%
        return;
    }
    PropertiesBean propertiesBean_ = client_.getProperties(request);
    if (propertiesBean_ == null) {
        return;
    }
%>