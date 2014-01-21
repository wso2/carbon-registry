/*
 *  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.reporting.ui.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.registry.reporting.stub.ReportingAdminServiceStub;
import org.wso2.carbon.registry.reporting.stub.beans.xsd.ReportConfigurationBean;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import javax.activation.DataHandler;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class ReportGeneratorClient {
    
    private static final Log log = LogFactory.getLog(ReportGeneratorClient.class);
    private ReportingAdminServiceStub stub = null;
    
    public ReportGeneratorClient(HttpServletRequest request, ServletConfig config) {
        HttpSession session = request.getSession();
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.
                getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String epr = backendServerURL + "ReportingAdminService";

        try {
            stub = new ReportingAdminServiceStub(configContext, epr);

            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        } catch (AxisFault e) {
            log.error("Failed to initiate report generator client.", e);
        }
    }
    
    public DataHandler getReportBytes(ReportConfigurationBean configuration)
            throws Exception {
        return stub.getReportBytes(configuration);
    }

    public void scheduleReport(ReportConfigurationBean configuration)
            throws Exception {
        stub.scheduleReport(configuration);
    }
    
    public void stopScheduledReport(String name) throws Exception {
        stub.stopScheduledReport(name);
    }

    public void saveReport(ReportConfigurationBean configuration)
            throws Exception {
        stub.saveReport(configuration);
    }
    
    public ReportConfigurationBean[] getSavedReports()
            throws Exception {
        ReportConfigurationBean[] savedReports = stub.getSavedReports();
        if (savedReports == null || savedReports[0] == null) {
            savedReports = new ReportConfigurationBean[0];
        }
        return savedReports;
    }

    public ReportConfigurationBean getSavedReport(String name)
            throws Exception {
        return stub.getSavedReport(name);
    }

    public void deleteSavedReport(String name)
            throws Exception {
        stub.deleteSavedReport(name);
    }

    public void copySavedReport(String name, String newName)
            throws Exception {
        stub.copySavedReport(name, newName);
    }

    public String[] getAttributeNames(String className) throws Exception {
        return stub.getAttributeNames(className);
    }

    public String[] getMandatoryAttributeNames(String className) throws Exception {
        return stub.getMandatoryAttributeNames(className);
    }

}
