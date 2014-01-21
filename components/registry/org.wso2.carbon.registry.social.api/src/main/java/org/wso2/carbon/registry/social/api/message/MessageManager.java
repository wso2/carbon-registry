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
package org.wso2.carbon.registry.social.api.message;


import org.wso2.carbon.registry.social.api.SocialMessageException;
import org.wso2.carbon.registry.social.api.utils.FilterOptions;

import java.util.List;
import java.util.Set;

/**
 * This interface handles messages and message collections related to people and applications
 * <p/>
 * Implement this interface to according to the persistence storage of social data
 */
public interface MessageManager {
    /**
     * Returns an array of message collections corresponding to the given user  details
     *
     * @param userId  The userId to fetch for
     * @param fields  The fields to fetch for the message collections
     * @param options Pagination details
     * @return An array of MessageCollection
     * @throws SocialMessageException
     */
    public MessageCollection[] getMessageCollections(String userId, Set<String> fields,
                                                     FilterOptions options)
            throws SocialMessageException;

    /**
     * Creates a MessageCollection of the the user with given id and attributes
     *
     * @param userId  The userId to which the collection belongs to
     * @param msgCollection The MessageCollection object to retrieve the attrobutes
     * @param messageCollectionId  The id of the message collection
     * @throws SocialMessageException
     */

    public void createMessageCollection(String userId, MessageCollection msgCollection,
                                                     String messageCollectionId)
            throws SocialMessageException;

    /**
     * Deletes a message collection for the given arguments
     *
     * @param userId              The userId to create the message collection for
     * @param messageCollectionId The message Collection ID to be removed
     * @throws SocialMessageException
     */

    public void deleteMessageCollection(String userId,
                                        String messageCollectionId) throws SocialMessageException;

    /**
     * Modifies/Updates a message collection for the given arguments
     *
     * @param userId              The userId to modify the message collection for
     * @param msgCollection       Data for the message collection to be modified
     * @param messageCollectionId The message Collection ID to modify
     * @throws SocialMessageException
     */

    public void modifyMessageCollection(String userId, MessageCollection msgCollection,
                                        String messageCollectionId) throws SocialMessageException;

    /**
     * Returns an array of messages that correspond to the passed in data
     *
     * @param userId          The userId of the person to fetch message for
     * @param msgCollectionId The message Collection ID to fetch from, default @all
     * @param fields          The fields to fetch for the messages
     * @param msgIds          An explicit set of message ids to fetch
     * @param options         Options to control the fetch
     * @return
     * @throws SocialMessageException
     */

    public Message[] getMessages(String userId, String msgCollectionId, Set<String> fields,
                                 List<String> msgIds, FilterOptions options)
            throws SocialMessageException;

    /**
     * Posts a message to the user's specified message collection, to be sent to the set of recipients
     * specified in the message
     *
     * @param userId          The user posting the message
     * @param msgCollectionId The message collection Id to post to, default @outbox
     * @param message         The message object
     * @return 
     * @throws SocialMessageException
     */
    public void createMessage(String userId, String msgCollectionId, Message message)
            throws SocialMessageException;

    /**
     * Posts a message to the user's specified message collection, to be sent to the set of recipients
     * specified in the message
     *
     * @param userId          The user posting the message
     * @param appId           The app id
     * @param msgCollectionId The message collection Id to post to, default @outbox
     * @param message         The message to post
     * @return 
     * @throws SocialMessageException
     */
    public void createMessage(String userId, String appId, String msgCollectionId, Message message)
            throws SocialMessageException;

    /**
     * Deletes a set of messages for a given user/message collection
     *
     * @param userId          The userId of the person whose messages to be deleted
     * @param msgCollectionId The Message Collection ID to delete from, default @all
     * @param messageIds      List of messageIds to delete
     * @throws SocialMessageException
     */
    public void deleteMessages(String userId, String msgCollectionId, List<String> messageIds)
            throws SocialMessageException;

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
            throws SocialMessageException;


}
