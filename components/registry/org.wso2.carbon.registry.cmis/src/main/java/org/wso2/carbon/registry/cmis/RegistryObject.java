/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.cmis;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.exceptions.*;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.*;
import org.apache.chemistry.opencmis.commons.impl.server.ObjectInfoImpl;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.wso2.carbon.registry.cmis.util.CMISConstants;
import org.wso2.carbon.registry.cmis.util.CommonUtil;
import org.wso2.carbon.registry.cmis.util.PropertyHelper;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.cmis.impl.FolderTypeHandler;


import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.*;

/**
 *  This class wraps a registry object along with other related type/path manager instances
 *  Act as the super class for Other Document/Folder classes.
 */
public abstract class RegistryObject {

    private static final Logger log = LoggerFactory.getLogger(RegistryObject.class);

    private final Registry repository;
    private Resource resource;
    protected final RegistryTypeManager typeManager;
    protected final PathManager pathManager;


    /**
     * Create a new instance wrapping a Registry <code>node</code>.
     *
     * @param resource  the Registry <code>node</code> to represent
     * @param typeManager
     * @param pathManager
     * //@param typeHandlerManager
     */
    protected RegistryObject(Registry repository,Resource resource, RegistryTypeManager typeManager, PathManager pathManager) {
        this.resource = resource;
        this.typeManager = typeManager;
        this.pathManager = pathManager;
        this.repository = repository;
        //this.typeHandlerManager = typeHandlerManager;
    }

    /**
     * @return  the Registry <code>node</code> represented by this instance
     */
    public Resource getNode() {
        return resource;

    }

    public void setNode(Resource node){
        resource = node;
    }
    
    protected Registry getRepository(){
    	return this.repository;
    }
    
    /**
     * @return  the name of the CMIS object represented by this instance
     * @throws  CmisRuntimeException
     */
    public String getName() {
        try {
            return getNodeName();
        }
        catch (RegistryException e) {
            String msg = "Unable to get the name of CMIS object";
            log.error(msg, e);
            throw new CmisRuntimeException(msg, e);
        }
    }
    
    /**
     * @return  the id of the CMIS object represented by this instance
     * @throws  CmisRuntimeException
     */
    public String getId() {
        try {
            return getObjectId();
        }
        catch (RegistryException e) {
            String msg = "Failed to get the id of the CMIS object";
            log.error(msg, e);
            throw new CmisRuntimeException(msg, e);
        }
    }
    
    /**
     * @return  the typeId of the CMIS object represented by this instance
     */
    public String getTypeId() {
        return getTypeIdInternal();
    }

    /**
     * @return  <code>true</code> if this instance represent the root of the CMIS folder hierarchy.
     */
    public boolean isRoot() {
        return pathManager.isRoot(resource);
    }

    /**
     * @return  <code>true</code> if this instance represents a cmis:document type
     */
    public boolean isDocument() {
        return BaseTypeId.CMIS_DOCUMENT == getBaseTypeId();
    }

    /**
     * @return  <code>true</code> if this instance represents a cmis:folder type
     */
    public boolean isFolder() {
        return BaseTypeId.CMIS_FOLDER == getBaseTypeId();
    }

    /**
     * @return  <code>true</code> if this instance represents a versionable CMIS object
     */
    public boolean isVersionable() {
        return true;
    }
    
    /**
     * @return  this instance as a <code>RegistryDocument</code>
     * @throws CmisConstraintException if <code>this.isDocument() == false</code>
     */
    public RegistryDocument asDocument() {
        if (isDocument()) {
            return (RegistryDocument) this;
        } else {
            throw new CmisConstraintException("Not a document: " + this);
        }
    }

    /**
     * @return  this instance as a <code>RegistryFolder</code>
     * @throws CmisConstraintException if <code>this.isFolder() == false</code>
     */
    public RegistryFolder asFolder() {
        if (isFolder()) {
            return (RegistryFolder) this;
        } else {
            throw new CmisObjectNotFoundException("Not a folder: " + this);
        }
    }

    /**
     * @return  this instance as a <code>RegistryVersionBase</code>
     * @throws CmisConstraintException if <code>this.isVersionable() == false</code>
     */
    public RegistryVersionBase asVersion() {
        // TODO Removed  isVersionable() check here as it always returns true now. Any changes are
        // done to isVersionable() just make sure to change this method as necessary to reflect those..

        return (RegistryVersionBase) this;

    }

