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

package org.wso2.carbon.registry.common.beans;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

@SuppressWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class RatingBean {

    private int userRating = -1;
    private float averageRating;

    private String[] userStars = new String[5];
    private String[] averageStars = new String[5];

    protected String errorMessage;

    private boolean putAllowed;

    private boolean versionView;

    private boolean loggedIn;

    private String pathWithVersion;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getUserRating() {
        return userRating;
    }

    public void setUserRating(int userRating) {
        this.userRating = userRating;
    }

    public float getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(float averageRating) {
        this.averageRating = averageRating;
    }

    public String[] getUserStars() {
        return userStars;
    }

    public void setUserStars(String[] userStars) {
        this.userStars = userStars;
    }

    public String[] getAverageStars() {
        return averageStars;
    }

    public void setAverageStars(String[] averageStars) {
        this.averageStars = averageStars;
    }

    public boolean isPutAllowed() {
     return putAllowed;
    }

    public void setPutAllowed(boolean putAllowed) {
        this.putAllowed = putAllowed;
    }

    public boolean isVersionView() {
        return versionView;
    }

    public void setVersionView(boolean versionView) {
        this.versionView = versionView;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public String getPathWithVersion() {
        return pathWithVersion;
    }

    public void setPathWithVersion(String pathWithVersion) {
        this.pathWithVersion = pathWithVersion;
    }
}
