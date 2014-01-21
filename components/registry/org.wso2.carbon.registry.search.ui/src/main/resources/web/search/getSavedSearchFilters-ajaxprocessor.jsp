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
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.registry.search.ui.clients.SearchServiceClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>


<%
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String[] savedSearchFilterNames = null;
    try {
        SearchServiceClient client = new SearchServiceClient(cookie, config, session);
        savedSearchFilterNames = client.getSavedFilters();
    } catch (Exception e) {
        response.setStatus(500);
%>
<script type="text/javascript">
    CARBON.showErrorDialog("<%=e.getMessage()%>");
</script>
<%
        return;
    }
%>


<%  if (savedSearchFilterNames != null && savedSearchFilterNames.length > 0 &&
            savedSearchFilterNames[0] != null) { %>

        <select name="savedSearchFilterList" id="savedSearchFilterList">

    <%
        for (String filterName : savedSearchFilterNames) {
    %>
            <option value="<%= filterName%>"><%= filterName%></option>
    <%
        }
    %>
        </select>
<script type="text/javascript">
    document.getElementById("filterLoadButton").disabled=false;
    document.getElementById("filterDeleteButton").disabled=false;
</script>
<%
    } else {
%>
        <select name="savedSearchFilterList" id="savedSearchFilterList" disabled="true">
            <option>None</option>
        </select>
        <script type="text/javascript">
            document.getElementById("filterLoadButton").disabled=true;
            document.getElementById("filterDeleteButton").disabled=true;
        </script>
<%
    }
%>