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

import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.test.base.BaseTestCase;


public class AddResourceUtilTest extends BaseTestCase {

    @Override
    public void setUp() throws RegistryException {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        registry.delete("/sample/resource");
        super.tearDown();
    }

    public void testAddResource() throws Exception {

        String[][] properties = new String[][]{{"key1", "val1"}, {"key2", "val2"}};

        AddResourceUtil.addResource("/sample/resource", "application/xml", "Sample description", null, null,
                                    (UserRegistry) registry, properties);

        assertTrue(registry.resourceExists("/sample/resource"));

    }
}