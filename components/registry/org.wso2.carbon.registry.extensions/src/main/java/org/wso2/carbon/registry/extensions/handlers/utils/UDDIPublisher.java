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

import com.ibm.wsdl.BindingImpl;
import com.ibm.wsdl.PortImpl;
import com.ibm.wsdl.PortTypeImpl;
import com.ibm.wsdl.ServiceImpl;
import com.ibm.wsdl.extensions.http.HTTPAddressImpl;
import com.ibm.wsdl.extensions.soap.SOAPAddressImpl;
import com.ibm.wsdl.extensions.soap.SOAPBindingImpl;
import com.ibm.wsdl.extensions.soap12.SOAP12AddressImpl;
import com.ibm.wsdl.extensions.soap12.SOAP12BindingImpl;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.OMElementImpl;
import org.apache.axiom.om.impl.llom.OMTextImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.uddi.api_v3.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.extensions.beans.BusinessServiceInfo;
import org.wso2.carbon.registry.extensions.beans.ServiceDocumentsBean;
import org.wso2.carbon.registry.server.service.RegistryAdmin;
import org.wso2.carbon.registry.uddi.utils.UDDIConstants;
import org.wso2.carbon.registry.uddi.utils.UDDIUtil;

import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Service;
import javax.xml.namespace.QName;
import java.util.*;


/**
 * This class is used for publishing services to UDDI registry
 */
public class UDDIPublisher {

    private static final Log log = LogFactory.getLog(UDDIPublisher.class);
    private static final String BUSINESS_NAME = "WSO2";
    private static final String BUSINESS_HOMEPAGE_URL = "www.wso2.com";

    private BusinessServiceInfo businessServiceInfo;
    private HashMap<BindingImpl, TModelDetail> bindingTModelDetailHashMap;
    private HashMap<PortTypeImpl, TModelDetail> portTypeTModelDetailHashMap;

    public UDDIPublisher() {

        bindingTModelDetailHashMap = new HashMap<BindingImpl, TModelDetail>();
        portTypeTModelDetailHashMap = new HashMap<PortTypeImpl, TModelDetail>();
    }

    /**
     * This is used to publish a service into UDDI registry
     */
    public void publishBusinessService(AuthToken authToken, BusinessServiceInfo businessServiceInfo) throws RegistryException {
        this.businessServiceInfo = businessServiceInfo;
        if (log.isDebugEnabled()) {
            log.debug("Publishing business service is started.");
        }
        BusinessService businessService = null;

        try {

            String businessKey = saveBusiness(authToken);
            if (businessKey == null) {
                return;
            }

            WSDLInfo wsdlInfo = businessServiceInfo.getServiceWSDLInfo();
            String serviceName = businessServiceInfo.getServiceName();
            //If true : This is added by  add service UI (since it already has a service name)
            if (wsdlInfo != null && serviceName != null) {
                Map<QName, javax.wsdl.Service> services = getAllServices(wsdlInfo.getWSDLDefinition(), new ArrayList<String>());
                //If WSDL has more than one services we only add the service which has been specified by the user
                if (services.size() > 0) {
                    for (Service service1 : services.values()) {
                        ServiceImpl service = (ServiceImpl) service1;
                        if (serviceName.equals(service.getQName().getLocalPart())) {
                            businessServiceInfo.setService(service);
                            businessService = createBusinessService(businessKey);
                        }
                    }
                }
                UDDIUtil.publishBusinessService(businessService, authToken);
                //If true: WSDL has been added from add WSDL UI
            } else if (wsdlInfo != null) {
                for (Service service1 : getAllServices(wsdlInfo.getWSDLDefinition(), new ArrayList<String>()).values()) {
                    ServiceImpl service = (ServiceImpl) service1;
                    businessServiceInfo.setService(service);
                    businessService = createBusinessService(businessKey);
                    UDDIUtil.publishBusinessService(businessService, authToken);
                }
            } else {
                UDDIUtil.publishBusinessService(businessService, authToken);
            }
        } catch (RegistryException e) {
            throw new RegistryException("Faied to publish service to UDDI repository", e);
        }

        if (log.isDebugEnabled()) {
            log.debug("Business service has been successfully published into UDDI");
        }
    }

