/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.registry.extensions.handlers;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.*;

/**
 * Handler to manage processes.
 */
public class ProcessMediaTypeHandler extends Handler {
    private static final Log log = LogFactory.getLog(ProcessMediaTypeHandler.class);

    private List<String> smartLifecycleLinks = new LinkedList<String>();

    public void setSmartLifecycleLinks(OMElement locationConfiguration) throws RegistryException {
        Iterator confElements = locationConfiguration.getChildElements();
        while (confElements.hasNext()) {
            OMElement confElement = (OMElement)confElements.next();
            if (confElement.getQName().equals(new QName("key"))) {
                smartLifecycleLinks.add(confElement.getText());
            }
        }
    }

    public void put(RequestContext requestContext) throws RegistryException {

        if (!CommonUtil.isUpdateLockAvailable()) {
            return;
        }
        CommonUtil.acquireUpdateLock();
        try {
            Registry registry = requestContext.getRegistry();
            Resource resource = requestContext.getResource();
            if (resource == null) {
                throw new RegistryException("The resource is not available.");
            }
            String originalProcessPath = requestContext.getResourcePath().getPath();
            String resourceName = RegistryUtils.getResourceName(originalProcessPath);

            OMElement processInfoElement;
            Object resourceContent = resource.getContent();
            String processInfo;
            if (resourceContent instanceof String) {
                processInfo = (String) resourceContent;
            } else {
                processInfo = RegistryUtils.decodeBytes((byte[]) resourceContent);
            }
            try {
                XMLStreamReader reader = XMLInputFactory.newInstance().
                        createXMLStreamReader(new StringReader(processInfo));
                StAXOMBuilder builder = new StAXOMBuilder(reader);
                processInfoElement = builder.getDocumentElement();
            } catch (Exception e) {
                String msg = "Error in parsing the process content of the process. " +
                        "The requested path to store the process: " + originalProcessPath + ".";
                log.error(msg);
                throw new RegistryException(msg, e);
            }
            // derive the process path that the process should be saved.
            String processName = CommonUtil.getServiceName(processInfoElement);
            String processNamespace = CommonUtil.getServiceNamespace(processInfoElement);

            String processPath = RegistryUtils.getAbsolutePath(registry.getRegistryContext(),
                    RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + "/processes" +
                            (processNamespace == null ? "" :
                                    CommonUtil.derivePathFragmentFromNamespace(processNamespace)) +
                            processName);
            String processVersion =
                    org.wso2.carbon.registry.common.utils.CommonUtil.getServiceVersion(
                            processInfoElement);
            if (processVersion.length() == 0) {
                processVersion = "1.0.0";
                CommonUtil.setServiceVersion(processInfoElement, processVersion);
                resource.setContent(processInfoElement.toString());
            }
            String processVersionPath = CommonUtil.computeProcessPathWithVersion(
                    processPath, processVersion);
            // saving the artifact id.
            String processId = resource.getUUID();
            if (processId == null) {
                // generate a process id
                processId = UUID.randomUUID().toString();
                resource.setUUID(processId);
            }
            if (registry.resourceExists(processVersionPath)) {
                Resource oldResource = registry.get(processVersionPath);
                String oldContent = RegistryUtils.decodeBytes((byte[]) oldResource.getContent());
                OMElement oldProcessInfoElement = null;
                if (processInfo.equals(oldContent)) {
                    /* if user is not changing anything in process we skip the processing done in this handler */
                    return;
                }
            }
//            CommonUtil.addGovernanceArtifactEntryWithAbsoluteValues(
//                    CommonUtil.getUnchrootedSystemRegistry(requestContext),
//                    processId, processVersionPath);

            boolean alreadyAdded = false;
            String workflowURL = CommonUtil.getWorkflowURL(processInfoElement);
            if (workflowURL != null && workflowURL.startsWith(RegistryConstants.ROOT_PATH)) {
                // it seems workflowURL is a registry path..
                String workflowPath = RegistryUtils.getAbsolutePath(requestContext.getRegistryContext(),
                        workflowURL);
                boolean addItHere = false;
                if (!registry.resourceExists(workflowPath)) {
                    String msg = "Associating process to a non-existing workflow. workflow url: " + workflowPath + ", " +
                            "process path: " + processVersionPath + ".";
                    log.error(msg);
                    throw new RegistryException(msg);
                }
                if (!registry.resourceExists(processVersionPath)) {
                    addItHere = true;
                } else {
                    Association[] dependencies = registry.getAssociations(processVersionPath, CommonConstants.DEPENDS);
                    boolean dependencyFound = false;
                    if (dependencies != null) {
                        for (Association dependency : dependencies) {
                            if (workflowPath.equals(dependency.getDestinationPath())) {
                                dependencyFound = true;
                            }
                        }
                    }
                    if (!dependencyFound) {
                        addItHere = true;
                    }
                }
                if (addItHere) { // add the process right here..
                    ((ResourceImpl) resource).prepareContentForPut();
                    persistProcessResource(registry, resource, processVersionPath);
                    alreadyAdded = true;
                    // and make the associations

                    registry.addAssociation(processVersionPath, workflowPath, CommonConstants.DEPENDS);
                    registry.addAssociation(workflowPath, processVersionPath, CommonConstants.USED_BY);
                }
            }
            if (!alreadyAdded) {
                // we are adding the resource anyway.
                ((ResourceImpl) resource).prepareContentForPut();
                persistProcessResource(registry, resource, processVersionPath);
            }

            String symlinkLocation = RegistryUtils.getAbsolutePath(requestContext.getRegistryContext(),
                    requestContext.getResource().getProperty(RegistryConstants.SYMLINK_PROPERTY_NAME));
            if (!processPath.equals(originalProcessPath)) {
                // we are creating a sym link from process path to original process path.
                Resource processResource = requestContext.getRegistry().get(
                        RegistryUtils.getParentPath(originalProcessPath));
                String isLink = processResource.getProperty("registry.link");
                String mountPoint = processResource.getProperty("registry.mountpoint");
                String targetPoint = processResource.getProperty("registry.targetpoint");
                String actualPath = processResource.getProperty("registry.actualpath");
                if (isLink != null && mountPoint != null && targetPoint != null) {
                    symlinkLocation = actualPath + RegistryConstants.PATH_SEPARATOR;
                }
                if (symlinkLocation != null) {
                    registry.createLink(symlinkLocation + resourceName, processPath);
                }
            }
            // in this flow the resource is already added. marking the process completed..
            requestContext.setProcessingComplete(true);

        } finally {
            CommonUtil.releaseUpdateLock();
        }
    }

