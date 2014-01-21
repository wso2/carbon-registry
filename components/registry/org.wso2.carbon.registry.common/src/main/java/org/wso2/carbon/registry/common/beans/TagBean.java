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

package org.wso2.carbon.registry.common.beans;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import org.wso2.carbon.registry.common.beans.utils.Tag;

@SuppressWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class TagBean {

    private Tag[] tags;

    private String errorMessage;

    private boolean putAllowed;

    private boolean versionView;

    private boolean loggedIn;

    private String pathWithVersion;

    public Tag [] getTags() {
        return tags;
    }

    public void setTags(Tag [] tags) {
        this.tags = tags;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

     public boolean isPutAllowed() {
     return putAllowed;
    }

    public void setPutAllowed(boolean putAllowed) {
        this.putAllowed = putAllowed;
    }

    public boolean isVersionView() {
        return versionView;
    }

    public void setVersionView(boolean versionView) {
        this.versionView = versionView;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public String getPathWithVersion() {
        return pathWithVersion;
    }

    public void setPathWithVersion(String pathWithVersion) {
        this.pathWithVersion = pathWithVersion;
    }
}
