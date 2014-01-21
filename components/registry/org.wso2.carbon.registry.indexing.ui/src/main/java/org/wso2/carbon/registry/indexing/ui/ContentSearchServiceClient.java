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

package org.wso2.carbon.registry.indexing.ui;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.indexing.stub.generated.ContentSearchAdminServiceStub;
import org.wso2.carbon.registry.indexing.stub.generated.xsd.ResourceData;
import org.wso2.carbon.registry.indexing.stub.generated.xsd.SearchResultsBean;
import org.wso2.carbon.ui.CarbonUIUtil;

import java.rmi.RemoteException;

public class ContentSearchServiceClient {

    private static final Log log = LogFactory.getLog(ContentSearchServiceClient.class);

    private ContentSearchAdminServiceStub stub;
    private String epr;

    public ContentSearchServiceClient(String cookie, ServletConfig config, HttpSession session)
            throws RegistryException {

        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.
                getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        epr = backendServerURL + "ContentSearchAdminService";

        try {
            stub = new ContentSearchAdminServiceStub(configContext, epr);

            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        } catch (AxisFault axisFault) {
            String msg = "Failed to initiate search service client. " + axisFault.getMessage();
            log.error(msg, axisFault);
            throw new RegistryException(msg, axisFault);
        }
    }

    public void restartIndexing() throws RegistryException {
        try {
            stub.restartIndexing();
        } catch (Exception e) {
            String msg = "Failed to start re-indexing. " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg);
        }
    }

    public SearchResultsBean getSearchResults(HttpServletRequest request) throws RegistryException {

        String content = (String) Utils.getParameter(request, "content");
        SearchResultsBean bean = null;
        try {
        	//working around a bug which sets a worng content type
        	stub._getServiceClient().getOptions().setAction("urn:getContentSearchResults");
            bean = stub.getContentSearchResults(content);
            if (bean.getResourceDataList() == null) {
                bean.setResourceDataList(new ResourceData[0]);
            }
        } catch (Exception e) {
            String msg = "Failed to get search results from the search service. " +
                    e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg);
        }

        return bean;
    }
}
