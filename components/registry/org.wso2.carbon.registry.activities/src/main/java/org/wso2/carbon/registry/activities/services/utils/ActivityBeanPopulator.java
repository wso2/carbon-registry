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

package org.wso2.carbon.registry.activities.services.utils;

import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.LogEntry;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.common.beans.ActivityBean;
import org.wso2.carbon.registry.common.utils.CommonUtil;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;

public class ActivityBeanPopulator {

    private static final Log log = LogFactory.getLog(ActivityBeanPopulator.class);

    public static ActivityBean populate(UserRegistry userRegistry, String userName, String resourcePath, String fromDate,
                                        String toDate, String filter, String pageStr) throws Exception{
        UserRealm realm = userRegistry.getUserRealm();
        UserStoreManager reader = realm.getUserStoreManager();
        String[] roles = reader.getRoleListOfUser(userRegistry.getUserName());
        List list = Arrays.asList(roles);
        ActivityBean activityBean = new ActivityBean();

        if (resourcePath != null && resourcePath.equals("")) {
            resourcePath = null;
        }

        if (userName != null && userName.equals("")) {
            userName = null;
        }

        if (fromDate != null && fromDate.equals("")) {
            fromDate = null;
        }

        if (toDate != null && toDate.equals("")) {
            toDate = null;
        }

        if (filter == null) {
            filter = "1";
        }

        List<Integer> filterValues = new LinkedList<Integer>();
        if (filter.equals("resourceAdd")) {
            filterValues.add(LogEntry.ADD);

        } else if (filter.equals("resourceUpdate")) {
            filterValues.add(LogEntry.UPDATE);

        } else if (filter.equals("commentings")) {
            filterValues.add(LogEntry.COMMENT);
            filterValues.add(LogEntry.DELETE_COMMENT);

        } else if (filter.equals("taggings")) {
            filterValues.add(LogEntry.TAG);
            filterValues.add(LogEntry.REMOVE_TAG);

        } else if (filter.equals("ratings")) {
            filterValues.add(LogEntry.RATING);

        } else if (filter.equals("restore")) {
            filterValues.add(LogEntry.RESTORE);

        } else if (filter.equals("delete")) {
            filterValues.add(LogEntry.DELETE_RESOURCE);

        } else if (filter.equals("addAssociation")) {
            filterValues.add(LogEntry.ADD_ASSOCIATION);

        } else if (filter.equals("removeAssociation")) {
            filterValues.add(LogEntry.REMOVE_ASSOCIATION);

        } else if (filter.equals("createSymbolicLink")) {
            filterValues.add(LogEntry.CREATE_SYMBOLIC_LINK);

        } else if (filter.equals("createRemoteLink")) {
            filterValues.add(LogEntry.CREATE_REMOTE_LINK);

        } else if (filter.equals("removeLink")) {
            filterValues.add(LogEntry.REMOVE_LINK);

        } else if (filter.equals("associateAspect")) {
            filterValues.add(LogEntry.ASSOCIATE_ASPECT);

        } else {
            filterValues.add(-1);

        }

        try {
            List<LogEntry> logList = new LinkedList<LogEntry>();
            for (Integer filterValue : filterValues) {
                logList.addAll(Arrays.asList(userRegistry.getLogs(resourcePath, filterValue,
                        userName, computeDate(fromDate), computeDate(toDate), true)));
            }
            LogEntry[] logs = logList.toArray(new LogEntry[logList.size()]);
            Arrays.sort(logs, new Comparator<LogEntry>() {
                public int compare(LogEntry o1, LogEntry o2) {
                    return o2.getDate().compareTo(o1.getDate());
                }
            });
            if (list.contains(userRegistry.getUserRealm().getRealmConfiguration().getAdminRoleName())) {
                activityBean.setActivity(constructActivityStatements(userRegistry, logs));
            } else{
                activityBean.setActivity(constructActivityStatementsfornormaluser(userRegistry,
                        logs, userRegistry.getUserName()));
            }
        } catch (RegistryException e) {

            String msg = "Failed to get activities for generating activity search results " +
                    e.getMessage();
            log.error(msg, e);
            activityBean.setErrorMessage(msg);
        }
        return activityBean;
    }

