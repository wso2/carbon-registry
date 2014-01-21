/*
 *  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.wso2.carbon.registry.extensions.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.utils.AuthorizationUtils;
import org.wso2.carbon.user.mgt.UserMgtConstants;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Stack;

/**
 * @scr.component name="org.wso2.carbon.registry.resource.permissions" immediate="true"
 * @scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 */
@SuppressWarnings({"unused", "JavaDoc"})
public class RegistryResourcePermissionsServiceComponent {

    private static Log log = LogFactory.getLog(RegistryResourcePermissionsServiceComponent.class);
    private Stack<ServiceRegistration> serviceRegistrations = new Stack<ServiceRegistration>();
    private RegistryService registryService;

    protected void activate(ComponentContext context) {
        loadMappings();
        log.debug("Registry Resource Permissions component is activated");
    }

    protected void deactivate(ComponentContext context) {

    }

    protected void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    protected void unsetRegistryService(RegistryService registryService) {
        this.registryService = null;
    }

    private void loadMappings() {
        String configPath = CarbonUtils.getEtcCarbonConfigDirPath();
        if (configPath != null) {
            configPath += File.separator + "permission-mappings.xml";
            File mappingsXML = new File(configPath);
            if (mappingsXML.exists()) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(mappingsXML);
                    StAXOMBuilder builder = new StAXOMBuilder(fileInputStream);
                    OMElement configElement = builder.getDocumentElement();
                    int counter = 100;
                    Iterator mappings = configElement.getChildrenWithName(new QName("mapping"));
                    while (mappings.hasNext()) {
                        OMElement mapping = (OMElement) mappings.next();
                        String managementPermission =
                                mapping.getAttributeValue(new QName("managementPermission"));
                        String resourcePermission =
                                mapping.getAttributeValue(new QName("resourcePermission"));
                        String[] resourcePaths =
                                mapping.getAttributeValue(new QName("resourcePaths")).split(",");
                        for (String resourcePath : resourcePaths) {
                            AuthorizationUtils.addAuthorizeRoleListener(
                                    counter++, resourcePath.trim(), managementPermission,
                                    UserMgtConstants.EXECUTE_ACTION,
                                    new String[]{resourcePermission});
                        }
                    }
                } catch (XMLStreamException e) {
                    log.error("Unable to parse permission-mappings.xml", e);
                } catch (IOException e) {
                    log.error("Unable to read permission-mappings.xml", e);
                }
            }
        }
    }
}
