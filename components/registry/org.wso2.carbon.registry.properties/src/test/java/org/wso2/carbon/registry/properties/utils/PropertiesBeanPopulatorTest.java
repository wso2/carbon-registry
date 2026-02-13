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
package org.wso2.carbon.registry.properties.utils;

import org.junit.Before;
import org.junit.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.common.CommonConstants;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryRealm;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.properties.beans.PropertiesBean;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserStoreException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PropertiesBeanPopulatorTest {

    private UserRegistry userRegistry;
    private String resourcePath = "/_system/governance/repository/trunk/service/java/1.0.0/HelloService";
    private Resource resource;
    @Before
    public void setup() throws RegistryException, UserStoreException {
        userRegistry = mock(UserRegistry.class);
        resource = new ResourceImpl();
        when(userRegistry.get(anyString())).thenReturn(resource);
        when(userRegistry.getUserName()).thenReturn("admin");
        System.setProperty("carbon.home", "");

        AuthorizationManager authManager = mock(AuthorizationManager.class);
        when(authManager.isUserAuthorized("admin", resourcePath, ActionConstants
                .GET)).thenReturn(Boolean.TRUE);
        RegistryRealm registryRealm = mock(RegistryRealm.class);
        when(registryRealm.getAuthorizationManager()).thenReturn(authManager);
        when(userRegistry.getUserRealm()).thenReturn(registryRealm);
    }

    @Test
    public void testPopulate() throws Exception {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin");
            resource.setProperty(CommonConstants.RETENTION_USERNAME_PROP_NAME, "admin1");
            resource.setProperty(CommonConstants.RETENTION_WRITE_LOCKED_PROP_NAME, "true");
            resource.setProperty(CommonConstants.RETENTION_DELETE_LOCKED_PROP_NAME, "true");
            resource.setProperty("registry.mount.path", "_system_governance");
            resource.setProperty("asset.lifecycle", "ServiceLifecycle");
            PropertiesBean propertiesBean = PropertiesBeanPopulator.populate(userRegistry, resourcePath, "no");
            assertEquals("true", propertiesBean.getDeleteLocked());
            assertEquals("true", propertiesBean.getWriteLocked());
            assertArrayEquals(new String[]{}, propertiesBean.getLifecycleProperties());
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Test
    public void testPopulateWithoutProperties() throws Exception {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin");

            PropertiesBean propertiesBean = PropertiesBeanPopulator.populate(userRegistry, resourcePath, "no");
            assertEquals("false", propertiesBean.getDeleteLocked());
            assertEquals("false", propertiesBean.getWriteLocked());
            assertEquals(resourcePath, propertiesBean.getPathWithVersion());
            assertEquals(true, propertiesBean.isLoggedIn());
            assertEquals(false, propertiesBean.isPutAllowed());
            assertEquals(false, propertiesBean.isVersionView());
            assertArrayEquals(new String[]{}, propertiesBean.getLifecycleProperties());
            assertArrayEquals(new Property[]{}, propertiesBean.getProperties());
            assertArrayEquals(new String[]{}, propertiesBean.getSysProperties());
            assertArrayEquals(new String[]{}, propertiesBean.getValidationProperties());
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Test
    public void testPopulateProperties() throws Exception {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin");
            resource.setProperty("registry.mount.path", "_system_governance");
            resource.setProperty("asset.lifecycle", "ServiceLifecycle");
            resource.setProperty("registry.wsdl.Name", "HelloService");
            resource.setProperty("registry.lifecycle.ServiceLifecycle.state", "Production");
            resource.setProperty("registry.Aspects", "Review");

            PropertiesBean propertiesBean = PropertiesBeanPopulator.populate(userRegistry, resourcePath, "yes");

            assertEquals("false", propertiesBean.getDeleteLocked());
            assertEquals("false", propertiesBean.getWriteLocked());
            assertEquals(resourcePath, propertiesBean.getPathWithVersion());
            assertEquals(true, propertiesBean.isLoggedIn());
            assertEquals(false, propertiesBean.isPutAllowed());
            assertEquals(false, propertiesBean.isVersionView());
            assertEquals(2, propertiesBean.getLifecycleProperties().length);
            assertEquals(5, propertiesBean.getProperties().length);
            assertEquals(5, propertiesBean.getSysProperties().length);
            assertArrayEquals(new String[]{"registry.wsdl.Name"}, propertiesBean.getValidationProperties());
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

}
