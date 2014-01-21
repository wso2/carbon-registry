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
import org.apache.chemistry.opencmis.commons.data.*;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.*;
import org.apache.chemistry.opencmis.commons.exceptions.*;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.*;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.registry.cmis.util.CMISConstants;
import org.wso2.carbon.registry.cmis.util.CommonUtil;
import org.wso2.carbon.registry.cmis.util.PropertyHelper;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.cmis.impl.DocumentTypeHandler;
import org.wso2.carbon.registry.cmis.impl.FolderTypeHandler;

import org.wso2.carbon.registry.cmis.impl.UnversionedDocumentTypeHandler;

import java.math.BigInteger;
import java.util.*;

/**
 * This class represents the WSO2 Governance Registry back-end for the CMIS server.
 */
public class CMISRepository {
    private static final Logger log = LoggerFactory.getLogger(CMISRepository.class);

    private final Registry repository;
    private final RegistryTypeManager typeManager;
    private final PathManager pathManager;
    private final String REPOSITORY_ID = "WSO2 CMIS Repository";

    /**
     * Create a new <code>org.wso2.registry.chemistry.greg.CMISRepository</code> instance backed by a Governance Registry repository.
     *
     * @param repository  the CMIS repository
     * @param pathManager
     * @param typeManager
     *
     */
    public CMISRepository(Registry repository, PathManager pathManager, RegistryTypeManager typeManager) {
        this.repository = repository;
        this.typeManager = typeManager;
        this.pathManager = pathManager;
    }

    public Registry getRegistry(){
        return repository;
    }

    private String getRepositoryId(){
    	return REPOSITORY_ID;
    }
    
    /**
     * See CMIS 1.0 section 2.2.2.2 getRepositoryInfo
     */
    public RepositoryInfo getRepositoryInfo() {
        if(log.isTraceEnabled()) {
            log.trace("<<<<<<<< getRepositoryInfo");
        }
        return compileRepositoryInfo(getRepositoryId());
    }

    /**
     * See CMIS 1.0 section 2.2.2.2 getRepositoryInfo
     */
    public List<RepositoryInfo> getRepositoryInfos() {
        ArrayList<RepositoryInfo> infos = new ArrayList<RepositoryInfo>();
        infos.add(compileRepositoryInfo(getRepositoryId()));
        return infos;
    }

    /**
     * See CMIS 1.0 section 2.2.2.3 getTypeChildren
     */
    public TypeDefinitionList getTypeChildren(String typeId, boolean includePropertyDefinitions,
                                              BigInteger maxItems, BigInteger skipCount) {
        if(log.isTraceEnabled()) {
            log.trace("<<<<<<< getTypesChildren for type id " + typeId);
        }
        return typeManager.getTypeChildren(typeId, includePropertyDefinitions, maxItems, skipCount);
    }

    /**
     * See CMIS 1.0 section 2.2.2.5 getTypeDefinition
     */
    public TypeDefinition getTypeDefinition(String typeId) {

        if(log.isTraceEnabled()){
            log.trace("<<<<<<< getTypeDefinition for type id " + typeId);
        }

        TypeDefinition type = typeManager.getType(typeId);
        if (type == null) {
            throw new CmisObjectNotFoundException("Type '" + typeId + "' is unknown!");
        }

        return RegistryTypeManager.copyTypeDefinition(type);
    }

    /**
     * See CMIS 1.0 section 2.2.2.4 getTypeDescendants
     */
    public List<TypeDefinitionContainer> getTypesDescendants(String typeId, BigInteger depth,
                                                             Boolean includePropertyDefinitions) {

        return typeManager.getTypesDescendants(typeId, depth, includePropertyDefinitions);
    }

