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
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<%@ page import="org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient" %>
<%@ page import="org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean" %>
<%@ page import="org.wso2.carbon.registry.search.stub.common.xsd.ResourceData" %>
<%@ page import="org.wso2.carbon.registry.search.ui.clients.SearchServiceClient" %>
<%@ page import="org.wso2.carbon.registry.search.ui.report.beans.MetaDataReportBean" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.registry.core.pagination.PaginationContext" %>


<%
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    AdvancedSearchResultsBean advancedSearchBean;
    String requestedPage = request.getParameter(UIConstants.REQUESTED_PAGE);
    try {

        int start;
        int count = (int) (RegistryConstants.ITEMS_PER_PAGE * 1.5);
        if (requestedPage != null) {
            start = (int) ((Integer.parseInt(requestedPage) - 1) * (RegistryConstants.ITEMS_PER_PAGE * 1.5));
        } else {
            start = 1;
        }
        PaginationContext.init(start, count, "", "", 1500);
        SearchServiceClient client = new SearchServiceClient(cookie, config, session);
        advancedSearchBean = client.getAdvancedSearchResults(request);

    } catch (Exception e) {
        response.setStatus(500);
%>
<script type="text/javascript">
    CARBON.showErrorDialog("<%=e.getMessage()%>");
</script>
<%
        return;
    }
    ResourceData[] resourceDataList;
    resourceDataList = advancedSearchBean.getResourceDataList();
    int itemsPerPage = (int) (RegistryConstants.ITEMS_PER_PAGE * 1.5);
    int pageNumber = 1;
    int numberOfPages;

    if (requestedPage != null && requestedPage.length() > 0) {
        pageNumber = new Integer(requestedPage);
    }

    int rowCount = Integer.parseInt(session.getAttribute("row_count").toString());
    if (rowCount % itemsPerPage == 0) {
        numberOfPages = rowCount / itemsPerPage;
    } else {
        numberOfPages = rowCount / itemsPerPage + 1;
    }



    boolean resourceExists = false;
    ResourceServiceClient client;
    try {
        client = new ResourceServiceClient(config, session);
        String loginUser = request.getSession().getAttribute("logged-user").toString();
        try {
            client.getResourceTreeEntry("/_system/config/users/" + loginUser + "/searchFilters");
        } catch (Exception ignored1) {
            try {
                client.getResourceTreeEntry("/_system/config/users/" + loginUser);
            } catch (Exception ignored2) {
                client.getResourceTreeEntry("/_system/config/users");
            }
        }
        resourceExists = true;
    } catch (Exception ignored) {

    }