    /**
     * Factory method creating a new <code>RegistryNode</code> from a node at a given Registry path.
     *
     * @param path  Registry path of the node
     * @return  A new instance representing the Registry node at <code>path</code>
     * @throws CmisObjectNotFoundException  if <code>path</code> does not identify a Registry node
     * @throws CmisRuntimeException
     */
    public RegistryObject getNode(String path) throws RegistryException {

        return create(repository.get(path));

    }
    
    private Resource getResourceByPath(String path) throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
     * Factory method for creating a new <code>RegistryNode</code> instance from a Registry <code>Node</code>
     *
     * @param resource of registry <code>Resource</code>
     * @return  a new <code>RegistryObject</code>
     * Since my implementation is fixed for types Document & Folder, this method 
     * is implemented in the respective RegistryDocument or RegistryFolder classes.
     */
    protected abstract RegistryObject create(Resource resource);
    //Registry implementation calls getGregNode from DefaultDocumentHandler & DefaultFolderHandler.
    //Doc returns a new Registry version
    //Folder returns a new Registry Folder

    
    /**
     * Compile the <code>ObjectData</code> for this node
     */
    public ObjectData compileObjectType(Set<String> filter, Boolean includeAllowableActions,
                                        ObjectInfoHandler objectInfos, boolean requiresObjectInfo) {

        try {
            ObjectDataImpl result = new ObjectDataImpl();
            ObjectInfoImpl objectInfo = new ObjectInfoImpl();

            PropertiesImpl properties = new PropertiesImpl();
            //filter = filter == null ? null : new HashSet<String>(filter);
            
            if (filter != null)
                filter = new HashSet<String>(filter);
            
            compileProperties(properties, filter, objectInfo);
            result.setProperties(properties);
            if (filter != null && !filter.isEmpty()) {
                log.debug("Unknown filter properties: " + filter.toString());
            }

            if (Boolean.TRUE.equals(includeAllowableActions)) {
                result.setAllowableActions(getAllowableActions());
            }

            if (requiresObjectInfo) {
                objectInfo.setObject(result);
                objectInfos.addObjectInfo(objectInfo);
            }

            return result;
        } catch (RegistryException e) {
            String msg = "";
            log.error(msg, e);
            throw new CmisRuntimeException(msg, e);
        }
    }
    
    public AllowableActions getAllowableActions() {
        AllowableActionsImpl aas = new AllowableActionsImpl();
        aas.setAllowableActions(compileAllowableActions(new HashSet<Action>()));
        return aas;
    }

    
    /**
     * See CMIS 1.0 section 2.2.3.5 getObjectParents
     *
     * @return  parent of this object
     * @throws  CmisObjectNotFoundException  if this is the root folder
     * @throws  CmisRuntimeException
     */
    public RegistryFolder getParent() {
        try {
            if(resource.getPath().equals("/")){
                throw new CmisObjectNotFoundException("No parent for root folder");
            }
            Resource parent = repository.get(resource.getParentPath());
            if  (parent instanceof CollectionImpl){
                FolderTypeHandler handler = new FolderTypeHandler(getRepository(), pathManager, typeManager);
		        return handler.getGregNode(parent);
            } else{
                throw new CmisInvalidArgumentException("Resource found. Collection expected");
            }
        }
        catch (RegistryException e) {
            String msg = "Error trying to retrieve the resource ";
            log.error(msg, e);
            throw new CmisRuntimeException(msg, e);
        }
    }
    
    
   
    /**
     * Compile the allowed actions on the CMIS object represented by this instance
     * See CMIS 1.0 section 2.2.4.6 getAllowableActions
     *
     * @param aas  compilation of allowed actions
     * @return
     */
    protected Set<Action> compileAllowableActions(Set<Action> aas) {
        setAction(aas, Action.CAN_GET_OBJECT_PARENTS, true);
        setAction(aas, Action.CAN_GET_PROPERTIES, true);
        setAction(aas, Action.CAN_UPDATE_PROPERTIES, true);
        setAction(aas, Action.CAN_MOVE_OBJECT, true);
        if(pathManager.isRoot(getNode())){
            setAction(aas, Action.CAN_DELETE_OBJECT, false);
        } else {
            setAction(aas, Action.CAN_DELETE_OBJECT, true);
        }
        setAction(aas, Action.CAN_GET_ACL, false);
        setAction(aas, Action.CAN_APPLY_ACL, false);
        setAction(aas, Action.CAN_GET_OBJECT_RELATIONSHIPS, false);
        setAction(aas, Action.CAN_ADD_OBJECT_TO_FOLDER, false);
        setAction(aas, Action.CAN_REMOVE_OBJECT_FROM_FOLDER, false);
        setAction(aas, Action.CAN_APPLY_POLICY, false);
        setAction(aas, Action.CAN_GET_APPLIED_POLICIES, false);
        setAction(aas, Action.CAN_REMOVE_POLICY, false);
        setAction(aas, Action.CAN_CREATE_RELATIONSHIP, false);
        return aas;
    }
    
