/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.extensions.test.jdbc;

import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.test.utils.BaseTestCase;

public class IndexingTest extends BaseTestCase {
    /**
     * Registry instance for use in tests. Note that there should be only one Registry instance in a
     * JVM.
     */
    protected static Registry registry = null;

    public void setUp() throws RegistryException {
        super.setUp();
        if (registry == null) {
            EmbeddedRegistryService embeddedRegistry = ctx.getEmbeddedRegistryService();
            registry = embeddedRegistry.getConfigSystemRegistry();
        }

    }

    public void testIndexing() throws RegistryException {
        Resource r6 = registry.newResource();
        String r6Content = "this is r6 content";
        r6.setContent(RegistryUtils.encodeString(r6Content));
        r6.setDescription("production ready.");
        r6.setMediaType("text/plain");
        String r6Path = "/c1/r6";

        try {
            registry.put(r6Path, r6);
        } catch (RegistryException e) {

        }

        Collection collection1 = registry.searchContent("r6");
        String[] paths1 = (String[]) collection1.getContent();

        assertEquals("Search should return the relevant path corresponding to the content which includes " +
                "submitted keywords.", "/c1/r6", paths1[0]);

        Collection collection2 = registry.searchContent("non existing");
        String[] paths2 = (String[]) collection2.getContent();

        assertEquals("Search should not return results when non existing keywords are submitted.", 0, paths2.length);

        registry.delete(r6Path);
        Collection collection3 = registry.searchContent("r6");
        String[] paths3 = (String[]) collection3.getContent();

        assertEquals("Search should not return results for a deleted document", 0, paths3.length);

    }

    public void testMSWordIndexing() throws RegistryException {
        Resource r6 = registry.newResource();
        String r6Path = "/c1/r6";
        try {
            r6.setContentStream(getClass().getResourceAsStream("/sample.doc"));
        } catch (Exception e) {

        }
        r6.setMediaType("application/msword");
        try {
            registry.put(r6Path, r6);
        } catch (RegistryException e) {

        }

        Collection collection = registry.searchContent("word");
        String[] paths = (String[]) collection.getContent();

        assertEquals("Search should return the relevant path corresponding to the content which includes " +
                "submitted keywords.", "/c1/r6", paths[0]);

    }

    public void testMSExcelIndexing() throws RegistryException {
        Resource r7 = registry.newResource();
        String r7Path = "/c1/r7";
        try {
            r7.setContentStream(getClass().getResourceAsStream("/sample.xls"));
        } catch (Exception e) {

        }
        r7.setMediaType("application/vnd.ms-excel");
        try {
            registry.put(r7Path, r7);
        } catch (RegistryException e) {

        }

        Collection collection = registry.searchContent("excel");
        String[] paths = (String[]) collection.getContent();

        assertEquals("Search should return the relevant path corresponding to the content which includes " +
                "submitted keywords.", "/c1/r7", paths[0]);

    }

    public void testMSPowerpointIndexing() throws RegistryException {
        Resource r8 = registry.newResource();
        String r8Path = "/c1/r8";
        try {
            r8.setContentStream(getClass().getResourceAsStream("/sample.ppt"));
        } catch (Exception e) {

        }
        r8.setMediaType("application/vnd.ms-powerpoint");
        try {
            registry.put(r8Path, r8);
        } catch (RegistryException e) {

        }

        Collection collection = registry.searchContent("powerpoint");
        String[] paths = (String[]) collection.getContent();

        assertEquals("Search should return the relevant path corresponding to the content which includes " +
                "submitted keywords.", "/c1/r8", paths[0]);

    }

    public void testPDFIndexing() throws RegistryException {
        Resource r9 = registry.newResource();
        String r9Path = "/c1/r9";
        try {
            r9.setContentStream(getClass().getResourceAsStream("/sample.pdf"));
        } catch (Exception e) {

        }
        r9.setMediaType("application/pdf");
        try {
            registry.put(r9Path, r9);
        } catch (RegistryException e) {

        }

        Collection collection = registry.searchContent("pdf");
        String[] paths = (String[]) collection.getContent();

        assertEquals("Search should return the relevant path corresponding to the content which includes " +
                "submitted keywords.", "/c1/r9", paths[0]);

    }
}