    private static String [] constructActivityStatements(Registry registry, LogEntry[] logs) {

        String [] activity = new String [logs.length];

        for (int i = 0; i < logs.length; i++) {

            LogEntry logEntry = logs[i];
            if (logEntry.getAction() == LogEntry.ADD) {
                String entry = Boolean.toString(resourceExists(registry, logEntry)) + "|" + logEntry.getUserName() + "|" + logEntry.getUserName() + "|" + " has added the resource " + "|" + logEntry.getResourcePath() + "|" + logEntry.getResourcePath() + "|" + "  " + CommonUtil.formatDate(logEntry.getDate()) + ".";
                activity[i] = entry;

            } else if (logEntry.getAction() == LogEntry.UPDATE) {
                String entry = Boolean.toString(resourceExists(registry, logEntry)) + "|" + logEntry.getUserName() + "|" + logEntry.getUserName() + "|" + " has updated the resource " + "|" + logEntry.getResourcePath() + "|" + logEntry.getResourcePath() + "|" + "  " + CommonUtil.formatDate(logEntry.getDate()) + ".";
                activity[i] = entry;

            } else if (logEntry.getAction() == LogEntry.DELETE_RESOURCE) {
                String entry = Boolean.toString(resourceExists(registry, logEntry)) + "|" + logEntry.getUserName() + "|" + logEntry.getUserName() + "|" + " has deleted the resource " + "|" + logEntry.getResourcePath() + "|" + logEntry.getResourcePath() + "|" + "  " + CommonUtil.formatDate(logEntry.getDate()) + ".";
                activity[i] = entry;

            } else if (logEntry.getAction() == LogEntry.COMMENT) {
                String entry = Boolean.toString(resourceExists(registry, logEntry)) + "|" + logEntry.getUserName() + "|" + logEntry.getUserName() + "|" + " has commented on resource " + "|" + logEntry.getResourcePath() + "|" + logEntry.getResourcePath() + "|" + "  " + CommonUtil.formatDate(logEntry.getDate()) + " with the following comment." + "|" + logEntry.getActionData();
                activity[i] = entry;

            } else if (logEntry.getAction() == LogEntry.DELETE_COMMENT) {
                String entry = Boolean.toString(resourceExists(registry, logEntry)) + "|" + logEntry.getUserName() + "|" + logEntry.getUserName() + "|" + " has deleted the comment '" + logEntry.getActionData() + "' on the resource " + "|" + logEntry.getResourcePath() + "|" + logEntry.getResourcePath() + "|" + "  " + CommonUtil.formatDate(logEntry.getDate()) + ".";
                activity[i] = entry;

            } else if (logEntry.getAction() == LogEntry.TAG) {
                String entry = Boolean.toString(resourceExists(registry, logEntry)) + "|" + logEntry.getUserName() + "|" + logEntry.getUserName() + "|" + " has tagged the resource " + "|" + logEntry.getResourcePath() + "|" + logEntry.getResourcePath() + "|" + " with tag '" + logEntry.getActionData() + "'  " + CommonUtil.formatDate(logEntry.getDate()) + ".";
                activity[i] = entry;

            } else if (logEntry.getAction() == LogEntry.REMOVE_TAG) {
                String entry = Boolean.toString(resourceExists(registry, logEntry)) + "|" + logEntry.getUserName() + "|" + logEntry.getUserName() + "|" + " has removed the tag '" + logEntry.getActionData() + "' on the resource " + "|" + logEntry.getResourcePath() + "|" + logEntry.getResourcePath() + "|" + "  " + CommonUtil.formatDate(logEntry.getDate()) + ".";
                activity[i] = entry;

            } else if (logEntry.getAction() == LogEntry.RATING) {
                String entry = Boolean.toString(resourceExists(registry, logEntry)) + "|" + logEntry.getUserName() + "|" + logEntry.getUserName() + "|" + " has rated the resource " + "|" + logEntry.getResourcePath() + "|" + logEntry.getResourcePath() + "|" + " with rating " + logEntry.getActionData() + "  " + CommonUtil.formatDate(logEntry.getDate()) + ".";
                activity[i] = entry;

            } else if (logEntry.getAction() == LogEntry.ADD_ASSOCIATION) {
                String[] actionData = logEntry.getActionData().split(";");
                if (actionData != null && actionData.length >= 2) {
                    String entry = Boolean.toString(resourceExists(registry, logEntry)) + "|" + logEntry.getUserName() + "|" + logEntry.getUserName() + "|" + " has added an association of type " + actionData[0] + " from the resource " + "|" + logEntry.getResourcePath() + "|" + logEntry.getResourcePath() + "|" + " to the resource " + actionData[1] + "  " + CommonUtil.formatDate(logEntry.getDate()) + ".";
                    activity[i] = entry;
                }

            } else if (logEntry.getAction() == LogEntry.REMOVE_ASSOCIATION) {
                String[] actionData = logEntry.getActionData().split(";");
                if (actionData != null && actionData.length >= 2) {
                    String entry = Boolean.toString(resourceExists(registry, logEntry)) + "|" + logEntry.getUserName() + "|" + logEntry.getUserName() + "|" + " has removed an association of type " + actionData[0] + " from the resource " + "|" + logEntry.getResourcePath() + "|" + logEntry.getResourcePath() + "|" + " to the resource " + actionData[1] + "  " + CommonUtil.formatDate(logEntry.getDate()) + ".";
                    activity[i] = entry;
                }

            } else if (logEntry.getAction() == LogEntry.RESTORE) {
                String entry = Boolean.toString(resourceExists(registry, logEntry)) + "|" + logEntry.getUserName() + "|" + logEntry.getUserName() + "|" + " has restored the resource " + "|" + logEntry.getResourcePath() + "|" + logEntry.getResourcePath() + "|" + ((logEntry.getActionData() != null) ? " to the version " + logEntry.getActionData() : "") + "  " + CommonUtil.formatDate(logEntry.getDate()) + ".";
                activity[i] = entry;
            } else if (logEntry.getAction() == LogEntry.ASSOCIATE_ASPECT) {
                String entry = Boolean.toString(resourceExists(registry, logEntry)) + "|" + logEntry.getUserName() + "|" + logEntry.getUserName() + "|" + " has associated the aspect " + logEntry.getActionData() + " to the resource " + "|" + logEntry.getResourcePath() + "|" + logEntry.getResourcePath() + "|" + "  " + CommonUtil.formatDate(logEntry.getDate()) + ".";
                activity[i] = entry;
            }
            else if (logEntry.getAction() == LogEntry.COPY) {
                String entry = Boolean.toString(resourceExists(registry, logEntry)) + "|" + logEntry.getUserName() + "|" + logEntry.getUserName() + "|" + " has copied the resource " + "|" + logEntry.getResourcePath() + "|" + logEntry.getResourcePath() + "|" + " to the Location " + logEntry.getActionData() + "  " + CommonUtil.formatDate(logEntry.getDate()) + ".";
                activity[i] = entry;
            }
            else if (logEntry.getAction() == LogEntry.RENAME) {
                String entry = Boolean.toString(resourceExists(registry, logEntry)) + "|" + logEntry.getUserName() + "|" + logEntry.getUserName() + "|" + " has renamed the resource to " + "|" + logEntry.getResourcePath() + "|" + logEntry.getResourcePath() + "|" + " from old Name: " + logEntry.getActionData() + "  " + CommonUtil.formatDate(logEntry.getDate()) + ".";
                activity[i] = entry;
            }
            else if (logEntry.getAction() == LogEntry.MOVE) {
                String entry = Boolean.toString(resourceExists(registry, logEntry)) + "|" + logEntry.getUserName() + "|" + logEntry.getUserName() + "|" + " has moved the resource to " + "|" + logEntry.getResourcePath() + "|" + logEntry.getResourcePath() + "|" + " from the Location " + logEntry.getActionData() + "  " + CommonUtil.formatDate(logEntry.getDate()) + ".";
                activity[i] = entry;
            }
            else if (logEntry.getAction() == LogEntry.CREATE_SYMBOLIC_LINK) {
                String entry = Boolean.toString(resourceExists(registry, logEntry)) + "|" + logEntry.getUserName() + "|" + logEntry.getUserName() + "|" + " has created a symbolic link " + "|" + logEntry.getResourcePath() + "|" + logEntry.getResourcePath() + "|" + " to the Location " + logEntry.getActionData() + "  " + CommonUtil.formatDate(logEntry.getDate()) + ".";
                activity[i] = entry;
            }
            else if (logEntry.getAction() == LogEntry.CREATE_REMOTE_LINK) {
                if (logEntry.getActionData().contains(";")) {
                    String[] actionData = logEntry.getActionData().split(";");
                    if (actionData != null && actionData.length >= 2) {
                        String entry = Boolean.toString(resourceExists(registry, logEntry)) + "|" + logEntry.getUserName() + "|" + logEntry.getUserName() + "|" + " has created a remote link " + "|" + logEntry.getResourcePath() + "|" + logEntry.getResourcePath() + "|" + " to the instance " + actionData[0] + " at path " + actionData[1] + "  " + CommonUtil.formatDate(logEntry.getDate()) + ".";
                        activity[i] = entry;
                    }
                } else {
                    String entry = Boolean.toString(resourceExists(registry, logEntry)) + "|" + logEntry.getUserName() + "|" + logEntry.getUserName() + "|" + " has created a remote link " + "|" + logEntry.getResourcePath() + "|" + logEntry.getResourcePath() + "|" + " to the Location " + logEntry.getActionData() + "  " + CommonUtil.formatDate(logEntry.getDate()) + ".";
                    activity[i] = entry;
                }
            }
            else if (logEntry.getAction() == LogEntry.REMOVE_LINK) {
                String entry = Boolean.toString(resourceExists(registry, logEntry)) + "|" + logEntry.getUserName() + "|" + logEntry.getUserName() + "|" + " has removed link " + "|" + logEntry.getResourcePath() + "|" + logEntry.getResourcePath() + "|" + "  " + CommonUtil.formatDate(logEntry.getDate()) + ".";
                activity[i] = entry;
            }
        }

        return activity;
    }

