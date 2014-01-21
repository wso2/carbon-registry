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

import javax.management.NotificationBroadcasterSupport;
import java.util.Date;

/**
 * Extended API for handling notifications.
 */
public interface INotificationService extends IEventsService {

    /**
     * Method to create a notification.
     *
     * @param timestamp    the timestamp
     * @param notification the message.
     */
    void addNotification(Date timestamp, String notification);

    /**
     * Registers a JMX broadcaster.
     *
     * @param broadcaster the broadcaster.
     * @param event       the type of event.
     */
    void registerBroadcaster(NotificationBroadcasterSupport broadcaster, String event);

}
