/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.common.utils;

import junit.framework.TestCase;
import org.apache.axis2.context.MessageContext;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class RegistryUtilTest extends TestCase {

    public void testGetPathFromParameter() throws Exception {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getParameter("path")).thenReturn("/_system/governance/resource1");
        String path = RegistryUtil.getPath(httpServletRequest);
        assertEquals("Expecting /_system/governance/resource1 as path value ", path, "/_system/governance/resource1");
    }

    public void testGetPathFromAttribute() throws Exception {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getAttribute("path")).thenReturn("/_system/governance/resource1");
        String path = RegistryUtil.getPath(httpServletRequest);
        assertEquals("Expecting /_system/governance/resource1 as path value ", path, "/_system/governance/resource1");

    }

    public void testGetPathErrorCase() throws Exception {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        String path = RegistryUtil.getPath(httpServletRequest);
        assertNull(path);

    }

    public void testGetSessionResourcePath() throws Exception {
        MessageContext messageContext = mock(MessageContext.class);
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        String path = "/_system/governance/trunk";
        HttpSession httpSession = mock(HttpSession.class);
        MessageContext.setCurrentMessageContext(messageContext);
        when(messageContext.getProperty("transport.http.servletRequest")).thenReturn(httpServletRequest);
        when(httpServletRequest.getSession()).thenReturn(httpSession);
        when(httpSession.getAttribute(RegistryConstants.SESSION_RESOURCE_PATH)).thenReturn(path);
        String userRegistryReturned = RegistryUtil.getSessionResourcePath();
        assertEquals(path, userRegistryReturned);
        MessageContext.setCurrentMessageContext(null);
    }

    public void testGetSessionResourcePathErrorCase() throws Exception {
        MessageContext.setCurrentMessageContext(null);
        try {
            RegistryUtil.getSessionResourcePath();
        } catch (RegistryException e) {
            assertEquals("Could not get the user's Registry session. Message context not found.", e.getMessage());
        }
    }

    public void testSetSessionResourcePath() throws Exception {
        MessageContext messageContext = mock(MessageContext.class);
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        String path = "/_system/governance/soapservces";
        HttpSession httpSession = mock(HttpSession.class);
        MessageContext.setCurrentMessageContext(messageContext);
        when(messageContext.getProperty("transport.http.servletRequest")).thenReturn(httpServletRequest);
        when(httpServletRequest.getSession()).thenReturn(httpSession);
        when(httpSession.getAttribute(RegistryConstants.SESSION_RESOURCE_PATH)).thenReturn(path);
        RegistryUtil.setSessionResourcePath(path);
        MessageContext.setCurrentMessageContext(null);
    }

    public void testSetSessionResourcePathErrorCase() throws Exception {
        MessageContext.setCurrentMessageContext(null);
        try {
            String path = "/_system/governance/soapservces";
            RegistryUtil.setSessionResourcePath(path);
        } catch (RegistryException e) {
            assertEquals("Could not get the user's Registry session. Message context not found.", e.getMessage());
        }
    }

    public void testGetResourcePath() throws Exception {
        MessageContext messageContext = mock(MessageContext.class);
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        String path = "/_system/governance/service1";
        HttpSession httpSession = mock(HttpSession.class);
        MessageContext.setCurrentMessageContext(messageContext);
        when(messageContext.getProperty("transport.http.servletRequest")).thenReturn(httpServletRequest);
        when(httpServletRequest.getSession()).thenReturn(httpSession);
        when(httpSession.getAttribute(RegistryConstants.SESSION_RESOURCE_PATH)).thenReturn(path);
        String userRegistryReturned = RegistryUtil.getResourcePath();
        assertEquals(path, userRegistryReturned);
        MessageContext.setCurrentMessageContext(null);
    }

    public void testGetResourcePathRootPath() throws Exception {
        MessageContext messageContext = mock(MessageContext.class);
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpSession httpSession = mock(HttpSession.class);
        MessageContext.setCurrentMessageContext(messageContext);
        when(messageContext.getProperty("transport.http.servletRequest")).thenReturn(httpServletRequest);
        when(httpServletRequest.getSession()).thenReturn(httpSession);
        when(httpSession.getAttribute(RegistryConstants.SESSION_RESOURCE_PATH)).thenReturn(null);
        String userRegistryReturned = RegistryUtil.getResourcePath();
        assertEquals(RegistryConstants.ROOT_PATH, userRegistryReturned);
        MessageContext.setCurrentMessageContext(null);
    }

    public void testGetResourcePathErrorCase() throws Exception {
        MessageContext.setCurrentMessageContext(null);
        try {
            RegistryUtil.getResourcePath();
        } catch (RegistryException e) {
            assertEquals("Could not get the user's Registry session. Message context not found.", e.getMessage());
        }
    }


    public void testGenerateOptionsFor() throws Exception {
        String[] options = {"Chandana", "Danesh", "Kasun"};
        assertEquals("<option value=\"Chandana\" selected>Chandana</option>\n" +
                             "<option value=\"Danesh\">Danesh</option>\n" +
                             "<option value=\"Kasun\">Kasun</option>\n",
                     RegistryUtil.generateOptionsFor("Chandana", options));
    }


    public void testGetResourcePathFromVersionPath() throws Exception {
        String version1Path = "/_system/governance/trunk;version:1";
        String version2Path = "/_system/governance/trunk;version:1";
        String path = "/_system/governance/trunk";
        assertEquals(path, RegistryUtil.getResourcePathFromVersionPath(version1Path));
        assertEquals(path, RegistryUtil.getResourcePathFromVersionPath(version2Path));
    }
}