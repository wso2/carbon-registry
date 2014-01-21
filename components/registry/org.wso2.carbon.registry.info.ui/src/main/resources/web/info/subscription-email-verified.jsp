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
<%@ page import="org.wso2.carbon.registry.info.ui.clients.InfoServiceClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%
    String location = "../admin/index.jsp";
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    InfoServiceClient client;
    String email = null;
    try {
        client = new InfoServiceClient(cookie, config, session);
    } catch (Exception ignore) {
        return;
    }
    if (request.getParameter("failed") == null) {
        if (session.getAttribute("email-verification-error-redirect") != null) {
            session.removeAttribute("email-verification-error-redirect");
        }
        if (session.getAttribute("intermediate-data") != null) {
            try {
                email = client.verifyEmail(request);
            } catch (Exception ignore) {}
            session.removeAttribute("intermediate-data");
        }
    }
    if (email == null) {
%>
<fmt:bundle basename="org.wso2.carbon.registry.info.ui.i18n.Resources">
<script type="text/javascript">
    CARBON.showErrorDialog("<fmt:message key="verfication.of.the.email.address.failed"/>",
            function() {window.location = '<%=location%>';},
            function() {window.location = '<%=location%>';});
</script>
</fmt:bundle>
<%
    } else {
%>
<fmt:bundle basename="org.wso2.carbon.registry.info.ui.i18n.Resources">
<script type="text/javascript">
    CARBON.showInfoDialog("<fmt:message key="the.email.address"/> <%=email%> <fmt:message key="has.been.successfully.verified"/>",
            function() {window.location = '<%=location%>';},
            function() {window.location = '<%=location%>';});
</script>
</fmt:bundle>
<%
    }
%>