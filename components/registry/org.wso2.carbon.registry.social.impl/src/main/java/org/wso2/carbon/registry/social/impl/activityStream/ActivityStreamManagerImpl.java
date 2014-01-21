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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.social.api.SocialDataException;
import org.wso2.carbon.registry.social.api.activityStream.ActivityEntry;
import org.wso2.carbon.registry.social.api.activityStream.ActivityObject;
import org.wso2.carbon.registry.social.api.activityStream.ActivityStreamManager;
import org.wso2.carbon.registry.social.api.activityStream.MediaLink;
import org.wso2.carbon.registry.social.api.people.relationship.RelationshipManager;
import org.wso2.carbon.registry.social.api.utils.FilterOptions;
import org.wso2.carbon.registry.social.impl.SocialImplConstants;
import org.wso2.carbon.registry.social.impl.internal.SocialDSComponent;
import org.wso2.carbon.registry.social.impl.people.relationship.RelationshipManagerImpl;
import org.wso2.carbon.registry.social.impl.utils.FilterOptionsImpl;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*implementation of the {@link org.wso2.carbon.registry.social.api.activityStream.ActivityStreamManager}
 interface
* <p>
* This implementation uses the {@link org.wso2.carbon.registry.core.Registry} to store social
* {@link org.wso2.carbon.registry.social.api.activityStream.ActivityStreamManager} data
* </p>
* <p>
* An activity is stored as a {@link org.wso2.carbon.registry.core.Registry}
 * {@link org.wso2.carbon.registry.core.Resource}
* and the attributes of that activity are stored as properties of that registry resource
* </p>
* <p>
* <p/>
* Each Activity is grouped according to the appId of it
* </p>
* <p>
* Resource path : users/{userId}/{appId}/{activityId}
* </p>
*/
public class ActivityStreamManagerImpl implements ActivityStreamManager {
    private static Log log = LogFactory.getLog(ActivityStreamManagerImpl.class);

    /* The Registry object used throughout */
    private Registry registry = null;

    /* Setting the Registry object */

    public void setRegistry(Registry reg) {
        this.registry = reg;
    }
    /* Getting the Registry object for this manager */

    public Registry getRegistry() throws RegistryException {
        if (this.registry != null) {
            return this.registry;
        } else {
            return SocialDSComponent.getRegistry();
        }
    }

    @Override
    public void saveActivity(String userId, ActivityEntry activityEntry)
            throws SocialDataException {
        try {

            saveActivity(userId, activityEntry, false);

        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new SocialDataException("Error while saving activity for person " + userId +
                                          " with activityId " + activityEntry.getId(), e);
        }
    }

    @Override
    public void deleteActivity(String userId, String activityId, String appId)
            throws SocialDataException {
        String activityResourcePath = SocialImplConstants.USER_REGISTRY_ROOT + userId +
                                      SocialImplConstants.ACTIVITY_PATH + appId +
                                      SocialImplConstants.SEPARATOR + activityId;
        try {
            registry = getRegistry();
            if (registry.resourceExists(activityResourcePath)) {
                registry.delete(activityResourcePath);
            } else {
                throw new SocialDataException
                        ("Activity with specified activityId " + activityId + " doesn't exist");
            }
        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new SocialDataException("Error while deleting activity for person " + userId +
                                          " for activityId " + activityId, e);
        }
    }

