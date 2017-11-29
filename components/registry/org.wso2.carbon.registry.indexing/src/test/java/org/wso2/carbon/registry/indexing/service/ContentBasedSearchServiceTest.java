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
package org.wso2.carbon.registry.indexing.service;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.Whitebox;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.common.ResourceData;
import org.wso2.carbon.registry.common.TermData;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryRealm;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.indexing.indexer.IndexerException;
import org.wso2.carbon.registry.indexing.solr.SolrClient;
import org.wso2.carbon.registry.indexing.util.IndexingTestUtils;
import org.wso2.carbon.user.core.AuthorizationManager;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@PrepareForTest({SolrClient.class})
public class ContentBasedSearchServiceTest {

    private ContentBasedSearchService service;
    private SolrClient client;
    private UserRegistry registry;

    @Before
    public void setUp() throws Exception {
        service = mock(ContentBasedSearchService.class);
        Path registryPath = IndexingTestUtils.getResourcePath("registry-indexing.xml");
        System.setProperty("wso2.registry.xml", registryPath.toString());
        System.setProperty("carbon.home", registryPath.toString());
        Path resourcePath = IndexingTestUtils.getResourcePath("conf");
        System.setProperty("carbon.config.dir.path", resourcePath.toString());
        client = mock(SolrClient.class);
        PowerMockito.mockStatic(SolrClient.class);
        Whitebox.setInternalState(SolrClient.class, "instance", client);
        registry = mock(UserRegistry.class);
        when(registry.resourceExists(anyString())).thenReturn(Boolean.TRUE);
        when(service.getRootRegistry()).thenReturn(registry);
    }

    @After
    public void cleanUp() {
        System.clearProperty("wso2.registry.xml");
        System.clearProperty("carbon.home");
        System.clearProperty("carbon.config.dir.path");
    }

