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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.common.SolrException;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.indexing.AsyncIndexer;
import org.wso2.carbon.registry.indexing.solr.IndexDocument;
import org.wso2.carbon.registry.indexing.util.IndexingTestUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class PDFIndexerTest extends TestCase {

    private static final Log log = LogFactory.getLog(PDFIndexerTest.class);
    private String pdfFileInText = null;
    private byte[] fileContent = null;
    private String mediaType = "application/pdf";
    private int tenantID = -1234;
    private String tenantDomain = "carbon.super";

    public void setUp() throws Exception {
        Path resourcePath = IndexingTestUtils.getResourcePath("unit-test-sample.pdf");

        assert resourcePath != null;
        fileContent = Files.readAllBytes(resourcePath);
        if (resourcePath.toFile().exists()) {
            File pdfFile = resourcePath.toFile();
            PDDocument document = null;
            try {
                document = PDDocument.load(pdfFile);
                if (!document.isEncrypted()) {
                    PDFTextStripper tStripper = new PDFTextStripper();
                    pdfFileInText = tStripper.getText(document);
                    log.info("PDF file content: " +pdfFileInText);
                }
            } finally {
                if (document != null) {
                    document.close();
                }
            }
        }
    }

    public void testGetIndexedDocument() throws Exception {
        String path = "/_system/local/temp";
        AsyncIndexer.File2Index fileData = new AsyncIndexer.File2Index(fileContent, mediaType,
                path, tenantID, tenantDomain);
        PDFIndexer pdfIndexer = new PDFIndexer();
        IndexDocument document = pdfIndexer.getIndexedDocument(fileData);
        assertEquals(path, document.getPath());
    }


    public void testGetIndexedDocumentWithInvalidContent() throws Exception {
        String path = "/_system/local/temp";
        byte[] byteContent = RegistryUtils.encodeString(pdfFileInText);
        AsyncIndexer.File2Index fileData = new AsyncIndexer.File2Index(byteContent, mediaType,
                path, tenantID, tenantDomain);
        PDFIndexer pdfIndexer = new PDFIndexer();
        try {
            pdfIndexer.getIndexedDocument(fileData);
            fail("Missing exception");
        } catch (SolrException e) {
            assertEquals("Failed to write to the index", e.getMessage());
        }
    }

}
