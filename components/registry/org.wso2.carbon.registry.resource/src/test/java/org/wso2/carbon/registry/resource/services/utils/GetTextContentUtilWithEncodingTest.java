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

import junit.framework.TestCase;

import javax.activation.DataHandler;


public class GetTextContentUtilWithEncodingTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        System.setProperty("carbon.registry.character.encoding", "UTF-8");
        super.setUp();
    }

    public void testGetByteContent() throws Exception {
        DataHandler dataHandler = GetTextContentUtil.getByteContent("http://google.com");
        assertNotNull(dataHandler);
    }

    @Override
    protected void tearDown() throws Exception {
        System.clearProperty("carbon.registry.character.encoding");
        super.tearDown();
    }
}
