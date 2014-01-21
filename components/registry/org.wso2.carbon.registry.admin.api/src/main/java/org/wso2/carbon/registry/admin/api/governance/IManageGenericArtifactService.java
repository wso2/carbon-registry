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

package org.wso2.carbon.registry.admin.api.governance;

import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.lang.String;

/**
 * This provides functionality to manage generic artifacts
 * on the registry.
 * <br />
 * <b>Statistics:</b>
 * <ul>
 * <li>addArtifact</li>
 * <li>listArtifacts</li>
 * <li>editArtifact</li>
 * </ul>
 *
 *@param <ArtifactsBean> a bean containing a list of artifacts on the repository.
 */
public interface IManageGenericArtifactService<ArtifactsBean> {

    /**
     * Method to add an artifact to the repository.
     *
     * @param key the identifier of the artifact.
     * @param payload the information payload of the artifact.
     * @param lifecycleAttribute the name of the lifecycle attribute.
     * @return the path of the artifact.
     * @throws Exception if the operation failed.
     */
    String addArtifact(String key, String payload, String lifecycleAttribute) throws RegistryException;

    /**
     * Method to list the artifacts to a defined criteria.
     *
     * @param key the identifier of the artifact.
     * @param criteria the listing criteria.
     * @return an ArtifactsBean object with artifacts matching to the criteria.
     * @throws Exception if the operation failed.
     */
    ArtifactsBean listArtifacts(String key, String criteria);

    /**
     * Method to list the artifacts by name.
     *
     * @param key the identifier of the artifact.
     * @param name the artifact name.
     * @return an ArtifactsBean object with artifacts matching to the criteria.
     * @throws Exception if the operation failed.
     */
    ArtifactsBean listArtifactsByName(String key, final String name);

    /**
     * Method to edit the artifacts.
     *
     * @param path the path of the artifact.
     * @param key the identifier of the artifact.
     * @param payload the information payload of the artifact.
     * @param lifecycleAttribute the name of the lifecycle attribute.
     * @return the location on the repository where lifecycle configurations are stored.
     * @throws Exception if the operation failed.
     */
    String editArtifact(String path, String key, String payload, String lifecycleAttribute) throws RegistryException;

    /**
     * Method to obtain the content of an Artifact..
     *
     * @param path the path of the artifact.
     * @return the location on the repository where lifecycle configurations are stored.
     * @throws Exception if the operation failed.
     */
    String getArtifactContent(String path) throws RegistryException;

    /**
     * Method to obtain the UI configuration of an artifact..
     *
     * @param key the identifier of the artifact.
     * @return the location on the repository where lifecycle configurations are stored.
     * @throws Exception if the operation failed.
     */
    String getArtifactUIConfiguration(String key) throws RegistryException;

    /**
     * Method to set the UI configuration for an artifact..
     *
     * @param key the identifier of the artifact.
     * @param content the new content of the artifact.
     * @return true if the configuration is properly set..
     * @throws Exception if the operation failed.
     */
    boolean setArtifactUIConfiguration(String key, String content) throws RegistryException;

    /**
     * Get all the states from the LC
     *
     * @param LCName
     * @return
     */
    String[] getAllLifeCycleState(String LCName);


}
