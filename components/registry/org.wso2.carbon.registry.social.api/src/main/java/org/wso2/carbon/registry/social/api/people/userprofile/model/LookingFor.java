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

public enum LookingFor {
    /**
     * Interested in dating.
     */
    DATING("DATING", "Dating"),
    /**
     * Looking for friends.
     */
    FRIENDS("FRIENDS", "Friends"),
    /**
     * Looking for a relationship.
     */
    RELATIONSHIP("RELATIONSHIP", "Relationship"),
    /**
     * Just want to network.
     */
    NETWORKING("NETWORKING", "Networking"),
    /** */
    ACTIVITY_PARTNERS("ACTIVITY_PARTNERS", "Activity partners"),
    /** */
    RANDOM("RANDOM", "Random");

    /**
     * The Json representation of the value.
     */
    private final String jsonString;

    /**
     * The value used for display purposes.
     */
    private final String displayValue;

    /**
     * Construct a looking for enum.
     *
     * @param jsonString   the json representation of the enum.
     * @param displayValue the value used for display purposes.
     */
    private LookingFor(String jsonString, String displayValue) {
        this.jsonString = jsonString;
        this.displayValue = displayValue;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return this.jsonString;
    }

    /**
     *
     */
    public String getDisplayValue() {
        return displayValue;
    }
}

