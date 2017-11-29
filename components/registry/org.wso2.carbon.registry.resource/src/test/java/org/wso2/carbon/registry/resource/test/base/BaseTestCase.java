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

package org.wso2.carbon.registry.resource.test.base;

import junit.framework.TestCase;
import org.junit.Before;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.config.RegistryConfiguration;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.internal.RegistryDataHolder;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.core.jdbc.realm.InMemoryRealmService;
import org.wso2.carbon.user.core.service.RealmService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class BaseTestCase extends TestCase {

    protected static RegistryContext ctx;
    protected static Registry registry = null;

    public void setUp() throws RegistryException {
        if (System.getProperty("carbon.home") == null) {
            File file = new File("src/test/resources/carbon-home");
            if (file.exists()) {
                System.setProperty("carbon.home", file.getAbsolutePath());
            }
        }
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain("foo.com");
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(1);
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
    }
}
