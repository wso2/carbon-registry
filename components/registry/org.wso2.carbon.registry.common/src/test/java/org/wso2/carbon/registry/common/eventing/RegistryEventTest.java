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

package org.wso2.carbon.registry.common.eventing;

import junit.framework.TestCase;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.utils.ServerConstants;

import java.io.File;
import java.nio.file.Paths;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class RegistryEventTest extends TestCase {

    private static final String basedir = Paths.get("").toAbsolutePath().toString();
    private static final String testDir = Paths.get(basedir, "src", "test", "resources").toString();
    private static final File testSampleDirectory = Paths.get("target", "carbon-utils-test-directory").toFile();

    private RegistryEvent registryEvent;

    @Override
    protected void setUp() throws Exception {
        testSampleDirectory.mkdirs();
        System.setProperty(ServerConstants.CARBON_HOME, testDir);
        ServerConfiguration serverConfiguration = mock(ServerConfiguration.class);
        when(serverConfiguration.getFirstProperty("Name")).thenReturn("registry");
        registryEvent = new RegistryEvent();
        super.setUp();
    }


    public void testGetTenantId() throws Exception {
        assertEquals(-1, registryEvent.getTenantId());
        registryEvent.setTenantId(-1234);

        assertEquals(-1234, registryEvent.getTenantId());
    }

    public void testGetTimestamp() throws Exception {
        assertNotNull(registryEvent.getTimestamp());
    }

    public void testGetRegistrySessionDetails() throws Exception {
        assertNotNull(registryEvent.getRegistrySessionDetails());
        assertEquals(-1, registryEvent.getRegistrySessionDetails().getTenantId());
        assertNull(registryEvent.getRegistrySessionDetails().getChroot());
        assertNull(registryEvent.getRegistrySessionDetails().getUsername());

        registryEvent.getRegistrySessionDetails().setTenantId(-1234);
        assertEquals(-1234, registryEvent.getRegistrySessionDetails().getTenantId());

        registryEvent.getRegistrySessionDetails().setChroot("/_system/governance");
        assertEquals("/_system/governance", registryEvent.getRegistrySessionDetails().getChroot());

        registryEvent.getRegistrySessionDetails().setUsername("admin");
        assertEquals("admin", registryEvent.getRegistrySessionDetails().getUsername());
    }


    public void testGetContextDetails() throws Exception {
        assertNotNull(registryEvent.getContextDetails());
        assertEquals(-1, registryEvent.getContextDetails().getTenantId());

        registryEvent.getContextDetails().setTenantId(-1234);
        assertEquals(-1234, registryEvent.getContextDetails().getTenantId());

        registryEvent.getContextDetails().setUsername("admin");
        assertEquals("admin", registryEvent.getContextDetails().getUsername());

    }

    public void testGetOperationDetails() throws Exception {
        assertNotNull(registryEvent.getOperationDetails());
        registryEvent.setOperationDetails(null, "report", RegistryEvent.ResourceType.UNKNOWN);


        registryEvent.setOperationDetails("/_system/governance", "report", RegistryEvent.ResourceType.COLLECTION);
        assertEquals("/_system/governance", registryEvent.getOperationDetails().getPath());
        assertEquals("collection", registryEvent.getOperationDetails().getResourceType());
        assertEquals("report", registryEvent.getOperationDetails().getEventType());


        registryEvent.setOperationDetails("/_system/governance", "report", RegistryEvent.ResourceType.RESOURCE);

        assertEquals("/_system/governance", registryEvent.getOperationDetails().getPath());
        assertEquals("resource", registryEvent.getOperationDetails().getResourceType());
        assertEquals("report", registryEvent.getOperationDetails().getEventType());
    }

    public void testGetServerDetails() throws Exception {
        assertNotNull(registryEvent.getServerDetails());
    }


    public void testGetMessage() throws Exception {
        assertNull(registryEvent.getMessage());
        registryEvent.setMessage("Error Message");
        assertEquals("Error Message", registryEvent.getMessage());
    }

    public void testGetTopic() throws Exception {
        assertNull(registryEvent.getTopic());
        registryEvent.setTopic("Topic");
        assertEquals("/registry/notificationsTopic", registryEvent.getTopic());
    }
}