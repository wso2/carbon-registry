/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.registry.reporting.ui.clients;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.common.utils.CommonUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.reporting.stub.beans.xsd.ReportConfigurationBean;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

public class ReportGeneratorServlet extends HttpServlet {

    private static final Log log = LogFactory.getLog(ReportGeneratorServlet.class);

    private ServletConfig servletConfig;

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        this.servletConfig = servletConfig;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            getContent(request, response, servletConfig);
        } catch (Exception e) {
            String msg = "Failed to generate report content.";
            log.error(msg, e);
            response.setStatus(500);
        }
    }

    public static void getContent(HttpServletRequest request, HttpServletResponse response,
                                  ServletConfig config)
            throws Exception {

        try {
            ReportGeneratorClient client = new ReportGeneratorClient(request, config);
            ReportConfigurationBean bean =
                    client.getSavedReport(request.getParameter("reportName"));
            String reportTemplate = request.getParameter("reportTemplate");
            if (reportTemplate != null) {
                bean.setTemplate(reportTemplate);
            }
            String reportType = request.getParameter("reportType");
            if (reportType != null) {
                bean.setType(reportType);
            }
            String reportClass = request.getParameter("reportClass");
            if (reportClass != null) {
                bean.setReportClass(reportClass);
            }
            String attributes = request.getParameter("attributes");
            if (attributes != null && attributes.length() > 0) {
                Map<String, String> attributeMap =
                        CommonUtil.attributeArrayToMap(bean.getAttributes());
                attributes = attributes.substring(0, attributes.length() - 1);
                String[] attributeStrings = attributes.split("\\^");
                for (String temp : attributeStrings) {
                    String[] pair = temp.split("\\|");
                    attributeMap.put(pair[0].substring("attribute".length()), pair[1]);
                }
                bean.setAttributes(CommonUtil.mapToAttributeArray(attributeMap));
            }

            response.setDateHeader("Last-Modified", new Date().getTime());
            String extension;
            String mediaType;
            if (bean.getType().toLowerCase().equals("pdf")) {
                mediaType = "application/pdf";
                extension = ".pdf";
            } else if (bean.getType().toLowerCase().equals("excel")) {
                mediaType = "application/vnd.ms-excel";
                extension = ".xls";
            } else if (bean.getType().toLowerCase().equals("html")) {
                mediaType = "application/html";
                extension = ".html";
            } else {
                mediaType = "application/download";
                extension = "";
            }

            response.setHeader("Content-Disposition", "attachment; filename=\"" + bean.getName() +
                    extension + "\"");
            response.setContentType(mediaType);

            ServletOutputStream servletOutputStream = response.getOutputStream();
            try {
                client.getReportBytes(bean).writeTo(servletOutputStream);
                response.flushBuffer();
                servletOutputStream.flush();
            } finally {
                if (servletOutputStream != null) {
                    servletOutputStream.close();
                }
            }

        } catch (RegistryException e) {
            String msg = "Failed to get resource content. " + e.getMessage();
            log.error(msg, e);
            response.setStatus(500);
        }
    }
}