    @Override
    public ActivityEntry[] getActivityEntries(String[] userIds, String groupId, String appId,
                                              Set<String> fields, FilterOptions options)
            throws SocialDataException {
        RelationshipManager relationshipManager = new RelationshipManagerImpl();
        List<ActivityEntry> activityList = new ArrayList<ActivityEntry>();
        List<String> userIdsToFetch = new ArrayList<String>();
        String[] userIdArray;
        // Handle GroupId
        if (groupId.equals(SocialImplConstants.GROUP_ID_SELF)) {
            userIdArray = userIds;
        } else if (groupId.equals(SocialImplConstants.GROUP_ID_FRIENDS)) {
            for (String id : userIds) {
                if (relationshipManager.getRelationshipList(id) != null) {
                    for (String friend : relationshipManager.getRelationshipList(id)) {
                        userIdsToFetch.add(friend);
                    }
                }
            }
            userIdArray = new String[userIdsToFetch.size()];
            userIdArray = userIdsToFetch.toArray(userIdArray);
        } else if (groupId.equals(SocialImplConstants.GROUP_ID_ALL)) {

            for (String id : userIds) {
                if (relationshipManager.getRelationshipList(id) != null) {
                    for (String friend : relationshipManager.getRelationshipList(id)) {
                        userIdsToFetch.add(friend);
                    }
                }
                userIdsToFetch.add(id);
            }
            userIdArray = new String[userIdsToFetch.size()];
            userIdArray = userIdsToFetch.toArray(userIdArray);
        } else {
            return new ActivityEntryImpl[0];
        }
        for (String userId : userIdArray) {    /*for each userId */
            try {
                registry = getRegistry();
                String resourcePath = SocialImplConstants.USER_REGISTRY_ROOT + userId +
                                      SocialImplConstants.ACTIVITY_PATH + appId;
                Collection activityCollection;
                if (options == null) {
                    options = new FilterOptionsImpl();
                    options.setMax(0);
                    options.setFirst(0);
                }
                if (registry.resourceExists(resourcePath)) {
                    int max = options.getMax() == 0 ? SocialImplConstants.DEFAULT_RETURN_ARRAY_SIZE
                                                    : options.getMax();
                    activityCollection = registry.get(resourcePath,
                                                      options.getFirst(),
                                                      max);

                    String[] activityResourcePaths = activityCollection.getChildren();
                    for (String path : activityResourcePaths) {   /* for each activity resource */
                        // ../activities/{appId}/<activityId>
                        String activityId = path.substring(
                                path.lastIndexOf(SocialImplConstants.SEPARATOR) + 1);
                        // Checks whether the path is the of the nextActivityId resource
                        if ((!activityId.equalsIgnoreCase(SocialImplConstants.NEXT_ACTIVITY_ID)) &&
                            getUserActivity(userId, appId, activityId) != null) {
                            activityList.add(getUserActivity(userId, appId, activityId));

                        }
                    }
                }
            } catch (RegistryException e) {
                log.error(e.getMessage(), e);
                throw new SocialDataException(
                        "Error while retrieving activities for user " + userId, e);
            }

        }
        ActivityEntry[] activityEntries = new ActivityEntryImpl[activityList.size()];
        return activityList.toArray(activityEntries);
    }


    public ActivityEntry[] getActivityEntries(String userId, String groupId, String appId,
                                              Set<String> fields, FilterOptions options,
                                              String[] activityIds) throws SocialDataException {
        if (activityIds == null) {
            throw new SocialDataException("Invalid activity ids");
        }
        if (activityIds.length <= 0) {
            getActivityEntries(new String[]{userId}, groupId, appId, fields, options);
        }
        //TODO: GroupID
        //TODO: FilterOptions is not used
        // Max size of returned activity array - default value

        ActivityEntry[] activityEntries = new ActivityEntryImpl[activityIds.length];
        for (int index = 0; index < activityIds.length; index++) {
            activityEntries[index] = getActivityEntry(userId, groupId, appId, fields, activityIds[index]);
        }

        return activityEntries;
    }

    @Override
    public ActivityEntry getActivityEntry(String userId, String groupId, String appId,
                                          Set<String> fields, String activityId)
            throws SocialDataException {
        //TODO: Set<String> fields is not currently used -- all fields are retrieved
        ActivityEntry userActivityEntry = null;
        if ((userId == null || userId.trim().equals("") || appId == null ||
             appId.trim().equals("") || activityId == null || activityId.trim().equals(""))) {
            throw new SocialDataException("Invalid parameters provided. " +
                                          "One or more of the parameter is either null or empty");
        }
        try {
            registry = getRegistry();
            if (groupId == null || groupId.equals(SocialImplConstants.GROUP_ID_SELF)) {
                // retrieve activity for user {userId}
                userActivityEntry = getUserActivity(userId, appId, activityId);

            }
            //TODO: Handle GroupID= @friends, @topfriends etc
        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new SocialDataException("Error while retrieving activity of user " +
                                          userId + " with activityId " + activityId, e);
        }

        return userActivityEntry;
    }

    /**
     * Retrieves the Activity object for the given userId,appId,activityId combination
     *
     * @param userId     The id of the person
     * @param appId      Application id
     * @param activityId The ide of the activity
     * @return An Activiy object for the passed in userId,appId,activityId combination
     * @throws RegistryException registry exception
     */

