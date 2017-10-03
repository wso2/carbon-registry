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
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
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
import org.wso2.carbon.registry.indexing.indexer.Indexer;
import org.wso2.carbon.registry.indexing.indexer.JSONIndexer;
import org.wso2.carbon.user.core.UserStoreManager;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.Matchers.*;

@PrepareForTest({SolrClient.class, PrivilegedCarbonContext.class})
public class SolrClientTest extends TestCase {
    SolrClient client;
    EmbeddedSolrServer server;


    @Before
    public void setUp() throws Exception {
        System.setProperty("carbon.home", "temp");
        client = PowerMockito.mock(SolrClient.class);
        PowerMockito.mockStatic(SolrClient.class);
        Whitebox.setInternalState(SolrClient.class, "instance", client);

        server = PowerMockito.mock(EmbeddedSolrServer.class);
        Field field = SolrClient.class.getDeclaredField("server");
        field.setAccessible(true);
        field.set(client, server);
    }

    @Test
    public void testDeleteIndexByQuery() throws Exception {

    }

    public void testGenerateIdInAddDocument() throws Exception {
        IndexDocument indexDocument = new IndexDocument();
        indexDocument.setPath("/_system/governance/trunk/test");
        indexDocument.setContentAsText("Test Indexing");
        indexDocument.setRawContent("Testing...");
        indexDocument.setTenantId(-12345);

        final SolrInputDocument[] document = {null};
        Mockito.when(server.add((SolrInputDocument) anyObject())).thenAnswer(new Answer<UpdateResponse>() {
            @Override
            public UpdateResponse answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                document[0] = (SolrInputDocument) args[0];
                return null;
            }
        });

