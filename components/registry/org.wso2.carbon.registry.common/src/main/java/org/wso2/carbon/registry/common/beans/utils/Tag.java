/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.common.beans.utils;

/**
 * Represents a tag and its metadata. Instances of this class is returned from the Registry
 * interface, when tags for a given resource path is queried.
 */
public class Tag {

    /**
     * Name of the tag. This may contain spaces.
     */
    private String tagName;

    /**
     * Number of taggings done using this tag. If a Tag object is returned as a result of a
     * Registry.getTags(String resourcePath) method, then this contains the number of users who
     * tagged the given resource using this tag.
     */
    private long tagCount;

    /**
     * Tags are categorized according the tag count. Then the category indicates the validity
     * of the tag. See setTagCount(...) method to understand how category is calculated based on
     * the tag count. This is used in the WSO2 Registry web UI to generate the tag cloud.
     */
    private int category = 1;

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public long getTagCount() {
        return tagCount;
    }

    public void setTagCount(long tagCount) {
        this.tagCount = tagCount;

        if (tagCount <= 2) {
            category = 1;
        } else if (tagCount > 2 && tagCount < 8) {
            category = 2;
        } else if (tagCount > 8 && tagCount < 20) {
            category = 3;
        } else if (tagCount > 20 && tagCount < 50) {
            category = 4;
        } else if (tagCount > 50) {
            category = 5;
        }
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }
}
