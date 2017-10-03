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
import org.apache.solr.common.SolrException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.indexing.AsyncIndexer;
import org.wso2.carbon.registry.indexing.solr.IndexDocument;
import org.wso2.carbon.registry.indexing.util.IndexingTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;

public class MSPowerpointIndexerTest extends TestCase {
    private byte[] fileContent = null;
    private String mediaType = "application/ppt";
    private int tenantID = -1234;
    private String tenantDomain = "carbon.super";

    public void setUp() throws Exception {
        Path resourcePath = IndexingTestUtils.getResourcePath("unit-test-sample.ppt");
        if (resourcePath != null) {
            fileContent = Files.readAllBytes(resourcePath);
        }
    }


    public void testGetIndexedDocument() throws Exception {
        String path = "/_system/local/temp";
        AsyncIndexer.File2Index fileData = new AsyncIndexer.File2Index(fileContent, mediaType,
                path, tenantID, tenantDomain);
        MSPowerpointIndexer indexer = new MSPowerpointIndexer();
        IndexDocument document = indexer.getIndexedDocument(fileData);
        assertEquals(path, document.getPath());
    }

    public void testGetIndexedDocumentInvalidContent() throws Exception {
        String path = "/_system/local/temp";
        byte[] byteContent = RegistryUtils.encodeString("Testing");
        AsyncIndexer.File2Index fileData = new AsyncIndexer.File2Index(byteContent, mediaType,
                path, tenantID, tenantDomain);
        MSPowerpointIndexer indexer = new MSPowerpointIndexer();
        try {
            indexer.getIndexedDocument(fileData);
            fail("Missing exception");
        } catch (SolrException e) {
            assertEquals("Failed to write to the index", e.getMessage());
        }
    }

}
