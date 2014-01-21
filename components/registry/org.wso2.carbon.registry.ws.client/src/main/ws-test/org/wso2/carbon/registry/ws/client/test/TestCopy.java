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

package org.wso2.carbon.registry.ws.client.test;

import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

public class TestCopy extends TestSetup {

    public TestCopy(String text) {
        super(text);
    }

    public void testResourceCopy() throws Exception {

        Resource r1 = registry.newResource();
        r1.setProperty("test", "copy");
        r1.setContent("c");
        registry.put("/test1/copy/c1/copy1", r1);

        Collection c1 = registry.newCollection();
        registry.put("/test1/move", c1);

        registry.copy("/test1/copy/c1/copy1", "/test1/copy/c2/copy2");

        Resource newR1 = registry.get("/test1/copy/c2/copy2");
        assertEquals("Copied resource should have a property named 'test' with value 'copy'.",
                newR1.getProperty("test"), "copy");

        Resource oldR1 = registry.get("/test1/copy/c1/copy1");
        assertEquals("Original resource should have a property named 'test' with value 'copy'.",
                oldR1.getProperty("test"), "copy");

        String newContent = RegistryUtils.decodeBytes((byte[]) newR1.getContent());
        String oldContent = RegistryUtils.decodeBytes((byte[]) oldR1.getContent();
        assertEquals("Contents are not equal in copied resources", newContent, oldContent);
    }

    public void testCollectionCopy() throws Exception {

        Resource r1 = registry.newResource();
        r1.setProperty("test", "copy");
        r1.setContent("c");
        registry.put("/test1/copy/copy3/c3/resource1", r1);

        Collection c1 = registry.newCollection();
        registry.put("/test1/move", c1);

        registry.copy("/test1/copy/copy3", "/test1/newc/copy3");

        Resource newR1 = registry.get("/test1/newc/copy3/c3/resource1");
        assertEquals("Copied resource should have a property named 'test' with value 'copy'.",
                newR1.getProperty("test"), "copy");

        Resource oldR1 = registry.get("/test1/copy/copy3/c3/resource1");
        assertEquals("Original resource should have a property named 'test' with value 'copy'.",
                oldR1.getProperty("test"), "copy");
    }
}
