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
import org.apache.commons.io.filefilter.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.registry.common.utils.CommonUtil;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.HandlerLifecycleManager;
import org.wso2.carbon.registry.core.jdbc.handlers.filters.URLMatcher;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.extensions.handlers.scm.ExternalContentHandler;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @scr.component name="org.wso2.carbon.registry.scm" immediate="true"
 * @scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 */
@SuppressWarnings({"unused", "JavaDoc"})
public class RegistrySCMServiceComponent {

    private static Log log = LogFactory.getLog(RegistrySCMServiceComponent.class);
    private static final int DEFAULT_UPDATE_FREQUENCY = 60;

    protected void activate(ComponentContext context) {
        log.debug("Registry SCM component is activated");
    }

    protected void deactivate(ComponentContext context) {
        log.debug("Registry SCM component is deactivated");
    }

    protected void setRegistryService(RegistryService registryService) {
        try {
            registerConnections(registryService);
        } catch (RegistryException e) {
            log.error("Unable to register connections", e);
        }
    }

    protected void unsetRegistryService(RegistryService registryService) {
    }

    private void registerConnections(RegistryService registryService) throws RegistryException {
        String configPath = CarbonUtils.getRegistryXMLPath();
        if (configPath != null) {
            File registryXML = new File(configPath);
            if (registryXML.exists()) {
                try {
                    CurrentSession.setCallerTenantId(MultitenantConstants.SUPER_TENANT_ID);
                    FileInputStream fileInputStream = new FileInputStream(registryXML);
                    StAXOMBuilder builder = new StAXOMBuilder(
                            CarbonUtils.replaceSystemVariablesInXml(fileInputStream));
                    OMElement configElement = builder.getDocumentElement();
                    SecretResolver secretResolver = SecretResolverFactory.create(configElement, false);
                    OMElement scm = configElement.getFirstChildWithName(new QName("scm"));
                    if (scm != null) {
                        ScheduledExecutorService executorService =
                                Executors.newScheduledThreadPool(10);
                        Iterator connections = scm.getChildrenWithName(new QName("connection"));
                        while (connections.hasNext()) {
                            OMElement connection = (OMElement) connections.next();

                            String checkOutURL =
                                    connection.getAttributeValue(new QName("checkOutURL"));
                            // Read-Only by default, and can be disabled if needed by setting
                            // Read-Only to false.
                            boolean readOnly = !Boolean.toString(false).equalsIgnoreCase(
                                    connection.getAttributeValue(new QName("readOnly")));
                            String checkInURL =
                                    connection.getAttributeValue(new QName("checkInURL"));
                            String workingDir =
                                    connection.getAttributeValue(new QName("workingDir"));
                            String mountPoint =
                                    connection.getAttributeValue(new QName("mountPoint"));
                            String username =
                                    connection.getFirstChildWithName(new QName("username")).getText();
                            String password =
                                    connection.getFirstChildWithName(new QName("password")).getText();
                            int updateFrequency = DEFAULT_UPDATE_FREQUENCY;
                            try {
                                updateFrequency = Integer.parseInt(
                                        connection.getAttributeValue(new QName("updateFrequency")));
                            } catch (NumberFormatException ignore) {

                            }
                            UserRegistry registry = registryService
                                    .getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME);
                            File directory = new File(workingDir);
                            if (!directory.exists() && !directory.isDirectory()) {
                                log.error("A valid directory was not found in path: " + workingDir);
                                continue;
                            }
                            String filePath = directory.getAbsolutePath();
                            if (!registry.resourceExists(mountPoint)) {
                                Collection collection = registry.newCollection();
                                collection.setProperty(RegistryConstants.REGISTRY_NON_RECURSIVE,
                                        "true");
                                registry.put(mountPoint, collection);
                            }
                            loadRegistryResources(registry, directory, filePath, mountPoint);
                            ExternalContentHandler externalContentHandler =
                                    new ExternalContentHandler();
                            externalContentHandler.setFilePath(filePath);
                            externalContentHandler.setMountPath(mountPoint);
                            URLMatcher urlMatcher = new URLMatcher();
                            urlMatcher.setPattern(Pattern.quote(mountPoint) + "($|" +
                                    RegistryConstants.PATH_SEPARATOR + ".*|" +
                                    RegistryConstants.URL_SEPARATOR + ".*)");
                            RegistryContext registryContext = registry.getRegistryContext();
                            registryContext.registerNoCachePath(mountPoint);
                            registryContext.getHandlerManager().addHandler(null,
                                    urlMatcher, externalContentHandler,
                                    HandlerLifecycleManager.TENANT_SPECIFIC_SYSTEM_HANDLER_PHASE);
                            executorService.scheduleWithFixedDelay(new SCMUpdateTask(directory,
                                    checkOutURL, checkInURL, readOnly, externalContentHandler,
                                    username, CommonUtil.getResolvedPassword(secretResolver,"scm",password)), 0, updateFrequency, TimeUnit.MINUTES);
                        }
                    }
                } catch (XMLStreamException e) {
                    log.error("Unable to parse registry.xml", e);
                } catch (IOException e) {
                    log.error("Unable to read registry.xml", e);
                } catch (CarbonException e) {
                    log.error("An error occurred during system variable replacement", e);
                } finally {
                    CurrentSession.removeCallerTenantId();
                }
            }
        }
    }

    private void loadRegistryResources(Registry registry, File directory, String workingDir,
                                       String mountPoint) throws RegistryException {
        File[] files = directory.listFiles((FileFilter) new AndFileFilter(HiddenFileFilter.VISIBLE,
                new OrFileFilter(DirectoryFileFilter.INSTANCE, FileFileFilter.FILE)));
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                loadRegistryResources(registry, file, workingDir, mountPoint);
            } else {
                // convert windows paths so that it fits into the Unix-like registry path structure.
                String path = mountPoint +
                        file.getAbsolutePath().substring(workingDir.length()).replace("\\", "/");
                if (!registry.resourceExists(path)) {
                    registry.put(path, registry.newResource());
                }
            }
        }
    }
}
