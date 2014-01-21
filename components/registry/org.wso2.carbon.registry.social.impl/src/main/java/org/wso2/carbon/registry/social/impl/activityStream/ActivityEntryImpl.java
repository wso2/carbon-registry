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


import org.wso2.carbon.registry.social.api.activityStream.ActivityEntry;
import org.wso2.carbon.registry.social.api.activityStream.ActivityObject;
import org.wso2.carbon.registry.social.api.activityStream.MediaLink;

public class ActivityEntryImpl implements ActivityEntry {

    private ActivityObject actor;
    private String content;
    private ActivityObject generator;
    private MediaLink icon;
    private String id;
    private ActivityObject object;
    private String published;
    private ActivityObject provider;
    private ActivityObject target;
    private String title;
    private String updated;
    private String url;
    private String verb;


    /**
     * Create a new empty ActivityEntry
     */
    public ActivityEntryImpl() {
    }

    public ActivityObject getActor() {
        return actor;
    }


    public void setActor(ActivityObject actor) {
        this.actor = actor;
    }


    public String getContent() {
        return content;
    }


    public void setContent(String content) {
        this.content = content;
    }


    public ActivityObject getGenerator() {
        return generator;
    }


    public void setGenerator(ActivityObject generator) {
        this.generator = generator;
    }


    public MediaLink getIcon() {
        return icon;
    }


    public void setIcon(MediaLink icon) {
        this.icon = icon;
    }

    public String getUrl() {
        return url;
    }


    public void setUrl(String url) {
        this.url = url;
    }

    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public ActivityObject getObject() {
        return object;
    }


    public void setObject(ActivityObject object) {
        this.object = object;
    }


    public String getPublished() {
        return published;
    }


    public void setPublished(String published) {
        this.published = published;

    }


    public ActivityObject getProvider() {
        return provider;
    }


    public void setProvider(ActivityObject provider) {
        this.provider = provider;

    }


    public ActivityObject getTarget() {
        return target;
    }


    public void setTarget(ActivityObject target) {
        this.target = target;

    }


    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;

    }


    public String getUpdated() {
        return updated;
    }


    public void setUpdated(String updated) {
        this.updated = updated;

    }


    public String getVerb() {
        if (verb == null) {
            verb = "post";
        }
        return verb;
    }


    public void setVerb(String verb) {
        this.verb = verb;

    }


}
