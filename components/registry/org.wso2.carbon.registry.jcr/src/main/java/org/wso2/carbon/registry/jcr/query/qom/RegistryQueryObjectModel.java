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

package org.wso2.carbon.registry.jcr.query.qom;

import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.jcr.RegistryNode;
import org.wso2.carbon.registry.jcr.RegistrySession;
import org.wso2.carbon.registry.jcr.query.RegistryQueryResult;
import org.wso2.carbon.registry.jcr.util.query.qom.QOMUtil;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.*;
import javax.jcr.version.VersionException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;


public class RegistryQueryObjectModel implements QueryObjectModel {

    private Source source = null;
    private Constraint constraint = null;
    private Ordering[] orderings = null;
    private Column[] columns = null;
    private RegistrySession session = null;
    private String statement = "";

    private List sourceSet = new ArrayList();

    public RegistryQueryObjectModel(Source source, Constraint constraint,
                                    Ordering[] orderings, Column[] columns, Session session) {

        this.source = source;
        this.constraint = constraint;
        if (orderings != null) {
            this.orderings = Arrays.copyOf(orderings, orderings.length);
        }
        if (columns != null) {
            this.columns = Arrays.copyOf(columns, columns.length);
        }
        this.session = (RegistrySession) session;

    }

    public Source getSource() {

        return source;
    }

    public Constraint getConstraint() {

        return constraint;
    }

    public Ordering[] getOrderings() {

        if (orderings != null) {

            return Arrays.copyOf(orderings, orderings.length);
        } else {
            return new Ordering[0];
        }
    }

    public Column[] getColumns() {

        if (columns != null) {

            return Arrays.copyOf(columns, columns.length);
        } else {
            return new Column[0];
        }
    }

    public List evaluateAnd(List set, Constraint andConstraint) throws RepositoryException {  //mine

        List andResult = new ArrayList();
        List result1;
        List result2;

        Constraint constraint1 = ((RegistryAnd) andConstraint).getConstraint1();
        Constraint constraint2 = ((RegistryAnd) andConstraint).getConstraint2();

        if (constraint1 instanceof And) {

            result1 = evaluateAnd(set, constraint1);
        } else {

            result1 = executeQuery(set, constraint1);
        }

        if (constraint2 instanceof And) {

            result2 = evaluateAnd(set, constraint2);
        } else {

            result2 = executeQuery(set, constraint2);
        }

        Iterator it1 = result1.iterator();

        while (it1.hasNext()) {

            RegistryNode temp = (RegistryNode) it1.next();
            Iterator it2 = result2.iterator();

            while (it2.hasNext()) {

                if (temp.getPath().equals(((RegistryNode) it2.next()).getPath())) {
                    andResult.add(temp);
                }
            }
        }

        return getfilteredFinalResult(set, andResult);

    }


    public List evaluateNot(List source_set, Constraint andConstraint) throws RepositoryException {     //mine

        List result1;

        Constraint constraint1 = ((RegistryAnd) andConstraint).getConstraint1();

        if (constraint1 instanceof Not) {

            result1 = evaluateNot(source_set, constraint1);
        } else {

            result1 = executeQuery(source_set, constraint1);
        }

        Iterator it1 = result1.iterator();
        List tempSet = new ArrayList();

        while (it1.hasNext()) {

            RegistryNode temp = (RegistryNode) it1.next();
            Iterator it2 = source_set.iterator();

            while (it2.hasNext()) {

                if (temp.getPath().equals(((RegistryNode) it2.next()).getPath())) {

                    tempSet.add(temp);
                }
            }
        }

        source_set.removeAll(tempSet);

        return source_set;

    }

