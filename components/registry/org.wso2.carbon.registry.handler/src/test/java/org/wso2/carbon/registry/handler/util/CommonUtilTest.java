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
package org.wso2.carbon.registry.handler.util;

import org.junit.Test;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.handler.beans.SimulationRequest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class CommonUtilTest {

    @Test
    public void getRegistryService() throws Exception {
        RegistryService service = mock(RegistryService.class);
        CommonUtil.setRegistryService(service);
        assertEquals(service, CommonUtil.getRegistryService());
    }

    @Test
    public void testSimulateRegistryForNullOperation() throws Exception {
        SimulationRequest simulationRequest = new SimulationRequest();
    }

    @Test
    public void getSimulationResponse() throws Exception {

    }

    @Test
    public void updateHandler() throws Exception {

    }

    @Test
    public void addHandler() throws Exception {

    }

    @Test
    public void handlerExists() throws Exception {

    }

    @Test
    public void deleteHandler() throws Exception {

    }

    @Test
    public void getHandlerConfiguration() throws Exception {

    }

    @Test
    public void getHandlerList() throws Exception {

    }

    @Test
    public void getContextRoot() throws Exception {

    }

    @Test
    public void setContextRoot() throws Exception {

    }

    @Test
    public void generateHandler() throws Exception {

    }

    @Test
    public void removeHandler() throws Exception {

    }

    @Test
    public void isHandlerNameInUse() throws Exception {

    }

    @Test
    public void addDefaultHandlersIfNotAvailable() throws Exception {

    }

    @Test
    public void getSimulationService() throws Exception {

    }

    @Test
    public void setSimulationService() throws Exception {

    }

    @Test
    public void deserializeHandlerConfiguration() throws Exception {

    }

    @Test
    public void serializeHandlerConfiguration() throws Exception {

    }

}
