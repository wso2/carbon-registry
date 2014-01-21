/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.extensions.test.app;

import org.wso2.carbon.registry.extensions.test.jdbc.DefaultLifecycleTest;
import org.wso2.carbon.registry.extensions.test.utils.RegistryServer;
import org.wso2.carbon.registry.app.RemoteRegistry;

import java.net.URL;

/**
 * Test the DefaultLifecycle over APP.
 */
public class APPBasedLifeCycleTest extends DefaultLifecycleTest {
    RegistryServer server = new RegistryServer();
    String clientPort = "8081";

    public void setUp() {
        try {
            String portProp = System.getProperty("clientPort");
            if (portProp != null) {
                clientPort = portProp;
            }

            if (registry == null) {
                server.start();
                registry = new RemoteRegistry(new URL("http://localhost:" + clientPort +
                                                      "/wso2registry"),
                                              "admin", "admin");
            }
        } catch (Exception e) {
            fail("Failed to initialize the registry.");
        }
    }
}
