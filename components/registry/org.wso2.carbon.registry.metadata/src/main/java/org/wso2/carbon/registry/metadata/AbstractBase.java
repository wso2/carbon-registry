package org.wso2.carbon.registry.metadata;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.api.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
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
    protected Map<String,String> propertyBag;
    protected Registry registry;
    private static final Log log = LogFactory.getLog(AbstractBase.class);
    protected StateMachineLifecycle lifecycle;
    protected boolean isVersionType;

    //    = new StringBuilder(Constants.BASE_STORAGE_PATH).append(mediaType.split("/")[0].replaceAll(".","/")).append("/").append(mediaType.split("/")).toString();


    public AbstractBase(String name,boolean isVersionType,Registry registry) throws RegistryException {
        this.name = name;
        this.uuid = Util.getNewUUID();
        this.isVersionType = isVersionType;
        this.registry = registry;
        this.propertyBag = new HashMap<String, String>();
    }

    public AbstractBase(String name,String uuid,boolean isVersionType,Map<String,String> propertyBag,Registry registry) throws RegistryException {
        this.name = name;
        this.uuid = uuid;
        this.propertyBag = propertyBag;
        this.isVersionType = isVersionType;
        this.registry = registry;
    }

    protected static void add(Registry registry,Base metadata,MetadataProvider provider,String path) throws RegistryException {
        Resource resource = provider.buildResource(metadata,registry.newResource());
        putResource(registry,path, resource);
    }

    protected static void update(Registry registry,Base metadata,MetadataProvider provider,String path) throws RegistryException {
        Resource resource = provider.buildResource(metadata,getResource(registry,metadata.getUUID()));
        putResource(registry,path,resource);
    }

    /**
     * Deletes the meta data instance that represents from the given UUID
     * @param uuid  UUID of the instance
     */
    protected static void deleteResource(Registry registry,String uuid) throws RegistryException{
//        TODO remove from index
        String path = Util.getMetadataPath(uuid, registry);
        if(registry.resourceExists(path)) {
            registry.delete(path);
        } else {
            log.error("Metadata instance " + uuid + " does not exists");
        }
    }

    /**
     *
     * @return all meta data instances and their children that denotes from this particular media type
     */
    protected static Base[] getAll(Registry registry,MetadataProvider provider) throws RegistryException {
//        TODO get from index
        return null;
    }

    /**
     *  Search all meta data instances of this particular type with the given search attributes
     * @param criteria Key value map that has search attributes
     * @return
     */
    protected static Base[] find(Registry registry,Map<String,String> criteria,MetadataProvider provider) throws RegistryException {
        // TODO get from index
        return null;
    }

    /**
     * Returns the meta data instance that can be identified by the given UUID
     * @param uuid - UUID of the metadata insatnce
     * @return meta data from the UUID
     */
    protected static Base get(Registry registry,String uuid,MetadataProvider provider) throws RegistryException {
        return provider.get(getResource(registry,uuid),registry);
    }

    protected static Resource getResource(Registry registry,String uuid) throws RegistryException {
        String path = Util.getMetadataPath(uuid, registry);
        if(path == null){
            return null;
        }
        if(registry.resourceExists(path)) {
            return registry.get(path);
        } else {
            log.error("Metadata instance " + uuid + " does not exists at path "+path);
            return null;
        }
    }



    protected static void putResource(Registry registry,String path, Resource resource) throws RegistryException {
        boolean succeeded = false;
        try {
            registry.beginTransaction();
            if(registry.resourceExists(path)){
                throw new RegistryException("Metadata instance " + resource.getUUID() + " already exists at "+path);
            }
            registry.put(path, resource);
            succeeded = true;
        }
        catch (RegistryException e) {
            throw new RegistryException("Failed to persist the resource");
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

    protected ArrayList<Base> getAllVersions(String uuid,String versionMediaType) throws RegistryException {
        //    Can do the same from the index search .
        ArrayList<Base> list = new ArrayList<Base>();
        for(Association as:getAssociations(registry,uuid,Constants.CHILD_VERSION)){
            if(registry.resourceExists(as.getDestinationPath())) {
                Resource r = registry.get(as.getDestinationPath());
                list.add(Util.getProvider(versionMediaType).get(r,registry));
            }
        }

//        Base[] arr = new Base[list.size()];
//        arr = list.toArray(arr);
        return list;
    }

    protected  static Association [] getAssociations(Registry registry,String sourceUUID,String type) throws RegistryException {
     return registry.getAssociations(Util.getMetadataPath(sourceUUID, registry), type);
    }

    public void attachLifecycle(String name) throws RegistryException {
        this.lifecycle = new StateMachineLifecycle(registry,name,uuid);
        registry.associateAspect(Util.getMetadataPath(uuid,registry),name);
    }

    public void detachLifecycle() throws RegistryException {
       registry.removeAspect(Util.getMetadataPath(uuid,registry));
    }

    public StateMachineLifecycle getLifecycle() {
        return this.lifecycle;
    }

    public Map<String, String> getPropertyBag() {
        return propertyBag;
    }


}
