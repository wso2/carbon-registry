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
package org.wso2.carbon.registry.common.ui;

public class WSDLConstants {
     public static final String WSDL = "Wsdl";
     public static final String WSDL_VERSION_VALUE_11 = "1.1";
     public static final String WSDL_VERSION_VALUE_20 = "2.0";
     public static final String WSDL_VERSION_VALUE_UNKNOWN = "Unknown";
     public static final String WSDL_DOCUMENTATION_EXPR = "/wsdl:definitions/wsdl:documentation";
     public static final String WSDL_DOCUMENTATION = "Documentation";
     public static final String WSDL_VERSION = "Version";
     public static final String WSDL_SERVICE = "Service";
     public static final String SERVICE_EXPR = "/wsdl:definitions/wsdl:service";
     public static final String TARGET_NAMESPACE = "ancestor::*/@targetNamespace";
     public static final String SERVICE_NAME_ATTRIBUTE = "name";
     public static final String PORT_EXPR = "wsdl:port";
     public static final String BINDING_ATTRIBUTE= "binding";
     public static final String BINDING= "Binding";
     public static final String PORT_NAME_ATTRIBUTE = "name";
     public static final String SERVICE_PORT = "Port";
     public static final String SOAP_BINDING_EXPR= "soap:binding";
     public static final String SOAP12_BINDING_EXPR= "soap12:binding";
     public static final String HTTP_BINDING_EXPR= "http:binding";
     public static final String SOAP_VERSION= "SOAP Version";
     public static final String SOAP_11= "SOAP 1.1";
     public static final String SOAP_12= "SOAP 1.2";
     public static final String HTTP_BINDING = "Http Binding";
     public static final String HTTP_BINDING_TRUE = "True";
     public static final String SOAP_11_ENDPOINT_EXPR = "soap:address/@location";
     public static final String SOAP_12_ENDPOINT_EXPR = "soap12:address/@location";
     public static final String HTTP_ENDPOINT_EXPR = "http:address/@location";
     public static final String SOAP_11_ENDPOINT = "Address";
     public static final String SOAP_12_ENDPOINT = "Address";
     public static final String HTTP_ENDPOINT = "Address";
     public static final String BINDING_STYLE_EXPR = "//@style";
     public static final String BINDING_STYLE = "Style";
     public static final String BINDING_TRANSPORT_ATTRIBUTE = "transport";
     public static final String BINDING_TRANSPORT = "Transport";
     public static final String HTTP_TRANSPORT_ATTRIBUTE_VALUE = "http://schemas.xmlsoap.org/soap/http";
     public static final String HTTP_TRANSPORT_VALUE = "Http";
     public static final String NON_HTTP_TRANSPORT_VALUE = "Non Http";
     public static final String BINDING_VERB_ATTRIBUTE = "verb";
     public static final String BINDING_VERB = "Verb";
     public static final String BINDING_PORT_TYPE = "type";
     public static final String PORT_TYPE = "Port Type";
     public static final String PORT_TYPE_OPERATIONS_EXPR = "wsdl:operation/@name";
     public static final String OPERATIONS = "Operation(s)";
     public static final String BINDING_EXPR = "/wsdl:definitions/wsdl:binding";
     public static final String BINDING_NAME_ATTRIBUTE = "name";
     public static final String PORT_TYPE_EXPR = "/wsdl:definitions/wsdl:portType";
     public static final String POR_TYPE_NAME_ATTRIBUTE = "name";
     public static final String SCHEMA_IMPORTS_EXPR = "//xsd:import/@schemaLocation";
     public static final String WSDL_IMPORTS_EXPR = "//wsdl:import/@location";
     public static final String SCHEMA_IMPORTS = "Schema Import(s)";
     public static final String WSDL_IMPORTS = "WSDL Import(s)";
     public static final String RESOURCE_JSP_PAGE = "resource.jsp";
     public static final String PATH_REQ_PARAMETER = "path";
}
