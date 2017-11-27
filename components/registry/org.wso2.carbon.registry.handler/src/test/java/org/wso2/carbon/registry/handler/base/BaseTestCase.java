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

package org.wso2.carbon.registry.handler.base;

import org.junit.After;
import org.junit.Before;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.config.RegistryConfiguration;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.internal.RegistryDataHolder;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.core.jdbc.realm.InMemoryRealmService;
import org.wso2.carbon.user.core.service.RealmService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BaseTestCase {

    protected static RegistryContext ctx;
    protected static Registry registry = null;
    protected static Registry configRegistry = null;
    private static final String OS_NAME_KEY = "os.name";
    private static final String WINDOWS_PARAM = "indow";

    @Before
    public void setUp() throws Exception {
        if (System.getProperty("carbon.home") == null) {
            File file = getResourcePath("carbon-home").toFile();
            if (file.exists()) {
                System.setProperty("carbon.home", file.getAbsolutePath());
            }
        }
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234, true);
        // The line below is responsible for initializing the cache.
        CarbonContext.getThreadLocalCarbonContext();

        String carbonHome = System.getProperty("carbon.home");
        String carbonXMLPath = carbonHome + File.separator + "repository"
                + File.separator + "conf" + File.separator + "carbon.xml";
        RegistryConfiguration regConfig = new RegistryConfiguration(carbonXMLPath);
        RegistryCoreServiceComponent.setRegistryConfig(regConfig);

        RealmService realmService = new InMemoryRealmService();
        RegistryDataHolder.getInstance().setRealmService(realmService);
        InputStream is = null;

        try {
            is = this.getClass().getClassLoader().getResourceAsStream("registry.xml");
            ctx = RegistryContext.getBaseInstance(is, realmService);
        } finally {
            if( is != null) {
                try {
                    is.close();
                } catch (IOException ignore) {

                }
            }
        }

        ctx.setSetup(true);
        ctx.selectDBConfig("h2-db");

        EmbeddedRegistryService embeddedRegistry = ctx.getEmbeddedRegistryService();
        new RegistryCoreServiceComponent().registerBuiltInHandlers(embeddedRegistry);
        registry = embeddedRegistry.getRegistry("admin", "admin");
        configRegistry = embeddedRegistry.getConfigUserRegistry("admin", "admin");
    }

    @After
    public void tearDown() throws Exception {
        PrivilegedCarbonContext.endTenantFlow();
    }

    private static Path getResourcePath(String... resourcePaths) {
        URL resourceURL = BaseTestCase.class.getClassLoader().getResource("");
        if (resourceURL != null) {
            String resourcePath = resourceURL.getPath();
            if (resourcePath != null) {
                resourcePath = System.getProperty(OS_NAME_KEY).contains(WINDOWS_PARAM) ?
                        resourcePath.substring(1) : resourcePath;
                return Paths.get(resourcePath, resourcePaths);
            }
        }
        return null; // Resource do not exist
    }
}
