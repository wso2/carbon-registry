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
package org.wso2.carbon.registry.eventing.internal;

import org.wso2.carbon.registry.admin.api.jmx.INotificationService;

import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

public class JMXEventsBean implements INotificationService {

    private static final SimpleDateFormat NOTIFICATION_TIME =
            new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss,SSS] ");

    private volatile Queue<String> notifications = new LinkedList<String>();
    private NotificationBroadcasterSupport broadcaster;
    private String event;
    private AtomicLong sequenceNumber = new AtomicLong();

    public void addNotification(Date timestamp, String notification) {
        broadcaster.sendNotification(new Notification(event, broadcaster,
                sequenceNumber.getAndIncrement(), timestamp.getTime(), notification));
        notifications.add(NOTIFICATION_TIME.format(timestamp) + notification);
    }

    public String[] getList() {
        return notifications.toArray(new String[notifications.size()]);
    }

    public void clearAll() {
        notifications = new LinkedList<String>();
    }

    public void registerBroadcaster(NotificationBroadcasterSupport broadcaster, String event) {
        this.broadcaster = broadcaster;
        this.event = event;
    }
}
