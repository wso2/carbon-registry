/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.resource.services.utils;

import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.resource.beans.PermissionBean;
import org.wso2.carbon.registry.resource.test.base.BaseTestCase;

public class PermissionUtilTest extends BaseTestCase {

    @Override
    public void setUp() throws RegistryException {
        super.setUp();

        Resource resource = registry.newResource();
        byte[] r1content = RegistryUtils.encodeString("Resource 14 content");
        resource.setContent(r1content);
        resource.setMediaType("application/test");
        registry.put("/test/2017/10/14", resource);
    }


    public void testGetPermissions() throws Exception {
        PermissionBean permissionBean = PermissionUtil.getPermissions((UserRegistry) registry, "/test/2017/10/14");
        assertEquals("Internal/everyone", permissionBean.getRoleNames()[0].toString());
        assertEquals("system/wso2.anonymous.role", permissionBean.getRoleNames()[1].toString());
        assertEquals(0, permissionBean.getUserNames().length);
        assertTrue(permissionBean.isAuthorizeAllowed());
        assertTrue(permissionBean.isDeleteAllowed());
        assertTrue(permissionBean.isPutAllowed());
        assertFalse(permissionBean.isVersionView());
        assertEquals(0, permissionBean.getUserPermissions().length);
        assertEquals("INTERNAL/everyone", permissionBean.getRolePermissions()[0].getUserName());
        assertFalse(permissionBean.getRolePermissions()[0].isAuthorizeAllow());
        assertFalse(permissionBean.getRolePermissions()[0].isAuthorizeDeny());
        assertFalse(permissionBean.getRolePermissions()[0].isDeleteAllow());
        assertFalse(permissionBean.getRolePermissions()[0].isDeleteDeny());
        assertTrue(permissionBean.getRolePermissions()[0].isReadAllow());
        assertFalse(permissionBean.getRolePermissions()[0].isReadDeny());
        assertFalse(permissionBean.getRolePermissions()[0].isWriteAllow());
        assertFalse(permissionBean.getRolePermissions()[0].isWriteDeny());
    }

    @Override
    protected void tearDown() throws Exception {
        registry.delete("/test/2017/10/14");
        super.tearDown();
    }
}