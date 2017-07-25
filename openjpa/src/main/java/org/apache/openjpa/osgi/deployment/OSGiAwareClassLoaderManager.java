package org.apache.openjpa.osgi.deployment;

import org.apache.openjpa.osgi.PersistenceActivator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.util.*;

public class OSGiAwareClassLoaderManager {
    // these maps are used to retrieve the classloader used for different bundles
    private static Map<String, Bundle> puToBundle = Collections.synchronizedMap(new HashMap<String, Bundle>());
    private static Map<Bundle, String[]> bundleToPUs = Collections.synchronizedMap(new HashMap<Bundle, String[]>());

    private Map<String, ClassLoader> puClassLoaders = new HashMap<String, ClassLoader>();

    /**
     * Add a bundle to the list of bundles managed by this persistence provider
     * The bundle is indexed so it's classloader can be accessed
     *
     * @param bundle
     * @param persistenceUnitNames
     */
    public static void addBundle(Bundle bundle, String[] persistenceUnitNames) {
        for (int i = 0; i < persistenceUnitNames.length; i++) {
            String name = persistenceUnitNames[i];
            puToBundle.put(name, bundle);
        }
        bundleToPUs.put(bundle, persistenceUnitNames);
    }

    /**
     * Removed a bundle from the list of bundles managed by this persistence provider
     * This typically happens on deactivation.
     *
     * @param bundle
     */
    public static void removeBundle(Bundle bundle) {
        String[] persistenceUnitNames = bundleToPUs.remove(bundle);
        if (persistenceUnitNames != null) {
            for (int i = 0; i < persistenceUnitNames.length; i++) {
                String name = persistenceUnitNames[i];
                puToBundle.remove(name);
            }
        }
    }

    public static boolean includesBundle(Bundle bundle) {
        return bundleToPUs.containsKey(bundle);
    }

    /**
     * Answer the ClassLoader to use to create an EntityManager.
     * The result is a CompositeClassLoader capable of loading
     * classes from the optionally provided ClassLoader, the
     * bundle that provides the persistence unit (i.e., contains the
     * persistence.xml, and EclipseLink classes (since the
     * persistence unit bundle may not have an explicit dependency
     * on EclipseLink).
     *
     * @param persistenceUnitName Persistence unit name
     * @return ClassLoader
     */
    public ClassLoader getClassLoader(String persistenceUnitName) {
        // This method is called more than once for the same persistence unit so
        // check to see if a ClassLoader already constructed and if so return it.
        ClassLoader previouslyDefinedLoader = puClassLoaders.get(persistenceUnitName);
        if (previouslyDefinedLoader != null) {
            return previouslyDefinedLoader;
        }

        List<ClassLoader> loaders = new ArrayList<ClassLoader>();


        // If a bundle has registered for a persistence unit
        // then wrap the bundle to support the ClassLoader interface.
        Bundle bundle = puToBundle.get(persistenceUnitName);
        if (bundle != null) {
            ClassLoader bundleClassLoader = new OSGiBundleProxyClassLoader(bundle);
            loaders.add(bundleClassLoader);
        }

        if ((bundle == null)) {
            return null;
        }

        // Add the EclipseLink Core bundle loader so we can see classes
        // (such as platforms) in fragments,
        BundleContext context = PersistenceActivator.getContext();
        if (context != null) {
            // Add the OpenJPA bundle ClassLoader so that we can load
            // OpenJPA classes.
            ClassLoader eclipseLinkJpaClassLoader = new OSGiBundleProxyClassLoader(context.getBundle());
            loaders.add(eclipseLinkJpaClassLoader);
        }

        ClassLoader puClassLoader = new CompositeClassLoader(loaders);

        puClassLoaders.put(persistenceUnitName, puClassLoader);  // cache to avoid reconstruction
        return puClassLoader;
    }

}
