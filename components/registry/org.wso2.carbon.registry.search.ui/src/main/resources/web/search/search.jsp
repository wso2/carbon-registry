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
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%@ page import="org.wso2.carbon.registry.common.ui.UIConstants" %>
<%@ page import="org.wso2.carbon.registry.search.stub.beans.xsd.SearchResultsBean" %>
<%@ page import="org.wso2.carbon.registry.search.ui.clients.SearchServiceClient" %>
<%@ page import="org.wso2.carbon.registry.search.stub.common.xsd.ResourceData" %>
<%@ page import="org.wso2.carbon.registry.search.stub.common.xsd.TagCount" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.registry.common.ui.utils.UIUtil" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<%@ page import="java.net.URLEncoder" %>
<script type="text/javascript" src="../search/js/search.js"></script>
<link rel="stylesheet" type="text/css"
      href="../resources/css/registry.css"/>
    <carbon:breadcrumb label="search.results"
                       resourceBundle="org.wso2.carbon.registry.search.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>
<style type="text/css">
    ul.elementList{
        margin:5px 10px;
    }
    ul.elementList li{
        list-style-type:circle;
    }
</style>
    <div id="middle">
        <fmt:bundle basename="org.wso2.carbon.registry.search.ui.i18n.Resources">
        <h2><fmt:message key="search.results"/></h2>
        </fmt:bundle>
        <div id="workArea">
            <%
                String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
                SearchResultsBean searchBean;
                try {
                    SearchServiceClient client = new SearchServiceClient(cookie, config, session);
                    searchBean = client.getSearchResults(request);
                } catch (Exception e) {
                    response.setStatus(500);
                    CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
                    session.setAttribute(CarbonUIMessage.ID, uiMsg);
            %>
            <jsp:include page="../admin/error.jsp?<%=e.getMessage()%>"/>
            <%
                    return;
                }
