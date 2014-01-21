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

package org.wso2.carbon.registry.cmis.impl;

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.DocumentTypeDefinitionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.registry.cmis.util.CMISConstants;
import org.wso2.carbon.registry.cmis.util.CommonUtil;

import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.cmis.*;

/**
 * Type handler that provides cmis:document.
 */
public class DocumentTypeHandler extends AbstractTypeHandler {

    public DocumentTypeHandler(Registry repository, PathManager pathManager,
                               RegistryTypeManager typeManager) {
		super(repository, pathManager, typeManager);
	}

	private static final Logger log = LoggerFactory.getLogger(DocumentTypeHandler.class);

    public String getTypeId() {
        return BaseTypeId.CMIS_DOCUMENT.value();
    }

    public TypeDefinition getTypeDefinition() {
        DocumentTypeDefinitionImpl documentType = new DocumentTypeDefinitionImpl();
        documentType.setBaseTypeId(BaseTypeId.CMIS_DOCUMENT);
        documentType.setIsControllableAcl(false);
        documentType.setIsControllablePolicy(false);
        documentType.setIsCreatable(true);
        documentType.setDescription("Document");
        documentType.setDisplayName("Document");
        documentType.setIsFileable(true);
        documentType.setIsFulltextIndexed(false);
        documentType.setIsIncludedInSupertypeQuery(true);
        documentType.setLocalName("Document");
        documentType.setLocalNamespace(RegistryTypeManager.NAMESPACE);
        documentType.setIsQueryable(true);
        documentType.setQueryName(RegistryTypeManager.DOCUMENT_TYPE_ID);
        documentType.setId(RegistryTypeManager.DOCUMENT_TYPE_ID);
        documentType.setIsVersionable(true);
        documentType.setContentStreamAllowed(ContentStreamAllowed.ALLOWED);

        RegistryTypeManager.addBasePropertyDefinitions(documentType);
        RegistryTypeManager.addDocumentPropertyDefinitions(documentType);

        return documentType;
    }


    //Method can be replaced with GREG. Returns GregDocument
    public RegistryDocument getGregNode(Resource node) throws RegistryException {
        String version = node.getPath();
        String[] versions = repository.getVersions(node.getPath());

        if(versions !=null && versions.length != 0){
            version = versions[0];
        }
    	return new RegistryVersion(repository, node, version, typeManager, pathManager);
    }

    
    public RegistryObject createDocument(RegistryFolder parentFolder, String name, Properties properties, ContentStream contentStream, VersioningState versioningState) {
        try {
        	Resource fileNode = repository.newResource();
        	
        	// write content, if available
            if(contentStream != null && contentStream.getStream() != null){
            	//set stream
                fileNode.setProperty(CMISConstants.GREG_DATA, "true");
            	fileNode.setContentStream(contentStream.getStream());
            }

            //Put to registry AS A PWC (Look at getDestPathOfNode() )
            String destinationPath = CommonUtil.getTargetPathOfNode(parentFolder, name);
            repository.put(destinationPath, fileNode);
            fileNode = repository.get(destinationPath);
            
        	// compile the properties
            RegistryFolder.setProperties(repository, fileNode, getTypeDefinition(), properties);

            //Set MIMETYPE
            if (contentStream != null && contentStream.getMimeType() != null) {
            	fileNode.setProperty(CMISConstants.GREG_MIMETYPE, contentStream.getMimeType());
                fileNode.setMediaType(contentStream.getMimeType());
            }

            repository.put(destinationPath, fileNode);
            fileNode = repository.get(destinationPath);

            if (versioningState == VersioningState.NONE) {
                fileNode.setProperty(CMISConstants.GREG_UNVERSIONED_TYPE, "true");
                repository.put(destinationPath, fileNode);
                return new RegistryUnversionedDocument(repository, fileNode, typeManager, pathManager);
            }

            //Else, create as a PWC. See spec
            //TODO Set the destination of this PWC to a temp and put it to it's intended location when checked in
            fileNode.setProperty(CMISConstants.GREG_IS_CHECKED_OUT, "true");

            //Put to registry
            repository.put(destinationPath, fileNode);

            RegistryObject gregFileNode = getGregNode(fileNode);
            RegistryVersionBase gregVersion = gregFileNode.asVersion();
            if(versioningState==VersioningState.CHECKEDOUT){

                //Put to checked out tracker
                Resource resource = null;
                if(repository.resourceExists(CMISConstants.GREG_CHECKED_OUT_TRACKER)){
                    resource  = repository.get(CMISConstants.GREG_CHECKED_OUT_TRACKER);
                } else{
                    resource = repository.newResource();
                    //Have to set content, otherwise Greg will throw exception when browsing this file in Workbench
                    resource.setContent("tracker");
                }
                resource.setProperty(gregVersion.getNode().getPath(), "true");
                repository.put(CMISConstants.GREG_CHECKED_OUT_TRACKER, resource);

                //Set property saying this was created as a PWC
                gregVersion.getNode().setProperty(CMISConstants.GREG_CREATED_AS_PWC, "true");
                repository.put(gregVersion.getNode().getPath(), gregVersion.getNode());
                gregVersion = getGregNode(repository.get(gregVersion.getNode().getPath())).asVersion();
                return gregVersion.getPwc();
            } else  {
                if( versioningState == VersioningState.MAJOR){
                    gregVersion.getNode().addProperty(CMISConstants.GREG_VERSION_STATE, CMISConstants.GREG_MAJOR_VERSION);
                } else if (versioningState==VersioningState.MINOR){
                    gregVersion.getNode().addProperty(CMISConstants.GREG_VERSION_STATE, CMISConstants.GREG_MINOR_VERSION);
                }
                //put properties
                repository.put(gregVersion.getNode().getPath(), gregVersion.getNode());

                return gregVersion.checkin(null, null, "auto checkin");
            }
        }
        catch (RegistryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisStorageException(e.getMessage(), e);
        }
    }
}

