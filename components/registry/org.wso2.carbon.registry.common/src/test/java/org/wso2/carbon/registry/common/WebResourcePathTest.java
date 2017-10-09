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

package org.wso2.carbon.registry.common;

import junit.framework.TestCase;

public class WebResourcePathTest extends TestCase {

    private WebResourcePath webResourcePath;


    @Override
    protected void setUp() throws Exception {
        webResourcePath = new WebResourcePath();
        super.setUp();
    }

    public void testGetNavigateName() throws Exception {
        assertNull(webResourcePath.getNavigateName());
        webResourcePath.setNavigateName("govregistry");
        assertEquals("govregistry", webResourcePath.getNavigateName());
    }

    public void testGetNavigatePath() throws Exception {
        assertNull(webResourcePath.getNavigatePath());
        webResourcePath.setNavigatePath("/_system/governance");
        assertEquals("/_system/governance", webResourcePath.getNavigatePath());
    }
}