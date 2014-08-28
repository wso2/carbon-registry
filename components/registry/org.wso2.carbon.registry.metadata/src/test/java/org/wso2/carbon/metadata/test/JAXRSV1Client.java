package org.wso2.carbon.metadata.test;


import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.metadata.lifecycle.StateMachineLifecycle;
import org.wso2.carbon.registry.metadata.service.HTTPServiceV1;
import org.wso2.carbon.registry.metadata.service.ServiceV1;
import org.wso2.carbon.registry.metadata.version.HTTPServiceVersionV1;

import java.util.HashMap;
import java.util.Map;

public class JAXRSV1Client {

    public static void main(String[] args) throws RegistryException {

// Create a service
        Registry registry =null ;// Obtain a remote/internal registry instance to start with

        HTTPServiceV1 http1 = new HTTPServiceV1(registry,"foo",new HTTPServiceVersionV1(registry,"1.0.0-SNAPSHOT"));
        http1.setOwner("serviceOwner");
        http1.setProperty("createdDate","12-12-2012");

// Save the service
        HTTPServiceV1.add(registry,http1);

// Update a service
        HTTPServiceV1 newService = HTTPServiceV1.get(registry,http1.getUUID());
        newService.setOwner("newOwner");
        HTTPServiceV1.update(registry,newService);

// Fetch all services
        HTTPServiceV1[] services = HTTPServiceV1.getAll(registry);

// Search services
        Map<String,String> criteria = new HashMap<String, String>();
        criteria.put("owner","newOwner");
        HTTPServiceV1[] results = HTTPServiceV1.find(registry,criteria);

//  Create new Version of a service
        HTTPServiceVersionV1 httpV1 = http1.newVersion("1.0.0");
        httpV1.setEndpointUrl("http://test.rest/stockquote");
        httpV1.setProperty("isSecured","true");

//  Save a service version
        HTTPServiceVersionV1.add(registry,httpV1);

//  Lifecycle operations fora service
        httpV1.attachLifecycle("HTTPServiceLifecycle");
        StateMachineLifecycle lc = httpV1.getLifecycle();
        lc.transfer("Promote");
        StateMachineLifecycle.State currentState = lc.getCurrentState();

//  Delete service version
        HTTPServiceVersionV1 v1 = HTTPServiceVersionV1.get(registry,httpV1.getUUID());
        HTTPServiceV1.delete(registry,v1.getUUID());

    }
}
