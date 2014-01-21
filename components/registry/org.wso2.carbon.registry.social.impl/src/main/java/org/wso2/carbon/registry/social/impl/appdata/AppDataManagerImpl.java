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
package org.wso2.carbon.registry.social.impl.appdata;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.social.api.SocialDataException;
import org.wso2.carbon.registry.social.api.appdata.AppDataManager;
import org.wso2.carbon.registry.social.impl.SocialImplConstants;
import org.wso2.carbon.registry.social.impl.internal.SocialDSComponent;
import org.wso2.carbon.registry.social.impl.people.relationship.RelationshipManagerImpl;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

/**
 * An implementation of the {@link org.wso2.carbon.registry.social.api.appdata.AppDataManager} interface
 * <p>
 * This implementation uses the {@link org.wso2.carbon.registry.core.Registry} to store app data
 * </p>
 * <p>
 * The app data  is stored as a {@link org.wso2.carbon.registry.core.Registry}  {@link org.wso2.carbon.registry.core.Resource}
 * The key-value pairs are stored as resource properties
 * </p>
 * <p>
 * <p/>
 * Each AppData key-value is grouped according to the appId of it
 * </p>
 * <p>
 * Resource path : /{AppData}/{appId}/[key][value]
 * </p>
 */
public class AppDataManagerImpl implements AppDataManager {
    private static Log log = LogFactory.getLog(AppDataManagerImpl.class);
    private Registry registry;
    /* Setting the Registry object */

    public void setRegistry(Registry reg) {
        this.registry = reg;
    }

    /* The Registry object used throughout */

    public Registry getRegistry() throws RegistryException {
        if (this.registry != null) {
            return this.registry;
        } else {
            return SocialDSComponent.getRegistry();
        }
    }

    /**
     * Retrieves app data for the specified user list and group
     *
     * @param userIds A set of userIds whose app data to be retrieved
     * @param groupId The group
     * @param appId   The app
     * @param fields  The fields to return
     * @return A collection of appData for the given user list and group
     * @throws SocialDataException
     */
    public Map<String, Map<String, String>> getPersonData(String[] userIds, String groupId,
                                                          String appId, Set<String> fields)
            throws SocialDataException {

        List<String> userIdsToFetch = new ArrayList<String>();

        // Check for groupId
        if (SocialImplConstants.GROUP_ID_FRIENDS.equals(groupId)) {
            for (String id : userIds) {
                String[] friendsList = new RelationshipManagerImpl().getRelationshipList(id);
                for (String friend : friendsList) {
                    userIdsToFetch.add(friend);
                }
            }
            userIds = new String[userIdsToFetch.size()];
            userIds = userIdsToFetch.toArray(userIds);
        }
        Map<String, Map<String, String>> personDataMap = new HashMap<String, Map<String, String>>();
        for (String id : userIds) {
            try {
                Map<String, String> data = getAppData(id, appId, fields);
                if (data == null) {
                  //  log.error("No data found for the user " + id);
                    return new HashMap<String, Map<String, String>>();
                }
                personDataMap.put(id, data);
            }
            catch (RegistryException e) {
                log.error(e.getMessage(), e);
                throw new SocialDataException(
                        "Error while retrieving app data with id " + appId + " for user " + id, e);
            }

        }
        return personDataMap;
    }

    /**
     * Deletes data for the specified user and group
     *
     * @param userId  The userId of the person whose app data to be removed
     * @param groupId The group
     * @param appId   The app
     * @param fields  The fields to delete. Empty implies all
     * @throws SocialDataException
     */

    public void deletePersonData(String userId, String groupId, String appId, Set<String> fields)
            throws SocialDataException {
        try {
            registry = getRegistry();
            Resource appDataResource;
            String appDataResourcePath = SocialImplConstants.APP_DATA_REGISTRY_ROOT +
                                         SocialImplConstants.SEPARATOR + appId +
                                         SocialImplConstants.SEPARATOR + userId;
            if (registry.resourceExists(appDataResourcePath)) {
                appDataResource = registry.get(appDataResourcePath);
                for (String key : fields) {
                    appDataResource.removeProperty(key);
                }
                registry.put(appDataResourcePath, appDataResource);
            }
        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new SocialDataException(
                    "Error while deleting app data with id " + appId + " for user " + userId, e);
        }
    }

