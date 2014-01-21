/*
 *  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.ws.api;

public class WSResourceData {

    private WSResource resource;
    private WSAssociation[] associations;
    private WSTag[] tags;
    private WSComment[] comments;
    private int rating;
    private float averageRating;

    public WSResource getResource() {
        return resource;
    }

    public void setResource(WSResource resource) {
        this.resource = resource;
    }

    public WSAssociation[] getAssociations() {
        return associations;
    }

    public void setAssociations(WSAssociation[] associations) {
        this.associations = associations;
    }

    public WSTag[] getTags() {
        return tags;
    }

    public void setTags(WSTag[] tags) {
        this.tags = tags;
    }

    public WSComment[] getComments() {
        return comments;
    }

    public void setComments(WSComment[] comments) {
        this.comments = comments;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public float getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(float averageRating) {
        this.averageRating = averageRating;
    }
}
