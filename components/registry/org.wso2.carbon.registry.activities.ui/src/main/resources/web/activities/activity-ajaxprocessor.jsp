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
<%@ page import="org.wso2.carbon.registry.activities.ui.clients.ActivityServiceClient" %>
<%@ page import="org.wso2.carbon.registry.activities.ui.report.beans.ActivityReportBean" %>
<%@ page import="org.wso2.carbon.registry.common.beans.ActivityBean" %>
<%@ page import="org.wso2.carbon.registry.common.ui.UIConstants" %>
<%@ page import="org.wso2.carbon.registry.common.ui.utils.UIUtil" %>
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>

<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.registry.core.pagination.PaginationContext" %>


<fmt:bundle basename="org.wso2.carbon.registry.activities.ui.i18n.Resources">

    <%
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        ActivityServiceClient client = new ActivityServiceClient(cookie, config, session);
        ActivityBean activityBean;
        String requestedPage = request.getParameter(UIConstants.REQUESTED_PAGE);

        int start;
        int count = (int) (RegistryConstants.ITEMS_PER_PAGE * 1.5);
        if (requestedPage != null) {
            start = (int) ((Integer.parseInt(requestedPage) - 1) * (RegistryConstants.ITEMS_PER_PAGE * 1.5));
        } else {
            start = 1;
        }
        PaginationContext.init(start, count, "", "", 1500);
        activityBean = client.getActivities(request);
        String[] allActivities = null;
        String[] activities;
        if (activityBean != null) {
            allActivities = activityBean.getActivity();
        }
        if (allActivities != null && allActivities.length != 0) {
            int itemsPerPage = (int)(RegistryConstants.ITEMS_PER_PAGE * 1.5);

            int pageNumber;
            if (requestedPage != null && requestedPage.length() > 0) {
                pageNumber = new Integer(requestedPage);
            } else {
                pageNumber = 1;
            }

            int rowCount = Integer.parseInt(session.getAttribute("row_count").toString());
            int numberOfPages;
            if (rowCount % itemsPerPage == 0) {
                numberOfPages = rowCount / itemsPerPage;
            } else {
                numberOfPages = rowCount / itemsPerPage + 1;
            }
            activities = allActivities;
    %>
    <%
        if ( CarbonUIUtil.isContextRegistered(config, "/reporting/") && activities!=null && activities.length > 0) {
    %>
            <carbon:report
                    component="org.wso2.carbon.registry.activities"
                    template="Activity Search Report"
                    pdfReport="true"
                    htmlReport="true"
                    excelReport="true"
                    reportDataSession="activitySearchReport"
                    />
                <%
        }
    %>
    <h3><fmt:message key="search.results"/></h3>
    <table cellpadding="0" cellspacing="0" border="0" style="width:100%" class="styledLeft">
        <thead>
        <tr>
            <th align="left"><fmt:message key="activities"/></th>
        </tr>
        </thead>
        <%
            boolean isUserAuthorized = CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/resources/browse");
            for (int i = 0; i < activities.length; i++) {
                String implodedActivity = activities[i];
                if (implodedActivity == null) {
                    continue;
                }
                String[] explodedActivity = implodedActivity.split("\\|");
                if (explodedActivity == null || explodedActivity.length < 7) {
                    continue;
                }

                String activity;
                // If this is a "delete" activity
                if (Boolean.toString(false).equals(explodedActivity[0]) ||
                        explodedActivity[3].trim().contains("has deleted the resource")) {
                    activity = "<a href='#" + explodedActivity[1] + "'>" + explodedActivity[2] + "</a>"
                            + explodedActivity[3] +  explodedActivity[5] + explodedActivity[6];
                }

                // Activities which are not "delete" activities
                else {
                    String path = explodedActivity[4];
                    try {
                        path = URLEncoder.encode(path, "UTF-8");
                    } catch (Exception ignore) {}
                    if(isUserAuthorized){
                        activity = "<a href='../userprofile/index.jsp?username=" +
                                explodedActivity[1] + "&fromUserMgt=false'>" + explodedActivity[2
                                ] + "</a>"
                                + explodedActivity[3] + "<a href='../resources/resource.jsp?region=region3&item=resource_browser_menu&viewType=std&path=" + path + "'>" + explodedActivity[5]
                                + "</a>" + explodedActivity[6];
                    }   else {
                        activity = "<a href='#" + explodedActivity[1] + "'>" + explodedActivity[2] + "</a>"
                                + explodedActivity[3] +  explodedActivity[5]
                                + explodedActivity[6];
                    }

                }
                if (explodedActivity.length > 7 && explodedActivity[7] != null) {
                    activity += "<br/>" + explodedActivity[7];
                }

        %>
        <tr>
            <td><%=activity%>
            </td>
        </tr>
        <%
            }
        %>
        <%
          // Set the Activity search details to session variable "activitySearchReport"
            List<ActivityReportBean> activityBeanList = new ArrayList<ActivityReportBean>();

            for (int j = 0; j < allActivities.length; j++) {

                String aActivityFull = allActivities[j];
                if (aActivityFull == null) {
                    continue;
                }
                String[] activityDetails = aActivityFull.split("\\|");
                if (activityDetails == null || activityDetails.length < 7) {
                    continue;
                }

                ActivityReportBean activityReportBean = new ActivityReportBean();

                activityReportBean.setUserName(activityDetails[1]);
                String tempActivity = activityDetails[3];
                activityReportBean.setActivity(tempActivity.substring(4));
                activityReportBean.setResourcePath(activityDetails[4]);
                String tempTime =  activityDetails[6];

                if(tempActivity.contains("commented on resource"))
                {
                    activityReportBean.setAccessedTime(tempTime.substring(4,tempTime.indexOf("w")));
                }
                else {
                    activityReportBean.setAccessedTime(tempTime);
                }
                activityBeanList.add(activityReportBean);
            }
            request.getSession().setAttribute("activitySearchReport", activityBeanList);

       %>
        <carbon:resourcePaginator pageNumber="<%=pageNumber%>" numberOfPages="<%=numberOfPages%>"
                                  resourceBundle="org.wso2.carbon.registry.activities.ui.i18n.Resources"
                                  nextKey="next" prevKey="prev"
                                  paginationFunction="submitActivityForm(1,{0})" /> 
        <%--<tr>
            <td class="pagingRow" style="text-align:center;padding-top:10px; padding-bottom:10px;">

                <%
                    if (pageNumber == 1) {
                %>
                <span class="disableLink">< Prev</span>
                <%
                } else {
                %>
                <a class="pageLinks" title="<fmt:message key="page.x.to.y"><fmt:param value="<%=(pageNumber - 1)%>"/></fmt:message>"
                   onclick="submitActivityForm(1,<%=(pageNumber-1)%>)"><
                    <fmt:message key="prev"/></a>
                <%
                    }
                    if (numberOfPages <= 10) {
                        for (int pageItem = 1; pageItem <= numberOfPages; pageItem++) { %>

                <a title="<fmt:message key="page.x.to.y"><fmt:param value="<%=pageItem%>"/></fmt:message>" class=<% if(pageNumber==pageItem){ %>"pageLinks-selected"<% } else { %>"pageLinks" <% } %>
                onclick="submitActivityForm(1,<%=pageItem%>)" ><%=pageItem%></a>
                <% }
                } else {
                    // FIXME: The equals comparisons below looks buggy. Need to test whether the desired
                    // behaviour is met, when there are more than ten pages.
                    String place = "middle";
                    int pageItemFrom = pageNumber - 2;
                    int pageItemTo = pageNumber + 2;

                    if (numberOfPages - pageNumber <= 5) place = "end";
                    if (pageNumber <= 5) place = "start";

                    if (place == "start") {
                        pageItemFrom = 1;
                        pageItemTo = 7;
                    }
                    if (place == "end") {
                        pageItemFrom = numberOfPages - 7;
                        pageItemTo = numberOfPages;
                    }

                    if (place == "end" || place == "middle") {


                        for (int pageItem = 1; pageItem <= 2; pageItem++) { %>

                <a class="pageLinks" title="<fmt:message key="page.x.to.y"><fmt:param value="<%=pageItem%>"/></fmt:message>"
                   onclick="submitActivityForm(1,<%=pageItem%>)"><%=pageItem%>
                </a>
                <% } %>
                ...
                <%
                    }

                    for (int pageItem = pageItemFrom; pageItem <= pageItemTo; pageItem++) { %>

                <a title="<fmt:message key="page.x.to.y"><fmt:param value="<%=pageItem%>"/></fmt:message>" class=<% if(pageNumber==pageItem){ %>"pageLinks-selected"<% } else {%>"pageLinks"<% } %>
                onclick="submitActivityForm(1,<%=pageItem%>)"><%=pageItem%></a>
                <% }

                    if (place == "start" || place == "middle") {
                %>
                ...
                <%
                    for (int pageItem = (numberOfPages - 1); pageItem <= numberOfPages; pageItem++) { %>

                <a class="pageLinks" title="<fmt:message key="page.x.to.y"><fmt:param value="<%=pageItem%>"/></fmt:message>"
                   onclick="submitActivityForm(1,<%=pageItem%>)"
                   style="margin-left:5px;margin-right:5px;"><%=pageItem%>
                </a>
                <% }
                }

                    if (place == "middle") {

                    }
                    //End middle display
                }
                    if (pageNumber == numberOfPages) {
                %>
                <span class="disableLink"><fmt:message key="next"/> ></span>
                <%
                } else {
                %>
                <a class="pageLinks" title="<fmt:message key="page.x.to.y"><fmt:param value="<%=(pageNumber - 1)%>"/></fmt:message>"
                   onclick="submitActivityForm(1,<%=(pageNumber+1)%>)">Next
                    ></a>
                <%
                    }
                %>
                <span id="xx<%=pageNumber%>" style="display:none" />
            </td>
        </tr>--%>

    </table>
    <%
    } else {
    %>
    <strong>
        <fmt:message key="no.activities.found"/>.
    </strong>
    <%
        }
    %>
</fmt:bundle>