    /**
     * Add <code>action</code> to <code>actions</code> iff <code>condition</code> is true.
     *
     * @param actions
     * @param action
     * @param condition
     */
    protected static void setAction(Set<Action> actions, Action action, boolean condition) {
        if (condition) {
            actions.add(action);
        } else {
            actions.remove(action);
        }
    }



    /**
     * @return  the change token of the CMIS object represented by this instance
     * @throws RegistryException
     */
    protected String getChangeToken() throws RegistryException {
        return null;
    }
    
    
    protected String getCreatedBy() throws RegistryException {
        return resource.getAuthorUserName();
    }

    /**
     * @return  the last modification date of the CMIS object represented by this instance
     * @throws RegistryException
     */
    protected GregorianCalendar getLastModified() throws RegistryException {
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(resource.getLastModified());
        return gregorianCalendar;
    }

    /**
     * @return  the last modifier of the CMIS object represented by this instance
     * @throws RegistryException
     */
    protected String getLastModifiedBy() throws RegistryException {
        return resource.getLastUpdaterUserName();
    }

    protected GregorianCalendar getCreated() throws RegistryException {
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(resource.getCreatedTime());
        return gregorianCalendar;
    }
    
    /**
     * Utility function to retrieve the length of a property of a Registry <code>Node</code>.
     *
     * @param node
     * @param propertyName
     * @return
     * @throws RegistryException
     */	
    protected static long getPropertyLength(Resource node, String propertyName) throws RegistryException {
        String property = node.getProperty(propertyName);
        //if property asks for REGISTRY_DATA then this should return the length of the content stream //my assumption
        if(propertyName.equals(CMISConstants.GREG_DATA)){
            if(property != null && !property.equals("true")){
                return 0;
            }
        	long count = 0;
            //node.getContent();
            Object dataObject = node.getContent();

        	if(dataObject == null){
        		return count;
        	} else{
                count = 0;
                byte[] buffer = new byte[64 * 1024];
                InputStream inputStream = node.getContentStream();
                try {
                    int b = inputStream.read(buffer);
                    while (b > -1) {
                        count += b;
                        b = inputStream.read(buffer);
                    }
                    inputStream.close();
                } catch (IOException e) {
                    throw new CmisRuntimeException(e.getMessage(),e);
                }
                return count;
        	}
        	
        } else{
            if(property == null){
                return -1;
            } else{
        	    return property.length();
            }
        }
    }
    
    
    /**
     * Retrieve the context node of the CMIS object represented by this instance. The
     * context node is the node which is used to derive the common properties from
     * (creation date, modification date, ...)
     *
     * @return  the context node
     * @throws RegistryException
     */
    protected abstract Resource getContextNode() throws RegistryException;


    /**
     * @return  the value of the <code>cmis:objectTypeId</code> property
     */
    protected abstract String getTypeIdInternal();

    /**
     * @return  the value of the <code>cmis:baseTypeId</code> property
     */
    protected abstract BaseTypeId getBaseTypeId();

    protected String getVersionSeriesId() throws RegistryException {

        //TODO Think on this later
        String path = resource.getPath();
        if(path.endsWith(CMISConstants.PWC_SUFFIX)){
            int endIndex = path.indexOf(CMISConstants.PWC_SUFFIX);
            return path.substring(0, endIndex);
        } else if(path.contains(";")){
            int endIndex = path.indexOf(';');
            return path.substring(0, endIndex);
        }

        return resource.getPath();

    }

    /**
     * @return  the name of the underlying Registry <code>node</code>.
     * @throws RegistryException
     */
    protected String getNodeName() throws RegistryException {
    	String path = resource.getPath(); //TODO check if it starts with '/'
        if(path.equals("/")){
            return "/";
        }
    	String[] parts = path.split("/");
        String actualName = null;
    	if(parts == null){
            actualName = path;
    	} else{
    		actualName = parts[parts.length-1];
    	}

        //Stored as abc_pwc in the registry but presented as pwc to cmis client
        if(actualName.endsWith(CMISConstants.PWC_SUFFIX)){
            return actualName.substring(0, actualName.indexOf(CMISConstants.PWC_SUFFIX));
        } else {
            //TODO if name includes the ;version:xxx part maybe remove the ;version:xxx
            return actualName;
        }
    }

