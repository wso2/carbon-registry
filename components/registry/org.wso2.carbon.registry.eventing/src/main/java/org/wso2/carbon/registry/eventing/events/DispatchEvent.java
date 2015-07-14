/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.eventing.events;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.wso2.carbon.event.core.Message;
import org.wso2.carbon.registry.common.eventing.RegistryEvent;

import java.util.Map;

public class DispatchEvent extends Message {

    private String endpoint = null;

    private boolean doRest = false;

    private RegistryEvent event = null;

    public DispatchEvent(RegistryEvent event, String endpoint, boolean doRest) {
        super();
        this.event = event;
        this.setEndpoint(endpoint);
        this.setDoRest(doRest);
        setProperties(event.getParameters());
    }

    public OMElement getMessage() {
        if (event.getMessage() instanceof OMElement) {
            return (OMElement) event.getMessage();
        } else if (event.getMessage() instanceof String) {
            OMFactory factory = OMAbstractFactory.getOMFactory();
            OMNamespace namespace = factory.createOMNamespace(
                    RegistryEvent.REGISTRY_EVENT_NS, "ns");
            OMElement payload = factory.createOMElement("Event", namespace);
            factory.createOMElement("Message", namespace, payload).setText(
                    (String) event.getMessage());

            factory.createOMElement("Timestamp", namespace, payload).setText(
                    event.getTimestamp());
            RegistryEvent.Context contextDetails = event.getContextDetails();
            RegistryEvent.Operation operationDetails =
                    event.getOperationDetails();
            RegistryEvent.RegistrySession registrySessionDetails =
                    event.getRegistrySessionDetails();
            RegistryEvent.Server serverDetails = RegistryEvent.getServerDetails();

            OMElement details = factory.createOMElement("Details", namespace, payload);

            OMElement registrySession =
                    factory.createOMElement("Session", namespace, details);
            String chroot = registrySessionDetails.getChroot();
            if (chroot == null) {
                chroot = "undefined";
            }
            factory.createOMElement("Chroot", namespace, registrySession).setText(
                    chroot);
            String username = registrySessionDetails.getUsername();
            if (username == null) {
                username = contextDetails.getUsername();
                if (username == null) {
                    username = "unknown";
                }
            }
            factory.createOMElement("Username", namespace, registrySession).setText(
                    username);
            int tenantId = registrySessionDetails.getTenantId();
            if (tenantId == -1) {
                tenantId = contextDetails.getTenantId();
                if (tenantId == -1) {
                    tenantId = event.getTenantId();
                }
            }
            factory.createOMElement("TenantId", namespace, registrySession).setText(
                    Integer.toString(tenantId));

            OMElement operation =
                    factory.createOMElement("Operation", namespace, details);
            factory.createOMElement("Path", namespace, operation).setText(
                    operationDetails.getPath());
            factory.createOMElement("EventType", namespace, operation).setText(
                    operationDetails.getEventType());
            factory.createOMElement("ResourceType", namespace, operation).setText(
                    operationDetails.getResourceType());
            @SuppressWarnings("unchecked")
            Map<String, String> parameters = event.getParameters();
            for (Map.Entry<String, String> e : parameters.entrySet()) {
                factory.createOMElement(e.getKey(), namespace, operation).setText(
                        e.getValue());
            }

            OMElement server = factory.createOMElement("Server", namespace, details);
            factory.createOMElement("HostName", namespace, server).setText(
                    serverDetails.getHostIPAddress());

            OMElement product = factory.createOMElement("Product", namespace, server);
            factory.createOMElement("Name", namespace, product).setText(
                    serverDetails.getProductName());
            factory.createOMElement("Version", namespace, product).setText(
                    serverDetails.getProductVersion());

            OMElement os = factory.createOMElement("OS", namespace, server);
            factory.createOMElement("Name", namespace, os).setText(
                    serverDetails.getOSDetails().getOperatingSystemName());
            factory.createOMElement("Version", namespace, os).setText(
                    serverDetails.getOSDetails().getOperatingSystemVersion());
            factory.createOMElement("Architecture", namespace, os).setText(
                    serverDetails.getOSDetails().getOperatingSystemArchitecture());

            OMElement user = factory.createOMElement("User", namespace, server);
            factory.createOMElement("Name", namespace, user).setText(
                    serverDetails.getOSUser().getUsername());
            factory.createOMElement("Country", namespace, user).setText(
                    serverDetails.getOSUser().getCountry());
            factory.createOMElement("Language", namespace, user).setText(
                    serverDetails.getOSUser().getLanguage());
            factory.createOMElement("TimeZone", namespace, user).setText(
                    serverDetails.getOSUser().getTimezone());

            OMElement java = factory.createOMElement("Java", namespace, server);
            factory.createOMElement("Vendor", namespace, java).setText(
                    serverDetails.getJVMDetails().getJavaVendor());
            factory.createOMElement("Version", namespace, java).setText(
                    serverDetails.getJVMDetails().getJavaVersion());

            OMElement jvm = factory.createOMElement("JVM", namespace, java);
            factory.createOMElement("Name", namespace, jvm).setText(
                    serverDetails.getJVMDetails().getJavaVMName());
            factory.createOMElement("Version", namespace, jvm).setText(
                    serverDetails.getJVMDetails().getJavaVMVersion());
            payload.build();
            return payload;
        }
        return null;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public boolean isDoRest() {
        return doRest;
    }

    public void setDoRest(boolean doRest) {
        this.doRest = doRest;
    }

    public int getTenantId() {
        return event.getTenantId();
    }

    public void setTenantId(int tenantId) {
        this.event.setTenantId(tenantId);
    }

    public String getTimestamp() {
        return event.getTimestamp();
    }

    public RegistryEvent.RegistrySession getRegistrySessionDetails() {
        return event.getRegistrySessionDetails();
    }

    public RegistryEvent.Context getContextDetails() {
        return event.getContextDetails();
    }

    public RegistryEvent.Operation getOperationDetails() {
        return event.getOperationDetails();
    }

    public String getTopic() {
        return event.getTopic();
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getParameters() {
        return event.getParameters();
    }

    public RegistryEvent getEvent(){
        return event;
    }
}
