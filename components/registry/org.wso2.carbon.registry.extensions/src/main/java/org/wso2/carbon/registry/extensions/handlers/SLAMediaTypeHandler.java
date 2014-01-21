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
 * Handler to manage SLA.
 */
public class SLAMediaTypeHandler extends Handler {
    private static final Log log = LogFactory.getLog(SLAMediaTypeHandler.class);

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
            String originalSLAPath = requestContext.getResourcePath().getPath();
            String resourceName = RegistryUtils.getResourceName(originalSLAPath);

            OMElement slaInfoElement;
            Object resourceContent = resource.getContent();
            String slaInfo;
            if (resourceContent instanceof String) {
                slaInfo = (String) resourceContent;
            } else {
                slaInfo = RegistryUtils.decodeBytes((byte[]) resourceContent);
            }
            try {
                XMLStreamReader reader = XMLInputFactory.newInstance().
                        createXMLStreamReader(new StringReader(slaInfo));
                StAXOMBuilder builder = new StAXOMBuilder(reader);
                slaInfoElement = builder.getDocumentElement();
            } catch (Exception e) {
                String msg = "Error in parsing the sla content of the sla. " +
                        "The requested path to store the sla: " + originalSLAPath + ".";
                log.error(msg);
                throw new RegistryException(msg, e);
            }
            // derive the sla path that the sla should be saved.
            String slaName = CommonUtil.getServiceName(slaInfoElement);

            String slaPath = RegistryUtils.getAbsolutePath(registry.getRegistryContext(),
                    RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + "/sla/" + slaName);
            String slaVersion =
                    org.wso2.carbon.registry.common.utils.CommonUtil.getServiceVersion(
                            slaInfoElement);
            if (slaVersion.length() == 0) {
                slaVersion = "1.0.0";
                CommonUtil.setServiceVersion(slaInfoElement, slaVersion);
                resource.setContent(slaInfoElement.toString());
            }
            String slaVersionPath = CommonUtil.computeSLAPathWithVersion(
                    slaPath, slaVersion);
            // saving the artifact id.
            String slaId = resource.getUUID();
            if (slaId == null) {
                // generate a sla id
                slaId = UUID.randomUUID().toString();
                resource.setUUID(slaId);
            }
            if (registry.resourceExists(slaVersionPath)) {
                Resource oldResource = registry.get(slaVersionPath);
                String oldContent = RegistryUtils.decodeBytes((byte[]) oldResource.getContent());
                OMElement oldSLAInfoElement = null;
                if (slaInfo.equals(oldContent)) {
                    /* if user is not changing anything in sla we skip the processing done in this handler */
                    return;
                }
            }
//            CommonUtil.addGovernanceArtifactEntryWithAbsoluteValues(
//                    CommonUtil.getUnchrootedSystemRegistry(requestContext),
//                    slaId, slaVersionPath);

            boolean alreadyAdded = false;
            String workflowURL = CommonUtil.getWorkflowURL(slaInfoElement);
            if (workflowURL != null && workflowURL.startsWith(RegistryConstants.ROOT_PATH)) {
                // it seems workflowURL is a registry path..
                String workflowPath = RegistryUtils.getAbsolutePath(requestContext.getRegistryContext(),
                        workflowURL);
                boolean addItHere = false;
                if (!registry.resourceExists(workflowPath)) {
                    String msg = "Associating sla to a non-existing workflow. workflow url: " + workflowPath + ", " +
                            "sla path: " + slaVersionPath + ".";
                    log.error(msg);
                    throw new RegistryException(msg);
                }
                if (!registry.resourceExists(slaVersionPath)) {
                    addItHere = true;
                } else {
                    Association[] dependencies = registry.getAssociations(slaVersionPath, CommonConstants.DEPENDS);
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
                if (addItHere) { // add the sla right here..
                    ((ResourceImpl) resource).prepareContentForPut();
                    persistSLAResource(registry, resource, slaVersionPath);
                    alreadyAdded = true;
                    // and make the associations

                    registry.addAssociation(slaVersionPath, workflowPath, CommonConstants.DEPENDS);
                    registry.addAssociation(workflowPath, slaVersionPath, CommonConstants.USED_BY);
                }
            }
            if (!alreadyAdded) {
                // we are adding the resource anyway.
                ((ResourceImpl) resource).prepareContentForPut();
                persistSLAResource(registry, resource, slaVersionPath);
            }

            String symlinkLocation = RegistryUtils.getAbsolutePath(requestContext.getRegistryContext(),
                    requestContext.getResource().getProperty(RegistryConstants.SYMLINK_PROPERTY_NAME));
            if (!slaPath.equals(originalSLAPath)) {
                // we are creating a sym link from sla path to original sla path.
                Resource slaResource = requestContext.getRegistry().get(
                        RegistryUtils.getParentPath(originalSLAPath));
                String isLink = slaResource.getProperty("registry.link");
                String mountPoint = slaResource.getProperty("registry.mountpoint");
                String targetPoint = slaResource.getProperty("registry.targetpoint");
                String actualPath = slaResource.getProperty("registry.actualpath");
                if (isLink != null && mountPoint != null && targetPoint != null) {
                    symlinkLocation = actualPath + RegistryConstants.PATH_SEPARATOR;
                }
                if (symlinkLocation != null) {
                    registry.createLink(symlinkLocation + resourceName, slaPath);
                }
            }
            // in this flow the resource is already added. marking the sla completed..
            requestContext.setProcessingComplete(true);

        } finally {
            CommonUtil.releaseUpdateLock();
        }
    }

    private void persistSLAResource(Registry registry, Resource resource,
                                        String slaVersionPath) throws RegistryException {
        String patchVersionPath = RegistryUtils.getParentPath(slaVersionPath);
        if (!registry.resourceExists(patchVersionPath)) {
            String minorVersionPath = RegistryUtils.getParentPath(patchVersionPath);
            if (!registry.resourceExists(minorVersionPath)) {
                String majorVersionPath = RegistryUtils.getParentPath(minorVersionPath);
                if (!registry.resourceExists(majorVersionPath)) {
                    String versionCollectionPath = RegistryUtils.getParentPath(majorVersionPath);
                    if (!registry.resourceExists(versionCollectionPath)) {
                        Collection collection = registry.newCollection();
                        collection.setMediaType(
                                CommonConstants.SLA_VERSION_COLLECTION_MEDIA_TYPE);
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
                    collection.setMediaType(CommonConstants.SLA_MAJOR_VERSION_MEDIA_TYPE);
                    registry.put(majorVersionPath, collection);
                }
                Collection collection = registry.newCollection();
                collection.setMediaType(CommonConstants.SLA_MINOR_VERSION_MEDIA_TYPE);
                registry.put(minorVersionPath, collection);
            }
            Collection collection = registry.newCollection();
            collection.setMediaType(CommonConstants.SLA_PATCH_VERSION_MEDIA_TYPE);
            registry.put(patchVersionPath, collection);
        }
        registry.put(slaVersionPath, resource);
    }
}
