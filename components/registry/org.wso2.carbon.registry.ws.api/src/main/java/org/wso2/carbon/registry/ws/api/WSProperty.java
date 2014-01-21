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
 * The WSProperty class is a web service compatible version of the Java Property class.
 *
 */
public class WSProperty {
    
    private String key;
    private String[] values;
    
    /**
     * Get the key
     * 
     * @return key
     */
    public String getKey() {
        return key;
    }
    
    /**
     * Set the key
     * 
     * @param key
     */
    public void setKey(String key) {
        this.key = key;
    }
    
    /**
     * Get values according to the corresponding key
     * 
     * @return values
     */
    public String[] getValues() {
        return values;
    }
    
    /**
     * Set the values for the corresponding key
     * 
     * @param values
     */
    public void setValues(String[] values) {
        this.values = values;
    }
}
