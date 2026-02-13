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

import org.apache.axis2.context.MessageContext;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.indexing.util.IndexingTestUtils;
import org.wso2.carbon.utils.WaitBeforeShutdownObserver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.nio.file.Path;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

public class UtilsTest {
    @Test
    public void testGetRegistryService() throws Exception {
        RegistryService service = Mockito.mock(RegistryService.class);
        Utils.setRegistryService(service);
        assertEquals(service, Utils.getRegistryService());
    }

    @Test
    public void testGetWaitBeforeShutdownObservers() throws Exception {
        WaitBeforeShutdownObserver service1 = Mockito.mock(WaitBeforeShutdownObserver.class);
        Utils.setWaitBeforeShutdownObserver(service1);
        WaitBeforeShutdownObserver service2 = Mockito.mock(WaitBeforeShutdownObserver.class);
        Utils.setWaitBeforeShutdownObserver(service2);
        WaitBeforeShutdownObserver[] serviceArray = new WaitBeforeShutdownObserver[] {service1, service2};
        assertArrayEquals(serviceArray, Utils.getWaitBeforeShutdownObservers());
    }

    @Test
    public void testClearWaitBeforeShutdownObserver() throws Exception {
        WaitBeforeShutdownObserver service1 = Mockito.mock(WaitBeforeShutdownObserver.class);
        Utils.setWaitBeforeShutdownObserver(service1);
        Utils.clearWaitBeforeShutdownObserver();
        assertEquals(0, Utils.getWaitBeforeShutdownObservers().length);
    }

    @Test
    public void testGetDefaultEventingServiceURL() throws Exception {
        String defaultEventingServiceURL = "https://localhost:9443/registry";
        Utils.setDefaultEventingServiceURL(defaultEventingServiceURL);
        assertEquals(defaultEventingServiceURL, Utils.getDefaultEventingServiceURL());
    }

    @Test
    public void testGetRemoteTopicHeaderName() throws Exception {
        String remoteTopicHeader = "test";
        Utils.setRemoteTopicHeaderName(remoteTopicHeader);
        assertEquals(remoteTopicHeader, Utils.getRemoteTopicHeaderName());
    }

    @Test
    public void testGetRemoteTopicHeaderNS() throws Exception {
        String remoteTopicHeaderNS = "wso2.org";
        Utils.setRemoteTopicHeaderNS(remoteTopicHeaderNS);
        assertEquals(remoteTopicHeaderNS, Utils.getRemoteTopicHeaderNS());
    }

    @Test
    public void testGetRemoteSubscriptionStoreContext() throws Exception {
        String remoteSubscriptionStoreContext = "test";
        Utils.setRemoteSubscriptionStoreContext(remoteSubscriptionStoreContext);
        assertEquals(remoteSubscriptionStoreContext, Utils.getRemoteSubscriptionStoreContext());
    }

    @Test
    public void testIsIndexingConfigAvailable() throws Exception {
        Path registryPath = IndexingTestUtils.getResourcePath("registry-indexing.xml");
        System.setProperty("wso2.registry.xml", registryPath.toString());
        System.setProperty("carbon.home", registryPath.toString());
        assertTrue(Utils.isIndexingConfigAvailable());
        System.clearProperty("wso2.registry.xml");
        System.clearProperty("carbon.home");
    }

    @Test(expected = RegistryException.class)
    public void testReadIndexingConfigWithInvalidPath() throws RegistryException {
        Path registryPath = IndexingTestUtils.getResourcePath("registry-1.xml");
        System.setProperty("wso2.registry.xml", registryPath.toString());
        System.setProperty("carbon.home", registryPath.toString());
        assertTrue(Utils.isIndexingConfigAvailable());
        System.clearProperty("wso2.registry.xml");
        System.clearProperty("carbon.home");
    }

    @Test(expected = RegistryException.class)
    public void testReadIndexingConfigWithInvalidContent() throws RegistryException {
        Path registryPath = IndexingTestUtils.getResourcePath("registry-invalid.xml");
        System.setProperty("wso2.registry.xml", registryPath.toString());
        System.setProperty("carbon.home", registryPath.toString());
        assertTrue(Utils.isIndexingConfigAvailable());
        System.clearProperty("wso2.registry.xml");
        System.clearProperty("carbon.home");
    }

    @Test(expected = RegistryException.class)
    public void testGetRegistryWithoutMessageContext() throws RegistryException {
        Utils.getRegistry();
    }

    @Test
    public void testGetRegistry() throws RegistryException {
        MessageContext context = new MessageContext();
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpSession session = Mockito.mock(HttpSession.class);
        when(session.getAttribute(RegistryConstants.USER_REGISTRY)).thenReturn(null);
        when(request.getSession()).thenReturn(session);
        context.setProperty("transport.http.servletRequest", request);
        MessageContext.setCurrentMessageContext(context);

        RegistryService service = Mockito.mock(RegistryService.class);
        UserRegistry registry = Mockito.mock(UserRegistry.class);
        when(service.getUserRegistry()).thenReturn(registry);

        final Registry[] registries = {null};
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                registries[0] = (Registry) args[1];
                return null;
            }
        }).when(session).setAttribute(eq(RegistryConstants.USER_REGISTRY), any(Registry.class));
        Utils.setRegistryService(service);

        Registry registry1 = Utils.getRegistry();
        assertEquals(registry, registry1);
        assertEquals(registry, registries[0]);
    }

    @Test(expected = RegistryException.class)
    public void testGetRegistryWithoutRegistryService() throws RegistryException {
        MessageContext context = new MessageContext();
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpSession session = Mockito.mock(HttpSession.class);
        when(session.getAttribute(RegistryConstants.USER_REGISTRY)).thenReturn(null);
        when(request.getSession()).thenReturn(session);
        context.setProperty("transport.http.servletRequest", request);
        MessageContext.setCurrentMessageContext(context);

        Utils.getRegistry();
    }
}