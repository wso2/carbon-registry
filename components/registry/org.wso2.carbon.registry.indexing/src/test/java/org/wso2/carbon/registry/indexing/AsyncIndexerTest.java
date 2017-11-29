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
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.Whitebox;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryAuthorizationManager;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryRealm;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.indexing.solr.SolrClient;
import org.wso2.carbon.registry.indexing.util.IndexingTestUtils;

import java.nio.file.Path;
import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;

@PrepareForTest({AsyncIndexerTest.class})
public class AsyncIndexerTest {

    private SolrClient client = null;

    @Before
    public void setup() throws Exception {
        Path registryPath = IndexingTestUtils.getResourcePath("registry-indexing.xml");
        System.setProperty("wso2.registry.xml", registryPath.toString());
        System.setProperty("carbon.home", registryPath.toString());
        Path resourcePath = IndexingTestUtils.getResourcePath("conf");
        System.setProperty("carbon.config.dir.path", resourcePath.toString());
        client = mock(SolrClient.class);
        PowerMockito.mockStatic(SolrClient.class);
        Whitebox.setInternalState(SolrClient.class, "instance", client);

        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
        when(userRegistry.resourceExists("/_system/local/index")).thenReturn(true);
        ResourceImpl resource = new ResourceImpl();
        resource.setPath("/_system/local/index");
        resource.setContent("Test Indexing Tasks");
        resource.setMediaType("application/json");
        resource.setCreatedTime(Calendar.getInstance().getTime());
        resource.setProperty("registry.resource.symlink.path", "/_system/local/index");
        when(userRegistry.get("/_system/local/index")).thenReturn(resource);
        when(registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME)).thenReturn(userRegistry);
        Utils.setRegistryService(registryService);

        RegistryRealm registryRealm = mock(RegistryRealm.class);
        RegistryAuthorizationManager authorizationManager = mock(RegistryAuthorizationManager.class);
        when(authorizationManager.getAllowedRolesForResource("/_system/local/index", ActionConstants.GET))
                .thenReturn(new String[]{"admin"});
        when(registryRealm.getAuthorizationManager()).thenReturn(authorizationManager);
        when(userRegistry.getUserRealm()).thenReturn(registryRealm);

    }

    @After
    public void cleanUp() {
        System.clearProperty("wso2.registry.xml");
        System.clearProperty("carbon.home");
        System.clearProperty("carbon.config.dir.path");
    }

    @Test
    public void createFileIndex() throws RegistryException {
        byte[] data = RegistryUtils.encodeString("testing");
        String mediaType = "application/json";
        String path = "/_system/local/index";
        int tenant = -12345;
        String tenantDomain = "fb.com";
        AsyncIndexer.File2Index file2Index = new AsyncIndexer.File2Index(data, mediaType, path, tenant, tenantDomain,
                "ServiceLifecycle", "Development");
        assertEquals(data, file2Index.data);
        assertEquals(mediaType, file2Index.mediaType);
        assertEquals(path, file2Index.path);
        assertEquals(tenant, file2Index.tenantId);
        assertEquals(tenantDomain, file2Index.tenantDomain);
    }

    @Test
    public void createFileIndex02() {
        String path = "/_system/local/index";
        int tenant = -12345;
        String tenantDomain = "fb.com";
        String sourceURL = null;
        AsyncIndexer.File2Index file2Index = new AsyncIndexer.File2Index(path, tenant, tenantDomain, sourceURL);
        assertEquals(path, file2Index.path);
        assertEquals(tenant, file2Index.tenantId);
        assertEquals(tenantDomain, file2Index.tenantDomain);
        assertEquals(sourceURL, file2Index.sourceURL);
    }

    @Test
    public void createFileIndex03() throws RegistryException {
        byte[] data = RegistryUtils.encodeString("testing");
        String mediaType = "application/json";
        String path = "/_system/local/index";
        int tenant = -12345;
        String tenantDomain = "fb.com";
        AsyncIndexer.File2Index file2Index = new AsyncIndexer.File2Index(data, mediaType, path, tenant, tenantDomain);
        assertEquals(data, file2Index.data);
        assertEquals(mediaType, file2Index.mediaType);
        assertEquals(path, file2Index.path);
        assertEquals(tenant, file2Index.tenantId);
        assertEquals(tenantDomain, file2Index.tenantDomain);
    }

    @Test
    public void addFile() throws Exception {
        AsyncIndexer indexer = new AsyncIndexer();
        byte[] data = RegistryUtils.encodeString("testing");
        String mediaType = "application/json";
        String path = "/_system/local/index";
        int tenant = -12345;
        String tenantDomain = "fb.com";
        AsyncIndexer.File2Index file2Index = new AsyncIndexer.File2Index(data, mediaType, path, tenant, tenantDomain);
        indexer.addFile(file2Index);
    }

    @Test
    public void getClient() throws Exception {
        AsyncIndexer indexer = new AsyncIndexer();
        assertEquals(client, indexer.getClient());
    }

    @Test
    public void run() throws Exception {
        AsyncIndexer indexer = new AsyncIndexer();
        byte[] data = RegistryUtils.encodeString("testing");
        String mediaType = "application/json";
        String path = "/_system/local/index";
        int tenant = -12345;
        String tenantDomain = "fb.com";
        AsyncIndexer.File2Index file2Index = new AsyncIndexer.File2Index(data, mediaType, path, tenant, tenantDomain);
        indexer.addFile(file2Index);
        indexer.run();
    }

    @Test
    public void createIndexDocument() throws Exception {
        byte[] data = RegistryUtils.encodeString("testing");
        String mediaType = "application/json";
        String path = "/_system/local/index";
        int tenant = -1234;
        String tenantDomain = "fb.com";
        AsyncIndexer.File2Index file2Index = new AsyncIndexer.File2Index(data, mediaType, path, tenant, tenantDomain);
        AsyncIndexer.IndexingTask indexingTask = new AsyncIndexer.IndexingTask(file2Index);
        indexingTask.run();
    }

}