    public List evaluateComparison(List set, Constraint constraint) throws RepositoryException{  //mine
        Operand operand1 = ((RegistryComparison) constraint).getOperand1();
        Operand operand2 = ((RegistryComparison) constraint).getOperand2();
        Value scalarOperandValue = null;
        Value[] dynamicOperandValues = null;
        List resultSet = new ArrayList();

        String operator = ((RegistryComparison) constraint).getOperator();

        try {
            ValueFactory valueFactory = session.getValueFactory();

            for (Object obj : set) {
                Node node = (Node) obj;

                if (operand1 instanceof PropertyValue) {
                    String propName = ((PropertyValue) operand1).getPropertyName();
                    dynamicOperandValues = QOMUtil.getPropertyValueFromName(propName, node);
                    if(dynamicOperandValues == null) {
                      continue;
                    }

                } else if (operand1 instanceof Literal) {
                    dynamicOperandValues[0] = ((Literal) operand1).getLiteralValue();
                } else if (operand1 instanceof BindVariableValue) {
                    dynamicOperandValues[0] = valueFactory.createValue(((BindVariableValue) operand1).getBindVariableName());
                }

                if (operand2 instanceof Literal) {
                    scalarOperandValue = ((Literal) operand2).getLiteralValue();
                } else if (operand2 instanceof BindVariableValue) {
                    scalarOperandValue = valueFactory.createValue(((BindVariableValue) operand2).getBindVariableName());
                } else if (operand2 instanceof PropertyValue) {
                    String propName = ((PropertyValue) operand2).getPropertyName();
                    scalarOperandValue = QOMUtil.getPropertyValueFromName(propName, node)[0];
                }


                for (Value value : dynamicOperandValues) {
                    boolean comparison = QOMUtil.evalComparison(value, scalarOperandValue,operator);
                    if (comparison) {
                        resultSet.add(node);
                    }
                }

            }

        } catch (RepositoryException e) {
            throw new RepositoryException("Error while executing query comparison " + e.getMessage());
        }

        return resultSet;
    }



    /**
     * Tests whether the selector  node is a child of a node reachable by absolute path path.
     * A node-tuple satisfies the constraint only if:
     * selectorNode.getParent().isSame(session.getNode(path))
     * <p/>
     * Though we recieve a Set as the selector domain we query the result from the whole node tree workspace
     */

    public List evaluateChildNode(List set, Constraint constraint) throws RepositoryException {  //mine

        List result = new ArrayList();
        String path = ((RegistryChildNode) constraint).getParentPath();
//        String selector = ((RegistryChildNode) constraint).getSelectorName();

        try {
            Iterator it = set.iterator();
            RegistryNode node = null;
            while (it.hasNext()) {
                node = (RegistryNode) it.next();
                if (node.getPath().contains(path)) {

                    result.add(node);
                }

            }

        } catch (RepositoryException e) {

            e.printStackTrace();
        }

        return getfilteredFinalResult(set, result);
    }

    public List evaluateDescendantNode(List set, Constraint constraint) throws RepositoryException {  //mine
        //Done
        List result = new ArrayList();

        String path = ((RegistryDescendantNode) constraint).getAncestorPath();
//        String selector = ((RegistryDescendantNode) constraint).getSelectorName();   // The "set" has all selector nodes that "selectorname" has

        try {
            RegistryNode node = null;
            Iterator it = set.iterator();

            while (it.hasNext()) {

                node = ((RegistryNode) it.next());

                if (node.getPath().contains(path)) {
                    result.add(node);
                }

            }

        } catch (RepositoryException e) {

            e.printStackTrace();
        }

        return getfilteredFinalResult(set, result);

    }

    public List evaluateFullTextSearch(List set, Constraint constraint) {  //mine
        // TODO
        return null;
    }

    public List evaluateOr(List set, Constraint constraint) throws RepositoryException {  //mine


        List orResult;
        List result1;
        List result2;

        Constraint constraint1 = ((RegistryOr) constraint).getConstraint1();
        Constraint constraint2 = ((RegistryOr) constraint).getConstraint2();

        if (constraint1 instanceof Or) {

            result1 = evaluateOr(set, constraint1);
        } else {

            result1 = executeQuery(set, constraint1);
        }

        if (constraint2 instanceof Or) {

            result2 = evaluateOr(set, constraint2);
        } else {

            result2 = executeQuery(set, constraint2);
        }


        Iterator it1 = result1.iterator();
        Iterator it2 = result2.iterator();
        RegistryNode node1;
        RegistryNode node2;
        Iterator it;

        while (it1.hasNext()) {
            node1 = (RegistryNode) it1.next();

            while (it2.hasNext()) {

                node2 = (RegistryNode) it2.next();

                if (node1.getPath().equals(node2.getPath())) {

                    result2.remove(node2);
                    it2 = result2.iterator();

                }

            }
        }

        result1.addAll(result2);
        orResult = result1;

        return getfilteredFinalResult(set, orResult);
    }

