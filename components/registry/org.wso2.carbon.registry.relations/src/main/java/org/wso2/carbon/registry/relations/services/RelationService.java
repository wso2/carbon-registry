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

package org.wso2.carbon.registry.relations.services;


import org.wso2.carbon.registry.admin.api.relations.IRelationService;
import org.wso2.carbon.registry.common.services.RegistryAbstractAdmin;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.relations.beans.AssociationTreeBean;
import org.wso2.carbon.registry.relations.beans.DependenciesBean;
import org.wso2.carbon.registry.relations.services.utils.AssociationTreeBeanPopulator;
import org.wso2.carbon.registry.relations.services.utils.DependenciesBeanPopulator;
import org.wso2.carbon.registry.relations.services.utils.CommonUtil;

public class RelationService extends RegistryAbstractAdmin implements IRelationService {

    public DependenciesBean getDependencies(String path) throws RegistryException {
        UserRegistry registry = (UserRegistry)getRootRegistry();
        return DependenciesBeanPopulator.populate(registry, path);
    }

    public void addAssociation(String path, String type, String associationPaths,
                               String todo) throws RegistryException {
        UserRegistry registry = (UserRegistry) getRootRegistry();
        if (RegistryUtils.isRegistryReadOnly(registry.getRegistryContext())) {
            return;
        }
        if(todo.equals("add")){
                if (associationPaths.startsWith(RegistryConstants.ROOT_PATH) &&
                        !registry.resourceExists(associationPaths)) {
                    throw new RegistryException("The given association path " + associationPaths +
                            " does not exist.");
                }
                registry.addAssociation(path, associationPaths, type);
            } else {
                registry.removeAssociation(path, associationPaths, type);
            }
        DependenciesBeanPopulator.populate(registry, path);
    }

    public AssociationTreeBean getAssociationTree(String path, String type) throws RegistryException{
        UserRegistry registry = (UserRegistry)getRootRegistry();
        return AssociationTreeBeanPopulator.populate(registry, path, type);
    }
}
