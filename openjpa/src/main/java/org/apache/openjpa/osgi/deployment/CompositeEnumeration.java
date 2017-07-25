package org.apache.openjpa.osgi.deployment;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * CompositeEnumeration is, as the name implies, a Composite of Enumerations.
 * It provides a way to iterate across a set of Enumerations as if they are
 * a single Enumeration.  The order of the elements returned reflects the order
 * of the Enumerations in the Vector passed to the constructor.
 *
 * @author shsmith from EclipseLink
 *
 * @see org.apache.openjpa.osgi.deployment.CompositeClassLoader
 *
 * @param <T>
 */
public class CompositeEnumeration<T> implements Enumeration<T> {

    private Enumeration<T> currentEnumeration;
    private Iterator<Enumeration<T>> enumerationIterator;

    public CompositeEnumeration(List<Enumeration<T>> enumerations) {
        this.enumerationIterator = enumerations.iterator();
        if (this.enumerationIterator.hasNext()) {
            this.currentEnumeration = this.enumerationIterator.next();
        } else {
            this.currentEnumeration = new NullObjectEnumeration();
        }
    }

    public boolean hasMoreElements() {
        boolean hasMoreElements = this.currentEnumeration.hasMoreElements();
        if (hasMoreElements) {
            return true;
        } else {
            if (this.enumerationIterator.hasNext()) {
                this.currentEnumeration = this.enumerationIterator.next();
                return this.hasMoreElements();
            } else {
                return false;
            }
        }
    }

    public T nextElement() {
        return this.currentEnumeration.nextElement();
    }

    private final class NullObjectEnumeration implements Enumeration<T> {
        public boolean hasMoreElements() {
            return false;
        }

        public T nextElement() {
            throw new NoSuchElementException();
        }
    }


}

