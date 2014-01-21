/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.registry.eventing.events;

import org.wso2.carbon.registry.common.eventing.RegistryEvent;

public class ChildCreatedEvent<T> extends RegistryEvent<T> {

    private String resourcePath = null;

    public static final String EVENT_NAME = "ChildCreated";

    public ChildCreatedEvent() {
        super();
    }

    /**
     * Construct the Registry Event by using the message
     * @param message any Object
     */
    public ChildCreatedEvent(T message) {
        super(message);
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
        setTopic(TOPIC_SEPARATOR + EVENT_NAME + resourcePath);
        setOperationDetails(resourcePath, EVENT_NAME, RegistryEvent.ResourceType.COLLECTION);
    }

    public String getResourcePath() {
        return resourcePath;
    }
}
