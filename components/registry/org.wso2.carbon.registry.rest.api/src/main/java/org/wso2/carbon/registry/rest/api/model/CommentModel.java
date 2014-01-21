
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

import org.wso2.carbon.registry.core.Comment;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class provides the getters and setters to model the comment to a resource
 */
@XmlRootElement(name = "CommentModel")
public class CommentModel {

	private String authorUserName;
	private long commentID;
	private String commentPath;
	private String description;
	private String createdTime;
	
	public CommentModel(Comment comment) {
		this.authorUserName = comment.getAuthorUserName();
		String[] commentPath = comment.getCommentPath().split(":");
		this.commentID = Long.valueOf(commentPath[1]);
		this.commentPath = comment.getCommentPath();
		this.description = comment.getDescription();
		this.createdTime = comment.getCreatedTime().toGMTString();
	}

    public CommentModel() {
    }

    public String getAuthorUserName() {
		return authorUserName;
	}
	
	public long getCommentID() {
		return commentID;
	}
	
	public String getCommentPath() {
		return commentPath;
	}
	
	public String getCreatedTime() {
		return createdTime;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setAuthorUserName(String authorUserName) {
		this.authorUserName = authorUserName;
	}
	
	public void setCommentID(long commentID) {
		this.commentID = commentID;
	}
	
	public void setCommentPath(String commentPath) {
		this.commentPath = commentPath;
	}
	
	public void setCreatedTime(String createdTime) {
		this.createdTime = createdTime;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
}
