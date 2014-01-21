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

package org.wso2.carbon.registry.common.beans.utils;

public class EventType {

    private String resourceEvent = null;
    private String collectionEvent = null;
    private String id = null;

    public String getResourceEvent() {
        return resourceEvent;
    }

    public void setResourceEvent(String resourceEvent) {
        this.resourceEvent = resourceEvent;
    }

    public String getCollectionEvent() {
        return collectionEvent;
    }

    public void setCollectionEvent(String collectionEvent) {
        this.collectionEvent = collectionEvent;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
