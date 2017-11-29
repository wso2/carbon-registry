/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.indexing.indexer;

import junit.framework.TestCase;
import org.junit.After;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.Whitebox;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.Tag;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryAuthorizationManager;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryRealm;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.indexing.AsyncIndexer;
import org.wso2.carbon.registry.indexing.IndexingManager;
import org.wso2.carbon.registry.indexing.solr.IndexDocument;
import org.wso2.carbon.registry.indexing.solr.SolrClient;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Properties;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@PrepareForTest({IndexingManager.class, SolrClient.class})
public class IndexDocumentCreatorTest extends TestCase {

    public void testCreateIndexDocument() throws Exception {
        IndexingManager manager = mock(IndexingManager.class);
        PowerMockito.mockStatic(IndexingManager.class);
        Whitebox.setInternalState(IndexingManager.class, "instance", manager);

        ResourceImpl resource = mock(ResourceImpl.class);
        UserRegistry registry = mock(UserRegistry.class);
        when(resource.getAspects()).thenReturn(Arrays.asList("ServiceLifecycle"));
        when(resource.getAuthorUserName()).thenReturn("admin");
        when(resource.getCreatedTime()).thenReturn(Calendar.getInstance().getTime());
        when(resource.getLastModified()).thenReturn(Calendar.getInstance().getTime());
        when(resource.getContent()).thenReturn(null);
        when(resource.getDescription()).thenReturn("Testing Resource");
        when(resource.getMediaType()).thenReturn("application/test");
        when(resource.getPath()).thenReturn("/_system/local/temp");
        Properties properties = new Properties();
        properties.put("key1", Arrays.asList("val1"));
        properties.put("key2", Arrays.asList("val12"));
        when(resource.getProperties()).thenReturn(properties);

        RegistryRealm registryRealm = PowerMockito.mock(RegistryRealm.class);
        RegistryAuthorizationManager authorizationManager = PowerMockito.mock(RegistryAuthorizationManager.class);
        when(authorizationManager.getAllowedRolesForResource("/_system/local/temp", ActionConstants.GET))
                .thenReturn(new String[]{"admin"});
        when(registryRealm.getAuthorizationManager()).thenReturn(authorizationManager);
        Tag tag = new Tag();
        tag.setTagName("aaa");

        when(registry.getTags("/_system/local/temp")).thenReturn(new Tag[]{tag});
        when(registry.get("/_system/local/temp")).thenReturn(resource);
        when(registry.getUserRealm()).thenReturn(registryRealm);
        when(manager.getRegistry(-1234)).thenReturn(registry);

        System.setProperty("carbon.home", "temp");
        SolrClient client = PowerMockito.mock(SolrClient.class);
        PowerMockito.mockStatic(SolrClient.class);
        Whitebox.setInternalState(SolrClient.class, "instance", client);


        final IndexDocument[] document = {null};
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                document[0] = (IndexDocument) args[0];
                return null;
            }
        }).when(client).addDocument((IndexDocument) anyObject());

        String path = "/_system/local/temp";
        String textContent = "testing";
        byte[] byteContent = RegistryUtils.encodeString(textContent);
        String mediaType = "application/text";
        int tenantID = -1234;
        String tenantDomain = "carbon.super";

        AsyncIndexer.File2Index fileData = new AsyncIndexer.File2Index(byteContent, mediaType,
                path, tenantID, tenantDomain);
        IndexDocumentCreator creator = new IndexDocumentCreator(fileData, resource);
        creator.createIndexDocument();
        assertEquals(1, document.length);
        assertEquals("/_system/local/temp", document[0].getPath());
    }

    public void testInitiazeIndexDocument() {
        String path = "/_system/governance/trunk/test";
        String contentAsText = "testing";
        String rawContent = "testing raw content";
        int tenantId = 1234;
        IndexDocument indexDocument = new IndexDocument(path, contentAsText, rawContent, tenantId);
        assertEquals(path, indexDocument.getPath());
        assertEquals(contentAsText, indexDocument.getContentAsText());
        assertEquals(rawContent, indexDocument.getRawContent());
        assertEquals(tenantId, indexDocument.getTenantId());
    }

    @After
    public void cleanUp() {
        System.clearProperty("wso2.registry.xml");
        System.clearProperty("carbon.home");
        System.clearProperty("carbon.config.dir.path");
    }
}
