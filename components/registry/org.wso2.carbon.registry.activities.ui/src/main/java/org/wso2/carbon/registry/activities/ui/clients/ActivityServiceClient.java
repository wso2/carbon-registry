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

package org.wso2.carbon.registry.activities.ui.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.pagination.PaginationUtils;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;
import org.wso2.carbon.registry.activities.stub.ActivityAdminServiceStub;
import org.wso2.carbon.registry.common.IActivityService;
import org.wso2.carbon.registry.common.beans.ActivityBean;
import org.wso2.carbon.ui.CarbonUIUtil;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class ActivityServiceClient implements IActivityService {

    private static final Log log = LogFactory.getLog(ActivityServiceClient.class);

    private ActivityAdminServiceStub stub;

    private IActivityService proxy = null;
    private HttpSession session;
    public ActivityServiceClient(String cookie, ServletConfig config, HttpSession session)
            throws RegistryException {
        this.session =session;
        if (proxy == null) {

            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(),
                    session);
            ConfigurationContext configContext = (ConfigurationContext) config.
                    getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            String epr = backendServerURL + "ActivityAdminService";

            try {
                stub = new ActivityAdminServiceStub(configContext, epr);

                ServiceClient client = stub._getServiceClient();
                Options option = client.getOptions();
                option.setManageSession(true);
                option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                        cookie);

            } catch (AxisFault axisFault) {
                String msg = "Failed to initiate comment service client. " + axisFault.getMessage();
                log.error(msg, axisFault);
                throw new RegistryException(msg, axisFault);
            }
            proxy = this;
        }
    }

    public void setSession(String sessionId, HttpSession session) {/* Not required at client-side.*/}

    public void removeSession(String sessionId) {/* Not required at client-side.*/}

    public ActivityBean getActivities(HttpServletRequest request) {

        String sessionId = UUIDGenerator.generateUUID();

        String userName = request.getParameter("userName");
        String resourcePath = request.getParameter("path");
        String fromDate = request.getParameter("fromDate");
        String toDate = request.getParameter("toDate");
        String filter = request.getParameter("filter");
        String pageStr = request.getParameter("page");

        try {
            return proxy.getActivities(userName, resourcePath, fromDate, toDate, filter, pageStr,
                    sessionId);
        } catch (Exception e) {
            String msg = "Failed to get activities from the activity service.";
            log.error(msg, e);
            return null;
        }
    }

    public ActivityBean getRecentActivitiesForLoginUser(HttpServletRequest request) {

        String sessionId = UUIDGenerator.generateUUID();

        String LOGGED_USER = "logged-user";

        String userName = (String)request.getSession().getAttribute(LOGGED_USER);
        String resourcePath = null;
        String fromDate = null;
        String toDate = null;
        String filter = "all";
        String pageStr = "1";

        try {
            return proxy.getActivities(userName, resourcePath, fromDate, toDate, filter, pageStr,
                    sessionId);
        } catch (Exception e) {
            String msg = "Failed to get activities from the activity service.";
            log.error(msg, e);
            return null;
        }
    }

    public ActivityBean getActivities(String userName, String resourcePath, String fromDate,
                                String toDate, String filter, String pageStr, String sessionId)
            throws RegistryException {

        ActivityBean result = new ActivityBean();

        try {
            org.wso2.carbon.registry.activities.stub.beans.xsd.ActivityBean bean = null;
            if (PaginationContext.getInstance() == null) {

                bean = stub.getActivities(userName, resourcePath, fromDate, toDate, filter, pageStr,
                        sessionId);
            } else {
                PaginationUtils.copyPaginationContext(stub._getServiceClient());
                bean = stub.getActivities(userName, resourcePath, fromDate, toDate, filter, pageStr,
                        sessionId);
                int rowCount = PaginationUtils.getRowCount(stub._getServiceClient());
                session.setAttribute("row_count", Integer.toString(rowCount));
            }
            if (bean.getActivity() == null) {
                bean.setActivity(new String[0]);
            }
            result.setErrorMessage(bean.getErrorMessage());
            result.setActivity(bean.getActivity());
        } catch (Exception e) {
            String msg = e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        } finally {
            PaginationContext.destroy();
        }

        return result;
    }
}
