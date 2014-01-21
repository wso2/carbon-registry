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
        label="add.report"
        resourceBundle="org.wso2.carbon.registry.reporting.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

<fmt:bundle basename="org.wso2.carbon.registry.reporting.ui.i18n.Resources">
    <div id="middle">
        <h2><fmt:message key="add.report"/></h2>
        <div id="workArea">
            <div id="add-report-div" style="padding-bottom:10px;">
                <form id="reportForm" name="reportForm" action="" method="POST">
                    <table class="styledLeft">
                        <thead>
                        <tr>
                            <th><fmt:message key="add.new.report"/></th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td class="formRow">
                                <table class="normal" cellspacing="0">
                                    <tr>
                                        <td class="leftCol-small" ><fmt:message key="report.name"/>&nbsp;<span class="required">*</span></td>
                                        <td>
                                            <input type="text" id="reportName" name="reportName" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="leftCol-small" ><fmt:message key="report.template"/>&nbsp;<span class="required">*</span></td>
                                        <td>
                                            <input type="text" id="reportTemplate" name="reportTemplate"/>
                                            <input type="button" class="button"
                                                   value=".." title="<fmt:message key="select.path"/>"
                                                   onclick="showResourceTree('reportTemplate');"/>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="leftCol-small" ><fmt:message key="report.type"/></td>
                                        <td>
                                            <select name="text" id="reportType" name="reportType">
                                                <option value="PDF"><fmt:message key="report.type.pdf"/></option>
                                                <option value="Excel"><fmt:message key="report.type.excel"/></option>
                                                <option value="HTML"><fmt:message key="report.type.html"/></option>
                                            </select>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="leftCol-small" ><fmt:message key="report.class"/>&nbsp;<span class="required">*</span></td>
                                        <td>
                                            <input type="text" id="reportClass" name="reportClass" />
                                            <input type="button" class="button"
                                                   value="<fmt:message key="load.attributes"/>" title="<fmt:message key="load.attributes"/>"
                                                   onclick="loadAttributes($('reportClass').value, '', false);"/>
                                        </td>
                                    </tr>
                                </table>
                                    <div id="report-attributes-div"></div>
                            </td>
                        </tr>
                        <tr>
                            <td class="buttonRow">
                                <input type="button" id="addReportButton" class="button registryWriteOperation" value="<fmt:message key="add"/>"
                                       onclick="addReport();"/>&nbsp;
                                <input type="button"
                                       class="button"
                                       value="<fmt:message key="cancel"/>"
                                       onclick="window.location = '../registry-reporting/reports.jsp?region=region3&item=registry_reporting_menu'"/>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </form>
            </div>
        </div>
    </div>
</fmt:bundle>