    private ActivityEntry getUserActivity(String userId, String appId, String activityId)
            throws RegistryException {
        registry = getRegistry();
        ActivityEntry userActivityEntry;
        // retrieve activity for user {userId}

        String selfActivityResourcePath = SocialImplConstants.USER_REGISTRY_ROOT + userId +
                                          SocialImplConstants.ACTIVITY_PATH + appId +
                                          SocialImplConstants.SEPARATOR + activityId;
        Resource selfActivityResource;
        if (registry.resourceExists(selfActivityResourcePath)) {
            // requested activity exists
            selfActivityResource = registry.get(selfActivityResourcePath);
            userActivityEntry = getPropertyAddedActivityEntry(selfActivityResource);

            /*Handling mediaLinks */
            String mediaLinkResourcePath = selfActivityResourcePath +
                                           SocialImplConstants.ACTIVITY_STREAM_MEDIA_PATH;
            Resource mediaResource;
            if (registry.resourceExists(mediaLinkResourcePath)) {
                mediaResource = registry.get(mediaLinkResourcePath);
                userActivityEntry.setIcon(getPropertyAddedMediaLink(mediaResource));
            }
            /*Handling actor object*/
            String actorResourcePath = selfActivityResourcePath +
                                       SocialImplConstants.ACTIVITY_STREAM_ACTOR_PATH;
            Resource actorResource;
            if (registry.resourceExists(actorResourcePath)) {
                actorResource = registry.get(actorResourcePath);
                String actorMediaResourcePath = actorResourcePath +
                                                SocialImplConstants.ACTIVITY_STREAM_MEDIA_PATH;
                Resource actorMediaResource;
                ActivityObject ackObj = new ActivityObjectImpl();
                if (registry.resourceExists(actorMediaResourcePath)) {
                    actorMediaResource = registry.get(actorMediaResourcePath);

                    ackObj.setImage(getPropertyAddedMediaLink(actorMediaResource));
                }
                ackObj = getPropertyAddedActivityObject(actorResource);
                userActivityEntry.setActor(ackObj);
            }

            /*Handling generator object*/
            String generatorResourcePath = selfActivityResourcePath +
                                           SocialImplConstants.ACTIVITY_STREAM_GENERATOR_PATH;
            Resource generatorResource;
            if (registry.resourceExists(generatorResourcePath)) {
                generatorResource = registry.get(generatorResourcePath);
                String generatorMediaResourcePath = generatorResourcePath +
                                                    SocialImplConstants.ACTIVITY_STREAM_MEDIA_PATH;
                Resource generatorMediaResource;
                ActivityObject ackGenObj = new ActivityObjectImpl();
                if (registry.resourceExists(generatorMediaResourcePath)) {
                    generatorMediaResource = registry.get(generatorMediaResourcePath);

                    ackGenObj.setImage(getPropertyAddedMediaLink(generatorMediaResource));
                }
                ackGenObj = getPropertyAddedActivityObject(generatorResource);

                userActivityEntry.setGenerator(ackGenObj);
            }

            /*Handling 'object' object*/
            String objectResourcePath = selfActivityResourcePath +
                                        SocialImplConstants.ACTIVITY_STREAM_OBJECT_PATH;
            Resource objectResource;
            if (registry.resourceExists(objectResourcePath)) {
                objectResource = registry.get(objectResourcePath);
                String objectMediaResourcePath = objectResourcePath +
                                                 SocialImplConstants.ACTIVITY_STREAM_MEDIA_PATH;
                Resource objectMediaResource;
                ActivityObject ackObObj = new ActivityObjectImpl();
                if (registry.resourceExists(objectMediaResourcePath)) {
                    objectMediaResource = registry.get(objectMediaResourcePath);

                    ackObObj.setImage(getPropertyAddedMediaLink(objectMediaResource));
                }
                ackObObj = getPropertyAddedActivityObject(objectResource);
                userActivityEntry.setObject(ackObObj);


            }


            /*Handling 'target' object*/
            String targetResourcePath = selfActivityResourcePath +
                                        SocialImplConstants.ACTIVITY_STREAM_OBJECT_PATH;
            Resource targetResource;
            if (registry.resourceExists(targetResourcePath)) {
                targetResource = registry.get(targetResourcePath);
                String targetMediaResourcePath = targetResourcePath +
                                                 SocialImplConstants.ACTIVITY_STREAM_MEDIA_PATH;
                Resource targetMediaResource;
                ActivityObject acktargetObj = new ActivityObjectImpl();
                if (registry.resourceExists(targetMediaResourcePath)) {
                    targetMediaResource = registry.get(targetMediaResourcePath);

                    acktargetObj.setImage(getPropertyAddedMediaLink(targetMediaResource));
                }
                acktargetObj = getPropertyAddedActivityObject(targetResource);
                userActivityEntry.setTarget(acktargetObj);
            }


            /*Handling 'provider' object*/
            String providerResourcePath = selfActivityResourcePath +
                                          SocialImplConstants.ACTIVITY_STREAM_OBJECT_PATH;
            Resource providerResource;


            if (registry.resourceExists(providerResourcePath)) {
                providerResource = registry.get(providerResourcePath);

                String providerMediaResourcePath = providerResourcePath +
                                                   SocialImplConstants.ACTIVITY_STREAM_MEDIA_PATH;
                Resource providerMediaResource;
                ActivityObject ackproviderObj = new ActivityObjectImpl();
                if (registry.resourceExists(providerMediaResourcePath)) {
                    providerMediaResource = registry.get(providerMediaResourcePath);

                    ackproviderObj.setImage(getPropertyAddedMediaLink(providerMediaResource));
                }
                ackproviderObj = getPropertyAddedActivityObject(providerResource);
                userActivityEntry.setTarget(ackproviderObj);


            }


        } else {
            //requested activity doesn't exist
            log.error("No activity found with id " + activityId);
            return null;

        }
        return userActivityEntry;
    }

