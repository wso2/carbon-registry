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

package org.wso2.carbon.registry.common.beans;

import junit.framework.TestCase;
import org.wso2.carbon.registry.common.beans.utils.SubscriptionInstance;

public class SubscriptionBeanTest extends TestCase {

    private SubscriptionBean subscriptionBean;

    @Override
    protected void setUp() throws Exception {
        subscriptionBean = new SubscriptionBean();
        super.setUp();
    }

    public void testGetVersionView() throws Exception {
        assertFalse(subscriptionBean.getVersionView());
        subscriptionBean.setVersionView(true);
        assertTrue(subscriptionBean.getVersionView());
    }

    public void testGetUserName() throws Exception {
        assertNull(subscriptionBean.getUserName());
        subscriptionBean.setUserName("Chandana");
        assertEquals("Chandana", subscriptionBean.getUserName());
    }

    public void testGetRoles() throws Exception {
        assertNull(subscriptionBean.getRoles());
        String[] roles = new String[2];
        roles[0] = "Admin";
        roles[1] = "Danesh";
        subscriptionBean.setRoles(roles);
        assertEquals(2, subscriptionBean.getRoles().length);
    }

    public void testGetUserAccessLevel() throws Exception {
        assertEquals(0, subscriptionBean.getUserAccessLevel());

        subscriptionBean.setUserAccessLevel(SubscriptionBean.UserAccessLevel.AUTHORIZE);
        assertEquals(SubscriptionBean.UserAccessLevel.AUTHORIZE, subscriptionBean.getUserAccessLevel());

        subscriptionBean.setUserAccessLevel(SubscriptionBean.UserAccessLevel.DELETE);
        assertEquals(SubscriptionBean.UserAccessLevel.DELETE, subscriptionBean.getUserAccessLevel());

        subscriptionBean.setUserAccessLevel(SubscriptionBean.UserAccessLevel.READ);
        assertEquals(SubscriptionBean.UserAccessLevel.READ, subscriptionBean.getUserAccessLevel());

        subscriptionBean.setUserAccessLevel(SubscriptionBean.UserAccessLevel.NONE);
        assertEquals(SubscriptionBean.UserAccessLevel.NONE, subscriptionBean.getUserAccessLevel());
    }

    public void testGetRoleAccessLevel() throws Exception {

        assertEquals(0, subscriptionBean.getRoleAccessLevel());

        subscriptionBean.setRoleAccessLevel(SubscriptionBean.UserAccessLevel.AUTHORIZE);
        assertEquals(SubscriptionBean.UserAccessLevel.AUTHORIZE, subscriptionBean.getRoleAccessLevel());

        subscriptionBean.setRoleAccessLevel(SubscriptionBean.UserAccessLevel.DELETE);
        assertEquals(SubscriptionBean.UserAccessLevel.DELETE, subscriptionBean.getRoleAccessLevel());

        subscriptionBean.setRoleAccessLevel(SubscriptionBean.UserAccessLevel.READ);
        assertEquals(SubscriptionBean.UserAccessLevel.READ, subscriptionBean.getRoleAccessLevel());

        subscriptionBean.setRoleAccessLevel(SubscriptionBean.UserAccessLevel.NONE);
        assertEquals(SubscriptionBean.UserAccessLevel.NONE, subscriptionBean.getRoleAccessLevel());
    }

    public void testGetErrorMessage() throws Exception {
        assertNull(subscriptionBean.getErrorMessage());
        subscriptionBean.setErrorMessage("Error Message");
        assertEquals("Error Message", subscriptionBean.getErrorMessage());
    }

    public void testGetSubscriptionInstances() throws Exception {
        assertNull(subscriptionBean.getSubscriptionInstances());
        SubscriptionInstance[] subscriptionInstances = new SubscriptionInstance[1];
        SubscriptionInstance subscriptionInstance = new SubscriptionInstance();
        subscriptionBean.setSubscriptionInstances(subscriptionInstances);
        assertEquals(1, subscriptionBean.getSubscriptionInstances().length);

        subscriptionInstances[0] = subscriptionInstance;
        subscriptionBean.setSubscriptionInstances(subscriptionInstances);

        assertEquals(1, subscriptionBean.getSubscriptionInstances().length);

        assertEquals(subscriptionInstance, subscriptionBean.getSubscriptionInstances()[0]);
    }

    public void testGetLoggedIn() throws Exception {
        assertFalse(subscriptionBean.getLoggedIn());
        subscriptionBean.setLoggedIn(true);
        assertTrue(subscriptionBean.getLoggedIn());
    }

    public void testGetPathWithVersion() throws Exception {
        assertNull(subscriptionBean.getPathWithVersion());

        subscriptionBean.setPathWithVersion("/_system/governance/trunk:version=1");

        assertEquals("/_system/governance/trunk:version=1", subscriptionBean.getPathWithVersion());
    }
}