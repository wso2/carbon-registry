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

package org.wso2.carbon.registry.extensions.ui.clients;

import org.wso2.carbon.registry.extensions.stub.ResourceAdminServiceStub;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpSession;
import javax.activation.DataHandler;

public class
        ResourceServiceClient {

    private static final Log log = LogFactory.getLog(ResourceServiceClient.class);

    private ResourceAdminServiceStub stub;
    private String epr;

    public ResourceServiceClient(
            String cookie, String backendServerURL, ConfigurationContext configContext)
            throws RegistryException {

        epr = backendServerURL + "ResourceAdminService";

        try {
            stub = new ResourceAdminServiceStub(configContext, epr);

            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        } catch (AxisFault axisFault) {
            String msg = "Failed to initiate resource service client. " + axisFault.getMessage();
            log.error(msg, axisFault);
            throw new RegistryException(msg, axisFault);
        }
    }

    public ResourceServiceClient(String cookie, ServletConfig config, HttpSession session)
            throws RegistryException {

        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.
                getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        epr = backendServerURL + "ResourceAdminService";

        try {
            stub = new ResourceAdminServiceStub(configContext, epr);

            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        } catch (AxisFault axisFault) {
            String msg = "Failed to initiate resource service client. " + axisFault.getMessage();
            log.error(msg, axisFault);
            throw new RegistryException(msg, axisFault);
        }
    }

    public ResourceServiceClient(ServletConfig config, HttpSession session)
            throws RegistryException {

        String cookie = (String)session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.
                getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        epr = backendServerURL + "ResourceAdminService";

        try {
            stub = new ResourceAdminServiceStub(configContext, epr);

            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        } catch (AxisFault axisFault) {
            String msg = "Failed to initiate resource service client. " + axisFault.getMessage();
            log.error(msg, axisFault);
            throw new RegistryException(msg, axisFault);
        }
    }

    public void addExtension(String name, DataHandler content) throws Exception {
        try {
            Options options = stub._getServiceClient().getOptions();
            options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
            options.setTimeOutInMilliSeconds(300000);
            stub.addExtension(name, content);
        } catch (Exception e) {
            String msg = "Failed to add extension.";
            log.error(msg, e);
            throw e;
        }
    }

    public String[] listExtensions() throws Exception {
        try {
            return stub.listExtensions();
        } catch (Exception e) {
            String msg = "Failed to list extensions.";
            log.error(msg, e);
            throw e;
        }
    }

    public boolean deleteExtension(String name) throws Exception {
        try {
            return stub.removeExtension(name);    
        } catch (Exception e) {
            String msg = "Failed to remove extension.";
            log.error(msg, e);
            throw e;
        }
    }

}
