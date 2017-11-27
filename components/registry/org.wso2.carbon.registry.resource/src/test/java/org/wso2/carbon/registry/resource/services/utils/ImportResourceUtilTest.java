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

public class ImportResourceUtilTest extends BaseTestCase {

    @Override
    public void setUp() throws RegistryException {
        super.setUp();

        Resource r1 = registry.newResource();
        byte[] r1content = RegistryUtils.encodeString("R1 content");
        r1.setContent(r1content);
        r1.setMediaType("application/test");
        r1.setProperty("key", "value");
        registry.put("/test/2017/10/25", r1);
    }

    @Override
    protected void tearDown() throws Exception {
        registry.delete("/test/2017/10/25");
        super.tearDown();
    }

    public void testImportResource() throws Exception {

        String[][] properties = new String[][]{{"key1", "val1"}, {"key2", "val2"}};


        String path = ImportResourceUtil.importResource("/test/2017/10", "25", "application/xml", "Sample description",
                                                        "http://blog.napagoda.com/", "test", (UserRegistry) registry,
                                                        properties);

        assertEquals("/test/2017/10/25", path);
        Resource resource = registry.get("/test/2017/10/25");
        assertEquals("application/xml", resource.getMediaType());
        assertEquals("Sample description", resource.getDescription());
        assertEquals("val1", resource.getPropertyValues("key1").get(0));
        assertEquals("val2", resource.getProperty("key2"));
        resource.discard();

    }

    public void testImportResourceError() throws Exception {

        try {
            ImportResourceUtil.importResource("/test/2017/10", "26", "application/xml", "Sample description",
                                              "http://123.wso2.com/", "test", (UserRegistry) registry,
                                              null);
        } catch (RegistryException ex) {
            assertEquals(
                    "Failed to import resource from the URL http://123.wso2.com/ to path /test/2017/10/26. Could not " +
                            "read from the given URL: http://123.wso2.com/",
                    ex.getMessage());
        }

    }

}