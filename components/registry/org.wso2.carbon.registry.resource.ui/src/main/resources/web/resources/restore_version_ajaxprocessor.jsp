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
<%@ page import="org.wso2.carbon.registry.resource.ui.processors.RestoreVersionProcessor" %>
<%@ page import="org.wso2.carbon.registry.resource.ui.Utils" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.net.URLEncoder" %>
<%
    String resourcePath = request.getParameter("path");
    try {
        resourcePath = URLEncoder.encode(resourcePath, "UTF-8");
    } catch (Exception ignore) {}
    String resourceConsumer = Utils.getResourceConsumer(request);
    String viewMode = Utils.getResourceViewMode(request);
    String targetDivID = Utils.getTargetDivID(request);       
    String errorMessage = null;
    try {
        String version = RestoreVersionProcessor.process(request, response, config);

        CarbonUIMessage carbonMessage = new CarbonUIMessage("Successfully restored to version : " + version, CarbonUIMessage.INFO);
        request.getSession().setAttribute(CarbonUIMessage.ID, carbonMessage);

    } catch (Exception e) {
        errorMessage = e.getMessage();
        response.setStatus(500);
        CarbonUIMessage uiMsg = new CarbonUIMessage("Faled to restore the resource.", CarbonUIMessage.ERROR, e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
    }
%>

<% if (errorMessage != null) { %>

<script type="text/javascript">
	location.href='../admin/error.jsp?errorMsg=<%=errorMessage%>'
</script>

<% } %>
<% if ("inlined".equals(viewMode)) { %>
<jsp:forward page="../resources/resource_ajaxprocessor.jsp?path=<%=resourcePath%>&resourceViewMode=<%=viewMode%>&resourcePathConsumer=<%=resourceConsumer%>&targetDivID=<%=targetDivID%>"/>
<% } else { %>
<%-- encoding needs to be done twice due to ajax call in between. --%>
<script type="text/javascript">
	location.href='../resources/resource.jsp?region=region3&item=resource_browser_menu&viewType=std&path=<%=resourcePath%>';
</script>
<% } %>