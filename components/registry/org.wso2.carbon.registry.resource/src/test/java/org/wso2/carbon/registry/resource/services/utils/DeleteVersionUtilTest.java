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

public class DeleteVersionUtilTest extends BaseTestCase {

    @Override
    public void setUp() throws RegistryException {
        super.setUp();

        Resource resource = registry.newResource();
        byte[] r1content = RegistryUtils.encodeString("Resource 17 content");
        resource.setContent(r1content);
        resource.setMediaType("application/test");
        resource.setDescription("Sample 17 Description");
        resource.setVersionableChange(true);
        registry.put("/test/2017/10/19", resource);
        registry.createVersion("/test/2017/10/19");
    }


    public void testProcess() throws Exception {
        DeleteVersionUtil.process("/test/2017/10/19", 1, (UserRegistry) registry);
    }

    public void testProcessErrorCase() throws Exception {
        try {
            DeleteVersionUtil.process("/test/2017/10/19", 3, (UserRegistry) registry);
        } catch (RegistryException ex) {
            assertEquals("Failed to delete the version of the 3 shapshot of /test/2017/10/19. The snapshot with the ID: 3 doesn't exists", ex.getMessage());
        }
    }

    @Override
    protected void tearDown() throws Exception {
        registry.delete("/test/2017/10/19");
        super.tearDown();
    }
}