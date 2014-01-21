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
<%@ page import="org.wso2.carbon.registry.reporting.stub.beans.xsd.ReportConfigurationBean" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="org.wso2.carbon.registry.common.utils.CommonUtil" %>
<%
    try{
        ReportGeneratorClient client = new ReportGeneratorClient(request, config);
        ReportConfigurationBean bean = new ReportConfigurationBean();
        bean.setName(request.getParameter("reportName"));
        String reportTemplate = request.getParameter("reportTemplate");
        if (reportTemplate != null && reportTemplate.length() > 0) {
            bean.setTemplate(reportTemplate);
        }
        String reportType = request.getParameter("reportType");
        if (reportType != null && reportType.length() > 0) {
            bean.setType(reportType);
        }
        String reportClass = request.getParameter("reportClass");
        if (reportClass != null && reportClass.length() > 0) {
            bean.setReportClass(reportClass);
        }
        String attributes = request.getParameter("attributes");
        if (attributes != null && attributes.length() > 0) {
            attributes = attributes.substring(0, attributes.length() - 1);
            String[] attributeStrings = attributes.split("\\^");
            Map<String, String> attributeMap = new HashMap<String, String>();
            for (String temp : attributeStrings) {
                String[] pair = temp.split("\\|");
                attributeMap.put(pair[0].substring("attribute".length()), pair[1]);
            }
            bean.setAttributes(CommonUtil.mapToAttributeArray(attributeMap));
        } else {
            bean.setAttributes(new String[0]);
        }
        client.saveReport(bean);
    } catch (Exception e){
        e.printStackTrace();
        response.setStatus(500);
        return;
    }

%>