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


    /**
     * <p>Represents an 'Activity' within the Activity Streams JSON 1.0
     * specification.  Refer to http://activitystrea.ms/head/json-activity.html</p>
     */

    public interface ActivityEntry {

        /**
         * Fields that represent the JSON elements.
         */
        public static enum Field {
            /*
            the json field for actor-The entity that performed the activity.
            */
            ACTOR("actor"),
            /*
            the json field for content-Visual elements such as thumbnail images MAY be included.
            */
            CONTENT("content"),
            /*
           the json field for generator- the application that generated the activity.
            */
            GENERATOR("generator"),
            /*
           the json field for icon of an activity
            */
            ICON("icon"),
            /*
            the json field for activity id
            */
            ID("id"),
            /*
            the json field for object-the primary object of the activity.
            */
            OBJECT("object"),
            /*
           the json field for published property-The date and time at which the activity was published.
            */
            PUBLISHED("published"),
            /*
            the json field for provider-the application that published the activity.
            */
            PROVIDER("provider"),
            /*
            the json field for target-the target of the activity.
            */
            TARGET("target"),
            /*
            the json field for title for the activity.
            */
            TITLE("title"),
            /*
            the json field for the date and time at which a previously published activity has been modified.
            */
            UPDATED("updated"),
            /*
            the json field for url-a resource providing an HTML representation of the activity.
            */
            URL("url"),
            /*
            the json field for verb-The action that the activity describes.If the verb is not specified
            ,or if the value is null, the verb is assumed to be "post".
            */
            VERB("verb");


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
         * <p>getActor</p>
         *
         * @return a {@link org.wso2.carbon.registry.social.api.activityStream.ActivityObject} object.
         */
        ActivityObject getActor();

        /**
         * <p>setActor</p>
         *
         * @param actor a {@link org.wso2.carbon.registry.social.api.activityStream.ActivityObject} object.
         */
        void setActor(ActivityObject actor);

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
         * <p>getGenerator</p>
         *
         * @return a {@link org.wso2.carbon.registry.social.api.activityStream.ActivityObject} object.
         */
        ActivityObject getGenerator();

        /**
         * <p>setGenerator</p>
         *
         * @param generator a {@link org.wso2.carbon.registry.social.api.activityStream.ActivityObject} object.
         */
        void setGenerator(ActivityObject generator);

        /**
         * <p>getIcon</p>
         *
         * @return a {@link org.wso2.carbon.registry.social.api.activityStream.MediaLink} object.
         */
        MediaLink getIcon();

        /**
         * <p>setIcon</p>
         *
         * @param icon a {@link org.wso2.carbon.registry.social.api.activityStream.MediaLink} object.
         */
        void setIcon(MediaLink icon);

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
         * <p>getObject</p>
         *
         * @return a {@link org.wso2.carbon.registry.social.api.activityStream.ActivityObject} object.
         */
        ActivityObject getObject();

        /**
         * <p>setObject</p>
         *
         * @param object a {@link org.wso2.carbon.registry.social.api.activityStream.ActivityObject} object.
         */
        void setObject(ActivityObject object);

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
         * <p>getProvider</p>
         *
         * @return a {@link org.wso2.carbon.registry.social.api.activityStream.ActivityObject} object.
         */
        ActivityObject getProvider();

        /**
         * <p>setServiceProvider</p>
         *
         * @param provider a {@link org.wso2.carbon.registry.social.api.activityStream.ActivityObject} object.
         */
        void setProvider(ActivityObject provider);

        /**
         * <p>getTarget</p>
         *
         * @return a {@link org.wso2.carbon.registry.social.api.activityStream.ActivityObject} object.
         */
        ActivityObject getTarget();

        /**
         * <p>setTarget</p>
         *
         * @param target a {@link org.wso2.carbon.registry.social.api.activityStream.ActivityObject} object.
         */
        void setTarget(ActivityObject target);

        /**
         * <p>getTitle</p>
         *
         * @return a {@link java.lang.String} object.
         */
        String getTitle();

        /**
         * <p>setTitle</p>
         *
         * @param title a {@link java.lang.String} object.
         */
        void setTitle(String title);

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

        /**
         * <p>getVerb</p>
         *
         * @return a {@link java.lang.String} object.
         */
        String getVerb();

        /**
         * <p>setVerb</p>
         *
         * @param verb a {@link java.lang.String} object.
         */
        void setVerb(String verb);

    }


