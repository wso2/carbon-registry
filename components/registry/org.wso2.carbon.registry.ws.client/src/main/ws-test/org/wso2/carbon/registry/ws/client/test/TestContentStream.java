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

import java.io.*;

import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

public class TestContentStream extends TestSetup {
    public TestContentStream(String text) {
        super(text);
    }

    public void testputResourceasStreamXML() throws RegistryException, FileNotFoundException, Exception {

        final String description = "testPutXMLResourceAsBytes";
        final String mediaType = "application/xml";

        // Establish where we are putting the resource in registry
        final String registryPath = "/wso2registry/conf/pom.xml";

        InputStream is = new BufferedInputStream(new FileInputStream("pom.xml"));
        String st = null;
        try {
            st = slurp(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Resource resource = registry.newResource();

        resource.setContent(RegistryUtils.encodeString(st));
        resource.setDescription(description);
        resource.setMediaType(mediaType);
        registry.put(registryPath, resource);


        Resource r2 = registry.get(registryPath);

        assertEquals("File content is not matching", RegistryUtils.decodeBytes((byte[]) resource.getContent()),
                RegistryUtils.decodeBytes((byte[]) r2.getContent()));

    }

    public void testContentStreaming() throws Exception {


        Resource r3 = registry.newResource();
        String path = "/content/stream/content.txt";
        r3.setContent(RegistryUtils.encodeString("this is the content"));
        r3.setDescription("this is test desc");
        r3.setMediaType("plain/text");
        r3.setProperty("test2", "value2");
        r3.setProperty("test1", "value1");
        registry.put(path, r3);

        Resource r4 = registry.get("/content/stream/content.txt");

        assertEquals("Content is not equal.", RegistryUtils.decodeBytes((byte[]) r3.getContent()),
                RegistryUtils.decodeBytes((byte[]) r4.getContent()));

        InputStream isTest = r4.getContentStream();

        assertEquals("Content stream is not equal.", RegistryUtils.decodeBytes((byte[]) r3.getContent()),
                convertStreamToString(isTest));

        r3.discard();
    }

    public void testsetContainStreamXML() throws Exception {

        final String description = "testPutXMLResourceAsBytes";
        final String mediaType = "application/xml";

        // Establish where we are putting the resource in registry
        final String registryPath = "/wso2registry/contentstream/conf/pom.xml";

        InputStream is = new BufferedInputStream(new FileInputStream("pom.xml"));

        Resource resource = registry.newResource();

        resource.setContentStream(is);
        resource.setDescription(description);
        resource.setMediaType(mediaType);
        registry.put(registryPath, resource);


        Resource r2 = registry.get(registryPath);

        assertEquals("File content is not matching", RegistryUtils.decodeBytes((byte[]) resource.getContent()),
                RegistryUtils.decodeBytes((byte[]) r2.getContent()));

    }

    public String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }

    public static String slurp(InputStream in) throws IOException {
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1;) {
            out.append(new String(b, 0, n));
        }
        return out.toString();
    }
}
