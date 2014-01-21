/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.admin.api.resource;

/**
 * This provides an interface to manage text resources.
 */
public interface ITextResourceManagementService {

    /**
     * Method to obtain the text content of the given resource. For this method to work the resource
     * must be a text-resource.
     *
     * @param path the resource path.
     *
     * @return the text content of the resource as a String.
     *
     * @throws Exception if the operation failed.
     */
    String getTextContent(String path) throws Exception;

    /**
     * Method to update the text content of the given resource. For this method to work the resource
     * must be a text-resource.
     *
     * @param path    the resource path.
     * @param content the updated text content.
     *
     * @return whether the operation was successful or not.
     * @throws Exception if the operation failed due to an unexpected error.
     */
    boolean updateTextContent(String path, String content) throws Exception;

    /**
     * Method to add a text resource to the repository.
     *
     * @param parentPath   the parent path (or the path at which we are adding this resource).
     * @param resourceName the name of the resource.
     * @param mediaType    the media type of the resource.
     * @param description  the description for the newly added resource.
     * @param content      the resource content as a String.
     *
     * @return whether the operation was successful or not.
     * @throws Exception if the operation failed due to an unexpected error.
     */
    boolean addTextContent(
            String parentPath, String resourceName, String mediaType, String description, String content)
            throws Exception;
}
