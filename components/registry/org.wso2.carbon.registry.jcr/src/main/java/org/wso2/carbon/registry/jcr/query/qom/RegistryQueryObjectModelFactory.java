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

import org.wso2.carbon.registry.jcr.RegistrySession;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.qom.*;

public class RegistryQueryObjectModelFactory implements QueryObjectModelFactory {

    RegistrySession session;

    public RegistryQueryObjectModelFactory(RegistrySession session) {

        this.session = session;

    }

    public QueryObjectModel createQuery(Source source, Constraint constraint, Ordering[] orderings, Column[] columns) throws InvalidQueryException, RepositoryException {

        return new RegistryQueryObjectModel(source, constraint, orderings, columns, session);
    }

    public Selector selector(String s, String s1) throws InvalidQueryException, RepositoryException {

        return new RegistrySelector(s, s1);
    }

    public Join join(Source source, Source source1, String s, JoinCondition joinCondition) throws InvalidQueryException, RepositoryException {
        return new RegistryJoin(source, source1, s, joinCondition);
    }

    public EquiJoinCondition equiJoinCondition(String s, String s1, String s2, String s3) throws InvalidQueryException, RepositoryException {

        return new RegistryEquiJoinCondition(s, s1, s2, s3);
    }

    public SameNodeJoinCondition sameNodeJoinCondition(String s, String s1, String s2) throws InvalidQueryException, RepositoryException {

        return new RegistrySameNodeJoinCondition(s, s1, s2);
    }

    public ChildNodeJoinCondition childNodeJoinCondition(String s, String s1) throws InvalidQueryException, RepositoryException {

        return new RegistryChildNodeJoinCondition(s, s1);
    }

    public DescendantNodeJoinCondition descendantNodeJoinCondition(String s, String s1) throws InvalidQueryException, RepositoryException {
        return new RegistryDescendantNodeJoinCondition(s, s1);
    }

    public And and(Constraint constraint, Constraint constraint1) throws InvalidQueryException, RepositoryException {
        return new RegistryAnd(constraint, constraint1);
    }

    public Or or(Constraint constraint, Constraint constraint1) throws InvalidQueryException, RepositoryException {
        return new RegistryOr(constraint, constraint1);
    }

    public Not not(Constraint constraint) throws InvalidQueryException, RepositoryException {

        return new RegistryNot(constraint);
    }

    public Comparison comparison(DynamicOperand dynamicOperand, String s, StaticOperand staticOperand) throws InvalidQueryException, RepositoryException {
        return new RegistryComparison(dynamicOperand, s, staticOperand);
    }

    public PropertyExistence propertyExistence(String s, String s1) throws InvalidQueryException, RepositoryException {

        return new RegistryPropertyExistence(s, s1);
    }

    public FullTextSearch fullTextSearch(String s, String s1, StaticOperand staticOperand) throws InvalidQueryException, RepositoryException {

        return new RegistryFullTextSearch(s, s1, staticOperand);
    }

    public SameNode sameNode(String s, String s1) throws InvalidQueryException, RepositoryException {

        return new RegistrySameNode(s, s1);
    }

    public ChildNode childNode(String s, String s1) throws InvalidQueryException, RepositoryException {

        return new RegistryChildNode(s, s1);
    }

    public DescendantNode descendantNode(String s, String s1) throws InvalidQueryException, RepositoryException {

        return new RegistryDescendantNode(s, s1);
    }

    public PropertyValue propertyValue(String s, String s1) throws InvalidQueryException, RepositoryException {

        return new RegistryPropertyValue(s, s1);
    }

    public Length length(PropertyValue propertyValue) throws InvalidQueryException, RepositoryException {

        return new RegistryLength(propertyValue);
    }

    public NodeName nodeName(String s) throws InvalidQueryException, RepositoryException {

        return new RegistryNodeName(s);
    }

    public NodeLocalName nodeLocalName(String s) throws InvalidQueryException, RepositoryException {

        return new RegistryNodeLocalName(s);
    }

    public FullTextSearchScore fullTextSearchScore(String s) throws InvalidQueryException, RepositoryException {

        return new RegistryFullTextSearchScore(s);
    }

    public LowerCase lowerCase(DynamicOperand dynamicOperand) throws InvalidQueryException, RepositoryException {

        return new RegistryLowerCase(dynamicOperand);
    }

    public UpperCase upperCase(DynamicOperand dynamicOperand) throws InvalidQueryException, RepositoryException {

        return new RegistryUpperCase(dynamicOperand);
    }

    public BindVariableValue bindVariable(String s) throws InvalidQueryException, RepositoryException {

        return new RegistryBindVariableValue(s);
    }

    public Literal literal(Value value) throws InvalidQueryException, RepositoryException {

        return new RegistryLiteral(value);
    }

    public Ordering ascending(DynamicOperand dynamicOperand) throws InvalidQueryException, RepositoryException {

        return new RegistryOrdering(dynamicOperand);
    }

    public Ordering descending(DynamicOperand dynamicOperand) throws InvalidQueryException, RepositoryException {

        return new RegistryOrdering(dynamicOperand);
    }

    public Column column(String s, String s1, String s2) throws InvalidQueryException, RepositoryException {

        return new RegistryColumn(s, s1, s2);
    }
}
