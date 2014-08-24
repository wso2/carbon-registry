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
     * NOTE: This operation is heavy operation since the LC state always in the resource.
     * To maintain the accuracy of the data, always fetch the resource to get the details.
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
