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

import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.jcr.RegistryNode;
import org.wso2.carbon.registry.jcr.RegistrySession;
import org.wso2.carbon.registry.jcr.nodetype.RegistryNodeType;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.version.VersionException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RegistryQuery implements Query {

    private String statement = "";
    private String language = "";
    private RegistrySession session;


    public RegistryQuery(String statement, String language, RegistrySession session) {

        this.statement = statement;
        this.language = language;
        this.session = session;

    }

    public QueryResult execute() throws InvalidQueryException, RepositoryException {

        QueryResult jcr_result = null;
        Registry registry;
        String sql1 = "";
        registry = session.getUserRegistry();

        if (statement.startsWith("org.wso2.registry.direct.query")) {

            sql1 = statement.split(";;")[1];
        } else {

            sql1 = getConvertedRegQuery(statement);
        }


        Resource q1 = null;
        List nodes = new ArrayList();

        try {
//this modifications are done so that the queries are not stored in the registry.
//            q1 = registry.newResource();

//            q1.setContent(sql1);

//            q1.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);

//            q1.addProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME,

//                    RegistryConstants.RESOURCES_RESULT_TYPE);

//            registry.put(RegistryConstants.CONFIG_REGISTRY_BASE_PATH + RegistryConstants.QUERIES_COLLECTION_PATH + "/custom-queries", q1);
            Map parameters = new HashMap();
            parameters.put("query", sql1);
//            Resource result = registry.executeQuery(RegistryConstants.CONFIG_REGISTRY_BASE_PATH + RegistryConstants.QUERIES_COLLECTION_PATH + "/custom-queries", parameters);
            Resource result = registry.executeQuery(null, parameters);

            String[] paths = (String[]) result.getContent();

            for (String path : paths) {

                nodes.add(session.getNode(path));
            }

        } catch (RegistryException e) {

            e.printStackTrace();
        }

        jcr_result = new RegistryQueryResult(nodes, session);


        return jcr_result;
    }


    public void setLimit(long l) {

    }

    public void setOffset(long l) {

    }

    public String getStatement() {

        return statement;
    }

    public String getLanguage() {

        return language;
    }

    public String getStoredQueryPath() throws ItemNotFoundException, RepositoryException {
        return RegistryConstants.CONFIG_REGISTRY_BASE_PATH +
                RegistryConstants.QUERIES_COLLECTION_PATH +
                "/custom-queries";
    }

    public Node storeAsNode(String s) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, UnsupportedRepositoryOperationException, RepositoryException {
        RegistryNode node = new RegistryNode(s, session);
        CollectionImpl subCollection = null;

        try {
            subCollection = (CollectionImpl) session.getUserRegistry().newCollection();
            subCollection.setDescription("nt:query");
            session.getUserRegistry().put(s, subCollection);

        } catch (RegistryException e) {
            e.printStackTrace();

        }
        node.nodeType = (RegistryNodeType) node.getParent().getPrimaryNodeType();
        node.setPrimaryType("nt:query");

//        node.nodeType.setNode(node);

        return node;
    }

    public void bindValue(String s, Value value) throws IllegalArgumentException, RepositoryException {


    }

    public String[] getBindVariableNames() throws RepositoryException {
        return new String[0];
    }

    private String getConvertedRegQuery(String s) { //Here we assume that all properties are set here under jcr namespace conditions
        String temp[];
        String nodetype = "";
        String reg_sql = "";
        String secndPart = "";
        String firstPart = "";


        if (s.contains("WHERE")) {

            temp = s.split("WHERE");
            firstPart = temp[0];
            secndPart = temp[1];
            if (secndPart.startsWith(" ")) {
                secndPart = secndPart.substring(1, secndPart.length());
            }
            if (secndPart.endsWith(" ")) {
                secndPart = secndPart.substring(0, secndPart.length() - 1);

            }

            String tt[] = firstPart.split(" ");

            for (String tc : tt) {

                if (tc.contains(":")) {
                    nodetype = tc;
                    break;
                }

            }

            String secndPArr[] = secndPart.split(" ");
            String new_secndPart = "";

            for (int i = 0; i < secndPArr.length; i++) {

                String tmpp = secndPArr[i].trim();

                if ((tmpp.contains(":")) && (i < (secndPArr.length - 2)) && (!tmpp.contains("CONTAINS"))) {

                    if ((secndPArr[i + 1].equals("="))) {
                        tmpp = "PP.REG_NAME='" + tmpp + "'" + "AND PP.REG_VALUE = " + secndPArr[i + 2];
                        i = i + 2;
                    } else if ((secndPArr[i + 1].equals(">"))) {
                        tmpp = "PP.REG_NAME='" + tmpp + "'" + "AND PP.REG_VALUE > " + secndPArr[i + 2];
                        i = i + 2;
                    } else if ((secndPArr[i + 1].equals("<"))) {
                        tmpp = "PP.REG_NAME='" + tmpp + "'" + "AND PP.REG_VALUE < " + secndPArr[i + 2];
                        i = i + 2;
                    } else if ((secndPArr[i + 1].equals("!="))) {
                        tmpp = "PP.REG_NAME='" + tmpp + "'" + "AND PP.REG_VALUE != " + secndPArr[i + 2];
                        i = i + 2;
                    } else if ((secndPArr[i + 1].equals(">="))) {
                        tmpp = "PP.REG_NAME='" + tmpp + "'" + "AND PP.REG_VALUE >= " + secndPArr[i + 2];
                        i = i + 2;
                    } else if ((secndPArr[i + 1].equals("<="))) {
                        tmpp = "PP.REG_NAME='" + tmpp + "'" + "AND PP.REG_VALUE <= " + secndPArr[i + 2];
                        i = i + 2;
                    } else {

                        tmpp = "PP.REG_NAME = '" + tmpp + "'";
                    }

                }

                new_secndPart = new_secndPart.concat(tmpp + " ");

            }
            reg_sql = "SELECT R.REG_PATH_ID, R.REG_NAME FROM REG_RESOURCE R, REG_PROPERTY PP, REG_RESOURCE_PROPERTY RP WHERE R.REG_PATH_ID=RP.REG_PATH_ID AND R.REG_NAME IS NULL AND RP.REG_RESOURCE_NAME IS NULL AND RP.REG_PROPERTY_ID=PP.REG_ID AND REG_DESCRIPTION = '" + nodetype + "'";
            reg_sql = reg_sql.concat(" AND(" + new_secndPart + ")");

        } else {

            firstPart = s;
            String tt[] = firstPart.split(" ");

            for (String tc : tt) {

                if (tc.contains(":")) {
                    nodetype = tc;
                    break;
                }
            }
            reg_sql = "SELECT R.REG_PATH_ID, R.REG_NAME FROM REG_RESOURCE R, REG_PROPERTY PP, REG_RESOURCE_PROPERTY RP WHERE R.REG_PATH_ID=RP.REG_PATH_ID AND R.REG_NAME IS NULL AND RP.REG_RESOURCE_NAME IS NULL AND RP.REG_PROPERTY_ID=PP.REG_ID AND REG_DESCRIPTION = '" + nodetype + "'";
        }

        return reg_sql;

    }
}
