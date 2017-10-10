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

public class SubscriptionInstanceTest extends TestCase {

    private SubscriptionInstance subscriptionInstance;

    @Override
    protected void setUp() throws Exception {
        subscriptionInstance = new SubscriptionInstance();
        super.setUp();
    }

    public void testGetAddress() throws Exception {
        assertNull(subscriptionInstance.getAddress());
        subscriptionInstance.setAddress("SOAP");
        assertEquals("SOAP", subscriptionInstance.getAddress());
    }

    public void testGetEventName() throws Exception {
        assertNull(subscriptionInstance.getEventName());
        subscriptionInstance.setEventName("adminConsole");
        assertEquals("adminConsole", subscriptionInstance.getEventName());
    }

    public void testGetDigestType() throws Exception {
        assertNull(subscriptionInstance.getDigestType());
        subscriptionInstance.setDigestType("SOAP");
        assertEquals("SOAP", subscriptionInstance.getDigestType());
    }

    public void testGetTopic() throws Exception {
        assertNull(subscriptionInstance.getTopic());
        subscriptionInstance.setTopic("Topic");
        assertEquals("Topic", subscriptionInstance.getTopic());
    }

    public void testGetNotificationMethod() throws Exception {
        assertNull(subscriptionInstance.getNotificationMethod());
        subscriptionInstance.setNotificationMethod("email");
        assertEquals("email", subscriptionInstance.getNotificationMethod());
    }

    public void testGetId() throws Exception {
        assertNull(subscriptionInstance.getId());
        subscriptionInstance.setId("1234567890");
        assertEquals("1234567890", subscriptionInstance.getId());
    }

    public void testGetSubManUrl() throws Exception {
        assertNull(subscriptionInstance.getSubManUrl());
        subscriptionInstance.setSubManUrl("http://localhost:9443/registry");
        assertEquals("http://localhost:9443/registry", subscriptionInstance.getSubManUrl());
    }

    public void testGetOwner() throws Exception {
        assertNull(subscriptionInstance.getOwner());
        subscriptionInstance.setOwner("admin");
        assertEquals("admin", subscriptionInstance.getOwner());
    }
}