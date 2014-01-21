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
<%@ page import="org.wso2.carbon.registry.info.ui.clients.InfoServiceClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %><%
    String location = "../admin/index.jsp";
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    try {
        // Create an instance to make sure that the session is live.
        new InfoServiceClient(cookie, config, session);
    } catch (Exception ignore) {
        return;
    }
    if (request.getParameter("confirmation") != null) {
        session.setAttribute("email-verification-error-redirect",
                "../info/subscription-email-verified.jsp?failed=failed");
        location = "../email-verification/validator_ajaxprocessor.jsp?confirmation=" +
                request.getParameter("confirmation");
    }
%>
<script type="text/javascript">
    window.location = '<%=location%>';
</script>