    /**
     * This method is an utility method that that handling the cyclic dependent wsdl imports, and wraps javax.wsdl.Definition.getServices.
     * Functions similar to javax.wsdl.Definition.getAllServices(this cannot handle cyclic wsdl imports).
     * @param wsdlDefinition
     * @param traversedDefinitionList
     * @return
     */
    private Map<QName, javax.wsdl.Service> getAllServices(Definition wsdlDefinition, List<String> traversedDefinitionList) {
        final Map<QName, javax.wsdl.Service> servicesMap = new HashMap<QName, javax.wsdl.Service>();
        //return if already processed. this avoids cyclic dependencies.
        if (traversedDefinitionList.contains(wsdlDefinition.getTargetNamespace())) {
            return null;
        }
        traversedDefinitionList.add(wsdlDefinition.getTargetNamespace());
        //process the imports
        for (Object imprtCol : wsdlDefinition.getImports().values()) {
            Vector<Import> importVector = (Vector) imprtCol;
            for (Import importEel : importVector) {
                if (importEel.getDefinition() == null) {
                    continue;
                }
                Map<QName, javax.wsdl.Service> childMap = getAllServices(importEel.getDefinition(), traversedDefinitionList);
                if (childMap != null) {
                    servicesMap.putAll(childMap);
                }
            }
        }
        //process the root node
        if (!wsdlDefinition.getServices().isEmpty()) {
            servicesMap.putAll(wsdlDefinition.getServices());
        }
        return servicesMap;
    }
    /**
     * This method is used to create the businessService object
     *
     * @param businessKey This is used for indicating which businessEntity belongs this service.
     * @return businessService object
     */
    private BusinessService createBusinessService(String businessKey) {
        BusinessService businessService = new BusinessService();
        businessService.setBusinessKey(businessKey);
        ServiceImpl service = businessServiceInfo.getService();

        if (businessServiceInfo.getServiceDescription() != null) {
            //set service description
            Description serviceDesc = new Description();
            serviceDesc.setLang(UDDIConstants.ENGLISH);
            serviceDesc.setValue(businessServiceInfo.getServiceDescription());
            businessService.getDescription().add(serviceDesc);
        }
        CategoryBag categoryBag = new CategoryBag();
        addKeyedReferenceToCategoryBag(categoryBag, "uddi:uddi.org:wsdl:types", "WSDL type",
                "service");
        if (service != null) {
            String serviceName = service.getQName().getLocalPart();
            String serviceNamespace = service.getQName().getNamespaceURI();

            Name name = new Name();
            name.setValue(serviceName);
            businessService.getName().add(name);
            Map<String, PortImpl> ports = service.getPorts();

            if (ports != null) {
                BindingTemplates bindingTemplates = new BindingTemplates();
                for (PortImpl port : ports.values()) {
                    BindingTemplate bindingTemplate = null;
                    List extensibilityElementList = port.getExtensibilityElements();
                    if (extensibilityElementList.size() > 0) {
                        for (Object address : extensibilityElementList) {
                            String className = address.getClass().getSimpleName();

                            if (UDDIConstants.SOAP11_ADDRESS_CLASS.equals(className)) {
                                bindingTemplate = createBindingTemplateForSOAP11(bindingTemplate,
                                        port, address);
                            } else if (UDDIConstants.SOAP12_ADDRESS_CLASS.equals(className)) {
                                bindingTemplate = createBindingTemplateForSOAP12(bindingTemplate, port,
                                        address);
                            } else if (UDDIConstants.HTTP_ADDRESS_CLASS.equals(className)) {
                                bindingTemplate = createBindingTemplateForHTTP(bindingTemplate,
                                        port, address);
                            }
                        }
                        if (bindingTemplate != null) {
                            bindingTemplates.getBindingTemplate().add(bindingTemplate);
                        }
                    }
                }
                businessService.setBindingTemplates(bindingTemplates);
            }
            addKeyedReferenceToCategoryBag(categoryBag, "uddi:uddi.org:xml:namespace", "service namespace",
                    serviceNamespace);
            addKeyedReferenceToCategoryBag(categoryBag, "uddi:uddi.org:xml:localname", "service local name",
                    serviceName);
        }
        businessService.setCategoryBag(categoryBag);
        return businessService;
    }

