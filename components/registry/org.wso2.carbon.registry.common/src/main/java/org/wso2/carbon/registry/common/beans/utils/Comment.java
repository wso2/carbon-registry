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
package org.wso2.carbon.registry.common.beans.utils;

import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.Calendar;

/**
 * Represents comments and its metadata. Note that only the Comment.text field needs to be filled
 * when adding new comments. All other attributes are ignored and they are filled with appropriate
 * values for the current context. Therefore, when constructing an instance of this class outside
 * the Registry impl, it is recommended to use new Comment("my comment text") constructor.
 */
public class Comment {

    //TODO: There are so many duplicate methods in this class. Deprecate everything unwanted.

    /**
     * Path of the comment. Each comment has a path in the form /projects/esb/config.xml;comments:12
     *
     */
    private String commentPath;

    /**
     * Comment text. This may contain any string including HTML segments.
     */
    private String text;

    /**
     * Username of the user who added this comment.
     */
    private String user;

    /**
     * Date and time at which this comment is added.
     */
    private Calendar time;

    /**
     * Path of the resource on which this comment is made.
     */
    private String resourcePath;

    public Comment() {
    }

    public Comment(String commentText) {
        this.text = commentText;
        time = Calendar.getInstance();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    /**
     * Get the comment time.
     * @return the time the comment was made.
     * @deprecated please use getCreatedTime() instead
     */
    public Calendar getTime() {
        return time;
    }

    public void setTime(Calendar time) {
        this.time = time;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getContent() throws RegistryException {
        return getText();
    }

    public void setContent(Object content) throws RegistryException {
        setText((String)content);
    }

    public String getDescription() {
        return getText();
    }

    public void setDescription(String description) {
        setText(description);
    }

    public String getCommentPath() {
        return commentPath;
    }

    public void setCommentPath(String commentPath) {
        this.commentPath = commentPath;
    }

    public String getMediaType() {
        return "application/atom+xml";
    }

    @Deprecated
    @SuppressWarnings("unused")
    public void setMediaType(String mediaType) {
    }

    public String getAuthorUserName() {
        return user;
    }

    public void setAuthorUserName(String user) {
        this.user = user;
    }

    public Calendar getLastModified() {
        return time;
    }

    public void setLastModified(Calendar time) {
        this.time = time;
    }

    public Calendar getCreatedTime() {
        return time;
    }

    public void setCreatedTime(Calendar time) {
        this.time = time;
    }
}

