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
package org.wso2.carbon.metadata.server.impl;

import org.wso2.carbon.metadata.server.api.Collection;

/**
 * Created by chandana on 1/27/16.
 */
public class CollectionImpl extends Collection {

    protected CollectionImpl(String uuid) {
        super(uuid);
    }

    /**
     * Path of the parent collection of the resource. If the resource path is
     * /servers/config/users.xml, parent path is /servers/config.
     */
    protected String parentPath;

    /**
     * Method to get the parent path.
     *
     * @return the parent path.
     */
    public String getParentPath() {
        if (parentPath != null) {
            return parentPath;
        }
        if ((this.getKey() == null) || this.getKey().length() == 1) {
            return null;
        }
        int i = this.getKey().lastIndexOf('/');
        return this.getKey().substring(0, i);
    }

    /**
     * Method to set the parent path.
     *
     * @param parentPath the parent path.
     */
    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }
}
