/*
 *  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.wso2.carbon.registry.admin.api.jmx;

/**
 * API methods for performing subscription operations.
 */
public interface ISubscriptionsService {

    /**
     * Creates a new subscription.
     *
     * @param endpoint        The endpoint to subscribe to.
     * @param isRestEndpoint  Whether the endpoint is a REST endpoint.
     * @param path            The resource path.
     * @param eventName       The event name.
     *
     * @return the subscription identifier.
     */
    String subscribe(String endpoint, boolean isRestEndpoint, String path, String eventName);

    /**
     * Unsubscribes a user from the system.
     *
     * @param id the subscription identifier
     */
    void unsubscribe(String id);

    /**
     * Retrieves list of event names.
     *
     * @return list of event names.
     */
    String[] getEventNames();

    /**
     * Retrieves list of subscriptions.
     *
     * @return The list of subscriptions.
     */
    String[] getList();

}