    /**
     * @return  the object id of the CMIS object represented by this instance
     * @throws RegistryException
     */
    protected String getObjectId() throws RegistryException {
        return getVersionSeriesId();
    }

    /**
     * Add Id property to the CMIS object represented by this instance
     */
    protected final void addPropertyId(PropertiesImpl props, String typeId, Set<String> filter, String id, String value) {
        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        if (value.equals(CMISConstants.GREG_PROPERTY_NOT_SET)) {
            value = null;
        }

        PropertyIdImpl prop = new PropertyIdImpl(id, value);
        prop.setQueryName(id);
        props.addProperty(prop);
    }

    protected final void addPropertyId(PropertiesImpl props, String typeId, Set<String> filter, String id, List<String> values) {

        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        PropertyIdImpl prop = new PropertyIdImpl(id, values);
        prop.setQueryName(id);
        props.addProperty(prop);
    }

    /**
     * Add string property to the CMIS object represented by this instance
     */
    protected final void addPropertyString(PropertiesImpl props, String typeId, Set<String> filter, String id, String value) {
        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        PropertyStringImpl prop = new PropertyStringImpl(id, value);
        prop.setQueryName(id);
        props.addProperty(prop);
    }

    /**
     * Add integer property to the CMIS object represented by this instance
     */
    protected final void addPropertyInteger(PropertiesImpl props, String typeId, Set<String> filter, String id, long value) {
        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        PropertyIntegerImpl prop = new PropertyIntegerImpl(id, BigInteger.valueOf(value));
        prop.setQueryName(id);
        props.addProperty(prop);
    }

    /**
     * Add boolean property to the CMIS object represented by this instance
     */
    protected final void addPropertyBoolean(PropertiesImpl props, String typeId, Set<String> filter, String id, boolean value) {
        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        PropertyBooleanImpl prop = new PropertyBooleanImpl(id, value);
        prop.setQueryName(id);
        props.addProperty(prop);
    }

    /**
     * Add date-time property to the CMIS object represented by this instance
     */
    protected final void addPropertyDateTime(PropertiesImpl props, String typeId, Set<String> filter, String id,
                                             GregorianCalendar value) {

        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        PropertyDateTimeImpl prop = new PropertyDateTimeImpl(id, value);
        prop.setQueryName(id);
        props.addProperty(prop);
    }

    /**
     * Validate a set of properties against a filter and its definitions
     */
    protected final boolean checkAddProperty(Properties properties, String typeId, Set<String> filter, String id) {
        if (properties == null || properties.getProperties() == null) {
            throw new IllegalArgumentException("Properties must not be null!");
        }

        if (id == null) {
            throw new IllegalArgumentException("Id must not be null!");
        }

        TypeDefinition type = typeManager.getType(typeId);
        if (type == null) {
            throw new IllegalArgumentException("Unknown type: " + typeId);
        }
        if (!type.getPropertyDefinitions().containsKey(id)) {
            throw new IllegalArgumentException("Unknown property: " + id);
        }

        String queryName = type.getPropertyDefinitions().get(id).getQueryName();

        if (queryName != null && filter != null) {
            if (filter.contains(queryName)) {
                filter.remove(queryName);
            } else {
                return false;
            }
        }

        return true;
    }
    
    
    public final void updateProperties(Resource resource, String typeId, Properties properties) {
        PropertyUpdater.create(typeManager, typeId, properties).apply(repository, resource);
    }
	
    
    /**
     * See CMIS 1.0 section 2.2.4.12 updateProperties
     *
     * @throws CmisStorageException
     */
    public RegistryObject updateProperties(Properties properties) {

        // get and check the new name
        String newName = PropertyHelper.getStringProperty(properties, PropertyIds.NAME);
        boolean rename = newName != null && !getName().equals(newName);

        if (rename && isRoot()) {
            throw new CmisUpdateConflictException("Cannot rename root node");
        }

        try{

            Resource newNode;

            if (rename) {
                String destPath = CommonUtil.getDestPathOfNode(getNode().getParentPath(), newName);
                repository.rename(getNode().getPath(), destPath);
                newNode = repository.get(destPath);
                //newNode.setProperty(PropertyIds.NAME);
                setNode(newNode);
            } else {
                newNode = resource;
            }

        	// Are there properties to update?
            PropertyUpdater propertyUpdater = PropertyUpdater.create(typeManager, getTypeId(), properties);

            RegistryVersionBase gregVersion = isVersionable() ? asVersion() : null;

            // Update properties. Checkout if required
            boolean autoCheckout = false;
            if (!propertyUpdater.isEmpty()) {
                autoCheckout = gregVersion != null && !gregVersion.isCheckedOut();
                if (autoCheckout) {
                    gregVersion.checkout();
                }

                // update the properties
                propertyUpdater.apply(repository, resource);
            }

            //session.save();
            if (autoCheckout) {
                // auto versioning -> return new version created by checkin
                return gregVersion.checkin(null, null, "auto checkout");
            } else if (gregVersion != null && gregVersion.isCheckedOut()) {
                // the node is checked out -> return pwc.
                RegistryVersionBase gregNewVersion = create(newNode).asVersion();
                return gregNewVersion.getPwc();
            } else {
                // non versionable or not a new node -> return this
                return create(newNode);
            }
        }
        catch (RegistryException e) {
            String msg = "Failed to update properties ";
            log.error(msg, e);
            throw new CmisStorageException(msg, e);
        }

    }



