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

package org.wso2.carbon.registry.properties.ui.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.registry.common.ui.UIConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceStub;
import org.wso2.carbon.registry.properties.stub.beans.xsd.PropertiesBean;
import org.wso2.carbon.registry.properties.stub.beans.xsd.RetentionBean;
import org.wso2.carbon.registry.properties.stub.utils.xsd.Property;
import org.wso2.carbon.registry.properties.ui.Utils;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class PropertiesServiceClient {

    private static final Log log = LogFactory.getLog(PropertiesServiceClient.class);

    private PropertiesAdminServiceStub stub;
    private String epr;

    public PropertiesServiceClient(ServletConfig config, HttpSession session)
            throws RegistryException {

        String cookie = (String)session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.
                getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        epr = backendServerURL + "PropertiesAdminService";

        try {
            stub = new PropertiesAdminServiceStub(configContext, epr);

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

    public PropertiesBean getProperties(HttpServletRequest request) {

        String path = (String) Utils.getParameter(request, "path");
        Boolean view = (Boolean)request.getSession().getAttribute(UIConstants.SHOW_SYSPROPS_ATTR);
        String viewProps;
        if(view != null) {
            if(view.booleanValue()) {
                viewProps = "yes";
            } else {
                viewProps = "no";
            }
        } else {
            viewProps = "no";
        }
        PropertiesBean bean = null;
        try {
            bean = stub.getProperties(path, viewProps);
            if (bean == null) {
                return null;
            }
            if (bean.getLifecycleProperties() == null) {
                bean.setLifecycleProperties(new String[0]);
            }
            if (bean.getSysProperties() == null) {
                bean.setSysProperties(new String[0]);
            }
            if (bean.getValidationProperties() == null) {
                bean.setValidationProperties(new String[0]);
            }
            if (bean.getProperties() == null) {
                bean.setProperties(new Property[0]);
            }
        } catch (Exception e) {
            String msg = "Failed to get properties. " +
                    e.getMessage();
            log.error(msg, e);
            e.printStackTrace();
        }
        return bean;
    }

    public void setProperty (HttpServletRequest request) throws Exception {
        String path = (String) Utils.getParameter(request, "path");
        String name = (String) Utils.getParameter(request, "name");
        String value = (String) Utils.getParameter(request, "value");
        try {
            stub.setProperty(path, name, value);
        } catch (Exception e) {
            String msg = "Failed to add the property. " +
                    e.getMessage();
            log.error(msg, e);
            throw new Exception(msg,e);
        }
    }

    public void updateProperty(HttpServletRequest request) throws Exception {
        String path = (String) Utils.getParameter(request, "path");
        String name = (String) Utils.getParameter(request, "name");
        String value = (String) Utils.getParameter(request, "value");
        String oldName = (String) Utils.getParameter(request, "oldName");
        try {
            stub.updateProperty(path, name, value, oldName);
        } catch (Exception e) {
            String msg = "Failed to update the property. " +
                    e.getMessage();
            log.error(msg, e);
            throw new Exception(e);
        }
    }

    public void removeProperty(HttpServletRequest request) throws Exception {
        String path = (String) Utils.getParameter(request, "path");
        String name = (String) Utils.getParameter(request, "name");
        try {
            stub.removeProperty(path, name);
        } catch (Exception e) {
            String msg = "Failed to remove the property. " +
                    e.getMessage();
            log.error(msg, e);
            throw new Exception(e);
        }
    }

    public boolean setRetentionProperties(HttpServletRequest request) throws Exception {
        try {
            RetentionBean bean;
            String path = request.getParameter("path");
            String fromDate = request.getParameter("fromDate");
            if (fromDate == null || "".equals(fromDate)) {
                bean = null;
            } else {
                bean = new RetentionBean();
                bean.setFromDate(fromDate);
                bean.setToDate(request.getParameter("toDate"));
                String lockedOperationsParam = request.getParameter("lockedOperations");
                bean.setWriteLocked(lockedOperationsParam.contains("write"));
                bean.setDeleteLocked(lockedOperationsParam.contains("delete"));
            }
            stub.setRetentionProperties(path, bean);
        } catch (Exception e) {
            log.error("Failed to add retention: " + e.getMessage(), e);
            throw new Exception(e);
        }
        return true;
    }

    public RetentionBean getRetentionProperties(HttpServletRequest request)
            throws RegistryException {
        try {
            return stub.getRetentionProperties(request.getParameter("path"));
        } catch (Exception e) {
            String msg = "Could not retrieve retention details " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }
}
