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

package org.wso2.carbon.registry.common.beans.utils;

import junit.framework.TestCase;

public class TagTest extends TestCase {

    private Tag tag;

    @Override
    protected void setUp() throws Exception {
        tag = new Tag();
        super.setUp();
    }

    public void testGetTagName() throws Exception {
        assertNull(tag.getTagName());
        tag.setTagName("wso2");
        assertEquals("wso2", tag.getTagName());
    }

    public void testGetTagCount() throws Exception {
        tag.setTagCount(1);
        assertEquals(1, tag.getTagCount());
    }

    public void testGetCategory() throws Exception {
        assertEquals(1, tag.getCategory());

        tag.setTagCount(4);
        assertEquals(2, tag.getCategory());

        tag.setTagCount(14);
        assertEquals(3, tag.getCategory());

        tag.setTagCount(34);
        assertEquals(4, tag.getCategory());

        tag.setTagCount(64);
        assertEquals(5, tag.getCategory());

    }
}