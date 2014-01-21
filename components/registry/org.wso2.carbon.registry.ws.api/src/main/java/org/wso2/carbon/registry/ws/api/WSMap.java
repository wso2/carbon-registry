/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.registry.ws.api;

/**
 * The WSMap class is a web service compatible version of the Java Map class.
 * It represents a single key value pair
 */
public class WSMap {
    private String key;
    private String value;
    
    /**
     * Set key
     * 
     * @param key
     */

    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Set value
     * 
     * @param value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /** 
     * Get key
     * 
     * @return key
     */
    public String getKey() {

        return key;
    }

    /**
     * Get value
     * 
     * @return string
     */
    public String getValue() {
        return value;
    }
}
