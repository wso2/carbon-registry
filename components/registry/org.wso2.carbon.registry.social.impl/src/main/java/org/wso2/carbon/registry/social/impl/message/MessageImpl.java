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
import org.wso2.carbon.registry.social.api.message.Message;
import org.wso2.carbon.registry.social.api.people.userprofile.model.Url;

import java.util.Date;
import java.util.List;

/**
 * An implementation of the {@link org.wso2.carbon.registry.social.api.message.Message} interface
 */
@SuppressWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public final class MessageImpl implements Message {

    private String appUrl;
    private String body;
    private String bodyId;
    private List<String> collectionIds;
    private String id;
    private String inReplyTo;
    private List<String> recipients;
    private List<String> replies;
    private String senderId;
    private Status status;
    private Date timeSent;
    private String title;
    private String titleId;
    private Type type;
    private Date updated;
    private List<Url> urls;

    /**
     * Default constructor
     */
    public MessageImpl() {
    }

    /**
     * Overriden Constructor
     */
    public MessageImpl(String initBody, String initTitle, Type initType) {
        this.body = initBody;
        this.title = initTitle;
        this.type = initType;
    }

    /**
     * Gets the App URL for a message.
     * <p/>
     * Used if an App generated the message.
     *
     * @return the Application URL
     */
    public String getAppUrl() {
        return appUrl;
    }

    /**
     * Set the App URL for a message.
     *
     * @param appUrl the URL to set.
     */
    public void setAppUrl(String appUrl) {
        this.appUrl = appUrl;
    }

    /**
     * Gets the main text of the message.
     *
     * @return the main text of the message
     */
    public String getBody() {
        return this.body;
    }

    /**
     * Sets the main text of the message.
     * HTML attributes are allowed and are sanitized by the container
     *
     * @param newBody the main text of the message
     */
    public void setBody(String newBody) {
        this.body = newBody;
    }

    /**
     * Gets the body id.
     * Used for message submission
     *
     * @return the body ID
     */
    public String getBodyId() {
        return bodyId;
    }

    /**
     * Sets the body id.
     *
     * @param bodyId A valid body id defined in the gadget XML.
     */
    public void setBodyId(String bodyId) {
        this.bodyId = bodyId;
    }

    /**
     * Gets the collection Ids for this message.
     */
    public List<String> getCollectionIds() {
        return collectionIds;
    }

    /**
     * Sets the collection Ids for this message.
     */
    public void setCollectionIds(List<String> collectionIds) {
        this.collectionIds = collectionIds;
    }

    /**
     * Gets the unique ID of the message
     *
     * @return the ID of the message
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique ID of the message.
     *
     * @param id the ID value to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the parent message ID.
     *
     * @return message id
     */
    public String getInReplyTo() {
        return inReplyTo;
    }

    /**
     * Sets the parent message ID
     *
     * @param parentId the parentId to set
     */
    public void setInReplyTo(String parentId) {
        this.inReplyTo = parentId;
    }

    /**
     * Gets the recipient list of the message.
     *
     * @return the recipients of the message
     */
    public List<String> getRecipients() {
        return this.recipients;
    }

    /**
     * Sets recipients
     *
     * @param recipients
     */
    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

    /**
     * Gets the list of Replies to this message
     *
     * @return
     */
    public List<String> getReplies() {
        return replies;
    }

    public void setReplies(List<String> replies) {
        this.replies = replies;
    }

    /**
     * Gets the sender ID value.
     *
     * @return sender person id
     */
    public String getSenderId() {
        return senderId;
    }

    /**
     * sets the sender ID.
     *
     * @param senderId the sender id to set
     */
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    /**
     * Gets the Status of the message.
     *
     * @return the status of the message
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Sets the Status of the message.
     *
     * @param status the status to set
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Gets the time the message was sent.
     *
     * @return the message sent time
     */
    public Date getTimeSent() {
        return timeSent;
    }

    /**
     * Sets the time the message was sent.
     *
     * @param timeSent the time the message was sent
     */
    public void setTimeSent(Date timeSent) {
        this.timeSent = timeSent;
    }

    /**
     * Gets the title of the message.
     *
     * @return the title of the message
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Sets the title of the message.
     * HTML attributes are allowed and are sanitized by the container.
     *
     * @param newTitle the title of the message
     */
    public void setTitle(String newTitle) {
        this.title = newTitle;
    }

    /**
     * Gets the title ID for this message.
     * Used for message submission.
     *
     * @return the title Id
     */
    public String getTitleId() {
        return titleId;
    }

    /**
     * Sets the title ID for this message.
     * Used for message submission.
     *
     * @param titleId the title ID as defined in the gadget XML
     */
    public void setTitleId(String titleId) {
        this.titleId = titleId;
    }

    /**
     * Gets the type of the message, as specified by opensocial.Message.Type.
     *
     * @return the type of message (enum Message.Type)
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets the type of the message, as specified by opensocial.Message.Type.
     *
     * @param newType the type of message (enum Message.Type)
     */
    public void setType(Type newType) {
        this.type = newType;
    }

    /**
     * Gets the updated timestamp for the message.
     *
     * @return the updated date of the message
     */
    public Date getUpdated() {
        return this.updated;
    }

    /**
     * Sets the updated timestamp for the message.
     */

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    /**
     * Get the URLs related to the message
     *
     * @return the URLs related to the person, their webpages, or feeds
     */
    public List<Url> getUrls() {
        return this.urls;
    }

    /**
     * Set the URLs related to the message
     *
     * @param urls the URLs related to the person, their webpages, or feeds
     */
    public void setUrls(List<Url> urls) {
        this.urls = urls;
    }


}
