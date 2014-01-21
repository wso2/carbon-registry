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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.social.api.SocialMessageException;
import org.wso2.carbon.registry.social.api.message.Message;
import org.wso2.carbon.registry.social.api.message.MessageCollection;
import org.wso2.carbon.registry.social.api.message.MessageManager;
import org.wso2.carbon.registry.social.api.people.userprofile.model.Url;
import org.wso2.carbon.registry.social.api.utils.FilterOptions;
import org.wso2.carbon.registry.social.impl.SocialImplConstants;
import org.wso2.carbon.registry.social.impl.internal.SocialDSComponent;
import org.wso2.carbon.registry.social.impl.people.userprofile.model.impl.UrlImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An implementation of the {@link org.wso2.carbon.registry.social.api.message.MessageManager} interface
 * <p>
 * This implementation uses the {@link org.wso2.carbon.registry.core.Registry} to store messages
 * </p>
 * <p>
 * The Message and MessageCollections  are stored as a {@link org.wso2.carbon.registry.core.Registry}  {@link org.wso2.carbon.registry.core.Resource}
 * The properties are stored as  resource properties
 * </p>
 * <p>
 * <p/>
 * Each message is grouped  under a message collection
 * </p>
 * <p>
 * Resource path : /users/{userId}/messages/{messageCollectionId}/{messageId}/<key><value>
 * </p>
 */

public class MessageManagerImpl implements MessageManager {
    private Registry registry = null;
    private static Log log = LogFactory.getLog(MessageManagerImpl.class);

    public void setRegistry(Registry reg) {
        this.registry = reg;
    }

    public Registry getRegistry() throws RegistryException {
        if (this.registry != null) {
            return this.registry;
        } else {
            return SocialDSComponent.getRegistry();
        }
    }

    /**
     * Returns an array of message collections corresponding to the given user  details
     *
     * @param userId  The userId to fetch for
     * @param fields  The fields to fetch for the message collections
     * @param options Pagination details
     * @return An array of MessageCollection
     * @throws SocialMessageException /{userId}/messages/{messageCollectionId}/
     */


    public MessageCollection[] getMessageCollections(String userId, Set<String> fields,
                                                     FilterOptions options)
            throws SocialMessageException {
        MessageCollection[] resultArray = new MessageCollectionImpl[0];
        String messageCollectionPath = SocialImplConstants.USER_REGISTRY_ROOT + userId +
                                       SocialImplConstants.MESSAGES_PATH;
        try {
            registry = getRegistry();
            Collection messageCollections;
            if (registry.resourceExists(messageCollectionPath)) {

                // Using the FilterOptions
                if (options != null && options.getMax() > 0) {
                    messageCollections = registry.get(messageCollectionPath,
                                                      options.getFirst(), options.getMax());
                } else {
                    messageCollections = registry.get(messageCollectionPath, 0,
                                                      SocialImplConstants.DEFAULT_RETURN_ARRAY_SIZE);
                }
                String[] childResourcePath = messageCollections.getChildren();
                List<MessageCollection> resultList = new ArrayList<MessageCollection>();
                for (String path : childResourcePath) {       // Creates MessageCollection objects for each child resource
                    if (registry.resourceExists(path)) {
                        resultList.add(getPropertiesAddedMessageCollectionObj(
                                registry.get(path), fields));
                    }

                }
                if (resultList.size() > 0) {
                    resultArray = resultList.toArray(resultArray);
                } else {
                    resultArray = null;
                }
            } else {
                throw new SocialMessageException("No MessageCollection exists for user " + userId);

            }
        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new SocialMessageException(
                    "Error while retrieving message collections for user " + userId, e);
        }

        return resultArray;
    }