    private static Boolean resourceExists(Registry registry, LogEntry entry) {
        try {
            return registry != null && entry != null && entry.getResourcePath() != null &&
                    registry.resourceExists(entry.getResourcePath());
        } catch (RegistryException ignore) {
            return false;
        }
    }

    private static String [] constructActivityStatementsfornormaluser(Registry registry, LogEntry[] logs,String Username) {

        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < logs.length; i++) {

            LogEntry logEntry = logs[i];
            if(logEntry.getUserName().equals(Username)){
                if (logEntry.getAction() == LogEntry.ADD) {
                    String entry = Boolean.toString(resourceExists(registry, logEntry)) + "|" + logEntry.getUserName() + "|" + logEntry.getUserName() + "|" + " has added the resource " + "|" + logEntry.getResourcePath() + "|" + logEntry.getResourcePath() + "|" + "  " + CommonUtil.formatDate(logEntry.getDate()) + ".";
                    list.add(entry);

                } else if (logEntry.getAction() == LogEntry.UPDATE) {
                    String entry = Boolean.toString(resourceExists(registry, logEntry)) + "|" + logEntry.getUserName() + "|" + logEntry.getUserName() + "|" + " has updated the resource " + "|" + logEntry.getResourcePath() + "|" + logEntry.getResourcePath() + "|" + "  " + CommonUtil.formatDate(logEntry.getDate()) + ".";
                    list.add(entry);

                } else if (logEntry.getAction() == LogEntry.DELETE_RESOURCE) {
                    String entry = Boolean.toString(resourceExists(registry, logEntry)) + "|" + logEntry.getUserName() + "|" + logEntry.getUserName() + "|" + " has deleted the resource " + "|" + logEntry.getResourcePath() + "|" + logEntry.getResourcePath() + "|" + "  " + CommonUtil.formatDate(logEntry.getDate()) + ".";
                    list.add(entry);

                } else if (logEntry.getAction() == LogEntry.COMMENT) {
                    String entry = Boolean.toString(resourceExists(registry, logEntry)) + "|" + logEntry.getUserName() + "|" + logEntry.getUserName() + "|" + " has commented on resource " + "|" + logEntry.getResourcePath() + "|" + logEntry.getResourcePath() + "|" + "  " + CommonUtil.formatDate(logEntry.getDate()) + " with the following comment." + "|" + logEntry.getActionData();
                    list.add(entry);

                } else if (logEntry.getAction() == LogEntry.DELETE_COMMENT) {
                    String entry = Boolean.toString(resourceExists(registry, logEntry)) + "|" + logEntry.getUserName() + "|" + logEntry.getUserName() + "|" + " has deleted the comment '" + logEntry.getActionData() + "' on the resource " + "|" + logEntry.getResourcePath() + "|" + logEntry.getResourcePath() + "|" + "  " + CommonUtil.formatDate(logEntry.getDate()) + ".";
                    list.add(entry);

                } else if (logEntry.getAction() == LogEntry.TAG) {
                    String entry = Boolean.toString(resourceExists(registry, logEntry)) + "|" + logEntry.getUserName() + "|" + logEntry.getUserName() + "|" + " has tagged the resource " + "|" + logEntry.getResourcePath() + "|" + logEntry.getResourcePath() + "|" + " with tag '" + logEntry.getActionData() + "'  " + CommonUtil.formatDate(logEntry.getDate()) + ".";
                    list.add(entry);

                } else if (logEntry.getAction() == LogEntry.REMOVE_TAG) {
                    String entry = Boolean.toString(resourceExists(registry, logEntry)) + "|" + logEntry.getUserName() + "|" + logEntry.getUserName() + "|" + " has removed the tag '" + logEntry.getActionData() + "' on the resource " + "|" + logEntry.getResourcePath() + "|" + logEntry.getResourcePath() + "|" + "  " + CommonUtil.formatDate(logEntry.getDate()) + ".";
                    list.add(entry);

                } else if (logEntry.getAction() == LogEntry.RATING) {
                    String entry = Boolean.toString(resourceExists(registry, logEntry)) + "|" + logEntry.getUserName() + "|" + logEntry.getUserName() + "|" + " has rated the resource " + "|" + logEntry.getResourcePath() + "|" + logEntry.getResourcePath() + "|" + " with rating " + logEntry.getActionData() + "  " + CommonUtil.formatDate(logEntry.getDate()) + ".";
                    list.add(entry);

                } else if (logEntry.getAction() == LogEntry.ADD_ASSOCIATION) {
                    String[] actionData = logEntry.getActionData().split(";");
                    if (actionData != null && actionData.length >= 2) {
                        String entry = Boolean.toString(resourceExists(registry, logEntry)) + "|" + logEntry.getUserName() + "|" + logEntry.getUserName() + "|" + " has added an association of type " + actionData[0] + " from the resource " + "|" + logEntry.getResourcePath() + "|" + logEntry.getResourcePath() + "|" + " to the resource " + actionData[1] + "  " + CommonUtil.formatDate(logEntry.getDate()) + ".";
                        list.add(entry);
                    }

                } else if (logEntry.getAction() == LogEntry.REMOVE_ASSOCIATION) {
                    String[] actionData = logEntry.getActionData().split(";");
                    if (actionData != null && actionData.length >= 2) {
                        String entry = Boolean.toString(resourceExists(registry, logEntry)) + "|" + logEntry.getUserName() + "|" + logEntry.getUserName() + "|" + " has removed an association of type " + actionData[0] + " from the resource " + "|" + logEntry.getResourcePath() + "|" + logEntry.getResourcePath() + "|" + " to the resource " + actionData[1] + "  " + CommonUtil.formatDate(logEntry.getDate()) + ".";
                        list.add(entry);
                    }

                } else if (logEntry.getAction() == LogEntry.RESTORE) {
                    String entry = Boolean.toString(resourceExists(registry, logEntry)) + "|" + logEntry.getUserName() + "|" + logEntry.getUserName() + "|" + " has restored the resource " + "|" + logEntry.getResourcePath() + "|" + logEntry.getResourcePath() + "|" + ((logEntry.getActionData() != null) ? " to the version " + logEntry.getActionData() : "") + "  " + CommonUtil.formatDate(logEntry.getDate()) + ".";
                    list.add(entry);

                } else if (logEntry.getAction() == LogEntry.ASSOCIATE_ASPECT) {
                    String entry = Boolean.toString(resourceExists(registry, logEntry)) + "|" + logEntry.getUserName() + "|" + logEntry.getUserName() + "|" + " has associated the aspect " + logEntry.getActionData() + " to the resource " + "|" + logEntry.getResourcePath() + "|" + logEntry.getResourcePath() + "|" + "  " + CommonUtil.formatDate(logEntry.getDate()) + ".";
                    list.add(entry);

                } else if (logEntry.getAction() == LogEntry.COPY) {
                    String entry = Boolean.toString(resourceExists(registry, logEntry)) + "|" + logEntry.getUserName() + "|" + logEntry.getUserName() + "|" + " has copied the resource " + "|" + logEntry.getResourcePath() + "|" + logEntry.getResourcePath() + "|" + " to the Location " + logEntry.getActionData() + "  " + CommonUtil.formatDate(logEntry.getDate()) + ".";
                    list.add(entry);

                } else if (logEntry.getAction() == LogEntry.RENAME) {
                    String entry = Boolean.toString(resourceExists(registry, logEntry)) + "|" + logEntry.getUserName() + "|" + logEntry.getUserName() + "|" + " has renamed the resource " + "|" + logEntry.getResourcePath() + "|" + logEntry.getResourcePath() + "|" + " to new Name: " + logEntry.getActionData() + "  " + CommonUtil.formatDate(logEntry.getDate()) + ".";
                    list.add(entry);

                } else if (logEntry.getAction() == LogEntry.MOVE) {
                    String entry = Boolean.toString(resourceExists(registry, logEntry)) + "|" + logEntry.getUserName() + "|" + logEntry.getUserName() + "|" + " has moved the resource " + "|" + logEntry.getResourcePath() + "|" + logEntry.getResourcePath() + "|" + " to the Location " + logEntry.getActionData() + "  " + CommonUtil.formatDate(logEntry.getDate()) + ".";
                    list.add(entry);

                } else if (logEntry.getAction() == LogEntry.CREATE_SYMBOLIC_LINK) {
                    String entry = Boolean.toString(resourceExists(registry, logEntry)) + "|" + logEntry.getUserName() + "|" + logEntry.getUserName() + "|" + " has created a symbolic link " + "|" + logEntry.getResourcePath() + "|" + logEntry.getResourcePath() + "|" + " to the Location " + logEntry.getActionData() + "  " + CommonUtil.formatDate(logEntry.getDate()) + ".";
                    list.add(entry);

                } else if (logEntry.getAction() == LogEntry.CREATE_REMOTE_LINK) {
                    if (logEntry.getActionData().contains(";")) {
                        String[] actionData = logEntry.getActionData().split(";");
                        if (actionData != null && actionData.length >= 2) {
                            String entry = Boolean.toString(resourceExists(registry, logEntry)) + "|" + logEntry.getUserName() + "|" + logEntry.getUserName() + "|" + " has created a remote link " + "|" + logEntry.getResourcePath() + "|" + logEntry.getResourcePath() + "|" + " to the instance " + actionData[0] + " at path " + actionData[1] + "  " + CommonUtil.formatDate(logEntry.getDate()) + ".";
                            list.add(entry);
                        }
                    } else {
                        String entry = Boolean.toString(resourceExists(registry, logEntry)) + "|" + logEntry.getUserName() + "|" + logEntry.getUserName() + "|" + " has created a remote link " + "|" + logEntry.getResourcePath() + "|" + logEntry.getResourcePath() + "|" + " to the Location " + logEntry.getActionData() + "  " + CommonUtil.formatDate(logEntry.getDate()) + ".";
                        list.add(entry);
                    }

                } else if (logEntry.getAction() == LogEntry.REMOVE_LINK) {
                    String entry = Boolean.toString(resourceExists(registry, logEntry)) + "|" + logEntry.getUserName() + "|" + logEntry.getUserName() + "|" + " has removed link " + "|" + logEntry.getResourcePath() + "|" + logEntry.getResourcePath() + "|" + "  " + CommonUtil.formatDate(logEntry.getDate()) + ".";
                    list.add(entry);
                }
            }

        }
        String[] activity = new String[list.size()];
        for(int i=0;i<list.size();i++){
            activity[i] = list.get(i);
        }
        return activity;
    }
    /**
     * Converts given strings to Dates
     *
     * @param dateString Allowed format mm/dd/yyyy
     * @return Date corresponding to the given string date
     */
    private static Date computeDate(String dateString) throws RegistryException {

        if (dateString == null || dateString.length() == 0) {
            return null;
        }

        DateFormat formatter = DateFormat.getDateInstance(DateFormat.SHORT);
        try {
            return formatter.parse(dateString);
        } catch (ParseException e) {
            String msg = "Date format is invalid: " + dateString;
            throw new RegistryException(msg, e);
        }
    }
}
