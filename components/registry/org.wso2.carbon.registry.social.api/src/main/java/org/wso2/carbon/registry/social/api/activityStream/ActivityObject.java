/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.social.api.activityStream;

import java.util.List;

/**
 * A representation of an Activity's object.
 * <p/>
 * Note that an Activity's object may contain fields from an Activity when
 * the objectType is of type 'activity'.  As such, ActivityObject becomes
 * a superset of Activity.  Refer to the Activity Streams spec.
 */
public interface ActivityObject {

    /**
     * Fields that represent the JSON elements.
     */
    public static enum Field {
        // Activity's object fields

        /**
         * the json field for attachments- A collection of one or more additional, associated objects,
         * similar to the concept of attached files in an email message.
         */
        ATTACHMENTS("attachments"),
        /*
        the json field for author-the entity that created or authored the activity object.
        */
        AUTHOR("author"),
        /*
        the json field for content-Visual elements such as thumbnail images.
        */
        CONTENT("content"),
        /*
        the json field for display name of the activity object.
        */
        DISPLAY_NAME("displayName"),
        /*
       the json field for downstream duplicates property to identify activity objects that duplicate
       this object's content.[when there are known objects, possibly in a different system,
       that duplicate the content in this object.]
        */
        DOWNSTREAM_DUPLICATES("downstreamDuplicates"),
        /*
       the json field for id of activity object.
        */
        ID("id"),
        /*
       the json field for image of the activity object.
        */
        IMAGE("image"),
        /*
       the json field for activity object type
        */
        OBJECT_TYPE("objectType"),
        /*
       the json field for published property-The date and time at which the object was published.
        */
        PUBLISHED("published"),
        /*
       the json field for summary of the activity object.
        */
        SUMMARY("summary"),
        /*
       the json field for updated property-The date and time at which a previously published object
       has been modified
        */
        UPDATED("updated"),
        /*
       the json field for upstream duplicates to identify activity objects that duplicate this
       object's content.[when a publisher is knowingly duplicating with a new ID the content from
       another activity object.
        */
        UPSTREAM_DUPLICATES("upstreamDuplicates"),
        /*
       the json field for url-a resource providing an HTML representation of the activity object.
        */
        URL("url");


        // The name of the JSON element
        private final String jsonString;

        /**
         * Constructs the field base for the JSON element.
         *
         * @param jsonString the name of the element
         */
        private Field(String jsonString) {
            this.jsonString = jsonString;
        }

        /**
         * Returns the name of the JSON element.
         *
         * @return String the name of the JSON element
         */
        public String toString() {
            return jsonString;
        }
    }

    /**
     * <p>getAttachments</p>
     *
     * @return a list of {@link org.wso2.carbon.registry.social.api.activityStream.ActivityObject} object
     */
    List<ActivityObject> getAttachments();

    /**
     * <p>setAttachments</p>
     *
     * @param attachments a list of {@link org.wso2.carbon.registry.social.api.activityStream.ActivityObject} objects
     */
    void setAttachments(List<ActivityObject> attachments);

    /**
     * <p>getAuthor</p>
     *
     * @return a {@link org.wso2.carbon.registry.social.api.activityStream.ActivityObject} object
     */
    ActivityObject getAuthor();

    /**
     * <p>setAuthor</p>
     *
     * @param author a {@link org.wso2.carbon.registry.social.api.activityStream.ActivityObject} object
     */
    void setAuthor(ActivityObject author);

    /**
     * <p>getContent</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getContent();

    /**
     * <p>setContent</p>
     *
     * @param content a {@link java.lang.String} object.
     */
    void setContent(String content);

    /**
     * <p>getDisplayName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getDisplayName();

    /**
     * <p>setDisplayName</p>
     *
     * @param displayName a {@link java.lang.String} object
     */
    void setDisplayName(String displayName);

    /**
     * <p>getDownstreamDuplicates</p>
     *
     * @return a list of {@link java.lang.String} objects
     */
    List<String> getDownstreamDuplicates();

    /**
     * <p>setDownstreamDuplicates</p>
     *
     * @param downstreamDuplicates a list of {@link java.lang.String} objects
     */
    void setDownstreamDuplicates(List<String> downstreamDuplicates);

    /**
     * <p>getId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getId();

    /**
     * <p>setId</p>
     *
     * @param id a {@link java.lang.String} object.
     */
    void setId(String id);

    /**
     * <p>getImage</p>
     *
     * @return a {@link org.wso2.carbon.registry.social.api.activityStream.MediaLink} object
     */
    MediaLink getImage();

    /**
     * <p>setImage</p>
     *
     * @param image a {@link org.wso2.carbon.registry.social.api.activityStream.MediaLink} object
     */
    void setImage(MediaLink image);

    /**
     * <p>getObjectType</p>
     *
     * @return a {@link java.lang.String} object
     */
    String getObjectType();

    /**
     * <p>setObjectType</p>
     *
     * @param objectType a {@link java.lang.String} object
     */
    void setObjectType(String objectType);

    /**
     * <p>getPublished</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getPublished();

    /**
     * <p>setPublished</p>
     *
     * @param published a {@link java.lang.String} object.
     */
    void setPublished(String published);

    /**
     * <p>getSummary</p>
     *
     * @return a {@link java.lang.String} object
     */
    String getSummary();

    /**
     * <p>setSummary</p>
     *
     * @param summary a {@link java.lang.String} object
     */
    void setSummary(String summary);

    /**
     * <p>getUpdated</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getUpdated();

    /**
     * <p>setUpdated</p>
     *
     * @param updated a {@link java.lang.String} object.
     */
    void setUpdated(String updated);

    /**
     * <p>getUpstreamDuplicates</p>
     *
     * @return a list of {@link java.lang.String} objects
     */
    List<String> getUpstreamDuplicates();

    /**
     * <p>setUpstreamDuplicates</p>
     *
     * @param upstreamDuplicates a list of {@link java.lang.String} objects
     */
    void setUpstreamDuplicates(List<String> upstreamDuplicates);

    /**
     * <p>getUrl</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getUrl();

    /**
     * <p>setUrl</p>
     *
     * @param url a {@link java.lang.String} object.
     */
    void setUrl(String url);

}
