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
package org.wso2.carbon.registry.extensions.jmx;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.common.utils.CommonUtil;
import org.wso2.carbon.registry.core.LogEntry;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.LinkedList;
import java.util.List;

public class Activities implements ActivitiesMBean {

    private static final Log log = LogFactory.getLog(Activities.class);

    private Registry registry;

    public Activities(Registry registry) {
        this.registry = registry;
    }

    public String[] getActivitiesForUser(String username) {
        return getActivities(null, username);
    }

    public String[] getActivitiesForPath(String path) {
        return getActivities(path, null);
    }

    public String[] getList() {
        return getActivities(null, null);
    }

    private String[] getActivities(String path, String username) {
        List<String> activities = new LinkedList<String>();
        try {
            LogEntry[] logs = registry.getLogs(path, -1, username, null, null, true);
            for (LogEntry log : logs) {
                switch (log.getAction()) {
                    case LogEntry.ADD:
                        activities.add(log.getUserName() + " has added the resource " +
                                log.getResourcePath() + " at " +
                                CommonUtil.formatDate(log.getDate()) + ".");
                        break;
                    case LogEntry.UPDATE:
                        activities.add(log.getUserName() + " has updated the resource " +
                                log.getResourcePath() + " at " +
                                CommonUtil.formatDate(log.getDate()) + ".");
                        break;
                    case LogEntry.DELETE_RESOURCE:
                        activities.add(log.getUserName() + " has deleted the resource " +
                                log.getResourcePath() + " at " +
                                CommonUtil.formatDate(log.getDate()) + ".");
                        break;
                    case LogEntry.COMMENT:
                        activities.add(log.getUserName() + " has commented on the resource " +
                                log.getResourcePath() + " at " +
                                CommonUtil.formatDate(log.getDate()) + ".");
                        break;
                    case LogEntry.DELETE_COMMENT:
                        activities.add(log.getUserName() + " has deleted a comment on the " +
                                "resource " + log.getResourcePath() + " at " +
                                CommonUtil.formatDate(log.getDate()) + ".");
                        break;
                    case LogEntry.TAG:
                        activities.add(log.getUserName() + " has tagged the resource " +
                                log.getResourcePath() + " at " +
                                CommonUtil.formatDate(log.getDate()) + ".");
                        break;
                    case LogEntry.REMOVE_TAG:
                        activities.add(log.getUserName() + " has removed tag on the resource " +
                                log.getResourcePath() + " at " +
                                CommonUtil.formatDate(log.getDate()) + ".");
                        break;
                    case LogEntry.RATING:
                        activities.add(log.getUserName() + " has rated the resource " +
                                log.getResourcePath() + " at " +
                                CommonUtil.formatDate(log.getDate()) + ".");
                        break;
                    case LogEntry.RESTORE:
                        activities.add(log.getUserName() + " has restored the resource " +
                                log.getResourcePath() + " at " +
                                CommonUtil.formatDate(log.getDate()) + ".");
                        break;
                    case LogEntry.ADD_ASSOCIATION:
                        activities.add(log.getUserName() + " has created an association to " +
                                "resource " + log.getResourcePath() + " at " +
                                CommonUtil.formatDate(log.getDate()) + ".");
                        break;
                    case LogEntry.REMOVE_ASSOCIATION:
                        activities.add(log.getUserName() + " has removed an association to " +
                                "resource " + log.getResourcePath() + " at " +
                                CommonUtil.formatDate(log.getDate()) + ".");
                        break;
                    case LogEntry.CREATE_REMOTE_LINK:
                        activities.add(log.getUserName() + " has created a remote link to " +
                                "resource " + log.getResourcePath() + " at " +
                                CommonUtil.formatDate(log.getDate()) + ".");
                        break;
                    case LogEntry.CREATE_SYMBOLIC_LINK:
                        activities.add(log.getUserName() + " has created a symbolic link to " +
                                "resource " + log.getResourcePath() + " at " +
                                CommonUtil.formatDate(log.getDate()) + ".");
                        break;
                    case LogEntry.REMOVE_LINK:
                        activities.add(log.getUserName() + " has removed a link to resource " +
                                log.getResourcePath() + " at " +
                                CommonUtil.formatDate(log.getDate()) + ".");
                        break;
                    case LogEntry.ASSOCIATE_ASPECT:
                        activities.add(log.getUserName() + " has associated an aspect to " +
                                "resource " + log.getResourcePath() + " at " +
                                CommonUtil.formatDate(log.getDate()) + ".");
                        break;
                }
            }
        } catch (RegistryException e) {
            String msg = "An error occurred while reading audit logs.";
            log.error(msg, e);
            // we are unable to throw a customized exception or an exception with the cause or if
            // not, JConsole needs additional Jars to marshall exceptions.
            throw new RuntimeException(Utils.buildMessageForRuntimeException(e, msg));
        }
        return activities.toArray(new String[activities.size()]);
    }
}
