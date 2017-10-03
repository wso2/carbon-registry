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
package org.wso2.carbon.registry.indexing;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.indexing.util.IndexingTestUtils;
import org.wso2.carbon.utils.WaitBeforeShutdownObserver;

import java.nio.file.Path;

public class UtilsTest {
    @Test
    public void testGetRegistryService() throws Exception {
        RegistryService service = Mockito.mock(RegistryService.class);
        Utils.setRegistryService(service);
        Assert.assertEquals(service, Utils.getRegistryService());
    }

    @Test
    public void testGetWaitBeforeShutdownObservers() throws Exception {
        WaitBeforeShutdownObserver service1 = Mockito.mock(WaitBeforeShutdownObserver.class);
        Utils.setWaitBeforeShutdownObserver(service1);
        WaitBeforeShutdownObserver service2 = Mockito.mock(WaitBeforeShutdownObserver.class);
        Utils.setWaitBeforeShutdownObserver(service2);
        WaitBeforeShutdownObserver[] serviceArray = new WaitBeforeShutdownObserver[] {service1, service2};
        Assert.assertArrayEquals(serviceArray, Utils.getWaitBeforeShutdownObservers());
    }

    @Test
    public void testClearWaitBeforeShutdownObserver() throws Exception {
        WaitBeforeShutdownObserver service1 = Mockito.mock(WaitBeforeShutdownObserver.class);
        Utils.setWaitBeforeShutdownObserver(service1);
        Utils.clearWaitBeforeShutdownObserver();
        Assert.assertEquals(0, Utils.getWaitBeforeShutdownObservers().length);
    }

    @Test
    public void testGetDefaultEventingServiceURL() throws Exception {
        String defaultEventingServiceURL = "https://localhost:9443/registry";
        Utils.setDefaultEventingServiceURL(defaultEventingServiceURL);
        Assert.assertEquals(defaultEventingServiceURL, Utils.getDefaultEventingServiceURL());
    }

    @Test
    public void testGetRemoteTopicHeaderName() throws Exception {
        String remoteTopicHeader = "test";
        Utils.setRemoteTopicHeaderName(remoteTopicHeader);
        Assert.assertEquals(remoteTopicHeader, Utils.getRemoteTopicHeaderName());
    }

    @Test
    public void testGetRemoteTopicHeaderNS() throws Exception {
        String remoteTopicHeaderNS = "wso2.org";
        Utils.setRemoteTopicHeaderNS(remoteTopicHeaderNS);
        Assert.assertEquals(remoteTopicHeaderNS, Utils.getRemoteTopicHeaderNS());
    }

    @Test
    public void testGetRemoteSubscriptionStoreContext() throws Exception {
        String remoteSubscriptionStoreContext = "test";
        Utils.setRemoteSubscriptionStoreContext(remoteSubscriptionStoreContext);
        Assert.assertEquals(remoteSubscriptionStoreContext, Utils.getRemoteSubscriptionStoreContext());
    }

    @Test
    public void testIsIndexingConfigAvailable() throws Exception {
        Path registryPath = IndexingTestUtils.getResourcePath("registry.xml");
        System.setProperty("wso2.registry.xml", registryPath.toString());
        System.setProperty("carbon.home", registryPath.toString());
        Assert.assertTrue(Utils.isIndexingConfigAvailable());
    }

    @Test(expected = RegistryException.class)
    public void testReadIndexingConfigWithInvalidPath() throws RegistryException {
        Path registryPath = IndexingTestUtils.getResourcePath("registry-1.xml");
        System.setProperty("wso2.registry.xml", registryPath.toString());
        System.setProperty("carbon.home", registryPath.toString());
        Assert.assertTrue(Utils.isIndexingConfigAvailable());
    }

    @Test(expected = RegistryException.class)
    public void testReadIndexingConfigWithInvalidContent() throws RegistryException {
        Path registryPath = IndexingTestUtils.getResourcePath("registry-invalid.xml");
        System.setProperty("wso2.registry.xml", registryPath.toString());
        System.setProperty("carbon.home", registryPath.toString());
        Assert.assertTrue(Utils.isIndexingConfigAvailable());
    }

}