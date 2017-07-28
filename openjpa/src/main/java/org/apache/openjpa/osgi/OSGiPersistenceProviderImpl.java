/*
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.apache.openjpa.osgi;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.kernel.BrokerFactory;
import org.apache.openjpa.kernel.ConnectionRetainModes;
import org.apache.openjpa.lib.conf.ConfigurationProvider;
import org.apache.openjpa.lib.conf.Configurations;
import org.apache.openjpa.osgi.deployment.OSGiAwareClassLoaderManager;
import org.apache.openjpa.persistence.*;
import org.apache.openjpa.persistence.osgi.BundleUtils;

import java.util.Map;

public class OSGiPersistenceProviderImpl extends PersistenceProviderImpl {

    private OSGiAwareClassLoaderManager classLoaderManager;

    public OSGiPersistenceProviderImpl(){
        classLoaderManager = new OSGiAwareClassLoaderManager();
    }


    @Override
    public OpenJPAEntityManagerFactory createEntityManagerFactory(String name, String resource, Map m) {
        OSGiPersistenceProductDerivation pd = new OSGiPersistenceProductDerivation();
        try {
            Object poolValue = Configurations.removeProperty(EMF_POOL, m);
            if(classLoaderManager == null){
                throw new PersistenceException("OSGiAwareClasssLoaderManager null.", null, null, true);
            }
            ConfigurationProvider cp = pd.load(resource, name, m, classLoaderManager.getClassLoader(name));
            if (cp == null) {
                return null;
            }

            BrokerFactory factory = getBrokerFactory(cp, poolValue, BundleUtils.getBundleClassLoader());
            OpenJPAConfiguration conf = factory.getConfiguration();
            _log = conf.getLog(OpenJPAConfiguration.LOG_RUNTIME);
            pd.checkPuNameCollisions(_log,name);

            // add enhancer
            loadAgent(factory);

            // Create appropriate LifecycleEventManager
            loadValidator(factory);

            if (conf.getConnectionRetainModeConstant() == ConnectionRetainModes.CONN_RETAIN_ALWAYS) {
                // warn about EMs holding on to connections.
                _log.warn(_loc.get("retain-always", conf.getId()));
            }

            OpenJPAEntityManagerFactory emf = JPAFacadeHelper.toEntityManagerFactory(factory);
            if (_log.isTraceEnabled()) {
                _log.trace(this + " creating " + emf + " for PU " + name + ".");
            }
            return emf;
        } catch (Exception e) {
            if (_log != null) {
                _log.error(_loc.get("create-emf-error", name), e);
            }

            /*
             *
             * Maintain 1.x behavior of throwing exceptions, even though
             * JPA2 9.2 - createEMF "must" return null for PU it can't handle.
             *
             * JPA 2.0 Specification Section 9.2 states:
             * "If a provider does not qualify as the provider for the named persistence unit,
             * it must return null when createEntityManagerFactory is invoked on it."
             * That specification compliance behavior has happened few lines above on null return.
             * Throwing runtime exception in the following code is valid (and useful) behavior
             * because the qualified provider has encountered an unexpected situation.
             */
            throw PersistenceExceptions.toPersistenceException(e);
        }
    }
}
