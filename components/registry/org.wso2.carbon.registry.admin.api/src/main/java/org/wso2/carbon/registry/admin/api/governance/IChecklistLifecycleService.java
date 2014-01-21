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

/**
 * This provides functionality to manage a check-list of a given lifecycle which is available for a
 * particular resource or collection, with the corresponding lifecycle.
 * <br />
 * <b>Statistics:</b>
 * <ul>
 * <li>addAspect</li>
 * <li>removeAspect</li>
 * <li>invokeAspect</li>
 * <li>invokeAspectWithParams</li>
 * </ul>
 *
 * @param <LifecycleBean> a bean representing a lifecycle definition available for a given resource.
 *                        This can be used to manage the lifecycle of the given resource, or check
 *                        on the available lifecycle actions or list the various properties
 *                        available.
 */
public interface IChecklistLifecycleService<LifecycleBean> {

    /**
     * Method to obtain the lifecycle bean for the given resource path.
     *
     * @param path the resource path.
     *
     * @return the lifecycle bean for the given resource path.
     * @throws Exception if the operation failed.
     */
    LifecycleBean getLifecycleBean(String path) throws Exception;

    /**
     * Method to add the named aspect to the given resource path.
     *
     * @param path   the resource path.
     * @param aspect the name of the aspect to add.
     *
     * @throws Exception if the operation failed.
     */
    void addAspect(String path, String aspect) throws Exception;

    /**
     * Method to invoke an action of the defined aspect at the given resource (or collection) path.
     *
     * @param path   the resource (or collection) path.
     * @param aspect the name of the aspect.
     * @param action the action to invoke.
     * @param items  the values corresponding to the state of various check-list items. If the first
     *               check-list item is checked, the value of the first item on this list would be
     *               'true' or 'false' if it was not. If the second check-list item is checked, the
     *               value of the second item on this list would be 'true' or 'false' if it was not.
     *               The same applies for the remaining items on this list.
     *
     * @throws Exception if the operation failed.
     */
    void invokeAspect(String path, String aspect, String action, String[] items) throws Exception;

    /**
     * Method to invoke an action of the defined aspect at the given resource (or collection) path.
     *
     * @param path   the resource (or collection) path.
     * @param aspect the name of the aspect.
     * @param action the action to invoke.
     * @param items  the values corresponding to the state of various check-list items. If the first
     *               check-list item is checked, the value of the first item on this list would be
     *               'true' or 'false' if it was not. If the second check-list item is checked, the
     *               value of the second item on this list would be 'true' or 'false' if it was not.
     *               The same applies for the remaining items on this list.
     * @param params Additional parameters.
     *
     * @throws Exception if the operation failed.
     */
    void invokeAspectWithParams(String path, String aspect, String action,
                                String[] items, String[][] params) throws Exception;

    /**
     * Method to remove the named aspect from the given resource path.
     *
     * @param path   the resource path.
     * @param aspect the name of the aspect to remove.
     *
     * @throws Exception if the operation failed.
     */
    void removeAspect(String path, String aspect) throws Exception;
}
