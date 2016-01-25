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

import java.util.List;

/**
 * The Collection is specific type of {@link Resource} that can contain other
 * resources (including other collections). We call the resources contained in a collection as the
 * children of the collection and the collection is called the parent of its children.
 */
public class Collection extends Resource {

    private List<String> childrens;

    /**
     * Get the list of UUID of the child resources.
     *
     * @return childrens List of UUID of child resources
     */
    public List<String> getChildrens() {
        return childrens;
    }

    /**
     * Set the list of UUID of the child resources.
     *
     * @param childrens List of UUIDs
     */
    public void setChildrens(List<String> childrens) {
        this.childrens = childrens;
    }

    /**
     * Method to get the content of the collection is not supported.
     *
     * @throws MetadataStoreException operation is not support for collection
     */
    public Object getContent() throws MetadataStoreException {
        throw new MetadataStoreException("Get content operation is not supported for Collections");
    }
}
