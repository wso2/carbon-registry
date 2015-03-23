/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.extensions.test;

import junit.framework.TestCase;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.extensions.handlers.utils.EndpointUtils;

public class EndpointUtilsTest extends TestCase {

    public void testSetEndpointFullPath() throws RegistryException {
        assertEquals("ep-ConvertAcceleration-asmx",
                EndpointUtils.deriveEndpointNameFromUrl("http://www.webservicex.net/ConvertAcceleration.asmx"));
        assertEquals("ep-ConvertTorque-asmx",
                EndpointUtils.deriveEndpointNameFromUrl("http://www.webservicex.net/ConvertTorque.asmx"));
    }

    public void testEndpointContentWithOverview() throws RegistryException {
        assertTrue(EndpointUtils.getEndpointContentWithOverview("http://www.webservicex.net/convertVolume.asmx",
                "/_system/governance/trunk/endpoints/net/webservicex/www/ep-convertVolume-asmx",
                EndpointUtils.deriveEndpointNameFromUrl("http://www.webservicex.net/convertVolume.asmx"), "1.0.0")
                .equals("<ns:endpoint xmlns:ns=\"http://ws.apache.org/ns/synapse\" name=\"gov/net/webservicex/www"
                        + "/ep-convertVolume-asmx\"><ns:overview_name>ep-convertVolume-asmx</ns:overview_name><ns:"
                        + "overview_version>1.0.0</ns:overview_version><ns:address uri=\"http://www.webservicex.net"
                        + "/convertVolume.asmx\"/></ns:endpoint>"));
        System.out.println("getEndpointContentWithOverview 1: " + EndpointUtils
                .getEndpointContentWithOverview("http://www.webservicex.net/convertVolume.asmx",
                        "/_system/governance/trunk/endpoints/net/webservicex/www/ep-convertVolume-asmx",
                        EndpointUtils.deriveEndpointNameFromUrl("http://www.webservicex.net/convertVolume.asmx"),
                        "1.0.0"));

        assertTrue(EndpointUtils.getEndpointContentWithOverview("http://www.webservicex.net/convertMetricWeight.asmx",
                "/_system/governance/trunk/endpoints/net/webservicex/www/ep-convertMetricWeight-asmx",
                EndpointUtils.deriveEndpointNameFromUrl("http://www.webservicex.net/convertMetricWeight.asmx"), "1.0.0")
                .equals("<ns:endpoint xmlns:ns=\"http://ws.apache.org/ns/synapse\" name=\"gov/net/webservicex/www"
                        + "/ep-convertMetricWeight-asmx\"><ns:overview_name>ep-convertMetricWeight-asmx"
                        + "</ns:overview_name><ns:overview_version>1.0.0</ns:overview_version><ns:address"
                        + " uri=\"http://www.webservicex.net/convertMetricWeight.asmx\"/></ns:endpoint>"));
        System.out.println("getEndpointContentWithOverview 1: " + EndpointUtils
                .getEndpointContentWithOverview("http://www.webservicex.net/convertMetricWeight.asmx",
                        "/_system/governance/trunk/endpoints/net/webservicex/www/ep-convertMetricWeight-asmx",
                        EndpointUtils.deriveEndpointNameFromUrl("http://www.webservicex.net/convertMetricWeight.asmx"),
                        "1.0.0"));
    }
}
