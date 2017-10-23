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

import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.resource.test.base.BaseTestCase;

public class AddCollectionUtilTest extends BaseTestCase {

    @Override
    public void setUp() throws RegistryException {
        super.setUp();

        Resource r1 = registry.newResource();
        byte[] r1content = RegistryUtils.encodeString("R1 content");
        r1.setContent(r1content);
        r1.setMediaType("application/test");
        registry.put("/test/2017/10/24", r1);
    }

    @Override
    protected void tearDown() throws Exception {
        registry.delete("/test/2017/10/24");
        super.tearDown();
    }


    public void testProcess() throws Exception {

        String path = AddCollectionUtil.process("/", "collection", "collection", "Description for collection",
                                                (UserRegistry) registry);
        assertEquals("/", path);
        assertTrue(registry.resourceExists("/collection"));
        assertTrue(registry.get("/collection") instanceof Collection);
        assertEquals("Description for collection", registry.get("/collection").getDescription());

    }
}