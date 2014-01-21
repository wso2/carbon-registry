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

package org.wso2.carbon.registry.relations.beans;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

@SuppressWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class AssociationTreeBean {

    private String resourcePath;
    private String assoType;
    private String associationTree = "";
    private int assoIndex = 0;          // Use to give an unique id for the generated html elements
    private String [] treeCache;

    protected String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getAssoType() {
        //if (assoType.equals(CommonConstants.ASSOCIATION_TYPE01)) assoType = "Dependency";
        return assoType;
    }

    public void setAssoType(String assoType) {
        this.assoType = assoType;
    }

    public String getAssociationTree() {
        return associationTree;
    }

    public void setAssociationTree(String associationTree) {
        this.associationTree = associationTree;
    }

    public int getAssoIndex() {
        return assoIndex;
    }

    public void setAssoIndex(int assoIndex) {
        this.assoIndex = assoIndex;
    }

    public String [] getTreeCache() {
        return treeCache;
    }

    public void setTreeCache(String [] treeCache) {
        this.treeCache = treeCache;
    }
}
