/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.social.impl.activityStream;


import org.wso2.carbon.registry.social.api.activityStream.ActivityObject;
import org.wso2.carbon.registry.social.api.activityStream.MediaLink;

import java.util.List;

public class ActivityObjectImpl implements ActivityObject {

    private List<ActivityObject> attachments;
    private ActivityObject author;
    private String content;
    private String displayName;
    private List<String> downstreamDuplicates;
    private String id;
    private MediaLink image;
    private String objectType;
    private String published;
    private String summary;
    private String updated;
    private List<String> upstreamDuplicates;
    private String url;


    /**
     * Constructs an empty ActivityObject.
     */
    public ActivityObjectImpl() {
    }


    public List<ActivityObject> getAttachments() {
        return attachments;
    }


    public void setAttachments(List<ActivityObject> attachments) {
        this.attachments = attachments;

    }


    public ActivityObject getAuthor() {
        return author;
    }


    public void setAuthor(ActivityObject author) {
        this.author = author;

    }


    public String getContent() {
        return content;
    }


    public void setContent(String content) {
        this.content = content;
    }


    public String getDisplayName() {
        return displayName;
    }


    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**  */
    public List<String> getDownstreamDuplicates() {
        return downstreamDuplicates;
    }


    public void setDownstreamDuplicates(List<String> downstreamDuplicates) {
        this.downstreamDuplicates = downstreamDuplicates;
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public MediaLink getImage() {
        return image;
    }


    public void setImage(MediaLink image) {
        this.image = image;
    }


    public String getObjectType() {
        return objectType;
    }


    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }


    public String getPublished() {
        return published;
    }


    public void setPublished(String published) {
        this.published = published;
    }


    public String getSummary() {
        return summary;
    }


    public void setSummary(String summary) {
        this.summary = summary;
    }


    public String getUpdated() {
        return updated;
    }


    public void setUpdated(String updated) {
        this.updated = updated;
    }


    public List<String> getUpstreamDuplicates() {
        return upstreamDuplicates;
    }


    public void setUpstreamDuplicates(List<String> upstreamDuplicates) {
        this.upstreamDuplicates = upstreamDuplicates;
    }


    public String getUrl() {
        return url;
    }


    public void setUrl(String url) {
        this.url = url;
    }


}
