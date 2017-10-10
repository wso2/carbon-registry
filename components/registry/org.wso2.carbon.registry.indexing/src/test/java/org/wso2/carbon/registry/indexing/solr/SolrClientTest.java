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
package org.wso2.carbon.registry.indexing.solr;

import junit.framework.TestCase;
import org.apache.axis2.context.MessageContext;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryRealm;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.indexing.AsyncIndexer;
import org.wso2.carbon.registry.indexing.IndexingConstants;
import org.wso2.carbon.registry.indexing.SolrConstants;
import org.wso2.carbon.registry.indexing.Utils;
import org.wso2.carbon.registry.indexing.indexer.JSONIndexer;
import org.wso2.carbon.registry.indexing.util.IndexingTestUtils;
import org.wso2.carbon.user.core.UserStoreManager;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SolrClient.class})
public class SolrClientTest extends TestCase {
    SolrClient client;
    EmbeddedSolrServer server;


    @Before
    public void setUp() throws Exception {
        Path carbonHome = Paths.get("target", "test-classes", "carbon-home");
        System.setProperty("carbon.home", carbonHome.toString());
        Path registryPath = IndexingTestUtils.getResourcePath("registry-indexing.xml");
        System.setProperty("wso2.registry.xml", registryPath.toString());
        System.setProperty("carbon.home", registryPath.toString());
        Path resourcePath = IndexingTestUtils.getResourcePath("conf");
        System.setProperty("carbon.config.dir.path", resourcePath.toString());

        CoreContainer coreContainer = mock(CoreContainer.class);
        whenNew(CoreContainer.class).withParameterTypes(String.class).withArguments(anyString())
                .thenReturn(coreContainer);
        server = mock(EmbeddedSolrServer.class);
        whenNew(EmbeddedSolrServer.class).withParameterTypes(CoreContainer.class, String.class).withArguments
                (anyObject(), anyString()).thenReturn(server);
        client = SolrClient.getInstance();
        MessageContext.currentMessageContext.remove();
    }

    @After
    public void cleanUp() throws NoSuchFieldException, IllegalAccessException {
        System.clearProperty("wso2.registry.xml");
        System.clearProperty("carbon.home");
        System.clearProperty("carbon.config.dir.path");
        Field field = SolrClient.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(client, null);
        server = null;
        client = null;
    }

    public void testDeleteIndexByQuery() throws Exception {
        try {
            String query = "overview_name_s:sampleApi";
            when(server.deleteByQuery(anyString())).thenReturn(null);
            when(server.commit()).thenThrow(new SolrServerException("Error while commiting the changes"));
            client.deleteIndexByQuery(query);
            fail("Missing exception");
        } catch (SolrException e) {
            assertEquals("Failure at deleting", e.getMessage());
        }
    }


    public void testGenerateIdInAddDocument() throws Exception {
        IndexDocument indexDocument = new IndexDocument();
        indexDocument.setPath("/_system/governance/trunk/test");
        indexDocument.setContentAsText("Test Indexing");
        indexDocument.setRawContent("Testing...");
        indexDocument.setTenantId(-12345);

        final SolrInputDocument[] document = {null};
        when(server.add((SolrInputDocument) anyObject())).thenAnswer(new Answer<UpdateResponse>() {
            @Override
            public UpdateResponse answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                document[0] = (SolrInputDocument) args[0];
                return null;
            }
        });

