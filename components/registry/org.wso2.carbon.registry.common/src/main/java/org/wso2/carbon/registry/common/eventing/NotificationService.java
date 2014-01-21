/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.common.eventing;

import java.util.Map;

public interface NotificationService {

    /**
     * Sends a notification to the default endpoint by forwarding the event.
     *
     * @param event the event to be sent.
     * @throws Exception if the operation failed.
     */
    public void notify(RegistryEvent event) throws Exception;

    /**
     * Sends a notification (SOAP) to the given endpoint by forwarding the event.
     *
     * @param event the event to be sent.
     * @param endpoint the endpoint to be notified.
     * @throws Exception if the operation failed.
     */
    public void notify(RegistryEvent event, String endpoint) throws Exception;

    /**
     * Sends a notification (SOAP or REST) to the given endpoint by forwarding the event.
     *
     * @param event the event to be sent.
     * @param endpoint the endpoint to be notified.
     * @param doRest indicates whether the given notification should be sent as a REST message.
     * @throws Exception if the operation failed.
     */
    public void notify(RegistryEvent event, String endpoint, boolean doRest)
            throws Exception;

    /**
     * Registers an event type, so that users can subscribe from the front-end administration
     * console.
     *
     * @param typeId id of the event type.
     * @param resourceEvent the event associated with resources.
     * @param collectionEvent the event associated with collections.
     */
    public void registerEventType(String typeId, String resourceEvent, String collectionEvent);

    /**
     * Retrieves a map of registered event types. This map is of format &lt; String, String[] &gt;.
     *
     * @return map of event types.
     */
    public Map getEventTypes();

    /**
     * Registers an exclusion for a given event type. if you want to disallow subscription to a
     * particular event type for a particular path or a set of paths, you can simply register one or
     * more exclusion pattern for that event type.
     *
     * @param typeId id of the event type.
     * @param path corresponding path.
     */
    public void registerEventTypeExclusion(String typeId, String path);

    /**
     * Determines whether an event type exclusion exists.
     *
     * @param typeId id of the event type.
     * @param path corresponding path.
     * @return true if an exclusion exists or false otherwise.
     */
    public boolean isEventTypeExclusionRegistered(String typeId, String path);

}
