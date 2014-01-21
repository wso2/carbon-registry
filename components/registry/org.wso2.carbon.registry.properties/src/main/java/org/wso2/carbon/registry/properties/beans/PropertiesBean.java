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

package org.wso2.carbon.registry.properties.beans;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import org.wso2.carbon.registry.properties.utils.Property;

/**
 * Class to represent a collection of properties
 */
@SuppressWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class PropertiesBean {

    private Property[] properties;

    private String [] validationProperties;

    private String [] lifecycleProperties;

    private String [] sysProperties;

    private boolean putAllowed;

    private boolean versionView;

    private boolean loggedIn;

    private String pathWithVersion;
    private String isWriteLocked;

    private String isDeleteLocked;

    public String getWriteLocked() {
        return isWriteLocked;
    }

    public void setWriteLocked(String writeLocked) {
        isWriteLocked = writeLocked;
    }

    public String getDeleteLocked() {
        return isDeleteLocked;
    }

    public void setDeleteLocked(String deleteLocked) {
        isDeleteLocked = deleteLocked;
    }

    /**
     * Method to get properties
     *
     * @return array of Property beans
     */
    public Property [] getProperties() {
        return properties;
    }
    
    /**
     * Method to set properties
     * 
     * @param properties array of Property beans
     */
    public void setProperties(Property [] properties) {
        this.properties = properties;
    }
    
    /**
     * Method to get the property names related to wsdl validation
     *
     * @return array of property names
     */
    public String [] getValidationProperties() {
        return validationProperties;
    }

    /**
     * Method to set the property names related to wsdl validation
     *
     * @param validationProperties array of property names
     */
    public void setValidationProperties(String [] validationProperties) {
        this.validationProperties = validationProperties;
    }

    /**
     * Method to get the property names related to lifecycles
     *
     * @return array of property names
     */
    public String [] getLifecycleProperties() {
        return lifecycleProperties;
    }

    /**
     * Method to set the property names related to lifecycles
     *
     * @param lifecycleProperties array of property names
     */
    public void setLifecycleProperties(String [] lifecycleProperties) {
        this.lifecycleProperties = lifecycleProperties;
    }

    /**
     * Method to get the property names of system properties
     *
     * @return array of property names
     */
    public String [] getSysProperties() {
        return sysProperties;
    }

    /**
     * Method to set the property names of system properties.
     *
     * @param sysProperties array of property names
     */
    public void setSysProperties(String [] sysProperties) {
        this.sysProperties = sysProperties;
    }

    /**
     * Method to check whether put is allowed for the resource
     *
     * @return true, if the put is allowed, false otherwise.
     */
    public boolean isPutAllowed() {
     return putAllowed;
    }

    /**
     * Method to set whether the put is allowed for the resource.
     *
     * @param putAllowed whether put is allowed or not.
     */
    public void setPutAllowed(boolean putAllowed) {
        this.putAllowed = putAllowed;
    }

    /**
     * Method to check whether the requested resource is a versioned resource.
     *
     * @return true if it is a versioned resource, false otherwise
     */
    public boolean isVersionView() {
        return versionView;
    }

    /**
     * Method to set whether the requested resource is a versioned resource.
     *
     * @param versionView whether the requested resource is a versioned resource.
     */
    public void setVersionView(boolean versionView) {
        this.versionView = versionView;
    }

    /**
     * Method to check whether the requested user is logged in (not an anonymous user).
     *
     * @return true if the user is logged in, false otherwise.
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }

    /**
     * Method to set whether the requested user is logged in (not an anonymous user).
     *
     * @param loggedIn whether the requested user is logged in (not an anonymous user.
     */
    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    /**
     * Method to get the path with version.
     *
     * @return the path with version.
     */
    public String getPathWithVersion() {
        return pathWithVersion;
    }

    /**
     * Method to set the path with version.
     *
     * @param pathWithVersion path with version.
     */
    public void setPathWithVersion(String pathWithVersion) {
        this.pathWithVersion = pathWithVersion;
    }
}