    @Override
    public void deleteActivityEntries(String userId, String groupId, String appId,
                                      Set<String> activityIds) throws SocialDataException {
        //TODO: Handle groupId
        for (String activityId : activityIds) {
            deleteActivity(userId, activityId, appId);
        }
    }

    @Override
    public void updateActivityEntry(String userId, ActivityEntry activityEntry)
            throws SocialDataException {
        try {
            saveActivity(userId, activityEntry, true);

        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new SocialDataException(e);
        }
    }

    @Override
    public void createActivityEntry(String userId, String groupId, String appId,
                                    Set<String> fields, ActivityEntry activityEntry)
            throws SocialDataException {
        // Should save the activity according to given parameters
        if (activityEntry == null) {
            String errorMsg = "Cannot create activity. Passed Activity argument is null";
            log.error(errorMsg);
            throw new SocialDataException(errorMsg);
        }
        //if the posted time is not set, set system time as posted time
        if (activityEntry.getPublished() == null) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            activityEntry.setPublished(dateFormat.format(date));
        }

        ActivityObject generator = new ActivityObjectImpl();
        generator.setDisplayName(appId);
        activityEntry.setGenerator(generator);

        ActivityObject actor = new ActivityObjectImpl();
        actor.setDisplayName(userId);
        activityEntry.setActor(actor);

