/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.registry.ws.api;

import javax.activation.DataHandler;


/**
 * The WSResource class is a web service compatible representation of the Resource class.
 *
 */
public class WSResource {

    private DataHandler contentFile;
    private boolean isCollection;
    
    private String authorUserName;
    private long createdTime;
    private int dbBasedContentID;
    private String description;
    private boolean directory;
    private String id;
    private long lastModified;
    private String lastUpdaterUserName;
    private long matchingSnapshotID;
    private String mediaType;
    protected String name;
    private String parentPath;
    private String path;
    protected int pathID;
    private String permanentPath;
    private WSProperty[] properties;
    private boolean propertiesModified;
    private long snapshotID;
    private int state;
    protected int tenantId;
    private String userName;
    private boolean versionableChange;
    private long versionNumber;
    private String uuid;

    /**
     * get the UUID of the resource
     *
     * @return the uuid of the resource
     */
    public String getUUID() {
        return uuid;
    }

    /**
     * set the UUID of the resource
     *
     * @param uuid the uuid of the resource
     */
    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    /**
     * Returns a data handler that contains the content. Used to deliver content through web services.
     * 
     * @return content
     */
    public DataHandler getContentFile() {
        return contentFile;
    }
    
    /**
     * Sets the data handler with the content of the resource.
     * 
     * @param contentFile
     */
    public void setContentFile(DataHandler contentFile) {
        this.contentFile = contentFile;
    }
    
    /**
     * Get the user name of the resource author.
     *
     * @return the user name of the resource author.
     */
    public String getAuthorUserName() {
        return authorUserName;
    }
    
    /**
     * Set the author name. Needed when creating WSResources to transfer while 
     * preserving the original author.
     * 
     * @param authorUserName
     */
    public void setAuthorUserName(String authorUserName) {
        this.authorUserName = authorUserName;
    }
    
    public int getDbBasedContentID() {
        return dbBasedContentID;
    }
    public void setDbBasedContentID(int dbBasedContentID) {
        this.dbBasedContentID = dbBasedContentID;
    }
    
    /**
     * Method to get the description.
     *
     * @return the description.
     */
    public String getDescription() {
        return description;
    }
    
    /** 
     * Set the description 
     * 
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    public boolean isDirectory() {
        return directory;
    }
    public void setDirectory(boolean directory) {
        this.directory = directory;
    }
    
    /**
     * The Resource ID, In the default implementation this returns the path.
     *
     * @return the resource id
     */
    public String getId() {
        return id;
    }
    
    /**
     * Set the resource ID. The default is the path of the resource.
     * 
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Method to get the last modified date as a long integer.
     *
     * @return the last modified date.
     */
    public long getLastModified() {
        return lastModified;
    }
    
    /**
     * Method to set the last modified date.
     * 
     * @param lastModified as a long integer
     */
    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }
    
    /**
     * Method to get the last updated user name.
     *
     * @return the last updated user name.
     */
    public String getLastUpdaterUserName() {
        return lastUpdaterUserName;
    }
    
    /**
     * Set the last updated user name.
     * 
     * @param lastUpdaterUserName
     */
    public void setLastUpdaterUserName(String lastUpdaterUserName) {
        this.lastUpdaterUserName = lastUpdaterUserName;
    }
    
    
    public long getMatchingSnapshotID() {
        return matchingSnapshotID;
    }
    public void setMatchingSnapshotID(long matchingSnapshotID) {
        this.matchingSnapshotID = matchingSnapshotID;
    }
    
    /**
     * Get media type.
     *
     * @return the media type.
     */
    public String getMediaType() {
        return mediaType;
    }
    
    /**
     * Set media type.
     * 
     * @param mediaType
     */
    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Get the parent path.
     *
     * @return the parent path.
     */
    public String getParentPath() {
        return parentPath;
    }
    
    /**
     * Set the parent path,
     * 
     * @param parentPath
     */
    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }
    
    /**
     * Method to get the path. the unique identifier of the resources in the present state.
     *
     * @return the path.
     */
    public String getPath() {
        return path;
    }
    
    /**
     * Set the path.
     * 
     * @param path
     */
    public void setPath(String path) {
        this.path = path;
    }
    
    
    public int getPathID() {
        return pathID;
    }
    public void setPathID(int pathID) {
        this.pathID = pathID;
    }
    
    
    public String getPermanentPath() {
        return permanentPath;
    }
    public void setPermanentPath(String permanentPath) {
        this.permanentPath = permanentPath;
    }
    
    /**
     * Returns all properties of the WSResource. Properties are stored as key (String) -> values
     * (List) pairs. 
     * 
     * @return All properties of the resource as WSProperty array.
     */
    public WSProperty[] getProperties() {
        return properties;
    }
    
    /**
     * Set properties of a WSResource.
     *  
     * @param properties
     */
    public void setProperties(WSProperty[] properties) {
        this.properties = properties;
    }
    public boolean isPropertiesModified() {
        return propertiesModified;
    }
    public void setPropertiesModified(boolean propertiesModified) {
        this.propertiesModified = propertiesModified;
    }
    public long getSnapshotID() {
        return snapshotID;
    }
    public void setSnapshotID(long snapshotID) {
        this.snapshotID = snapshotID;
    }
    public int getState() {
        return state;
    }
    public void setState(int state) {
        this.state = state;
    }
    public int getTenantId() {
        return tenantId;
    }
    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public boolean isVersionableChange() {
        return versionableChange;
    }
    public void setVersionableChange(boolean versionableChange) {
        this.versionableChange = versionableChange;
    }
    public long getVersionNumber() {
        return versionNumber;
    }
    public void setVersionNumber(long versionNumber) {
        this.versionNumber = versionNumber;
    }
    
    /**
     * Method to get the created time.
     *
     * @return the created time
     */
    public long getCreatedTime() {
        return createdTime;
    }
    
    /**
     * Set created time. Needed when creating WSResources to transfer while 
     * preserving the original created time.
     * 
     * @param createdTime
     */
    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }
    
    /**
     * Check whether this WSResource is actually a WSCollection.
     * This method is used since web services do not support inheritance.
     * 
     * @return whether this is a collection or not
     */
    public boolean isCollection() {
        return isCollection;
    }
    
    /**
     * Set whether the WSResource is a WSCollection or not.
     * 
     * @param isCollection
     */
    public void setCollection(boolean isCollection) {
        this.isCollection = isCollection;
    }
    
    
}