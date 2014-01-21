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
package org.wso2.carbon.registry.handler.services;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.admin.api.handler.IHandlerManagementService;
import org.wso2.carbon.registry.common.services.RegistryAbstractAdmin;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.handler.beans.HandlerConfigurationBean;
import org.wso2.carbon.registry.handler.beans.SimulationRequest;
import org.wso2.carbon.registry.handler.beans.SimulationResponse;
import org.wso2.carbon.registry.handler.util.CommonUtil;

import javax.xml.stream.XMLStreamException;

public class HandlerManagementService extends RegistryAbstractAdmin implements
        IHandlerManagementService<SimulationResponse, SimulationRequest> {

    private static final Log log = LogFactory.getLog(HandlerManagementService.class);

    public String getHandlerCollectionLocation() throws Exception  {
        return CommonUtil.getContextRoot();
    }

    public void setHandlerCollectionLocation(String location) throws Exception {
        CommonUtil.setContextRoot(location);
    }

    public String[] getHandlerList() throws Exception  {
        Registry configSystemRegistry = getConfigSystemRegistry();
        return CommonUtil.getHandlerList(configSystemRegistry);    
    }

    public String getHandlerConfiguration(String name) throws Exception  {
        Registry configSystemRegistry = getConfigSystemRegistry();
        return CommonUtil.getHandlerConfiguration(configSystemRegistry, name);
    }

    public boolean deleteHandler(String name) throws Exception {
        RegistryUtils.recordStatistics(name);
        Registry configSystemRegistry = getConfigSystemRegistry();
        return !RegistryUtils.isRegistryReadOnly(configSystemRegistry.getRegistryContext()) &&
                CommonUtil.deleteHandler(configSystemRegistry, name);
    }

    private String parseHandlerConfiguration(String payload) throws XMLStreamException {
        OMElement configurationElement = AXIOMUtil.stringToOM(payload);
        HandlerConfigurationBean handlerConfigurationBean =
                CommonUtil.deserializeHandlerConfiguration(configurationElement);
        if (handlerConfigurationBean == null ||
                handlerConfigurationBean.getHandlerClass() == null ||
                handlerConfigurationBean.getFilter() == null ||
                handlerConfigurationBean.getFilter().getFilterClass() == null) {
            return null;
        }
        return CommonUtil.serializeHandlerConfiguration(handlerConfigurationBean).toString();
    }

    public boolean createHandler(String payload) throws Exception {
        RegistryUtils.recordStatistics(payload);
        Registry configSystemRegistry = getConfigSystemRegistry();
        String parsedPayload;
        try {
            parsedPayload = parseHandlerConfiguration(payload);
        } catch (Exception e) {
            log.error("Unable to parse the given handler configuration.", e);
            throw new Exception("Unable to parse the given handler configuration. " +
                    e.getMessage());
        }
        if (parsedPayload == null) {
            throw new Exception("The provided handler configuration is invalid.");
        }
        return !RegistryUtils.isRegistryReadOnly(configSystemRegistry.getRegistryContext()) &&
                CommonUtil.addHandler(configSystemRegistry, parsedPayload);
    }

    public boolean updateHandler(String oldName, String payload) throws Exception {
        RegistryUtils.recordStatistics(oldName, payload);
        Registry configSystemRegistry = getConfigSystemRegistry();
        String parsedPayload;
        try {
            parsedPayload = parseHandlerConfiguration(payload);
        } catch (Exception e) {
            log.error("Unable to parse the given handler configuration.", e);
            throw new Exception("Unable to parse the given handler configuration. " +
                    e.getMessage());
        }
        if (parsedPayload == null) {
            throw new Exception("The provided handler configuration is invalid.");
        }
        return !RegistryUtils.isRegistryReadOnly(configSystemRegistry.getRegistryContext()) &&
                CommonUtil.updateHandler(configSystemRegistry, oldName, parsedPayload);
    }

    public SimulationResponse simulate(SimulationRequest request) throws Exception {
        Registry rootRegistry = getRootRegistry();
        CommonUtil.simulateRegistryOperation(rootRegistry, request);
        return CommonUtil.getSimulationResponse();
    }
}
