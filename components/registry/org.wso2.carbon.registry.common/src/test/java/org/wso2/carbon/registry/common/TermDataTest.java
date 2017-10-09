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

public class TermDataTest extends TestCase {

    private TermData termData;


    @Override
    protected void setUp() throws Exception {
        termData = new TermData("soapservice", 1000);
        super.setUp();
    }

    public void testGetTerm() throws Exception {
        assertEquals("soapservice", termData.getTerm());
        termData.setTerm("esbendpoint");
        assertEquals("esbendpoint", termData.getTerm());
    }

    public void testGetFrequency() throws Exception {
        assertEquals(1000, termData.getFrequency());
        termData.setFrequency(500);
        assertEquals(500, termData.getFrequency());
    }
}