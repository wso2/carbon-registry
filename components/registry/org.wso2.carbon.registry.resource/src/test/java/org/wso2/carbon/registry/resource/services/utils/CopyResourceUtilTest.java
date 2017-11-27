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
import org.wso2.carbon.registry.resource.test.base.BaseTestCase;

public class CopyResourceUtilTest extends BaseTestCase {

    @Override
    public void setUp() throws RegistryException {
        super.setUp();

        Resource r1 = registry.newResource();
        byte[] r1content = RegistryUtils.encodeString("R1 content");
        r1.setContent(r1content);
        r1.setMediaType("application/test");
        registry.put("/test/2017/10/19", r1);
    }

    @Override
    protected void tearDown() throws Exception {
        registry.delete("/test/2017/10/20");
        super.tearDown();
    }

    public void testCopyResource() throws Exception {

        assertTrue(registry.resourceExists("/test/2017/10/19"));
        assertFalse(registry.resourceExists("/test/2017/10/20"));

        CopyResourceUtil.copyResource((UserRegistry) registry, null, "/test/2017/10/19", "/test/2017/10/", "20");

        assertTrue(registry.resourceExists("/test/2017/10/19"));
        assertTrue(registry.resourceExists("/test/2017/10/20"));

    }

    public void testCopyResourceError() throws Exception {

        try {
            CopyResourceUtil.copyResource((UserRegistry) registry, null, "/test/2017/10/99999", "/test/2017/10/", "21");
        } catch (RegistryException ex) {
            assertEquals("Failed to copy the resource. Old resource path( /test/2017/10/99999 ) is not valid",
                         ex.getMessage());
        }

    }
}