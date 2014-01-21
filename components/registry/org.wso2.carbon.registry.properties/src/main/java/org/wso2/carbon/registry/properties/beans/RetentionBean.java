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
package org.wso2.carbon.registry.properties.beans;

/**
 * This bean encapsulated resource retention details
 */
public class RetentionBean {

    private String fromDate;
    private String toDate;
    private boolean writeLocked;
    private boolean deleteLocked;
    private String userName;
    private boolean readOnly;

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public boolean getWriteLocked() {
        return writeLocked;
    }

    public void setWriteLocked(boolean writeLocked) {
        this.writeLocked = writeLocked;
    }

    public boolean getDeleteLocked() {
        return deleteLocked;
    }

    public void setDeleteLocked(boolean deleteLocked) {
        this.deleteLocked = deleteLocked;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
}
