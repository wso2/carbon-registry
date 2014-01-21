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
package org.wso2.carbon.registry.social.impl.test.people.relationship;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryRealm;
import org.wso2.carbon.registry.social.impl.SocialImplConstants;
import org.wso2.carbon.registry.social.impl.people.relationship.RelationshipManagerImpl;
import org.wso2.carbon.registry.social.impl.test.activity.BaseTestCase;

public class RelationshipManagerImplTest extends BaseTestCase {

    protected static Registry registry = null;
    protected static RegistryRealm realm = null;
    private RelationshipManagerImpl manager;

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
/*
    public String[] getRelationshipList(String user)throws Exception{
        setUp();
         manager = new RelationshipManagerImpl();
        manager.setRegistry(registry);
         return manager.getRelationshipList(user);

    }*/
    public void testRelationshipRequesting() throws Exception {
        manager = new RelationshipManagerImpl();
        manager.setRegistry(registry);
        manager.requestRelationship("UserA", "UserB");
        String[] requests = manager.getPendingRelationshipRequests("UserB");
        assertEquals(requests.length, 1);
        manager.requestRelationship("UserC", "UserB");
        requests = manager.getPendingRelationshipRequests("UserB");
        assertEquals(requests.length, 2);
        assertEquals(manager.getRelationshipStatus("UserA", "UserB"), SocialImplConstants.RELATIONSHIP_STATUS_REQUEST_PENDING);
        assertEquals(manager.getRelationshipStatus("UserA", "UserC"), SocialImplConstants.RELATIONSHIP_STATUS_NONE);
        assertEquals(manager.getRelationshipStatus("UserC", "UserB"), SocialImplConstants.RELATIONSHIP_STATUS_REQUEST_PENDING);
        assertEquals(manager.getRelationshipStatus("UserB", "UserA"), SocialImplConstants.RELATIONSHIP_STATUS_REQUEST_RECEIVED);
        assertEquals(manager.getRelationshipStatus("UserB", "UserC"), SocialImplConstants.RELATIONSHIP_STATUS_REQUEST_RECEIVED);
        manager.acceptRelationshipRequest("UserB", "UserA");
        assertEquals(manager.getRelationshipStatus("UserB", "UserA"), SocialImplConstants.RELATIONSHIP_STATUS_FRIEND);
        assertEquals(manager.getRelationshipStatus("UserA", "UserB"), SocialImplConstants.RELATIONSHIP_STATUS_FRIEND);
        manager.requestRelationship("UserA", "UserC");
        assertEquals(manager.getRelationshipStatus("UserC", "UserA"), SocialImplConstants.RELATIONSHIP_STATUS_REQUEST_RECEIVED);
        manager.ignoreRelationship("UserC", "UserA");
        assertEquals(manager.getRelationshipStatus("UserC", "UserA"), SocialImplConstants.RELATIONSHIP_STATUS_NONE);
        manager.removeRelationship("UserA", "UserB");
        assertEquals(manager.getRelationshipStatus("UserB", "UserA"), SocialImplConstants.RELATIONSHIP_STATUS_NONE);
        assertEquals(manager.getRelationshipStatus("UserA", "UserB"), SocialImplConstants.RELATIONSHIP_STATUS_NONE);
        assertEquals(manager.getPendingRelationshipRequests("UserB").length, 1);
        assertEquals(manager.getRelationshipList("UserA").length, 0);
        assertEquals(manager.getRelationshipStatus("userA","userA"),SocialImplConstants.RELATIONSHIP_STATUS_SELF);
        assertNull(manager.getRelationshipStatus("userB",""));
    }


}