%>
<fmt:bundle basename="org.wso2.carbon.registry.search.ui.i18n.Resources">
    <%
        if (CarbonUIUtil.isContextRegistered(config, "/reporting/") && resourceDataList != null && resourceDataList.length > 0) {
    %>
    <carbon:report
            component="org.wso2.carbon.registry.search"
            template="MetaDataReportTemplate"
            pdfReport="true"
            htmlReport="true"
            excelReport="true"
            reportDataSession="metaDataSearchReport"
            />
    <%
        }
    %>


    <h3 style="margin-top:20px;margin-bottom:20px;"> <fmt:message key="search.results"/> </h3>

    <table cellpadding="0" cellspacing="0" border="0" style="width:100%" class="styledLeft">
        <thead>
        <tr>
            <th style="padding-left:5px;text-align:left;">&nbsp;</th>
            <th style="padding-left:5px;text-align:left;"><fmt:message key="created"/></th>
            <th style="padding-left:5px;text-align:left;"><fmt:message key="author"/></th>
            <th style="padding-left:5px;text-align:left;"><fmt:message key="rating"/></th>
        </tr>
        </thead>
        <tbody>
        <%

            if (resourceDataList != null && resourceDataList.length >0) {
        %>
        <%
            for (ResourceData resourceData : resourceDataList) {
                if (resourceData == null) {
                    continue;
                }
                String tempPath = resourceData.getResourcePath();
                try {
                    tempPath = URLEncoder.encode(tempPath, "UTF-8");
                } catch (Exception ignore) {
                }
        %>
        <tr id="1">
            <% if (resourceData.getResourceType().equals("collection")) { %>
            <td style="padding-left:5px;padding-top:3px;text-align:left;"><img
                    src="images/icon-folder-small.gif" style="margin-right:5px;" align="top"/>
                <% if (CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/resources/browse")) {%>
                <a onclick="directToResource('../resources/resource.jsp?region=region3&item=resource_browser_menu&viewType=std&path=<%=tempPath%>')"
                   href="#"><%=resourceData.getResourcePath()%>
                </a>
                <% } else { %>
                <%=resourceData.getResourcePath()%>
                <% } %>
            </td>
            <% } %>
            <% if (resourceData.getResourceType().equals("resource")) { %>
            <td style="padding-left:5px;padding-top:3px;text-align:left;"><img
                    src="images/resource.gif" style="margin-right:5px;" align="top"/>
                <% if (CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/resources/browse")) {%>
                <a onclick="directToResource('../resources/resource.jsp?region=region3&item=resource_browser_menu&viewType=std&path=<%=tempPath%>')"
                   href="#"><%=resourceData.getResourcePath()%>
                </a>
                <% } else { %>
                <%=resourceData.getResourcePath()%>
                <% } %>
            </td>
            <% } %>

            <td style="padding-left:5px;padding-top:3px;text-align:left;">
                <nobr><%=resourceData.getFormattedCreatedOn()%>
                </nobr>
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

        <% } %>
        <!--Setting Metadata search results to metaDataSearchReportBean-->
        <%
            List<MetaDataReportBean> searchReportBeanList = new ArrayList<MetaDataReportBean>();


            for (ResourceData resourceDataFull : resourceDataList) {
                if (resourceDataFull == null) {
                    continue;
                }
                MetaDataReportBean metaDataSearchReportBean = new MetaDataReportBean();

                metaDataSearchReportBean.setAuthorName(resourceDataFull.getAuthorUserName());
                metaDataSearchReportBean.setResourcePath(resourceDataFull.getResourcePath());
                metaDataSearchReportBean.setCreatedDate(resourceDataFull.getFormattedCreatedOn());
                metaDataSearchReportBean.setAverageRating(Float.toString(resourceDataFull.getAverageRating()));

                searchReportBeanList.add(metaDataSearchReportBean);
            }
            request.getSession().setAttribute("metaDataSearchReport", searchReportBeanList);

        %>
        <carbon:resourcePaginator pageNumber="<%=pageNumber%>" numberOfPages="<%=numberOfPages%>"
                                  resourceBundle="org.wso2.carbon.registry.search.ui.i18n.Resources"
                                  nextKey="next" prevKey="prev" tdColSpan="4"
                                  paginationFunction="submitAdvSearchForm({0})" />
        <%
        } else {
        %>
        <tr id="1">
            <td style="padding-left:5px;padding-top:3px;text-align:left;">
                <strong><fmt:message key="your.search.did.not.match.any.resources"/></strong>
            </td>
        </tr>
        <%
            }
        %>

        <tr>
            <td colspan="4">

                <div class="search-subtitle" style="padding-left:10px;padding-bottom:10px"><fmt:message
                        key="save.search"/></div>
                <div style="padding-left:10px;color:#666666;font-style:italic;"><fmt:message
                        key="search.save.txt"/></div>


                <form id="saveAdvancedSearchForm" name="saveAdvancedSearch" action=""
                      method="get">
                    <table class="normal">
                        <tr>
                            <td class="leftCol-small"><fmt:message key="filter.name"/></td>
                            <td>
                                <input type="text" name="saveFilterName" id="#_saveFilterName"
                                       onkeypress="handletextBoxKeyPress(event)"/>
                            </td>
                            <td>
                                <input type="button" id="#_clicked"
                                       value="<fmt:message key="save"/>" class="button"
                                       onclick="submitSaveSearchForm()"/>
                            </td>
                        </tr>
                    </table>
                </form>
            </td>
        </tr>

        </tbody>
    </table>
</fmt:bundle>