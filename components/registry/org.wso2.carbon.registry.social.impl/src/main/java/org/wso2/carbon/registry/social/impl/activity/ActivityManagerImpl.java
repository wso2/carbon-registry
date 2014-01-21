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

package org.wso2.carbon.registry.social.impl.activity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.social.api.SocialDataException;
import org.wso2.carbon.registry.social.api.activity.Activity;
import org.wso2.carbon.registry.social.api.activity.ActivityManager;
import org.wso2.carbon.registry.social.api.activity.MediaItem;
import org.wso2.carbon.registry.social.api.people.relationship.RelationshipManager;
import org.wso2.carbon.registry.social.api.utils.FilterOptions;
import org.wso2.carbon.registry.social.impl.SocialImplConstants;
import org.wso2.carbon.registry.social.impl.internal.SocialDSComponent;
import org.wso2.carbon.registry.social.impl.people.relationship.RelationshipManagerImpl;
import org.wso2.carbon.registry.social.impl.utils.FilterOptionsImpl;

import java.io.Serializable;
import java.util.*;

/**
 * An implementation of the {@link org.wso2.carbon.registry.social.api.activity.ActivityManager} interface
 * <p>
 * This implementation uses the {@link org.wso2.carbon.registry.core.Registry} to store social {@link org.wso2.carbon.registry.social.api.activity.Activity} data
 * </p>
 * <p>
 * An activity is stored as a {@link org.wso2.carbon.registry.core.Registry}  {@link org.wso2.carbon.registry.core.Resource}
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

public class ActivityManagerImpl implements ActivityManager {
    private static Log log = LogFactory.getLog(ActivityManagerImpl.class);

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

    /**
     * Persists the activity for the given userId
     *
     * @param userId       The userId of the person whose activity to be stored
     * @param userActivity The Activity object of the user to be stored
     * @throws org.wso2.carbon.registry.social.api.SocialDataException
     *
     */
    public void saveActivity(String userId, Activity userActivity) throws SocialDataException {


        try {

            saveActivity(userId, userActivity, false);

        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new SocialDataException("Error while saving activity for person " + userId +
                    " with activityId " + userActivity.getId(), e);
        }

    }

    /**
     * Removes the activity of the given userId
     *
     * @param userId     The userId of the person whose activity to be removed
     * @param activityId The id of the activity to be removed
     * @param appId      The appId of the activity
     * @throws org.wso2.carbon.registry.social.api.SocialDataException
     *
     */
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

    /**
     * Updates the activity of the given userId
     *
     * @param userId       The userId of the person whose activity to be modified
     * @param userActivity The activity to be modified
     * @throws org.wso2.carbon.registry.social.api.SocialDataException
     *
     */
    public void updateActivity(String userId, Activity userActivity) throws SocialDataException {
        try {
            saveActivity(userId, userActivity, true);

        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new SocialDataException(e);
        }
    }

    /**
     * Returns an array of Activity objects to the passed parameters
     *
     * @param userIds The set of userIds for which activities to be fetched
     * @param groupId The groupId
     * @param appId   The appId of the activities to be fetched
     * @param fields  The fields to return. Empty set implies all
     * @param options The sorting/filtering/pagination options
     * @throws org.wso2.carbon.registry.social.api.SocialDataException
     *
     */
    public Activity[] getActivities(String[] userIds, String groupId, String appId,
                                    Set<String> fields, FilterOptions options)
            throws SocialDataException {

        RelationshipManager relationshipManager=new RelationshipManagerImpl();
        List<Activity> activityList = new ArrayList<Activity>();
        List<String> userIdsToFetch=new ArrayList<String>();
        String[] userIdArray=null;
        // Handle GroupId
        if(groupId.equals(SocialImplConstants.GROUP_ID_SELF)){
            userIdArray=userIds;
        }
        else if(groupId.equals(SocialImplConstants.GROUP_ID_FRIENDS)){
            for(String id:userIds){
                if(relationshipManager.getRelationshipList(id)!=null){
                    for(String friend:relationshipManager.getRelationshipList(id)){
                        userIdsToFetch.add(friend);
                    }
                }
            }
            userIdArray=new String[userIdsToFetch.size()];
            userIdArray=userIdsToFetch.toArray(userIdArray);
        } else {
            return new ActivityImpl[0];
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
                    int max = options.getMax() == 0 ? SocialImplConstants.DEFAULT_RETURN_ARRAY_SIZE : options.getMax();
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
        Activity[] activities = new ActivityImpl[activityList.size()];
        return activityList.toArray(activities);
    }

    /**
     * Returns an array of Activity objects to the passed parameters  , sorted according to the posted time
     *
     * @param userIds The set of userIds for which activities to be fetched
     * @param groupId The groupId
     * @param appId   The appId of the activities to be fetched
     * @param fields  The fields to return. Empty set implies all
     * @param options The sorting/filtering/pagination options
     * @throws org.wso2.carbon.registry.social.api.SocialDataException
     *
     */
    public Activity[] getSortedActivities(String[] userIds, String groupId, String appId,
                                          Set<String> fields, FilterOptions options)
            throws SocialDataException {
        Activity[] activities = getActivities(userIds, groupId, appId, fields, options);
        Arrays.sort(activities, new ActivityComparator());
        return activities;
    }

    /**
     * Returns an array of Activity objects to the passed activityIds
     *
     * @param userId      The userId of the  person whose activity to be fetched
     * @param groupId     The groupId
     * @param appId       The appId of the activities to be fetched
     * @param fields      The fields to return. Empty set implies all
     * @param options     The sorting/filtering/pagination options
     * @param activityIds The ids of the activities to be fetched
     * @throws org.wso2.carbon.registry.social.api.SocialDataException
     *
     */
    public Activity[] getActivities(String userId, String groupId, String appId, Set<String> fields,
                                    FilterOptions options, String[] activityIds)
            throws SocialDataException {
        if (activityIds == null) {
            throw new SocialDataException("Invalid activity ids");
        }
        if (activityIds.length <= 0) {
            getActivities(new String[]{userId}, groupId, appId, fields, options);
        }
         //TODO: GroupID
        //TODO: FilterOptions is not used
        // Max size of returned activity array - default value

        Activity[] activities = new ActivityImpl[activityIds.length];
        for (int index = 0; index < activityIds.length; index++) {
            if (activityIds[index] != null) {
                activities[index] = getActivity(userId, groupId, appId, fields, activityIds[index]);
            }
        }
        
        return activities;
    }

    /**
     * Returns an array of Activity objects to the passed activityIds
     *
     * @param userId      The userId of the  person whose activity to be fetched
     * @param groupId     The groupId
     * @param appId       The appId of the activities to be fetched
     * @param fields      The fields to return. Empty set implies all
     * @param options     The sorting/filtering/pagination options
     * @param activityIds The ids of the activities to be fetched
     * @throws org.wso2.carbon.registry.social.api.SocialDataException
     *
     */
    public Activity[] getSortedActivities(String userId, String groupId, String appId, Set<String> fields,
                                          FilterOptions options, String[] activityIds)
            throws SocialDataException {
        Activity[] activities = getActivities(userId, groupId, appId, fields, options, activityIds);
        Arrays.sort(activities, new ActivityComparator());
        return activities;
    }

    /**
     * Returns the Activity object for the given user
     *
     * @param userId     The userId of the  person whose activity to be fetched
     * @param groupId    The groupId
     * @param appId      The appId of the activity
     * @param fields     The fields to return. Empty set implies all
     * @param activityId The activityId of the activity to be fetched
     * @return A Activity object for the given parameters
     * @throws org.wso2.carbon.registry.social.api.SocialDataException
     *
     */
    public Activity getActivity(String userId, String groupId, String appId, Set<String> fields,
                                String activityId) throws SocialDataException {
        //TODO: Set<String> fields is not currently used -- all fields are retrieved
        Activity userActivity = null;
        if ((userId == null || userId.trim().equals("") || appId == null ||
                appId.trim().equals("") || activityId == null || activityId.trim().equals(""))) {
            throw new SocialDataException("Invalid parameters provided. " +
                    "One or more of the parameter is either null or empty");
        }
        try {
            registry = getRegistry();
            if (groupId == null || groupId.equals(SocialImplConstants.GROUP_ID_SELF)) {
                // retrieve activity for user {userId}
                userActivity = getUserActivity(userId, appId, activityId);

            }
            //TODO: Handle GroupID= @friends, @topfriends etc
        }
        catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new SocialDataException("Error while retrieving activity of user " +
                    userId + " with activityId " + activityId, e);
        }

        return userActivity;
    }

    /**
     * Deletes the activities for the given activityIds
     *
     * @param userId      The userId of the person whose activities to be deleted
     * @param groupId     The groupId
     * @param appId       The appId
     * @param activityIds The activityIds to be deleted
     * @throws org.wso2.carbon.registry.social.api.SocialDataException
     *
     */
    public void deleteActivities(String userId, String groupId, String appId,
                                 Set<String> activityIds) throws SocialDataException {
        //TODO: Handle groupId
        for (String activityId : activityIds) {
            deleteActivity(userId, activityId, appId);
        }

    }

    /**
     * Creates an Activity object for the given person using the given details
     *
     * @param userId   The userId of the person to create the activity for
     * @param groupId  The groupId
     * @param appId    The appId of the activity
     * @param fields   Fields of the activity to be created
     * @param activity The activity to create
     * @throws org.wso2.carbon.registry.social.api.SocialDataException
     *
     */
    public void createActivity(String userId, String groupId, String appId, Set<String> fields,
                               Activity activity)
            throws SocialDataException {
        // Should save the activity according to given parameters
        if (activity == null) {
            String errorMsg = "Cannot create activity. Passed Activity argument is null";
            log.error(errorMsg);
            throw new SocialDataException(errorMsg);
        }
        //if the posted time is not set, set system time as posted time
        if (activity.getPostedTime() == null || activity.getPostedTime() == 0) {
            activity.setPostedTime(System.currentTimeMillis());
        }
        activity.setAppId(appId);
        activity.setUserId(userId);
        //TODO: Handle Set<String> fields - All fields are retrieved currently
        saveActivity(userId, activity);

    }

    /**
     * Adds or update the attributes of the Activity as properties for the Activity registry resource
     *
     * @param userActivity     The Activity object from which to retrieve the attributes
     * @param activityResource The registry Resource object to add/update properties
     * @param isUpdate         true - if required to update the properties
     *                         false - if required to add the properties
     * @return The registry resource passed as method parameter added/updated with its properties
     */
    private Resource getPropertiesAddedActivityResourceObj(Activity userActivity,
                                                           Resource activityResource,
                                                           boolean isUpdate) {
        if (userActivity == null || activityResource == null) {
            return null;
        }
        /* Retrieves attributes of the Activity and adds it to a Map */
        Map<String, String> properties = new HashMap<String, String>();
        if (userActivity.getAppId() != null) {
            properties.put(SocialImplConstants.ACTIVITY_APP_ID, userActivity.getAppId());
        }
        if (userActivity.getBody() != null) {
            properties.put(SocialImplConstants.ACTIVITY_BODY, userActivity.getBody());
        }
        if (userActivity.getBodyId() != null) {
            properties.put(SocialImplConstants.ACTIVITY_BODY_ID, userActivity.getBodyId());
        }
        if (userActivity.getExternalId() != null) {
            properties.put(SocialImplConstants.ACTIVITY_EXTERNAL_ID, userActivity.getExternalId());
        }
        if (userActivity.getId() != null) {
            properties.put(SocialImplConstants.ACTIVITY_ID, userActivity.getId());
        }
        if (userActivity.getPostedTime() != null) {
            properties.put(SocialImplConstants.ACTIVITY_POSTED_TIME,
                    userActivity.getPostedTime().toString());
        }
        if (userActivity.getPriority() != null) {
            properties.put(SocialImplConstants.ACTIVITY_PRIORITY,
                    userActivity.getPriority().toString());
        }
        if (userActivity.getStreamFaviconUrl() != null) {
            properties.put(SocialImplConstants.ACTIVITY_STREAM_FAVICON_URL,
                    userActivity.getStreamFaviconUrl());
        }
        if (userActivity.getStreamSourceUrl() != null) {
            properties.put(SocialImplConstants.ACTIVITY_STREAM_SOURCE_URL,
                    userActivity.getStreamSourceUrl());
        }
        if (userActivity.getStreamTitle() != null) {
            properties.put(SocialImplConstants.ACTIVITY_STREAM_TITLE,
                    userActivity.getStreamTitle());
        }
        if (userActivity.getStreamUrl() != null) {
            properties.put(SocialImplConstants.ACTIVITY_STREAM_URL, userActivity.getStreamUrl());
        }
        if (userActivity.getTitle() != null) {
            properties.put(SocialImplConstants.ACTIVITY_TITLE, userActivity.getTitle());
        }
        if (userActivity.getTitleId() != null) {
            properties.put(SocialImplConstants.ACTIVITY_TITLE_ID, userActivity.getTitleId());
        }
        if (userActivity.getUpdated() != null) {
            properties.put(SocialImplConstants.ACTIVITY_UPDATED,
                    userActivity.getUpdated().getTime() + "");
        }
        if (userActivity.getUrl() != null) {
            properties.put(SocialImplConstants.ACTIVITY_URL, userActivity.getUrl());
        }
        if (userActivity.getUserId() != null) {
            properties.put(SocialImplConstants.ACTIVITY_USER_ID, userActivity.getUserId());
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
     * Adds or update the attributes of the MediaItem as properties for the Media tem registry resource
     *
     * @param activityMediaItem The MediaItem object to retrieve properties
     * @param mediaItemResource The mediaitem registry  resource to add properties
     * @param isUpdate          true- if required to update the properties
     *                          false- if required to add the properties
     * @return The passed in registry resource with propteries added
     */
    private Resource getPropertyAddedMediaItemResourceObj(MediaItem activityMediaItem,
                                                          Resource mediaItemResource,
                                                          boolean isUpdate) {
        if (mediaItemResource == null || activityMediaItem == null) {
            return null;
        }
        Map<String, String> properties = new HashMap<String, String>();

        if (activityMediaItem.getMimeType() != null) {
            properties.put(SocialImplConstants.ACTIVITY_MEDIA_ITEM_MIME_TYPE,
                    activityMediaItem.getMimeType());
        }
        if (activityMediaItem.getType() != null) {
            properties.put(SocialImplConstants.ACTIVITY_MEDIA_ITEM_TYPE,
                    activityMediaItem.getType().name());
        }
        if (activityMediaItem.getThumbnailUrl() != null) {
            properties.put(SocialImplConstants.ACTIVITY_MEDIA_ITEM_THUMBNAIL_URL,
                    activityMediaItem.getThumbnailUrl());
        }
        if (activityMediaItem.getUrl() != null) {
            properties.put(SocialImplConstants.ACTIVITY_MEDIA_ITEM_URL,
                    activityMediaItem.getUrl());
        }
        attachPropertyToResource(mediaItemResource, properties, isUpdate);
        return mediaItemResource;
    }

    /**
     * Adds the properties of the activity resource as attributes to a Activity object and returns that Activity object
     *
     * @param activityResource The registry resource to retrieve properties from
     * @return An Activity object with the its attributes added
     */
    private Activity getPropertyAddedActivityObj(Resource activityResource) {
        Activity activityObj = new ActivityImpl();
        String value;
        if ((value = activityResource.getProperty(
                SocialImplConstants.ACTIVITY_APP_ID)) != null) {
            activityObj.setAppId(value);
        }
        if ((value = activityResource.getProperty(
                SocialImplConstants.ACTIVITY_BODY)) != null) {
            activityObj.setBody(value);
        }
        if ((value = activityResource.getProperty(
                SocialImplConstants.ACTIVITY_BODY_ID)) != null) {
            activityObj.setBodyId(value);
        }
        if ((value = activityResource.getProperty(
                SocialImplConstants.ACTIVITY_EXTERNAL_ID)) != null) {
            activityObj.setExternalId(value);
        }
        if ((value = activityResource.getProperty(
                SocialImplConstants.ACTIVITY_ID)) != null) {
            activityObj.setId(value);
        }
        if ((value = activityResource.getProperty(
                SocialImplConstants.ACTIVITY_UPDATED)) != null) {
            // this property holds the date in long (ms)
            Date updatedDate = new Date(Long.valueOf(value));
            activityObj.setUpdated(updatedDate);
        }
        if ((value = activityResource.getProperty(
                SocialImplConstants.ACTIVITY_POSTED_TIME)) != null) {
            activityObj.setPostedTime(Long.valueOf(value));
        }
        if ((value = activityResource.getProperty(
                SocialImplConstants.ACTIVITY_PRIORITY)) != null) {
            activityObj.setPriority(Float.valueOf(value));
        }
        if ((value = activityResource.getProperty(
                SocialImplConstants.ACTIVITY_STREAM_FAVICON_URL)) != null) {
            activityObj.setStreamFaviconUrl(value);
        }
        if ((value = activityResource.getProperty(
                SocialImplConstants.ACTIVITY_STREAM_SOURCE_URL)) != null) {
            activityObj.setStreamSourceUrl(value);
        }
        if ((value = activityResource.getProperty(
                SocialImplConstants.ACTIVITY_STREAM_TITLE)) != null) {
            activityObj.setStreamTitle(value);
        }
        if ((value = activityResource.getProperty(
                SocialImplConstants.ACTIVITY_STREAM_URL)) != null) {
            activityObj.setStreamUrl(value);
        }
        if ((value = activityResource.getProperty(
                SocialImplConstants.ACTIVITY_TITLE)) != null) {
            activityObj.setTitle(value);
        }
        if ((value = activityResource.getProperty(
                SocialImplConstants.ACTIVITY_TITLE_ID)) != null) {
            activityObj.setTitleId(value);
        }
        if ((value = activityResource.getProperty(
                SocialImplConstants.ACTIVITY_USER_ID)) != null) {
            activityObj.setUserId(value);
        }
        if ((value = activityResource.getProperty(
                SocialImplConstants.ACTIVITY_URL)) != null) {
            activityObj.setUrl(value);
        }
        return activityObj;
    }

    /**
     * Adds the properties of the mediaitem resource as attributes to a MediaItem object and returns that MediaItem object
     *
     * @param itemResource The registry resource to retrieve the mediaitem properties
     * @return A MediaItem object with its attributes added
     */
    private MediaItem getPropertiesAddedMediaItemObj(Resource itemResource) {
        MediaItem item = new MediaItemImpl();
        String value;
        if ((value = itemResource.getProperty(
                SocialImplConstants.ACTIVITY_MEDIA_ITEM_MIME_TYPE)) != null) {
            item.setMimeType(value);
        }
        if ((value = itemResource.getProperty(
                SocialImplConstants.ACTIVITY_MEDIA_ITEM_THUMBNAIL_URL)) != null) {
            item.setThumbnailUrl(value);
        }
        if ((value = itemResource.getProperty(
                SocialImplConstants.ACTIVITY_MEDIA_ITEM_URL)) != null) {
            item.setUrl(value);
        }
        if ((value = itemResource.getProperty(
                SocialImplConstants.ACTIVITY_MEDIA_ITEM_TYPE)) != null) {
            item.setType(MediaItem.Type.valueOf(value));   //TODO:?
        }
        return item;
    }

    /**
     * Retrieves the Activity object for the given userId,appId,activityId combination
     *
     * @param userId     The id of the person
     * @param appId      The id of the application
     * @param activityId The ide of the activity
     * @return An Activiy object for the passed in userId,appId,activityId combination
     * @throws RegistryException
     */

    private Activity getUserActivity(String userId, String appId, String activityId)
            throws RegistryException {
        registry = getRegistry();
        Activity userActivity;
        // retrieve activity for user {userId}

        String selfActivityResourcePath = SocialImplConstants.USER_REGISTRY_ROOT + userId +
                SocialImplConstants.ACTIVITY_PATH + appId +
                SocialImplConstants.SEPARATOR + activityId;
        Resource selfActivityResource;
        if (registry.resourceExists(selfActivityResourcePath)) {
            // requested activity exists
            selfActivityResource = registry.get(selfActivityResourcePath);
            userActivity = getPropertyAddedActivityObj(selfActivityResource);

            /* Handle media items */
            String mediaItemResourcePath = selfActivityResourcePath +
                    SocialImplConstants.ACTIVITY_MEDIA_ITEM_PATH;
            int noOfMediaItems;
            if (registry.resourceExists(mediaItemResourcePath) &&
                    registry.get(mediaItemResourcePath).getProperty(
                            SocialImplConstants.ACTIVITY_MEDIA_ITEM_NOS) != null) {
                noOfMediaItems = Integer.valueOf(registry.get(mediaItemResourcePath).
                        getProperty(SocialImplConstants.ACTIVITY_MEDIA_ITEM_NOS));
                String itemResourcePath;
                List<MediaItem> mediaItemList = new ArrayList<MediaItem>();
                for (int index = 0; index < noOfMediaItems; index++) {
                    itemResourcePath = mediaItemResourcePath +
                            SocialImplConstants.SEPARATOR + index;
                    Resource mediaItemResource;
                    if (registry.resourceExists(itemResourcePath)) {
                        mediaItemResource = registry.get(itemResourcePath);
                        // retrieve mediaItem properties
                        // add to mediaItems list
                        mediaItemList.add(getPropertiesAddedMediaItemObj(mediaItemResource));
                    }
                }
                // add the mediaItem list to the activity object
                userActivity.setMediaItems(mediaItemList);
            }


            /* Handle Template Params */
            String templateParamResourcePath = selfActivityResourcePath +
                    SocialImplConstants.
                            ACTIVITY_TEMPLATE_PARAMS_PATH;
            Resource templateParamResource;
            if (registry.resourceExists(templateParamResourcePath)) {
                templateParamResource = registry.get(templateParamResourcePath);
                Properties props = templateParamResource.getProperties();
                userActivity.setTemplateParams(new HashMap<String, String>((Map) props));
            }

        } else {
            //requested activity doesn't exist
            log.error("No activity found with id " + activityId);
            return null;

        }
        return userActivity;
    }

    /**
     * Persists/updates the Activity object as a registry resource
     *
     * @param userId       The id of the person
     * @param userActivity The Activity object to persist/update
     * @param isUpdate     true- if required to update &persist
     *                     false- if required to persist
     * @throws RegistryException
     */
    private void saveActivity(String userId, Activity userActivity, boolean isUpdate)
            throws RegistryException {
        registry = getRegistry();
        String nextActivityPath = SocialImplConstants.USER_REGISTRY_ROOT +
                userId +
                SocialImplConstants.ACTIVITY_PATH +
                userActivity.getAppId() +
                SocialImplConstants.NEXT_ACTIVITY_ID_PATH;
        /* Check for null activity id  */
        if (userActivity.getId() == null) {
            // No activity Id specified

            Resource nextActivityIdResource;
            int nextActivityId;
            if (registry.resourceExists(nextActivityPath)) {
                nextActivityIdResource = registry.get(nextActivityPath);
                if (nextActivityIdResource.getProperty(SocialImplConstants.NEXT_ACTIVITY_ID) != null) {
                    nextActivityId = Integer.valueOf(nextActivityIdResource.getProperty(SocialImplConstants.NEXT_ACTIVITY_ID));
                } else {
                    nextActivityId = 0;
                }

            } else {
                // there's no nextActivityId Resource
                nextActivityId = 0;
                nextActivityIdResource = registry.newCollection();
            }
            nextActivityIdResource.setProperty(SocialImplConstants.NEXT_ACTIVITY_ID, (nextActivityId + 1) + "");
            registry.put(nextActivityPath, nextActivityIdResource);
            userActivity.setId(nextActivityId + "");
        } else {
            // There's an activityId specified
            Resource nextActivityIdResource = null;
            int nextActivityId = -1;
            if (registry.resourceExists(nextActivityPath)) {
                nextActivityIdResource = registry.get(nextActivityPath);
                if (nextActivityIdResource.getProperty(SocialImplConstants.NEXT_ACTIVITY_ID) != null) {
                    nextActivityId = Integer.valueOf(nextActivityIdResource.getProperty(SocialImplConstants.NEXT_ACTIVITY_ID));
                }

            }

            String userActivityId = userActivity.getId();
            boolean isIntegerId = false;
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
                    userActivity.setId(nextActivityId + "");
                    nextActivityIdResource.setProperty(SocialImplConstants.NEXT_ACTIVITY_ID,
                            (nextActivityId + 1) + "");
                    registry.put(nextActivityPath, nextActivityIdResource);
                }


                /*else if (nextActivityId > userActivityIdInteger) {
                    // This userActivity's Id possibly exists
                    // So check for it
                    String activityResourcePath = SocialImplConstants.USER_REGISTRY_ROOT +
                                                  userId +
                                                  SocialImplConstants.ACTIVITY_PATH +
                                                  userActivity.getAppId();
                    Collection activityResource = null;
                    if (registry.resourceExists(activityResourcePath)) {
                       activityResource=registry.get(activityResourcePath);
                       
                    }
                }*/


            }
        }
        /* Recording Activities */
        String newActivityPath = SocialImplConstants.USER_REGISTRY_ROOT +
                userId +
                SocialImplConstants.ACTIVITY_PATH +
                userActivity.getAppId() +
                SocialImplConstants.SEPARATOR +
                userActivity.getId();
        Resource newActivityResource;
        if (registry.resourceExists(newActivityPath)) {
            newActivityResource = registry.get(newActivityPath);
        } else {
            newActivityResource = registry.newCollection();
        }

        newActivityResource = getPropertiesAddedActivityResourceObj(userActivity,
                newActivityResource, isUpdate);
        registry.put(newActivityPath, newActivityResource);
        /* Handling MediaItems */
        if (userActivity.getMediaItems() != null) {
            String resourcePath = newActivityPath +
                    SocialImplConstants.ACTIVITY_MEDIA_ITEM_PATH;
            Resource mediaResource;
            if (registry.resourceExists(resourcePath)) {
                mediaResource = registry.get(resourcePath);
            } else {
                mediaResource = registry.newCollection();
            }
            // Add the no. of mediaItems as a property to this resource
            mediaResource.addProperty(SocialImplConstants.ACTIVITY_MEDIA_ITEM_NOS,
                    userActivity.getMediaItems().size() + "");
            registry.put(resourcePath, mediaResource);
            // Add all mediaItems as resources in the path /activityId/{mediaItem}/{mediaItemId}/...
            Resource mediaItemResource;
            int index = 0;
            String itemResourcePath;
            for (MediaItem item : userActivity.getMediaItems()) {
                itemResourcePath = resourcePath + SocialImplConstants.SEPARATOR + index;

                if (registry.resourceExists(itemResourcePath)) {
                    mediaItemResource = registry.get(itemResourcePath);
                } else {
                    mediaItemResource = registry.newCollection();
                }
                mediaItemResource = getPropertyAddedMediaItemResourceObj(
                        item, mediaItemResource, isUpdate);
                registry.put(itemResourcePath, mediaItemResource);
                index++;
            }
        }
        /* Handling TemplateParams */
        if (userActivity.getTemplateParams() != null) {
            String templateParamResourcePath = newActivityPath +
                    SocialImplConstants.ACTIVITY_TEMPLATE_PARAMS_PATH;
            Resource templateParamResource;
            if (registry.resourceExists(templateParamResourcePath)) {
                templateParamResource = registry.get(templateParamResourcePath);
            } else {
                templateParamResource = registry.newCollection();
            }
            for (String param : userActivity.getTemplateParams().keySet()) {
                if (isUpdate) {
                    String oldValue = templateParamResource.getProperty(param);
                    templateParamResource.editPropertyValue(
                            param, oldValue, userActivity.getTemplateParams().get(param));
                } else {
                    templateParamResource.addProperty(param,
                            userActivity.getTemplateParams().get(param));
                }
            }
            registry.put(templateParamResourcePath, templateParamResource);
        }


    }

    /**
     * A Comparator to sort activities according to posted time
     */
    static class ActivityComparator implements Comparator<Activity>, Serializable {

        public int compare(Activity activity1, Activity activity2) {
            if (activity1.getPostedTime() == null) {
                activity1.setPostedTime(System.currentTimeMillis());
            }
            if (activity2.getPostedTime() == null) {
                activity2.setPostedTime(System.currentTimeMillis());
            }

            if (activity1.getPostedTime() < activity2.getPostedTime()) {
                return 1;
            } else if (activity1.getPostedTime() > activity2.getPostedTime()) {
                return -1;
            }
            return 0;
        }

    }
}