    /**
     * See CMIS 1.0 section 2.2.4.1 createDocument
     */
    public String createDocument(Properties properties, String folderId, ContentStream contentStream,
                                 VersioningState versioningState) {

        if(log.isTraceEnabled()) {
            log.trace("<<<<<<< Creating the document for folder Id:" + folderId
                + "versioning state:" + versioningState.toString() );
        }

        // check properties
        if (!CommonUtil.isNonEmptyProperties(properties)) {
            throw new CmisInvalidArgumentException("Properties must be set!");
        }

        // check type
        String typeId = PropertyHelper.getTypeId(properties);
        TypeDefinition type = typeManager.getType(typeId);
        if (type == null) {
            throw new CmisObjectNotFoundException("Type '" + typeId + "' is unknown!");
        }

        boolean isVersionable = RegistryTypeManager.isVersionable(type);
        if (!isVersionable && versioningState != VersioningState.NONE) {
            throw new CmisConstraintException("Versioning not supported for " + typeId);
        }

        String name = PropertyHelper.getStringProperty(properties, PropertyIds.NAME);
        // get parent Node
        RegistryFolder parent = getGregNode(folderId).asFolder();

        if (isVersionable && versioningState == VersioningState.NONE) {
            throw new CmisConstraintException("Versioning required for " + typeId);
        }

        if(versioningState == VersioningState.NONE){
            UnversionedDocumentTypeHandler handler = new UnversionedDocumentTypeHandler(getRegistry(), pathManager, typeManager);
            RegistryObject gregNode = handler.createDocument(parent, name, properties, contentStream, versioningState);
            return gregNode.getId();
        } else{
            DocumentTypeHandler typeHandler = new DocumentTypeHandler(getRegistry(), pathManager, typeManager);
            RegistryObject gregNode = typeHandler.createDocument(parent, name, properties, contentStream, versioningState);
            return gregNode.getId();
        }
    }

    /**
     * See CMIS 1.0 section 2.2.4.2 createDocumentFromSource
     */
    public String createDocumentFromSource(String sourceId, Properties properties, String folderId,
                                           VersioningState versioningState) {

        if(log.isTraceEnabled()) {
    	    log.trace("<<<<<<<< Creating document from source with source Id:" + sourceId +
                ", folder Id:" + folderId + " and versioning state:" + versioningState.toString());
        }

        // get parent folder Node
        RegistryFolder parent = getGregNode(folderId).asFolder();

        // get source document Node
        RegistryDocument source = getGregNode(sourceId).asDocument();

        boolean isVersionable = source.isVersionable();
        // Below throws same exception with different message based on spec
        if (!isVersionable && versioningState != VersioningState.NONE) {
            throw new CmisConstraintException("Versioning not supported for " + sourceId);
        }

        if (isVersionable && versioningState == VersioningState.NONE) {
            throw new CmisConstraintException("Versioning required for " + sourceId);
        }

        // create child from source
        RegistryObject gregNode = parent.addNodeFromSource(source, properties);
        return gregNode.getId();
    }




    /**
     * See CMIS 1.0 section 2.2.4.3 createFolder
     */
    public String createFolder(Properties properties, String folderId) {

        if(log.isTraceEnabled()) {
            log.trace("<<<<<<<<< Create folder with folder Id:" + folderId);
        }

        // check properties
        if (!CommonUtil.isNonEmptyProperties(properties)) {
            throw new CmisInvalidArgumentException("Properties must be set!");
        }

        // check type
        String typeId = PropertyHelper.getTypeId(properties);
        TypeDefinition type = typeManager.getType(typeId);
        if (type == null) {
            throw new CmisObjectNotFoundException("Type '" + typeId + "' is unknown!");
        }

        //TODO check the name
        String name = PropertyHelper.getStringProperty(properties, PropertyIds.NAME);

        // get parent Node
        RegistryFolder parent = getGregNode(folderId).asFolder();
        FolderTypeHandler typeHandler = new FolderTypeHandler(getRegistry(), pathManager, typeManager);
        RegistryObject gregNode = typeHandler.createFolder(parent, name, properties);
        return gregNode.getId();
    }

    /**
     * See CMIS 1.0 section 2.2.4.13 moveObject
     */
    public ObjectData moveObject(Holder<String> objectId, String targetFolderId,
                                 ObjectInfoHandler objectInfos, boolean requiresObjectInfo) {

        if(log.isTraceEnabled()) {
    	    log.trace("<<<<<<<< Moving object from " + objectId.getValue() + " to "
                + targetFolderId);
        }

        if (!CommonUtil.hasObjectId(objectId)) {
            throw new CmisInvalidArgumentException("Id is not valid!");
        }

        // get the node and parent
        RegistryObject gregNode = getGregNode(objectId.getValue());
        RegistryFolder parent = getGregNode(targetFolderId).asFolder();
        gregNode = gregNode.move(parent);
        objectId.setValue(gregNode.getId());
        return gregNode.compileObjectType(null, false, objectInfos, requiresObjectInfo);
    }

