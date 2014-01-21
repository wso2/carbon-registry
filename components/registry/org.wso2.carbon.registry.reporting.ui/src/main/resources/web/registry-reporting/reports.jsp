<%--
~  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~  Licensed under the Apache License, Version 2.0 (the "License");
~  you may not use this file except in compliance with the License.
~  You may obtain a copy of the License at
~
~        http://www.apache.org/licenses/LICENSE-2.0
~
~  Unless required by applicable law or agreed to in writing, software
~  distributed under the License is distributed on an "AS IS" BASIS,
~  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
~  See the License for the specific language governing permissions and
~  limitations under the License.
--%>
<%@ page import="org.wso2.carbon.registry.reporting.stub.beans.xsd.ReportConfigurationBean" %>
<%@ page import="org.wso2.carbon.registry.reporting.ui.clients.ReportGeneratorClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.registry.core.utils.RegistryUtils" %>
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<%@ page import="org.wso2.carbon.registry.reporting.ui.utils.Utils" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<link type="text/css" rel="stylesheet" href="css/menu.css"/>
<link type="text/css" rel="stylesheet" href="css/style.css"/>
<link type="text/css" rel="stylesheet" href="../resources/css/registry.css"/>

<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<jsp:include page="../registry_common/registry_common-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
<script type="text/javascript" src="../registry_common/js/registry_common.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<script type="text/javascript" src="../registry-reporting/js/reports.js"></script>

<carbon:jsi18n
        resourceBundle="org.wso2.carbon.registry.reporting.ui.i18n.JSResources"
        request="<%=request%>" namespace="org.wso2.carbon.registry.reporting.ui"/>

<carbon:breadcrumb
        label="registry.reporting.menu"
        resourceBundle="org.wso2.carbon.registry.reporting.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>
<%
    ReportConfigurationBean[] savedReports;
    try{
        ReportGeneratorClient client = new ReportGeneratorClient(request, config);
        savedReports = client.getSavedReports();
    } catch (Exception e){
        response.setStatus(500);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
        return;
    }

%>
<fmt:bundle basename="org.wso2.carbon.registry.reporting.ui.i18n.Resources">
    <div id="middle">
    <h2><fmt:message key="manage.reports"/></h2>
<%
    if (savedReports.length == 0) {
%>
        <div id="workArea">
            <div class="registryWriteOperation">
                <fmt:message
                        key="no.reports.are.currently.defined.click.add.report.to.create.a.new.report"/>
            </div>
            <div class="registryNonWriteOperation">
                <fmt:message
                        key="no.reports.are.currently.defined"/>
            </div>
            <div id="reportOptionTable" class="registryWriteOperation" style="height:20px;margin-top:5px;">

                <a class="icon-link" href="add_report.jsp"
                   style="background-image: url(../admin/images/add.gif);">
                    <fmt:message key="add.report"/>
                </a>
            </div>
        </div>
<%
    } else {
%>      <div id="workArea">
            <table cellpadding="0" border="0" class="styledLeft" id="reportsTable">
                <thead>
                <tr>
                    <th><fmt:message key="report.name"/></th>
                    <th><fmt:message key="report.type"/></th>
                    <th><fmt:message key="report.template"/></th>
                    <th><fmt:message key="actions"/></th>
                </tr>
                </thead>
                <tbody>
<%
        int start;
        int itemsPerPage = (int)(RegistryConstants.ITEMS_PER_PAGE * 1.5);
        int pageNumber;
        int numberOfPages;
        String requestedPage = request.getParameter("requestedPage");
    
        if(requestedPage != null && requestedPage.length()>0){
            pageNumber = new Integer(requestedPage);
        } else{
            pageNumber = 1;
        }
    
        if(savedReports.length % itemsPerPage==0){
            numberOfPages =  savedReports.length / itemsPerPage;
        }
        else{
            numberOfPages =  (savedReports.length / itemsPerPage)+1;
        }
    
        if(savedReports.length < itemsPerPage){
            start = 0;
        }
        else{
            start = (pageNumber - 1) * itemsPerPage;
        }
        ReportConfigurationBean[] pagedReports =
                Utils.getPaginatedReports(start, itemsPerPage, savedReports);
        for (ReportConfigurationBean report : pagedReports) {
%>
                <tr>
                    <td style="width:30%"><a href="edit_report.jsp?reportName=<%=report.getName()%>"><%=report.getName()%></a></td>
                    <td style="width:10%"><%=report.getType() != null ? CarbonUIUtil.geti18nString("report.type." + report.getType().toLowerCase(), "org.wso2.carbon.registry.reporting.ui.i18n.Resources", request.getLocale()) : ""%></td>
                    <td style="width:20%"><a href="../resources/resource.jsp?region=region3&item=resource_browser_menu&viewType=std&path=<%=report.getTemplate()%>"><%=RegistryUtils.getResourceName(
                            report.getTemplate())%></a></td>
                    <td style="width:40%">
                        <a class="copy-icon-link" href="javascript:void(0)" onclick="copyReport('<%=report.getName()%>')" >
                            <fmt:message key="copy"/>
                        </a>&nbsp;
                        <a class="delete-icon-link" href="javascript:void(0)" onclick="deleteReport('<%=report.getName()%>')" >
                            <fmt:message key="delete"/>
                        </a>&nbsp;
                        <a class="icon-link" href="generate_report.jsp?reportName=<%=report.getName()%>"
                                             style="background-image: url(../registry-reporting/images/generate.gif);">
                            <fmt:message key="generate"/>
                        </a>&nbsp;
                        <% if (report.getScheduled()) { %>
                        <a class="icon-link" onclick="stopReport('<%=report.getName()%>')" href="javascript:void(0)"
                           style="background-image: url(../registry-reporting/images/stop.gif);">
                            <fmt:message key="stop"/>
                        </a>
                        <% } else { %>
                        <a class="icon-link" href="schedule_report.jsp?reportName=<%=report.getName()%>"
                           style="background-image: url(../registry-reporting/images/schedule.gif);">
                            <fmt:message key="schedule"/>
                        </a>
                        <% } %>
                    </td>
                </tr>
<%
        }
%>
                </tbody>
            </table>
            <script type="text/javascript">
                alternateTableRows('reportsTable', 'tableEvenRow', 'tableOddRow');
            </script>
            <div id="reportOptionTable" class="registryWriteOperation" style="height:20px;margin-top:5px;">

                <a class="icon-link" href="add_report.jsp"
                   style="background-image: url(../admin/images/add.gif);">
                    <fmt:message key="add.report"/>
                </a>
            </div>
            <carbon:resourcePaginator pageNumber="<%=pageNumber%>" numberOfPages="<%=numberOfPages%>"
                                  resourceBundle="org.wso2.carbon.registry.reporting.ui.i18n.Resources"
                                  nextKey="next" prevKey="prev"
                                  paginationFunction="submitPage(1,{0})"/>
        </div>
<%
    }
%>
    </div>
</fmt:bundle>