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


public class EventTypeTest extends TestCase {

    private EventType eventType;

    @Override
    protected void setUp() throws Exception {
        eventType = new EventType();
        super.setUp();
    }

    public void testGetResourceEvent() throws Exception {
        assertNull(eventType.getResourceEvent());
        eventType.setResourceEvent("event1");
        assertEquals("event1", eventType.getResourceEvent());
    }

    public void testGetCollectionEvent() throws Exception {
        assertNull(eventType.getCollectionEvent());
        eventType.setCollectionEvent("CollectionEvent");
        assertEquals("CollectionEvent", eventType.getCollectionEvent());

    }

    public void testGetId() throws Exception {
        assertNull(eventType.getId());
        eventType.setId("1234567890");
        assertEquals("1234567890", eventType.getId());

    }
}