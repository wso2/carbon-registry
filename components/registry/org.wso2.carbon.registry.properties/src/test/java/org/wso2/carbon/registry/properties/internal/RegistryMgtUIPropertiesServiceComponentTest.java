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
package org.wso2.carbon.registry.properties.internal;

import org.junit.Test;
import org.wso2.carbon.registry.core.service.RegistryService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class RegistryMgtUIPropertiesServiceComponentTest {
    @Test
    public void activate() throws Exception {
        RegistryMgtUIPropertiesServiceComponent serviceComponent = new RegistryMgtUIPropertiesServiceComponent();
        serviceComponent.activate(null);
    }

    @Test
    public void deactivate() throws Exception {
        RegistryMgtUIPropertiesServiceComponent serviceComponent = new RegistryMgtUIPropertiesServiceComponent();
        serviceComponent.deactivate(null);
    }

    @Test
    public void setRegistryService() throws Exception {
        RegistryService registryService = mock(RegistryService.class);
        RegistryMgtUIPropertiesServiceComponent serviceComponent = new RegistryMgtUIPropertiesServiceComponent();
        serviceComponent.setRegistryService(registryService);
        assertEquals(registryService, PropertiesDataHolder.getInstance().getRegistryService());
    }

    @Test
    public void unsetRegistryService() throws Exception {
        RegistryService registryService = mock(RegistryService.class);
        RegistryMgtUIPropertiesServiceComponent serviceComponent = new RegistryMgtUIPropertiesServiceComponent();
        serviceComponent.unsetRegistryService(registryService);
        assertNull(PropertiesDataHolder.getInstance().getRegistryService());
    }

}