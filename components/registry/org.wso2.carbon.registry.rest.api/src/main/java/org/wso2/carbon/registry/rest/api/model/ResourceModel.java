
/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.rest.api.model;

import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class provides getters and setters to model the meta information of a resource
 */
@XmlRootElement(name = "ResourceModel")
public class ResourceModel {
	private String mediaType;
	private String uuid;
	private String authorUsername;
	private String lastModifiedUsername;
	private String createdTime;
	private String lastModifiedTime;
	private String description;
	
	public ResourceModel(Resource resource) throws RegistryException 
	{
		this.mediaType = resource.getMediaType();
		this.uuid = resource.getUUID();
		this.authorUsername = resource.getAuthorUserName();
		this.lastModifiedUsername = resource.getLastUpdaterUserName();
		this.description = resource.getDescription();
		this.createdTime = resource.getCreatedTime().toGMTString();
		this.lastModifiedTime = resource.getLastModified().toGMTString();		
	}

    public ResourceModel() {
    }

    public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}

	public String getMediaType() {
		return mediaType;
	}

	public String getAuthorUsername() {
		return authorUsername;
	}

	public void setAuthorUsername(String authorUsername) {
		this.authorUsername = authorUsername;
	}

	public String getLastModifiedUsername() {
		return lastModifiedUsername;
	}

	public void setLastModifiedUsername(String lastModifiedUsername) {
		this.lastModifiedUsername = lastModifiedUsername;
	}

	public String getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(String createdTime) {
		this.createdTime = createdTime;
	}

	public String getLastModifiedTime() {
		return lastModifiedTime;
	}

	public void setLastModifiedTime(String lastModifiedTime) {
		this.lastModifiedTime = lastModifiedTime;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	
}
