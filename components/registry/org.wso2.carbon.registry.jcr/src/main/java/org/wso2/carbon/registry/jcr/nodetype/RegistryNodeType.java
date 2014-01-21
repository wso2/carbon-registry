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

package org.wso2.carbon.registry.jcr.nodetype;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.jcr.util.RegistryNodeTypeUtil;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class RegistryNodeType implements NodeType {

    private NodeTypeTemplate ntd;
    private NodeTypeManager nodeTypeManager;
    private static Log log = LogFactory.getLog(RegistryNodeType.class);
    public static final String UNDEFINED_NODE_NAME = "*";


    public RegistryNodeType(NodeTypeDefinition ntd, NodeTypeManager nodeTypeManager) {

        this.ntd = (RegistryNodeTypeTemplate) ntd;
        this.nodeTypeManager = nodeTypeManager;
    }

    public NodeType[] getSupertypes() {
        Set<NodeType> nodeTypeList = new HashSet<NodeType>();
        try {

            for (String ntName : ntd.getDeclaredSupertypeNames()) {
                nodeTypeList.add(nodeTypeManager.getNodeType(ntName));
            }

            // if no super types, at least return nt:base
            if (nodeTypeList.size() == 0 && (!ntd.isMixin()) &&
                    !ntd.getName().equals("nt:base")) {
                nodeTypeList.add(nodeTypeManager.getNodeType("nt:base"));
            }

        } catch (RepositoryException e) {
            log.error("Registry Error while getting super types " + e.getMessage());
        }

        return nodeTypeList.toArray(new NodeType[0]);
    }

    public NodeType[] getDeclaredSupertypes() {

        return getSupertypes();        // Assume one level of inheritance
    }

    public NodeTypeIterator getSubtypes() {
        Set<NodeType> nodeTypeList = new HashSet<NodeType>();
        List nttList = ntd.getNodeDefinitionTemplates();

        if (nttList.size() != 0) {
            for (Object ntt : nttList) {
                String ntName = ((NodeDefinition) ntt).getName();
                try {
                    nodeTypeList.add(nodeTypeManager.getNodeType(ntName));
                } catch (RepositoryException e) {
                    log.error("Registry Error while getting super types " + e.getMessage());
                }
            }
        }

        return new RegistryNodeTypeIterator(nodeTypeList);
    }

    public NodeTypeIterator getDeclaredSubtypes() {
        return getSubtypes();   // Assume one level of inheritance
    }

    public boolean isNodeType(String s) {

        if ("nt:base".equals(s)) {
            return true;
        }

        boolean isNodetype = ntd.getName().equals(s); //TODO MUST check bottom-up in a nodetype tree to check isNodetype
        if (!isNodetype) {
            for (String ntsuper : ntd.getDeclaredSupertypeNames()) {
                if (s.equals(ntsuper)) {
                    isNodetype = true;
                    break;
                }
            }
        }
        return isNodetype;
    }


    private void addPrimaryTypePropIfNotExists(PropertyDefinition[] pdfs) throws RepositoryException {

        // check and add primary type as property
        boolean hasPrimaryTypeProp = false;
        for (PropertyDefinition pd : pdfs) {
            if (pd.getName().equals("jcr:primaryType")) {
                hasPrimaryTypeProp = true;
            }
        }

        if (!hasPrimaryTypeProp) {
            ntd.getPropertyDefinitionTemplates().add(RegistryNodeTypeUtil.
                    createJCRPrimaryTypeProperty(nodeTypeManager, ntd.getName()));
        }

    }

    public PropertyDefinition[] getPropertyDefinitions() {
        PropertyDefinition[] pdfs;
        List<PropertyDefinition> pdList = null;
        if (ntd != null) {
            try {
                pdfs = ntd.getDeclaredPropertyDefinitions();
                addPrimaryTypePropIfNotExists(pdfs);
            } catch (RepositoryException e) {
                //             TODO  Log Error
            }
            return ntd.getDeclaredPropertyDefinitions();
        } else {
            return new PropertyDefinition[0];
        }

    }

    public NodeDefinition[] getChildNodeDefinitions() {
        if (ntd != null) {
            return ntd.getDeclaredChildNodeDefinitions();
        } else {
            return new NodeDefinition[0];
        }
    }

    public boolean canSetProperty(String s, Value value) {

        if (value == null) {
            return canRemoveItem(s);
        }

        PropertyDefinition property = getMatchingPropDef(ntd.getDeclaredPropertyDefinitions(), value.getType(), s, false);
        int TYPE = property.getRequiredType();

        if (property.isProtected()) {
            return false;
        }

        switch (TYPE) {

            case PropertyType.BINARY:
                if (validateBinaryPropertyCanSet(value, property)) {
                    return true;
                }
                break;
            case PropertyType.DATE:
                if (validateDatePropertyCanSet(value, property)) {
                    return true;
                }
                break;
            case PropertyType.PATH:
                if (validatePathPropertyCanSet(value, property)) {
                    return true;
                }
                break;
            case PropertyType.STRING:
                return true;
            case PropertyType.BOOLEAN:
                return true;
            case PropertyType.DOUBLE:
                return true;
            case PropertyType.LONG:
                return true;
            case PropertyType.NAME:
                return true;
            default:
                break;

        }

//        if ((pd != null) && (pd.getName().equals(s)) &&
//                (value.getType() == pd.getRequiredType())) {
//            return true;
//        }
        return false;

    }


    private PropertyDefinition getMatchingPropDef(PropertyDefinition[] pdfs, int type, String name, boolean isMultiValued) {

        for (PropertyDefinition pd : pdfs) {
            int reqType = pd.getRequiredType();
            if ((name.equals(pd.getName())
                    && (isMultiValued == pd.isMultiple())

//                    || reqType == PropertyType.UNDEFINED
//                    || type == PropertyType.UNDEFINED)))
            )) {

                return pd;
            }
        }

        return null;
    }


    private boolean validateBinaryPropertyCanSet(Value value, PropertyDefinition pd) {
        if ((value.getType() == PropertyType.STRING
                || value.getType() == PropertyType.BINARY
                || value.getType() == PropertyType.DOUBLE
                || value.getType() == PropertyType.DATE
                || value.getType() == PropertyType.LONG
                || value.getType() == PropertyType.BOOLEAN
                || value.getType() == PropertyType.NAME
                || value.getType() == PropertyType.PATH)) {

            return true;
        }
        return false;
    }

    private boolean isPathInFormat(Value value) {
        //TODO add more special characters to validate a path
        try {
            String path = value.getString();
            if (!"".equals(path)
                    && (path.contains(":")
                    || path.contains(",")
                    || path.contains("%")
                    || path.contains("^")
                    || path.contains("*")
                    || path.contains("(")
                    || path.contains(")")
            )) {
                return false;
            }

        } catch (RepositoryException e) {
            return true;
        }
        return true;
    }

    private boolean isDateInFormat(Value value) {
        try {
            if (value.getString().contains("time=")) {
                return true;
            }
        } catch (RepositoryException e) {
            return false;
        }
        return false;
    }

    //TODO add simple date format chek also
    public boolean isValidDate(String inDate) {

        if (inDate == null)
            return false;

        //set the format to use as a constructor argument
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        if (inDate.trim().length() != dateFormat.toPattern().length())
            return false;

        dateFormat.setLenient(false);

        try {
            //parse the inDate parameter
            dateFormat.parse(inDate.trim());
        } catch (ParseException pe) {
            return false;
        }
        return true;
    }

    private boolean validatePathPropertyCanSet(Value value, PropertyDefinition pd) {

        if (value.getType() == PropertyType.NAME
                || value.getType() == PropertyType.PATH
                ) {
            return true;
        }

        if (value.getType() == PropertyType.DOUBLE
                || value.getType() == PropertyType.BOOLEAN
                || value.getType() == PropertyType.LONG) {
            return false;
        }

        if (value.getType() != PropertyType.PATH) {
            if (isPathInFormat(value)) {
                return true;
            }
        }

        return false;
    }

    private boolean validateDatePropertyCanSet(Value value, PropertyDefinition pd) {

        if (value.getType() == PropertyType.DOUBLE
                || value.getType() == PropertyType.DATE
                || value.getType() == PropertyType.LONG) {

            return true;
        }

        if (value.getType() != PropertyType.DATE) {
            if (isDateInFormat(value)) {
                return true;
            }
        }

        return false;
    }


    private boolean validateBooleanPropertyCanSet(Value value) {
        for (Object o : ntd.getPropertyDefinitionTemplates()) {
            PropertyDefinitionTemplate pd = (PropertyDefinitionTemplate) o;
            if ((pd.getRequiredType() == PropertyType.BINARY) && (
                    value.getType() == PropertyType.STRING
                            || value.getType() == PropertyType.DOUBLE
                            || value.getType() == PropertyType.DATE
                            || value.getType() == PropertyType.LONG
                            || value.getType() == PropertyType.BOOLEAN
                            || value.getType() == PropertyType.NAME
                            || value.getType() == PropertyType.PATH)) {

                return true;
            }
        }
        return false;
    }


    private boolean isPropertyProtected(String propName) {
        for (Object o : ntd.getPropertyDefinitionTemplates()) {
            PropertyDefinitionTemplate pd = (PropertyDefinitionTemplate) o;
            if ((pd != null) && (pd.getName().equals(propName)) && (pd.isProtected())) {
                return true;
            }
        }
        return false;
    }

    private boolean validatePropertyIsMultiple(String propName) {
        for (Object o : ntd.getPropertyDefinitionTemplates()) {
            PropertyDefinitionTemplate pd = (PropertyDefinitionTemplate) o;
            if ((pd != null) && (pd.getName().equals(propName)) && (!pd.isMultiple())) {
                return false;
            }
        }
        return true;
    }

    public boolean canSetProperty(String s, Value[] values) {

        //return false if the property is not multiple
        if (!validatePropertyIsMultiple(s)) {
            return false;
        }
        if (isPropertyProtected(s)) {
            return false;
        }

        for (Value value : values) {
            if (!canSetProperty(s, value)) {
                return false;
            }
        }
        return true;
    }

    public boolean canAddChildNode(String s) {          //TODO

        //return false if the match child def not have a default primary type
        for (NodeDefinition nd : getChildNodeDefinitions()) {
            if (s.equals(nd.getName()) && nd.getDefaultPrimaryTypeName() != null) {
                return true;
            }
        }

        // true if resedual child defs has a non null primary type  for a undefined child name
        if (!isChildNameInDefList(s)) {
            for (NodeDefinition nd : getChildNodeDefinitions()) {
                if (nd.getDefaultPrimaryTypeName() != null) {
                    return true;
                }
            }
        }

        if (!isChildNameInDefList(s)) {
            return false;
        }

        return false;
    }

    public boolean canAddChildNode(String s, String s1) {     //TODO
//        s- ChildNode name
//       s1 = nodeType name

        //Cannot add child for Mixin types
        if (s1.startsWith("mix")) {
            return false;
        }


        if (checkOnlyNodeNameUndefinedRule(s1)) {
            return true;

        }

        if (!isChildNameInDefList(s)) {
            return false;
        }

        for (NodeDefinition nd : getChildNodeDefinitions()) {
            if (nd.getName().equals(s) && (isReqTypesContainsNodeType(nd, s1))) {
                return true;
            }
        }
        return false;
    }

    private boolean checkOnlyNodeNameUndefinedRule(String s1) {
        // if child node name is undefined and still if nodetype of a child node def  matches will return true
        for (NodeDefinition nd : getChildNodeDefinitions()) {
            if (isReqTypesContainsNodeType(nd, s1)) {
                return true;
            }
        }
        return false;
    }

    private boolean isReqTypesContainsNodeType(NodeDefinition nd, String s1) {

        //Also check NTBase Cannot Be Added
        for (String name : nd.getRequiredPrimaryTypeNames()) {
            if ((!"nt:base".equals(s1)) &&
                    ("nt:unstructured".equals(s1) || name.equals(s1))) {
                return true;
            }
        }
        return false;
    }

    private boolean checkNTBaseCannotAdded(String ntUnstr, String name) {
        if (ntUnstr.equals("nt:unstructured") && "nt:base".equals(name)) {
            return true;
        } else {
            return false;
        }

    }

    private boolean isChildNameInDefList(String name) {
        for (NodeDefinition nd : getChildNodeDefinitions()) {
            if (name.equals(nd.getName())) {
                return true;
            }
        }
        return false;
    }

    public boolean canRemoveItem(String s) {
        boolean canRemove = true;

        // check mandatory /protected properties
        for (Object pdt : ntd.getPropertyDefinitionTemplates()) {
            if (((PropertyDefinition) pdt).getName().equals(s)) {
                if (((PropertyDefinition) pdt).isMandatory()
                        || ((PropertyDefinition) pdt).isProtected()) {
                    canRemove = false;
                    return canRemove;
                }
            }
        }

        // check mandatory /protected child items
        for (NodeDefinition nd : ntd.getDeclaredChildNodeDefinitions()) {
            if (nd.getName().equals(s)) {
                if (nd.isMandatory() || nd.isProtected()) {
                    canRemove = false;
                    return canRemove;
                }
            }
        }

        return canRemove;
    }

    public boolean canRemoveNode(String s) {

        boolean canRemoveNd = true;

        Iterator it = ntd.getNodeDefinitionTemplates().iterator();
        NodeDefinition nd = null;

        while (it.hasNext()) {

            nd = (NodeDefinitionTemplate) it.next();

            if ((nd != null) && (nd.getName().equals(s)) &&
                    (nd.isProtected() || nd.isMandatory())) {
                canRemoveNd = false;
                return canRemoveNd;
            }

        }
        return canRemoveNd;

    }

    public boolean canRemoveProperty(String s) {

        boolean canRemovePrp = true;

        Iterator it = ntd.getPropertyDefinitionTemplates().iterator();
        PropertyDefinition pd = null;

        while (it.hasNext()) {
            pd = (PropertyDefinitionTemplate) it.next();
            if (((pd != null) && pd.getName().equals(s) &&
                    (pd.isProtected() || pd.isMandatory()))) {
                canRemovePrp = false;
                return canRemovePrp;
            }
        }
        return canRemovePrp;
    }

    public String getName() {
        return ntd.getName();
    }

    public String[] getDeclaredSupertypeNames() {

        return ntd.getDeclaredSupertypeNames();

    }

    public boolean isAbstract() {

        return ntd.isAbstract();
    }

    public boolean isMixin() {
        return ntd.isMixin();
    }

    public boolean hasOrderableChildNodes() {
        return ntd.hasOrderableChildNodes();
    }

    public boolean isQueryable() {
        return ntd.isQueryable();
    }

    public String getPrimaryItemName() {
        return ntd.getPrimaryItemName();
    }

    public PropertyDefinition[] getDeclaredPropertyDefinitions() {
        return ntd.getDeclaredPropertyDefinitions();
    }

    public NodeDefinition[] getDeclaredChildNodeDefinitions() {

        return ntd.getDeclaredChildNodeDefinitions();
    }

    public NodeTypeTemplate getDefinition() {   // Non JCR method
        return (NodeTypeTemplate) ntd;
    }


}                                         