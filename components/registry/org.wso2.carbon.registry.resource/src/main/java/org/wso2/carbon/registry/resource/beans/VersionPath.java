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

package org.wso2.carbon.registry.resource.beans;

import java.util.Date;
import java.util.Calendar;

public class VersionPath {

    private String completeVersionPath;

    private String activeResourcePath;

    private long versionNumber;

    private String updater;

    private Calendar updatedOn;

    public String getCompleteVersionPath() {
        return completeVersionPath;
    }

    public void setCompleteVersionPath(String completeVersionPath) {
        this.completeVersionPath = completeVersionPath;
    }

    public String getActiveResourcePath() {
        return activeResourcePath;
    }

    public void setActiveResourcePath(String activeResourcePath) {
        this.activeResourcePath = activeResourcePath;
    }

    public long getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(long versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getUpdater() {
        return updater;
    }

    public void setUpdater(String updater) {
        this.updater = updater;
    }

    public Calendar getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(Calendar updatedOn) {
        this.updatedOn = updatedOn;
    }
}
