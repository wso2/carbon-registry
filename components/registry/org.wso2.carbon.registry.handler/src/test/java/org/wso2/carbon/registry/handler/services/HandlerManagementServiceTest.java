/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.registry.handler.services;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.transport.http.HTTPConstants;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.context.internal.OSGiDataHolder;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.handler.base.BaseTestCase;
import org.wso2.carbon.registry.handler.beans.SimulationRequest;
import org.wso2.carbon.registry.handler.util.CommonUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class HandlerManagementServiceTest extends BaseTestCase {

    private String handlerConfiguration = "<handler class=\"org.wso2.carbon.registry.extensions.handlers.ServiceMediaTypeHandler\">" +
            "<filter class=\"org.wso2.carbon.registry.core.jdbc.handlers.filters.MediaTypeMatcher\">" +
            "<property name=\"mediaType\">application/vnd.wso2-service+xml</property>" +
            "</filter>" +
            "</handler>";

    private String updateHandlerConfig = "    <handler class=\"org.wso2.carbon.registry.extensions.handlers.DeleteSubscriptionHandler\">\n" +
            "         <filter class=\"org.wso2.carbon.registry.core.jdbc.handlers.filters.URLMatcher\">\n" +
            "                 <property name=\"pattern\">.*</property>\n" +
            "         </filter>\n" +
            "    </handler>";


    @Test
    public void getHandlerCollectionLocation() throws Exception {
        HandlerManagementService managementService = new HandlerManagementService();
        Assert.assertEquals(RegistryConstants.HANDLER_CONFIGURATION_PATH, managementService
                .getHandlerCollectionLocation());
    }

    @Test
    public void setHandlerCollectionLocation() throws Exception {
        String handlerContextRoot = "/repository/components/org.wso2.carbon.governance/handlers1/";
        HandlerManagementService managementService = new HandlerManagementService();
        managementService.setHandlerCollectionLocation(handlerContextRoot);
        assertEquals(handlerContextRoot, managementService.getHandlerCollectionLocation());
        CommonUtil.setContextRoot(RegistryConstants.HANDLER_CONFIGURATION_PATH);
    }

    @Test
    public void testHandlerList() throws Exception {
        String handlerName= "org.wso2.carbon.registry.extensions.handlers.ServiceMediaTypeHandler";
        String updatedHandlerName = "org.wso2.carbon.registry.extensions.handlers.DeleteSubscriptionHandler";
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin");
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setRegistry(RegistryType.SYSTEM_CONFIGURATION,
                    configRegistry);

            OSGiDataHolder holder = OSGiDataHolder.getInstance();
            holder.setRegistryService(ctx.getEmbeddedRegistryService());
            HandlerManagementService managementService = new HandlerManagementService();
            managementService.createHandler(handlerConfiguration);
            String[] handlerList = managementService.getHandlerList();
            Assert.assertArrayEquals(new String[]{handlerName}, handlerList);
            Assert.assertEquals(handlerConfiguration, managementService.getHandlerConfiguration(handlerName));

            Assert.assertTrue(managementService.updateHandler(handlerName, updateHandlerConfig));
            handlerList = managementService.getHandlerList();
            Assert.assertArrayEquals(new String[]{updatedHandlerName}, handlerList);

            Assert.assertTrue(managementService.deleteHandler(updatedHandlerName));
            handlerList = managementService.getHandlerList();
            Assert.assertArrayEquals(null, handlerList);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Test
    public void simulate() throws Exception {
        MessageContext messageContext = new MessageContext();
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(RegistryConstants.ROOT_REGISTRY_INSTANCE)).thenReturn(registry);
        when(servletRequest.getSession()).thenReturn(session);
        messageContext.setProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST, servletRequest);
        AxisService axisService = new AxisService();
        axisService.addParameter(new Parameter(CarbonConstants.ADMIN_SERVICE_PARAM_NAME, "true"));
        messageContext.setAxisService(axisService);
        MessageContext.setCurrentMessageContext(messageContext);

        final SimulationRequest simulationRequest = new SimulationRequest();
        simulationRequest.setOperation("resourceexists");
        simulationRequest.setPath("/test/2017/10/18");

        CommonUtil.setSimulationService(new RegistryCoreServiceComponent.DefaultSimulationService());
        HandlerManagementService managementService = new HandlerManagementService();
        managementService.simulate(simulationRequest);
        assertNotNull(CommonUtil.getSimulationResponse().getStatus());
        assertEquals(3, CommonUtil.getSimulationResponse().getStatus().length);
    }

}
