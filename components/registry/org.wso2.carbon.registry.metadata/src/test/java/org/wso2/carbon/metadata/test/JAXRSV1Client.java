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

package org.wso2.carbon.metadata.test;


import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.metadata.exception.MetadataException;
import org.wso2.carbon.registry.metadata.lifecycle.StateMachineLifecycle;
import org.wso2.carbon.registry.metadata.models.endpoint.HTTPEndpointV1;
import org.wso2.carbon.registry.metadata.models.service.HTTPServiceV1;
import org.wso2.carbon.registry.metadata.models.version.ServiceVersionV1;

import java.util.HashMap;
import java.util.Map;

public class JAXRSV1Client {

    public static void main(String[] args) throws MetadataException {

// Create a service
        Registry registry = null;// Obtain a remote/internal registry instance to start with

        HTTPServiceV1 http1 = new HTTPServiceV1(registry, "foo", new ServiceVersionV1(registry, "1.0.0-SNAPSHOT"));
        http1.setOwner("serviceOwner");
        http1.setProperty("createdDate", "12-12-2012");

// Save the service
        HTTPServiceV1.add(registry, http1);

// Update a service
        HTTPServiceV1 newService = HTTPServiceV1.get(registry, http1.getUUID());
        newService.setOwner("newOwner");
        HTTPServiceV1.update(registry, newService);

// Fetch all services
        HTTPServiceV1[] services = HTTPServiceV1.getAll(registry);

// Search services
        Map<String, String> criteria = new HashMap<String, String>();
        criteria.put(HTTPServiceV1.KEY_OWNER, "newOwner");
        HTTPServiceV1[] results = HTTPServiceV1.find(registry, criteria);

//  Create new Version of a service
        ServiceVersionV1 httpV1 = http1.newVersion("1.0.0");
        HTTPEndpointV1 ep = new HTTPEndpointV1(registry,"myep1");
        ep.setUrl("http://test.rest/stockquote");
        httpV1.addEndpoint(ep);
        httpV1.setProperty("isSecured", "true");

//  Save a service version
        ServiceVersionV1.add(registry, httpV1);

//  Lifecycle operations fora service
        httpV1.attachLifecycle("HTTPServiceLifecycle");
        StateMachineLifecycle lc = httpV1.getLifecycle();
        lc.transfer("Promote");
        StateMachineLifecycle.State currentState = lc.getCurrentState();

//  Delete service version
        ServiceVersionV1 v1 = ServiceVersionV1.get(registry, httpV1.getUUID());
        HTTPServiceV1.delete(registry, v1.getUUID());

    }
}
