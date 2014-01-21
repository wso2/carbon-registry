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
package org.wso2.carbon.registry.social.api.people.userprofile.model;

/**
 * The Account interface describes the an account held by a person.
 */

public interface Account {

    /**
     * The fields that represent the account object in json form.
     * <p/>
     * <p>
     * All of the fields that an account can have, all fields are required
     * </p>
     */
    public static enum Field {
        /**
         * the json field for domain.
         */
        DOMAIN("domain"),
        /**
         * the json field for userId.
         */
        USER_ID("userId"),
        /**
         * the json field for username.
         */
        USERNAME("username");

        /**
         * The json field that the instance represents.
         */
        private final String jsonString;

        /**
         * create a field base on the a json element.
         *
         * @param jsonString the name of the element
         */
        private Field(String jsonString) {
            this.jsonString = jsonString;
        }

        /**
         * emit the field as a json element.
         *
         * @return the field name
         */
        @Override
        public String toString() {
            return this.jsonString;
        }
    }

    /**
     * The top-most authoritative domain for this account, e.g. "twitter.com". This is the Primary
     * Sub-Field for this field, for the purposes of sorting and filtering.
     *
     * @return the domain
     */
    String getDomain();

    /**
     * The top-most authoritative domain for this account, e.g. "twitter.com". This is the Primary
     * Sub-Field for this field, for the purposes of sorting and filtering. *
     *
     * @param domain the domain
     */
    void setDomain(String domain);

    /**
     * A user ID number, usually chosen automatically, and usually numeric but sometimes alphanumeric,
     * e.g. "12345" or "1Z425A".
     *
     * @return the userId
     */
    String getUserId();

    /**
     * A user ID number, usually chosen automatically, and usually numeric but sometimes alphanumeric,
     * e.g. "12345" or "1Z425A".
     *
     * @param userId the userId
     */
    void setUserId(String userId);

    /**
     * An alphanumeric user name, usually chosen by the user, e.g. "jsmarr".
     *
     * @return the username
     */
    String getUsername();

    /**
     * An alphanumeric user name, usually chosen by the user, e.g. "jsmarr".
     *
     * @param username the username
     */
    void setUsername(String username);

}
