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
import org.wso2.carbon.registry.jcr.util.RegistryJCRItemOperationUtil;
import org.wso2.carbon.registry.jcr.util.RegistryJCRSpecificStandardLoderUtil;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeDefinitionTemplate;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RegistryNodeDefinitionTemplate implements NodeDefinitionTemplate {

    private String name = null;
    private String[] reqPrimTypeNames = null;
    private String deftPrimTypeName = null;

    private boolean isAutoCreated = false;
    private boolean isMandatory = false;
    private boolean isProtected = false;
    private boolean allowSameNameSib = false;
    private NodeTypeManager nodeTypeManager;

    private int onParVersion = 1;
    private String declaringNT;
    private static Log log = LogFactory.getLog(RegistryPropertyDefinitionTemplate.class);

    public RegistryNodeDefinitionTemplate(NodeTypeManager nodeTypeManager) {
        this.nodeTypeManager = nodeTypeManager;
    }

    public void setName(String s) throws ConstraintViolationException {
        if ((s == null) || !RegistryJCRSpecificStandardLoderUtil.isValidJCRName(s)) {
            throw new ConstraintViolationException("Not a valid JCR node type name");
        }
        if ((s != null) && s.contains("{")) {
            s = RegistryJCRItemOperationUtil.replaceNameSpacePrefixURIS(s);
        }

        this.name = s;

    }

    public void setAutoCreated(boolean b) {
        this.isAutoCreated = b;
    }

    public void setMandatory(boolean b) {
        this.isMandatory = b;
    }

    public void setOnParentVersion(int i) {

        this.onParVersion = i;
    }

    public void setProtected(boolean b) {
        this.isProtected = b;
    }

    public void setRequiredPrimaryTypeNames(String[] strings) throws ConstraintViolationException {

        if (strings == null) {
            throw new ConstraintViolationException("Null is not a valid JCR name");
        }
        for (int i = 0; i < strings.length; i++) {
            if (!RegistryJCRSpecificStandardLoderUtil.isValidJCRName(strings[i])) {
                throw new ConstraintViolationException("Invalid JCR super type type name");
            }

            if (strings[i].contains("{")) {
                strings[i] = RegistryJCRItemOperationUtil.replaceNameSpacePrefixURIS(strings[i]);
            }
        }
        this.reqPrimTypeNames = Arrays.copyOf(strings, strings.length);
    }

    public void setDefaultPrimaryTypeName(String s) throws ConstraintViolationException {

        if (!RegistryJCRSpecificStandardLoderUtil.isValidJCRName(s)) {
            throw new ConstraintViolationException("Invalid node type " + s);
        }
        if ((s != null) && s.contains("{")) {
            s = RegistryJCRItemOperationUtil.replaceNameSpacePrefixURIS(s);
        }

        this.deftPrimTypeName = s;
    }

    public void setSameNameSiblings(boolean b) {

        this.allowSameNameSib = b;
    }

    public NodeType[] getRequiredPrimaryTypes() {
        if (reqPrimTypeNames == null) {
            return null;
        }

        List<NodeType> nodeTypeList = new ArrayList<NodeType>();
        for (String s : getRequiredPrimaryTypeNames()) {
            if (s != null) {
                try {
                    nodeTypeList.add(nodeTypeManager.getNodeType(s));
                } catch (RepositoryException e) {
                    e.printStackTrace();
                    //LOG ERROR
                }
            }
        }
        return nodeTypeList.toArray(new NodeType[0]);
    }

    public String[] getRequiredPrimaryTypeNames() {

        if (reqPrimTypeNames != null) {

            return Arrays.copyOf(reqPrimTypeNames, reqPrimTypeNames.length);
        } else {

            return null;
        }
    }

    public NodeType getDefaultPrimaryType() {

        try {
            if (deftPrimTypeName != null) {
                return nodeTypeManager.getNodeType(deftPrimTypeName);
            }
        } catch (RepositoryException e) {
            //LOG ERROR
        }
        return null;
    }

    public String getDefaultPrimaryTypeName() {

        return deftPrimTypeName;
    }

    public boolean allowsSameNameSiblings() {

        return allowSameNameSib;
    }

    public NodeType getDeclaringNodeType() {
        NodeType nodeType = null;
        try {
            nodeType = nodeTypeManager.getNodeType(declaringNT);
        } catch (RepositoryException e) {
            log.error("Error occurred while getting declared node type : " + declaringNT);
        }
        return nodeType;
    }

    public String getName() {
        //TODO need a if to check the name =="*"
        return name;
    }

    public boolean isAutoCreated() {

        return isAutoCreated;
    }

    public boolean isMandatory() {

        return isMandatory;
    }

    public int getOnParentVersion() {

        return onParVersion;
    }

    public boolean isProtected() {

        return isProtected;
    }

    // nt - Custom non jcr setter and getter for give the declared node type  awareness to the prop def
    public void setDeclaringNodeTypeName(String nt) {
        this.declaringNT = nt;
    }
}