        SolrClient.getInstance().addDocument(indexDocument);
        assertNotNull(document[0]);
        assertEquals("/_system/governance/trunk/testtenantId-12345", document[0].get("id").getValue());
    }


    public void testAddDocumentWithoutPath() throws Exception {
        IndexDocument indexDocument = new IndexDocument();
        indexDocument.setContentAsText("Test Indexing");
        indexDocument.setRawContent("Testing...");
        indexDocument.setTenantId(-12345);

        final SolrInputDocument[] document = {null};
        when(server.add((SolrInputDocument) anyObject())).thenAnswer(new Answer<UpdateResponse>() {
            @Override
            public UpdateResponse answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                document[0] = (SolrInputDocument) args[0];
                return null;
            }
        });

        SolrClient.getInstance().addDocument(indexDocument);
        assertNotNull(document[0]);
        assertEquals("nulltenantId-12345", document[0].get("id").getValue());
    }

    public void testPropertiesInAddDocument() throws Exception {
        IndexDocument indexDocument = new IndexDocument();
        indexDocument.setPath("/_system/governance/trunk/test2");
        indexDocument.setContentAsText("Test Indexing");
        indexDocument.setRawContent("Testing...");
        indexDocument.setTenantId(-12345);
        List<String> propertyList = new ArrayList<String>();
        propertyList.add("key1,a1,a2,a3");
        propertyList.add("key2,1,2,3,4");
        propertyList.add("key3,1.2,2.4,3.5,5.6,6.7");
        Map<String,List<String>> fields = new HashMap<>();
        fields.put(IndexingConstants.FIELD_PROPERTY_VALUES, propertyList);
        indexDocument.setFields(fields);

        final SolrInputDocument[] document = {null};
        when(server.add((SolrInputDocument) anyObject())).thenAnswer(new Answer<UpdateResponse>() {
            @Override
            public UpdateResponse answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                document[0] = (SolrInputDocument) args[0];
                return null;
            }
        });

        SolrClient.getInstance().addDocument(indexDocument);
        assertNotNull(document[0].getFieldValues("key1_ss"));
        assertEquals(3, document[0].getFieldValues("key1_ss").size());
        assertArrayEquals(new String[]{"a1","a2","a3"}, document[0].getFieldValues("key1_ss").toArray());
        assertNotNull(document[0].getFieldValues("key2_is"));
        assertEquals(4, document[0].getFieldValues("key2_is").size());
        assertArrayEquals(new Integer[]{1,2,3,4}, document[0].getFieldValues("key2_is").toArray());
        assertNotNull(document[0].getFieldValues("key3_ds"));
        assertEquals(5, document[0].getFieldValues("key3_ds").size());
        assertArrayEquals(new Double[]{1.2,2.4,3.5,5.6,6.7}, document[0].getFieldValues("key3_ds")
                .toArray());
    }

    public void testMultitiValuedInAddDocument() throws Exception {
        IndexDocument indexDocument = new IndexDocument();
        indexDocument.setPath("/_system/governance/trunk/test");
        indexDocument.setContentAsText("Test Indexing");
        indexDocument.setRawContent("Testing...");
        indexDocument.setTenantId(-12345);
        List<String> tagsList = new ArrayList<String>();
        tagsList.add("tag1");
        tagsList.add("tag2");
        tagsList.add("tag3");
        Map<String,List<String>> fields = new HashMap<>();
        fields.put(IndexingConstants.FIELD_TAGS, tagsList);
        indexDocument.setFields(fields);

        final SolrInputDocument[] document = {null};
        when(server.add((SolrInputDocument) anyObject())).thenAnswer(new Answer<UpdateResponse>() {
            @Override
            public UpdateResponse answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                document[0] = (SolrInputDocument) args[0];
                return null;
            }
        });

        SolrClient.getInstance().addDocument(indexDocument);
        assertNotNull(document[0]);
        assertEquals(3, document[0].getFieldValues("tags_ss").size());
        assertArrayEquals(tagsList.toArray(), document[0].getFieldValues("tags_ss").toArray());
    }

    public void testAllowedRolesInAddDocument() throws Exception {
        IndexDocument indexDocument = new IndexDocument();
        indexDocument.setPath("/_system/governance/trunk/test");
        indexDocument.setContentAsText("Test Indexing");
        indexDocument.setRawContent("Testing...");
        indexDocument.setTenantId(-12345);
        List<String> rolesList = new ArrayList<String>();
        rolesList.add("role1");
        rolesList.add("role2");
        rolesList.add("role3");
        Map<String,List<String>> fields = new HashMap<>();
        fields.put(IndexingConstants.FIELD_ALLOWED_ROLES, rolesList);
        indexDocument.setFields(fields);

        final SolrInputDocument[] document = {null};
        when(server.add((SolrInputDocument) anyObject())).thenAnswer(new Answer<UpdateResponse>() {
            @Override
            public UpdateResponse answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                document[0] = (SolrInputDocument) args[0];
                return null;
            }
        });

        SolrClient.getInstance().addDocument(indexDocument);
        assertNotNull(document[0]);
        assertEquals(3, document[0].getFieldValues("allowedRoles").size());
        assertArrayEquals(rolesList.toArray(), document[0].getFieldValues("allowedRoles").toArray());
    }

    public void testDateFieldsInAddDocument() throws Exception {
        IndexDocument indexDocument = new IndexDocument();
        indexDocument.setPath("/_system/governance/trunk/test");
        indexDocument.setContentAsText("Test Indexing");
        indexDocument.setRawContent("Testing...");
        indexDocument.setTenantId(-12345);
        List<String> dateList = new ArrayList<>();
        Date createdDate = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat(SolrConstants.REG_LOG_DATE_FORMAT, Locale.ENGLISH);
        dateList.add(sdf.format(createdDate));

        Map<String,List<String>> fields = new HashMap<>();
        fields.put(IndexingConstants.FIELD_CREATED_DATE, dateList);
        fields.put(IndexingConstants.FIELD_LAST_UPDATED_DATE, dateList);
        indexDocument.setFields(fields);

        final SolrInputDocument[] document = {null};
        when(server.add((SolrInputDocument) anyObject())).thenAnswer(new Answer<UpdateResponse>() {
            @Override
            public UpdateResponse answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                document[0] = (SolrInputDocument) args[0];
                return null;
            }
        });

        SolrClient.getInstance().addDocument(indexDocument);
        assertNotNull(document[0]);
        SimpleDateFormat indexSdf = new SimpleDateFormat(SolrConstants.SOLR_DATE_FORMAT, Locale.ENGLISH);
        assertEquals(indexSdf.format(createdDate), document[0].getFieldValues(IndexingConstants.FIELD_CREATED_DATE +
                SolrConstants
                .SOLR_DATE_FIELD_KEY_SUFFIX).toArray()[0]);
        assertEquals(indexSdf.format(createdDate), document[0].getFieldValues(IndexingConstants.FIELD_LAST_UPDATED_DATE +
                SolrConstants
                        .SOLR_DATE_FIELD_KEY_SUFFIX).toArray()[0]);
    }

    public void testResourceNameInAddDocument() throws Exception {
        IndexDocument indexDocument = new IndexDocument();
        indexDocument.setPath("/_system/governance/trunk/test");
        indexDocument.setContentAsText("Test Indexing");
        indexDocument.setRawContent("Testing...");
        indexDocument.setTenantId(-12345);
        List<String> nameList = new ArrayList<String>();
        nameList.add("test");

        Map<String,List<String>> fields = new HashMap<>();
        fields.put(IndexingConstants.FIELD_RESOURCE_NAME, nameList);
        indexDocument.setFields(fields);

        final SolrInputDocument[] document = {null};
        when(server.add((SolrInputDocument) anyObject())).thenAnswer(new Answer<UpdateResponse>() {
            @Override
            public UpdateResponse answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                document[0] = (SolrInputDocument) args[0];
                return null;
            }
        });

        SolrClient.getInstance().addDocument(indexDocument);
        assertNotNull(document[0]);
        assertEquals("test", document[0].getField(IndexingConstants
                .FIELD_RESOURCE_NAME + SolrConstants.SOLR_STRING_FIELD_KEY_SUFFIX).getValue());
    }

    public void testExceptionInAddDocument() throws Exception {
        IndexDocument indexDocument = new IndexDocument();
        indexDocument.setPath("/_system/governance/trunk/test");
        indexDocument.setContentAsText("Test Indexing");
        indexDocument.setRawContent("Testing...");
        indexDocument.setTenantId(-12345);

        try {
            when(server.add((SolrInputDocument) anyObject())).thenThrow(new SolrServerException( "Testing..."));
            SolrClient.getInstance().addDocument(indexDocument);
            fail("Exception is missing when server fail to execute");
        } catch (SolrException e) {
            assertEquals("Error at indexing.", e.getMessage());
        }
    }

    public void testIndexDocument() throws Exception {
        String jsonContent = "{\n" +
                "\tcolor: \"red\",\n" +
                "\tvalue: \"#f00\"\n" +
                "}";
        String mediaType = "application/json";
        int tenantID = -1234;
        String tenantDomain = "carbon.super";
        String path = "/_system/local/temp";
        byte[] byteContent = RegistryUtils.encodeString(jsonContent);
        AsyncIndexer.File2Index fileData = new AsyncIndexer.File2Index(byteContent, mediaType,
                path, tenantID, tenantDomain);
        JSONIndexer jsonIndexer = new JSONIndexer();
        final SolrInputDocument[] document = {null};
        when(server.add((SolrInputDocument) anyObject())).thenAnswer(new Answer<UpdateResponse>() {
            @Override
            public UpdateResponse answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                document[0] = (SolrInputDocument) args[0];
                return null;
            }
        });

        SolrClient.getInstance().indexDocument(fileData, jsonIndexer);
    }

    public void testQuery() throws Exception {
        Map<String, String> queryfields = new HashMap<>();
        queryfields.put("mediaType", "application/json");
        queryfields.put("author", "%admin%");
        queryfields.put("updater", "%admin%");

        UserStoreManager userStoreManager = Mockito.mock(UserStoreManager.class);
        when(userStoreManager.getRoleListOfUser("admin")).thenReturn(new String[] {"admin",
                "internal/everyone"});
        RegistryRealm registryRealm = mock(RegistryRealm.class);

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin");
            when(registryRealm.getUserStoreManager()).thenReturn(userStoreManager);

            RegistryService registryService = Mockito.mock(RegistryService.class);
            UserRegistry registry = Mockito.mock(UserRegistry.class);
            when(registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME, -1234)).thenReturn
                    (registry);
            when(registry.getUserRealm()).thenReturn(registryRealm);
            Whitebox.setInternalState(Utils.class, "registryService", registryService);

            final SolrQuery[] query = new SolrQuery[1];
            when(server.query((SolrQuery) anyObject())).thenAnswer(new Answer<QueryResponse>() {
                @Override
                public QueryResponse answer(InvocationOnMock invocation) throws Throwable {
                    Object[] args = invocation.getArguments();
                    query[0] = (SolrQuery) args[0];
                    return new QueryResponse();
                }
            });
            SolrDocumentList list = client.query(-1234, queryfields);
            assertNull(list);
            String [] results = new String[] {"tenantId:\\-1234", "allowedRoles:(admin OR internal\\/everyone)",
                    "mediaType_s:application/json", "author_s:*admin*", "updater_s:*admin*"};
            assertArrayEquals(results, query[0].getFilterQueries());
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public void testQueryWithkeyword() throws Exception {
        UserStoreManager userStoreManager = Mockito.mock(UserStoreManager.class);
        when(userStoreManager.getRoleListOfUser("admin")).thenReturn(new String[] {"admin",
                "internal/everyone"});
        RegistryRealm registryRealm = mock(RegistryRealm.class);

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin");
            when(registryRealm.getUserStoreManager()).thenReturn(userStoreManager);

            RegistryService registryService = Mockito.mock(RegistryService.class);
            UserRegistry registry = Mockito.mock(UserRegistry.class);
            when(registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME, -1234)).thenReturn
                    (registry);
            when(registry.getUserRealm()).thenReturn(registryRealm);
            Whitebox.setInternalState(Utils.class, "registryService", registryService);

            final SolrQuery[] query = new SolrQuery[1];
            when(server.query((SolrQuery) anyObject())).thenAnswer(new Answer<QueryResponse>() {
                @Override
                public QueryResponse answer(InvocationOnMock invocation) throws Throwable {
                    Object[] args = invocation.getArguments();
                    query[0] = (SolrQuery) args[0];
                    return new QueryResponse();
                }
            });
            SolrDocumentList list = client.query("testing...", -1234);
            assertNull(list);

            assertEquals("testing...", query[0].getQuery());
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public void testQueryWithMultiplekeywords() throws Exception {
        UserStoreManager userStoreManager = Mockito.mock(UserStoreManager.class);
        when(userStoreManager.getRoleListOfUser("admin")).thenReturn(new String[] {"admin",
                "internal/everyone"});
        RegistryRealm registryRealm = mock(RegistryRealm.class);

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin");
            when(registryRealm.getUserStoreManager()).thenReturn(userStoreManager);

            RegistryService registryService = Mockito.mock(RegistryService.class);
            UserRegistry registry = Mockito.mock(UserRegistry.class);
            when(registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME, -1234)).thenReturn
                    (registry);
            when(registry.getUserRealm()).thenReturn(registryRealm);
            Whitebox.setInternalState(Utils.class, "registryService", registryService);

            final SolrQuery[] query = new SolrQuery[1];
            when(server.query((SolrQuery) anyObject())).thenAnswer(new Answer<QueryResponse>() {
                @Override
                public QueryResponse answer(InvocationOnMock invocation) throws Throwable {
                    Object[] args = invocation.getArguments();
                    query[0] = (SolrQuery) args[0];
                    return new QueryResponse();
                }
            });
            SolrDocumentList list = client.query("tags:aaa&author:admin&content:unit", -1234);
            assertNull(list);

            assertEquals("tags_ss:aaa&author_s:admin&unit", query[0].getQuery());
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }


    public void testFacetQuery() throws Exception {
        UserStoreManager userStoreManager = Mockito.mock(UserStoreManager.class);
        when(userStoreManager.getRoleListOfUser("admin")).thenReturn(new String[] {"admin",
                "internal/everyone"});
        RegistryRealm registryRealm = mock(RegistryRealm.class);

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin");
            when(registryRealm.getUserStoreManager()).thenReturn(userStoreManager);

            RegistryService registryService = Mockito.mock(RegistryService.class);
            UserRegistry registry = Mockito.mock(UserRegistry.class);
            when(registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME, -1234)).thenReturn
                    (registry);
            when(registry.getUserRealm()).thenReturn(registryRealm);
            Whitebox.setInternalState(Utils.class, "registryService", registryService);

            final SolrQuery[] query = new SolrQuery[1];
            when(server.query((SolrQuery) anyObject())).thenAnswer(new Answer<QueryResponse>() {
                @Override
                public QueryResponse answer(InvocationOnMock invocation) throws Throwable {
                    Object[] args = invocation.getArguments();
                    query[0] = (SolrQuery) args[0];
                    return new QueryResponse();
                }
            });

            List<FacetField.Count> facetlist = client.facetQuery("testing...", "tags", -1234);
            assertTrue(facetlist.isEmpty());
            String[] queryFacets = new String[] {"tags_ss"};
            assertEquals("testing...", query[0].getQuery());
            assertArrayEquals(queryFacets, query[0].getFacetFields());

        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public void testDeleteFromIndex() throws Exception {
        final String[] id = new String[1];
        when(server.deleteById(anyString())).thenAnswer(new Answer<UpdateResponse>() {
            @Override
            public UpdateResponse answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                id[0] = (String) args[0];
                return null;
            }
        });

        client.deleteFromIndex("/_system/governance/trunk/test" , -1234);
        assertEquals("/_system/governance/trunk/testtenantId-1234", id[0]);
    }

}