%>
<fmt:bundle basename="org.wso2.carbon.registry.search.ui.i18n.Resources">
<%
                ResourceData[] searchResults = searchBean.getResourceDataList();

                if (searchResults.length == 0) {
            %>
            <table class="styledLeft">
                <tr>
                    <td>
                        <div style="margin-top: 10px;">
                            <strong>
                                <fmt:message key="your.search.did.not.match"/>
                                &quot;<%=request.getParameter("criteria")%>&quot;
                            </strong>
                            <div style="margin-top: 10px">
                                <fmt:message key="please.retry.with.following"/>
                            </div>
                        </div>
                        <ul class="elementList" style="margin-left: 20px">
                            <li><fmt:message key="make.sure.all.words.are.spelled.correctly"/></li>
                            <li><fmt:message key="try.different.keywords"/></li>
                            <li><fmt:message key="try.more.general.keywords"/></li>
                            <li><fmt:message key="try.fewer.keywords"/></li>
                        </ul>
                    </td>
                </tr>
            </table>
            <% } %>

            <table cellpadding="0" cellspacing="0" border="0" class="styledLeft" style="width:100%">
                <%
                    int pageNumber;
                    int numberOfPages;
                    int itemsPerPage = (int)(RegistryConstants.ITEMS_PER_PAGE * 0.7);
                    if (searchResults.length != 0) {
                        String pageStr = request.getParameter("page");
                        if (pageStr != null) {
                            pageNumber = Integer.parseInt(pageStr);
                        } else {
                            pageNumber = 1;
                        }
                        if (searchResults.length % itemsPerPage == 0) {
                            numberOfPages = searchResults.length / itemsPerPage;
                        } else {
                            numberOfPages = searchResults.length / itemsPerPage + 1;
                        }
                %>
                <thead>
                <tr>
                    <th style="padding-left:5px;text-align:left;"><fmt:message key="resource"/></th>
                    <th style="padding-left:5px;text-align:left;"><fmt:message
                            key="created.date"/></th>
                    <th style="padding-left:5px;text-align:left;"><fmt:message key="author"/></th>
                    <th style="padding-left:5px;text-align:left;"><fmt:message key="rating"/></th>
                </tr>
                </thead>
                <%

                    for (int i = (pageNumber - 1) * itemsPerPage;
                         i < pageNumber * itemsPerPage && i < searchResults.length; i++) {
                        ResourceData resourceData = searchResults[i];
                        String resourcePath = resourceData.getResourcePath();
                        try {
                            resourcePath = URLEncoder.encode(resourcePath, "UTF-8");
                        } catch (Exception ignore) {}
                %>
                <tr id="1">
                    <% if (resourceData.getResourceType().equals("collection")) { %>
                    <td style="padding-left:5px;padding-top:3px;text-align:left;"><img
                            src="images/icon-folder-small.gif" style="margin-right:5px;" align="top"/>
                        <% if (CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/resources/browse")) {%>
                        <a href="../resources/resource.jsp?region=region3&item=resource_browser_menu&viewType=std&path=<%=resourcePath%>"><%=resourceData.getResourcePath()%></a>
                        <% } else { %>
                        <%=resourceData.getResourcePath()%>
                        <% } %>
                    </td>
                    <% } %>
                    <% if (resourceData.getResourceType().equals("resource")) { %>
                    <td style="padding-left:5px;padding-top:3px;text-align:left;"><img
                            src="images/resource.gif" style="margin-right:5px;" align="top"/>
                        <% if (CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/resources/browse")) {%>
                        <a href="../resources/resource.jsp?region=region3&item=resource_browser_menu&viewType=std&path=<%=resourcePath%>"><%=resourceData.getResourcePath()%></a>
                        <% } else { %>
                        <%=resourceData.getResourcePath()%>
                        <% } %>
                    </td>                   <% } %>
                    <td style="padding-left:5px;padding-top:3px;text-align:left;"><%=resourceData.getFormattedCreatedOn()%>
                    </td>
                    <td style="padding-left:5px;padding-top:3px;text-align:left;"><%=resourceData.getAuthorUserName()%>
                    </td>
                    <td style="padding-left:5px;padding-top:3px;text-align:left;">
                    <div style="width:140px;">
                        <img src="images/r<%=resourceData.getAverageStars()[0]%>.gif"/>
                        <img src="images/r<%=resourceData.getAverageStars()[1]%>.gif"/>
                        <img src="images/r<%=resourceData.getAverageStars()[2]%>.gif"/>
                        <img src="images/r<%=resourceData.getAverageStars()[3]%>.gif"/>
                        <img src="images/r<%=resourceData.getAverageStars()[4]%>.gif"/>
                        (<%=resourceData.getAverageRating()%>)
                    </div>
                    </td>
                </tr>
                <tr>
                    <td colspan="4">
                        &nbsp;
                        <%
                            if (!"Content".equalsIgnoreCase(request.getParameter("searchType"))) {
                                TagCount[] tagCounts = resourceData.getTagCounts();

                                for (int j = 0; j < tagCounts.length; j++) {
                                    String tag = tagCounts[j].getKey();
                                    String count = String.valueOf(tagCounts[j].getValue());
                                    if (((Long) tagCounts[j].getValue()).longValue() > 0) {
                        %>

                        <%=tag%> (<%=count%>) <% if (j != tagCounts.length - 1) { %> | <% } %>

                        <% }
                        }
                        }%>


                    </td>
                </tr>

                <% }  %>
            </table>
            <table width="100%" style="text-align:center; padding-top: 10px; margin-bottom: -10px">
                <carbon:resourcePaginator pageNumber="<%=pageNumber%>" numberOfPages="<%=numberOfPages%>"
                                          resourceBundle="org.wso2.carbon.governance.list.ui.i18n.Resources"
                                          nextKey="next" prevKey="prev"
                                          paginationFunction="loadPagedList({0})" />
                <% } else {
                %>
                <tr>
                    <td colspan="4" style="border:none;"></td>

                </tr>

                <%
                    }
                %>
            </table>
            <% if (CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/search/advanced-search")) { %>
            <div style="margin-top:10px;">
                <a href="../search/advancedSearch.jsp?region=region3&item=registry_search_menu" class="icon-link"
                   style="background-image:url(./images/search.gif);">
                    <fmt:message key="try.advanced.search"/>
                </a>
            </div>
            <% } %>
            <br/>
            <br/>
</fmt:bundle>
        </div>
    </div>
<script type="text/javascript">
    alternateTableRows('customTable','tableEvenRow','tableOddRow');

    function loadPagedList(page) {
        window.location = '<%="../search/search.jsp?region=region3&item=registry_search_menu&searchType=tag&criteria=" + request.getParameter("criteria")%>';
    }
</script>


