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
 * The WSCollection class is a web service compatible version of the Collection class.
 * It is used to represent the difference between a WSResource and WSCollection.
 */
public class WSCollection extends WSResource {
    
    protected int childCount;
    protected String[] children;

    /**
     * Returns the child count of the Collection
     * 
     * @return child count
     */
    public int getChildCount() {
        return childCount;
    }

    /**
     * Sets the child count of the Collection
     * 
     * @param childCount child count
     */
    public void setChildCount(int childCount) {
        this.childCount = childCount;
    }

    /**
     * Returns the children of the Collection
     *
     * @return child resource paths.
     */
    public String[] getChildren() {
        return children;
    }

    /**
     * Sets the children of the Collection
     *
     * @param children child resource paths.
     */
    public void setChildren(String[] children) {
        this.children = children;
    }
}
