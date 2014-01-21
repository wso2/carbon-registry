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

public class ContentBean {

    private String mediaType;

    private boolean collection;

    private boolean putAllowed;

    private boolean versionView;

    private boolean loggedIn;

    private String pathWithVersion;

    private String contentPath;

    private String realPath;

    private String absent ="false";

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public boolean isCollection() {
        return collection;
    }

    public void setCollection(boolean collection) {
        this.collection = collection;
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

    public String getContentPath() {
        return contentPath;
    }

    public void setContentPath(String contentPath) {
        this.contentPath = contentPath;
    }

    public String getRealPath() {
        return realPath;
    }

    public void setRealPath(String realPath) {
        this.realPath = realPath;
    }

    public String getAbsent() {
        return absent;
    }

    public void setAbsent(String absent) {
        if(absent != null){
            this.absent = absent;
        }
    }
}