    /**
     * See CMIS 1.0 section 2.2.4.14 deleteObject
     *
     * @throws CmisRuntimeException
     */
    public void delete(boolean allVersions, boolean isPwc) {
        try {
        	String path = resource.getPath();
            repository.delete(path);
        }
        catch (RegistryException e) {
            String msg = "Failed to delete the object ";
            log.error(msg, e);
            throw new CmisRuntimeException(msg, e);
        }
    }

    /**
     * See CMIS 1.0 section 2.2.4.13 moveObject
     *
     * @throws CmisStorageException
     */
    public RegistryObject move(RegistryFolder parent) {
        try {
            // move it if target location is not same as source location
        	//TODO 
            String destPath = CommonUtil.getDestPathOfNode(parent.getNode().getPath(), getNodeName());
            String srcPath  = resource.getPath();
            Resource newNode;
            if (srcPath.equals(destPath)) {
                newNode = resource;
            } else {
                repository.move(srcPath, destPath);
                newNode = repository.get(destPath);
            }

            return create(newNode);
        }
        catch (RegistryException e) {
            String msg = "Failed ot move the object ";
            log.error(msg, e);
            throw new CmisStorageException(msg, e);
        }
    }
    
    @Override
    public String toString() {
        return resource.getPath();
    }

    protected static final class PropertyUpdater {
        private final List<PropertyData<?>> removeProperties = new ArrayList<PropertyData<?>>();
        private final List<PropertyData<?>> updateProperties = new ArrayList<PropertyData<?>>();

        private PropertyUpdater() { }

        public static PropertyUpdater create(RegistryTypeManager typeManager, String typeId, Properties properties) {
            if (properties == null) {
                throw new CmisConstraintException("No properties!");
            }

            // get the property definitions
            TypeDefinition type = typeManager.getType(typeId);
            if (type == null) {
                throw new CmisObjectNotFoundException("Type '" + typeId + "' is unknown!");
            }

            PropertyUpdater propertyUpdater = new PropertyUpdater();
            // update properties
            for (PropertyData<?> prop : properties.getProperties().values()) {
                PropertyDefinition<?> propDef = type.getPropertyDefinitions().get(prop.getId());

                // do we know that property?
                if (propDef == null) {
                    throw new CmisInvalidArgumentException("Property '" + prop.getId() + "' is unknown!");
                }

                // skip content stream file name
                if (propDef.getId().equals(PropertyIds.CONTENT_STREAM_FILE_NAME)) {
                    log.warn("Cannot set " + PropertyIds.CONTENT_STREAM_FILE_NAME + ". Ignoring");
                    continue;
                }

                // silently skip name
                if (propDef.getId().equals(PropertyIds.NAME)) {
                    continue;
                }

                // can it be set?
                if (propDef.getUpdatability() == Updatability.READONLY) {
                    throw new CmisConstraintException("Property '" + prop.getId() + "' is readonly!");
                }

                if (propDef.getUpdatability() == Updatability.ONCREATE) {
                    throw new CmisConstraintException("Property '" + prop.getId() + "' can only be set on create!");
                }

                // default or value
                PropertyData<?> newProp;
                newProp = PropertyHelper.isPropertyEmpty(prop)
                        ? PropertyHelper.getDefaultValue(propDef)
                        : prop;

                // Schedule for remove or update
                if (newProp == null) {
                    propertyUpdater.removeProperties.add(prop);
                } else {
                    propertyUpdater.updateProperties.add(newProp);
                }
            }

            return propertyUpdater;
        }