        Mockito.doCallRealMethod().when(client).addDocument((IndexDocument) anyObject());
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
        Mockito.when(server.add((SolrInputDocument) anyObject())).thenAnswer(new Answer<UpdateResponse>() {
            @Override
            public UpdateResponse answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                document[0] = (SolrInputDocument) args[0];
                return null;
            }
        });

        Mockito.doCallRealMethod().when(client).addDocument((IndexDocument) anyObject());
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
        Mockito.when(server.add((SolrInputDocument) anyObject())).thenAnswer(new Answer<UpdateResponse>() {
            @Override
            public UpdateResponse answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                document[0] = (SolrInputDocument) args[0];
                return null;
            }
        });

        Mockito.doCallRealMethod().when(client).addDocument((IndexDocument) anyObject());
        SolrClient.getInstance().addDocument(indexDocument);
        assertNotNull(document[0].getFieldValues("key1_ss"));
        assertEquals(3, document[0].getFieldValues("key1_ss").size());
        Assert.assertArrayEquals(new String[]{"a1","a2","a3"}, document[0].getFieldValues("key1_ss").toArray());
        Assert.assertNotNull(document[0].getFieldValues("key2_is"));
        Assert.assertEquals(4, document[0].getFieldValues("key2_is").size());
        Assert.assertArrayEquals(new Integer[]{1,2,3,4}, document[0].getFieldValues("key2_is").toArray());
        Assert.assertNotNull(document[0].getFieldValues("key3_ds"));
        Assert.assertEquals(5, document[0].getFieldValues("key3_ds").size());
        Assert.assertArrayEquals(new Double[]{1.2,2.4,3.5,5.6,6.7}, document[0].getFieldValues("key3_ds")
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
        Mockito.when(server.add((SolrInputDocument) anyObject())).thenAnswer(new Answer<UpdateResponse>() {
            @Override
            public UpdateResponse answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                document[0] = (SolrInputDocument) args[0];
                return null;
            }
        });

        Mockito.doCallRealMethod().when(client).addDocument((IndexDocument) anyObject());
        SolrClient.getInstance().addDocument(indexDocument);
        Assert.assertNotNull(document[0]);
        Assert.assertEquals(3, document[0].getFieldValues("tags_ss").size());
        Assert.assertArrayEquals(tagsList.toArray(), document[0].getFieldValues("tags_ss").toArray());
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
        Mockito.when(server.add((SolrInputDocument) anyObject())).thenAnswer(new Answer<UpdateResponse>() {
            @Override
            public UpdateResponse answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                document[0] = (SolrInputDocument) args[0];
                return null;
            }
        });

        Mockito.doCallRealMethod().when(client).addDocument((IndexDocument) anyObject());
        SolrClient.getInstance().addDocument(indexDocument);
        Assert.assertNotNull(document[0]);
        Assert.assertEquals(3, document[0].getFieldValues("allowedRoles").size());
        Assert.assertArrayEquals(rolesList.toArray(), document[0].getFieldValues("allowedRoles").toArray());
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
        Mockito.when(server.add((SolrInputDocument) anyObject())).thenAnswer(new Answer<UpdateResponse>() {
            @Override
            public UpdateResponse answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                document[0] = (SolrInputDocument) args[0];
                return null;
            }
        });

        Mockito.doCallRealMethod().when(client).addDocument((IndexDocument) anyObject());
        SolrClient.getInstance().addDocument(indexDocument);
        Assert.assertNotNull(document[0]);
        SimpleDateFormat indexSdf = new SimpleDateFormat(SolrConstants.SOLR_DATE_FORMAT, Locale.ENGLISH);
        Assert.assertEquals(indexSdf.format(createdDate), document[0].getFieldValues(IndexingConstants.FIELD_CREATED_DATE +
                SolrConstants
                .SOLR_DATE_FIELD_KEY_SUFFIX).toArray()[0]);
        Assert.assertEquals(indexSdf.format(createdDate), document[0].getFieldValues(IndexingConstants.FIELD_LAST_UPDATED_DATE +
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
        Mockito.when(server.add((SolrInputDocument) anyObject())).thenAnswer(new Answer<UpdateResponse>() {
            @Override
            public UpdateResponse answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                document[0] = (SolrInputDocument) args[0];
                return null;
            }
        });

        Mockito.doCallRealMethod().when(client).addDocument((IndexDocument) anyObject());
        SolrClient.getInstance().addDocument(indexDocument);
        Assert.assertNotNull(document[0]);
        Assert.assertEquals("test", document[0].getField(IndexingConstants
                .FIELD_RESOURCE_NAME + SolrConstants.SOLR_STRING_FIELD_KEY_SUFFIX).getValue());
    }

    public void testExceptionInAddDocument() throws Exception {
        IndexDocument indexDocument = new IndexDocument();
        indexDocument.setPath("/_system/governance/trunk/test");
        indexDocument.setContentAsText("Test Indexing");
        indexDocument.setRawContent("Testing...");
        indexDocument.setTenantId(-12345);

        try {
            Mockito.when(server.add((SolrInputDocument) anyObject())).thenThrow(new SolrServerException( "Testing..."));
            Mockito.doCallRealMethod().when(client).addDocument((IndexDocument) anyObject());
            SolrClient.getInstance().addDocument(indexDocument);
            Assert.fail("Exception is missing when server fail to execute");
        } catch (SolrException e) {
            Assert.assertEquals("Error at indexing.", e.getMessage());
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
        Mockito.when(server.add((SolrInputDocument) anyObject())).thenAnswer(new Answer<UpdateResponse>() {
            @Override
            public UpdateResponse answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                document[0] = (SolrInputDocument) args[0];
                return null;
            }
        });
        Mockito.doCallRealMethod().when(client).indexDocument((AsyncIndexer.File2Index) anyObject(), (Indexer) anyObject());
        SolrClient.getInstance().indexDocument(fileData, jsonIndexer);
    }

    public void testQuery() throws Exception {
        Map<String, String> queryfields = new HashMap<>();
        queryfields.put("mediaType", "application/json");
        queryfields.put("author", "%admin%");
        queryfields.put("updater", "%admin%");

        UserStoreManager userStoreManager = Mockito.mock(UserStoreManager.class);
        Mockito.when(userStoreManager.getRoleListOfUser("admin")).thenReturn(new String[] {"admin",
                "internal/everyone"});
        RegistryRealm registryRealm = PowerMockito.mock(RegistryRealm.class);

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin");
            Mockito.when(registryRealm.getUserStoreManager()).thenReturn(userStoreManager);

            RegistryService registryService = Mockito.mock(RegistryService.class);
            UserRegistry registry = Mockito.mock(UserRegistry.class);
            Mockito.when(registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME, -1234)).thenReturn
                    (registry);
            Mockito.when(registry.getUserRealm()).thenReturn(registryRealm);
            Whitebox.setInternalState(Utils.class, "registryService", registryService);

            final SolrQuery[] query = new SolrQuery[1];
            Mockito.when(server.query((SolrQuery) anyObject())).thenAnswer(new Answer<QueryResponse>() {
                @Override
                public QueryResponse answer(InvocationOnMock invocation) throws Throwable {
                    Object[] args = invocation.getArguments();
                    query[0] = (SolrQuery) args[0];
                    return new QueryResponse();
                }
            });
            Mockito.doCallRealMethod().when(client).query(anyInt(), (Map<String, String>) anyMap());
            Mockito.doCallRealMethod().when(client).query(anyString(), anyInt(), (Map<String, String>) anyMap());
            SolrDocumentList list = client.query(-1234, queryfields);
            Assert.assertNull(list);
            String [] results = new String[] {"tenantId:\\-1234", "allowedRoles:(admin OR internal\\/everyone)",
                    "mediaType_s:application/json", "author_s:*admin*", "updater_s:*admin*"};
            Assert.assertArrayEquals(results, query[0].getFilterQueries());
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public void testQueryWithkeyword() throws Exception {
        UserStoreManager userStoreManager = Mockito.mock(UserStoreManager.class);
        Mockito.when(userStoreManager.getRoleListOfUser("admin")).thenReturn(new String[] {"admin",
                "internal/everyone"});
        RegistryRealm registryRealm = PowerMockito.mock(RegistryRealm.class);

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin");
            Mockito.when(registryRealm.getUserStoreManager()).thenReturn(userStoreManager);

            RegistryService registryService = Mockito.mock(RegistryService.class);
            UserRegistry registry = Mockito.mock(UserRegistry.class);
            Mockito.when(registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME, -1234)).thenReturn
                    (registry);
            Mockito.when(registry.getUserRealm()).thenReturn(registryRealm);
            Whitebox.setInternalState(Utils.class, "registryService", registryService);

            final SolrQuery[] query = new SolrQuery[1];
            Mockito.when(server.query((SolrQuery) anyObject())).thenAnswer(new Answer<QueryResponse>() {
                @Override
                public QueryResponse answer(InvocationOnMock invocation) throws Throwable {
                    Object[] args = invocation.getArguments();
                    query[0] = (SolrQuery) args[0];
                    return new QueryResponse();
                }
            });
            Mockito.doCallRealMethod().when(client).query(anyString(), anyInt());
            Mockito.doCallRealMethod().when(client).query(anyString(), anyInt(), (Map<String, String>) anyMap());
            SolrDocumentList list = client.query("testing...", -1234);
            Assert.assertNull(list);

            Assert.assertEquals("testing...", query[0].getQuery());
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public void testQueryWithMultiplekeywords() throws Exception {
        UserStoreManager userStoreManager = Mockito.mock(UserStoreManager.class);
        Mockito.when(userStoreManager.getRoleListOfUser("admin")).thenReturn(new String[] {"admin",
                "internal/everyone"});
        RegistryRealm registryRealm = PowerMockito.mock(RegistryRealm.class);

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin");
            Mockito.when(registryRealm.getUserStoreManager()).thenReturn(userStoreManager);

            RegistryService registryService = Mockito.mock(RegistryService.class);
            UserRegistry registry = Mockito.mock(UserRegistry.class);
            Mockito.when(registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME, -1234)).thenReturn
                    (registry);
            Mockito.when(registry.getUserRealm()).thenReturn(registryRealm);
            Whitebox.setInternalState(Utils.class, "registryService", registryService);

            final SolrQuery[] query = new SolrQuery[1];
            Mockito.when(server.query((SolrQuery) anyObject())).thenAnswer(new Answer<QueryResponse>() {
                @Override
                public QueryResponse answer(InvocationOnMock invocation) throws Throwable {
                    Object[] args = invocation.getArguments();
                    query[0] = (SolrQuery) args[0];
                    return new QueryResponse();
                }
            });
            Mockito.doCallRealMethod().when(client).query(anyString(), anyInt());
            Mockito.doCallRealMethod().when(client).query(anyString(), anyInt(), (Map<String, String>) anyMap());
            SolrDocumentList list = client.query("tags:aaa&author:admin&content:unit", -1234);
            Assert.assertNull(list);

            Assert.assertEquals("tags_ss:aaa&author_s:admin&unit", query[0].getQuery());
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }


    public void testFacetQuery() throws Exception {
        UserStoreManager userStoreManager = Mockito.mock(UserStoreManager.class);
        Mockito.when(userStoreManager.getRoleListOfUser("admin")).thenReturn(new String[] {"admin",
                "internal/everyone"});
        RegistryRealm registryRealm = PowerMockito.mock(RegistryRealm.class);

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin");
            Mockito.when(registryRealm.getUserStoreManager()).thenReturn(userStoreManager);

            RegistryService registryService = Mockito.mock(RegistryService.class);
            UserRegistry registry = Mockito.mock(UserRegistry.class);
            Mockito.when(registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME, -1234)).thenReturn
                    (registry);
            Mockito.when(registry.getUserRealm()).thenReturn(registryRealm);
            Whitebox.setInternalState(Utils.class, "registryService", registryService);

            final SolrQuery[] query = new SolrQuery[1];
            Mockito.when(server.query((SolrQuery) anyObject())).thenAnswer(new Answer<QueryResponse>() {
                @Override
                public QueryResponse answer(InvocationOnMock invocation) throws Throwable {
                    Object[] args = invocation.getArguments();
                    query[0] = (SolrQuery) args[0];
                    return new QueryResponse();
                }
            });
            Mockito.doCallRealMethod().when(client).facetQuery(anyString(), anyString(), anyInt());
            Mockito.doCallRealMethod().when(client).facetQuery(anyString(), anyInt(), (Map<String, String>) anyMap());
            List<FacetField.Count> facetlist = client.facetQuery("testing...", "tags", -1234);
            Assert.assertTrue(facetlist.isEmpty());
            String[] queryFacets = new String[] {"tags_ss"};
            Assert.assertEquals("testing...", query[0].getQuery());
            Assert.assertArrayEquals(queryFacets, query[0].getFacetFields());

        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public void testDeleteFromIndex() throws Exception {
        final String[] id = new String[1];
        Mockito.when(server.deleteById(anyString())).thenAnswer(new Answer<UpdateResponse>() {
            @Override
            public UpdateResponse answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                id[0] = (String) args[0];
                return null;
            }
        });
        Mockito.doCallRealMethod().when(client).deleteFromIndex(anyString(), anyInt());
        client.deleteFromIndex("/_system/governance/trunk/test" , -1234);
        Assert.assertEquals("/_system/governance/trunk/testtenantId-1234", id[0]);
    }

}
