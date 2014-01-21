/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.ws.client.test.security;

import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

public class RenameTest extends SecurityTestSetup {
    public RenameTest(String text) {
        super(text);
    }

    public void testRootLevelResourceRename() throws Exception {

        Resource r1 = registry.newResource();
        r1.setProperty("test", "rename");
        r1.setContent("some text");
        registry.put("/rename2", r1);

        registry.rename("/rename2", "/rename4");

        boolean failed = false;
        try {
            registry.get("/rename2");
        } catch (Exception e) {
            failed = true;
        }
        assertTrue("Resource should not be accessible from the old path after renaming.", failed);

        Resource newR1 = registry.get("/rename4");
        assertEquals("Resource should contain a property with name test and value rename.",
                newR1.getProperty("test"), "rename");
    }

    public void testGeneralResourceRename() throws Exception {

        Resource r1 = registry.newResource();
        r1.setProperty("test", "rename");
        r1.setContent("some text");
        registry.put("/tests/rename1", r1);

        registry.rename("/tests/rename1", "rename2");

        boolean failed = false;
        try {
            Resource originalR1 = registry.get("/tests/rename1");
        } catch (Exception e) {
            failed = true;
        }
        assertTrue("Resource should not be accessible from the old path after renaming.", failed);

        Resource newR1 = registry.get("/tests/rename2");
        assertEquals("Resource should contain a property with name test and value rename.",
                newR1.getProperty("test"), "rename");
    }

    public void testRootLevelCollectionRename() throws Exception {

        Resource r1 = registry.newResource();
        r1.setProperty("test", "rename");
        r1.setContent("some text");
        registry.put("/rename34k/c1/dummy", r1);

        registry.rename("/rename34k", "/rename44k");

        boolean failed = false;
        try {
            Resource originalR1 = registry.get("/rename34k/c1/dummy");
        } catch (Exception e) {
            failed = true;
        }
        assertTrue("Resource should not be " +
                "accessible from the old path after renaming the parent.", failed);

        Resource newR1 = registry.get("/rename44k/c1/dummy");
        assertEquals("Resource should contain a property with name test and value rename.",
                newR1.getProperty("test"), "rename");
    }

    public void testGeneralCollectionRename() throws Exception {

        Resource r1 = registry.newResource();
        r1.setProperty("test", "rename");
        r1.setContent("some text");
        registry.put("/c2/rename3/c1/dummy", r1);

        registry.rename("/c2/rename3", "rename4");

        boolean failed = false;
        try {
            Resource originalR1 = registry.get("/c2/rename3/c1/dummy");
        } catch (Exception e) {
            failed = true;
        }
        assertTrue("Resource should not be " +
                "accessible from the old path after renaming the parent.", failed);

        Resource newR1 = registry.get("/c2/rename4/c1/dummy");
        assertEquals("Resource should contain a property with name test and value rename.",
                newR1.getProperty("test"), "rename");
    }
}
