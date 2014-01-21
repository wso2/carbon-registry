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

package org.wso2.carbon.registry.resource.ui.processors.utils;

public class ResourceTreeData {

    String resourceTree;
    int resourceTreeIndex;

    public ResourceTreeData() {
        resourceTree = "";
        resourceTreeIndex = 1;
    }

    public String getResourceTree() {
        return resourceTree;
    }

    public void setResourceTree(String resourceTree) {
        this.resourceTree = resourceTree;
    }

    public void appendToTree(String treePart) {
        resourceTree += treePart;
    }

    public int getResourceTreeIndex() {
        return resourceTreeIndex;
    }

    public void setResourceTreeIndex(int resourceTreeIndex) {
        this.resourceTreeIndex = resourceTreeIndex;
    }

    public void incrementTreeIndex() {
        resourceTreeIndex++;
    }
}
