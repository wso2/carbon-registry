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
package org.wso2.carbon.registry.admin.api.handler;

/**
 * Provides functionality to list (view, edit, delete), and simulate handlers configured on the
 * Registry.
 * <br />
 * <b>Statistics:</b>
 * <ul>
 * <li>createHandler</li>
 * <li>deleteHandler</li>
 * <li>updateHandler</li>
 * </ul>
 *
 * @param <SimulationResponse> This object contains the execution status of a set of handlers used
 *                             in a simulation operation. The returned simulation response object
 *                             contains an array of HandlerSimulationStatus objects, which contains
 *                             the name of the handler the execution status.
 *
 * @param <SimulationRequest>  This object contains the parameters required for performing a
 *                             simulation of handlers on the registry. This object can capture
 *                             information such as operation, mediaType, path and other optional
 *                             parameters.
 */
@SuppressWarnings("unused")
public interface IHandlerManagementService<SimulationResponse, SimulationRequest> {

    /**
     * Method to obtain the location at which the handler collection is stored on the registry.
     *
     * @return the resource path of the handler collection.
     * @throws Exception if the operation failed.
     */
    String getHandlerCollectionLocation() throws Exception;

    /**
     * Method to set the location at which the handler collection is stored on the registry.
     *
     * @param location the resource path of the handler collection.
     *
     * @throws Exception if the operation failed.
     */
    void setHandlerCollectionLocation(String location) throws Exception;

    /**
     * Method to obtain the list of handlers that have been configured through the Handler
     * Administration API.
     *
     * @return the list of handlers.
     * @throws Exception if the operation failed.
     */
    String[] getHandlerList() throws Exception;

    /**
     * Method to obtain the configuration of a named handler.
     *
     * @param name the name of the handler.
     *
     * @return the handler configuration.
     * @throws Exception if the operation failed.
     */
    String getHandlerConfiguration(String name) throws Exception;

    /**
     * Method to delete the configuration of a named handler.
     *
     * @param name the name of the handler.
     *
     * @return whether the operation was successful.
     * @throws Exception if an error occurred.
     */
    boolean deleteHandler(String name) throws Exception;

    /**
     * Method to create a handler using the provided configuration.
     *
     * @param payload the handler configuration.
     *
     * @return whether the operation was successful.
     * @throws Exception if an error occurred.
     */
    boolean createHandler(String payload) throws Exception;

    /**
     * Method to update the configuration of a named handler, using the provided configuration
     *
     * @param name    the name of the handler.
     * @param payload the handler configuration.
     *
     * @return whether the operation was successful.
     * @throws Exception if an error occurred.
     */
    boolean updateHandler(String name, String payload) throws Exception;

    /**
     * Method to simulate handlers on a registry.
     *
     * @param request the simulation request object.
     *
     * @return the response containing the status of the handler execution.
     *
     * @throws Exception if the operation failed.
     */
    SimulationResponse simulate(SimulationRequest request) throws Exception;
}
