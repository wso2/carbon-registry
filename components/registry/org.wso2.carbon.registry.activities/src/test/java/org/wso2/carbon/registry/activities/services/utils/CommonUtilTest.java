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
package org.wso2.carbon.registry.activities.services.utils;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CommonUtilTest {
    @Test
    public void testGetRegistryService() throws Exception {
        RegistryService service = mock(RegistryService.class);
        CommonUtil.setRegistryService(service);
        assertEquals(service, CommonUtil.getRegistryService());
    }

    @Test
    public void getRegistry() throws Exception {
        UserRegistry userRegistry = mock(UserRegistry.class);
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(RegistryConstants.USER_REGISTRY)).thenReturn(userRegistry);
        Assert.assertEquals(userRegistry, CommonUtil.getRegistry(session));
    }

    @Test
    public void getRegistryWithoutSession() throws Exception {
        UserRegistry userRegistry = mock(UserRegistry.class);
        MessageContext messageContext = new MessageContext();
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(RegistryConstants.USER_REGISTRY)).thenReturn(userRegistry);
        when(servletRequest.getSession()).thenReturn(session);
        messageContext.setProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST, servletRequest);
        MessageContext.setCurrentMessageContext(messageContext);
        assertEquals(userRegistry, CommonUtil.getRegistry(null));
    }

}