    /**
     * Create binding template for a WSDL port that has soap:address
     *
     * @param bindingTemplate bindingTemplate reference
     * @param port            WSDL port
     * @param address         Endpoint address
     * @return bindingTemplate object
     */
    private BindingTemplate createBindingTemplateForSOAP11(BindingTemplate bindingTemplate,
                                                           PortImpl port,
                                                           Object address) {
        bindingTemplate = new BindingTemplate();

        SOAPAddressImpl soapAddress = (SOAPAddressImpl) address;
        String endpoint = soapAddress.getLocationURI();
        AccessPoint accessPoint = new AccessPoint();
        accessPoint.setValue(endpoint);
        accessPoint.setUseType(UDDIConstants.END_POINT);
        bindingTemplate.setAccessPoint(accessPoint);

        TModelInstanceDetails tModelinstanceDetails = constructTModelInstanceDetails(port);
        bindingTemplate.setTModelInstanceDetails(tModelinstanceDetails);

        return bindingTemplate;
    }

    /**
     * Create binding template for a WSDL port that has soap12:address
     *
     * @param bindingTemplate bindingTemplate reference
     * @param port            WSDL port
     * @param address         Endpoint address
     * @return bindingTemplate object
     */
    private BindingTemplate createBindingTemplateForSOAP12(BindingTemplate bindingTemplate,
                                                           PortImpl port,
                                                           Object address) {
        bindingTemplate = new BindingTemplate();

        SOAP12AddressImpl soap12Address = (SOAP12AddressImpl) address;
        String endpoint = soap12Address.getLocationURI();
        AccessPoint accessPoint = new AccessPoint();
        accessPoint.setValue(endpoint);
        accessPoint.setUseType(UDDIConstants.END_POINT);
        bindingTemplate.setAccessPoint(accessPoint);

        TModelInstanceDetails tModelinstanceDetails = constructTModelInstanceDetails(port);
        bindingTemplate.setTModelInstanceDetails(tModelinstanceDetails);

        return bindingTemplate;
    }

    /**
     * Create binding template for a WSDL port that has http:address
     *
     * @param bindingTemplate bindingTemplate reference
     * @param port            WSDL port
     * @param address         Endpoint address
     * @return bindingTemplate object
     */
    private BindingTemplate createBindingTemplateForHTTP(BindingTemplate bindingTemplate,
                                                         PortImpl port,
                                                         Object address) {
        bindingTemplate = new BindingTemplate();

        HTTPAddressImpl httpAddress = (HTTPAddressImpl) address;
        String endpoint = httpAddress.getLocationURI();
        AccessPoint accessPoint = new AccessPoint();
        accessPoint.setValue(endpoint);
        accessPoint.setUseType(UDDIConstants.END_POINT);
        bindingTemplate.setAccessPoint(accessPoint);

        TModelInstanceDetails tModelinstanceDetails = constructTModelInstanceDetails(port);

        if(tModelinstanceDetails ==null){
            return null;
        }
        bindingTemplate.setTModelInstanceDetails(tModelinstanceDetails);

        return bindingTemplate;
    }


    /**
     * This method used to create TModelInstanceDetails object
     *
     * @param port WSDL port
     * @return tModelInstanceDetails object for a port
     */
    private TModelInstanceDetails constructTModelInstanceDetails(PortImpl port) {

        TModelInstanceDetails tModelInstanceDetails = new TModelInstanceDetails();
        BindingImpl binding = (BindingImpl) port.getBinding();
        PortTypeImpl portType = (PortTypeImpl) binding.getPortType();

        if (portType == null) {
            log.warn("portType null therefore failed to published to UDDI");
            return null;
        }

        String tModelKey = publishPortTypeTModels(portType, tModelInstanceDetails);
        publishBindingTModel(binding, tModelKey, port.getName(), tModelInstanceDetails);

        if (businessServiceInfo.getDocuments() != null) {
            publishDocumentTModel(tModelInstanceDetails);
        }
        if (businessServiceInfo.getServiceEndpoints() != null) {
            publishEndpointDetails(tModelInstanceDetails);
        }
        return tModelInstanceDetails;
    }

