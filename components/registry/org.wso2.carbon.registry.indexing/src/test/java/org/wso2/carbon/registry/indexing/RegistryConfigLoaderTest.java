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

import junit.framework.TestCase;
import org.junit.After;
import org.wso2.carbon.registry.indexing.util.IndexingTestUtils;

import java.nio.file.Path;

public class RegistryConfigLoaderTest extends TestCase {

    RegistryConfigLoader configLoader = null;
    public void setUp() throws Exception {
        Path registryPath = IndexingTestUtils.getResourcePath("registry-indexing.xml");
        System.setProperty("wso2.registry.xml", registryPath.toString());
        System.setProperty("carbon.home", registryPath.toString());
        configLoader = RegistryConfigLoader.getInstance();
    }

    @After
    public void cleanUp() {
        System.clearProperty("wso2.registry.xml");
        System.clearProperty("carbon.home");
        System.clearProperty("carbon.config.dir.path");
    }

    public void testGetIndexerPoolSize() throws Exception {
        assertNotNull(configLoader);
        assertEquals(50, configLoader.getIndexerPoolSize());
    }

    public void testIsSkipIndexingCache() throws Exception {
        assertNotNull(configLoader);
        assertEquals(false, configLoader.isSkipIndexingCache());
    }

    public void testSetSkipIndexingCache() throws Exception {
        assertNotNull(configLoader);
        configLoader.setSkipIndexingCache(true);
        assertEquals(true, configLoader.isSkipIndexingCache());
        configLoader.setSkipIndexingCache(false);
    }

    public void testGetBatchSize() throws Exception {
        assertNotNull(configLoader);
        assertEquals(50, configLoader.getBatchSize());
    }

    public void testGetSolrServerUrl() throws Exception {
        assertNotNull(configLoader);
        assertNull(configLoader.getSolrServerUrl());
    }

    public void testGetIndexingFreqInSecs() throws Exception {
        assertNotNull(configLoader);
        assertEquals(3, configLoader.getIndexingFreqInSecs());
    }

    public void testGetLastAccessTimeLocation() throws Exception {
        assertNotNull(configLoader);
        assertEquals("/_system/local/repository/components/org.wso2.carbon.registry/indexing/lastaccesstime",
                configLoader.getLastAccessTimeLocation());
    }

    public void testGetIndexerMap() throws Exception {
        assertNotNull(configLoader);
        assertFalse(configLoader.getIndexerMap().isEmpty());
        assertEquals(10, configLoader.getIndexerMap().size());
    }

    public void testGetExclusionPatterns() throws Exception {
        assertNotNull(configLoader);
        assertEquals(2, configLoader.getExclusionPatterns().length);
    }

    public void testIsStartIndexing() throws Exception {
        assertNotNull(configLoader);
        assertEquals(true, configLoader.IsStartIndexing());
    }

    public void testGetStartingDelayInSecs() throws Exception {
        assertNotNull(configLoader);
        assertEquals(35, configLoader.getStartingDelayInSecs());
    }
}
