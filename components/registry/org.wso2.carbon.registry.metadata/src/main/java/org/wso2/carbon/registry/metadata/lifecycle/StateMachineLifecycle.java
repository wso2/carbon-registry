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

import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.metadata.Util;

import java.util.Map;

public class StateMachineLifecycle {

    private Registry registry;
    private String name;
    private String uuid;

    public StateMachineLifecycle(Registry registry, String name, String uuid){
        this.registry = registry;
        this.name = name;
        this.uuid = uuid;
    }

    public void transfer(String action) throws RegistryException {
        registry.invokeAspect(Util.getMetadataPath(uuid, registry), this.name, action);
    }

    public void transfer(String action,Map<String,String> params) throws RegistryException {
        registry.invokeAspect(Util.getMetadataPath(uuid,registry),this.name,action,params);
    }

    /**
     * NOTE: This is a heavy operation since the LC state always in the resource.
     * To maintain the accuracy of the data, always fetch the resource from registry to get the details.
     * TODO improve this to get this from a cached value
     * @return the current state of this life cycle
     */
    public State getCurrentState() throws RegistryException {
        String path = Util.getMetadataPath(uuid,registry);
        if(registry.resourceExists(path)) {
           Resource r = registry.get(path);
            return new State(getLCState(r));
        } else {
          throw new RegistryException("Resource " + uuid + "does not exists");
        }
    }

    private String getLCState(Resource resource){
        return resource.getProperty("registry.lifecycle." + resource.getProperty("registry.LC.name") + ".state");
    }

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
