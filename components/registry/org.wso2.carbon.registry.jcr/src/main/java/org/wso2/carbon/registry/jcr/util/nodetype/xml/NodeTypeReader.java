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

package org.wso2.carbon.registry.jcr.util.nodetype.xml;


import org.apache.axiom.om.OMElement;
import org.jaxen.JaxenException;
import org.wso2.carbon.registry.jcr.RegistryValue;
import org.wso2.carbon.registry.jcr.nodetype.RegistryNodeDefinitionTemplate;
import org.wso2.carbon.registry.jcr.nodetype.RegistryPropertyDefinitionTemplate;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.*;
import javax.jcr.query.qom.QueryObjectModelConstants;
import javax.jcr.version.OnParentVersionAction;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NodeTypeReader {
    NodeTypeManager nodeTypeManager;
    NodeTypeTemplate nodeTypeTemplate;

    public NodeTypeReader(NodeTypeManager nodeTypeManager) {
        this.nodeTypeManager = nodeTypeManager;
    }

    private void buildSupertypes(OMElement omElement) throws ConstraintViolationException {
        Iterator supertypeIt = omElement.getChildrenWithLocalName("supertypes");
        while (supertypeIt.hasNext()) {   //one time process
            OMElement supertypes = (OMElement) supertypeIt.next();
            Iterator it1 = supertypes.getChildrenWithLocalName("supertype");
            List<String> superTypeList = new ArrayList<String>();
            while (it1.hasNext()) {
                superTypeList.add(((OMElement) it1.next()).getText()); //super types
            }
            nodeTypeTemplate.setDeclaredSuperTypeNames(superTypeList.toArray(new String[0]));
        }

    }

    private void buildChildNodeDefs(OMElement omElement) throws RepositoryException {

        Iterator cndIt = omElement.getChildrenWithLocalName("childNodeDefinition");
        while (cndIt.hasNext()) {
            NodeDefinitionTemplate nodeDefinitionTemplate = nodeTypeManager.createNodeDefinitionTemplate();
            OMElement childDefs = (OMElement) cndIt.next();

            nodeDefinitionTemplate.setName(getAttrValue("name", childDefs));    // TODO should validate *

            nodeDefinitionTemplate.setAutoCreated(Boolean.valueOf(getAttrValue("autoCreated", childDefs)));
            nodeDefinitionTemplate.setMandatory(Boolean.valueOf(getAttrValue("mandatory", childDefs)));
            nodeDefinitionTemplate.setProtected(Boolean.valueOf(getAttrValue("protected", childDefs)));
            nodeDefinitionTemplate.setOnParentVersion(OnParentVersionAction.valueFromName(getAttrValue("onParentVersion", childDefs)));
            nodeDefinitionTemplate.setSameNameSiblings(Boolean.valueOf(getAttrValue("sameNameSiblings", childDefs)));
            String dpt = getAttrValue("defaultPrimaryType", childDefs); // check value length >0
            if (dpt != null && dpt.length() > 0) {
                nodeDefinitionTemplate.setDefaultPrimaryTypeName(dpt);
            }

            Iterator reqPriTypesIt = childDefs.getChildrenWithLocalName("requiredPrimaryTypes");
            while (reqPriTypesIt.hasNext()) {   //one time
                OMElement supertypes = (OMElement) reqPriTypesIt.next();
                Iterator supertype = supertypes.getChildrenWithLocalName("requiredPrimaryType");
                List<String> rptList = new ArrayList<String>();
                while (supertype.hasNext()) {
                    rptList.add(((OMElement) supertype.next()).getText()); // requiredPrimaryType
                }
                nodeDefinitionTemplate.setRequiredPrimaryTypeNames(rptList.toArray(new String[0]));
            }

            ((RegistryNodeDefinitionTemplate) nodeDefinitionTemplate).
                    setDeclaringNodeTypeName(nodeTypeTemplate.getName());
            nodeTypeTemplate.getNodeDefinitionTemplates().add(nodeDefinitionTemplate);
        }


    }


    private String getAttrValue(String key, OMElement omElement) {
        if ((omElement.getAttribute(new QName(key)) != null) && !"".equals(omElement.getAttribute(new QName(key)).getAttributeValue())) {
            return omElement.getAttribute(new QName(key)).getAttributeValue();
        } else {
            return null;
        }
    }

    private void buildPropertyDefs(OMElement omElement) throws RepositoryException {

        Iterator iterator = omElement.getChildrenWithName(new QName("propertyDefinition"));
        while (iterator.hasNext()) {
            PropertyDefinitionTemplate propertyDefinitionTemplate = nodeTypeManager.createPropertyDefinitionTemplate();
            OMElement child = (OMElement) iterator.next();

            propertyDefinitionTemplate.setName(
                    getAttrValue("name", child));
            propertyDefinitionTemplate.setAutoCreated(Boolean.valueOf(
                    getAttrValue("autoCreated", child)));
            propertyDefinitionTemplate.setMandatory(Boolean.valueOf(
                    getAttrValue("mandatory", child)));
            propertyDefinitionTemplate.setProtected(Boolean.valueOf(
                    getAttrValue("protected", child)));
            propertyDefinitionTemplate.setOnParentVersion(OnParentVersionAction.
                    valueFromName(getAttrValue("onParentVersion", child)));
            propertyDefinitionTemplate.setMultiple(Boolean.valueOf(
                    getAttrValue("multiple", child)));
            propertyDefinitionTemplate.setFullTextSearchable(Boolean.valueOf(
                    getAttrValue("isFullTextSearchable", child)));
            propertyDefinitionTemplate.setQueryOrderable(Boolean.valueOf(
                    getAttrValue("isQueryOrderable", child)));

            String aqop = getAttrValue("availableQueryOperators", child);

            if (aqop != null && aqop.length() > 0) {
                String[] ops = aqop.split(" ");
                List<String> queryOpt = new ArrayList<String>();
                for (String op1 : ops) {
                    String opt = op1.trim();
                    if (opt.equals(Constants.EQ_ENTITY)) {
                        queryOpt.add(QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO);
                    } else if (opt.equals(Constants.NE_ENTITY)) {
                        queryOpt.add(QueryObjectModelConstants.JCR_OPERATOR_NOT_EQUAL_TO);
                    } else if (opt.equals(Constants.LT_ENTITY)) {
                        queryOpt.add(QueryObjectModelConstants.JCR_OPERATOR_LESS_THAN);
                    } else if (opt.equals(Constants.LE_ENTITY)) {
                        queryOpt.add(QueryObjectModelConstants.JCR_OPERATOR_LESS_THAN_OR_EQUAL_TO);
                    } else if (opt.equals(Constants.GT_ENTITY)) {
                        queryOpt.add(QueryObjectModelConstants.JCR_OPERATOR_GREATER_THAN);
                    } else if (opt.equals(Constants.GE_ENTITY)) {
                        queryOpt.add(QueryObjectModelConstants.JCR_OPERATOR_GREATER_THAN_OR_EQUAL_TO);
                    } else if (opt.equals(Constants.LIKE_ENTITY)) {
                        queryOpt.add(QueryObjectModelConstants.JCR_OPERATOR_LIKE);
                    } else {
                        throw new NoSuchNodeTypeException("Not a valid query operator" + op1);
                    }
                }
                propertyDefinitionTemplate.setAvailableQueryOperators(
                        queryOpt.toArray(new String[0]));
            }


            propertyDefinitionTemplate.setRequiredType(PropertyType.valueFromName(
                    getAttrValue("requiredType", child)));
            String valueConstraints = getAttrValue("valueConstraints", child);
            if (valueConstraints != null) {   // Values should put inside []
                String tmp = valueConstraints.substring(1, valueConstraints.length() - 1);
                String s[] = tmp.split(",");
                propertyDefinitionTemplate.setValueConstraints(s);
            }

            String defaultValues = getAttrValue("defaultValues", child);
            if ((defaultValues != null) && (!"".equals(defaultValues))) {   // should put inside []
                String tmp = defaultValues.substring(1, defaultValues.length() - 1);
                String s[] = tmp.split(",");


                Value[] values = new Value[s.length];

                for (int i = 0; i < s.length; i++) {   // Creates an array of Value instances
                    values[i] = new RegistryValue(s[i]);
                }
                propertyDefinitionTemplate.setDefaultValues(values);
            }

            ((RegistryPropertyDefinitionTemplate) propertyDefinitionTemplate).
                    setDeclaringNodeTypeName(nodeTypeTemplate.getName());
//            TODO support following additionally
//            <valueConstraints> <valueConstraint/> </valueConstraints>
//            <defaultValues> <defaultValues/> </defaultValues>
            nodeTypeTemplate.getPropertyDefinitionTemplates().add(propertyDefinitionTemplate);
        }
    }


    public NodeTypeTemplate buildNodeType(OMElement omElement) throws JaxenException, RepositoryException {

        nodeTypeTemplate = nodeTypeManager.createNodeTypeTemplate();

        nodeTypeTemplate.setName(getAttrValue("name", omElement));
        nodeTypeTemplate.setMixin(Boolean.valueOf(getAttrValue("isMixin", omElement)));
        nodeTypeTemplate.setOrderableChildNodes(Boolean.valueOf(getAttrValue("hasOrderableChildNodes", omElement)));
        nodeTypeTemplate.setAbstract(Boolean.valueOf(getAttrValue("isAbstract", omElement)));
        nodeTypeTemplate.setQueryable(Boolean.valueOf(getAttrValue("isQueryable", omElement)));
        nodeTypeTemplate.setPrimaryItemName(getAttrValue("primaryItemName", omElement));

        buildSupertypes(omElement);

        buildChildNodeDefs(omElement);
        buildPropertyDefs(omElement);
//        System.out.println(omElement.toString());

        return nodeTypeTemplate;

    }


}