    /**
     * This method is used to publish the portType tModel to the UDDI registry
     *
     * @param portType             WSDL portType
     * @param tModelInstantDetails TModelInstantDetails reference that is used by the bindingTemplate
     * @return tModelKey of the published portType tModel
     */
    private String publishPortTypeTModels(PortTypeImpl portType,
                                          TModelInstanceDetails tModelInstantDetails) {
        TModel tModel = null;
        String tModelKey = null;
        TModelDetail tModelDetail = null;
        //check TModel has already published
        if (!portTypeTModelDetailHashMap.containsKey(portType)) {
            tModel = constructPortTypeTModel(portType);
            try {
                tModelDetail = UDDIUtil.saveTModel(tModel);
                portTypeTModelDetailHashMap.put(portType, tModelDetail);
                if (log.isDebugEnabled()) {
                    log.debug("PortType: " + portType.getQName().getLocalPart() +
                            " TModel has been successfully published to UDDI.");
                }
            } catch (RegistryException e) {
                log.warn("Failed to publish portType: " + portType.getQName().getLocalPart() + " TModel");
            }
        } else {
            tModelDetail = portTypeTModelDetailHashMap.get(portType);
        }
        if (tModelDetail != null) {

            TModelInstanceInfo tModelInstanceInfo = new TModelInstanceInfo();

            Description desc = new Description();
            desc.setValue("The wsdl:portType that this wsdl:port implements.");
            desc.setLang(UDDIConstants.ENGLISH);
            tModelInstanceInfo.getDescription().add(desc);

            tModelKey = tModelDetail.getTModel().iterator().next().getTModelKey();
            tModelInstanceInfo.setTModelKey(tModelKey);
            tModelInstantDetails.getTModelInstanceInfo().add(tModelInstanceInfo);
        }
        return tModelKey;
    }

    /**
     * This is used for creating the portType tModel
     *
     * @param portType WSDL portType
     * @return portType TModel
     */
    private TModel constructPortTypeTModel(PortTypeImpl portType) {
        TModel tModel = new TModel();

        Name portTypeName = new Name();
        portTypeName.setValue(portType.getQName().getLocalPart());
        tModel.setName(portTypeName);

        RegistryAdmin admin = new RegistryAdmin();
        String wsdlUrl = admin.getHTTPPermalink(businessServiceInfo.getServiceWSDLInfo().getProposedRegistryURL());
        /* Remove version info from URL
         * (http://127.0.0.1:9763/registry/resource/_system/governance/wsdls/http/server/BPSServer.wsdl;version:658)
         */
        if (wsdlUrl != null && wsdlUrl.contains(UDDIConstants.REGISTRY_VERSION)) {
            wsdlUrl = wsdlUrl.split(UDDIConstants.REGISTRY_VERSION)[0];
        }

        OverviewDoc overviewDoc = new OverviewDoc();
        OverviewURL overviewUrl = new OverviewURL();
        overviewUrl.setValue(wsdlUrl);
        overviewUrl.setUseType(UDDIConstants.WSDL_INTERFACE);
        overviewDoc.setOverviewURL(overviewUrl);
        tModel.getOverviewDoc().add(overviewDoc);

        CategoryBag categoryBag = new CategoryBag();
        String portTypeNamespace = portType.getQName().getNamespaceURI();

        if (portTypeNamespace != null) {
            addKeyedReferenceToCategoryBag(categoryBag, "uddi:uddi.org:xml:namespace", "portType namespace",
                    portTypeNamespace);
        }
        addKeyedReferenceToCategoryBag(categoryBag, "uddi:uddi.org:wsdl:types", "WSDL type",
                "portType");

        tModel.setCategoryBag(categoryBag);
        return tModel;
    }

