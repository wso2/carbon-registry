/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.registry.uddi.uddiversionmodule;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;

public class UddiVersionHandler extends AbstractHandler implements Handler {
    private static final Log log = LogFactory.getLog(UddiVersionHandler.class);

    private static final String ENABLE = "enable";
    private static final String UDDI_SYSTEM_PROPERTY = "uddi";
    private static final String UDDI_V2 = "urn:uddi-org:api_v2";
    private static final String UDDI_V3 = "urn:uddi-org:api_v3";

    public String getName() {
        return this.getClass().getName();
    }

    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        try {
            if (ENABLE.equals(System.getProperty(UDDI_SYSTEM_PROPERTY))) {
                SOAPEnvelope envelope = msgContext.getEnvelope();
                OMNamespace namespace = envelope.getNamespace();
                String nameSpace = namespace.getNamespaceURI();
                if (nameSpace != null) {
                    log.info("first " + envelope.getBody().getFirstElementNS().getNamespaceURI());
                    log.info(envelope.toString());

                    if (envelope.getBody().getFirstElementNS().getNamespaceURI().equals(UDDI_V2)) {
                        OMFactory omFactory = envelope.getOMFactory();
                        OMElement firstElement = envelope.getBody().getFirstElement();
                        setNamespace(firstElement, omFactory.createOMNamespace(UDDI_V3,
                                firstElement.getNamespace().getPrefix()));

                    }
                    log.info("second " + envelope.getBody().getFirstElementNS().getNamespaceURI());
                    log.info(envelope.toString());
                }
            }
        } catch (Exception e) {
            log.error("An error occurred while processing SOAP message.", e);
        }
        return InvocationResponse.CONTINUE;
    }

    private void setNamespace(OMElement element, OMNamespace omNamespace) {
        element.setNamespace(omNamespace);
        Iterator childElements = element.getChildElements();
        while (childElements.hasNext()) {
            OMElement child = (OMElement) childElements.next();
            child.setNamespace(omNamespace);
            setNamespace(child, omNamespace);
        }
    }
}
