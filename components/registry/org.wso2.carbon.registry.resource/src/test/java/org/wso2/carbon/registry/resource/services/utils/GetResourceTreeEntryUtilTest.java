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
import org.wso2.carbon.registry.resource.beans.ResourceTreeEntryBean;
import org.wso2.carbon.registry.resource.test.base.BaseTestCase;

public class GetResourceTreeEntryUtilTest extends BaseTestCase {

    @Override
    public void setUp() throws RegistryException {
        super.setUp();

        Resource resource = registry.newResource();
        byte[] r1content = RegistryUtils.encodeString("Resource content");
        resource.setContent(r1content);
        resource.setMediaType("application/test");
        registry.put("/test/2017/10/13", resource);
    }

    public void testGetResourceTreeEntryCollection() throws Exception {
        ResourceTreeEntryBean resourceTreeEntryBean = GetResourceTreeEntryUtil.getResourceTreeEntry("/test/2017/10",
                                                                                                    (UserRegistry)
                                                                                                            registry);
        assertNotNull(resourceTreeEntryBean);
        assertTrue(resourceTreeEntryBean.isCollection());
        assertEquals(1, resourceTreeEntryBean.getChildren().length);
        assertEquals("/test/2017/10/13", resourceTreeEntryBean.getChildren()[0]);
    }

    public void testGetResourceTreeEntryResource() throws Exception {
        ResourceTreeEntryBean resourceTreeEntryBean = GetResourceTreeEntryUtil.getResourceTreeEntry("/test/2017/10/13",
                                                                                                    (UserRegistry) registry);
        assertNotNull(resourceTreeEntryBean);
        assertFalse(resourceTreeEntryBean.isCollection());
    }

    @Override
    protected void tearDown() throws Exception {
        registry.delete("/test/2017/10/13");
        super.tearDown();
    }
}