    /**
     * See CMIS 1.0 section 2.2.4.16 setContentStream
     */
    public void setContentStream(Holder<String> objectId, Boolean overwriteFlag,
                                 ContentStream contentStream) {

        if(log.isTraceEnabled()) {
    	    log.trace("<<<<<<< Set the content stream for object " + objectId.getValue() +
                " with overwrite set to " + overwriteFlag);
        }
        
        if (!CommonUtil.hasObjectId(objectId)) {
            throw new CmisInvalidArgumentException("Id is not valid!");
        }

        String pathOfObject = objectId.getValue();

        /*If it's a version (We cannot set or delete content from versions.
          If this is a version, it should mean the base version is passed.
          If not, then an exception)
         */

        String pathToGet = objectId.getValue();

        if(pathOfObject.contains(";")){

            pathToGet = pathOfObject.substring(0, pathOfObject.indexOf(";"));

            //get path of latest version
            try {
                String pathOfLatestVersion = repository.getVersions(pathToGet)[0];

                if( pathOfLatestVersion.equals(pathOfObject )){
                    //It's okay
                } else{
                    throw new CmisInvalidArgumentException("Cannot set or delete content in a Version");
                }
            } catch (RegistryException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        RegistryDocument gregDocument = getGregNode(pathToGet).asDocument();
        String id = gregDocument.setContentStream(contentStream, Boolean.TRUE.equals(overwriteFlag)).getId();
        objectId.setValue(id);
    }

    /**
     * See CMIS 1.0 section 2.2.4.14 deleteObject
     */
    public void deleteObject(String objectId, Boolean allVersions) {

        if(log.isTraceEnabled()) {
            log.trace("<<<<<< Delete Object with Id" + objectId
            + " with all versions set to " + allVersions);
        }

        // get the node
        RegistryObject gregNode = getGregNode(objectId);

        //If PWC cancelCheckOut
        //String property = gregNode.getNode().getProperty(GregProperty.GREG_IS_CHECKED_OUT);
        //boolean isCheckedOut = property !=null && property.equals("true");
        if(objectId.endsWith(CMISConstants.PWC_SUFFIX)){
            cancelCheckout(objectId);
        } else{
            gregNode.delete(Boolean.TRUE.equals(allVersions), RegistryPrivateWorkingCopy.isPwc(repository, objectId));
        }
    }

    /**
     * See CMIS 1.0 section 2.2.4.15 deleteTree
     */
    public FailedToDeleteData deleteTree(String folderId) {

        if(log.isTraceEnabled()) {
            log.trace("<<<<<<< deleteTree with folder id " + folderId);
        }

        //check for root
        if(folderId.equals("/")){
            throw new CmisInvalidArgumentException("Cannot delete root folder");
        }

        // get the folder
        RegistryFolder gregFolder = getGregNode(folderId).asFolder();
        return gregFolder.deleteTree();
    }

    /**
     * See CMIS 1.0 section 2.2.4.12 updateProperties
     */
    public ObjectData updateProperties(Holder<String> objectId, Properties properties,
                                       ObjectInfoHandler objectInfos, boolean objectInfoRequired) {

        if(log.isTraceEnabled()){
    	    log.trace("<<<<<<< updateProperties for object id: " + objectId.getValue());
        }

        if (objectId == null) {
            throw new CmisInvalidArgumentException("Id is not valid!");
        }

        // get the node
        RegistryObject gregNode = getGregNode(objectId.getValue());
        String id = gregNode.updateProperties(properties).getId();
        objectId.setValue(id);
        return gregNode.compileObjectType(null, false, objectInfos, objectInfoRequired);
    }

    /**
     * See CMIS 1.0 section 2.2.4.7 getObject
     */
    public ObjectData getObject(String objectId, String filter, Boolean includeAllowableActions,
                                ObjectInfoHandler objectInfos, boolean requiresObjectInfo) {

        if(log.isTraceEnabled()){
    	    log.trace("<<<<<<<< getObject for id: " + objectId);
        }

        // check id
        if (objectId == null) {
            throw new CmisInvalidArgumentException("Object Id must be set.");
        }

        // get the node
        RegistryObject gregNode = getGregNode(objectId);

        // gather properties
        return gregNode.compileObjectType(splitFilter(filter), includeAllowableActions, objectInfos, requiresObjectInfo);
    }

    /**
     * See CMIS 1.0 section 2.2.4.8 getProperties
     */
    public Properties getProperties(String objectId, String filter, Boolean includeAllowableActions,
                                    ObjectInfoHandler objectInfos, boolean requiresObjectInfo) {

    	ObjectData object = getObject(objectId, filter, includeAllowableActions, objectInfos, requiresObjectInfo);
        return object.getProperties();
    }

    /**
     * See CMIS 1.0 section 2.2.4.6 getAllowableActions
     */
    public AllowableActions getAllowableActions(String objectId) {

        if(log.isTraceEnabled()) {
            log.trace("<<<<<<< getAllowableActions for object id " + objectId);
        }

        RegistryObject gregNode = getGregNode(objectId);
        return gregNode.getAllowableActions();

    }

    /**
     * See CMIS 1.0 section 2.2.4.10 getContentStream
     */
    public ContentStream getContentStream(String objectId, BigInteger offset, BigInteger length) {

        if(log.isTraceEnabled()) {
            log.trace("<<<<<<< getContentStream for object id " + objectId);
        }

        if (offset != null || length != null) {
            throw new CmisInvalidArgumentException("Offset and Length are not supported!");
        }

        // get the node
        RegistryDocument gregDocument = getGregNode(objectId).asDocument();

        ContentStream contentStream = gregDocument.getContentStream();
        if(contentStream.getStream() != null){
            return contentStream;
        } else{
            throw new CmisConstraintException("Resource content is empty");
        }

    }

    /**
     * See CMIS 1.0 section 2.2.3.1 getChildren
     */
    public ObjectInFolderList getChildren(String folderId, String filter,
                                          Boolean includeAllowableActions, Boolean includePathSegment, BigInteger maxItems, BigInteger skipCount,
                                          ObjectInfoHandler objectInfos, boolean requiresObjectInfo) {
        if(log.isTraceEnabled()){
        	log.trace("<<<<<< getChildren for folder id " + folderId);
        }

        // skip and max
        int skip = skipCount == null ? 0 : skipCount.intValue();
        if (skip < 0) {
            skip = 0;
        }

        int max = maxItems == null ? Integer.MAX_VALUE : maxItems.intValue();
        if (max < 0) {
            max = Integer.MAX_VALUE;
        }

        // get the folder
        RegistryFolder gregFolder = getGregNode(folderId).asFolder();

        // set object info of the the folder
        if (requiresObjectInfo) {
            gregFolder.compileObjectType(null, false, objectInfos, requiresObjectInfo);
        }

        // prepare result
        ObjectInFolderListImpl result = new ObjectInFolderListImpl();
        result.setObjects(new ArrayList<ObjectInFolderData>());
        result.setHasMoreItems(false);
        int count = 0;

        // iterate through children
        Set<String> splitFilter = splitFilter(filter);
        Iterator<RegistryObject> childNodes = gregFolder.getNodes();
        while (childNodes.hasNext()) {
            RegistryObject child = childNodes.next();
            count++;

            if (skip > 0) {
                skip--;
                continue;
            }

            if (result.getObjects().size() >= max) {
                result.setHasMoreItems(true);
                continue;
            }

            // build and add child object
            ObjectInFolderDataImpl objectInFolder = new ObjectInFolderDataImpl();
            objectInFolder.setObject(child.compileObjectType(splitFilter, includeAllowableActions, objectInfos,
                    requiresObjectInfo));

            if (Boolean.TRUE.equals(includePathSegment)) {
                objectInFolder.setPathSegment(child.getName());
            }

            result.getObjects().add(objectInFolder);
        }

        result.setNumItems(BigInteger.valueOf(count));
        return result;
    }

    /**
     * See CMIS 1.0 section 2.2.3.2 getDescendants
     */
    public List<ObjectInFolderContainer> getDescendants(String folderId, BigInteger depth,
                                                        String filter, Boolean includeAllowableActions, Boolean includePathSegment, ObjectInfoHandler objectInfos,
                                                        boolean requiresObjectInfo, boolean foldersOnly) {

        if(log.isTraceEnabled()) {
    	    log.trace("<<<<<< getDescendants or getFolderTree for folder id " + folderId);
        }
        // check depth
        int d = depth == null ? 2 : depth.intValue();
        if (d == 0) {
            throw new CmisInvalidArgumentException("Depth must not be 0!");
        }
        if (d < -1) {
            d = -1;
        }

        // get the folder
        RegistryFolder gregFolder = getGregNode(folderId).asFolder();

        // set object info of the the folder
        if (requiresObjectInfo) {
            gregFolder.compileObjectType(null, false, objectInfos, requiresObjectInfo);
        }

        // get the tree
        List<ObjectInFolderContainer> result = new ArrayList<ObjectInFolderContainer>();
        gatherDescendants(gregFolder, result, foldersOnly, d, splitFilter(filter), includeAllowableActions,
                includePathSegment, objectInfos, requiresObjectInfo);

        return result;
    }

    /**
     * See CMIS 1.0 section 2.2.3.4 getFolderParent
     */
    public ObjectData getFolderParent(String folderId, String filter, ObjectInfoHandler objectInfos,
                                      boolean requiresObjectInfo) {

    	List<ObjectParentData> parents = getObjectParents(folderId, filter, false, false, objectInfos,
                requiresObjectInfo);

        if (parents.isEmpty()) {
            throw new CmisInvalidArgumentException("The root folder has no parent!");
        }

        return parents.get(0).getObject();
    }

    /**
     * See CMIS 1.0 section 2.2.3.5 getObjectParents
     */
    public List<ObjectParentData> getObjectParents(String objectId, String filter,
                                                   Boolean includeAllowableActions, Boolean includeRelativePathSegment, ObjectInfoHandler objectInfos,
                                                   boolean requiresObjectInfo) {

        if(log.isTraceEnabled()) {
    	    log.trace("<<<<<<< getObjectParents for object id " + objectId);
        }

        // get the file or folder
        RegistryObject gregNode = getGregNode(objectId);

        // don't climb above the root folder
        if (gregNode.isRoot()) {
            return Collections.emptyList();
        }

        // set object info of the the object
        if (requiresObjectInfo) {
            gregNode.compileObjectType(null, false, objectInfos, requiresObjectInfo);
        }

        // get parent
        RegistryObject parent = gregNode.getParent();
        ObjectData object = parent.compileObjectType(splitFilter(filter), includeAllowableActions, objectInfos,
                requiresObjectInfo);

        ObjectParentDataImpl result = new ObjectParentDataImpl();
        result.setObject(object);
        if (Boolean.TRUE.equals(includeRelativePathSegment)) {
            result.setRelativePathSegment(gregNode.getName());
        }

        return Collections.singletonList((ObjectParentData) result);
    }

    /**
     * See CMIS 1.0 section 2.2.4.9 getObjectByPath
     */
    public ObjectData getObjectByPath(String folderPath, String filter, boolean includeAllowableActions,
                                      boolean includeACL, ObjectInfoHandler objectInfos, boolean requiresObjectInfo) {

        if(log.isTraceEnabled()) {
            log.trace("<<<<<<< getObjectByPath for folder path " + folderPath);
        }

        // check path
        if (folderPath == null || !PathManager.isAbsolute(folderPath)) {
            throw new CmisInvalidArgumentException("Invalid folder path!");
        }

        RegistryObject root = getGregNode(PathManager.CMIS_ROOT_ID);
        RegistryObject gregNode;
        if (PathManager.isRoot(folderPath)) {
            gregNode = root;
        } else {
            String path = PathManager.relativize(PathManager.CMIS_ROOT_PATH, folderPath);
            try{
                gregNode = root.getNode(path);
            }
            catch (RegistryException e) {
                throw new CmisObjectNotFoundException(e.getMessage(), e);
            }


        }

        return gregNode.compileObjectType(splitFilter(filter), includeAllowableActions, objectInfos, requiresObjectInfo);
    }

    /**
     * See CMIS 1.0 section 2.2.3.6 getCheckedOutDocs
     */
    public ObjectList getCheckedOutDocs(String folderId, String filter, String orderBy,
     Boolean includeAllowableActions, BigInteger maxItems, BigInteger skipCount){

        if(log.isTraceEnabled()) {
            log.trace("<<<<<<<< getCheckedOutDocs for folder id " + folderId);
        }

        // skip and max
        int skip = skipCount == null ? 0 : skipCount.intValue();
        if (skip < 0) {
            skip = 0;
        }

        int max = maxItems == null ? Integer.MAX_VALUE : maxItems.intValue();
        if (max < 0) {
            max = Integer.MAX_VALUE;
        }

        try {

            String[] children = null;

            if (folderId == null) {
                //Get the list of tracked out documents
                Resource tracker = repository.get(CMISConstants.GREG_CHECKED_OUT_TRACKER);
                children = tracker.getProperties().keySet().toArray(new String[0]);
            } else{
                RegistryFolder folder = getGregNode(folderId).asFolder();
                children  = folder.getNode().getChildren();
            }

            // prepare results
            ObjectListImpl result = new ObjectListImpl();
            result.setObjects(new ArrayList<ObjectData>());
            result.setHasMoreItems(false);

            // iterate through children
            Set<String> splitFilter = splitFilter(filter);
            int count = 0;
            for(String child: children) {

                if(getRegistry().resourceExists(child)){
                    RegistryObject node = getGregNode(child);

                    if (!node.isVersionable()) {
                        continue;
                    }

                    if(!isPrivateWorkingCopy(node)){
                        continue;
                    }
                    count++;

                    if (skip > 0) {
                        skip--;
                        continue;
                    }

                    if (result.getObjects().size() >= max) {
                        result.setHasMoreItems(true);
                        continue;
                    }

                    // build and add child object
                    RegistryPrivateWorkingCopy gregVersion = node.asVersion().getPwc();
                    ObjectData objectData = gregVersion.compileObjectType(splitFilter, includeAllowableActions, null, false);
                    result.getObjects().add(objectData);
                } else {
                    Resource resource = getRegistry().get(CMISConstants.GREG_CHECKED_OUT_TRACKER);
                    resource.removeProperty(child);
                    getRegistry().put(CMISConstants.GREG_CHECKED_OUT_TRACKER, resource);
                }
            }

            result.setNumItems(BigInteger.valueOf(count));
            return result;
        }
        catch(RegistryException e){
            throw new CmisRuntimeException(e.getMessage(), e);
        }

    }

    private boolean isPrivateWorkingCopy(RegistryObject node) {
        String checkedOutProperty = node.getNode().getProperty(CMISConstants.GREG_IS_CHECKED_OUT);
        String createdAsPwcProperty = node.getNode().getProperty(CMISConstants.GREG_CREATED_AS_PWC);

        boolean checkedOut = checkedOutProperty != null && checkedOutProperty.equals("true");
        boolean createdAsPwc = createdAsPwcProperty != null && createdAsPwcProperty.equals("true");
        boolean endsWithPwc = node.getNode().getPath().endsWith(CMISConstants.PWC_SUFFIX);

        return (checkedOut || createdAsPwc || endsWithPwc);

    }



    /**
     * See CMIS 1.0 section 2.2.7.1 checkOut
     */
    public void checkOut(Holder<String> objectId, Holder<Boolean> contentCopied) {

        if(log.isTraceEnabled()) {
            log.trace("<<<<<<<<< checkout for object id " + objectId.getValue());
        }
        // check id
        if (objectId == null || objectId.getValue() == null) {
            throw new CmisInvalidArgumentException("Object Id must be set.");
        }

        // get the node
        RegistryObject gregNode = getGregNode(objectId.getValue());
        if (!gregNode.isVersionable()) {
            throw new CmisUpdateConflictException("Not a version: " + gregNode);
        }

        // checkout
        RegistryPrivateWorkingCopy pwc = gregNode.asVersion().checkout();
        objectId.setValue(pwc.getId());
        if (contentCopied != null) {
            contentCopied.setValue(true);
        }
    }

    /**
     * See CMIS 1.0 section 2.2.7.2 cancelCheckout
     */
    public void cancelCheckout(String objectId) {

        if(log.isTraceEnabled()) {
            log.trace("<<<<<<<< cancelCheckout for object id " + objectId);
        }
        //TODO check what is the object id. Whether it is the id of orig copy or pwc.
        // check id
        if (objectId == null) {
            throw new CmisInvalidArgumentException("Object Id must be set.");
        }

        // get the node
        RegistryObject gregNode = getGregNode(objectId);
        if (!gregNode.isVersionable()) {
            throw new CmisUpdateConflictException("Not a version: " + gregNode);
        }

        // cancelCheckout
        gregNode.asVersion().cancelCheckout();
    }

    /**
     * See CMIS 1.0 section 2.2.7.3 checkedIn
     */
    public void checkIn(Holder<String> objectId, Boolean major, Properties properties,
                        ContentStream contentStream, String checkinComment) {

        if(log.isTraceEnabled()) {
            log.trace("<<<<<<< checkin for object id " + objectId.getValue());
        }

        // check id
        if (objectId == null || objectId.getValue() == null) {
            throw new CmisInvalidArgumentException("Object Id must be set.");
        }

        // get the node
        RegistryObject gregNode;
        try {
            gregNode = getGregNode(objectId.getValue());
        }
        catch (CmisObjectNotFoundException e) {
            throw new CmisUpdateConflictException(e.getCause().getMessage(), e.getCause());
        }

        if (!gregNode.isVersionable()) {
            throw new CmisUpdateConflictException("Not a version: " + gregNode);
        }

        // checkin
        RegistryVersion checkedIn = gregNode.asVersion().checkin(properties, contentStream, checkinComment);
        objectId.setValue(checkedIn.getId());
    }

    /**
     * See CMIS 1.0 section 2.2.7.6 getAllVersions
     */
    public List<ObjectData> getAllVersions(String objectId, String filter,
                                           Boolean includeAllowableActions, ObjectInfoHandler objectInfos, boolean requiresObjectInfo) {

        if(log.isTraceEnabled()) {
            log.trace("<<<<<<<<<< getAllVersions for object id " + objectId);
        }
        // check id
        if (objectId == null) {
            throw new CmisInvalidArgumentException("Object Id must be set.");
        }

        Set<String> splitFilter = splitFilter(filter);

        // get the node
        RegistryObject gregNode = getGregNode(objectId);

        // Collect versions
        if (gregNode.isVersionable()) {
            RegistryVersionBase gregVersion = gregNode.asVersion();

            Iterator<RegistryVersion> versions = gregVersion.getVersions();
            //if (versions.hasNext()) {
            //    versions.next(); // skip root version
            //}

            List<ObjectData> allVersions = new ArrayList<ObjectData>();
            while (versions.hasNext()) {
                RegistryVersion version = versions.next();
                ObjectData objectData = version.compileObjectType(splitFilter, includeAllowableActions, objectInfos,
                        requiresObjectInfo);
                allVersions.add(objectData);
            }

            // Add pwc if checked out
            //Skip if document was created as PWC
            String property = gregVersion.getNode().getProperty(CMISConstants.GREG_CREATED_AS_PWC);
            //TODO: property is checked for null. Might have to change this in the future.
            if (gregVersion.isDocumentCheckedOut() && (property == null) ) {
                RegistryPrivateWorkingCopy pwc = gregVersion.getPwc();
                ObjectData objectData = pwc.compileObjectType(splitFilter, includeAllowableActions, objectInfos,
                        requiresObjectInfo);

                allVersions.add(objectData);
            }

            // CMIS mandates descending order
            //Collections.reverse(allVersions);

            return allVersions;
        } else {
            // Single version
            ObjectData objectData = gregNode.compileObjectType(splitFilter, includeAllowableActions, objectInfos,
                    requiresObjectInfo);

            return Collections.singletonList(objectData);
        }

    }

    /**
     * See CMIS 1.0 section 2.2.6.1 query
     */
    public ObjectList query(String statement, Boolean searchAllVersions,
                            Boolean includeAllowableActions, BigInteger maxItems, BigInteger skipCount){
        //TODO: Not supported
        if(log.isTraceEnabled()) {
            log.trace("<<<<<<<<<<< query for the statement " + statement);
        }

        return new ObjectListImpl();
    }


    protected RepositoryInfo compileRepositoryInfo(String repositoryId) {
        RepositoryInfoImpl fRepositoryInfo = new RepositoryInfoImpl();

        fRepositoryInfo.setId(repositoryId);
        fRepositoryInfo.setName(getRepositoryName());
        fRepositoryInfo.setDescription(getRepositoryDescription());

        fRepositoryInfo.setCmisVersionSupported(CMISConstants.CMIS_VERSION);

        fRepositoryInfo.setProductName(CMISConstants.PRODUCT_NAME);
        fRepositoryInfo.setProductVersion("0.1");
        fRepositoryInfo.setVendorName(CMISConstants.VENDOR_NAME);
        fRepositoryInfo.setRootFolder("/");
        fRepositoryInfo.setThinClientUri("");

        RepositoryCapabilitiesImpl capabilities = new RepositoryCapabilitiesImpl();
        capabilities.setCapabilityAcl(CapabilityAcl.NONE);
        capabilities.setAllVersionsSearchable(false);
        capabilities.setCapabilityJoin(CapabilityJoin.NONE);
        capabilities.setSupportsMultifiling(false);
        capabilities.setSupportsUnfiling(false);
        capabilities.setSupportsVersionSpecificFiling(false);
        capabilities.setIsPwcSearchable(false);
        capabilities.setIsPwcUpdatable(true);
        capabilities.setCapabilityQuery(CapabilityQuery.NONE);
        capabilities.setCapabilityChanges(CapabilityChanges.NONE);
        capabilities.setCapabilityContentStreamUpdates(CapabilityContentStreamUpdates.ANYTIME);
        capabilities.setSupportsGetDescendants(true);
        capabilities.setSupportsGetFolderTree(true);
        capabilities.setCapabilityRendition(CapabilityRenditions.NONE);
        fRepositoryInfo.setCapabilities(capabilities);

        return fRepositoryInfo;
    }

    protected String getRepositoryName() {
    	return getRepositoryId();
    }

    protected String getRepositoryDescription() {
        return "CMIS Repository for Governance Registry";
    }

    protected RegistryObject getGregNode(String id) {
        try {
            if (id == null || id.length() == 0) {
                throw new CmisInvalidArgumentException("Null or empty id");
            }

            if (id.equals(PathManager.CMIS_ROOT_ID)) {
                return new FolderTypeHandler(repository, pathManager, typeManager).getGregNode(repository.get(id));
            }

            int k = id.indexOf(';');
            if (k >= 0) {
                //Version exists, however we must check whether the original resource is present.
                //If it doesn't, the version should be deleted and an exception will be thrown.
                //This is because in a CMIS repo, if the original resource is deleted then all
                // the versions should be deleted as well. When deleteTree is called although the original
                //resource is deleted, the versions exist. Hence for the following code.

                String originalResourceName = id.substring(0, id.indexOf(";"));
                if(!repository.resourceExists(originalResourceName)){

                    int beginIndex = id.indexOf(":")+1;
                    int endIndex = id.length();

                    String versionNumber = id.substring(beginIndex,endIndex);
                    long snapshotId = Long.parseLong(versionNumber);
                    repository.removeVersionHistory(originalResourceName, snapshotId);
                    throw new CmisObjectNotFoundException("Original resource of version does not exist");
                }

                //Node node = session.getNodeByIdentifier(nodeId);
                Resource node = null;

                node = repository.get(id);

                RegistryObject gregNode = null;
                if(node != null){
                	if(node instanceof Collection){
                		gregNode = new FolderTypeHandler(getRegistry(), pathManager, typeManager).getGregNode(node);
                	} else {
                		gregNode = new DocumentTypeHandler(getRegistry(), pathManager, typeManager).getGregNode(node);
                	}
                }
                //if (GregPrivateWorkingCopy.denotesPwc(versionName)) {
                //    return gregNode.asVersion().getPwc();
                //}
                //else {
                    return gregNode.asVersion().getVersion(id);
                //}
            } else {
            	Resource node = null;
                node = repository.get(id);
                RegistryObject gregNode = null;
                if(node!=null){
                	if(node instanceof Collection){
                		gregNode = new FolderTypeHandler(getRegistry(), pathManager, typeManager).getGregNode(repository.get(id));
                	} else {
                        //check if Unversioned type
                        String property = node.getProperty(CMISConstants.GREG_UNVERSIONED_TYPE);
                        if(property!=null && property.equals("true")){
                            gregNode = new UnversionedDocumentTypeHandler(getRegistry(), pathManager, typeManager).getGregNode(repository.get(id));
                            return gregNode;
                        }
                		gregNode = new DocumentTypeHandler(getRegistry(), pathManager, typeManager).getGregNode(repository.get(id));
                	    if (RegistryPrivateWorkingCopy.denotesPwc(repository, id)) {
                            return gregNode.asVersion().getPwc();
                        }
                    }
                }
                return gregNode;
            }

        } catch (RegistryException e) {
            String msg = "Failed to retrieve the node with id " + id;
            log.error(msg, e);
            throw new CmisObjectNotFoundException(msg, e);
        }
    }


    /**
     * Transitively gather the children of a node down to a specific depth
     */
    private static void gatherDescendants(RegistryFolder gregFolder, List<ObjectInFolderContainer> list,
            boolean foldersOnly, int depth, Set<String> filter, Boolean includeAllowableActions,
            Boolean includePathSegments, ObjectInfoHandler objectInfos, boolean requiresObjectInfo) {

        // iterate through children
        Iterator<RegistryObject> childNodes = gregFolder.getNodes();
        while (childNodes.hasNext()) {
            RegistryObject child = childNodes.next();

            // folders only?
            if (foldersOnly && !child.isFolder()) {
                continue;
            }

            // add to list
            ObjectInFolderDataImpl objectInFolder = new ObjectInFolderDataImpl();
            objectInFolder.setObject(child.compileObjectType(filter, includeAllowableActions, objectInfos,
                    requiresObjectInfo));

            if (Boolean.TRUE.equals(includePathSegments)) {
                objectInFolder.setPathSegment(child.getName());
            }

            ObjectInFolderContainerImpl container = new ObjectInFolderContainerImpl();
            container.setObject(objectInFolder);

            list.add(container);

            // move to next level
            if (depth != 1 && child.isFolder()) {
                container.setChildren(new ArrayList<ObjectInFolderContainer>());
                gatherDescendants(child.asFolder(), container.getChildren(), foldersOnly, depth - 1, filter,
                        includeAllowableActions, includePathSegments, objectInfos, requiresObjectInfo);
            }
        }
    }
    /**
     * Splits a filter statement into a collection of properties.
     */
    private static Set<String> splitFilter(String filter) {

        if (filter == null || filter.trim().length() == 0)
            return null;

        Set<String> result = new HashSet<String>();
        for (String s : filter.split(",")) {
            s = s.trim();
            if (s.equals("*")) {
                return null;
            } else if (s.length() > 0) {
                result.add(s);
            }
        }

        // set a few base properties
        // query name == id (for base type properties)
        result.add(PropertyIds.OBJECT_ID);
        result.add(PropertyIds.OBJECT_TYPE_ID);
        result.add(PropertyIds.BASE_TYPE_ID);

        return result;
    }
}