    /**
     * This method is used for publishing the binding tModel into UDDI registry.
     *
     * @param binding              WSDL binding
     * @param tModelKey            TModelKey of the portType tModel to which the wsdl:binding relates
     * @param portName             WSDL port local name
     * @param tModelInstantDetails TModelInstanceDetails reference that is used by the bindingTemplate
     */
    private void publishBindingTModel(BindingImpl binding,
                                      String tModelKey,
                                      String portName,
                                      TModelInstanceDetails tModelInstantDetails) {

        TModelDetail tModelDetail = null;
        //check TModel has already published
        if (!bindingTModelDetailHashMap.containsKey(binding)) {
            TModel tModel = constructBindingTModel(binding, tModelKey);
            try {
                tModelDetail = UDDIUtil.saveTModel(tModel);
                bindingTModelDetailHashMap.put(binding, tModelDetail);
                if (log.isDebugEnabled()) {
                    log.debug("Binding: " + binding.getQName().getLocalPart() + " TModel has been successfully published to UDDI.");
                }
            } catch (RegistryException e) {
                log.warn("Failed to publish binding: " + binding.getQName().getLocalPart() + " TModel");
            }
        } else {
            tModelDetail = bindingTModelDetailHashMap.get(binding);
        }
        if (tModelDetail != null) {
            TModelInstanceInfo tModelInstanceInfo = new TModelInstanceInfo();

            Description desc = new Description();
            desc.setValue("The wsdl:binding that this wsdl:port implements. The instanceParms specifies the port local name");
            desc.setLang(UDDIConstants.ENGLISH);
            tModelInstanceInfo.getDescription().add(desc);

            tModelInstanceInfo.setTModelKey(tModelDetail.getTModel().iterator().next().getTModelKey());
            InstanceDetails instanceDetails = new InstanceDetails();
            instanceDetails.setInstanceParms(portName);
            tModelInstanceInfo.setInstanceDetails(instanceDetails);
            tModelInstantDetails.getTModelInstanceInfo().add(tModelInstanceInfo);
        }
    }

    /**
     * This is used to create a binding TModel
     *
     * @param binding   WSDL binding
     * @param tModelKey TModelKey of the portType tModel to which the wsdl:binding relates
     * @return binding TModel
     */
    private TModel constructBindingTModel(BindingImpl binding, String tModelKey) {
        TModel tModel = new TModel();

        Name bindingName = new Name();
        bindingName.setValue(binding.getQName().getLocalPart());
        tModel.setName(bindingName);

        RegistryAdmin admin = new RegistryAdmin();
        String wsdlUrl = admin.getHTTPPermalink(businessServiceInfo.getServiceWSDLInfo().getProposedRegistryURL());
        /* Remove version info from URL
         * (http://127.0.0.1:9763/registry/resource/_system/governance/wsdls/http/server/BPSServer.wsdl;version:658)
         */
        if (wsdlUrl != null && wsdlUrl.contains(UDDIConstants.REGISTRY_VERSION)) {
            wsdlUrl = wsdlUrl.split(UDDIConstants.REGISTRY_VERSION)[0];
        }

        OverviewDoc overviewDoc = new OverviewDoc();
        OverviewURL overviewUrl = new OverviewURL();
        overviewUrl.setValue(wsdlUrl);
        overviewUrl.setUseType(UDDIConstants.WSDL_INTERFACE);
        overviewDoc.setOverviewURL(overviewUrl);
        tModel.getOverviewDoc().add(overviewDoc);


        CategoryBag categoryBag = new CategoryBag();
        String bindingNamespace = binding.getQName().getNamespaceURI();
        if (bindingNamespace != null) {
            addKeyedReferenceToCategoryBag(categoryBag, "uddi:uddi.org:xml:namespace", "binding namespace",
                    bindingNamespace);
        }
        addKeyedReferenceToCategoryBag(categoryBag, "uddi:uddi.org:wsdl:types", "WSDL type",
                "binding");
        addKeyedReferenceToCategoryBag(categoryBag, "uddi:uddi.org:wsdl:porttypereference",
                "portType reference", tModelKey);

        ArrayList<String> protocolNTransport = getBindingProtocolAndTransport(binding);
        /*It has only the protocol, In http:binding there is no separate transport tModel,
        therefore no keyedReference in the categoryBag from the Transport Categorization category system.*/
        if (protocolNTransport.size() == 2) {
            addKeyedReferenceToCategoryBag(categoryBag, "uddi:uddi.org:wsdl:categorization:protocol",
                    protocolNTransport.get(0), protocolNTransport.get(1));
        }
        //It has both protocol and transport
        if (protocolNTransport.size() == 4) {
            addKeyedReferenceToCategoryBag(categoryBag, "uddi:uddi.org:wsdl:categorization:protocol",
                    protocolNTransport.get(0), protocolNTransport.get(1));
            addKeyedReferenceToCategoryBag(categoryBag, "uddi:uddi.org:wsdl:categorization:transportport",
                    protocolNTransport.get(2), protocolNTransport.get(3));
        }
        addKeyedReferenceToCategoryBag(categoryBag, "uddi:uddi.org:categorization:types",
                "uddi-org:types", "wsdlSpec");

        tModel.setCategoryBag(categoryBag);
        return tModel;
    }

