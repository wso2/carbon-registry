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

import org.junit.Test;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryRealm;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.core.AuthorizationManager;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IndexingUtilsTest {
    @Test
    public void isAuthorized() throws Exception {
        UserRegistry registry = mock(UserRegistry.class);
        AuthorizationManager authManager = mock(AuthorizationManager.class);
        when(authManager.isUserAuthorized("admin", "/_system/governance/trunk/api/test", ActionConstants
                .GET)).thenReturn(Boolean.TRUE);
        RegistryRealm registryRealm = mock(RegistryRealm.class);
        when(registryRealm.getAuthorizationManager()).thenReturn(authManager);
        when(registry.getUserRealm()).thenReturn(registryRealm);
        when(registry.getUserName()).thenReturn("admin");
        assertTrue(IndexingUtils.isAuthorized(registry, "/_system/governance/trunk/api/test", ActionConstants
                .GET));
        assertFalse(IndexingUtils.isAuthorized(registry, "/_system/governance/trunk/api/test1", ActionConstants
                .GET));
        assertFalse(IndexingUtils.isAuthorized(registry, "/_system/governance/trunk/api/test",
                ActionConstants.PUT));
        assertFalse(IndexingUtils.isAuthorized(registry, null,
                ActionConstants.PUT));
    }

    @Test
    public void testGetByteContent() throws Exception {
        ResourceImpl resource = new ResourceImpl();
        String contentString = "Testing Content";
        resource.setContent(contentString);
        byte[] byteContent = IndexingUtils.getByteContent(resource, null);
        assertEquals(contentString, RegistryUtils.decodeBytes(byteContent));

        resource.setContent(null);
        byte[] emptyContent = IndexingUtils.getByteContent(resource, null);
        assertArrayEquals(new byte[0], emptyContent);
    }

    @Test
    public void testGetByteContentWithInvalidContent() throws Exception {
        ResourceImpl resource = new ResourceImpl();
        resource.setContent(new Object());
        try {
            IndexingUtils.getByteContent(resource, null);
            fail("Resource validation is missing");
        } catch (RegistryException e) {
            assertEquals("Cannot return input stream for content of type: java.lang.Object", e.getMessage());
        }
    }

    @Test
    public void testGetByteContentWithoutStream() throws Exception {
        ResourceImpl resource = mock(ResourceImpl.class);
        String contentString = "Testing Content";
        when(resource.getContent()).thenReturn(contentString);
        when(resource.getContentStream()).thenReturn(null);
        byte[] byteContent = IndexingUtils.getByteContent(resource, null);
        assertEquals(contentString, RegistryUtils.decodeBytes(byteContent));
    }

}