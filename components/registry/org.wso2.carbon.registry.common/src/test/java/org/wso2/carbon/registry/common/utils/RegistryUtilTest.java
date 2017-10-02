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

import javax.servlet.http.HttpServletRequest;

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
}