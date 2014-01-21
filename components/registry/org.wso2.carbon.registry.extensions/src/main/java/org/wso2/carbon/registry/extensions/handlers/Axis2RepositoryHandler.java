/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.extensions.handlers;

import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This handler implementation customizes the processing of collections of type Axis2 repository.
 * These collections are used to store artifacts required by Apache Axis2. They are given a custom
 * media type named application/vnd.apache.axis2 so that a media type filter can filter the
 * requests.
 *
 * Axis2 repositories contain three top level sub collections named conf, services and modules.
 * This handler creates and maintains Axis2 repository in this configuration.
 */
public class Axis2RepositoryHandler extends Handler {

    private static final Log log = LogFactory.getLog(Axis2RepositoryHandler.class);

    public Axis2RepositoryHandler() {}

    /**
     * This method adds the Axis2 repository collection in the given path and adds necessary sub
     * collections to it.
     *
     * @param requestContext Request details.
     * @throws RegistryException
     */
    public void put(RequestContext requestContext) throws RegistryException {

        String path = requestContext.getResourcePath().getPath();

        String confPath = path +
                          RegistryConstants.PATH_SEPARATOR + RegistryConstants
                .AXIS2_CONF_COLLECTION_NAME;
        CollectionImpl confCollection = new CollectionImpl();
        confCollection.setPath(confPath);
        confCollection.setMediaType(RegistryConstants.AXIS2_CONF_COLLECTION_MEDIA_TYPE);
        requestContext.getRegistry().put(confPath, confCollection);

        String servicesPath = path +
                              RegistryConstants.PATH_SEPARATOR + RegistryConstants
                .AXIS2_SERVICES_COLLECTION_NAME;
        CollectionImpl servicesCollection = new CollectionImpl();
        servicesCollection.setPath(servicesPath);
        servicesCollection.setMediaType(RegistryConstants.AXIS2_SERVICES_COLLECTION_MEDIA_TYPE);
        requestContext.getRegistry().put(servicesPath, servicesCollection);

        String modulesPath = path +
                             RegistryConstants.PATH_SEPARATOR + RegistryConstants
                .AXIS2_MODULES_COLLECTION_NAME;
        CollectionImpl modulesCollection = new CollectionImpl();
        modulesCollection.setPath(modulesPath);
        modulesCollection.setMediaType(RegistryConstants.AXIS2_MODULES_COLLECTION_MEDIA_TYPE);
        requestContext.getRegistry().put(modulesPath, modulesCollection);
    }

    /**
     * This method is invoked when it is attempted to put child resources to Axis2 repository.
     * It blocks the operation if the child resources do not belong to allowed types.
     *
     * @param requestContext Request details.
     * @throws RegistryException Throws if the child resource does not belong to allowed types.
     */
    public void putChild(RequestContext requestContext) throws RegistryException {

        Resource childResource = requestContext.getResource();
        if (childResource == null) {
            childResource = requestContext.getRegistry().
                    get(requestContext.getResourcePath().getPath());
            requestContext.setResource(childResource);
        }
        String childMediaType = requestContext.getResource().getMediaType();

        if(!(RegistryConstants.AXIS2_CONF_COLLECTION_MEDIA_TYPE.equals(childMediaType) ||
               RegistryConstants.AXIS2_SERVICES_COLLECTION_MEDIA_TYPE.equals(childMediaType) ||
               RegistryConstants.AXIS2_MODULES_COLLECTION_MEDIA_TYPE.equals(childMediaType))) {

            String msg = "Resources of type: " + childMediaType +
                    " are not allowed to add as child resources of " +
                    "the typed collection Axis2 Repository.";
            log.error(msg);
            throw new RegistryException(msg);
        }
    }
}
