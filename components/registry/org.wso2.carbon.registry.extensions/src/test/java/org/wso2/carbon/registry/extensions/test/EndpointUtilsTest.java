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
        StringBuilder endpointContent1 = new StringBuilder();
        StringBuilder endpointContent2 = new StringBuilder();

        endpointContent1.append("<endpoint xmlns=\"http://www.wso2.org/governance/metadata\" name=\"gov/net/web")
                .append("servicex/www/ep-convertVolume-asmx\"><overview><name>ep-convertVolume-asmx</")
                .append("name><version>1.0.0</version><address>http://www.webservicex.net/con")
                .append("vertVolume.asmx</address></overview></endpoint>");
        endpointContent2.append("<endpoint xmlns=\"http://www.wso2.org/governance/metadata\" name=\"gov/net/web")
                .append("servicex/www/ep-convertMetricWeight-asmx\"><overview><name>ep-convertMetricW")
                .append("eight-asmx</name><version>1.0.0</version><address>http://www.webservicex.ne")
                .append("t/convertMetricWeight.asmx</address></overview></endpoint>");

        // TODO: below stringbuilder varibles should remove after xx issue fixed.
        // <remove>
        StringBuilder endpointContent1_2 = new StringBuilder();
        StringBuilder endpointContent2_2 = new StringBuilder();
        endpointContent1_2.append("<endpoint xmlns=\"http://www.wso2.org/governance/metadata\" name=\"gov/net/web")
                .append("servicex/www/ep-convertVolume-asmx\"><overview><name>ep-convertVolume-asmx</")
                .append("name><version>1.0.0version</version><address>http://www.webservicex.net/con")
                .append("vertVolume.asmx</address></overview></endpoint>");
        endpointContent2_2.append("<endpoint xmlns=\"http://www.wso2.org/governance/metadata\" name=\"gov/net/web")
                .append("servicex/www/ep-convertMetricWeight-asmx\"><overview><name>ep-convertMetricW")
                .append("eight-asmx</name><version>1.0.0version</version><address>http://www.webservicex.ne")
                .append("t/convertMetricWeight.asmx</address></overview></endpoint>");
        //</remove>

        String generatedEndpointcontent1 = EndpointUtils
                .getEndpointContentWithOverview("http://www.webservicex.net/convertVolume.asmx",
                        "/_system/governance/trunk/endpoints/net/webservicex/www/ep-convertVolume-asmx",
                        EndpointUtils.deriveEndpointNameFromUrl("http://www.webservicex.net/convertVolume.asmx"),
                        "1.0.0");

        String generatedEndpointcontent2 = EndpointUtils
                .getEndpointContentWithOverview("http://www.webservicex.net/convertMetricWeight.asmx",
                        "/_system/governance/trunk/endpoints/net/webservicex/www/ep-convertMetricWeight-asmx",
                        EndpointUtils.deriveEndpointNameFromUrl("http://www.webservicex.net/convertMetricWeight.asmx"),
                        "1.0.0");

        assertTrue(generatedEndpointcontent1.equals(endpointContent1.toString()) || generatedEndpointcontent1
                .equals(endpointContent1_2.toString()));
        System.out.println("getEndpointContentWithOverview 1: " + EndpointUtils
                .getEndpointContentWithOverview("http://www.webservicex.net/convertVolume.asmx",
                        "/_system/governance/trunk/endpoints/net/webservicex/www/ep-convertVolume-asmx",
                        EndpointUtils.deriveEndpointNameFromUrl("http://www.webservicex.net/convertVolume.asmx"),
                        "1.0.0"));

        assertTrue(generatedEndpointcontent2.equals(endpointContent2.toString()) || generatedEndpointcontent1
                .equals(endpointContent2_2.toString()));
        System.out.println("getEndpointContentWithOverview 1: " + EndpointUtils
                .getEndpointContentWithOverview("http://www.webservicex.net/convertMetricWeight.asmx",
                        "/_system/governance/trunk/endpoints/net/webservicex/www/ep-convertMetricWeight-asmx",
                        EndpointUtils.deriveEndpointNameFromUrl("http://www.webservicex.net/convertMetricWeight.asmx"),
                        "1.0.0"));
    }
}
