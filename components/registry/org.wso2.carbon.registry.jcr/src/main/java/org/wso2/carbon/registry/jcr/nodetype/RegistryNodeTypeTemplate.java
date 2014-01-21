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

import org.wso2.carbon.registry.jcr.util.RegistryJCRItemOperationUtil;
import org.wso2.carbon.registry.jcr.util.RegistryJCRSpecificStandardLoderUtil;

import javax.jcr.nodetype.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class RegistryNodeTypeTemplate implements NodeTypeTemplate {

    NodeTypeDefinition ntd = null;

    private String name = null;
    private String[] declSupTpNames = null;
    private String primItmName = null;
    private boolean isAbstract = false;
    private boolean isMixin = false;
    private boolean isOrderbleChildN = false;
    private boolean isQble = false;

    private List<NodeDefinition> nodeDefList = new ArrayList<NodeDefinition>();
    private List<PropertyDefinition> propDefList = new ArrayList<PropertyDefinition>();


    public RegistryNodeTypeTemplate(NodeTypeDefinition ntd) {

        this.ntd = ntd;
        if (ntd != null) {
            setData();
        }

    }

    public RegistryNodeTypeTemplate() {

    }

    private void setData() {
        try {
            loadData();
        } catch (ConstraintViolationException e) {
            e.printStackTrace();
        }
    }

    private void loadData() throws ConstraintViolationException {

        setName(ntd.getName());
        setAbstract(ntd.isAbstract());
        setDeclaredSuperTypeNames(ntd.getDeclaredSupertypeNames());
        setMixin(ntd.isMixin());
        setOrderableChildNodes(ntd.hasOrderableChildNodes());
        setPrimaryItemName(ntd.getPrimaryItemName());
        setQueryable(ntd.isQueryable());

    }

    public void setName(String s) throws ConstraintViolationException {
        if (!RegistryJCRSpecificStandardLoderUtil.isValidJCRName(s)) {
            throw new ConstraintViolationException("Invalid JCR node type name");
        }

        if ((s != null) && s.contains("{")) {
            s = RegistryJCRItemOperationUtil.replaceNameSpacePrefixURIS(s);
        }

        name = s;

    }

    public void setDeclaredSuperTypeNames(String[] strings) throws ConstraintViolationException {
        if (strings == null) {
            throw new ConstraintViolationException("Null is not a valid array in jcr");
        }

        for (String s : strings) {
            if (!RegistryJCRSpecificStandardLoderUtil.isValidJCRName(s)) {
                throw new ConstraintViolationException("Invalid JCR super type type name");
            }
        }
        if (strings != null) { //TODO unnecessary check
            declSupTpNames = Arrays.copyOf(strings, strings.length);
        }
    }

    public void setAbstract(boolean b) {
        isAbstract = b;
    }

    public void setMixin(boolean b) {
        isMixin = b;
    }

    public void setOrderableChildNodes(boolean b) {
        isOrderbleChildN = b;
    }

    public void setPrimaryItemName(String s) throws ConstraintViolationException {
        if (!RegistryJCRSpecificStandardLoderUtil.isValidJCRName(s)) {
            throw new ConstraintViolationException("Invalid JCR super type type name");
        }
        primItmName = s;
    }

    public void setQueryable(boolean b) {
        isQble = b;
    }

    public List getPropertyDefinitionTemplates() {
        return propDefList;
    }

    public List getNodeDefinitionTemplates() {
        return nodeDefList;
    }

    public String getName() {
        return name;
    }

    public String[] getDeclaredSupertypeNames() {
        if (declSupTpNames != null) {
            return Arrays.copyOf(declSupTpNames, declSupTpNames.length);
        } else {
            return new String[]{};
        }

    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public boolean isMixin() {
        return isMixin;
    }

    public boolean hasOrderableChildNodes() {
        return isOrderbleChildN;
    }

    public boolean isQueryable() {
        return isQble;
    }

    public String getPrimaryItemName() {
        return primItmName;
    }

    public PropertyDefinition[] getDeclaredPropertyDefinitions() {

        if (ntd != null) {
            return ntd.getDeclaredPropertyDefinitions();
        } else if (propDefList.size() != 0) {
            return propDefList.toArray(new PropertyDefinition[0]);
        } else {
            return new PropertyDefinition[0];
        }
    }

    public NodeDefinition[] getDeclaredChildNodeDefinitions() {

        if (ntd != null) {
            return ntd.getDeclaredChildNodeDefinitions();
        } else if (nodeDefList.size() != 0) {
            return nodeDefList.toArray(new NodeDefinition[0]);
        } else {
            return new NodeDefinition[0];
        }
    }
}
