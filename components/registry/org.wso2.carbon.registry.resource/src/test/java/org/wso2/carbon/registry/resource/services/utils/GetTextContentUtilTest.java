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
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.resource.test.base.BaseTestCase;

import javax.activation.DataHandler;

public class GetTextContentUtilTest extends BaseTestCase {

    @Override
    public void setUp() throws RegistryException {
        super.setUp();

        Resource r1 = registry.newResource();
        byte[] r1content = RegistryUtils.encodeString("R1 content");
        r1.setContent(r1content);
        registry.put("/test/2017/10/11", r1);
    }


    public void testGetTextContent() throws Exception {

        String content = GetTextContentUtil.getTextContent("/test/2017/10/11", registry);
        assertEquals("R1 content", content);
    }

    public void testGetTextContentComplexPath() throws Exception {

        String content = GetTextContentUtil.getTextContent("/test/2017/10/../10/11", registry);
        assertEquals("R1 content", content);
    }

    public void testGetTextContentErrorCase() throws Exception {

        try {
            GetTextContentUtil.getTextContent("../test/2017/10/11", registry);
        } catch (RegistryException ex) {
            assertEquals("Could not get the content of the resource null. Caused by: Resource path is null",
                         ex.getMessage());
        }
    }

    public void testGetByteContent() throws Exception {
        DataHandler dataHandler = GetTextContentUtil.getByteContent("http://blog.napagoda.com");
        assertNotNull(dataHandler);
    }

    public void testGetByteContentWrongURl() throws Exception {
        try {
            GetTextContentUtil.getByteContent("http://blog123.napagoda.com");
        } catch (RegistryException ex) {
            assertEquals("Wrong or unavailable source URL http://blog123.napagoda.com.", ex.getMessage());
        }
    }

    @Override
    protected void tearDown() throws Exception {
        registry.delete("/test/2017/10/11");
        super.tearDown();
    }
}
