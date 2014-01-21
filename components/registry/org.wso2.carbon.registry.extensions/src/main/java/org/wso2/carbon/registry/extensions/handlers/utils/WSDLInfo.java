/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.extensions.handlers.utils;

import javax.wsdl.Definition;
import java.util.ArrayList;
                                                              
public class WSDLInfo {
    private String originalURL;
    private String proposedRegistryURL;
    private Definition wsdlDefinition;
    private ArrayList<String> wsdlDependencies;
    private ArrayList<String> schemaDependencies;
    private ArrayList<String> policyDependencies;
    private WSDLInfo parent;
    private boolean isMasterWSDL = false;
    private boolean isExistPolicyReferences = false;

    public WSDLInfo() {
        wsdlDependencies = new ArrayList<String>();
        schemaDependencies = new ArrayList<String>();
        policyDependencies = new ArrayList<String>();
    }

    public String getOriginalURL() {
        return originalURL;
    }

    public void setOriginalURL(String originalURL) {
        this.originalURL = originalURL;
    }

    public String getProposedRegistryURL() {
        return proposedRegistryURL;
    }

    public void setProposedRegistryURL(String proposedRegistryURL) {
        this.proposedRegistryURL = proposedRegistryURL;
    }

    public Definition getWSDLDefinition() {
        return wsdlDefinition;
    }

    public void setWSDLDefinition(Definition wsdlDefinition) {
        this.wsdlDefinition = wsdlDefinition;
    }

    public ArrayList<String> getWSDLDependencies() {
        return wsdlDependencies;
    }

    public void setWSDLDependencies(ArrayList<String> wsdlDependencies) {
        this.wsdlDependencies = wsdlDependencies;
    }

    public ArrayList<String> getSchemaDependencies() {
        return schemaDependencies;
    }

    public void setSchemaDependencies(ArrayList<String> schemaDependencies) {
        this.schemaDependencies = schemaDependencies;
    }

    public WSDLInfo getParent() {
        return parent;
    }

    public void setParent(WSDLInfo parent) {
        this.parent = parent;
    }

    public boolean isMasterWSDL() {
        return isMasterWSDL;
    }

    public void setMasterWSDL(boolean masterWSDL) {
        isMasterWSDL = masterWSDL;
    }

    public ArrayList<String> getPolicyDependencies() {
        return policyDependencies;
    }

    public void setPolicyDependencies(ArrayList<String> policyDependencies) {
        this.policyDependencies = policyDependencies;
    }

    public boolean isExistPolicyReferences() {
        return isExistPolicyReferences;
    }

    public void setExistPolicyReferences(boolean existPolicyReferences) {
        isExistPolicyReferences = existPolicyReferences;
    }
}
