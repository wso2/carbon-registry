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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

public class SynapseRepositoryHandler extends Handler {

    private static final Log log = LogFactory.getLog(SynapseRepositoryHandler.class);

    public Resource get(RequestContext requestContext) throws RegistryException {
        return null;
    }

    public void put(RequestContext requestContext) throws RegistryException {

        String path = requestContext.getResourcePath().getPath();

        String confPath = path +
                RegistryConstants.PATH_SEPARATOR + RegistryConstants
                .SYNAPSE_CONF_COLLECTION_NAME;
        CollectionImpl confCollection = new CollectionImpl();
        confCollection.setPath(confPath);
        confCollection.setMediaType(RegistryConstants.SYNAPSE_CONF_COLLECTION_MEDIA_TYPE);
        requestContext.getRegistry().put(confPath, confCollection);

        String seqPath = path + RegistryConstants.PATH_SEPARATOR +
                RegistryConstants.SYNAPSE_SEQUENCES_COLLECTION_NAME;
        CollectionImpl seqCollection = new CollectionImpl();
        seqCollection.setPath(seqPath);
        seqCollection.setMediaType(RegistryConstants.SYNAPSE_SEQUENCE_COLLECTION_MEDIA_TYPE);
        requestContext.getRegistry().put(seqPath, seqCollection);

        String epPath = path + RegistryConstants.PATH_SEPARATOR +
                RegistryConstants.SYNAPSE_ENDPOINT_COLLECTION_NAME;
        CollectionImpl epCollection = new CollectionImpl();
        epCollection.setPath(epPath);
        epCollection.setMediaType(RegistryConstants.SYNAPSE_ENDPOINT_COLLECTION_MEDIA_TYPE);
        requestContext.getRegistry().put(epPath, epCollection);

        String proxyServicesPath = path + RegistryConstants.PATH_SEPARATOR +
                RegistryConstants.SYNAPSE_PROXY_SERVICES_COLLECTION_NAME;
        CollectionImpl proxyCollection = new CollectionImpl();
        proxyCollection.setPath(proxyServicesPath);
        proxyCollection
                .setMediaType(RegistryConstants.SYNAPSE_PROXY_SERVICES_COLLECTION_MEDIA_TYPE);
        requestContext.getRegistry().put(proxyServicesPath, proxyCollection);

        String tasksPath = path + RegistryConstants.PATH_SEPARATOR +
                RegistryConstants.SYNAPSE_TASKS_COLLECTION_NAME;
        CollectionImpl tasksCollection = new CollectionImpl();
        tasksCollection.setPath(tasksPath);
        tasksCollection.setMediaType(RegistryConstants.SYNAPSE_TASKS_COLLECTION_MEDIA_TYPE);
        requestContext.getRegistry().put(tasksPath, tasksCollection);

        String entriesPath = path + RegistryConstants.PATH_SEPARATOR +
                RegistryConstants.SYNAPSE_ENTRIES_COLLECTION_NAME;
        CollectionImpl entryCollection = new CollectionImpl();
        entryCollection.setPath(entriesPath);
        entryCollection.setMediaType(RegistryConstants.SYNAPSE_ENTRIES_COLLECTION_MEDIA_TYPE);
        requestContext.getRegistry().put(entriesPath, entryCollection);
    }

    public void importResource(RequestContext requestContext) throws RegistryException {
    }

    public void delete(RequestContext requestContext) throws RegistryException {
    }

    public void putChild(RequestContext requestContext) throws RegistryException {

        Resource childResource = requestContext.getResource();
        if (childResource == null) {
            childResource = requestContext.getRegistry().
                    get(requestContext.getResourcePath().getPath());
            requestContext.setResource(childResource);
        }
        String childMediaType = requestContext.getResource().getMediaType();

        if(!(RegistryConstants.SYNAPSE_CONF_COLLECTION_MEDIA_TYPE.equals(childMediaType) ||
                RegistryConstants.SYNAPSE_SEQUENCE_COLLECTION_MEDIA_TYPE.equals(childMediaType) ||
                RegistryConstants.SYNAPSE_ENDPOINT_COLLECTION_MEDIA_TYPE.equals(childMediaType) ||
                RegistryConstants.SYNAPSE_PROXY_SERVICES_COLLECTION_MEDIA_TYPE.equals(childMediaType)
                || RegistryConstants.SYNAPSE_TASKS_COLLECTION_MEDIA_TYPE.equals(childMediaType) ||
                RegistryConstants.SYNAPSE_ENTRIES_COLLECTION_MEDIA_TYPE.equals(childMediaType))) {

            String msg = "Resources of type: " + childMediaType +
                    " are not allowed to add as child resources of " +
                    "the typed collection Synapse Repository.";
            log.error(msg);
            throw new RegistryException(msg);

        }
    }

    public void importChild(RequestContext requestContext) throws RegistryException {
    }
}
