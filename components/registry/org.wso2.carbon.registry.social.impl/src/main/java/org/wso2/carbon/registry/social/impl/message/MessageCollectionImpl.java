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
package org.wso2.carbon.registry.social.impl.message;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import org.wso2.carbon.registry.social.api.message.MessageCollection;
import org.wso2.carbon.registry.social.api.people.userprofile.model.Url;

import java.util.Date;
import java.util.List;

/**
 * An implementation of the {@link org.wso2.carbon.registry.social.api.message.MessageCollection} interface
 */
@SuppressWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class MessageCollectionImpl implements MessageCollection {

    private String id;
    private String title;
    private Integer total;
    private Integer unread;
    private Date updated;
    private List<Url> urls;

    public MessageCollectionImpl() {
    }

    /**
     * Gets the unique ID of the message collection.
     *
     * @return the ID of the message
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique ID of the message collection.
     *
     * @param id the ID value to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the title of the message collection.
     *
     * @return the title of the message
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the message message collection.
     *
     * @param newTitle the title of the message
     */
    public void setTitle(String newTitle) {
        this.title = newTitle;
    }

    /**
     * Gets the total number of messages for this collection.
     *
     * @return the total number of messages
     */
    public Integer getTotal() {
        return total;
    }

    /**
     * Sets the total number of messages for this collection
     *
     * @param total the total number of messages
     */
    public void setTotal(Integer total) {
        this.total = total;
    }

    /**
     * Gets the total number of unread messages.
     *
     * @return the total number of unread messages
     */
    public Integer getUnread() {
        return unread;
    }

    /**
     * Sets the total number of unread messages.
     *
     * @param unread the number of unread messages
     */
    public void setUnread(Integer unread) {
        this.unread = unread;
    }

    /**
     * Returns the last time this message collection was modified.
     *
     * @return the updated time
     */
    public Date getUpdated() {
        return updated;
    }

    /**
     * Sets the updated time for this message collection.
     *
     * @param updated
     */
    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    /**
     * Get the URLs related to the message collection.
     *
     * @return the URLs related to the message collection
     */
    public List<Url> getUrls() {
        return urls;
    }

    /**
     * Set the URLs related to the message collection
     *
     * @param urls the URLs related to the message collection
     */
    public void setUrls(List<Url> urls) {
        this.urls = urls;
    }
}
