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

package org.wso2.carbon.registry.common;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.common.utils.CommonUtil;

import java.util.Calendar;

@SuppressWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class ResourceData {

    private String name;
    private String resourcePath;
    private String relativePath;

    /**
     * RealPath for link resources
     */
    private String realPath;
    /**
     * Resource, Collection or Unknown
     * Unknown type is set for entities, to which the current user doesn't have get permissions
     */
    private String resourceType;

    private String authorUserName;
    private String description;
    private float averageRating;
    private String[] averageStars = new String[5];
    private Calendar createdOn;
    private boolean deleteAllowed;
    private boolean putAllowed;
    private boolean getAllowed;
    private TagCount[] tagCounts;
    private boolean link;
    private boolean externalLink;
    private boolean mounted;
    private String absent = "false";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        String[] q = name.split("\\?");
        if (q.length == 2) {
            if (q[1].startsWith("v=")) {
                //String versionNumber = q[1].substring("v=".length());
                //name = q[0] + " (version " + versionNumber + ")";
                name = q[0];
            }
        }

        this.name = name;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;

        if (RegistryConstants.ROOT_PATH.equals(resourcePath)) {
            relativePath = "";
        } else {
            if (resourcePath.startsWith(RegistryConstants.PATH_SEPARATOR)) {
                relativePath = resourcePath.substring(1, resourcePath.length());
            }
        }
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getAuthorUserName() {
        return authorUserName;
    }

    public void setAuthorUserName(String authorUserName) {
        this.authorUserName = authorUserName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(float averageRating) {
        this.averageRating = averageRating;
    }

    public String[] getAverageStars() {
        return averageStars;
    }

    public void setAverageStars(String[] averageStars) {
        this.averageStars = averageStars;
    }

    public Calendar getCreatedOn() {
        return createdOn;
    }

    public String getFormattedCreatedOn() {
        return CommonUtil.formatDate(createdOn.getTime());
    }

    public void setCreatedOn(Calendar createdOn) {
        this.createdOn = createdOn;
    }

    public boolean isDeleteAllowed() {
        return deleteAllowed;
    }

    public void setDeleteAllowed(boolean deleteAllowed) {
        this.deleteAllowed = deleteAllowed;
    }

    public boolean isPutAllowed() {
        return putAllowed;
    }

    public void setPutAllowed(boolean putAllowed) {
        this.putAllowed = putAllowed;
    }

    public TagCount [] getTagCounts() {
        return tagCounts;
    }

    public void setTagCounts(TagCount [] tagCounts) {
        this.tagCounts = tagCounts;
    }

    public boolean isLink() {
        return link;
    }

    public void setLink(boolean link) {
        this.link = link;   
    }
    
    public boolean isExternalLink() {
        return externalLink;
    }

    public void setExternalLink(boolean externalLink) {
        this.externalLink = externalLink;
    }

    public boolean isMounted() {
        return this.mounted;
    }

    public void setMounted(boolean mounted) {
        this.mounted = mounted;
    }

    public void setGetAllowed(boolean getAllowed){
        this.getAllowed = getAllowed;
    }

    public boolean isGetAllowed(){
        return this.getAllowed;
    }

    public String getRealPath() {
        return realPath;
    }

    public void setRealPath(String realPath) {
        this.realPath = realPath;
    }

    public String getAbsent() {
        return absent;
    }

    public void setAbsent(String absent) {
        if(absent != null){
            this.absent = absent;
        }
    }
}
