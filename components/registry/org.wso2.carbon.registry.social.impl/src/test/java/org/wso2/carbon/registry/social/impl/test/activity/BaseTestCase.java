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

package org.wso2.carbon.registry.social.impl.test.activity;

import junit.framework.TestCase;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.config.RegistryConfiguration;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.jdbc.realm.InMemoryRealmService;
import org.wso2.carbon.user.core.service.RealmService;

import java.io.File;
import java.io.InputStream;

public class BaseTestCase extends TestCase {
/*
    public void setUp() {
        if (System.getProperty("carbon.home") == null) {
            File file = new File("distribution/carbon-home");
            if (file.exists()) {
                System.setProperty("carbon.home", file.getAbsolutePath());
            }
            file = new File("../distribution/carbon-home");
            if (file.exists()) {
                System.setProperty("carbon.home", file.getAbsolutePath());
            }
        }

        *//*
        try {
            String carbonHome = System.getProperty("carbon.home");
            String carbonXMLPath = carbonHome + File.separator + "repository" + File.separator + "conf" + File.separator
                        + "carbon.xml";
            RegistryConfiguration regConfig = new RegistryConfiguration(carbonXMLPath);
            RealmUnawareRegistryCoreServiceComponent.setRegistryConfig(regConfig);
        } catch (RegistryException e) {
            throw new RuntimeException("Error creating RegistryConfig" + e.getMessage(), e);
        }
        *//*
    }*/

     protected static RegistryContext ctx;

    public void setUp() throws RegistryException {
        if (System.getProperty("carbon.home") == null) {
            File file = new File("src/test/resources/carbon-home");
            if (file.exists()) {
                System.setProperty("carbon.home", file.getAbsolutePath());
            }
        }

        // The line below is responsible for initializing the cache.
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain("foo.com");
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(1);
        CarbonContext.getThreadLocalCarbonContext();

        String carbonHome = System.getProperty("carbon.home");
        System.out.println("carbon home " + carbonHome);
        String carbonXMLPath = carbonHome + File.separator + "repository"
                + File.separator + "conf" + File.separator + "carbon.xml";
        RegistryConfiguration regConfig = new RegistryConfiguration(carbonXMLPath);
        RegistryCoreServiceComponent.setRegistryConfig(regConfig);

        RealmService realmService = new InMemoryRealmService();
        InputStream is;

        try {
            is = this.getClass().getClassLoader().getResourceAsStream(
                    "registry.xml");
        } catch (Exception e) {
            is = null;
        }
        ctx = RegistryContext.getBaseInstance(is, realmService);
        //RegistryConfigurationProcessor.populateRegistryConfig(is, ctx);
        ctx.setSetup(true);
        ctx.selectDBConfig("h2-db");
    }

    public class RealmUnawareRegistryCoreServiceComponent extends
                                                          RegistryCoreServiceComponent {

        public void setRealmService(RealmService realmService) {
            super.setRealmService(realmService);
        }
    }
}
