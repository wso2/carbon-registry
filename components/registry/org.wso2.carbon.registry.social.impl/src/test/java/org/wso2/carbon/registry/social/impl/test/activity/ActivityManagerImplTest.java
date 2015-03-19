/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.registry.social.impl.test.activity;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryRealm;
import org.wso2.carbon.registry.social.api.activity.Activity;
import org.wso2.carbon.registry.social.impl.activity.ActivityImpl;
import org.wso2.carbon.registry.social.impl.activity.ActivityManagerImpl;
import org.wso2.carbon.registry.social.impl.utils.FilterOptionsImpl;

import java.util.HashSet;
import java.util.Set;

public class ActivityManagerImplTest extends BaseTestCase {
    /*protected static Registry registry = null;

    protected static InMemoryEmbeddedRegistryService embeddedRegistryService = null;

    public void setUp() {
        super.setUp();
        if (embeddedRegistryService != null) {
            return;
        }
        try {
            embeddedRegistryService = new InMemoryEmbeddedRegistryService();
            RealmUnawareRegistryCoreServiceComponent comp = new RealmUnawareRegistryCoreServiceComponent();
            comp.setRealmService(embeddedRegistryService.getRealmService());
            comp.registerBuiltInHandlers(embeddedRegistryService);

            // get the realm config to retrieve admin username, password
            RealmConfiguration realmConfig = embeddedRegistryService.getBootstrapRealmConfiguration();
            registry = embeddedRegistryService.getUserRegistry(
                    realmConfig.getAdminUserName(), realmConfig.getAdminPassword());
        } catch (RegistryException e) {
            fail("Failed to initialize the registry. Caused by: " + e.getMessage());
        }

    }*/

    protected static Registry registry = null;
    protected static RegistryRealm realm = null;

    public void setUp() throws RegistryException {
        // below SOP will be removed after the testing.
        System.out.println("ActivityManagerImplTest:setUp()");
        super.setUp();
/*        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain("foo.com");
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(1);*/
        if (registry == null) {
            super.setUp();
            EmbeddedRegistryService embeddedRegistry = ctx.getEmbeddedRegistryService();
            RegistryCoreServiceComponent component = new RegistryCoreServiceComponent() {
                {
                    setRealmService(ctx.getRealmService());
                }
            };
            component.registerBuiltInHandlers(embeddedRegistry);
            registry = embeddedRegistry.getGovernanceUserRegistry("admin", "admin");
        }
    }

    public void test1CreateActivity() throws Exception {
        System.out.println("ActivityManagerImplTest:test1CreateActivity()");
        ActivityManagerImpl manager = new ActivityManagerImpl();
        manager.setRegistry(registry);
        Activity activity1 = new ActivityImpl();
        activity1.setUserId("admin");
        activity1.setId("1");
        activity1.setAppId("1");
        activity1.setTitle("Adding Gadget");
        //manager.saveActivity("admin", activity1);
        manager.createActivity("admin","self","1",null,activity1);
        Activity activity2 = manager.getActivity("admin", "self", "1", null, "1");
        assertNotNull(activity2);
        assertEquals("admin", activity2.getUserId());
        assertEquals("Adding Gadget", activity2.getTitle());
        assertEquals("1",activity2.getId());
        /* Checking the scenario - no activityId specified*/
        activity1 = new ActivityImpl();
        activity1.setUserId("admin");
        activity1.setAppId("99");
        activity1.setTitle("Adding GadgetXXX");
        manager.createActivity("admin","self","99",null,activity1);
        Activity activity3=new ActivityImpl();
        activity3.setUserId("admin");
        activity3.setAppId("99");
        activity3.setTitle("Adding GadgetYYY");
        manager.createActivity("admin","self","99",null,activity3);
        activity2 = manager.getActivity("admin", "self", "99", null, "1");
        assertNotNull(activity2);
        assertEquals("admin", activity2.getUserId());
        assertEquals("Adding GadgetYYY", activity2.getTitle());
        assertEquals("1",activity2.getId());
        String[] userIds=new String[1];
        userIds[0]="admin";
        Activity[] activities=manager.getActivities(userIds,"self","99",null,null);
        assertNotNull(activities);
        assertEquals(2,activities.length);
        assertNotNull(activities[0]);
        assertNotNull(activities[1]);
        assertEquals("Adding GadgetXXX",activities[0].getTitle());
        assertEquals("0",activities[0].getId());
        assertEquals("1",activities[1].getId());
        assertEquals("Adding GadgetYYY",activities[1].getTitle());
    }

    public void test2DeleteActivity() throws Exception {
        // below SOP will be removed after the testing.
        System.out.println("ActivityManagerImplTest:test2DeleteActivity()");
        ActivityManagerImpl manager = new ActivityManagerImpl();
        manager.setRegistry(registry);
        Activity activity1 = new ActivityImpl();
        activity1.setUserId("user1");
        activity1.setId("1");
        activity1.setAppId("1");
        activity1.setTitle("Adding Gadget");
        manager.saveActivity("user1", activity1);
        Activity activity2 = manager.getActivity("user1", "self", "1", null, "1");
        assertNotNull(activity2);
        assertEquals("user1", activity2.getUserId());
        assertEquals("Adding Gadget", activity2.getTitle());
        manager.deleteActivity("user1", "1", "1");
        activity2 = manager.getActivity("user1", "self", "1", null, "1");
        assertNull(activity2);
    }

