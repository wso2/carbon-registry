/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.jcr;

import javax.jcr.Binary;
import javax.jcr.RepositoryException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import  java.io.InputStream;

public class RegistryBinary implements Binary {

    private Object key;

    public RegistryBinary(Object key) {
        this.key = key;
    }

    public InputStream getStream() throws RepositoryException {

        ByteArrayInputStream in = null;
        byte[] a = null;

        if (key != null) {

            a = key.toString().getBytes();
            in = new ByteArrayInputStream(a);
        }


        return in;
    }

    public int read(byte[] bytes, long l) throws IOException, RepositoryException {  //TODO

        return 0;
    }

    public long getSize() throws RepositoryException {

        if (key != null) {
            long byteSize = key.toString().getBytes().length;


            return byteSize;
        } else
            return 0;

    }

    public void dispose() {     //TODO


    }


}
