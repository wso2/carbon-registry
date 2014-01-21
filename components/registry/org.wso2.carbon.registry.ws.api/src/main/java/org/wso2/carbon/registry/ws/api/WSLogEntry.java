/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.registry.ws.api;


/**
 * The WSLogEntry class is a web service compatible representation of
 * org.wso2.carbon.registry.core.LogEntry class. The above mentioned class cannot be directly
 * used as a argument/return type of a web service.
 */
public class WSLogEntry {
    /**
     * Path of the resource on which the action is performed.
     */
    private String resourcePath;

    /**
     * User who has performed the action.
     */
    private String userName;

    /**
     * Date and time at which the action is performed.
     */
    private long date;

    /**
     * Name of the actions. e.g. put, tag, comment
     */
    private int action;

    /**
     * Additional data to describe the actions. This depends on the action. e.g. comment text of the
     * comment action, tag name of the tag action.
     */
    private String actionData;

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public String getActionData() {
        return actionData;
    }

    public void setActionData(String actionData) {
        this.actionData = actionData;
    }
}
