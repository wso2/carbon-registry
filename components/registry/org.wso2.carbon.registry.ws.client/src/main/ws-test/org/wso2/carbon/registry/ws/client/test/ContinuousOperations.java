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

import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

public class ContinuousOperations extends TestSetup {
    public ContinuousOperations(String text) {
        super(text);
    }

    public void testContinousDelete() throws Exception {

        int iterations = 100;

        for (int i = 0; i < iterations; i++) {

            Resource res1 = registry.newResource();
            byte[] r1content = RegistryUtils.encodeString("R2 content");
            res1.setContent(r1content);
            String path = "/con-delete/test/" + i + 1;

            registry.put(path, res1);

            Resource resource1 = registry.get(path);

            assertEquals("File content is not matching", RegistryUtils.decodeBytes((byte[]) resource1.getContent()),
                    RegistryUtils.decodeBytes((byte[]) res1.getContent()));

            registry.delete(path);

            boolean value = false;

            if (registry.resourceExists(path)) {
                value = true;
            }

            assertFalse("Resoruce not found at the path", value);

            res1.discard();
            resource1.discard();
            Thread.sleep(100);
        }
    }

    public void testContinuousUpdate() throws Exception {

        int iterations = 100;

        for (int i = 0; i < iterations; i++) {

            Resource res1 = registry.newResource();
            byte[] r1content = RegistryUtils.encodeString("R2 content");
            res1.setContent(r1content);
            String path = "/con-delete/test-update/" + i + 1;

            registry.put(path, res1);

            Resource resource1 = registry.get(path);

            assertEquals("File content is not matching", RegistryUtils.decodeBytes((byte[]) resource1.getContent()),
                    RegistryUtils.decodeBytes((byte[]) res1.getContent()));

            Resource resource = new ResourceImpl();
            byte[] r1content1 = RegistryUtils.encodeString("R2 content updated");
            resource.setContent(r1content1);
            resource.setProperty("abc", "abc");

            registry.put(path, resource);

            Resource resource2 = registry.get(path);

            assertEquals("File content is not matching", RegistryUtils.decodeBytes((byte[]) resource.getContent()),
                    RegistryUtils.decodeBytes((byte[]) resource2.getContent()));

            resource.discard();
            res1.discard();
            resource1.discard();
            resource2.discard();
            Thread.sleep(100);
        }
    }
}