    @Test
    public void getContentSearchResults() throws Exception {
        SolrDocumentList resultList = new SolrDocumentList();
        SolrDocument document = new SolrDocument();
        document.setField("id", "/_system/governance/trunk/api/testtenantId=-1234");
        resultList.add(document);
        when(client.query(anyString(),anyInt())).thenReturn(resultList);
        when(service.getContentSearchResults(anyString())).thenCallRealMethod();
        when(service.searchContent(anyString(), (UserRegistry) anyObject())).thenCallRealMethod();
        AuthorizationManager authManager = mock(AuthorizationManager.class);
        when(authManager.isUserAuthorized("admin", "/_system/governance/trunk/api/test", ActionConstants
                .GET)).thenReturn(Boolean.TRUE);
        RegistryRealm registryRealm = mock(RegistryRealm.class);

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin");
            when(registryRealm.getAuthorizationManager()).thenReturn(authManager);
            when(registry.getUserRealm()).thenReturn(registryRealm);
            ResourceImpl resource = new ResourceImpl();
            resource.setPath("/_system/governance/trunk/api/test");
            resource.setAuthorUserName("admin");
            resource.setDescription("Unit Testing");
            resource.setCreatedTime(Calendar.getInstance().getTime());
            resource.setProperty("registry.user", "admin");
            when(registry.get("/_system/governance/trunk/api/test")).thenReturn(resource);
            when(registry.getAverageRating("/_system/governance/trunk/api/test")).thenReturn(2.5f);
            when(registry.getUserName()).thenReturn("admin");
            SearchResultsBean resultsBean = service.getContentSearchResults("testing");
            ResourceData[] resultDataList = resultsBean.getResourceDataList();
            assertNotNull(resultDataList);
            assertEquals(1, resultDataList.length);
            assertEquals("test", resultDataList[0].getName());
            assertEquals(2.5f, resultDataList[0].getAverageRating(), 0);

        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Test
    public void getContentSearchResultsWithException() throws Exception {
        SolrDocumentList resultList = new SolrDocumentList();
        SolrDocument document = new SolrDocument();
        document.setField("id", "/_system/governance/trunk/api/testtenantId=-1234");
        resultList.add(document);
        when(client.query(anyString(),anyInt())).thenReturn(resultList);
        when(service.getContentSearchResults(anyString())).thenCallRealMethod();
        when(service.searchContent(anyString(), (UserRegistry) anyObject())).thenThrow(new IndexerException
                ("Test Exception"));

        SearchResultsBean resultsBean = service.getContentSearchResults("testing");
        ResourceData[] resultDataList = resultsBean.getResourceDataList();
        assertNull(resultDataList);
    }

    @Test
    public void getAttributeSearchResults() throws Exception {
        SolrDocumentList resultList = new SolrDocumentList();
        SolrDocument document = new SolrDocument();
        document.setField("id", "/_system/governance/trunk/api/testtenantId=-1234");
        resultList.add(document);
        when(client.query(anyInt(), anyMap())).thenReturn(resultList);
        when(service.getAttributeSearchResults((String[][]) anyObject())).thenCallRealMethod();
        when(service.searchByAttribute(anyMap(), (UserRegistry) anyObject())).thenCallRealMethod();
        AuthorizationManager authManager = mock(AuthorizationManager.class);
        when(authManager.isUserAuthorized("admin", "/_system/governance/trunk/api/test", ActionConstants
                .GET)).thenReturn(Boolean.TRUE);
        RegistryRealm registryRealm = mock(RegistryRealm.class);

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin");
            when(registryRealm.getAuthorizationManager()).thenReturn(authManager);
            when(registry.getUserRealm()).thenReturn(registryRealm);
            ResourceImpl resource = new ResourceImpl();
            resource.setPath("/_system/governance/trunk/api/test");
            resource.setAuthorUserName("admin");
            resource.setDescription("Unit Testing");
            resource.setCreatedTime(Calendar.getInstance().getTime());
            resource.setProperty("registry.user", "admin");
            when(registry.get("/_system/governance/trunk/api/test")).thenReturn(resource);
            when(registry.getAverageRating("/_system/governance/trunk/api/test")).thenReturn(2.5f);
            when(registry.getUserName()).thenReturn("admin");
            String[][] searchAttributes = new String[2][2];
            searchAttributes[0][0] = "mediaType";
            searchAttributes[0][1] = "application/json";
            searchAttributes[1][0] = "overview_name";
            searchAttributes[1][1] = "test";
            SearchResultsBean resultsBean = service.getAttributeSearchResults(searchAttributes);
            ResourceData[] resultDataList = resultsBean.getResourceDataList();
            Assert.assertNotNull(resultDataList);
            assertEquals(1, resultDataList.length);
            assertEquals("test", resultDataList[0].getName());
            assertEquals(2.5f, resultDataList[0].getAverageRating(), 0);

        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }


    @Test
    public void getAttributeSearchResultsWithException() throws Exception {
        SolrDocumentList resultList = new SolrDocumentList();
        SolrDocument document = new SolrDocument();
        document.setField("id", "/_system/governance/trunk/api/testtenantId=-1234");
        resultList.add(document);
        when(client.query(anyInt(), anyMap())).thenReturn(resultList);
        when(service.getAttributeSearchResults((String[][]) anyObject())).thenCallRealMethod();
        when(service.searchByAttribute(anyMap(), (UserRegistry) anyObject())).thenThrow(new IndexerException
                ("Test Exception", new Exception()));

        String[][] searchAttributes = new String[2][2];
        searchAttributes[0][0] = "mediaType";
        searchAttributes[0][1] = "application/json";
        searchAttributes[1][0] = "overview_name";
        searchAttributes[1][1] = "test";
        SearchResultsBean resultsBean = service.getAttributeSearchResults(searchAttributes);
        ResourceData[] resultDataList = resultsBean.getResourceDataList();
        assertNull(resultDataList);
    }

    @Test
    public void getTermSearchResults() throws Exception {
        List<FacetField.Count> resultList = new ArrayList<>();
        FacetField.Count count = new FacetField.Count(new FacetField("tags_ss"), "tag1", 2);
        resultList.add(count);
        when(client.facetQuery(anyInt(), (Map)anyObject())).thenReturn(resultList);
        when(service.getTermSearchResults((String[][]) anyObject())).thenCallRealMethod();
        when(service.searchTerms((Map)anyObject(), (UserRegistry)anyObject()))
                .thenCallRealMethod();
        AuthorizationManager authManager = mock(AuthorizationManager.class);
        when(authManager.isUserAuthorized("admin", "/_system/governance/trunk/api/test", ActionConstants
                .GET)).thenReturn(Boolean.TRUE);
        RegistryRealm registryRealm = PowerMockito.mock(RegistryRealm.class);

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin");
            when(registryRealm.getAuthorizationManager()).thenReturn(authManager);
            when(registry.getUserRealm()).thenReturn(registryRealm);
            String[][] searchAttributes = new String[2][2];
            searchAttributes[0][0] = "facet.field";
            searchAttributes[0][1] = "tags";
            searchAttributes[1][0] = "mediaType";
            searchAttributes[1][1] = "application/json";
            SearchResultsBean resultsBean = service.getTermSearchResults(searchAttributes);
            TermData[] resultDataList = resultsBean.getTermDataList();
            Assert.assertNotNull(resultDataList);
            assertEquals(1, resultDataList.length);
            assertEquals("tag1", resultDataList[0].getTerm());
            assertEquals(2, resultDataList[0].getFrequency(), 0);

        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Test
    public void getTermSearchResultsWithException() throws Exception {
        List<FacetField.Count> resultList = new ArrayList<>();
        FacetField.Count count = new FacetField.Count(new FacetField("tags_ss"), "tag1", 2);
        resultList.add(count);
        when(client.facetQuery(anyInt(), (Map)anyObject())).thenReturn(resultList);
        when(service.getTermSearchResults((String[][]) anyObject())).thenCallRealMethod();
        when(service.searchTerms((Map)anyObject(), (UserRegistry)anyObject())).thenThrow(new IndexerException
                ("Test Exception", new Exception()));

            String[][] searchAttributes = new String[2][2];
            searchAttributes[0][0] = "facet.field";
            searchAttributes[0][1] = "tags";
            searchAttributes[1][0] = "mediaType";
            searchAttributes[1][1] = "application/json";
            SearchResultsBean resultsBean = service.getTermSearchResults(searchAttributes);
            TermData[] resultDataList = resultsBean.getTermDataList();
            assertNull(resultDataList);

    }

    @Test
    public void getLoggedInUserName() throws Exception {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin");
            ContentBasedSearchService service = new ContentBasedSearchService();
            assertEquals("admin", service.getLoggedInUserName());
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

    }

    @Test
    public void searchTermsByAttributes() throws Exception {
        List<FacetField.Count> resultList = new ArrayList<>();
        FacetField.Count count = new FacetField.Count(new FacetField("overview_name_s"), "tag1", 2);
        resultList.add(count);
        when(client.facetQuery(anyInt(), (Map)anyObject())).thenReturn(resultList);
        when(service.searchTerms((Map)anyObject(), (UserRegistry)anyObject()))
                .thenCallRealMethod();
        AuthorizationManager authManager = mock(AuthorizationManager.class);
        when(authManager.isUserAuthorized("admin", "/_system/governance/trunk/api/test", ActionConstants
                .GET)).thenReturn(Boolean.TRUE);
        RegistryRealm registryRealm = PowerMockito.mock(RegistryRealm.class);

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin");
            when(registryRealm.getAuthorizationManager()).thenReturn(authManager);
            when(registry.getUserRealm()).thenReturn(registryRealm);
            Map<String, String> attributeMap = new HashMap<>();
            attributeMap.put("facet.field", "overview_name");
            attributeMap.put("facet.limit", "10");
            attributeMap.put("facet.mincount", "1");
            attributeMap.put("facet.sort", "overview_version");
            attributeMap.put("facet.prefix", "ta");
            SearchResultsBean resultsBean = service.searchTerms(attributeMap, registry);
            TermData[] resultDataList = resultsBean.getTermDataList();
            Assert.assertNotNull(resultDataList);
            assertEquals(1, resultDataList.length);
            assertEquals("tag1", resultDataList[0].getTerm());
            assertEquals(2, resultDataList[0].getFrequency(), 0);

        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Test
    public void searchTermsByQuery() throws Exception {
        List<FacetField.Count> resultList = new ArrayList<>();
        FacetField.Count count = new FacetField.Count(new FacetField("tags_ss"), "tag1", 2);
        resultList.add(count);
        when(client.facetQuery(anyString(), anyString(), anyInt())).thenReturn(resultList);
        when(service.searchTermsByQuery(anyString(), anyString(), (UserRegistry)anyObject()))
                .thenCallRealMethod();
        AuthorizationManager authManager = mock(AuthorizationManager.class);
        when(authManager.isUserAuthorized("admin", "/_system/governance/trunk/api/test", ActionConstants
                .GET)).thenReturn(Boolean.TRUE);
        RegistryRealm registryRealm = PowerMockito.mock(RegistryRealm.class);

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin");
            when(registryRealm.getAuthorizationManager()).thenReturn(authManager);
            when(registry.getUserRealm()).thenReturn(registryRealm);
            SearchResultsBean resultsBean = service.searchTermsByQuery("testing", "tags", registry);
            TermData[] resultDataList = resultsBean.getTermDataList();
            Assert.assertNotNull(resultDataList);
            assertEquals(1, resultDataList.length);
            assertEquals("tag1", resultDataList[0].getTerm());
            assertEquals(2, resultDataList[0].getFrequency(), 0);

        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

}