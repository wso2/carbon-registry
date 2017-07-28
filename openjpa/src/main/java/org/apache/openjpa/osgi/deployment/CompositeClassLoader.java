package org.apache.openjpa.osgi.deployment;

import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.net.URL;
import java.io.InputStream;
import java.io.IOException;

public class CompositeClassLoader extends ClassLoader {
    // Hold the context class loaders for OSGi bundles which expose persistence units.
    private List<ClassLoader> classLoaders = new ArrayList<ClassLoader>();

    /**
     * Create composite class loader from list of class loaders.
     *
     * @param loaders class loader list
     */
    public CompositeClassLoader(List<ClassLoader> loaders) {
        classLoaders.addAll(loaders);
    }

    /**
     * Get the list of class loaders.
     *
     * @return classloader list
     */
    public List<ClassLoader> getClassLoaders() {
        return classLoaders;
    }

    /**
     * Sets the default assertion status for this class loader to
     * <tt>false</tt> and discards any package defaults or class assertion
     * on all contained class loaders.
     *
     * @see ClassLoader#clearAssertionStatus()
     */
    @Override
    public synchronized void clearAssertionStatus() {
        for (ClassLoader classLoader : getClassLoaders()) {
            classLoader.clearAssertionStatus();
        }
    }

    /**
     * Finds the resource with the given name.  Contained class
     * loaders are queried until one returns the requested
     * resource or <tt>null</tt> if not found.
     *
     * @see ClassLoader#getResource(String)
     */
    @Override
    public URL getResource(String name) {
        for (ClassLoader classLoader : getClassLoaders()) {
            URL resource = classLoader.getResource(name);
            if (resource != null) {
                return resource;
            }
        }
        return null;
    }

    /**
     * Returns an input stream for reading the specified resource.
     * Contained class loaders are queried until one returns the
     * requested resource stream or <tt>null</tt> if not found.
     *
     * @see ClassLoader#getResourceAsStream(String)
     */
    @Override
    public InputStream getResourceAsStream(String name) {
        for (ClassLoader classLoader : getClassLoaders()) {
            InputStream stream = classLoader.getResourceAsStream(name);
            if (stream != null) {
                return stream;
            }
        }
        return null;
    }

    /**
     * Finds all the resources with the given name. Contained class
     * loaders are queried and the results aggregated into a single
     * Enumeration.
     *
     * @throws IOException If I/O errors occur
     * @see ClassLoader#getResources(String)
     */
    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        List<Enumeration<URL>> enumerations = new ArrayList(getClassLoaders().size());
        for (int i = 0; i < getClassLoaders().size(); i++) {
            enumerations.add(i, getClassLoaders().get(i).getResources(name));
        }
        return new CompositeEnumeration<URL>(enumerations);
    }

    /**
     * Loads the class with the specified <a href="#name">binary name</a>.
     * Contained class loaders are queried until one returns the
     * requested class.
     *
     * @throws ClassNotFoundException If the class was not found
     * @see ClassLoader#loadClass(String)
     */
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        for (ClassLoader classLoader : getClassLoaders()) {
            try {
                Class<?> aClass = classLoader.loadClass(name);
                return aClass;
            } catch (ClassNotFoundException e) {                
            }
        }
        throw new ClassNotFoundException(name);
    }

    /**
     * Sets the desired assertion status for the named top-level class.
     *
     * @see ClassLoader#setClassAssertionStatus(String, boolean)
     */
    @Override
    public synchronized void setClassAssertionStatus(String className, boolean enabled) {
        for (ClassLoader classLoader : getClassLoaders()) {
            classLoader.setClassAssertionStatus(className, enabled);
        }
    }

    /**
     * Sets the default assertion status for this class loader.
     *
     * @see ClassLoader#setDefaultAssertionStatus(boolean)
     */
    @Override
    public synchronized void setDefaultAssertionStatus(boolean enabled) {
        for (ClassLoader classLoader : getClassLoaders()) {
            classLoader.setDefaultAssertionStatus(enabled);
        }
    }

    /**
     * Sets the package default assertion status for the named package.
     *
     * @see ClassLoader#setPackageAssertionStatus(String,boolean)
     */
    @Override
    public synchronized void setPackageAssertionStatus(String packageName, boolean enabled) {
        for (ClassLoader classLoader : getClassLoaders()) {
            classLoader.setPackageAssertionStatus(packageName, enabled);
        }
    }


}
