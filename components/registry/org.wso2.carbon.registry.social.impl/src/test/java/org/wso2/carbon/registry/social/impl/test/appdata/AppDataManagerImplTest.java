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
package org.wso2.carbon.registry.social.impl.test.appdata;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryRealm;
import org.wso2.carbon.registry.social.impl.appdata.AppDataManagerImpl;
import org.wso2.carbon.registry.social.impl.test.activity.BaseTestCase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AppDataManagerImplTest extends BaseTestCase {
    protected static Registry registry = null;
    protected static RegistryRealm realm = null;

    public void setUp() throws RegistryException {
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


    // methods to test
    //  savePersonData()
    //  updatePersonData()
    //  deletePersonData()
    //  getPersonData()


    public void testAppDataManager() throws Exception {
        AppDataManagerImpl manager = new AppDataManagerImpl();
        manager.setRegistry(registry);
        Map<String, String> personData = new HashMap<String, String>();
        personData.put("key1", "value1");
        personData.put("key2", "value2");
        personData.put("key3", "value3");
        /* save person data */
        manager.savePersonData("user1", "3", personData);
        Map<String, Map<String, String>> dataMap1;
        String[] userIds = new String[1];
        userIds[0]="user1";
        Set<String> fields = new HashSet<String>();
        fields.add("key1");
        fields.add("key2");
        /* get person data  */
        dataMap1 = manager.getPersonData(userIds, null, "3", fields);
        assertNotNull(dataMap1);
        assertEquals(dataMap1.size(), 1);
        assertEquals(dataMap1.get("user1").size(), 2);
        assertTrue(dataMap1.get("user1").get("key1") != null && dataMap1.get("user1").get("key2") != null && dataMap1.get("user1").get("key1").equals("value1"));
        assertNull(dataMap1.get("userC"));
        userIds = new String[1];
        userIds[0]="useC";
        dataMap1 = manager.getPersonData(userIds, null, "3", fields);
       // assertNull(dataMap1);
        fields = new HashSet<String>();
        fields.add("key1");
        /* Delete person data */
        manager.deletePersonData("user1", null, "3", fields);
        userIds = new String[1];
        userIds[0]="user1";
        fields = new HashSet<String>();
        fields.add("key1");
        fields.add("key2");
        fields.add("key3");
        dataMap1 = manager.getPersonData(userIds, null, "3", fields);
        assertNotNull(dataMap1);
        assertNull(dataMap1.get("user1").get("key1"));
        assertEquals(dataMap1.size(), 1);
        assertEquals(2, dataMap1.get("user1").size());
        assertTrue(dataMap1.get("user1").get("key3") != null && dataMap1.get("user1").get("key2") != null && dataMap1.get("user1").get("key3").equals("value3"));
        personData = new HashMap<String, String>();
        personData.put("key2", "newvalue2");
        fields = new HashSet<String>();
        fields.add("key2");
        /* Update person data */
        manager.updatePersonData("user1", null, "3", fields, personData);
        fields = new HashSet<String>();
        fields.add("key2");
        dataMap1 = manager.getPersonData(userIds, null, "3", fields);
        assertNotNull(dataMap1);
        assertEquals(dataMap1.size(), 1);
        assertEquals(1, dataMap1.get("user1").size());
        assertEquals("newvalue2", dataMap1.get("user1").get("key2"));

        /* value in fields but not in map */
        personData = new HashMap<String, String>();
        personData.put("key2", "newvalue2");
        fields = new HashSet<String>();
        fields.add("key2");
        fields.add("key3");
        manager.updatePersonData("user1", null, "3", fields, personData);
        fields = new HashSet<String>();
        fields.add("key2");
        dataMap1 = manager.getPersonData(userIds, null, "3", fields);
        assertNotNull(dataMap1);
        assertNull(dataMap1.get("user1").get("key3"));         // value for key3 should be deleted


    }

}
