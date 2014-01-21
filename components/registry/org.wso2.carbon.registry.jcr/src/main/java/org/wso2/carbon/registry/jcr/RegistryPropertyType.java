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


public final class RegistryPropertyType {

    public static final int STRING = 1;

    public static final int BINARY = 2;

    public static final int LONG = 3;

    public static final int DOUBLE = 4;

    public static final int DATE = 5;

    public static final int BOOLEAN = 6;

    public static final int NAME = 7;

    public static final int PATH = 8;

    public static final int REFERENCE = 9;

    public static final int WEAKREFERENCE = 10;

    public static final int URI = 11;

    public static final int DECIMAL = 12;

    public static final int UNDEFINED = 0;

    public static final String TYPENAME_STRING = "String";

    public static final String TYPENAME_BINARY = "Binary";

    public static final String TYPENAME_LONG = "Long";

    public static final String TYPENAME_DOUBLE = "Double";

    public static final String TYPENAME_DECIMAL = "Decimal";

    public static final String TYPENAME_DATE = "Date";

    public static final String TYPENAME_BOOLEAN = "Boolean";

    public static final String TYPENAME_NAME = "Name";

    public static final String TYPENAME_PATH = "Path";

    public static final String TYPENAME_REFERENCE = "Reference";

    public static final String TYPENAME_WEAKREFERENCE = "WeakReference";

    public static final String TYPENAME_URI = "URI";

    public static final String TYPENAME_UNDEFINED = "undefined";

    public static String nameFromValue(int type) {
        switch (type) {
            case STRING:
                return TYPENAME_STRING;
            case BINARY:
                return TYPENAME_BINARY;
            case BOOLEAN:
                return TYPENAME_BOOLEAN;
            case LONG:
                return TYPENAME_LONG;
            case DOUBLE:
                return TYPENAME_DOUBLE;
            case DECIMAL:
                return TYPENAME_DECIMAL;
            case DATE:
                return TYPENAME_DATE;
            case NAME:
                return TYPENAME_NAME;
            case PATH:
                return TYPENAME_PATH;
            case REFERENCE:
                return TYPENAME_REFERENCE;
            case WEAKREFERENCE:
                return TYPENAME_WEAKREFERENCE;
            case URI:
                return TYPENAME_URI;
            case UNDEFINED:
                return TYPENAME_UNDEFINED;
            default:
                throw new IllegalArgumentException("unknown property type: " + type);
        }
    }


    public static int valueFromName(String name) {
        if (name.equals(TYPENAME_STRING)) {
            return STRING;
        } else if (name.equals(TYPENAME_BINARY)) {
            return BINARY;
        } else if (name.equals(TYPENAME_BOOLEAN)) {
            return BOOLEAN;
        } else if (name.equals(TYPENAME_LONG)) {
            return LONG;
        } else if (name.equals(TYPENAME_DOUBLE)) {
            return DOUBLE;
        } else if (name.equals(TYPENAME_DECIMAL)) {
            return DECIMAL;
        } else if (name.equals(TYPENAME_DATE)) {
            return DATE;
        } else if (name.equals(TYPENAME_NAME)) {
            return NAME;
        } else if (name.equals(TYPENAME_PATH)) {
            return PATH;
        } else if (name.equals(TYPENAME_REFERENCE)) {
            return REFERENCE;
        } else if (name.equals(TYPENAME_WEAKREFERENCE)) {
            return WEAKREFERENCE;
        } else if (name.equals(TYPENAME_URI)) {
            return URI;
        } else if (name.equals(TYPENAME_UNDEFINED)) {
            return UNDEFINED;
        } else {
            throw new IllegalArgumentException("unknown type: " + name);
        }
    }


    private RegistryPropertyType() {
    }
}