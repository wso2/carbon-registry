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
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.server.ObjectInfoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.registry.cmis.util.CMISConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;


import java.io.BufferedInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Set;

/**
 * Instances of this class represent a cmis:document of an underlying Registry <code>Node</code>.
 */
public abstract class RegistryDocument extends RegistryObject{
	
	private static final Logger log = LoggerFactory.getLogger(RegistryDocument.class);

    public static final String MIME_UNKNOWN = "application/octet-stream";

	/**
     * Create a new instance wrapping a Registry <code>node</code>.
     *
     * @param resource           the Registry <code>node</code> to represent
     * @param typeManager
     * @param pathManager
     *
     */
    public RegistryDocument(Registry repository, Resource resource, RegistryTypeManager typeManager, PathManager pathManager) {
        super(repository, resource, typeManager, pathManager);
    }
    
    /**
     * @return  <code>true</code> if the document is checked out
     */
    public boolean isDocumentCheckedOut() {

        String property = getNode().getProperty(CMISConstants.GREG_IS_CHECKED_OUT);

        return (property != null && !property.equals("false"));

    }
    
    public ContentStream getContentStream(){

    	// compile data
        ContentStreamImpl result = new ContentStreamImpl();
        result.setFileName(getName());
            
        try {
            result.setLength(BigInteger.valueOf(getPropertyLength(getNode(), CMISConstants.GREG_DATA)));
            if(getNode().getContent() != null){
                String mimeType = getNode().getProperty(CMISConstants.GREG_MIMETYPE);
                result.setMimeType(mimeType);
                //result.setMimeType(getNode().getMediaType());
            } else {
                result.setMimeType(null);
            }
            if(getNode().getContent() != null){

                InputStream inputStream = getNode().getContentStream();
                result.setStream(new BufferedInputStream(inputStream));  // stream closed by consumer
            } else {
                result.setStream(null);
            }
        } catch (RegistryException e) {
            throw new CmisRuntimeException(e.getMessage(), e);
        }

        return result;
    }
    
    /**
     * See CMIS 1.0 section 2.2.4.16 setContentStream
     *
     * @throws CmisStorageException
     */
    public RegistryObject setContentStream(ContentStream contentStream, boolean overwriteFlag) {
        try {
            //Check for existing data
            Object dataObject = getNode().getContent();
            int length = 0;
            if (dataObject != null){
            	length = 1;
            }

            // check overwrite
            if (!overwriteFlag && length != 0) {
                throw new CmisContentAlreadyExistsException("Content already exists!");
            }

            RegistryVersionBase gregVersion = isVersionable() ? asVersion() : null;

            boolean autoCheckout = gregVersion != null && !gregVersion.isCheckedOut();
            RegistryVersionBase gregVersionContext = null;
            if (autoCheckout) {
                gregVersionContext = gregVersion.checkout();
            } else {
                gregVersionContext = gregVersion;
            }

            Resource resource = null;
            // write content, if available
            if(contentStream == null || contentStream.getStream() == null){

               resource = gregVersionContext.getNode();
               resource.setContent(null);
            } else{
            	//Sets the content stream
                resource = gregVersionContext.getNode();
            	resource.setContentStream(contentStream.getStream());
                //TODO MIME-Type --> DONE
            	//contentStream.getMimeType();
            }
            //put to registry
            String versionContextPath = gregVersionContext.getNode().getPath();
            getRepository().put(versionContextPath, resource);

            //set
            gregVersionContext.setNode(getRepository().get(versionContextPath));

            if (autoCheckout) {
                // auto versioning -> return new version created by checkin
                return gregVersionContext.checkin(null, null, "auto checkout");
            } else if (gregVersionContext != null) {
                // the node is checked out -> return pwc.
                return gregVersionContext.getPwc();
            } else {
                // non versionable -> return this
                return this;
            }
        }
        catch (RegistryException e) {
            log.error("Error occurred while setting content stream ", e);
            throw new CmisStorageException(e.getMessage());
        }

    }
    // TODO Remove
    /*private String convertStreamToString(java.io.InputStream is) {
        try {
            return new java.util.Scanner(is).useDelimiter("\\A").next();
        } catch (java.util.NoSuchElementException e) {
            return "";
        }
    }                */

    @Override
    protected String getTypeIdInternal() {
        return RegistryTypeManager.DOCUMENT_TYPE_ID;
    }

    @Override
    protected BaseTypeId getBaseTypeId() {
        return BaseTypeId.CMIS_DOCUMENT;
    }
   
    /* TODO in later extensions
	protected GregObject create(Resource resource) {
		// TODO Auto-generated method stub
		//new DefaultDocumentHandler().createDocument()
		return null;
	}
   */
    
    /**
     * @return  the value of the <code>cmis:isLatestVersion</code> property
     * @throws RegistryException
     */
    protected abstract boolean isLatestVersion() throws RegistryException;

    /**
     * @return  the value of the <code>cmis:isMajorVersion</code> property
     * @throws RegistryException
     */
    protected abstract boolean isMajorVersion() throws RegistryException;

    /**
     * @return  the value of the <code>cmis:isLatestMajorVersion</code> property
     * @throws RegistryException
     */
    protected abstract boolean isLatestMajorVersion() throws RegistryException;

