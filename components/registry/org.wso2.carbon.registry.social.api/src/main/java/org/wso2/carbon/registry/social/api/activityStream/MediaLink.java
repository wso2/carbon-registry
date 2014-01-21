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
 * <p>MediaLink interface.</p>
 */


public interface MediaLink {

    /**
     * Fields that represent the JSON elements.
     */
    public static enum Field {

        /**
         * the json field for duration-the length, in seconds, of the media resource.
         */
        DURATION("duration"),
        /**
         * the json field for the height, in pixels, of the media resource.
         */
        HEIGHT("height"),
        /**
         * the json field for url of the media resource being linked.
         */
        URL("url"),
        /**
         * the json field for the width, in pixels, of the media resource.
         */
        WIDTH("width");


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
     * Returns the duration of this media.
     *
     * @return Integer is the target's duration
     */
    Integer getDuration();

    /**
     * Sets the duration of this media.
     *
     * @param duration is the target's duration
     */
    void setDuration(Integer duration);

    /**
     * Sets the height of this media.
     *
     * @return Integer the target's height
     */
    Integer getHeight();

    /**
     * Sets the height of this media.
     *
     * @param height is the target's height
     */
    void setHeight(Integer height);

    /**
     * Returns the target URL of this MediaLink.
     *
     * @return a target
     */
    String getUrl();

    /**
     * Sets the target URL for this MediaLink.
     *
     * @param url a target link
     */
    void setUrl(String url);

    /**
     * <p>Returns the width of this media.</p>
     *
     * @return Integer the target's width
     */
    Integer getWidth();

    /**
     * Sets the width of this media.
     *
     * @param width is the target's width
     */
    void setWidth(Integer width);

}