    public List evaluatePropertyExistence(List set, Constraint constraint) throws RepositoryException {  //mine

        List result = new ArrayList();
//        String selector = ((RegistryPropertyExistence) constraint).getSelectorName();
        String propName = ((RegistryPropertyExistence) constraint).getPropertyName();

        String query = "SELECT R.REG_PATH_ID, R.REG_NAME FROM REG_RESOURCE R, REG_PROPERTY PP, " +
                "REG_RESOURCE_PROPERTY RP WHERE R.REG_PATH_ID=RP.REG_PATH_ID" +
                "AND R.REG_NAME IS NULL AND RP.REG_RESOURCE_NAME IS NULL " +
                "AND RP.REG_PROPERTY_ID=PP.REG_ID AND PP.REG_NAME=" + propName;

        statement = "org.wso2.registry.direct.query;;" + query;

        Iterator it = set.iterator();
        RegistryNode node = null;
        while (it.hasNext()) {
            node = (RegistryNode) it.next();
            if (node.hasProperty(propName)) {
                result.add(node);
            }
        }

        return getfilteredFinalResult(set, result);
    }

    public List evaluateSameNode(List set, Constraint constraint) throws RepositoryException {  //mine

        RegistryNode node = null;
        List result = new ArrayList();
//        String selector = ((RegistrySameNode) constraint).getSelectorName();
        String path = ((RegistrySameNode) constraint).getPath();

        try {

            node = (RegistryNode) session.getNode(path);
            result.add(node);
//            statement ="org.wso2.registry.direct.query;;SELECT R.REG_PATH_ID, R.REG_NAME FROM REG_RESOURCE R, REG_PATH PT WHERE PT.REG_PATH_VALUE ='"+path+"' AND R.REG_PATH_ID=PT.REG_PATH_ID AND R.REG_RESOURCE_NAME IS NULL";
            statement = "org.wso2.registry.direct.query;;SELECT R.REG_PATH_ID, R.REG_NAME FROM REG_RESOURCE R, REG_PATH PT WHERE PT.REG_PATH_VALUE ='" + path + "' AND R.REG_PATH_ID=PT.REG_PATH_ID AND R.REG_NAME IS NULL";

        } catch (RepositoryException e) {

        }

        return getfilteredFinalResult(set, result);
    }

    private List getfilteredFinalResult(List source, List result) throws RepositoryException {

        List finalResult = new ArrayList();
        Iterator it1 = source.iterator();

        while (it1.hasNext()) {

            RegistryNode temp1 = (RegistryNode) it1.next();
            Iterator it2 = result.iterator();

            while (it2.hasNext()) {
                RegistryNode temp2 = (RegistryNode) it2.next();

                if ((temp1.getPath()).equals(temp2.getPath())) {
                    finalResult.add(temp1);
                }
            }
        }

        return finalResult;
    }

    private List executeLocalQuery(String sql) throws RepositoryException {

        Registry registry = session.getUserRegistry();
        Resource q1 = null;
        List nodes = new ArrayList();

        try {
//these modifications are done so the queries are not stored in the registry

//            q1 = registry.newResource();
//
//            q1.setContent(sql);
//
//            q1.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);
//
//            q1.addProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME,
//
//                    RegistryConstants.RESOURCES_RESULT_TYPE);
//
//            registry.put(RegistryConstants.CONFIG_REGISTRY_BASE_PATH + RegistryConstants.QUERIES_COLLECTION_PATH + "/custom-queries", q1);
            Map parameters = new HashMap();
            parameters.put("query", sql);
//            Resource result = registry.executeQuery(RegistryConstants.CONFIG_REGISTRY_BASE_PATH + RegistryConstants.QUERIES_COLLECTION_PATH + "/custom-queries", parameters);
            Resource result = registry.executeQuery(null, parameters);

            String[] paths = (String[]) result.getContent();

            for (String path : paths) {

                nodes.add(session.getNode(path));
            }

        } catch (RegistryException e) {
            e.printStackTrace();
        }
        return nodes;
    }

