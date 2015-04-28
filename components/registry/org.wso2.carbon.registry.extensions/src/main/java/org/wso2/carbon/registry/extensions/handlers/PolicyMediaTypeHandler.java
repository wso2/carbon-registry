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

package org.wso2.carbon.registry.extensions.handlers;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.common.utils.artifact.manager.ArtifactManager;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.AuthorizationUtils;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;
import org.wso2.carbon.user.mgt.UserMgtConstants;

import javax.xml.namespace.QName;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class PolicyMediaTypeHandler extends Handler {
    private static final Log log = LogFactory.getLog(PolicyMediaTypeHandler.class);

    private String location = "/policies/";
    private String locationTag = "location";
    private OMElement locationConfiguration;

    public OMElement getPolicyLocationConfiguration() {
        return locationConfiguration;
    }

    public void setPolicyLocationConfiguration(OMElement locationConfiguration) throws RegistryException {
        Iterator confElements = locationConfiguration.getChildElements();
        while (confElements.hasNext()) {
            OMElement confElement = (OMElement)confElements.next();
            if (confElement.getQName().equals(new QName(locationTag))) {
                location = confElement.getText();
                if (!location.startsWith(RegistryConstants.PATH_SEPARATOR)) {
                    location = RegistryConstants.PATH_SEPARATOR + location;
                }
                if (!location.endsWith(RegistryConstants.PATH_SEPARATOR)) {
                    location = location + RegistryConstants.PATH_SEPARATOR;
                }
            }
        }
        this.locationConfiguration = locationConfiguration;
    }

    public void put(RequestContext requestContext) throws RegistryException {
        if (!CommonUtil.isUpdateLockAvailable()) {
            return;
        }
        CommonUtil.acquireUpdateLock();
        try {
            if (requestContext == null) {
                throw new RegistryException("The request context is not available.");
            }
            String path = requestContext.getResourcePath().getPath();
            Resource resource = requestContext.getResource();
            Registry registry = requestContext.getRegistry();

            Object resourceContentObj = resource.getContent();
            String resourceContent; // here the resource content is url
            if (resourceContentObj instanceof String) {
                resourceContent = (String)resourceContentObj;
                resource.setContent(RegistryUtils.encodeString(resourceContent));
            } else {
                resourceContent = RegistryUtils.decodeBytes((byte[])resourceContentObj);
            }
            try {
                if (registry.resourceExists(path)) {
                    Resource oldResource = registry.get(path);
                    byte[] oldContent = (byte[])oldResource.getContent();
                    if (oldContent != null && RegistryUtils.decodeBytes(oldContent).equals(resourceContent)) {
                        // this will continue adding from the default path.
                        return;
                    }
                }
            } catch (Exception e) {
                String msg = "Error in comparing the policy content updates. policy path: " + path + ".";
                log.error(msg, e);
                throw new RegistryException(msg, e);
            }
            Object newContent = RegistryUtils.encodeString((String)resourceContent);
            if (newContent != null) {
                InputStream inputStream = new ByteArrayInputStream((byte[])newContent);
                addPolicyToRegistry(requestContext, inputStream);
            }
            ArtifactManager.getArtifactManager().getTenantArtifactRepository().addArtifact(path);
        } finally {
            CommonUtil.releaseUpdateLock();
        }
    }

    public void importResource(RequestContext requestContext) throws RegistryException {
        if (!CommonUtil.isUpdateLockAvailable()) {
            return;
        }
        CommonUtil.acquireUpdateLock();
        try {
            String sourceURL = requestContext.getSourceURL();
            InputStream inputStream;
            try {
                if (sourceURL != null && sourceURL.toLowerCase().startsWith("file:")) {
                    String msg = "The source URL must not be file in the server's local file system";
                    throw new RegistryException(msg);
                }
                inputStream = new URL(sourceURL).openStream();
            } catch (IOException e) {
                throw new RegistryException("The URL " + sourceURL + " is incorrect.", e);
            }
            addPolicyToRegistry(requestContext, inputStream);
        } finally {
            CommonUtil.releaseUpdateLock();
        }
    }

    private void addPolicyToRegistry(RequestContext requestContext, InputStream inputStream) throws RegistryException {
        Resource policyResource;
        if (requestContext.getResource() == null) {
            policyResource = new ResourceImpl();
            policyResource.setMediaType("application/policy+xml");
        } else {
            policyResource = requestContext.getResource();
        }
        String version = requestContext.getResource().getProperty(RegistryConstants.VERSION_PARAMETER_NAME);
        if (version == null) {
            version = CommonConstants.POLICY_VERSION_DEFAULT_VALUE;
            requestContext.getResource().setProperty(RegistryConstants.VERSION_PARAMETER_NAME, version);
        }
        Registry registry = requestContext.getRegistry();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int nextChar;
        try {
            while ((nextChar = inputStream.read()) != -1) {
                outputStream.write(nextChar);
            }
            outputStream.flush();
        } catch (IOException e) {
            throw new RegistryException("Exception occured while reading policy content", e);
        }

        try {
            AXIOMUtil.stringToOM(RegistryUtils.decodeBytes(outputStream.toByteArray())).toString();
        } catch (Exception e) {
            throw new RegistryException("The given policy file does not contain valid XML.");
        }

        String resourcePath = requestContext.getResourcePath().getPath();
        String policyFileName = resourcePath.substring(resourcePath.lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1);
        Registry systemRegistry = CommonUtil.getUnchrootedSystemRegistry(requestContext);
        RegistryContext registryContext = requestContext.getRegistryContext();
        String commonLocation = getChrootedLocation(registryContext);
        if (!systemRegistry.resourceExists(commonLocation)) {
            systemRegistry.put(commonLocation, systemRegistry.newCollection());
        }

        String policyPath;
        if (!resourcePath.startsWith(commonLocation)
                && !resourcePath.equals(RegistryUtils.getAbsolutePath(registryContext,
                RegistryConstants.PATH_SEPARATOR + policyFileName))
                && !resourcePath.equals(RegistryUtils.getAbsolutePath(registryContext,
                RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH
                        + RegistryConstants.PATH_SEPARATOR + policyFileName))) {
            policyPath = resourcePath;
        }else{
            policyPath = commonLocation + version + "/" +  extractResourceFromURL(policyFileName, ".xml");
        }


//        CommonUtil.addGovernanceArtifactEntryWithAbsoluteValues(
//                    CommonUtil.getUnchrootedSystemRegistry(requestContext),
//                    policyId, policyPath);

        String relativeArtifactPath = RegistryUtils.getRelativePath(registry.getRegistryContext(), policyPath);
        // adn then get the relative path to the GOVERNANCE_BASE_PATH
        relativeArtifactPath = RegistryUtils.getRelativePathToOriginal(relativeArtifactPath,
                RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);

        Resource newResource;
        if (registry.resourceExists(policyPath)) {
            newResource = registry.get(policyPath);
        } else {
            newResource = new ResourceImpl();
            Properties properties = policyResource.getProperties();
            if (properties != null) {
                List<String> linkProperties = Arrays.asList(
                        RegistryConstants.REGISTRY_LINK,
                        RegistryConstants.REGISTRY_USER,
                        RegistryConstants.REGISTRY_MOUNT,
                        RegistryConstants.REGISTRY_AUTHOR,
                        RegistryConstants.REGISTRY_MOUNT_POINT,
                        RegistryConstants.REGISTRY_TARGET_POINT,
                        RegistryConstants.REGISTRY_ACTUAL_PATH,
                        RegistryConstants.REGISTRY_REAL_PATH);
                for (Map.Entry<Object, Object> e : properties.entrySet()) {
                    String key = (String) e.getKey();
                    if (!linkProperties.contains(key)) {
                        newResource.setProperty(key, (List<String>) e.getValue());
                    }
                }
            }
        }
        newResource.setMediaType("application/policy+xml");
        if (policyResource.getProperty(CommonConstants.SOURCE_PROPERTY) == null){
            newResource.setProperty(CommonConstants.SOURCE_PROPERTY, CommonConstants.SOURCE_AUTO);
        }else {
            newResource.setProperty(CommonConstants.SOURCE_PROPERTY, policyResource.getProperty(CommonConstants.SOURCE_PROPERTY));
        }
        String policyId = policyResource.getUUID();
        if (policyId == null) {
            // generate a service id
            policyId = UUID.randomUUID().toString();
        }
        newResource.setUUID(policyId);
        newResource.setContent(outputStream.toByteArray());
        addPolicyToRegistry(requestContext, policyPath, requestContext.getSourceURL(),
                newResource, registry);
        ((ResourceImpl)newResource).setPath(relativeArtifactPath);

        String symlinkLocation = RegistryUtils.getAbsolutePath(registryContext,
                newResource.getProperty(RegistryConstants.SYMLINK_PROPERTY_NAME));
        if (symlinkLocation != null) {
            Resource resource = requestContext.getRegistry().get(symlinkLocation);
            if (resource != null) {
                String isLink = resource.getProperty("registry.link");
                String mountPoint = resource.getProperty("registry.mountpoint");
                String targetPoint = resource.getProperty("registry.targetpoint");
                String actualPath = resource.getProperty("registry.actualpath");
                if (isLink != null && mountPoint != null && targetPoint != null) {
                    symlinkLocation = actualPath + RegistryConstants.PATH_SEPARATOR;
                }
            }
            requestContext.getSystemRegistry().createLink(symlinkLocation + policyFileName, policyPath);
        }
        requestContext.setResource(newResource);
        requestContext.setProcessingComplete(true);
    }

    /**
     * Method that gets called instructing a policy to be added the registry.
     *
     * @param context  the request context for this request.
     * @param path     the path to add the resource to.
     * @param url      the path from which the resource was imported from.
     * @param resource the resource to be added.
     * @param registry the registry instance to use.
     *
     * @throws RegistryException if the operation failed.
     */
    protected void addPolicyToRegistry(RequestContext context, String path, String url,
                                       Resource resource, Registry registry) throws RegistryException {
        context.setActualPath(path);
        registry.put(path, resource);
    }

    private String extractResourceFromURL(String policyURL, String suffix) {
        String resourceName = policyURL;
        if (policyURL.lastIndexOf("?") > 0) {
            resourceName = policyURL.substring(0, policyURL.indexOf("?")) + suffix;
        } else if (policyURL.indexOf(".") > 0) {
            resourceName = policyURL.substring(0, policyURL.lastIndexOf(".")) + suffix;
        } else if (!policyURL.endsWith(suffix)) {
            resourceName = policyURL + suffix;
        }
        return resourceName;
    }

    private String getChrootedLocation(RegistryContext registryContext) {
        return RegistryUtils.getAbsolutePath(registryContext,
                RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + location);
    }
}
