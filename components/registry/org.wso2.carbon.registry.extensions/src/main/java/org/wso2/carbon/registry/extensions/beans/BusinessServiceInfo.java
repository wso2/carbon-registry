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
package org.wso2.carbon.registry.extensions.beans;

import com.ibm.wsdl.ServiceImpl;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.registry.extensions.handlers.utils.WSDLInfo;

import java.util.ArrayList;

@SuppressWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class BusinessServiceInfo {

    private String serviceName;

    private String serviceNamespace;

    private String serviceDescription;

    private OMElement[] serviceEndpoints;

    private ArrayList<ServiceDocumentsBean> documents;

    private WSDLInfo serviceWSDLInfo;

    private ServiceImpl service;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceNamespace() {
        return serviceNamespace;
    }

    public void setServiceNamespace(String serviceNamespace) {
        this.serviceNamespace = serviceNamespace;
    }

    public String getServiceDescription() {
        return serviceDescription;
    }

    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    public OMElement[] getServiceEndpoints() {
        return serviceEndpoints;
    }

    public void setServiceEndpoints(OMElement[] serviceEndpoints) {
        this.serviceEndpoints = serviceEndpoints;
    }

    public ArrayList<ServiceDocumentsBean> getDocuments() {
        return documents;
    }

    public void setDocuments(ArrayList<ServiceDocumentsBean> documents) {
        this.documents = documents;
    }

    public WSDLInfo getServiceWSDLInfo() {
        return serviceWSDLInfo;
    }

    public void setServiceWSDLInfo(WSDLInfo serviceWSDLInfo) {
        this.serviceWSDLInfo = serviceWSDLInfo;
    }

    public ServiceImpl getService() {
        return service;
    }

    public void setService(ServiceImpl service) {
        this.service = service;
    }
}