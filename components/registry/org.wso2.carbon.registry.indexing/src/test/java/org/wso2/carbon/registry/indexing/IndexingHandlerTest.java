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
package org.wso2.carbon.registry.indexing;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.jdbc.Repository;
import org.wso2.carbon.registry.core.jdbc.VersionRepository;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryAuthorizationManager;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryRealm;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.indexing.solr.IndexDocument;
import org.wso2.carbon.registry.indexing.solr.SolrClient;
import org.wso2.carbon.registry.indexing.util.IndexingTestUtils;
import org.wso2.carbon.user.core.service.RealmService;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Calendar;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IndexingHandlerTest {

    private ResourceImpl resource = null;
    private UserRegistry registry = null;
    private Repository repository = null;
    private VersionRepository versionRepository = null;
    private String regResourcePath = "/_system/governance/trunk/soapservices/eu/dataaccess/footballpool/1.0.0/Info";


    @Before
    public void setup() throws Exception {
        Path registryPath = IndexingTestUtils.getResourcePath("registry-indexing.xml");
        assert registryPath != null;
        System.setProperty("wso2.registry.xml", registryPath.toString());
        Path carbonPath = IndexingTestUtils.getResourcePath(".");
        assert carbonPath != null;
        System.setProperty("carbon.home", carbonPath.toString());
        Path resourcePath = IndexingTestUtils.getResourcePath("conf");
        assert resourcePath != null;
        System.setProperty("carbon.config.dir.path", resourcePath.toString());

        registry = mock(UserRegistry.class);
        repository = mock(Repository.class);
        versionRepository = mock(VersionRepository.class);
        resource = new ResourceImpl();
        resource.setPath(regResourcePath);
        resource.setMediaType("application/vnd.wso2-soap-service+xml");
        when(repository.get(regResourcePath)).thenReturn(resource);
        when(registry.get(regResourcePath)).thenReturn(resource);
        when(registry.resourceExists(regResourcePath)).thenReturn(true);

        RegistryRealm registryRealm = mock(RegistryRealm.class);
        RegistryAuthorizationManager authorizationManager = mock(RegistryAuthorizationManager.class);
        when(authorizationManager.getAllowedRolesForResource
                (regResourcePath, ActionConstants.GET)).thenReturn(new String[]{"admin"});
        when(authorizationManager.getAllowedRolesForResource
                (regResourcePath + 1, ActionConstants.GET)).thenReturn(new String[]{"internal/everyone"});
        when(authorizationManager.isUserAuthorized(eq("admin"), anyString(), eq(ActionConstants.GET))).thenReturn
                (true);
        when(registryRealm.getAuthorizationManager()).thenReturn(authorizationManager);
        when(registry.getUserRealm()).thenReturn(registryRealm);


        try (InputStream is = IndexingHandlerTest.class.getClassLoader().getResourceAsStream("registry.xml")) {
            RealmService realmService = mock(RealmService.class);
            RegistryContext registryContext = RegistryContext.getBaseInstance(is, realmService);
            when(registry.getRegistryContext()).thenReturn(registryContext);
        }
        RegistryService registryService = mock(RegistryService.class);
        when(registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME)).thenReturn(registry);
        Utils.setRegistryService(registryService);
    }

    @After
    public void cleanUp() throws NoSuchFieldException, IllegalAccessException {
        System.clearProperty("wso2.registry.xml");
        System.clearProperty("carbon.home");
        System.clearProperty("carbon.config.dir.path");

    }

    @Test
    public void testPut() throws Exception {
        RequestContext requestContext = new RequestContext(registry, repository, versionRepository);
        requestContext.setResource(resource);
        IndexingHandler indexingHandler = new IndexingHandler();
        SolrClient client = mock(SolrClient.class);
        Field instanceField = SolrClient.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, client);
        final IndexDocument[] documents = new IndexDocument[1];
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                documents[0] = (IndexDocument) args[0];
                return null;
            }
        }).when(client).addDocument(any(IndexDocument.class));

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234, true);
            indexingHandler.put(requestContext);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        assertNotNull(documents[0]);
        assertEquals(regResourcePath, documents[0].getPath());
    }

    @Test
    public void testPutCollection() throws Exception {
        RequestContext requestContext = new RequestContext(registry, repository, versionRepository);
        Collection collection = new CollectionImpl();
        requestContext.setResource(collection);
        IndexingHandler indexingHandler = new IndexingHandler();
        SolrClient client = mock(SolrClient.class);
        Field instanceField = SolrClient.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, client);
        final IndexDocument[] documents = new IndexDocument[1];
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                documents[0] = (IndexDocument) args[0];
                return null;
            }
        }).when(client).addDocument(any(IndexDocument.class));

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234, true);
            indexingHandler.put(requestContext);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        assertNull(documents[0]);
    }

    @Test
    public void testDelete() throws Exception {
        RequestContext requestContext = new RequestContext(registry, repository, versionRepository);
        requestContext.setResource(resource);
        requestContext.setResourcePath(new ResourcePath(regResourcePath));
        IndexingHandler indexingHandler = new IndexingHandler();
        SolrClient client = mock(SolrClient.class);
        Field instanceField = SolrClient.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, client);
        final String[] deletePath = new String[1];
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                deletePath[0] = (String) args[0];
                return null;
            }
        }).when(client).deleteFromIndex(anyString(), anyInt());
        AsyncIndexer indexer = new AsyncIndexer();
        Field asyncIndexerField = IndexingHandler.class.getDeclaredField("asyncIndexer");
        asyncIndexerField.setAccessible(true);
        asyncIndexerField.set(null, indexer);
        CurrentSession.setTenantId(-1234);
        indexingHandler.delete(requestContext);
        long timeDifference = 5000;
        long startTime = Calendar.getInstance().getTimeInMillis();
        long endTime = Calendar.getInstance().getTimeInMillis();
        while (deletePath[0] == null && timeDifference > (endTime - startTime)){
            endTime = Calendar.getInstance().getTimeInMillis();
        }
        assertEquals(regResourcePath, deletePath[0]);
    }


    @Test
    public void searchContent() throws Exception {
        RequestContext requestContext = new RequestContext(registry, repository, versionRepository);
        requestContext.setKeywords("testing");
        CurrentSession.setUserRegistry(registry);

        IndexingHandler indexingHandler = new IndexingHandler();
        SolrClient client = mock(SolrClient.class);
        Field instanceField = SolrClient.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, client);
        CurrentSession.setTenantId(-1234);
        SolrDocumentList documentList = new SolrDocumentList();
        SolrDocument document = new SolrDocument();
        document.put("id", regResourcePath + "tenantId-1234");
        documentList.add(document);
        documentList.setNumFound(1);
        when(client.query(anyString(), anyInt())).thenReturn(documentList);

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin");
            Collection results = indexingHandler.searchContent(requestContext);
            assertNotNull(results);
            String[] filteredPaths = new String[]{regResourcePath};
            assertArrayEquals(filteredPaths, (String[]) results.getContent());
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

    }

    @Test
    public void importResource() throws Exception {
        RequestContext requestContext = new RequestContext(registry, repository, versionRepository);
        requestContext.setResource(resource);
        IndexingHandler indexingHandler = new IndexingHandler();
        SolrClient client = mock(SolrClient.class);
        Field instanceField = SolrClient.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, client);
        final IndexDocument[] documents = new IndexDocument[1];
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                documents[0] = (IndexDocument) args[0];
                return null;
            }
        }).when(client).addDocument(any(IndexDocument.class));

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234, true);
            indexingHandler.importResource(requestContext);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        assertNotNull(documents[0]);
        assertEquals(regResourcePath, documents[0].getPath());
    }

}