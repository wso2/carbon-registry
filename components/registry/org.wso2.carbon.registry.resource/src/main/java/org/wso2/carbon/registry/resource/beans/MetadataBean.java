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

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import org.wso2.carbon.registry.common.WebResourcePath;

@SuppressWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class MetadataBean {

   private boolean collection;

    private String path;

    private String pathWithVersion;

    private WebResourcePath[] navigatablePaths;

    private boolean versionView;

    private String activeResourcePath;

    private String formattedCreatedOn;

    private String author;

    private String formattedLastModified;

    private String lastUpdater;

    private String mediaType;

    private String permalink;

    private String serverBaseURL;

    private String description;

    private String contentPath;

    private boolean putAllowed;

    private String resourceVersion;

    private String isWriteLocked;

    private String isDeleteLocked;

    public String getWriteLocked() {
        return isWriteLocked;
    }

    public void setWriteLocked(String writeLocked) {
        isWriteLocked = writeLocked;
    }

    public String getDeleteLocked() {
        return isDeleteLocked;
    }

    public void setDeleteLocked(String deleteLocked) {
        isDeleteLocked = deleteLocked;
    }
    public String getResourceVersion() {
        return resourceVersion;
    }

    public void setResourceVersion(String resourceVersion) {
        this.resourceVersion = resourceVersion;
    }

    public boolean isCollection() {
        return collection;
    }

    public void setCollection(boolean collection) {
        this.collection = collection;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPathWithVersion() {
        return pathWithVersion;
    }

    public void setPathWithVersion(String pathWithVersion) {
        this.pathWithVersion = pathWithVersion;
    }

    public WebResourcePath[] getNavigatablePaths() {
        return navigatablePaths;
    }

    public void setNavigatablePaths(WebResourcePath[] navigatablePaths) {
        this.navigatablePaths = navigatablePaths;
    }

    public boolean isVersionView() {
        return versionView;
    }

    public void setVersionView(boolean versionView) {
        this.versionView = versionView;
    }

    public String getActiveResourcePath() {
        return activeResourcePath;
    }

    public void setActiveResourcePath(String activeResourcePath) {
        this.activeResourcePath = activeResourcePath;
    }

    public String getFormattedCreatedOn() {
        return formattedCreatedOn;
    }

    public void setFormattedCreatedOn(String formattedCreatedOn) {
        this.formattedCreatedOn = formattedCreatedOn;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getFormattedLastModified() {
        return formattedLastModified;
    }

    public void setFormattedLastModified(String formattedLastModified) {
        this.formattedLastModified = formattedLastModified;
    }

    public String getLastUpdater() {
        return lastUpdater;
    }

    public void setLastUpdater(String lastUpdater) {
        this.lastUpdater = lastUpdater;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getPermalink() {
        return permalink;
    }

    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    public String getServerBaseURL() {
        return serverBaseURL;
    }

    public void setServerBaseURL(String serverBaseURL) {
        this.serverBaseURL = serverBaseURL;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContentPath() {
        return contentPath;
    }

    public void setContentPath(String contentPath) {
        this.contentPath = contentPath;
    }

    public boolean isPutAllowed() {
        return putAllowed;
    }

    public void setPutAllowed(boolean putAllowed) {
        this.putAllowed = putAllowed;
    }
}
