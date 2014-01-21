/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.resource.services.utils;

import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.io.InputStream;
import java.io.IOException;

public class ResourceContentInputStream extends InputStream {

    private Resource resource;
    private InputStream inputStream;

    public ResourceContentInputStream(Resource resource) throws RegistryException {
        this.resource = resource;
        inputStream = resource.getContentStream();
    }

    public int read() throws IOException {
        return inputStream.read();
    }

    public int read(byte[] b) throws IOException {
        return inputStream.read(b);
    }

    public int read(byte[] b, int off, int len) throws IOException {
        return inputStream.read(b, off, len);
    }

    public long skip(long n) throws IOException {
        return inputStream.skip(n);
    }

    public int available() throws IOException {
        return inputStream.available();
    }

    public void close() throws IOException {
        inputStream.close();
        resource.discard();        
    }

    public void mark(int readlimit) {
        inputStream.mark(readlimit);
    }

    public void reset() throws IOException {
        inputStream.reset();
    }

    public boolean markSupported() {
        return inputStream.markSupported();
    }
}
