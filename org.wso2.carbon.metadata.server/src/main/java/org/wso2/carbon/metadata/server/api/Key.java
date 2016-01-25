/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.metadata.server.api;

/**
 * Contains the path of resource or collection.
 */
public class Key {

    /**
     * User provided key or Resource path of the resource.
     */
    private String key;

    /**
     * Method to get the resource path.
     *
     * @return the User provided key or resource path.
     */
    public String getKey() {
        return key;
    }

    /**
     * Method to set the resource path.
     *
     * @param key the User provided key or resource path.
     */
    public void setKey(String key) {
        this.key = key;
    }
}
