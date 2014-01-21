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

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import java.util.Iterator;
import java.util.List;


public class RegistryNodeIterator implements NodeIterator {

    List<Node> nodes;
    Iterator iterator;
    private long counter = 0;

    public RegistryNodeIterator(List<Node> list) {

        this.nodes = list;
        this.iterator = nodes.iterator();

    }

    public Node nextNode() {

        counter++;

        return (Node) iterator.next();
    }

    public void skip(long l) {
        counter = l;
    }

    public long getSize() {

        return nodes.size();
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