    public void test3UpdateActivity() throws Exception {
        // below SOP will be removed after the testing.
        System.out.println("ActivityManagerImplTest:testUpdateActivity()");
        ActivityManagerImpl manager = new ActivityManagerImpl();
        manager.setRegistry(registry);
        Activity activity1 = new ActivityImpl();
        activity1.setUserId("userD");
        activity1.setId("4");
        activity1.setAppId("5");
        activity1.setTitle("Adding Gadget");
        manager.saveActivity("userD", activity1);
        Activity activity2 = manager.getActivity("userD", "self", "5", null, "4");
        assertNotNull(activity2);
        assertEquals("userD", activity2.getUserId());
        assertEquals("Adding Gadget", activity2.getTitle());
        activity1.setTitle("New Comment added");
        manager.updateActivity("userD", activity1);
        activity2 = manager.getActivity("userD", "self", "5", null, "4");
        assertNotNull(activity2);
        assertEquals("userD", activity2.getUserId());
        assertEquals("New Comment added", activity2.getTitle());

    }

    public void test4GetActivities() throws Exception {
        // below SOP will be removed after the testing.
        System.out.println("ActivityManagerImplTest:test3GetActivities()");
        ActivityManagerImpl manager = new ActivityManagerImpl();
        manager.setRegistry(registry);
        Activity activity1 = new ActivityImpl();
        activity1.setUserId("userX");
        activity1.setId("4");
        activity1.setAppId("5");
        activity1.setTitle("Adding Gadget 1 ");
        manager.saveActivity("userX", activity1);
        Activity activity2 = new ActivityImpl();
        activity1.setUserId("userY");
        activity1.setId("8");
        activity1.setAppId("5");
        activity1.setTitle("Adding Gadget 2 ");
        manager.saveActivity("userY", activity1);
        activity1.setUserId("userY");
        activity1.setId("9");
        activity1.setAppId("5");
        activity1.setTitle("Adding Gadget 3 ");
        manager.saveActivity("userY", activity1);
        String[] users = new String[]{"userX", "userY"};
        Activity[] activities = manager.getActivities(users, "self", "5", null, new FilterOptionsImpl());
        assertEquals(3, activities.length);
        boolean test = false, test2 = false;
        for (Activity act : activities) {
            if (act.getTitle().equals("Adding Gadget 3 ")) {
                test = true;
            }
            if (act.getTitle().equals("Adding Gadget 2 ")) {
                test2 = true;
            }
        }
        assertTrue(test);
        assertTrue(test2);
    }


    public void test5GetActivities2() throws Exception {
        // below SOP will be removed after the testing.
        System.out.println("ActivityManagerImplTest:test4GetActivities2()");
        ActivityManagerImpl manager = new ActivityManagerImpl();
        manager.setRegistry(registry);
        Activity activity1 = new ActivityImpl();
        activity1.setUserId("userE");
        activity1.setId("9");
        activity1.setAppId("5");
        activity1.setTitle("Adding Gadget 1 ");
        manager.saveActivity("userE", activity1);
        activity1.setUserId("userE");
        activity1.setId("10");
        activity1.setAppId("5");
        activity1.setTitle("Adding Gadget 2 ");
        manager.saveActivity("userE", activity1);
        activity1.setUserId("userE");
        activity1.setId("8");
        activity1.setAppId("5");
        activity1.setTitle("Adding Gadget 3 ");
        manager.saveActivity("userE", activity1);
        Activity[] activities = manager.getActivities("userE", "self", "5", null, new FilterOptionsImpl(), new String[]{"8", "9", "10"});
        assertEquals(3, activities.length);
        boolean test = false, test2 = false;
        for (Activity act : activities) {
            if (act.getTitle().equals("Adding Gadget 3 ")) {
                test = true;
            }
            if (act.getTitle().equals("Adding Gadget 2 ")) {
                test2 = true;
            }
        }
        assertTrue(test);
        assertTrue(test2);

    }

    public void test6DeleteActivities() throws Exception {
        // below SOP will be removed after the testing.
        System.out.println("ActivityManagerImplTest:test5DeleteActivities()");
        ActivityManagerImpl manager = new ActivityManagerImpl();
        manager.setRegistry(registry);
        Activity activity1 = new ActivityImpl();
        activity1.setUserId("userA");
        activity1.setId("19");
        activity1.setAppId("7");
        activity1.setTitle("Adding Gadget 1 ");
        manager.saveActivity("userA", activity1);
        activity1.setUserId("userA");
        activity1.setId("20");
        activity1.setAppId("7");
        activity1.setTitle("Adding Gadget 2 ");
        manager.saveActivity("userA", activity1);
        activity1.setUserId("userA");
        activity1.setId("18");
        activity1.setAppId("7");
        activity1.setTitle("Adding Gadget 3 ");
        manager.saveActivity("userA", activity1);
        Activity[] activities = manager.getActivities(new String[]{"userA"}, "self", "7", null, new FilterOptionsImpl());
        assertEquals(3, activities.length);
        Set<String> activityIds = new HashSet<String>();

        activityIds.add("18");
        activityIds.add("20");
        manager.deleteActivities("userA", null, "7", activityIds);
        activities = manager.getActivities(new String[]{"userA"}, "self", "7", null, new FilterOptionsImpl());
        assertEquals(1, activities.length);
        activity1=manager.getActivity("userA",null,"7",null,"18");
        assertNull(activity1);
    }


}