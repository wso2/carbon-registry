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
import org.wso2.carbon.registry.api.Association;
import org.wso2.carbon.registry.common.ResourceData;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.metadata.exception.MetadataException;
import org.wso2.carbon.registry.metadata.lifecycle.StateMachineLifecycle;
import org.wso2.carbon.registry.metadata.provider.MetadataProvider;
import org.wso2.carbon.registry.metadata.version.HTTPServiceVersionV1;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractBase {

    protected String name;
    protected String uuid;
    protected Map<String,List<String>> propertyBag;
    protected Registry registry;
    private static final Log log = LogFactory.getLog(AbstractBase.class);
    protected StateMachineLifecycle lifecycle;
    protected boolean isVersionType;

    //    = new StringBuilder(Constants.BASE_STORAGE_PATH).append(mediaType.split("/")[0].replaceAll(".","/")).append("/").append(mediaType.split("/")).toString();


    public AbstractBase(String name,boolean isVersionType,Registry registry) throws MetadataException {
        this.name = name;
        this.uuid = Util.getNewUUID();
        this.isVersionType = isVersionType;
        this.registry = registry;
        this.propertyBag = new HashMap<String, List<String>>();
    }

    public AbstractBase(String name,String uuid,boolean isVersionType,Map<String,List<String>> propertyBag,Registry registry) throws MetadataException {
        this.name = name;
        this.uuid = uuid;
        this.propertyBag = propertyBag;
        this.isVersionType = isVersionType;
        this.registry = registry;
    }


    public void setProperty(String key, String value) {
        if(propertyBag.get(key) == null) {
            List<String> list = new ArrayList<String>();
            list.add(value);
            propertyBag.put(key,list);
        } else {
            propertyBag.get(key).add(value);
        }

    }

    public void removeProperty(String key) {
        propertyBag.remove(key);
    }

    public String getProperty(String key) {
        List<String> value = propertyBag.get(key);
        return value != null?propertyBag.get(key).get(0):null;
    }

    protected static void add(Registry registry,Base metadata,MetadataProvider provider,String path) throws MetadataException {
        try {
            Resource resource = provider.buildResource(metadata, registry.newResource());
            putResource(registry, path, resource);
        }catch (RegistryException e){
            throw new MetadataException(e.getMessage(),e);
        }
    }

    protected static void update(Registry registry,Base metadata,MetadataProvider provider,String path) throws MetadataException {
        Resource resource = provider.buildResource(metadata,getResource(registry,metadata.getUUID()));
        putResource(registry,path,resource);
    }

    /**
     * Deletes the meta data instance that represents from the given UUID
     * @param uuid  UUID of the instance
     */
    protected static void deleteResource(Registry registry,String uuid) throws MetadataException{
         try {
             String path = Util.getMetadataPath(uuid, registry);
             if (registry.resourceExists(path)) {
                 registry.delete(path);
             } else {
                 log.error("Metadata instance " + uuid + " does not exists");
             }
         } catch (RegistryException e){
            throw new MetadataException(e.getMessage(),e);
         }
    }

    /**
     *
     * @return all meta data instances and their children that denotes from this particular media type
     */
    protected static List<Base> getAll(Registry registry, MetadataProvider provider,String mt) throws MetadataException {
        List<Base> baseResult = new ArrayList<Base>();
        Map<String,String> criteria = new HashMap<String, String>();
        criteria.put("mediaType",mt);
        try {
        ResourceData[] results = Util.getAttributeSearchService().search(criteria);
        for(ResourceData resourceData:results){
           String path =  RegistryUtils.getRelativePathToOriginal(resourceData.getResourcePath(), RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);
           if(registry.resourceExists(path)){
              Resource resource = registry.get(path);
              baseResult.add(provider.get(resource,registry));
           }
        }
        } catch (RegistryException e){
            throw new MetadataException(e.getMessage(),e);
        }
        return baseResult;
    }

    /**
     *  Search all meta data instances of this particular type with the given search attributes
     * @param criteria Key value map that has search attributes
     * @return
     */
    protected static List<Base> find(Registry registry,Map<String,String> criteria,MetadataProvider provider,String mt) throws MetadataException {
        if(criteria != null && criteria.get("mediaType") == null){
             criteria.put("mediaType",mt);
        }
        List<Base> baseResult = new ArrayList<Base>();
        try {
        ResourceData[] results = Util.getAttributeSearchService().search(criteria);
        for(ResourceData resourceData:results){
            String path =  RegistryUtils.getRelativePathToOriginal(resourceData.getResourcePath(), RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);
            if(registry.resourceExists(path)){
                Resource resource = registry.get(path);
                baseResult.add(provider.get(resource,registry));
            }
        }
        } catch (RegistryException e){
            throw new MetadataException(e.getMessage(),e);
        }
        return baseResult;
    }

    /**
     * Returns the meta data instance that can be identified by the given UUID
     * @param uuid - UUID of the metadata insatnce
     * @return meta data from the UUID
     */
    protected static Base get(Registry registry,String uuid,MetadataProvider provider) throws MetadataException {
        return provider.get(getResource(registry,uuid),registry);
    }

    protected static Resource getResource(Registry registry,String uuid) throws MetadataException {
        String path = Util.getMetadataPath(uuid, registry);
        if(path == null){
            return null;
        }
        try {
        if(registry.resourceExists(path)) {
            return registry.get(path);
        } else {
            log.error("Metadata instance " + uuid + " does not exists at path "+path);
            return null;
        }
        } catch (RegistryException e){
            throw new MetadataException(e.getMessage(),e);
        }
    }



    protected static void putResource(Registry registry,String path, Resource resource) throws MetadataException {
        boolean succeeded = false;
        try {
            registry.beginTransaction();
            if(registry.resourceExists(path)){
                throw new MetadataException("Metadata instance " + resource.getUUID() + " already exists at "+path);
            }
            registry.put(path, resource);
            succeeded = true;
        }
        catch (RegistryException e) {
            throw new MetadataException("Failed to persist the resource");
        } finally {
            if (succeeded) {
                try {
                    registry.commitTransaction();
                } catch (RegistryException e) {
                    log.error("Error in commiting transaction for the meta data instance "+resource.getUUID(), e);
                }
            } else {
                try {
                    registry.rollbackTransaction();
                } catch (RegistryException e) {
                    log.error("Error in rollbacking transaction for the meta data instance "+resource.getUUID(), e);

                }
            }
        }
    }

    protected ArrayList<Base> getAllVersions(String uuid,String versionMediaType) throws MetadataException {
        //    Can do the same from the index search .
        ArrayList<Base> list = new ArrayList<Base>();
        try {
            for (Association as : getAssociations(registry, uuid, Constants.CHILD_VERSION)) {
                if (registry.resourceExists(as.getDestinationPath())) {
                    Resource r = registry.get(as.getDestinationPath());
                    list.add(Util.getProvider(versionMediaType).get(r, registry));
                }
            }
        } catch (RegistryException e){
            throw new MetadataException(e.getMessage(),e);
        }
        return list;
    }

    protected  static Association [] getAssociations(Registry registry,String sourceUUID,String type) throws MetadataException {
        Association[] associations = null;
        try{
     associations = registry.getAssociations(Util.getMetadataPath(sourceUUID, registry), type);
        } catch (RegistryException e){
            throw new MetadataException(e.getMessage(),e);
        }
      return associations;
    }

    public void attachLifecycle(String name) throws MetadataException {
        try {
        this.lifecycle = new StateMachineLifecycle(registry,name,uuid);
        registry.associateAspect(Util.getMetadataPath(uuid,registry),name);
        } catch (RegistryException e){
            throw new MetadataException(e.getMessage(),e);
        }
    }

    public void detachLifecycle() throws MetadataException {
        try {
       registry.removeAspect(Util.getMetadataPath(uuid,registry));
        } catch (RegistryException e){
            throw new MetadataException(e.getMessage(),e);
        }
    }

    public StateMachineLifecycle getLifecycle() {
        return this.lifecycle;
    }

    public Map<String, List<String>> getPropertyBag() {
        return propertyBag;
    }


}
