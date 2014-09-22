/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.registry.metadata;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.common.ResourceData;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.metadata.exception.MetadataException;
import org.wso2.carbon.registry.metadata.lifecycle.StateMachineLifecycle;
import org.wso2.carbon.registry.metadata.provider.version.VersionBaseProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class VersionBase {

    protected String name;
    protected String uuid;
    protected String mediaType;
    protected Map<String, List<String>> propertyBag;
    protected Map<String, List<String>> attributeMap;
    protected Registry registry;
    private static final Log log = LogFactory.getLog(VersionBase.class);
    protected StateMachineLifecycle lifecycle;
    protected final String rootStoragePath;
    private VersionBaseProvider provider;
    private String baseUUID;
    private String baseName;

    public VersionBase(String mediaType, String name, Registry registry) throws MetadataException {
        this.mediaType = mediaType;
        this.provider = Util.getVersionBaseProvider(mediaType);
        this.name = name;
        this.uuid = Util.getNewUUID();
        this.registry = registry;
        this.propertyBag = new HashMap<String, List<String>>();
        this.attributeMap = new HashMap<String, List<String>>();
        this.rootStoragePath = Constants.BASE_STORAGE_PATH
                + mediaType.split(";")[0].replaceAll("\\+", ".").replaceAll("\\.", "/").replaceAll("//","/")
                + "/v"
                + mediaType.split(";")[1].split("=")[1];
    }

    public VersionBase(String mediaType, String name, String uuid, String baseName,String baseUUID, Map<String, List<String>> propertyBag, Map<String, List<String>> attributeMap, Registry registry) throws MetadataException {
        this.mediaType = mediaType;
        this.name = name;
        this.uuid = uuid;
        this.baseName = baseName;
        this.baseUUID = baseUUID;
        this.propertyBag = propertyBag;
        this.attributeMap=attributeMap;
        this.registry = registry;
        this.rootStoragePath = Constants.BASE_STORAGE_PATH
                + mediaType.split(";")[0].replaceAll("\\+", ".").replaceAll("\\.", "/").replaceAll("//","/")
                + "/v"
                + mediaType.split(";")[1].split("=")[1];
    }

    /**
     * @return the UUID of the meta data instance
     */
    public String getUUID() throws MetadataException {
        return uuid;
    }

    /**
     * @return the human readable name that is given to the meta data instance
     */
    public String getName() throws MetadataException{
       return name;
    }

    /**
     * @return media type of the meta data instance that uniquely identifies the type of this instance
     */
    public String getMediaType() throws MetadataException{
       return mediaType;
    }


    public void setBaseUUID(String name) {
        this.baseUUID = name;
    }

    public String getBaseUUID() {
        return baseUUID;
    }

    public void setBaseName(String name) {
        this.baseName = name;
    }

    public String getBaseName() {
        return baseName;
    }


    public String getRootStoragePath() {
        return rootStoragePath;
    }

    /**
     * This is the property bag
     *
     * @param key   - property name
     * @param value - property value(single valued property)
     */
    public void setProperty(String key, String value) {
            List<String> list = new ArrayList<String>();
            list.add(value);
            propertyBag.put(key, list);
    }

    /**
     * Removes the property from this instance
     *
     * @param key name of the property
     */
    public void removeProperty(String key) {
            propertyBag.remove(key);
    }

    /**
     * Removes the property from this instance
     *
     * @param key name of the property
     * @return value of the property
     */
    public String getProperty(String key) {
        List<String> value = propertyBag.get(key);
        return value != null ? propertyBag.get(key).get(0) : null;
    }

    protected static String generateMetadataStoragePath(String name, String version, String rootStoragePath) {
        return rootStoragePath + "/" + name + "/" + version;
    }

    /**
     * Deletes the meta data instance that represents from the given UUID
     *
     * @param uuid UUID of the instance
     */
    public static void delete(Registry registry, String uuid) throws MetadataException {
        try {
            String path = Util.getMetadataPath(uuid, registry);
            if (registry.resourceExists(path)) {
                registry.delete(path);
            } else {
                log.error("Metadata instance " + uuid + " does not exists");
            }
        } catch (RegistryException e) {
            throw new MetadataException(e.getMessage(), e);
        }
    }

    protected static void add(Registry registry, VersionBase metadata, String path) throws MetadataException {
        try {
            Resource resource = Util.getVersionBaseProvider(metadata.getMediaType()).buildResource(metadata, registry.newResource());
            putResource(registry, path, resource);

            Util.createAssociation(registry, metadata.getBaseUUID(), metadata.getUUID(), Constants.ASSOCIATION_CHILD_VERSION);
            Util.createAssociation(registry, metadata.getUUID(), metadata.getBaseUUID(), Constants.ASSOCIATION_VERSION_OF);

        } catch (RegistryException e) {
            throw new MetadataException(e.getMessage(), e);
        }
    }

    protected static void update(Registry registry, VersionBase metadata, String path) throws MetadataException {
        Resource resource = Util.getVersionBaseProvider(metadata.getMediaType()).buildResource(metadata, getResource(registry, metadata.getUUID()));
        updateResource(registry, path, resource);
    }

    /**
     * @return all meta data instances and their children that denotes from this particular media type
     */
    protected static List<VersionBase> getAll(Registry registry, String mt) throws MetadataException {
        List<VersionBase> baseResult = new ArrayList<VersionBase>();
        Map<String, String> criteria = new HashMap<String, String>();
        criteria.put(Constants.ATTRIBUTE_MEDIA_TYPE, mt);
        try {
            ResourceData[] results = Util.getAttributeSearchService().search(criteria);
            for (ResourceData resourceData : results) {
                String path = RegistryUtils.getRelativePathToOriginal(resourceData.getResourcePath(), RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);
                if (registry.resourceExists(path)) {
                    Resource resource = registry.get(path);
                    baseResult.add(Util.getVersionBaseProvider(mt).get(resource, registry));
                }
            }
        } catch (RegistryException e) {
            throw new MetadataException(e.getMessage(), e);
        }
        return baseResult;
    }

    /**
     * Search all meta data instances of this particular type with the given search attributes
     *
     * @param criteria Key value map that has search attributes
     * @return
     */
    protected static List<VersionBase> find(Registry registry, Map<String, String> criteria, String mt) throws MetadataException {
        if (criteria != null && criteria.get(Constants.ATTRIBUTE_MEDIA_TYPE) == null) {
            criteria.put(Constants.ATTRIBUTE_MEDIA_TYPE, mt);
        }
        List<VersionBase> baseResult = new ArrayList<VersionBase>();
        try {
            ResourceData[] results = Util.getAttributeSearchService().search(criteria);
            for (ResourceData resourceData : results) {
                String path = RegistryUtils.getRelativePathToOriginal(resourceData.getResourcePath(), RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);
                if (registry.resourceExists(path)) {
                    Resource resource = registry.get(path);
                    baseResult.add(Util.getVersionBaseProvider(mt).get(resource, registry));
                }
            }
        } catch (RegistryException e) {
            throw new MetadataException(e.getMessage(), e);
        }
        return baseResult;
    }

    /**
     * Returns the meta data instance that can be identified by the given UUID
     *
     * @param uuid - UUID of the metadata insatnce
     * @return meta data from the UUID
     */
    protected static VersionBase get(Registry registry, String uuid, String mt) throws MetadataException {
        return  Util.getVersionBaseProvider(mt).get(getResource(registry, uuid), registry);
    }

    public void attachLifecycle(String name) throws MetadataException {
        try {
            this.lifecycle = new StateMachineLifecycle(registry, name, uuid);
            registry.associateAspect(Util.getMetadataPath(uuid, registry), name);
        } catch (RegistryException e) {
            throw new MetadataException(e.getMessage(), e);
        }
    }

    public void detachLifecycle() throws MetadataException {
        try {
            registry.removeAspect(Util.getMetadataPath(uuid, registry));
        } catch (RegistryException e) {
            throw new MetadataException(e.getMessage(), e);
        }
    }

    public StateMachineLifecycle getLifecycle() {
        return this.lifecycle;
    }

    public Map<String, List<String>> getPropertyBag() {
        return propertyBag;
    }

    public Map<String, List<String>> getAttributeMap() {
        return attributeMap;
    }

    private static Resource getResource(Registry registry, String uuid) throws MetadataException {
        String path = Util.getMetadataPath(uuid, registry);
        if (path == null) {
            return null;
        }
        try {
            if (registry.resourceExists(path)) {
                return registry.get(path);
            } else {
                log.error("Metadata instance " + uuid + " does not exists at path " + path);
                return null;
            }
        } catch (RegistryException e) {
            throw new MetadataException(e.getMessage(), e);
        }
    }


    private static void updateResource(Registry registry, String path, Resource resource) throws MetadataException {
        boolean succeeded = false;
        try {
            registry.beginTransaction();
            if (!registry.resourceExists(path)) {
                throw new MetadataException("Metadata instance " + resource.getUUID() + " does not exists at " + path +" for update");
            }
            registry.put(path, resource);
            succeeded = true;
        } catch (RegistryException e) {
            throw new MetadataException("Failed to update the resource");
        } finally {
            if (succeeded) {
                try {
                    registry.commitTransaction();
                } catch (RegistryException e) {
                    log.error("Error in commiting transaction for the meta data instance " + resource.getUUID(), e);
                }
            } else {
                try {
                    registry.rollbackTransaction();
                } catch (RegistryException e) {
                    log.error("Error in rollbacking transaction for the meta data instance " + resource.getUUID(), e);

                }
            }
        }
    }

    private static void putResource(Registry registry, String path, Resource resource) throws MetadataException {
        boolean succeeded = false;
        try {
            registry.beginTransaction();
            if (registry.resourceExists(path)) {
                throw new MetadataException("Metadata instance " + resource.getUUID() + " already exists at " + path);
            }
            registry.put(path, resource);
            succeeded = true;
        } catch (RegistryException e) {
            throw new MetadataException("Failed to persist the resource");
        } finally {
            if (succeeded) {
                try {
                    registry.commitTransaction();
                } catch (RegistryException e) {
                    log.error("Error in commiting transaction for the meta data instance " + resource.getUUID(), e);
                }
            } else {
                try {
                    registry.rollbackTransaction();
                } catch (RegistryException e) {
                    log.error("Error in rollbacking transaction for the meta data instance " + resource.getUUID(), e);

                }
            }
        }
    }

}
