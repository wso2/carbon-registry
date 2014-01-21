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
package org.wso2.carbon.registry.uddi.utils;


public class UDDIConstants {

    //Business entity property names
    public static final String BUSINESS_NAME = "businessName";
    public static final String BUSINESS_HOMEPAGE_URL="homePage";

    public static final String BUSINESS_HOMEPAGE = "homepage";

    public static final String ENGLISH = "en";

    public static final String SOAP11_ADDRESS_CLASS = "SOAPAddressImpl";
    public static final String SOAP12_ADDRESS_CLASS = "SOAP12AddressImpl";
    public static final String HTTP_ADDRESS_CLASS = "HTTPAddressImpl";

    public static final String SOAP11_BINDING_CLASS = "SOAPBindingImpl";
    public static final String SOAP12_BINDING_CLASS = "SOAP12BindingImpl";
    public static final String HTTP_BINDING_CLASS = "HTTPBindingImpl";

    //Constant values that are used to identify the protocol and transport
    public static final String SOAP_OVER_HTTP = "http://schemas.xmlsoap.org/soap/http";
    public static final String SOAP_OVER_JMS = "http://schemas.xmlsoap.org/soap/jms";
    public static final String SOAP_OVER_SMTP = "http://schemas.pocketsoap.com/soap/smtp";

    public static final String END_POINT = "endPoint";

    public static final String REGISTRY_VERSION = ";version:";

    public static final String SOAP_PROTOCOL = "SOAP Protocol";
    public static final String SOAP_PROTOCOL_TMODEL_KEY = "uddi:uddi.org:protocol:soap";

    public static final String HTTP_PROTOCOL = "HTTP Protocol";
    public static final String HTTP_PROTOCOL_TMODEL_KEY = "uddi:uddi.org:protocol:http";

    public static final String HTTP_TRANSPORT = "HTTP Transport";
    public static final String HTTP_TRANSPORT_TMODEL_KEY = "uddi:uddi.org:transport:http";

    public static final String SMTP_TRANSPORT = "SMTP Transport";
    public static final String SMTP_TRANSPORT_TMODEL_KEY = "uddi:uddi.org:transport:smtp";

    public static final String UDDI_TYPE_CATEGORY_SYSTEM = "uddi:uddi.org:categorization:types";
    public static final String TYPES_UNCHECKED = "types:unchecked";
    public static final String UNCHECKED = "unchecked";
    
    public static final String WSDL_INTERFACE = "wsdlInterface";
    public static final String TEXT = "text";
}