    /**
     * Creates a MessageCollection of the the user with given id and attributes
     *
     * @param userId              The userId to which the collection belongs to
     * @param msgCollection       The MessageCollection object to retrieve the attrobutes
     * @param messageCollectionId The id of the message collection
     * @throws SocialMessageException
     */
    public void createMessageCollection(String userId, MessageCollection msgCollection,
                                        String messageCollectionId)
            throws SocialMessageException {
        if (messageCollectionId == null || userId == null || msgCollection == null) {
            throw new SocialMessageException(
                    "Invalid parameters to create a message collection");
        }
        try {
            saveMessageCollection(userId, messageCollectionId, msgCollection, false);
        }
        catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new SocialMessageException("Error while creating message collection with id " +
                                             messageCollectionId + " for user " + userId, e);
        }
    }

    /**
     * Deletes a message collection for the given arguments
     *
     * @param userId              The userId to create the message collection for
     * @param messageCollectionId The message Collection ID to be removed
     * @throws SocialMessageException
     */
    public void deleteMessageCollection(String userId, String messageCollectionId)
            throws SocialMessageException {
        try {
            registry = getRegistry();
            String msgCollectionPath = SocialImplConstants.USER_REGISTRY_ROOT + userId +
                                       SocialImplConstants.MESSAGES_PATH +
                                       SocialImplConstants.SEPARATOR + messageCollectionId;
            if (registry.resourceExists(msgCollectionPath)) {
                registry.delete(msgCollectionPath);
            } else {
                throw new SocialMessageException("Message Collection doesn't exist with id " +
                                                 messageCollectionId);
            }
        }
        catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new SocialMessageException("Error while deleting message collection with id " +
                                             messageCollectionId + " for user " + userId, e);
        }
    }

    /**
     * Modifies/Updates a message collection for the given arguments
     *
     * @param userId              The userId to modify the message collection for
     * @param msgCollection       Data for the message collection to be modified
     * @param messageCollectionId The message Collection ID to modify
     * @throws SocialMessageException
     */
    public void modifyMessageCollection(String userId, MessageCollection msgCollection,
                                        String messageCollectionId) throws SocialMessageException {
        try {
            saveMessageCollection(userId, messageCollectionId, msgCollection, true);
        }
        catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new SocialMessageException("Error while modifying message collection with id " +
                                             messageCollectionId + " for user " + userId, e);
        }
    }

    /**
     * Returns an array of messages that correspond to the passed in data
     *
     * @param userId          The userId of the person to fetch message for
     * @param msgCollectionId The message Collection ID to fetch from, default @all
     * @param fields          The fields to fetch for the messages
     * @param msgIds          An explicit set of message ids to fetch
     * @param options         Options to control the fetch
     * @throws SocialMessageException
     */
    public Message[] getMessages(String userId, String msgCollectionId, Set<String> fields,
                                 List<String> msgIds, FilterOptions options)
            throws SocialMessageException {
        if (userId == null || msgCollectionId == null || msgIds == null) {
            throw new SocialMessageException("Invalid input parameters to retrieve message");
        }
        Message[] messages;
        List<Message> messagesList = new ArrayList<Message>();

        try {
            registry = getRegistry();
            for (String id : msgIds) {
                String messageResourcePath = SocialImplConstants.USER_REGISTRY_ROOT + userId +
                                             SocialImplConstants.MESSAGES_PATH +
                                             SocialImplConstants.SEPARATOR + msgCollectionId +
                                             SocialImplConstants.SEPARATOR + id;
                Resource messageResource;
                if (registry.resourceExists(messageResourcePath)) {
                    messageResource = registry.get(messageResourcePath);
                    //TODO: FilterOptions
                    messagesList.add(getPropertiesAddedMessageOjb(messageResource, fields));

                } else {
                    log.error("Message with specified messageId " + id +
                              " is not found");

                }
            }
            if (messagesList.size() <= 0) {
                // no messages found
                log.error("No messages found for the user " + userId);
                return null;
            }
            messages = new Message[messagesList.size()];
            messages = messagesList.toArray(messages);
        }
        catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new SocialMessageException(
                    "Error while retrieving messages for user " + userId, e);
        }

        return messages;
    }

    /**
     * Posts a message to the user's specified message collection, to be sent to the set of recipients
     * specified in the message
     *
     * @param userId          The user posting the message
     * @param msgCollectionId The message collection Id to post to, default @outbox
     * @param message         The message object
     * @throws SocialMessageException
     */
    public void createMessage(String userId, String msgCollectionId, Message message)
            throws SocialMessageException {
        if (msgCollectionId == null || userId == null || message == null) {
            throw new SocialMessageException(
                    "Invalid parameters to create a message");
        }
        try {
            saveMessage(userId, null, msgCollectionId, message.getId(), message, false);
        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new SocialMessageException(
                    "Error while creating message from message collection with id " +
                    msgCollectionId + " for user " + userId, e);
        }
    }

    /**
     * Posts a message to the user's specified message collection, to be sent to the set of recipients
     * specified in the message
     *
     * @param userId          The user posting the message
     * @param appId           The app id
     * @param msgCollectionId The message collection Id to post to, default @outbox
     * @param message         The message to post
     * @throws SocialMessageException
     */
    public void createMessage(String userId, String appId, String msgCollectionId,
                              Message message)
            throws SocialMessageException {
        if (msgCollectionId == null || userId == null || message == null) {
            throw new SocialMessageException(
                    "Invalid parameters to create a message");
        }
        try {
            saveMessage(userId, appId, msgCollectionId, message.getId(), message, false);
        }
        catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new SocialMessageException(
                    "Error while creating message from message collection with id " +
                    msgCollectionId + " for user " + userId, e);
        }
    }

    /**
     * Deletes a set of messages for a given user/message collection
     *
     * @param userId          The userId of the person whose messages to be deleted
     * @param msgCollectionId The Message Collection ID to delete from, default @all
     * @param messageIds      List of messageIds to delete
     * @throws SocialMessageException
     */
    public void deleteMessages(String userId, String msgCollectionId, List<String> messageIds)
            throws SocialMessageException {
        String messagePath = SocialImplConstants.USER_REGISTRY_ROOT + userId +
                             SocialImplConstants.MESSAGES_PATH +
                             SocialImplConstants.SEPARATOR + msgCollectionId;
        try {
            registry = getRegistry();
            String fullMessagePath;
            for (String id : messageIds) {
                fullMessagePath = messagePath + SocialImplConstants.SEPARATOR + id;
                if (registry.resourceExists(fullMessagePath)) {
                    registry.delete(fullMessagePath);
                } else {
                    throw new SocialMessageException(
                            "No message found with specified messageId " + id);
                }
            }
        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new SocialMessageException(
                    "Error while deleting message from message collection with id "
                    + msgCollectionId + " for user " + userId, e);
        }
    }

    /**
     * Modifies/Updates a specific message with new data
     *
     * @param userId    The userId of the person whose messaged to be modified
     * @param msgCollId The Message Collection ID to modify from, default @all
     * @param messageId The messageId to modify
     * @param message   The message details to modify
     * @throws SocialMessageException
     */
    public void modifyMessage(String userId, String msgCollId, String messageId, Message message)
            throws SocialMessageException {
        try {
            saveMessage(userId, null, msgCollId, message.getId(), message, true);
        }
        catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new SocialMessageException(
                    "Error while modifying message with id " + messageId + " for user " + userId, e);
        }
    }

    /**
     * Saves MessageCollection object as a Registry resource
     *
     * @param userId            The id of the user to whom the messageCollection belongs to
     * @param collectionId      The id of the messageCollection
     * @param messageCollection The MessageCollection object to save
     * @param isUpdate          If true, the messageCollection is updated with new values
     *                          If false, the messageCollection is added with the values
     * @throws RegistryException
     */
    private void saveMessageCollection(String userId, String collectionId,
                                       MessageCollection messageCollection, boolean isUpdate)
            throws RegistryException {
        String msgCollectionResourcePath = SocialImplConstants.USER_REGISTRY_ROOT + userId +
                                           SocialImplConstants.MESSAGES_PATH +
                                           SocialImplConstants.SEPARATOR + collectionId;
        Resource msgCollectionResource;

        registry = getRegistry();
        if (registry.resourceExists(msgCollectionResourcePath)) {
            msgCollectionResource = registry.get(msgCollectionResourcePath);
        } else {
            msgCollectionResource = registry.newCollection();
        }
        msgCollectionResource = getPropertyAddedMessageCollectionResource(messageCollection,
                                                                          msgCollectionResource,
                                                                          isUpdate);
        registry.put(msgCollectionResourcePath, msgCollectionResource);

    }

    /**
     * Adds the MessageCollection object attributes to the MessageCollection registry resource as
     * resource properties and returns the properties added registry resource
     *
     * @param msgCollection         The messageCollection object to retrieve attributes
     * @param msgCollectionResource The Registry resource to represent the MessageCollection object
     * @param isUpdate              If true, the resource property values are updated with new values
     *                              If false, the values are added as resource properties
     * @return The properties added Registry Resource
     */

    private Resource getPropertyAddedMessageCollectionResource(MessageCollection msgCollection,
                                                               Resource msgCollectionResource,
                                                               boolean isUpdate) {
        String value;
        String oldValue;
        if ((value = msgCollection.getId()) != null) {
            if (isUpdate) {             // need to edit the property value
                oldValue = msgCollectionResource.getProperty(SocialImplConstants.MSG_COLLECTION_ID);
                msgCollectionResource.editPropertyValue(SocialImplConstants.MSG_COLLECTION_ID,
                                                        oldValue, value);
            } else {                    // need to add the property
                msgCollectionResource.setProperty(SocialImplConstants.MSG_COLLECTION_ID, value);
            }
        }
        if ((value = msgCollection.getTitle()) != null) {
            if (isUpdate) {
                oldValue = msgCollectionResource.getProperty(
                        SocialImplConstants.MSG_COLLECTION_TITLE);
                msgCollectionResource.editPropertyValue(SocialImplConstants.MSG_COLLECTION_TITLE,
                                                        oldValue, value);
            } else {
                msgCollectionResource.setProperty(SocialImplConstants.MSG_COLLECTION_TITLE, value);
            }
        }
        if (msgCollection.getUpdated() != null) {
            if (isUpdate) {
                oldValue = msgCollectionResource.getProperty(
                        SocialImplConstants.MSG_COLLECTION_UPDATED_DATE);
                msgCollectionResource.editPropertyValue(
                        SocialImplConstants.MSG_COLLECTION_UPDATED_DATE, oldValue,
                        msgCollection.getUpdated().getTime() + "");
            } else {
                msgCollectionResource.setProperty(SocialImplConstants.MSG_COLLECTION_UPDATED_DATE,
                                                  msgCollection.getUpdated().getTime() + "");
            }

        }
        if (msgCollection.getTotal() != null) {
            if (isUpdate) {
                oldValue = msgCollectionResource.getProperty(
                        SocialImplConstants.MSG_COLLECTION_TOTAL_MESSAGES);
                msgCollectionResource.editPropertyValue(
                        SocialImplConstants.MSG_COLLECTION_TOTAL_MESSAGES, oldValue,
                        msgCollection.getTotal().toString());
            } else {
                msgCollectionResource.setProperty(SocialImplConstants.MSG_COLLECTION_TOTAL_MESSAGES,
                                                  msgCollection.getTotal().toString());
            }
        }
        if (msgCollection.getUnread() != null) {
            if (isUpdate) {
                oldValue = msgCollectionResource.getProperty(
                        SocialImplConstants.MSG_COLLECTION_UNREAD_MESSAGES);
                msgCollectionResource.editPropertyValue(
                        SocialImplConstants.MSG_COLLECTION_UNREAD_MESSAGES,
                        oldValue, msgCollection.getUnread().toString());
            } else {
                msgCollectionResource.setProperty(
                        SocialImplConstants.MSG_COLLECTION_UNREAD_MESSAGES,
                        msgCollection.getUnread().toString());
            }
        }
        if (msgCollection.getUrls() != null) {
            List<String> newPropertyValues = new ArrayList<String>();
            for (Url url : msgCollection.getUrls()) {
                if (url != null) {
                    if (isUpdate) {
                        // edit property values
                        newPropertyValues.add(url.getLinkText());

                    } else {
                        //add the property values
                        msgCollectionResource.setProperty(SocialImplConstants.MSG_COLLECTION_URLS,
                                                          url.getLinkText());
                    }
                }
            }
            if (isUpdate) {    // if it is an update, set the urls as new property values
                msgCollectionResource.setProperty(SocialImplConstants.MSG_COLLECTION_URLS,
                                                  newPropertyValues);
            }
        }
        return msgCollectionResource;
    }

    /**
     * Returns a MessageCollection type object with the attributes added from the registry resource properties
     *
     * @param collectionResource The registry resource to retrieve properties from
     * @param fields             The attributes to add to the MessageCollection object
     * @return A MessageCollection type object with attributes added
     */

    private MessageCollection getPropertiesAddedMessageCollectionObj(Resource collectionResource,
                                                                     Set<String> fields) {
        MessageCollection collectionObj = new MessageCollectionImpl();
        for (String field : fields) {
            if (SocialImplConstants.MSG_COLLECTION_ID.equalsIgnoreCase(field.trim())) {
                collectionObj.setId(collectionResource.getProperty(
                        SocialImplConstants.MSG_COLLECTION_ID));
            } else if (SocialImplConstants.MSG_COLLECTION_TITLE.equalsIgnoreCase(field.trim())) {
                collectionObj.setTitle(collectionResource.getProperty(
                        SocialImplConstants.MSG_COLLECTION_TITLE));
            } else if (SocialImplConstants.MSG_COLLECTION_TOTAL_MESSAGES.equalsIgnoreCase(
                    field.trim())) {
                collectionObj.setTotal(Integer.valueOf(collectionResource.getProperty(
                        SocialImplConstants.MSG_COLLECTION_TOTAL_MESSAGES)));
            } else if (SocialImplConstants.MSG_COLLECTION_UNREAD_MESSAGES.equalsIgnoreCase(
                    field.trim())) {
                collectionObj.setUnread(Integer.valueOf(collectionResource.getProperty(
                        SocialImplConstants.MSG_COLLECTION_UNREAD_MESSAGES)));
            } else if (SocialImplConstants.MSG_COLLECTION_UPDATED_DATE.equalsIgnoreCase(
                    field.trim())) {
                collectionObj.setUpdated(new Date(Long.valueOf(collectionResource.getProperty(
                        SocialImplConstants.MSG_COLLECTION_UPDATED_DATE))));
            } else if (SocialImplConstants.MSG_COLLECTION_URLS.equalsIgnoreCase(field.trim())) {
                List<Url> collectionUrls = new ArrayList<Url>();
                List<String> propertyValues = collectionResource.getPropertyValues(
                        SocialImplConstants.MSG_COLLECTION_URLS);
                for (String urlString : propertyValues) {
                    Url url = new UrlImpl();
                    url.setLinkText(urlString);
                    collectionUrls.add(url);
                }
                collectionObj.setUrls(collectionUrls);

            } else {
                //TODO: Handle non-field values
            }
        }

        return collectionObj;
    }

    /**
     * Saves/updates a message
     *
     * @param userId          The id of the person whom this message belongs to
     * @param appId           The id of the application to which this message belongs to
     * @param msgCollectionId The id of the messageCollection to which this message belongs to
     * @param messageId       The id of this message
     * @param message         The message object to save/update
     * @param isUpdate        True - if required to update the message, else False
     * @throws RegistryException
     */

    private void saveMessage(String userId, String appId, String msgCollectionId, String messageId,
                             Message message, boolean isUpdate) throws RegistryException {
        String messagePath;

        if (appId != null) {
            /*   /users/{userId}/messages/{appId}/{msgCollectionId}/{messageId}  */
            messagePath = SocialImplConstants.USER_REGISTRY_ROOT + userId +
                          SocialImplConstants.MESSAGES_PATH + SocialImplConstants.SEPARATOR +
                          appId + SocialImplConstants.SEPARATOR + msgCollectionId +
                          SocialImplConstants.SEPARATOR + messageId;
        } else {
            /*   /users/{userId}/messages/{msgCollectionId}/{messageId}  */
            messagePath = SocialImplConstants.USER_REGISTRY_ROOT + userId +
                          SocialImplConstants.MESSAGES_PATH + SocialImplConstants.SEPARATOR +
                          msgCollectionId + SocialImplConstants.SEPARATOR + messageId;
        }
        Resource messageResource;

        registry = getRegistry();
        if (registry.resourceExists(messagePath)) {
            messageResource = registry.get(messagePath);
        } else {
            messageResource = registry.newCollection();
        }
        messageResource = getPropertiesAddedMessageResource(message, messageResource, isUpdate);
        registry.put(messagePath, messageResource);


    }

    /**
     * Adds/updates the attributes of the message object as registry resource properties
     *
     * @param message         The Message Object to retrieve message attributes
     * @param messageResource The registry resource to add/update properties
     * @param isUpdate        True- if required to update the properties else False
     * @return The registry resource with the properties added
     */
    private Resource getPropertiesAddedMessageResource(Message message, Resource messageResource,
                                                       boolean isUpdate) {
        Map<String, String> properties = new HashMap<String, String>();
        if (message.getAppUrl() != null) {
            properties.put(SocialImplConstants.MSG_APP_URL, message.getAppUrl());
        }
        if (message.getBody() != null) {
            properties.put(SocialImplConstants.MSG_BODY, message.getBody());
        }
        if (message.getBodyId() != null) {
            properties.put(SocialImplConstants.MSG_BODY_ID, message.getBodyId());
        }
        if (message.getId() != null) {
            properties.put(SocialImplConstants.MSG_ID, message.getId());
        }
        if (message.getInReplyTo() != null) {
            properties.put(SocialImplConstants.MSG_IN_REPLY_TO, message.getInReplyTo());
        }
        if (message.getSenderId() != null) {
            properties.put(SocialImplConstants.MSG_SENDER_ID, message.getSenderId());
        }
        if (message.getStatus() != null) {
            properties.put(SocialImplConstants.MSG_STATUS, message.getStatus().toString());
        }
        if (message.getTimeSent() != null) {
            properties.put(SocialImplConstants.MSG_TIME_SENT, message.getTimeSent().getTime() + "");
        }
        if (message.getTitle() != null) {
            properties.put(SocialImplConstants.MSG_TITLE, message.getTitle());
        }
        if (message.getTitleId() != null) {
            properties.put(SocialImplConstants.MSG_TITLE_ID, message.getTitleId());
        }
        if (message.getType() != null) {
            properties.put(SocialImplConstants.MSG_TYPE, message.getType().toString());
        }
        if (message.getUpdated() != null) {
            properties.put(SocialImplConstants.MSG_UPDATED, message.getUpdated().getTime() + "");
        }

        attachPropertyToResource(messageResource, properties, isUpdate);

        List<String> propertyValues;
        if ((propertyValues = message.getCollectionIds()) != null) {
            messageResource.setProperty(SocialImplConstants.MSG_COLLECTION_ID, propertyValues);
        }
        if ((propertyValues = message.getRecipients()) != null) {
            messageResource.setProperty(SocialImplConstants.MSG_RECIPIENTS, propertyValues);
        }
        if ((propertyValues = message.getReplies()) != null) {
            messageResource.setProperty(SocialImplConstants.MSG_REPLIES, propertyValues);
        }
        List<Url> urls = message.getUrls();
        propertyValues = new ArrayList<String>();
        if (urls != null) {
            for (Url url : urls) {
                propertyValues.add(url.getLinkText());
            }
            messageResource.setProperty(SocialImplConstants.MSG_URLS, propertyValues);
        }


        return messageResource;
    }

    /**
     * Add/ edit a property value for the given Registry resource
     *
     * @param msgResource Registry resource to add/set property
     * @param properties  A Map consisting properties and it's values
     * @param isUpdate    True, if it is an update. False, if it is an addition
     */
    private void attachPropertyToResource(Resource msgResource, Map<String, String> properties,
                                          boolean isUpdate) {

        for (Map.Entry<String, String> e : properties.entrySet()) {
            if (e.getValue() != null) {
                if (isUpdate) {
                    String oldValue = msgResource.getProperty(e.getKey());
                    msgResource.editPropertyValue(e.getKey(), oldValue, e.getValue());
                } else {
                    msgResource.addProperty(e.getKey(), e.getValue());
                }
            }
        }

    }

    /**
     * Returns a Message type object with the attributes added from the registry resource properties
     *
     * @param msgResource The registry resource to retrieve message attributes from
     * @param fields      The attributes of the message to be set
     * @return
     */
    private Message getPropertiesAddedMessageOjb(Resource msgResource, Set<String> fields) {
        Message messageObj = new MessageImpl();
        for (String field : fields) {
            if (SocialImplConstants.MSG_APP_URL.equalsIgnoreCase(field.trim())) {
                messageObj.setAppUrl(msgResource.getProperty(SocialImplConstants.MSG_APP_URL));
            } else if (SocialImplConstants.MSG_BODY.equalsIgnoreCase(field.trim())) {
                messageObj.setBody(msgResource.getProperty(SocialImplConstants.MSG_BODY));
            } else if (SocialImplConstants.MSG_BODY_ID.equalsIgnoreCase(field.trim())) {
                messageObj.setBodyId(msgResource.getProperty(SocialImplConstants.MSG_BODY_ID));
            } else if (SocialImplConstants.MSG_ID.equalsIgnoreCase(field.trim())) {
                messageObj.setId(msgResource.getProperty(SocialImplConstants.MSG_ID));
            } else if (SocialImplConstants.MSG_IN_REPLY_TO.equalsIgnoreCase(field.trim())) {
                messageObj.setInReplyTo(
                        msgResource.getProperty(SocialImplConstants.MSG_IN_REPLY_TO));
            } else if (SocialImplConstants.MSG_SENDER_ID.equalsIgnoreCase(field.trim())) {
                messageObj.setSenderId(msgResource.getProperty(SocialImplConstants.MSG_SENDER_ID));
            } else if (SocialImplConstants.MSG_STATUS.equalsIgnoreCase(field.trim())) {
                messageObj.setStatus(Message.Status.valueOf(
                        msgResource.getProperty(SocialImplConstants.MSG_STATUS)));
            } else if (SocialImplConstants.MSG_TIME_SENT.equalsIgnoreCase(field.trim())) {
                messageObj.setTimeSent(new Date(Long.valueOf(
                        msgResource.getProperty(SocialImplConstants.MSG_TIME_SENT))));
            } else if (SocialImplConstants.MSG_TITLE.equalsIgnoreCase(field.trim())) {
                messageObj.setTitle(msgResource.getProperty(SocialImplConstants.MSG_TITLE));
            } else if (SocialImplConstants.MSG_TITLE_ID.equalsIgnoreCase(field.trim())) {
                messageObj.setTitleId(msgResource.getProperty(SocialImplConstants.MSG_TITLE_ID));
            } else if (SocialImplConstants.MSG_TYPE.equalsIgnoreCase(field.trim())) {
                messageObj.setType(Message.Type.valueOf(
                        msgResource.getProperty(SocialImplConstants.MSG_TYPE)));
            } else if (SocialImplConstants.MSG_UPDATED.equalsIgnoreCase(field.trim())) {
                messageObj.setUpdated(new Date(Long.valueOf(
                        msgResource.getProperty(SocialImplConstants.MSG_UPDATED))));
            }
        }


        return messageObj;
    }
}
