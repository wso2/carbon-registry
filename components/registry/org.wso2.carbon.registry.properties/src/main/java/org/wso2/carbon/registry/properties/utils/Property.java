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

package org.wso2.carbon.registry.properties.utils;

/**
 * A bean to represent a registry property.
 */
public class Property {

    private String key;
    private String value;

    /**
     * Method to get the key of the property.
     *
     * @return the property key.
     */
    public String getKey() {
        return key;
    }

    /**
     * Method to set the key of the property.
     *
     * @param key the property key.
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Method to get the value of the property
     *
     * @return the property value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Method to set the value of the property.
     *
     * @param value the property value.
     */
    public void setValue(String value) {
        this.value = value;
    }
}
