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

package org.wso2.carbon.registry.jcr.query;

import org.wso2.carbon.registry.jcr.RegistryNode;
import org.wso2.carbon.registry.jcr.RegistryValue;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Row;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


public class RegistryRow implements Row {
    RegistryNode node;

    public RegistryRow(RegistryNode node) {

        this.node = node;

    }

    public Value[] getValues() throws RepositoryException {

        List<Value> values = new LinkedList<Value>();
        int i = 0;

        if (getNode().getProperties() != null) {
            Iterator it = getNode().getProperties();

            while (it.hasNext()) {
                values.add(new RegistryValue(it.next()));

            }
            return values.toArray(new Value[values.size()]);
        }

        return new Value[0];
    }

    public Value getValue(String s) throws ItemNotFoundException, RepositoryException {

        if (getNode().getProperty(s) != null) {

            return getNode().getProperty(s).getValue();

        } else {
            return null;
        }

    }

    public Node getNode() throws RepositoryException {

        return node;
    }

    public Node getNode(String s) throws RepositoryException {

        return getNode().getNode(s);
    }

    public String getPath() throws RepositoryException {

        return getNode().getPath();
    }

    public String getPath(String s) throws RepositoryException {

        return getNode(s).getPath();
    }

    public double getScore() throws RepositoryException {  //TODO

        return 0;
    }

    public double getScore(String s) throws RepositoryException { //TODO
        return 0;
    }
}
