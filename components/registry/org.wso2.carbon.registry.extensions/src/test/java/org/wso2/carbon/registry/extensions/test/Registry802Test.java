/*
 *  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.wso2.carbon.registry.extensions.test;

import junit.framework.TestCase;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.extensions.handlers.utils.EndpointUtils;

public class Registry802Test extends TestCase {

    public void testEndpointUtils() throws RegistryException {
        String ep1 = EndpointUtils.deriveEndpointFromUrl(
                "jms:/FunctionService?transport.jms.DestinationType=queue&transport.jms." +
                        "ContentTypeProperty=Content-Type&java.naming.provider.url=" +
                        "tcp://localhost:61618&java.naming.factory.initial=org.apache.activemq." +
                        "jndi.ActiveMQInitialContextFactory&transport.jms." +
                        "ConnectionFactoryJNDIName=QueueConnectionFactory");
        System.out.println("Endpoint 1: " + ep1);
        String ep2 = EndpointUtils.deriveEndpointFromUrl(
                "jms:/CharithaService?transport.jms.DestinationType=queue&transport.jms." +
                        "ContentTypeProperty=Content-Type&java.naming.provider.url=" +
                        "tcp://localhost:61618&java.naming.factory.initial=org.apache.activemq." +
                        "jndi.ActiveMQInitialContextFactory&transport.jms." +
                        "ConnectionFactoryJNDIName=QueueConnectionFactory");
        System.out.println("Endpoint 2: " + ep2);
        assertFalse("The endpoints must be different", ep1.equals(ep2));
    }

}
