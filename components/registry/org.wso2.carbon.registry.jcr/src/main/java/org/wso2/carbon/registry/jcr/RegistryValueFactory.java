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

import javax.jcr.*;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;

public class RegistryValueFactory implements ValueFactory {
    RegistryValue value;
    RegistryBinary binary;

    public Value createValue(String s) {

        value = new RegistryValue(s);

        return value;
    }

    public Value createValue(String s, int i) throws ValueFormatException {

        value = new RegistryValue(s, i);

        return value;
    }

    public Value createValue(long l) {
        value = new RegistryValue(l);

        return value;
    }

    public Value createValue(double v) {

        value = new RegistryValue(v);

        return value;
    }

    public Value createValue(BigDecimal bigDecimal) {

        value = new RegistryValue(bigDecimal);

        return value;

    }

    public Value createValue(boolean b) {
        value = new RegistryValue(b);

        return value;
    }

    public Value createValue(Calendar calendar) {

        value = new RegistryValue(calendar);

        return value;

    }

    public Value createValue(InputStream inputStream) {
        value = new RegistryValue(inputStream);

        return value;
    }

    public Value createValue(Binary binary) {
        value = new RegistryValue(binary);

        return value;
    }

    public Value createValue(Node node) throws RepositoryException {
        value = new RegistryValue(node);

        return value;
    }

    public Value createValue(Node node, boolean b) throws RepositoryException {

        //Node-value ,boolean-weak
        //weak-weak - a boolean. If true then a PropertyType.WEAKREFERENCE is created, otherwise a PropertyType.REFERENCE is created.

        value = new RegistryValue(node);

        return value;
    }

    public Binary createBinary(InputStream inputStream) throws RepositoryException {

        binary = new RegistryBinary(inputStream);

        return binary;
    }
}