    /**
     * Updates app data for the specified user and group with the new values
     *
     * @param userId  The userId of the person whose app data to be modified
     * @param groupId The group
     * @param appId   The app
     * @param fields  The fields to update. Empty implies all
     * @param values  The new values to set
     * @throws SocialDataException
     */
    public void updatePersonData(String userId, String groupId, String appId, Set<String> fields,
                                 Map<String, String> values) throws SocialDataException {
        Set<String> fieldsToDelete = new TreeSet<String>();
        // If a field is in the param list but not in the map, that means it is a delete
        // Retrieve the fields to delete
        for (String field : fields) {
            if (!values.containsKey(field)) {
                fieldsToDelete.add(field);
            }
        }
        List<String> userIdsToFetch = new ArrayList<String>();

        // Check for groupId
        if (SocialImplConstants.GROUP_ID_FRIENDS.equals(groupId)) {
            String[] friendsList = new RelationshipManagerImpl().getRelationshipList(userId);
            for (String friend : friendsList) {
                userIdsToFetch.add(friend);
            }
        }
        else{
            userIdsToFetch.add(userId);
        }
        for (String id : userIdsToFetch) {
            // Update the fields
            savePersonAppData(userId, appId, values, true);
            // Deletes the fields which are not in the Map but in the param list
            deletePersonData(userId, groupId, appId, fieldsToDelete);
        }
    }

    /**
     * Save app data for the specified user with the given values
     *
     * @param userId The userId of the person whose app data to be modified
     * @param appId  The app
     * @param values The new values to set
     * @throws SocialDataException
     */
    public void savePersonData(String userId, String appId, Map<String, String> values)
            throws SocialDataException {
        savePersonAppData(userId, appId, values, false);

    }

    /**
     * Adds/updates the Map of key-value pairs (appData) to the registry resource
     *
     * @param appDataResource The registry resource to add properties
     * @param values          The Map of key-value appData
     * @param isUpdate        true- if required to update the properties
     *                        false- if required to add the properties
     * @return The registry resource with the appData added as properties
     */

    private Resource getAppDataAddedRegistryResource(Resource appDataResource,
                                                     Map<String, String> values, boolean isUpdate) {
        for (Map.Entry<String, String> e : values.entrySet()) {         /* for each key in the map */
            if (e.getValue() != null) {
                /* if (isUpdate) {*/
                String oldValue = appDataResource.getProperty(e.getKey());
                if (oldValue != null) {
                    appDataResource.editPropertyValue(e.getKey(), oldValue, e.getValue());  /* edit properties to the resource */
                }
                /* } else {*/
                else {
                    appDataResource.addProperty(e.getKey(), e.getValue());  /* add properties to the resource */
                }
                /* }*/
            }
        }
        return appDataResource;
    }

    /**
     * Persists/update person Appdata as registry reource
     *
     * @param userId   The id of the person to whom the appData belongs to
     * @param appId    The id of the application to which the appData belongs to
     * @param values   The appData key-value pairs
     * @param isUpdate True, if required to update the properties. Else false
     * @throws SocialDataException
     */
    private void savePersonAppData(String userId, String appId, Map<String, String> values,
                                   boolean isUpdate)
            throws SocialDataException {
        try {
            registry = getRegistry();
            Resource appDataResource;
            String appDataResourcePath = SocialImplConstants.APP_DATA_REGISTRY_ROOT +
                                         SocialImplConstants.SEPARATOR + appId +
                                         SocialImplConstants.SEPARATOR + userId;
            if (registry.resourceExists(appDataResourcePath)) {
                appDataResource = registry.get(appDataResourcePath);
            } else {
                appDataResource = registry.newCollection();
            }
            appDataResource = getAppDataAddedRegistryResource(appDataResource, values, isUpdate);
            registry.put(appDataResourcePath, appDataResource);

        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new SocialDataException(
                    "Error while saving app data with id " + appId + " for user " + userId, e);
        }
    }

    /**
     * Fetches AppData for the given userId,appId,fields collection
     *
     * @param userId The id of the person to fetch the AppData
     * @param appId  The appId to of the AppData to fetch
     * @param fields The fields of AppData to fetch
     * @return A Map<String,String> of AppData values
     * @throws RegistryException
     */
    private Map<String, String> getAppData(String userId, String appId, Set<String> fields)
            throws RegistryException {
        Map<String, String> appDataMap = new HashMap<String, String>();
        String appDataPath = SocialImplConstants.APP_DATA_REGISTRY_ROOT +
                             SocialImplConstants.SEPARATOR + appId + SocialImplConstants.SEPARATOR +
                             userId;
        Resource appDataResource;

        registry = getRegistry();
        if (registry.resourceExists(appDataPath)) {
            appDataResource = registry.get(appDataPath);
            if (fields != null && fields.size()>0) {
                for (String key : fields) {
                    String value;
                    if ((value = appDataResource.getProperty(key)) != null) {
                        appDataMap.put(key, value);
                    }
                }
            } else {
                //Handle when fields is null -> get All properties
                // TODO: refactor code
                Properties props = appDataResource.getProperties();

                for (Enumeration propKeys = props.keys(); propKeys.hasMoreElements();) {
                    String key = propKeys.nextElement().toString();
                    String propValue=props.get(key).toString();
                    //TODO: Re-write this code
                    appDataMap.put(key, propValue.substring(1,propValue.length()-1));
                }
            }
        } else {
            return null;
        }

        return appDataMap;
    }
}