    private void persistProcessResource(Registry registry, Resource resource,
                                        String processVersionPath) throws RegistryException {
        String patchVersionPath = RegistryUtils.getParentPath(processVersionPath);
        if (!registry.resourceExists(patchVersionPath)) {
            String minorVersionPath = RegistryUtils.getParentPath(patchVersionPath);
            if (!registry.resourceExists(minorVersionPath)) {
                String majorVersionPath = RegistryUtils.getParentPath(minorVersionPath);
                if (!registry.resourceExists(majorVersionPath)) {
                    String versionCollectionPath = RegistryUtils.getParentPath(majorVersionPath);
                    if (!registry.resourceExists(versionCollectionPath)) {
                        Collection collection = registry.newCollection();
                        collection.setMediaType(
                                CommonConstants.PROCESS_VERSION_COLLECTION_MEDIA_TYPE);
                        registry.put(versionCollectionPath, collection);
                        for (String key : smartLifecycleLinks) {
                            Resource linkResource = registry.newResource();
                            linkResource.setMediaType(
                                    CommonConstants.SMART_LIFECYCLE_LINK_MEDIA_TYPE);
                            registry.put(versionCollectionPath +
                                    RegistryConstants.PATH_SEPARATOR + key, linkResource);
                        }
                    }
                    Collection collection = registry.newCollection();
                    collection.setMediaType(CommonConstants.PROCESS_MAJOR_VERSION_MEDIA_TYPE);
                    registry.put(majorVersionPath, collection);
                }
                Collection collection = registry.newCollection();
                collection.setMediaType(CommonConstants.PROCESS_MINOR_VERSION_MEDIA_TYPE);
                registry.put(minorVersionPath, collection);
            }
            Collection collection = registry.newCollection();
            collection.setMediaType(CommonConstants.PROCESS_PATCH_VERSION_MEDIA_TYPE);
            registry.put(patchVersionPath, collection);
        }
        registry.put(processVersionPath, resource);
    }
}