    /**
     * Get binding protocol and transport information
     *
     * @param binding WSDL binding
     * @return ArrayList<String> containing protocol, TModelKey of protocol, transport and TModelKey of transport respectively
     */
    private ArrayList<String> getBindingProtocolAndTransport(BindingImpl binding) {
        ArrayList<String> protocolNTransport = new ArrayList<String>();
        if (binding.getExtensibilityElements().iterator().hasNext()) {
            String className = binding.getExtensibilityElements().iterator().next().getClass().getSimpleName();
            if (UDDIConstants.SOAP11_BINDING_CLASS.equals(className)) {
                SOAPBindingImpl soap11BindingProtocol =
                        (SOAPBindingImpl) binding.getExtensibilityElements().iterator().next();
                String transportUri = soap11BindingProtocol.getTransportURI();

                protocolNTransport.add(UDDIConstants.SOAP_PROTOCOL);
                protocolNTransport.add(UDDIConstants.SOAP_PROTOCOL_TMODEL_KEY);

                if (UDDIConstants.SOAP_OVER_HTTP.equals(transportUri)) {

                    protocolNTransport.add(UDDIConstants.HTTP_TRANSPORT);
                    protocolNTransport.add(UDDIConstants.HTTP_TRANSPORT_TMODEL_KEY);

                } else if (UDDIConstants.SOAP_OVER_SMTP.equals(transportUri)) {

                    protocolNTransport.add(UDDIConstants.SMTP_TRANSPORT);
                    protocolNTransport.add(UDDIConstants.SMTP_TRANSPORT_TMODEL_KEY);

                }
            } else if (UDDIConstants.SOAP12_BINDING_CLASS.equals(className)) {
                SOAP12BindingImpl soap12BindingProtocol =
                        (SOAP12BindingImpl) binding.getExtensibilityElements().iterator().next();
                String transportUri = soap12BindingProtocol.getTransportURI();

                protocolNTransport.add(UDDIConstants.SOAP_PROTOCOL);
                protocolNTransport.add(UDDIConstants.SOAP_PROTOCOL_TMODEL_KEY);

                if (UDDIConstants.SOAP_OVER_HTTP.equals(transportUri)) {

                    protocolNTransport.add(UDDIConstants.HTTP_TRANSPORT);
                    protocolNTransport.add(UDDIConstants.HTTP_TRANSPORT_TMODEL_KEY);

                } else if (UDDIConstants.SOAP_OVER_SMTP.equals(transportUri)) {

                    protocolNTransport.add(UDDIConstants.SMTP_TRANSPORT);
                    protocolNTransport.add(UDDIConstants.SMTP_TRANSPORT_TMODEL_KEY);

                }
            } else if (UDDIConstants.HTTP_BINDING_CLASS.equals(className)) {

                protocolNTransport.add(UDDIConstants.HTTP_PROTOCOL);
                protocolNTransport.add(UDDIConstants.HTTP_PROTOCOL_TMODEL_KEY);

            }
        }
        return protocolNTransport;
    }

