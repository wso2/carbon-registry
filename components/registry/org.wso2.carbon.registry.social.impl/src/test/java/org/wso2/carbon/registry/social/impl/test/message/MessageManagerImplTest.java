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
package org.wso2.carbon.registry.social.impl.test.message;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryRealm;
import org.wso2.carbon.registry.social.api.message.Message;
import org.wso2.carbon.registry.social.api.message.MessageCollection;
import org.wso2.carbon.registry.social.api.people.userprofile.model.Url;
import org.wso2.carbon.registry.social.impl.message.MessageCollectionImpl;
import org.wso2.carbon.registry.social.impl.message.MessageImpl;
import org.wso2.carbon.registry.social.impl.message.MessageManagerImpl;
import org.wso2.carbon.registry.social.impl.people.userprofile.model.impl.UrlImpl;
import org.wso2.carbon.registry.social.impl.test.activity.BaseTestCase;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MessageManagerImplTest extends BaseTestCase {
    protected static Registry registry = null;
    protected static RegistryRealm realm = null;
    private MessageManagerImpl manager;

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
    // getMessageCollection()
    // createMessageCollection()
    // modifyMessageCollection()
    // deleteMessageCollection()

    public void testMessageCollections() throws Exception {
        manager = new MessageManagerImpl();
        manager.setRegistry(registry);
        MessageCollection msgColl = new MessageCollectionImpl();
        msgColl.setTitle("inbox");
        msgColl.setUnread(3);
        msgColl.setUpdated(new Date());
        msgColl.setId("1");
        msgColl.setTotal(10);
        List<Url> collectionUrls = new ArrayList<Url>();
        Url url = new UrlImpl();
        url.setValue("testURL");
        collectionUrls.add(url);
        msgColl.setUrls(collectionUrls);
        /* create message collection */
        manager.createMessageCollection("UserZ", msgColl, "1");
        Set<String> fields = new HashSet<String>();
        fields.add("id");
        fields.add("TITLE");
        fields.add("total");
        fields.add("unread");
        fields.add("updated");
        fields.add("urls");
        /* get message collection */
        MessageCollection[] result1 = manager.getMessageCollections("UserZ", fields, null);
        assertNotNull(result1);
        assertEquals(1, result1.length);
        assertEquals(new Integer(10), result1[0].getTotal());
        assertNotNull(result1[0].getTitle());
        assertEquals("inbox", result1[0].getTitle());
        assertNotNull(result1[0].getId());
        assertNotNull(result1[0].getUnread());
        assertNotNull(result1[0].getUpdated());
        assertEquals(msgColl.getUpdated(), result1[0].getUpdated());
        msgColl.setTitle("All Messages");
        msgColl.setUnread(2);
        /* modify message collection */
        manager.modifyMessageCollection("UserZ", msgColl, "1");
        result1 = manager.getMessageCollections("UserZ", fields, null);
        assertNotNull(result1);
        assertEquals(1, result1.length);
        assertEquals(new Integer(10), result1[0].getTotal());
        assertNotNull(result1[0].getTitle());
        assertEquals("All Messages", result1[0].getTitle());
        assertNotNull(result1[0].getId());
        assertNotNull(result1[0].getUnread());
        assertEquals(new Integer(2), result1[0].getUnread());
        assertNotNull(result1[0].getUpdated());
        assertEquals(msgColl.getUpdated(), result1[0].getUpdated());
        /* delete message collection */
        manager.deleteMessageCollection("UserZ", "1");
        result1 = manager.getMessageCollections("UserZ", fields, null);
        assertNull(result1);
    }

    // methods to test
    // createMessage1()
    // createMessage2()
    // getMessage()
    // modifyMessage()
    // deleteMessage()
    public void testMessages() throws Exception {
        manager = new MessageManagerImpl();
        manager.setRegistry(registry);
        MessageCollection msgColl = new MessageCollectionImpl();
        msgColl.setTitle("inbox");
        msgColl.setUnread(3);
        msgColl.setUpdated(new Date());
        msgColl.setId("1");
        msgColl.setTotal(10);
        /* create message */
        manager.createMessageCollection("UserA", msgColl, "1");
        Message message1 = new MessageImpl();
        message1.setId("1");
        message1.setTitle("Test Message");
        message1.setBody("This is a test message");
        message1.setSenderId("UserA");
        manager.createMessage("UserA", "1", message1);
        List<String> msgIds = new ArrayList<String>();
        msgIds.add("1");
        Set<String> fields = new HashSet<String>();
        fields.add("id");
        fields.add("title");
        fields.add("body");
        fields.add("senderId");
        /* get message-1 */
        Message[] message2 = manager.getMessages("UserA", "1", fields, msgIds, null);
        assertNotNull(message2);
        assertEquals(1, message2.length);
        assertNotNull(message2[0]);
        assertEquals("Test Message", message2[0].getTitle());
        assertEquals("This is a test message", message2[0].getBody());
        assertEquals("UserA", message2[0].getSenderId());
        assertEquals("1", message2[0].getId());
        message1.setTitle("New Test Message");
        message1.setSenderId("UserXXX");
         /* modify message */
        manager.modifyMessage("UserA", "1", "1", message1);
        message2 = manager.getMessages("UserA", "1", fields, msgIds, null);
        assertNotNull(message2);
        assertEquals(1, message2.length);
        assertNotNull(message2[0]);
        assertEquals("New Test Message", message2[0].getTitle());
        assertEquals("This is a test message", message2[0].getBody());
        assertEquals("UserXXX", message2[0].getSenderId());
        /* delete message */
        msgIds=new ArrayList<String>();
        msgIds.add("1");
        manager.deleteMessages("UserA","1",msgIds);
        message2 = manager.getMessages("UserA", "1", fields, msgIds, null);
        assertNull(message2);

    }


}