        //TODO: Handle Set<String> fields - All fields are retrieved currently
        saveActivity(userId, activityEntry);
    }

    /**
     * Persists/updates the Activity as a registry resource
     *
     * @param userId        The id of the person
     * @param activityEntry The Activity object to persist/update
     * @param isUpdate      true- if required to update &persist
     *                      false- if required to persist
     * @throws RegistryException registry exception
     */
    private void saveActivity(String userId, ActivityEntry activityEntry, boolean isUpdate)
            throws RegistryException {
        registry = getRegistry();
        String nextActivityPath = SocialImplConstants.USER_REGISTRY_ROOT +
                                  userId +
                                  SocialImplConstants.ACTIVITY_PATH +
                                  activityEntry.getGenerator().getDisplayName() +
                                  SocialImplConstants.NEXT_ACTIVITY_ID_PATH;
        /* Check for null activity id  */
        if (activityEntry.getId() == null) {
            // No activity Id specified
            Resource nextActivityIdResource;
            int nextActivityId;
            if (registry.resourceExists(nextActivityPath)) {
                nextActivityIdResource = registry.get(nextActivityPath);
                if (nextActivityIdResource.getProperty(SocialImplConstants.NEXT_ACTIVITY_ID) != null) {
                    nextActivityId = Integer.valueOf(nextActivityIdResource.getProperty
                            (SocialImplConstants.NEXT_ACTIVITY_ID));
                } else {
                    nextActivityId = 0;
                }

            } else {
                // there's no nextActivityId Resource
                nextActivityId = 0;
                nextActivityIdResource = registry.newCollection();
            }
            nextActivityIdResource.setProperty(SocialImplConstants.NEXT_ACTIVITY_ID,
                                               (nextActivityId + 1) + "");
            registry.put(nextActivityPath, nextActivityIdResource);
            activityEntry.setId(nextActivityId + "");
        } else {
            // There's an activityId specified
            Resource nextActivityIdResource = null;
            int nextActivityId = -1;
            if (registry.resourceExists(nextActivityPath)) {
                nextActivityIdResource = registry.get(nextActivityPath);
                if (nextActivityIdResource.getProperty(SocialImplConstants.NEXT_ACTIVITY_ID) != null) {
                    nextActivityId = Integer.valueOf(nextActivityIdResource.getProperty
                                     (SocialImplConstants.NEXT_ACTIVITY_ID));
                }

            }

            String userActivityId = activityEntry.getId();
            boolean isIntegerId;
            int userActivityIdInteger = 0;
            try {
                userActivityIdInteger = Integer.parseInt(userActivityId);
                isIntegerId = true;

            } catch (NumberFormatException e) {
                isIntegerId = false;
            }
            if (nextActivityId > 0 && isIntegerId && nextActivityIdResource != null) {
                if (nextActivityId == userActivityIdInteger) {
                    // The new activity Id is same as the next ActivityId, So increase the nextActivityId
                    activityEntry.setId(nextActivityId + "");
                    nextActivityIdResource.setProperty(SocialImplConstants.NEXT_ACTIVITY_ID,
                                                       (nextActivityId + 1) + "");
                    registry.put(nextActivityPath, nextActivityIdResource);
                }


            }
        }
        /* Recording Activities */
        String newActivityPath = SocialImplConstants.USER_REGISTRY_ROOT +
                                 userId +
                                 SocialImplConstants.ACTIVITY_PATH +
                                 activityEntry.getGenerator().getDisplayName() +
                                 SocialImplConstants.SEPARATOR +
                                 activityEntry.getId();

        Resource newActivityResource;
        if (registry.resourceExists(newActivityPath)) {
            newActivityResource = registry.get(newActivityPath);
        } else {
            newActivityResource = registry.newCollection();
        }

        newActivityResource = saveActivityEntryResourceProperties(activityEntry,
                                                                  newActivityResource, isUpdate);
        registry.put(newActivityPath, newActivityResource);
        /* Handling MediaLinks */
        if (activityEntry.getIcon() != null) {
            String resourcePath = newActivityPath +
                                  SocialImplConstants.ACTIVITY_STREAM_MEDIA_PATH;
            Resource mediaResource;
            if (registry.resourceExists(resourcePath)) {
                mediaResource = registry.get(resourcePath);
            } else {
                mediaResource = registry.newCollection();
            }

            saveMediaLinkResourceProperties(
                    activityEntry.getIcon(), mediaResource, isUpdate);
            registry.put(resourcePath, mediaResource);
        }

        /*Handling actor object of the activity */
        if (activityEntry.getActor() != null) {
            HashMap<String, String> pathVals = new HashMap<String, String>();
            pathVals.put("activityPath", newActivityPath);
            pathVals.put("registryObjectPath", SocialImplConstants.ACTIVITY_STREAM_ACTOR_PATH);

            saveActivityObjectResourceProperties(activityEntry.getActor(), pathVals, isUpdate);
        }

        /*Handling generator object of the activity */
        if (activityEntry.getGenerator() != null) {
            HashMap<String, String> pathVals = new HashMap<String, String>();
            pathVals.put("activityPath", newActivityPath);
            pathVals.put("registryObjectPath", SocialImplConstants.ACTIVITY_STREAM_GENERATOR_PATH);

            saveActivityObjectResourceProperties(activityEntry.getGenerator(), pathVals, isUpdate);
        }

        /*Handling 'object' object of the activity */
        if (activityEntry.getObject() != null) {
            HashMap<String, String> pathVals = new HashMap<String, String>();
            pathVals.put("activityPath", newActivityPath);
            pathVals.put("registryObjectPath", SocialImplConstants.ACTIVITY_STREAM_OBJECT_PATH);

            saveActivityObjectResourceProperties(activityEntry.getObject(), pathVals, isUpdate);
        }

        /*Handling target object of the activity */
        if (activityEntry.getTarget() != null) {
            HashMap<String, String> pathVals = new HashMap<String, String>();
            pathVals.put("activityPath", newActivityPath);
            pathVals.put("registryObjectPath", SocialImplConstants.ACTIVITY_STREAM_GENERATOR_PATH);

            saveActivityObjectResourceProperties(activityEntry.getTarget(), pathVals, isUpdate);
        }

        /*Handling provider object of the activity */
        if (activityEntry.getProvider() != null) {
            HashMap<String, String> pathVals = new HashMap<String, String>();
            pathVals.put("activityPath", newActivityPath);
            pathVals.put("registryObjectPath", SocialImplConstants.ACTIVITY_STREAM_GENERATOR_PATH);

            saveActivityObjectResourceProperties(activityEntry.getProvider(), pathVals, isUpdate);
        }


    }

    /**
     * Adds the properties of the activity entry resource as attributes to a ActivityEntry object
     * and returns that ActivityEntry object
     *
     * @param activityResource The registry resource to retrieve properties from
     * @return An ActivityEntry object with the its attributes added
     */
    private ActivityEntry getPropertyAddedActivityEntry(Resource activityResource) {
        ActivityEntry activityEntry = new ActivityEntryImpl();

        String value;
        if ((value = activityResource.getProperty(
                SocialImplConstants.ACTIVITY_STREAM_ID)) != null) {
            activityEntry.setId(value);
        }
        if ((value = activityResource.getProperty(
                SocialImplConstants.ACTIVITYSTREAM_URL)) != null) {
            activityEntry.setUrl(value);
        }
        if ((value = activityResource.getProperty(
                SocialImplConstants.ACTIVITY_STREAM_VERB)) != null) {
            activityEntry.setVerb(value);
        }

        if ((value = activityResource.getProperty(
                SocialImplConstants.ACTIVITYSTREAM_TITLE)) != null) {
            activityEntry.setTitle(value);
        }
        if ((value = activityResource.getProperty(
                SocialImplConstants.ACTIVITY_STREAM_PUBLISHED)) != null) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date updateDate = new Date(value);
            activityEntry.setPublished(dateFormat.format(updateDate));
        }
        if ((value = activityResource.getProperty(
                SocialImplConstants.ACTIVITY_STREAM_CONTENT)) != null) {
            activityEntry.setContent(value);
        }

        if ((value = activityResource.getProperty(
                SocialImplConstants.ACTIVITY_STREAM_UPDATED)) != null) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date updateDate = new Date(value);
            activityEntry.setUpdated(dateFormat.format(updateDate));

        }

        return activityEntry;
    }

    /**
     * Adds or update the attributes of the Activity Entry as properties for the Activity registry resource
     *
     * @param userActivity     The ActivityEntry object from which to retrieve the attributes
     * @param activityResource The registry Resource object to add/update properties
     * @param isUpdate         true - if required to update the properties
     *                         false - if required to add the properties
     * @return The registry resource passed as method parameter added/updated with its properties
     */
    private Resource saveActivityEntryResourceProperties(ActivityEntry userActivity,
                                                         Resource activityResource,
                                                         boolean isUpdate) {
        if (userActivity == null || activityResource == null) {
            return null;
        }
        /* Retrieves attributes of the Activity and adds it to a Map */
        Map<String, String> properties = new HashMap<String, String>();
        if (userActivity.getId() != null) {
            properties.put(SocialImplConstants.ACTIVITY_STREAM_ID, userActivity.getId());
        }
        if (userActivity.getContent() != null) {
            properties.put(SocialImplConstants.ACTIVITY_STREAM_CONTENT, userActivity.getContent());
        }

        if (userActivity.getPublished() != null) {
            properties.put(SocialImplConstants.ACTIVITY_STREAM_PUBLISHED,
                           userActivity.getPublished());
        }
        if (userActivity.getTitle() != null) {
            properties.put(SocialImplConstants.ACTIVITYSTREAM_TITLE,
                           userActivity.getTitle());
        }
        if (userActivity.getUpdated() != null) {
            properties.put(SocialImplConstants.ACTIVITY_STREAM_UPDATED, userActivity.getUpdated());
        }
        if (userActivity.getUrl() != null) {
            properties.put(SocialImplConstants.ACTIVITYSTREAM_URL, userActivity.getUrl());
        }
        if (userActivity.getVerb() != null) {
            properties.put(SocialImplConstants.ACTIVITY_STREAM_VERB, userActivity.getVerb());
        }
        /* Attach the property map to the registry resource */
        attachPropertyToResource(activityResource, properties, isUpdate);
        return activityResource;

    }

    /**
     * Add/ edit a property value for the given Registry resource
     *
     * @param activityResource Registry resource to add/set property
     * @param properties       A Map consisting properties and it's values
     * @param isUpdate         True, if it is an update. False, if it is an addition
     */
    private void attachPropertyToResource(Resource activityResource, Map<String, String> properties,
                                          boolean isUpdate) {


        for (Map.Entry<String, String> e : properties.entrySet()) {
            if (e.getValue() != null) {
                if (isUpdate) {
                    String oldValue = activityResource.getProperty(e.getKey());
                    activityResource.editPropertyValue(e.getKey(), oldValue, e.getValue());
                } else {
                    activityResource.addProperty(e.getKey(), e.getValue());
                }
            }
        }

    }

    /**
     * Adds or update the attributes of the ActivityObject as properties for the ActivityObject
     * registry resource
     *
     * @param objResource The ActivityObject object to retrieve properties      *
     *                    <p/>
     *                    false- if required to add the properties
     * @return The passed in registry resource with properties added
     */
    private ActivityObject getPropertyAddedActivityObject(Resource objResource
    ) {
        ActivityObject actObject = new ActivityObjectImpl();
        String value;
        if (objResource == null) {
            return null;
        }
        //TODO: HAVE to handle author ,downstream/upstream duplicates and attachments properties


        if ((value = objResource.getProperty(SocialImplConstants.ACTIVITY_STREAM_OBJECT_CONTENT))
            != null) {
            actObject.setContent(value);
        }
        if ((value = objResource.getProperty(SocialImplConstants.ACTIVITY_STREAM_OBJECT_DISPLAYNAME))
            != null) {
            actObject.setDisplayName(value);
        }
        if ((value = objResource.getProperty(SocialImplConstants.ACTIVITY_STREAM_OBJECT_ID))
            != null) {
            actObject.setId(value);
        }
        if ((value = objResource.getProperty(SocialImplConstants.ACTIVITY_STREAM_OBJECT_URL))
            != null) {
            actObject.setUrl(value);
        }


        if ((value = objResource.getProperty(SocialImplConstants.ACTIVITY_STREAM_OBJECT_SUMMARY))
            != null) {
            actObject.setSummary(value);
        }
        if ((value = objResource.getProperty(SocialImplConstants.ACTIVITY_STREAM_OBJECT_UPDATED))
            != null) {
            actObject.setUpdated(value);
        }
        if ((value = objResource.getProperty(SocialImplConstants.ACTIVITYSTREAM_OBJECT_TYPE))
            != null) {
            actObject.setObjectType(value);
        }
        if ((value = objResource.getProperty(SocialImplConstants.ACTIVITYSTREAM_OBJECT_PUBLISHED))
            != null) {
            actObject.setPublished(value);
        }

        return actObject;
    }

    /**
     * Adds or update the attributes of the MediaLink as properties for the MediaLink
     * registry resource
     *
     * @param mediaLinkResource The MediaLink object to retrieve properties
     * @return MediaLink object
     */
    private MediaLink getPropertyAddedMediaLink(Resource mediaLinkResource
    ) {
        String value;
        MediaLink mLink = new MediaLinkImpl();
        if (mediaLinkResource == null) {
            return null;
        }
        if ((value = mediaLinkResource.getProperty(
                SocialImplConstants.ACTIVITY_STREAM_MEDIA_URL)) != null) {
            mLink.setUrl(value);
        }
        if ((value = mediaLinkResource.getProperty(SocialImplConstants.ACTIVITY_STREAM_MEDIA_DURATION))
            != null
            ) {
            mLink.setDuration(Integer.valueOf(value));
        }
        if ((value = mediaLinkResource.getProperty(SocialImplConstants.ACTIVITY_STREAM_MEDIA_HEIGHT))
            != null) {
            mLink.setHeight(Integer.valueOf(value));
        }
        if ((value = mediaLinkResource.getProperty(SocialImplConstants.ACTIVITY_STREAM_MEDIA_WIDTH))
            != null) {
            mLink.setWidth(Integer.valueOf(value));
        }

        return mLink;
    }

    /**
     * Returns an array of Activity objects to the passed parameters,sorted according to the posted time
     *
     * @param userIds The set of userIds for which activities to be fetched
     * @param groupId The groupId
     * @param appId   The appId of the activities to be fetched
     * @param fields  The fields to return. Empty set implies all
     * @param options The sorting/filtering/pagination options
     * @return Sorted ActivityEntry[]
     * @throws org.wso2.carbon.registry.social.api.SocialDataException
     *
     */

    public ActivityEntry[] getSortedActivityEntries(String[] userIds, String groupId, String appId,
                                                    Set<String> fields, FilterOptions options)
            throws SocialDataException {
        ActivityEntry[] activities = getActivityEntries(userIds, groupId, appId, fields, options);
        Arrays.sort(activities, new ActivityEntryComparator());
        return activities;
    }

    /**
     * Method to store MediaLink object as a registry resource
     *
     * @param activityMediaLink MediaLink object
     * @param mediaItemResource resource
     * @param isUpdate          update/save
     * @return registry resource
     */
    private Resource saveMediaLinkResourceProperties(MediaLink activityMediaLink,
                                                     Resource mediaItemResource,
                                                     boolean isUpdate) {
        if (mediaItemResource == null || activityMediaLink == null) {
            return null;
        }
        Map<String, String> properties = new HashMap<String, String>();

        if (activityMediaLink.getUrl() != null) {
            properties.put(SocialImplConstants.ACTIVITY_MEDIA_ITEM_MIME_TYPE,
                           activityMediaLink.getUrl());
        }
        if (activityMediaLink.getWidth() != null) {
            properties.put(SocialImplConstants.ACTIVITY_MEDIA_ITEM_TYPE,
                           activityMediaLink.getWidth().toString());
        }
        if (activityMediaLink.getHeight() != null) {
            properties.put(SocialImplConstants.ACTIVITY_MEDIA_ITEM_THUMBNAIL_URL,
                           activityMediaLink.getHeight().toString());
        }
        if (activityMediaLink.getDuration() != null) {
            properties.put(SocialImplConstants.ACTIVITY_MEDIA_ITEM_URL,
                           activityMediaLink.getDuration().toString());
        }
        attachPropertyToResource(mediaItemResource, properties, isUpdate);
        return mediaItemResource;
    }

    /**
     * Method to set attributes of ActivityObject object to the registry
     *
     * @param actObject ActivityObject
     * @param pathVals  registry path values
     * @param isUpdate  update/save
     */
    private void saveActivityObjectResourceProperties(ActivityObject actObject,
                                                      HashMap<String, String> pathVals,
                                                      boolean isUpdate) {
        String activityPath = pathVals.get("activityPath");
        String registryObjectPath = pathVals.get("registryObjectPath");
        String registryMediaPath = SocialImplConstants.ACTIVITY_STREAM_MEDIA_PATH;

        String resourceObjectPath = activityPath +
                                    registryObjectPath;
        Resource objectResource;
        try {
            if (registry.resourceExists(resourceObjectPath)) {
                objectResource = registry.get(resourceObjectPath);
            } else {
                objectResource = registry.newCollection();
            }
            saveActivityObjectProperties(
                    actObject, objectResource, isUpdate);
            registry.put(resourceObjectPath, objectResource);
            if (actObject.getImage() != null) {
                String resourceActorMediaPath = resourceObjectPath + registryMediaPath;
                Resource objectMediaResource;
                if (registry.resourceExists(resourceActorMediaPath)) {
                    objectMediaResource = registry.get(resourceActorMediaPath);
                } else {
                    objectMediaResource = registry.newCollection();
                }
                saveMediaLinkResourceProperties(
                        actObject.getImage(), objectMediaResource, isUpdate);
            }
            //TODO:Handle attachments and author property of activity objects.
        } catch (RegistryException e) {
            log.error("Exception occured in registry" + e.getMessage());

        }


    }

    /**
     * Method to store ActivityObject object as a registry resource
     *
     * @param activityObject    ActivityObject
     * @param mediaItemResource Registry resource
     * @param isUpdate          save/update
     * @return Registry resource
     */
    private Resource saveActivityObjectProperties(ActivityObject activityObject,
                                                  Resource mediaItemResource,
                                                  boolean isUpdate) {
        if (mediaItemResource == null || activityObject == null) {
            return null;
        }
        Map<String, String> properties = new HashMap<String, String>();

        if (activityObject.getUrl() != null) {
            properties.put(SocialImplConstants.ACTIVITY_STREAM_OBJECT_URL,
                           activityObject.getUrl());
        }
        if (activityObject.getDisplayName() != null) {
            properties.put(SocialImplConstants.ACTIVITY_STREAM_OBJECT_DISPLAYNAME,
                           activityObject.getDisplayName());
        }
        if (activityObject.getId() != null) {
            properties.put(SocialImplConstants.ACTIVITY_STREAM_OBJECT_ID,
                           activityObject.getId());
        }
        if (activityObject.getContent() != null) {
            properties.put(SocialImplConstants.ACTIVITY_STREAM_OBJECT_CONTENT,
                           activityObject.getContent());
        }
        if (activityObject.getObjectType() != null) {
            properties.put(SocialImplConstants.ACTIVITYSTREAM_OBJECT_TYPE,
                           activityObject.getObjectType());
        }
        if (activityObject.getPublished() != null) {
            properties.put(SocialImplConstants.ACTIVITYSTREAM_OBJECT_PUBLISHED,
                           activityObject.getPublished());
        }
        if (activityObject.getSummary() != null) {
            properties.put(SocialImplConstants.ACTIVITY_STREAM_OBJECT_SUMMARY,
                           activityObject.getSummary());
        }
        if (activityObject.getUpdated() != null) {
            properties.put(SocialImplConstants.ACTIVITY_STREAM_OBJECT_UPDATED,
                           activityObject.getUpdated());
        }
        /* if (activityObject.getDownstreamDuplicates() != null) {
          properties.put(SocialImplConstants.ACTIVITY_STREAM_OBJECT_DOWNSTREAM_DUPLICATES,
                         activityObject.getDownstreamDuplicates());
      }
      if (activityObject.getUpstreamDuplicates() != null) {
          properties.put(SocialImplConstants.ACTIVITY_STREAM_OBJECT_UPSTREAM_DUPLICATES,
                         activityObject.getUpstreamDuplicates());
      }  */
        attachPropertyToResource(mediaItemResource, properties, isUpdate);
        return mediaItemResource;
    }


    /**
     * A Comparator to sort activities according to posted time
     */
    static class ActivityEntryComparator implements Comparator<ActivityEntry>, Serializable {

        public int compare(ActivityEntry activity1, ActivityEntry activity2) {
            if (activity1.getPublished() == null && activity2.getPublished() == null) {
                return 0;   // both are null, equal
            } else if (activity1.getPublished() == null) {
                return -1;  // this is null, comes before real date
            } else if (activity2.getPublished() == null) {
                return 1;   // that is null, this comes after
            } else {      // compare publish dates in lexicographical order
                return activity1.getPublished().compareTo(activity2.getPublished());
            }
        }

    }
}
