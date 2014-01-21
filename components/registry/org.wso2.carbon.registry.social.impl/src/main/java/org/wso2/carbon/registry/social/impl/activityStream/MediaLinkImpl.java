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

import org.wso2.carbon.registry.social.api.activityStream.MediaLink;

public class MediaLinkImpl implements MediaLink {

    private Integer duration;
    private Integer height;
    private String url;
    private Integer width;

    /**
     * Create a new MediaLink
     */
    public MediaLinkImpl() {
    }

    /**
     * {@inheritDoc}
     */
    public Integer getDuration() {
        return duration;
    }


    public void setDuration(Integer duration) {
        this.duration = duration;
    }


    public Integer getHeight() {
        return height;
    }


    public void setHeight(Integer height) {
        this.height = height;
    }


    public String getUrl() {
        if (url == null) {
            return null;
        }
        return url;
    }


    public void setUrl(String url) {
        this.url = url;
    }


    public Integer getWidth() {
        return width;
    }


    public void setWidth(Integer width) {
        this.width = width;
    }


}