    private List getSourceSet(String ntName) {

        Registry registry = session.getUserRegistry();
        Resource q1 = null;
        String sql1 = "SELECT REG_PATH_ID, REG_NAME FROM REG_RESOURCE WHERE REG_DESCRIPTION ='" + ntName + "'";
        List nodes = new ArrayList();
        try {
//these modifications are done so the queries are not stored in the registry

/*
            q1 = registry.newResource();

            q1.setContent(sql1);

            q1.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);

            q1.addProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME,

                    RegistryConstants.RESOURCES_RESULT_TYPE);

            registry.put(RegistryConstants.CONFIG_REGISTRY_BASE_PATH + RegistryConstants.QUERIES_COLLECTION_PATH + "/custom-queries", q1);
*/
            Map parameters = new HashMap();
            parameters.put("query", sql1);
//            Resource result = registry.executeQuery(RegistryConstants.CONFIG_REGISTRY_BASE_PATH + RegistryConstants.QUERIES_COLLECTION_PATH + "/custom-queries", parameters);
            Resource result = registry.executeQuery(null, parameters);

            String[] paths = (String[]) result.getContent();

            for (String path : paths) {

                nodes.add(session.getNode(path));
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
        return nodes;

    }


    /**
     * @return
     * @throws InvalidQueryException
     * @throws RepositoryException   Constraints - And,Not,Comparison, ChildNode, , DescendantNode, FullTextSearch, Or, PropertyExistence, SameNode
     */

    public QueryResult execute() throws InvalidQueryException, RepositoryException {

        QueryResult queryResult = null;

        if (source instanceof Selector) {   // we do not support outer joins
            sourceSet = sourceSelector(source);


        } else if (source instanceof Join) {   // we do not support outer joins yet
            sourceSet = sourceJoin(source);

        }

        List result = executeQuery(sourceSet, getConstraint());
        queryResult = new RegistryQueryResult(result, session);

        return queryResult;
    }

    private List sourceSelector(Source source) {
        String ntName = ((RegistrySelector) source).getNodeTypeName();
        return getSourceSet(ntName);

    }

    private List sourceJoin(Source source) {

        List joinSourceSet = new ArrayList();
//        Source left = ((RegistryJoin) source).getLeft();
//        Source right = ((RegistryJoin) source).getRight();
//        Set leftSet;
//        Set rightSet;
//
//        if (left instanceof Join) {
//
//            leftSet = sourceJoin(left);
//
//        } else if (left instanceof Selector) {
//
//            leftSet = sourceSelector(left);
//
//        }
//
//        if (right instanceof Join) {
//
//            rightSet = sourceJoin(right);
//
//        } else if (left instanceof Selector) {
//
//            rightSet = sourceSelector(right);
//
//        }

        /**
         * Here consider the join type and then consider join condition to get the source set
         */

        return joinSourceSet;
    }

    public List executeQuery(List sourceSet, Constraint constraint) throws RepositoryException { //mine

        List set = null;

        if (constraint instanceof And) {

            set = evaluateAnd(sourceSet, constraint);
        } else if (constraint instanceof Not) {

            set = evaluateNot(sourceSet, constraint);
        } else if (constraint instanceof Comparison) {

            set = evaluateComparison(sourceSet, constraint);
        } else if (constraint instanceof ChildNode) {

            set = evaluateChildNode(sourceSet, constraint);
        } else if (constraint instanceof DescendantNode) {

            set = evaluateDescendantNode(sourceSet, constraint);
        } else if (constraint instanceof FullTextSearch) {

            set = evaluateFullTextSearch(sourceSet, constraint);
        } else if (constraint instanceof Or) {

            set = evaluateOr(sourceSet, constraint);
        } else if (constraint instanceof PropertyExistence) {

            set = evaluatePropertyExistence(sourceSet, constraint);
        } else if (constraint instanceof SameNode) {

            set = evaluateSameNode(sourceSet, constraint);
        }

        return set;
    }

    public void setLimit(long l) {

    }

    public void setOffset(long l) {

    }

    public String getStatement() {

        return statement;
    }

    public String getLanguage() {

        return null;
    }

    public String getStoredQueryPath() throws ItemNotFoundException, RepositoryException {

        return null;
    }

    public Node storeAsNode(String s) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, UnsupportedRepositoryOperationException, RepositoryException {

        return null;
    }

    public void bindValue(String s, Value value) throws IllegalArgumentException, RepositoryException {


    }

    public String[] getBindVariableNames() throws RepositoryException {

        return new String[0];

    }

}
