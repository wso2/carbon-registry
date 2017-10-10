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
package org.wso2.carbon.registry.indexing.service;

import org.junit.Before;
import org.junit.Test;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.apache.axis2.context.MessageContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class UtilsTest {
    private UserRegistry userRegistry = null;

    @Before
    public void setup() throws Exception {
        RegistryService service = mock(RegistryService.class);
        userRegistry = mock(UserRegistry.class);
        when(service.getUserRegistry()).thenReturn(userRegistry);
        Utils.setRegistryService(service);
    }

    @Test
    public void testGetRegistry() throws Exception {
        MessageContext context = new MessageContext();
        MessageContext.setCurrentMessageContext(context);
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        HttpSession httpSession = mock(HttpSession.class);
        when(httpSession.getAttribute(RegistryConstants.USER_REGISTRY)).thenReturn(userRegistry);
        when(httpRequest.getSession()).thenReturn(httpSession);
        context.setProperty("transport.http.servletRequest", httpRequest);
        assertEquals(userRegistry, Utils.getRegistry());
    }

    @Test(expected = RegistryException.class)
    public void testGetRegistryWithoutMC() throws Exception {
        MessageContext.setCurrentMessageContext(null);
        assertEquals(userRegistry, Utils.getRegistry());
    }

    @Test
    public void testGetRegistryFromService() throws Exception {
        MessageContext context = new MessageContext();
        MessageContext.setCurrentMessageContext(context);
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        HttpSession httpSession = mock(HttpSession.class);
        when(httpSession.getAttribute(RegistryConstants.USER_REGISTRY)).thenReturn(null);
        when(httpRequest.getSession()).thenReturn(httpSession);
        context.setProperty("transport.http.servletRequest", httpRequest);
        assertEquals(userRegistry, Utils.getRegistry());
    }

    @Test(expected = RegistryException.class)
    public void testGetRegistryWithoutService() throws Exception {
        MessageContext context = new MessageContext();
        MessageContext.setCurrentMessageContext(context);
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        HttpSession httpSession = mock(HttpSession.class);
        when(httpSession.getAttribute(RegistryConstants.USER_REGISTRY)).thenReturn(null);
        when(httpRequest.getSession()).thenReturn(httpSession);
        context.setProperty("transport.http.servletRequest", httpRequest);
        Utils.setRegistryService(null);
        assertEquals(userRegistry, Utils.getRegistry());
    }
}