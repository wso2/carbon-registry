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
<%@ page import="org.wso2.carbon.registry.resource.ui.processors.RenameResourceProcessor" %>
<%
    try {
        RenameResourceProcessor.process(request, response, config);

    } catch (Exception e) {
        String oldResourcePath = request.getParameter("oldResourcePath");
        String newName = request.getParameter("newName");
        String type = request.getParameter("type");
        if (type == null) {
            type = "resource";
        }
        response.setStatus(500);
        // out.write("Failed to rename " + type + " " + oldResourcePath + " To " + newName);
%>
<script type="text/javascript">
    CARBON.showErrorDialog("<%=e.getMessage()%>");
</script>
<%

return;
    }
%>
