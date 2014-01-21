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
import javax.jcr.Value;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.PropertyDefinitionTemplate;
import java.util.Arrays;

public class RegistryPropertyDefinitionTemplate implements PropertyDefinitionTemplate {


    private String name = null;
    private boolean isAutoCreated = false;
    private boolean isMandatory = false;
    private boolean isProtected = false;
    private boolean isMultiple = false;
    private boolean isFullTxtSearchable = false;
    private boolean isQueryOrderable = false;

    private int onParVersion = 1;
    private int reqType = 1;
    private String[] valConstraints = null;
    private String[] availableQOpr = null;

    private String declaringNT = null;
    private Value[] deftValues = null;
    private NodeTypeManager nodeTypeManager;
    private static Log log = LogFactory.getLog(RegistryPropertyDefinitionTemplate.class);


    public RegistryPropertyDefinitionTemplate(NodeTypeManager ntm) {
        this.nodeTypeManager = ntm;
    }

    public void setName(String s) throws ConstraintViolationException {

        // Null check for the property name
        if ((s == null) || !(RegistryJCRSpecificStandardLoderUtil.isValidJCRName(s))) {
            throw new ConstraintViolationException("Null is not a valid property name in JCR");
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

    public void setRequiredType(int i) {
        this.reqType = i;
    }

    public void setValueConstraints(String[] strings) {
        if (strings != null) {
            this.valConstraints = Arrays.copyOf(strings, strings.length);
        } else {
            this.valConstraints = null;
        }
    }

    public void setDefaultValues(Value[] values) {
        if (values != null) {
            this.deftValues = Arrays.copyOf(values, values.length);
        } else {
            this.deftValues = null;
        }
    }

    public void setMultiple(boolean b) {
        this.isMultiple = b;
    }

    public void setAvailableQueryOperators(String[] strings) {
        if (strings != null) {
            this.availableQOpr = Arrays.copyOf(strings, strings.length);
        } else {
            this.availableQOpr = null;
        }
    }

    public void setFullTextSearchable(boolean b) {
        this.isFullTxtSearchable = b;
    }

    public void setQueryOrderable(boolean b) {
        this.isQueryOrderable = b;
    }

    public int getRequiredType() {
        return reqType;
    }

    public String[] getValueConstraints() {

        if (valConstraints != null) {
            return Arrays.copyOf(valConstraints, valConstraints.length);
        } else {
            return null;
        }
    }

    public Value[] getDefaultValues() {

        if (deftValues != null) {
            return Arrays.copyOf(deftValues, deftValues.length);
        } else {
            return null;
        }

    }

    public boolean isMultiple() {
        return isMultiple;
    }

    public String[] getAvailableQueryOperators() {

        if (availableQOpr != null) {
            return Arrays.copyOf(availableQOpr, availableQOpr.length);
        } else {
            return null;
        }
    }

    public boolean isFullTextSearchable() {
        return isFullTxtSearchable;
    }

    public boolean isQueryOrderable() {
        return isQueryOrderable;
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
