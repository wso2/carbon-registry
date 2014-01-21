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
<%@ page import="org.wso2.carbon.registry.relations.ui.clients.RelationServiceClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.registry.relations.stub.beans.xsd.DependenciesBean" %>
<%@ page import="org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationBean" %>

<jsp:include page="../registry_common/registry_common-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
<script type="text/javascript" src="../registry_common/js/registry_common.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<jsp:include page="../relations/relations-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../relations/js/relations.js"></script>

<%
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    RelationServiceClient client = new RelationServiceClient(cookie, config, session);
    DependenciesBean bean;
    try {
        bean = client.getDependencies(request);
    } catch (Exception e) {
        return;
    }

    String type = request.getParameter("type");
    String source = request.getParameter("path");
    String destination = request.getParameter("destination");
    
    AssociationBean[] assocs = bean.getAssociationBeans();

    if (assocs != null) {
        for (AssociationBean assoc: assocs) {
            if ((source == null || source.equals(assoc.getSourcePath())) &&
                    (destination == null || destination.equals(assoc.getDestinationPath())) &&
                    (type == null || type.equals(assoc.getAssociationType()))) {
                %>----AssocationExists----<%
                return;
            }
        }
    }
%>