        public boolean isEmpty() {
            return removeProperties.isEmpty() && updateProperties.isEmpty();
        }

        public void apply(Registry repository,Resource resource) {
            try {
                for (PropertyData<?> prop: removeProperties) {
                    CommonUtil.removeProperty(repository, resource, prop);
                }
                for (PropertyData<?> prop: updateProperties) {
                    CommonUtil.setProperty(repository, resource, prop);
                }
            }
            catch (RegistryException e) {
                String msg = "Failed to apply properties to the resource";
                log.error(msg, e);
                throw new CmisStorageException(msg, e);
            }
        }
    }
    
    protected static GregorianCalendar getPropertyOrElse(Resource resource, String propertyName, GregorianCalendar defaultValue)
            throws RegistryException {

        if (hasProperty(resource, propertyName)) {
            Calendar date = toDate(resource.getProperty(propertyName));
            return toCalendar(date);
        } else {
            return defaultValue;
        }
    }
    
    protected static Calendar toDate (String propertyValue){
    	/*
    	 * TODO
    	 * Check whether propertyValue is ISO 8601 compliant
    	 * */
    	Calendar calendar = javax.xml.bind.DatatypeConverter.parseDateTime(propertyValue);
    	return calendar;
    }
    
    protected static GregorianCalendar toCalendar(Calendar date) {
        if (date instanceof GregorianCalendar) {
            return (GregorianCalendar) date;
        } else {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTimeZone(date.getTimeZone());
            calendar.setTimeInMillis(date.getTimeInMillis());
            return calendar;
        }
    }
    
    protected static String getPropertyOrElse(Resource resource, String propertyName, String defaultValue)
            throws RegistryException {

        return hasProperty(resource, propertyName)
            ? resource.getProperty(propertyName)
            : defaultValue;
    }
    
    protected static boolean hasProperty(Resource resource,String propertyName){
    	
    	String property = resource.getProperty(propertyName);

    	return property != null;
    }
    
    protected void compileProperties(PropertiesImpl properties, Set<String> filter, ObjectInfoImpl objectInfo)
            throws RegistryException {

        String typeId = getTypeIdInternal();
        BaseTypeId baseTypeId = getBaseTypeId();

        objectInfo.setBaseType(baseTypeId);
        objectInfo.setTypeId(typeId);
        objectInfo.setHasAcl(false);
        objectInfo.setVersionSeriesId(getVersionSeriesId());
        objectInfo.setRelationshipSourceIds(null);
        objectInfo.setRelationshipTargetIds(null);
        objectInfo.setRenditionInfos(null);
        objectInfo.setSupportsPolicies(false);
        objectInfo.setSupportsRelationships(false);

        // id
        String objectId = getObjectId();
        addPropertyId(properties, typeId, filter, PropertyIds.OBJECT_ID, objectId);
        objectInfo.setId(objectId);

        // name
        String name = getName();
        addPropertyString(properties, typeId, filter, PropertyIds.NAME, name);
        objectInfo.setName(name);

        // base type and type name
        addPropertyId(properties, typeId, filter, PropertyIds.BASE_TYPE_ID, baseTypeId.value());
        addPropertyId(properties, typeId, filter, PropertyIds.OBJECT_TYPE_ID, typeId);

        // created and modified by
        String createdBy = getCreatedBy();
        addPropertyString(properties, typeId, filter, PropertyIds.CREATED_BY, createdBy);
        objectInfo.setCreatedBy(createdBy);


        addPropertyString(properties, typeId, filter, PropertyIds.LAST_MODIFIED_BY, getLastModifiedBy());

        // creation and modification date
        GregorianCalendar created = getCreated();
        addPropertyDateTime(properties, typeId, filter, PropertyIds.CREATION_DATE, created);
        objectInfo.setCreationDate(created);

        GregorianCalendar lastModified = getLastModified();
        addPropertyDateTime(properties, typeId, filter, PropertyIds.LAST_MODIFICATION_DATE, lastModified);
        objectInfo.setLastModificationDate(lastModified);

        addPropertyString(properties, typeId, filter, PropertyIds.CHANGE_TOKEN, getChangeToken());
    }
    
}
