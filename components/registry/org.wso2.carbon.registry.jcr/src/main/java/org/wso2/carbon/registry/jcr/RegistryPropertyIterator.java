/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.jcr;

import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import java.util.Iterator;
import java.util.Set;

public class RegistryPropertyIterator implements PropertyIterator {

    Set<String> prop;
    private long counter = 0;
    Iterator iterator;

    public RegistryPropertyIterator(Set properties, RegistryNode node) {

        this.iterator = properties.iterator();
        this.prop = properties;
    }

    public Property nextProperty() {

        return (Property) next();
    }

    public void skip(long l) {

    }

    public long getSize() {

        return prop.size();
    }

    public long getPosition() {
        return counter;
    }

    public boolean hasNext() {
        return iterator.hasNext();
    }

    public Object next() {

        return iterator.next();
    }

    public void remove() {

        iterator.remove();

    }

}
