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
import org.wso2.carbon.registry.resource.beans.VersionsBean;
import org.wso2.carbon.registry.resource.test.base.BaseTestCase;

public class GetVersionsUtilTest extends BaseTestCase {

    @Override
    public void setUp() throws RegistryException {
        super.setUp();

        Resource resource = registry.newResource();
        byte[] r1content = RegistryUtils.encodeString("Resource 16 content");
        resource.setContent(r1content);
        resource.setMediaType("application/test");
        registry.put("/test/2017/10/16", resource);

        registry.createVersion("/test/2017/10/16"); //version 1
        registry.createVersion("/test/2017/10/16"); //version 2
    }

    public void testGetVersionsBean() throws Exception {

        VersionsBean versionsBean = GetVersionsUtil.getVersionsBean((UserRegistry) registry, "/test/2017/10/16");

        assertNotNull(versionsBean);
        assertTrue(versionsBean.isDeletePermissionAllowed());
        assertEquals("false", versionsBean.getWriteLocked());
        assertEquals("false", versionsBean.getDeleteLocked());
        assertEquals("/test/2017/10/16", versionsBean.getResourcePath());
        assertNull(versionsBean.getPermalink());
        assertNull(versionsBean.getPermalink());
        assertTrue(versionsBean.isPutAllowed());

        assertEquals(2, versionsBean.getVersionPaths().length);

        //view version 2 - latest
        assertEquals("/test/2017/10/16", versionsBean.getVersionPaths()[0].getActiveResourcePath());
        assertEquals("/test/2017/10/16;version:2", versionsBean.getVersionPaths()[0].getCompleteVersionPath());
        assertNotNull(versionsBean.getVersionPaths()[0].getUpdatedOn());
        assertEquals("admin", versionsBean.getVersionPaths()[0].getUpdater());
        assertEquals(2, versionsBean.getVersionPaths()[0].getVersionNumber());

        //view version 1
        assertEquals("/test/2017/10/16", versionsBean.getVersionPaths()[1].getActiveResourcePath());
        assertEquals("/test/2017/10/16;version:1", versionsBean.getVersionPaths()[1].getCompleteVersionPath());
        assertNotNull(versionsBean.getVersionPaths()[1].getUpdatedOn());
        assertEquals("admin", versionsBean.getVersionPaths()[1].getUpdater());
        assertEquals(1, versionsBean.getVersionPaths()[1].getVersionNumber());

    }

    @Override
    protected void tearDown() throws Exception {
        registry.delete("/test/2017/10/16");
        super.tearDown();
    }
}
