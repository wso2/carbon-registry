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

package org.wso2.carbon.registry.eventing.services;

import org.wso2.carbon.event.core.exception.EventBrokerException;
import org.wso2.carbon.event.core.subscription.Subscription;
import org.wso2.carbon.registry.common.eventing.NotificationService;

import java.util.List;

public interface EventingService extends NotificationService {

    /**
     * Retrieves subscription manager.
     *
     * @return an instance of the subscription manager associated with the internal event source.
     */
    public List<Subscription> getAllSubscriptions() throws EventBrokerException;

    /**
     * Retrieves remote subscription manager.
     *
     * @param userName the user name used to connect to the remote registry.
     * @param remoteURL the URL of the remote registry.
     * @return an instance of the subscription manager associated with the remote event source.
     */
    public List<Subscription> getAllSubscriptions(String userName, String remoteURL)
            throws EventBrokerException;

    /**
     * Retrieves the URL of the associated subscription manager.
     *
     * @return URL of the associated subscription manager.
     */
    public String getSubscriptionManagerUrl();

    /**
     * Creates a subscription.
     *
     * @param subscription subscription object to add.
     * @return unique identifier corresponding to the subscription made.
     */
    public String subscribe(Subscription subscription);

    /**
     * Creates a remote subscription.
     *
     * @param subscription subscription object to add.
     * @param userName the user name used to connect to the remote registry.
     * @param remoteURL the URL of the remote registry.
     * @return unique identifier corresponding to the subscription made.
     */
    public String subscribe(Subscription subscription, String userName, String remoteURL);

    /**
     * Retrieves the subscription corresponding to the given unique identifier.
     *
     * @param id unique identifier.
     * @return the corresponding subscription.
     */
    public Subscription getSubscription(String id);

    /**
     * Retrieves the remote subscription corresponding to the given unique identifier.
     *
     * @param id unique identifier.
     * @param userName the user name used to connect to the remote registry.
     * @param remoteURL the URL of the remote registry.
     * @return the corresponding subscription.
     */
    public Subscription getSubscription(String id, String userName, String remoteURL);

    /**
     * Remove subscription corresponding to the given unique identifier.
     *
     * @param id unique identifier.
     * @return the status of the operation.
     */
    public boolean unsubscribe(String id);

    /**
     * Remove subscription corresponding to the given unique identifier.
     *
     * @param id unique identifier.
     * @param userName the user name used to connect to the remote registry.
     * @param remoteURL the URL of the remote registry.
     * @return the status of the operation.
     */
    public boolean unsubscribe(String id, String userName, String remoteURL);
}
