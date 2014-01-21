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
<%@ page import="org.wso2.carbon.registry.reporting.ui.clients.ReportGeneratorClient" %>
<%@ page import="java.util.*" %>
<%@ page import="org.wso2.carbon.registry.common.utils.CommonUtil" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
    List<String> attributes = new ArrayList<String>();
    List<String> mandatoryAttributes = new ArrayList<String>();
    Map<String,String> attributeValues = Collections.emptyMap();
    
    try{
        ReportGeneratorClient client = new ReportGeneratorClient(request, config);
        String reportClass = request.getParameter("reportClass");
        if (reportClass == null || reportClass.length() == 0) {
            return;
        }
        String[] attributeNames = client.getAttributeNames(reportClass);
        if (attributeNames != null && attributeNames.length > 0) {
            attributes.addAll(Arrays.asList(attributeNames));
            String reportName = request.getParameter("reportName");
            if (reportName != null && reportName.length() > 0) {
                // mandatory attributes show up only when you have already created a report. For the first time, everything is optional.
                String[] mandatoryAttributeNames = client.getMandatoryAttributeNames(reportClass);
                if (mandatoryAttributeNames != null && mandatoryAttributeNames.length > 0) {
                    mandatoryAttributes.addAll(Arrays.asList(mandatoryAttributeNames));
                }
                attributeValues = CommonUtil.attributeArrayToMap(client.getSavedReport(reportName).getAttributes());
            }
        }
    } catch (Exception e){
        response.setStatus(500);
        return;
    }
%>
<table id="customTable" name="customTable" style="padding-top:0px !important;margin-top:0px !important;padding-bottom:0px !important;margin-bottom:0px !important" class="normal" cellspacing="0">
<%
    for (String attribute : attributes) {
%>
    <tr>
        <td style="padding-top:0px !important;margin-top:0px !important;padding-bottom:0px !important;margin-bottom:0px !important" class="leftCol-small"><%=attribute%><% if (mandatoryAttributes.contains(attribute)) {%>&nbsp;<span id="attribute<%=attribute%>Required" class="required">*</span><%}%></td>
        <td style="padding-top:0px !important;margin-top:0px !important;padding-bottom:0px !important;margin-bottom:0px !important" ><input type="text" id="attribute<%=attribute%>" name="attribute<%=attribute%>" value="<%=attributeValues.get(attribute) != null ? attributeValues.get(attribute) : ""%>"/></td>
    </tr>
<%
    }
%>
</table>