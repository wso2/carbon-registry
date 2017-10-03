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
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.indexing.AsyncIndexer;
import org.wso2.carbon.registry.indexing.solr.IndexDocument;

public class XMLIndexerTest extends TestCase {
    private String xmlContent = "<note>\n" +
            "  <to>Tove</to>\n" +
            "  <from>Jani</from>\n" +
            "  <heading>Reminder</heading>\n" +
            "  <body>Don't forget me this weekend!</body>\n" +
            "</note>";
    private String xmlInvalidContent = "<to>Tove</to>\n" +
            "  <from>Jani</from>\n" +
            "  <heading>Reminder</heading>\n" +
            "  <body>Don't forget me this weekend!</body>\n" +
            "</note>";
    private String mediaType = "application/xml";
    private int tenantID = -1234;
    private String tenantDomain = "carbon.super";

    public void testGetIndexedDocument() throws Exception {
        String path = "/_system/local/temp";
        byte[] byteContent = RegistryUtils.encodeString(xmlContent);
        AsyncIndexer.File2Index fileData = new AsyncIndexer.File2Index(byteContent, mediaType,
                path, tenantID, tenantDomain);
        XMLIndexer xmlIndexer = new XMLIndexer();
        IndexDocument document = xmlIndexer.getIndexedDocument(fileData);
        assertEquals(path, document.getPath());
    }

    public void testGetIndexedDocumentWithLC() throws Exception {
        String path = "/_system/local/temp";
        byte[] byteContent = RegistryUtils.encodeString(xmlContent);
        AsyncIndexer.File2Index fileData = new AsyncIndexer.File2Index(byteContent, mediaType,
                path, tenantID, tenantDomain);
        fileData.lcName = "ServiceLifeCycle";
        fileData.lcState = "Development";
        XMLIndexer xmlIndexer = new XMLIndexer();
        IndexDocument document = xmlIndexer.getIndexedDocument(fileData);
        assertEquals(path, document.getPath());
    }

    public void testGetIndexedDocumentWithInvalidContent() throws Exception {
        String path = "/_system/local/temp";
        byte[] byteContent = RegistryUtils.encodeString(xmlInvalidContent);
        AsyncIndexer.File2Index fileData = new AsyncIndexer.File2Index(byteContent, mediaType,
                path, tenantID, tenantDomain);
        fileData.lcName = "ServiceLifeCycle";
        fileData.lcState = "Development";
        XMLIndexer xmlIndexer = new XMLIndexer();
        IndexDocument document = xmlIndexer.getIndexedDocument(fileData);
        assertEquals(path, document.getPath());
    }

}