    /**
     * @return  the value of the <code>cmis:versionLabel</code> property
     * @throws RegistryException
     */
    protected abstract String getVersionLabel() throws RegistryException;

    /**
     * @return  the value of the <code>cmis:isVersionSeriesCheckedOut</code> property
     * @throws RegistryException
     */
    protected abstract boolean isCheckedOut() throws RegistryException;

    /**
     * @return  the value of the <code>cmis:versionSeriesCheckedOutId</code> property
     * @throws RegistryException
     */
    protected abstract String getCheckedOutId() throws RegistryException;

    /**
     * @return  the value of the <code>cmis:versionSeriesCheckedOutBy</code> property
     * @throws RegistryException
     */
    protected abstract String getCheckedOutBy() throws RegistryException;


    /**
     * @return  the value of the <code>cmis:checkinComment</code> property
     * @throws RegistryException
     */
    protected abstract String getCheckInComment() throws RegistryException;

    protected boolean getIsImmutable() {
        return false;
    }
    
    @Override
    protected void compileProperties(PropertiesImpl properties, Set<String> filter, ObjectInfoImpl objectInfo)
            throws RegistryException {

        super.compileProperties(properties, filter, objectInfo);


        if(getNode().getContent()!=null){
            objectInfo.setHasContent(true);
        } else{
            objectInfo.setHasContent(false);
        }
        objectInfo.setHasParent(true);
        objectInfo.setSupportsDescendants(false);
        objectInfo.setSupportsFolderTree(false);

        String typeId = getTypeIdInternal();
        Resource contextNode = getContextNode();

        // mutability
        addPropertyBoolean(properties, typeId, filter, PropertyIds.IS_IMMUTABLE, getIsImmutable());

        // content stream
        long length = getPropertyLength(contextNode,CMISConstants.GREG_DATA);
        addPropertyInteger(properties, typeId, filter, PropertyIds.CONTENT_STREAM_LENGTH, length);

        // mime type
        String mimeType = getPropertyOrElse(contextNode, CMISConstants.GREG_MIMETYPE, MIME_UNKNOWN);
        addPropertyString(properties, typeId, filter, PropertyIds.CONTENT_STREAM_MIME_TYPE, mimeType);
        objectInfo.setContentType(mimeType);

        // file name
        String fileName = getNodeName();
        addPropertyString(properties, typeId, filter, PropertyIds.CONTENT_STREAM_FILE_NAME, fileName);
        objectInfo.setFileName(fileName);

        addPropertyId(properties, typeId, filter, PropertyIds.CONTENT_STREAM_ID, getObjectId() + "/stream");

        // versioning
        addPropertyBoolean(properties, typeId, filter, PropertyIds.IS_LATEST_VERSION, isLatestVersion());
        addPropertyBoolean(properties, typeId, filter, PropertyIds.IS_MAJOR_VERSION, isMajorVersion());
        addPropertyBoolean(properties, typeId, filter, PropertyIds.IS_LATEST_MAJOR_VERSION, isLatestMajorVersion());
        addPropertyString(properties, typeId, filter, PropertyIds.VERSION_LABEL, getVersionLabel());
        addPropertyId(properties, typeId, filter, PropertyIds.VERSION_SERIES_ID, getVersionSeriesId());
        addPropertyString(properties, typeId, filter, PropertyIds.CHECKIN_COMMENT, getCheckInComment());

        boolean isCheckedOut = isCheckedOut();
        addPropertyBoolean(properties, typeId, filter, PropertyIds.IS_VERSION_SERIES_CHECKED_OUT, isCheckedOut);

        if (isCheckedOut) {
            addPropertyId(properties, typeId, filter, PropertyIds.VERSION_SERIES_CHECKED_OUT_ID, getCheckedOutId());
            //addPropertyString(properties, typeId, filter, PropertyIds.VERSION_SERIES_CHECKED_OUT_BY, getCheckedOutBy());
        } else{
            addPropertyId(properties, typeId, filter, PropertyIds.VERSION_SERIES_CHECKED_OUT_ID, CMISConstants.GREG_PROPERTY_NOT_SET);
            //addPropertyString(properties, typeId, filter, PropertyIds.VERSION_SERIES_CHECKED_OUT_BY, null);
        }

        addPropertyString(properties, typeId, filter, PropertyIds.VERSION_SERIES_CHECKED_OUT_BY, getCheckedOutBy());
    }

    @Override
    protected Set<Action> compileAllowableActions(Set<Action> aas) {
        Set<Action> result = super.compileAllowableActions(aas);
        try {
            boolean status = getNode().getContentStream() != null ? true : false;
            setAction(result, Action.CAN_GET_CONTENT_STREAM, status);
        } catch (RegistryException e) {
            log.error("Failed to get the content stream for the node " + getNode().getId() + " " , e);
            setAction(result, Action.CAN_GET_CONTENT_STREAM, false);
        }

        setAction(result, Action.CAN_SET_CONTENT_STREAM, true);
        setAction(result, Action.CAN_DELETE_CONTENT_STREAM, true);
        setAction(result, Action.CAN_GET_RENDITIONS, false);
        return result;

    }

}
