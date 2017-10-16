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
package org.wso2.carbon.registry.activities.internal;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.registry.activities.services.utils.CommonUtil;
import org.wso2.carbon.registry.core.service.RegistryService;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class RegistryMgtUIActivitiesServiceComponentTest {
    @Test
    public void activate() throws Exception {
        ComponentContext context = mock(ComponentContext.class);
        RegistryMgtUIActivitiesServiceComponent component = new RegistryMgtUIActivitiesServiceComponent();
        component.activate(context);
    }

    @Test
    public void deactivate() throws Exception {
        ComponentContext context = mock(ComponentContext.class);
        RegistryMgtUIActivitiesServiceComponent component = new RegistryMgtUIActivitiesServiceComponent();
        component.deactivate(context);
    }

    @Test
    public void setRegistryService() throws Exception {
        RegistryService service = mock(RegistryService.class);
        RegistryMgtUIActivitiesServiceComponent component = new RegistryMgtUIActivitiesServiceComponent();
        component.setRegistryService(service);
        assertEquals(service, CommonUtil.getRegistryService());
    }

    @Test
    public void unsetRegistryService() throws Exception {
        RegistryService service = mock(RegistryService.class);
        RegistryMgtUIActivitiesServiceComponent component = new RegistryMgtUIActivitiesServiceComponent();
        component.unsetRegistryService(service);
        assertNull(CommonUtil.getRegistryService());
    }

    @After
    public void cleanup() {
        CommonUtil.setRegistryService(null);
    }

}
