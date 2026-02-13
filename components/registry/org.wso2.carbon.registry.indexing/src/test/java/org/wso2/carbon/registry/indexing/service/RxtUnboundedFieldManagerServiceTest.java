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
package org.wso2.carbon.registry.indexing.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.indexing.util.IndexingTestUtils;
import org.wso2.carbon.registry.indexing.utils.RxtUnboundedDataLoadUtilsTest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class RxtUnboundedFieldManagerServiceTest {

    @Before
    public void setup() throws RegistryException {
        Path registryPath = IndexingTestUtils.getResourcePath("registry-indexing.xml");
        System.setProperty("wso2.registry.xml", registryPath.toString());
        System.setProperty("carbon.home", registryPath.toString());
        Path resourcePath = IndexingTestUtils.getResourcePath("conf");
        System.setProperty("carbon.config.dir.path", resourcePath.toString());

        UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
        Collection resultCollection = new CollectionImpl();
        resultCollection.setContent(new String[]
                {"/_system/config/repository/components/org.wso2.carbon.governance/configuration/restservice"});
        when(userRegistry.executeQuery(any(), any())).thenReturn(resultCollection);
        Resource rxtResource = new ResourceImpl();
        rxtResource.setMediaType("application/xml");
        InputStream is = null;
        try {
            is = RxtUnboundedDataLoadUtilsTest.class.getClassLoader().getResourceAsStream("restservice.rxt");
            rxtResource.setContentStream(is);
            when(userRegistry.get("/_system/config/repository/components/org.wso2.carbon" +
                    ".governance/configuration/restservice")).thenReturn(rxtResource);

            RegistryService service = Mockito.mock(RegistryService.class);
            org.wso2.carbon.registry.indexing.Utils.setRegistryService(service);
            when(service.getRegistry()).thenReturn(userRegistry);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignore) {

                }
            }
        }
    }

    @After
    public void cleanUp() throws NoSuchFieldException, IllegalAccessException {
        System.clearProperty("wso2.registry.xml");
        System.clearProperty("carbon.home");
        System.clearProperty("carbon.config.dir.path");
    }

    @Test
    public void getInstance() throws Exception {
        Object object = RxtUnboundedFieldManagerService.getInstance();
        assertTrue(object instanceof RxtUnboundedFieldManagerService);
    }

    @Test
    public void setActiveTenantsUnboundedFields() throws Exception {
        try {
            System.setProperty("carbon.home", "");
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234, true);
            RxtUnboundedFieldManagerService.getInstance().setActiveTenantsUnboundedFields(null);
            Map<Integer, Map<String, List<String>>> allTenantsUnboundedFields = RxtUnboundedFieldManagerService.getInstance
                    ().getTenantsUnboundedFields();
            assertNotNull(allTenantsUnboundedFields.get(-1234));
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Test
    public void setTenantsUnboundedFields() throws Exception {
        Map<String, List<String>> rxtData = new HashMap<>();
        String[] expectedFieldArray = new String[] {"uritemplate_urlPattern", "uritemplate_httpVerb",
                "uritemplate_authType", "contacts_entry", "endpoints_entry"};
        rxtData.put("application/vnd.wso2-restservice+xml", Arrays.asList(expectedFieldArray));
        RxtUnboundedFieldManagerService.getInstance().setTenantsUnboundedFields(1, rxtData);
        Map<Integer, Map<String, List<String>>> allTenantsUnboundedFields = RxtUnboundedFieldManagerService.getInstance
                ().getTenantsUnboundedFields();
        assertEquals(rxtData, allTenantsUnboundedFields.get(1));
    }

}