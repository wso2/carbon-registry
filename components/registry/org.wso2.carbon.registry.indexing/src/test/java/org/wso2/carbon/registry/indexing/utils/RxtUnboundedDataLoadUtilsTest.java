/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.registry.indexing.utils;

import org.junit.Before;
import org.junit.Test;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RxtUnboundedDataLoadUtilsTest {

    private UserRegistry userRegistry;

    @Before
    public void setup() throws RegistryException {
        userRegistry = mock(UserRegistry.class);
        Collection resultCollection = new CollectionImpl();
        resultCollection.setContent(new String[]
                {"/_system/config/repository/components/org.wso2.carbon.governance/configuration/restservice"});
        when(userRegistry.executeQuery(anyString(), (Map) anyObject())).thenReturn(resultCollection);
        Resource rxtResource = new ResourceImpl();
        rxtResource.setMediaType("application/xml");
        InputStream is = null;
        try {
            is = RxtUnboundedDataLoadUtilsTest.class.getClassLoader().getResourceAsStream("restservice.rxt");
            rxtResource.setContentStream(is);
            when(userRegistry.get("/_system/config/repository/components/org.wso2.carbon" +
                    ".governance/configuration/restservice")).thenReturn(rxtResource);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignore) {

                }
            }
        }
    }

    @Test
    public void getRxtData() throws Exception {
        Map<String, List<String>> rxtData = RxtUnboundedDataLoadUtils.getRxtData(userRegistry);
        assertNotNull(rxtData);
        assertTrue(rxtData.containsKey("application/vnd.wso2-restservice+xml"));
        String[] expectedFieldArray = new String[] {"uritemplate_urlPattern", "uritemplate_httpVerb",
                "uritemplate_authType", "contacts_entry", "endpoints_entry"};
        List<String> unboundFieldList = rxtData.get("application/vnd.wso2-restservice+xml");
        String[] actualFieldArray = new String[unboundFieldList.size()];
        actualFieldArray = unboundFieldList.toArray(actualFieldArray);
        assertArrayEquals(expectedFieldArray, actualFieldArray);
    }

}