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
package org.wso2.carbon.registry.admin.api.relations;

import org.wso2.carbon.registry.core.exceptions.RegistryException;

/**
 * Provides functionality to manage relationships (associations and dependencies) among resources
 * and collections.
 *
 * @param <AssociationTreeBean> This bean can be used to manage relationships from this resource.
 *                              Each association can have a type, which can be used to group a set
 *                              of related relationships.
 * @param <DependenciesBean>    This bean can be used to manage dependencies of this resource. A
 *                              dependency cannot have a type like an association. A dependency is
 *                              a special type of association of type 'depends'.
 */
public interface IRelationService<DependenciesBean, AssociationTreeBean> {

    /**
     * Method to obtain a list of dependencies.
     *
     * @param path the resource path.
     *
     * @return the list of dependencies.
     * @throws RegistryException if the operation failed.
     */
    DependenciesBean getDependencies(String path) throws RegistryException;

    /**
     * Method to add an association (or dependency).
     *
     * @param path             the resource path.
     * @param type             the type of association. If the type of association is 'depends' a
     *                         dependency will be created.
     * @param associationPaths the list of associations to be added.
     *
     * @throws RegistryException if the operation failed.
     */
    void addAssociation(String path, String type, String associationPaths,
                               String operation) throws RegistryException;

    // TODO: FIXME: The operation parameter is not required. Get rid of that and fix the
    // addAssociation method on the BE service.

    /**
     * Method to obtain a list of associations.
     * 
     * @param path the resource path.
     * @param type the type of association.
     *
     * @return the list of associations.
     * @throws RegistryException if the operation failed.
     */
    AssociationTreeBean getAssociationTree(String path, String type) throws RegistryException;
}