    /**
     * Publishing TModels that has further details regarding a service
     *
     * @param tModelInstanceDetails TModelInstanceDetails reference that is used by the bindingTemplate
     */
    private void publishDocumentTModel(TModelInstanceDetails tModelInstanceDetails) {

        ArrayList<ServiceDocumentsBean> documents = businessServiceInfo.getDocuments();

        Description desc = new Description();
        desc.setValue("This provides a documentation about the service");

        for (ServiceDocumentsBean document : documents) {

            TModelInstanceInfo tModelInstanceInfo = new TModelInstanceInfo();
            tModelInstanceInfo.getDescription().add(desc);

            TModel tModel = new TModel();
            Name documentName = new Name();
            documentName.setValue(document.getDocumentType());
            tModel.setName(documentName);

            OverviewDoc docDetails = new OverviewDoc();
            OverviewURL docUrl = new OverviewURL();
            docUrl.setValue(document.getDocumentUrl());
            docUrl.setUseType(UDDIConstants.TEXT);
            docDetails.setOverviewURL(docUrl);
            Description description = new Description();
            description.setValue(document.getDocumentDescription());
            docDetails.getDescription().add(description);
            tModel.getOverviewDoc().add(docDetails);

            TModelDetail tModelDetail = null;
            try {
                tModelDetail = UDDIUtil.saveTModel(tModel);
                if (log.isDebugEnabled()) {
                    log.debug("Document: " + documentName.getValue() + " TModel has been successfully published to UDDI.");
                }
            } catch (RegistryException e) {
                log.error("Failed to publish document: " + documentName.getValue() + " TModel",e);
            }
            if (tModelDetail != null) {
                tModelInstanceInfo.setTModelKey(tModelDetail.getTModel().iterator().next().getTModelKey());
                tModelInstanceDetails.getTModelInstanceInfo().add(tModelInstanceInfo);
            }
        }
    }


    /**
     * Publishing TModels that has additional endpoint details regarding a service
     *
     * @param tModelInstanceDetails TModelInstanceDetails reference that is used by the bindingTemplate
     */
    private void publishEndpointDetails(TModelInstanceDetails tModelInstanceDetails) {

        Description desc = new Description();
        desc.setValue("This provides the endpoint for an environment");

        OMElement[] elements = businessServiceInfo.getServiceEndpoints();

        for (OMElement element : elements) {
            String endpointDetail = ((OMTextImpl) ((OMElementImpl) element).getFirstOMChildIfAvailable()).getText();
            //endpointDetail contains both environment and endpoint that is separated by a colon
            String environment = endpointDetail.split(":")[0];
            int characterLengthForFirstColon = endpointDetail.indexOf(":") + 1;
            String endpoint = endpointDetail.substring(characterLengthForFirstColon);

            TModelInstanceInfo tModelInstanceInfo = new TModelInstanceInfo();
            tModelInstanceInfo.getDescription().add(desc);

            TModel tModel = new TModel();
            Name environmentName = new Name();
            environmentName.setValue(environment);
            tModel.setName(environmentName);

            OverviewDoc overviewDoc = new OverviewDoc();
            OverviewURL endpointUrl = new OverviewURL();
            endpointUrl.setValue(endpoint);
            overviewDoc.setOverviewURL(endpointUrl);
            tModel.getOverviewDoc().add(overviewDoc);

            TModelDetail tModelDetail = null;
            try {
                tModelDetail = UDDIUtil.saveTModel(tModel);
                if (log.isDebugEnabled()) {
                    log.debug("Endpoint: " + environmentName.getValue() + " TModel has been successfully published to UDDI.");
                }
            } catch (RegistryException e) {
                log.error("Failed to publish endpoint: " + environmentName.getValue() + " TModel",e);
            }
            if (tModelDetail != null) {
                tModelInstanceInfo.setTModelKey(tModelDetail.getTModel().iterator().next().getTModelKey());
                tModelInstanceDetails.getTModelInstanceInfo().add(tModelInstanceInfo);
            }

        }

    }


    /**
     * This is used for adding a KeyedReference to a CategoryBag
     *
     * @param categoryBag CategoryBag reference
     * @param tModelKey   tModelKey of the KeyedReference
     * @param keyName     KeyName of the KeyedReference
     * @param keyValue    KeyValue of the KeyedReference
     */
    private void addKeyedReferenceToCategoryBag(CategoryBag categoryBag, String tModelKey, String keyName,
                                                String keyValue) {
        KeyedReference keyedReference = new KeyedReference();
        keyedReference.setTModelKey(tModelKey);
        keyedReference.setKeyName(keyName);
        keyedReference.setKeyValue(keyValue);
        categoryBag.getKeyedReference().add(keyedReference);
    }

