/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.handler.ui.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.MediaTypesUtils;
import org.wso2.carbon.registry.handler.stub.HandlerManagementServiceStub;
import org.wso2.carbon.registry.handler.stub.beans.xsd.SimulationRequest;
import org.wso2.carbon.registry.handler.stub.beans.xsd.SimulationResponse;
import org.wso2.carbon.ui.CarbonUIUtil;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.LinkedList;
import java.util.List;

public class HandlerManagementServiceClient {

    private static final Log log = LogFactory.getLog(HandlerManagementServiceClient.class);

    private HandlerManagementServiceStub stub;

    public HandlerManagementServiceClient(String cookie, ServletConfig config, HttpSession session)
            throws RegistryException {

        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.
                getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String epr = backendServerURL + "HandlerManagementService";

        try {
            stub = new HandlerManagementServiceStub(configContext, epr);

            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        } catch (AxisFault axisFault) {
            String msg = "Failed to initiate handler management service client. " +
                    axisFault.getMessage();
            log.error(msg, axisFault);
            throw new RegistryException(msg, axisFault);
        }
    }

    public String[] getHandlerList(HttpServletRequest request) throws Exception  {
        String[] output;
        try {
            output = stub.getHandlerList();
        } catch (Exception e) {
            if (e instanceof RegistryException) {
                return new String[0];
            } else {
                throw e;
            }
        }
        return output;
    }

    public String getHandlerConfiguration(HttpServletRequest request) throws Exception  {
        String handlerName = request.getParameter("handlerName");
        String output;
        try {
            output = stub.getHandlerConfiguration(handlerName);
        } catch (Exception e) {
            if (e instanceof RegistryException) {
                return "";
            } else {
                throw e;
            }
        }
        return output;
    }

    public boolean newHandler(HttpServletRequest request) throws Exception {
        String payload = request.getParameter("payload");
        boolean output;
        try {
            output = stub.createHandler(payload);
        } catch (Exception e) {
            if (e instanceof RegistryException) {
                return false;
            } else {
                throw e;
            }
        }
        return output;
    }

    public boolean updateHandler(HttpServletRequest request) throws Exception {
        String handlerName = request.getParameter("handlerName");
        if (handlerName == null) {
            handlerName = "";
        }
        String payload = request.getParameter("payload");
        boolean output;
        try {
            output = stub.updateHandler(handlerName, payload);
        } catch (Exception e) {
            if (e instanceof RegistryException) {
                return false;
            } else {
                throw e;
            }
        }
        return output;
    }

    public boolean deleteHandler(HttpServletRequest request) throws Exception  {
        String handlerName = request.getParameter("handlerName");
        boolean output;
        try {
            output = stub.deleteHandler(handlerName);
        } catch (Exception e) {
            if (e instanceof RegistryException) {
                return false;
            } else {
                throw e;
            }
        }
        return output;
    }

    public SimulationResponse simulate(HttpServletRequest request) throws Exception  {
        SimulationRequest simulationRequest = new SimulationRequest();
        simulationRequest.setMediaType(MediaTypesUtils
                .getMimeTypeFromHumanReadableMediaType(request.getParameter("mediaType")));
        simulationRequest.setOperation(request.getParameter("operation"));
        simulationRequest.setPath(request.getParameter("path"));
        simulationRequest.setResourcePath(request.getParameter("resourcePath"));
        List<String> parameters = new LinkedList<String>();
        int i = 1;
        String temp = request.getParameter("param" + i);
        while (temp != null) {
            parameters.add(temp);
            i++;
            temp = request.getParameter("param" + i);
        }
        if (parameters.size() > 0) {
            simulationRequest.setParameters(parameters.toArray(new String[parameters.size()]));
        }
        SimulationResponse output;
        try {
            output = stub.simulate(simulationRequest);
        } catch (Exception e) {
            if (e instanceof RegistryException) {
                return new SimulationResponse();
            } else {
                throw e;
            }
        }
        return output;
    }
}
