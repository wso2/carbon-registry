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
import org.wso2.carbon.registry.jcr.RegistryNodeIterator;
import org.wso2.carbon.registry.jcr.RegistrySession;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;
import javax.jcr.query.RowIterator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class RegistryQueryResult implements QueryResult {
    List nodes = new ArrayList();


    public RegistryQueryResult(List nodes, RegistrySession session) {

        this.nodes = nodes;

    }

    public RegistryQueryResult() { //as we dont support QOM query operations

    }


    public String[] getColumnNames() throws RepositoryException {
        return new String[0];
    }

    public RowIterator getRows() throws RepositoryException {
        RegistryNode regNode = null;
        Set<RegistryRow> rows = new HashSet<RegistryRow>();

        for (Object node : nodes) {
            RegistryRow row;
            regNode = (RegistryNode) node;
            row = new RegistryRow((RegistryNode) node);

            rows.add(row);

        }

        return new RegistryRowIterator(rows);
    }

    public NodeIterator getNodes() throws RepositoryException {

        return new RegistryNodeIterator(nodes);
    }

    public String[] getSelectorNames() throws RepositoryException {    //TODO
        return new String[0];
    }
}
