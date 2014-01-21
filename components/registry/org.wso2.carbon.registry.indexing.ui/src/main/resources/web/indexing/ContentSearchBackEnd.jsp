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
<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>


<%@page import="org.wso2.carbon.ui.CarbonUIUtil"%><jsp:include
	page="../registry_common/registry_common-i18n-ajaxprocessor.jsp" />
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>
<%@page
	import="org.wso2.carbon.registry.indexing.ui.ContentSearchServiceClient"%>
<%@page
	import="org.wso2.carbon.registry.indexing.stub.generated.xsd.SearchResultsBean"%>
<%@page
	import="org.wso2.carbon.registry.indexing.stub.generated.xsd.ResourceData"%>
<%@page import="org.wso2.carbon.registry.common.ui.utils.UIUtil"%>

<!-- This page is included to display messages which are set to request scope or session scope -->
<jsp:include page="../dialog/display_messages.jsp" />

<carbon:jsi18n
	resourceBundle="org.wso2.carbon.registry.indexing.ui.i18n.JSResources"
	request="<%=request%>" />
<script type="text/javascript"
	src="../registry_common/js/registry_validation.js"></script>
<script type="text/javascript"
	src="../registry_common/js/registry_common.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>

<link rel="stylesheet" type="text/css"
	href="../resources/css/registry.css" />


<fmt:bundle
	basename="org.wso2.carbon.registry.indexing.ui.i18n.Resources">
	<div id="middle">

	<h2><fmt:message key="search.results" /></h2>

	<div id="workArea">
	<%-- <a href="../indexing/ContentSearch.jsp"><fmt:message
					key="back.to.content.search" /></a><br/><br/>--%>
	
	<form id="ContentSearchForm" name="ContentSearchForm"
		action="ContentSearch.jsp&submit=true" method="get">
	<table class="styledLeft normal" cellpadding="0" cellspacing="0" border="0">
		<tr class="formRow">
			<td style="width:100px;"><fmt:message key="enter.keywords" /></td>
			<td class="leftCol-small"><input type="text" name="content" value="" id="#_content"
				onkeypress="handletextBoxKeyPress(event)" style="width: 140px;" /></td>
			<td><input class="button" type="submit" value="<fmt:message key="search" />"/></td>
		</tr>
		
	</table>
	</form><br/><br/>
	<%
	if (request.getParameter("submit") != null) {
	if (request.getParameter("content") == null && request.getParameter("content").length() > 0) { 
	%>
		<script type="text/javascript">
		  
		  sessionAwareFunction(function() {
		  
		      CARBON.showWarnignDialog("<fmt:message key="error.enter.some.keywords"/>",function(){
		          return;
		      });
		  
		  }, "<fmt:message key="session.timed.out"/>");
		  // -->
		</script>
		<% 
		return;
	}
		
		String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    	ContentSearchServiceClient client = new ContentSearchServiceClient(cookie, config, session);
    	SearchResultsBean resultBean;
        try {
        	resultBean = client.getSearchResults(request);
        } catch (Exception e) {
        response.setStatus(500);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp?<%=e.getMessage()%>" />
<%
    return;
}

        ResourceData[] resourceDataList = resultBean.getResourceDataList();
        if (resourceDataList.length != 0) {
    %>
	


	<table cellpadding="0" cellspacing="0" border="0" style="width: 100%"
		class="styledLeft">
		<thead>
			<tr>
				<th style="padding-left: 5px; text-align: left;">&nbsp;</th>
				<th style="padding-left: 5px; text-align: left;"><fmt:message
					key="created" /></th>
				<th style="padding-left: 5px; text-align: left;"><fmt:message
					key="author" /></th>
				<th style="padding-left: 5px; text-align: left;"><fmt:message
					key="rating" /></th>
			</tr>
		</thead>
		<tbody>
			<%
            for (int i = 0; i < resourceDataList.length; i++) {
                ResourceData resourceData = resourceDataList[i];

                if (resourceData == null) {
                    continue;    
                }
        %>
			<tr id="1">
				<% if (resourceData.getResourceType().equals("collection")) { %>
				<td style="padding-left: 5px; padding-top: 3px; text-align: left;"><img
					src="images/icon-folder-small.gif" style="margin-right: 5px;"
					align="top" /><a
					href="../resources/resource.jsp?region=region3&item=resource_browser_menu&path=<%=resourceData.getResourcePath()%>"><%=resourceData.getResourcePath()%>
				</a></td>
				<% } %>
				<% if (resourceData.getResourceType().equals("resource")) { %>
				<td style="padding-left: 5px; padding-top: 3px; text-align: left;"><img
					src="images/resource.gif" style="margin-right: 5px;" align="top" /><a
					href="../resources/resource.jsp?region=region3&item=resource_browser_menu&path=<%=resourceData.getResourcePath()%>"><%=resourceData.getResourcePath()%>
				</a></td>
				<% } %>

				<td style="padding-left: 5px; padding-top: 3px; text-align: left;"><nobr><%=resourceData.getFormattedCreatedOn()%></nobr>
				</td>
				<td style="padding-left: 5px; padding-top: 3px; text-align: left;"><%=resourceData.getAuthorUserName()%>
				</td>
				<td style="padding-left: 5px; padding-top: 3px; text-align: left;">
				<div style="width: 140px;"><img
					src="images/r<%=resourceData.getAverageStars()[0]%>.gif" /> <img
					src="images/r<%=resourceData.getAverageStars()[1]%>.gif" /> <img
					src="images/r<%=resourceData.getAverageStars()[2]%>.gif" /> <img
					src="images/r<%=resourceData.getAverageStars()[3]%>.gif" /> <img
					src="images/r<%=resourceData.getAverageStars()[4]%>.gif" /> (<%=resourceData.getAverageRating()%>)
				</div>
				</td>
			</tr>
			<%--<tr id="1-des" style="display:none;">--%>
			<%--<td class="table-description">--%>
			<%--<%=resourceData.getDescription()%>--%>
			<%--</td>--%>
			<%--</tr>--%>

			<% } %>

		</tbody>
	</table>

	<%
    } else {
    %> <strong style="text-align: left;"><fmt:message
		key="your.search.did.not.match.any.resources" /></strong> <%
        }
	}
    %>
	</div>
	</div>
</fmt:bundle>