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
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.wso2.carbon.registry.common.ui.UIConstants" %>
<%@ page import="org.wso2.carbon.registry.relations.ui.clients.RelationServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationTreeBean" %>

<jsp:include page="../registry_common/registry_common-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
<script type="text/javascript" src="../registry_common/js/registry_common.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<jsp:include page="../relations/relations-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../relations/js/relations.js"></script>

<link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>

    <%
        String BUNDLE = "org.wso2.carbon.registry.relations.ui.i18n.Resources";
        ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        RelationServiceClient client = new RelationServiceClient(cookie, config, session);
        AssociationTreeBean associationTreeViewAction;
        try {
            associationTreeViewAction = client.getAssociationTree(request);
        } catch (Exception e) {
        response.setStatus(500);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
    <jsp:include page="../admin/error.jsp?<%=e.getMessage()%>"/>
<%
        return;
    }

        String errorMessage = (String) request.getSession().getAttribute(UIConstants.ERROR_MESSAGE);
        String associationType = associationTreeViewAction.getAssoType();
        String assoTree = associationTreeViewAction.getAssociationTree();
        if (assoTree == null || assoTree.trim().equals("")) {
            response.setStatus(500);
            return;
        }
        if(associationType.equals("asso")){



            associationType = resourceBundle.getString("association");
        } else if (associationType.equals(UIConstants.ASSOCIATION_TYPE01)){
            associationType = resourceBundle.getString("dependency");
        }
        if (errorMessage != null) {

            if (associationTreeViewAction.getAssoType().equals("asso")) {
                associationType = resourceBundle.getString("association");
            } else if (associationTreeViewAction.getAssoType().equals(UIConstants.ASSOCIATION_TYPE01)) {
                associationType = resourceBundle.getString("dependency");
            }


            request.getSession().setAttribute(UIConstants.ERROR_MESSAGE, null);
        }

        if (errorMessage != null) {
    %>
    <div class="error-message"><%=errorMessage%>
    </div>
    <% } %>

<fmt:bundle basename="org.wso2.carbon.registry.relations.ui.i18n.Resources">
    <div style="margin-top:10px;margin-bottom:15px;margin-left:10px;">
        <strong><fmt:message key="resource.path"/>: <%=associationTreeViewAction.getResourcePath()%></strong>
    </div>
    <div class="resource-tree-headding" style="height:20px;">
        <ul class="tree-row-object">
            <li class="first"><%=associationType%>
            </li>
            <% if (!associationTreeViewAction.getAssoType().equals(UIConstants.ASSOCIATION_TYPE01)) { %>
            <li class="second"><fmt:message key="association.type"/></li>
            <% } %>
        </ul>
    </div>
    <%=assoTree%>
</fmt:bundle>