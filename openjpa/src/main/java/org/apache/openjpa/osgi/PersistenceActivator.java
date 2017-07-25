/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openjpa.osgi;

import java.util.Hashtable;

import javax.persistence.spi.PersistenceProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.openjpa.osgi.deployment.OSGiAwareClassLoaderManager;
import org.apache.openjpa.persistence.PersistenceProviderImpl;
import org.osgi.framework.*;


/**
 * Used to discover/resolve JPA providers in an OSGi environment.
 *
 * @version $Rev$ $Date$
 */
public class PersistenceActivator implements BundleActivator, SynchronousBundleListener {

    private static Log log = LogFactory.getLog(PersistenceActivator.class);

    public static final String PERSISTENCE_PROVIDER = PersistenceProvider.class.getName();
    public static final String OSGI_PERSISTENCE_PROVIDER = PersistenceProviderImpl.class.getName();
    private static BundleContext ctx = null;

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext arg0) throws Exception {
        ctx = arg0;
        registerBundleListener();
        PersistenceProvider provider = new OSGiPersistenceProviderImpl();
        Hashtable<String, String> props = new Hashtable<String, String>();
        props.put(PERSISTENCE_PROVIDER, OSGI_PERSISTENCE_PROVIDER);
        ctx.registerService(PERSISTENCE_PROVIDER, provider, props);
    }

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext arg0) throws Exception {
        getContext().removeBundleListener(this);
    }

    /**
     * Add our bundle listener
     */
    private void registerBundleListener() {
        getContext().addBundleListener(this);
        Bundle bundles[] = getContext().getBundles();
        for (int i = 0; i < bundles.length; i++) {
            Bundle bundle = bundles[i];
            registerBundle(bundle);
        }
    }

    public void bundleChanged(BundleEvent bundleEvent) {
        switch (bundleEvent.getType()) {
            case BundleEvent.STARTING:
                registerBundle(bundleEvent.getBundle());
                break;

            case BundleEvent.STOPPING:
                deregisterBundle(bundleEvent.getBundle());
                break;
        }
    }

    /**
     * Store a reference to a bundle as it is started so the bundle
     * can be accessed later
     *
     * @param bundle
     */
    private void registerBundle(Bundle bundle) {
        if ((bundle.getState() & (Bundle.STARTING | Bundle.RESOLVED | Bundle.ACTIVE)) != 0) {
            if (!OSGiAwareClassLoaderManager.includesBundle(bundle)) {
                try {
                    String[] persistenceUnitNames = getPersistenceUnitNames(bundle);
                    if (persistenceUnitNames != null) {
                        OSGiAwareClassLoaderManager.addBundle(bundle, persistenceUnitNames);
                    }
                } catch (Exception e) {
                    log.warn(e);
                }
            }
        }
    }

    private String[] getPersistenceUnitNames(Bundle bundle) {
        String names = (String) bundle.getHeaders().get("JPA-PersistenceUnits");
        if (names != null) {
            if(log.isDebugEnabled())
                log.debug("JPA-PersistenceUnits:" + names);
            return names.split(",");
        } else {
            return null;
        }
    }


    private void deregisterBundle(Bundle bundle) {
        OSGiAwareClassLoaderManager.removeBundle(bundle);
    }

    public static BundleContext getContext() {
        return ctx;
    }
}