    /**
     * Create and save business entity
     *
     * @return businessKey of saved BusinessEntity
     */
    private String saveBusiness(AuthToken authToken) throws RegistryException {

        String businessKey = null;
        BusinessInfo businessInfo;
        String businessName = null;
        BusinessEntity businessEntity = new BusinessEntity();


            businessName = BUSINESS_NAME;
            String businessHomepageUrl = BUSINESS_HOMEPAGE_URL;

            //This is the first time GREG instance publish a businessEntity after startup
            if (UDDIUtil.businessKeyMap == null || !UDDIUtil.businessKeyMap.containsKey(businessHomepageUrl)) {
                businessInfo = findBusiness(businessName, businessHomepageUrl);

                //This business has never published into UDDI, then publish it.
                if (businessInfo == null) {
                    Name businessEntityName = new Name();
                    businessEntityName.setValue(businessName);
                    businessEntityName.setLang(UDDIConstants.ENGLISH);
                    businessEntity.getName().add(businessEntityName);

                    DiscoveryURL url = new DiscoveryURL();
                    url.setValue(businessHomepageUrl);
                    url.setUseType(UDDIConstants.BUSINESS_HOMEPAGE);

                    DiscoveryURLs discoveryUrls = new DiscoveryURLs();
                    discoveryUrls.getDiscoveryURL().add(url);
                    businessEntity.setDiscoveryURLs(discoveryUrls);
                    BusinessDetail businessDetail = null;
                    try {
                        businessDetail = UDDIUtil.publishBusiness(businessEntity, authToken);
                    } catch (RegistryException e) {
                        throw new RegistryException("Unable to publish the business entity" ,e);
                    }
                    businessKey = businessDetail.getBusinessEntity().iterator().next().getBusinessKey();
                    if(log.isDebugEnabled()){
                        log.debug("Business entity has been successfully published into UDDI");
                    }
                } else {
                    businessKey = businessInfo.getBusinessKey();
                    if(log.isDebugEnabled()){
                        log.debug("BusinessKey of the businessEntity:" + businessName + " has been successfully taken " +
                                "from UDDI registry.");
                    }
                }
                if (UDDIUtil.businessKeyMap == null) {
                    UDDIUtil.businessKeyMap = new HashMap<String, String>();
                }
                UDDIUtil.businessKeyMap.put(businessHomepageUrl, businessKey);
            } else {
                businessKey = UDDIUtil.businessKeyMap.get(businessHomepageUrl);
                if(log.isDebugEnabled()){
                    log.debug("BusinessKey of the businessEntity:" + businessName + " has been successfully taken.");
                }
            }
        return businessKey;
    }

    /**
     * This method used to check whether we have already publish the business into UDDI registry. If so get
     * businessKey of that BusinessEntity
     * @param businessName name of the business
     * @param businessHomepageUrl Homepage of the business
     * @return BusinessInfo object with businessKey
     */
    private BusinessInfo findBusiness(String businessName, String businessHomepageUrl) {

        FindBusiness business = new FindBusiness();
        BusinessList businessList = null;
        BusinessInfo businessInfo = null;

        Name name = new Name();
        name.setValue(businessName);

        DiscoveryURL homepageUrl = new DiscoveryURL();
        homepageUrl.setValue(businessHomepageUrl);
        DiscoveryURLs urls = new DiscoveryURLs();
        urls.getDiscoveryURL().add(homepageUrl);

        business.getName().add(name);
        business.setDiscoveryURLs(urls);
        try {
            businessList = UDDIUtil.findBusiness(business);

            /*BusinessList contains list of BusinessInfo with different businessKeys (We only allow to publish one
            businessKey)*/
            if (businessList.getBusinessInfos() != null) {
                //get businessInfo containing the businessKey
                businessInfo = businessList.getBusinessInfos().getBusinessInfo().iterator().next();
            }
        } catch (RegistryException e) {
            log.error("Failed to find the business" + businessName,e);
        }
        return businessInfo;
    }

}