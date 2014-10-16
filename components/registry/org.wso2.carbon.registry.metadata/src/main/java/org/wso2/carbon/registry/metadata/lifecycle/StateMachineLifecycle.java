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

package org.wso2.carbon.registry.metadata.lifecycle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.metadata.Util;
import org.wso2.carbon.registry.metadata.exception.MetadataException;

import java.util.Map;

/**
 * This class implemented to maintain the state of a particular meta data object's lifecycle
 */
public class StateMachineLifecycle {

    private Registry registry;
    private String name;
    private String uuid;
    private static final Log log = LogFactory.getLog(StateMachineLifecycle.class);


    public StateMachineLifecycle(Registry registry, String name, String uuid) {
        this.registry = registry;
        this.name = name;
        this.uuid = uuid;
    }

    public void checkItem(Map<String,String> map) throws MetadataException {
        try {
            registry.invokeAspect(Util.getMetadataPath(uuid, registry), this.name, "itemClick",map);
        } catch (RegistryException e) {
            log.error("Error occurred while invoking check item for lifecycle "+ name);
            throw new MetadataException(e.getMessage(), e);
        }
    }

    public void vote(Map<String,String> map) throws MetadataException {
        try {
            registry.invokeAspect(Util.getMetadataPath(uuid, registry), this.name, "voteClick",map);
        } catch (RegistryException e) {
            log.error("Error occurred while invoking approval vote for lifecycle "+ name);
            throw new MetadataException(e.getMessage(), e);
        }
    }

    public void transfer(String action) throws MetadataException {
        try {
            registry.invokeAspect(Util.getMetadataPath(uuid, registry), this.name, action);
        } catch (RegistryException e) {
            log.error("Error occurred while invoking operation " + action + " for lifecycle "+ name);
            throw new MetadataException(e.getMessage(), e);
        }
    }

    public void transfer(String action, Map<String, String> params) throws MetadataException {
        try {
            registry.invokeAspect(Util.getMetadataPath(uuid, registry), this.name, action, params);
        } catch (RegistryException e) {
            log.error("Error occurred while invoking operation " + action + " for lifecycle "+ name);
            throw new MetadataException(e.getMessage(), e);
        }
    }

    /**
     * NOTE: This is a heavy operation since the LC state always in the resource.
     * To maintain the accuracy of the data, always fetch the resource from registry to get the details.
     * TODO improve this to get this from a cached value
     *
     * @return the current state of this life cycle
     */
    public State getCurrentState() throws MetadataException {
        String path = Util.getMetadataPath(uuid, registry);
        try {
            if (registry.resourceExists(path)) {
                Resource r = registry.get(path);
                return new State(getLCState(r));
            }
        } catch (RegistryException e) {
            log.error("Error occurred while obtaining current state for lifecycle "+ name);
            throw new MetadataException("Resource " + uuid + "does not exists");
        }
        return null;
    }

    private String getLCState(Resource resource) {
        return resource.getProperty("registry.lifecycle." + resource.getProperty("registry.LC.name") + ".state");
    }

    /**
     * Represents a particular lifecycle state
     */
    public static class State {

        private String name;

        public State(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

}
