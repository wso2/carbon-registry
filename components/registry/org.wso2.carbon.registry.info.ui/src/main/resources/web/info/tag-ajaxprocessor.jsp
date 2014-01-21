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
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.registry.common.beans.TagBean" %>
<%@ page import="org.wso2.carbon.registry.common.beans.utils.Tag" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<fmt:bundle basename="org.wso2.carbon.registry.info.ui.i18n.Resources">
<%
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    InfoServiceClient client = new InfoServiceClient(cookie, config, session);
    TagBean tag;
    try {
        client.addTag(request);
        tag = client.getTags(request);
    } catch (Exception e) {
        response.setStatus(500);
        %><%=e.getMessage()%><%
        return;
    }

    Tag[] tags = tag.getTags();

    String content = "";
    if(tags.length > 0){
    for (int i = 0; i < tags.length; i++) {
        Tag tag1 = tags[i];
        String tagName = tag1.getTagName();
        String style = "cloud-x" + tag1.getCategory();
%>
<% if (CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/search/resources")) { %>
<a href="../search/search.jsp?region=region3&item=registry_search_menu&searchType=tag&criteria=<%=tagName%>" class="<%=style%>" onmouseover="showDel('<%=i%>')"><%=tagName%>
<% } else { %>
<a href="javascript:void(0);" class="<%=style%>" onmouseover="showDel('<%=i%>')"><%=tagName%>    
<% } %>
<% if (!tag.isVersionView()) { %>
</a><a class="closeButton registryWriteOperation" onclick="delTag('<%=tagName%>','<%=tag.getPathWithVersion()%>')" id="close<%=i%>" style="display:none" title="<fmt:message key="delete"/>"><img src="../admin/images/delete.gif" style="width:8px"/></a>&nbsp;&nbsp;
<% } %>
<%
    }
    }else{
%>
<div id="noTags" class="summeryStyle">
    <fmt:message key="no.tags.on.this.entry.yet"/>
</div>
<%
    }
%>

<%=content%>
</fmt:bundle>