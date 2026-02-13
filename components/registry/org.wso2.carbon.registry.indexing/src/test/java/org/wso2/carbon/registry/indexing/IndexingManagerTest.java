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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.indexing.indexer.Indexer;
import org.wso2.carbon.registry.indexing.indexer.XMLIndexer;
import org.wso2.carbon.registry.indexing.solr.SolrClient;
import org.wso2.carbon.registry.indexing.util.IndexingTestUtils;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IndexingManagerTest {

    private String lastAccessPath = "/_system/local/repository/components/org.wso2.carbon" +
            ".registry/indexing/lastaccesstime";

    @Before
    public void setup() throws Exception {
        Path registryPath = IndexingTestUtils.getResourcePath("registry-indexing.xml");
        assert registryPath != null;
        System.setProperty("wso2.registry.xml", registryPath.toString());
        System.setProperty("carbon.home", registryPath.toString());
        Path resourcePath = IndexingTestUtils.getResourcePath("conf");
        assert resourcePath != null;
        System.setProperty("carbon.config.dir.path", resourcePath.toString());
        SolrClient client = mock(SolrClient.class);
        Field instanceField = SolrClient.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, client);
        doNothing().when(client).deleteFromIndex(anyString(), anyInt());
    }

    @After
    public void cleanUp() throws NoSuchFieldException, IllegalAccessException {
        System.clearProperty("wso2.registry.xml");
        System.clearProperty("carbon.home");
        System.clearProperty("carbon.config.dir.path");

        Field instance = IndexingManager.getInstance().getClass().getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(IndexingManager.getInstance(), null);
    }

    @Test
    public void testStartIndexing() throws RegistryException {
        Long lastAccessTime = Calendar.getInstance().getTimeInMillis();
        UserRegistry userRegistry = mock(UserRegistry.class);
        ResourceImpl resource = new ResourceImpl();
        resource.setPath(lastAccessPath);
        resource.setCreatedTime(Calendar.getInstance().getTime());
        resource.setProperty("12", String.valueOf(lastAccessTime));
        resource.setProperty("-1234", String.valueOf(lastAccessTime));
        resource.setProperty("2", String.valueOf(lastAccessTime));
        when(userRegistry.get(lastAccessPath)).thenReturn(resource);
        doNothing().when(userRegistry).delete(lastAccessPath);
        RegistryService registryService = mock(RegistryService.class);
        when(userRegistry.resourceExists(lastAccessPath)).thenReturn(true);
        when(registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME)).thenReturn(userRegistry);
        Utils.setRegistryService(registryService);

        IndexingManager manager = IndexingManager.getInstance();
        manager.startIndexing();
        assertEquals(new Date(lastAccessTime), manager.getLastAccessTime(12));
    }

    @Test
    public void testRestartIndexing() throws RegistryException {
        UserRegistry userRegistry = mock(UserRegistry.class);
        Long lastAccessTime = Calendar.getInstance().getTimeInMillis();
        ResourceImpl resource = new ResourceImpl();
        resource.setPath(lastAccessPath);
        resource.setCreatedTime(Calendar.getInstance().getTime());
        resource.setProperty("1", String.valueOf(lastAccessTime));
        resource.setProperty("-1234", String.valueOf(lastAccessTime));
        resource.setProperty("2", String.valueOf(lastAccessTime));
        when(userRegistry.get(lastAccessPath)).thenReturn(resource);
        doNothing().when(userRegistry).delete(lastAccessPath);
        when(userRegistry.newResource()).thenReturn(resource);
        RegistryService registryService = mock(RegistryService.class);
        when(userRegistry.resourceExists(lastAccessPath)).thenReturn(true);
        when(registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME)).thenReturn(userRegistry);
        Utils.setRegistryService(registryService);

        final Resource[] finalResources = new Resource[1];
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                finalResources[0] = (Resource)args[1];
                return null;
            }
        }).when(userRegistry).put(anyString(), any(Resource.class));

        IndexingManager manager = IndexingManager.getInstance();
        Date currentDate = Calendar.getInstance().getTime();
        manager.setLastAccessTime(100, currentDate);
        manager.restartIndexing();
        assertEquals(1, finalResources.length);
        assertEquals(currentDate.getTime(), Long.parseLong(finalResources[0].getProperty("100")));
    }

    @Test
    public void testGetIndexerForMediaType() throws RegistryException {
        UserRegistry userRegistry = mock(UserRegistry.class);
        RegistryService registryService = mock(RegistryService.class);
        when(registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME)).thenReturn(userRegistry);
        Utils.setRegistryService(registryService);
        IndexingManager manager = IndexingManager.getInstance();
        Indexer indexer = manager.getIndexerForMediaType("application/xml");
        assertTrue(indexer instanceof XMLIndexer);
        assertNull(manager.getIndexerForMediaType("application/test"));
    }

    @Test
    public void testCanIndex() throws RegistryException {
        UserRegistry userRegistry = mock(UserRegistry.class);
        RegistryService registryService = mock(RegistryService.class);
        when(registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME)).thenReturn(userRegistry);
        Utils.setRegistryService(registryService);
        IndexingManager manager = IndexingManager.getInstance();
        assertTrue(manager.canIndex("/_system/local/index"));
        assertFalse(manager.canIndex("/_system/local/repository/components/org.wso2.carbon" +
                ".registry/mount/_system_governance"));
    }

    @Test
    public void testGetRegistryConfigs() throws RegistryException {
        UserRegistry userRegistry = mock(UserRegistry.class);
        RegistryService registryService = mock(RegistryService.class);
        when(registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME)).thenReturn(userRegistry);
        Utils.setRegistryService(registryService);
        IndexingManager manager = IndexingManager.getInstance();

        assertEquals(50, manager.getBatchSize());
        assertEquals(50, manager.getIndexerPoolSize());
        assertEquals(35, manager.getStartingDelayInSecs());
        assertEquals(3, manager.getIndexingFreqInSecs());
        assertFalse(manager.isCacheSkipped());
    }

    @Test
    public void testSubmitFileForIndexing() throws RegistryException {
        UserRegistry userRegistry = mock(UserRegistry.class);
        RegistryService registryService = mock(RegistryService.class);
        when(registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME)).thenReturn(userRegistry);
        Utils.setRegistryService(registryService);
        IndexingManager manager = IndexingManager.getInstance();

        manager.submitFileForIndexing(-1234, "carbon.super", "/_system/local/index", null);
    }

    @Test
    public void testGetRegistry() throws RegistryException {
        UserRegistry userRegistry = mock(UserRegistry.class);
        RegistryService registryService = mock(RegistryService.class);
        when(registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME)).thenReturn(userRegistry);
        when(registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME, 10)).thenReturn
                (userRegistry);
        when(registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME, -1)).thenThrow(new
                RegistryException("Invalid Tenant ID"));
        Utils.setRegistryService(registryService);
        IndexingManager manager = IndexingManager.getInstance();

        assertNotNull(manager.getRegistry(-1234));
        assertNotNull(manager.getRegistry(10));
        assertNull(manager.getRegistry(-1));
    }

    @Test
    public void testDeleteFromIndex() throws RegistryException {
        UserRegistry userRegistry = mock(UserRegistry.class);
        RegistryService registryService = mock(RegistryService.class);
        when(registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME)).thenReturn(userRegistry);
        Utils.setRegistryService(registryService);
        IndexingManager manager = IndexingManager.getInstance();

        manager.deleteFromIndex("/_system/local/index", 2);
    }
}