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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.AuthorizationUtils;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.handlers.utils.EndpointUtils;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;
import org.wso2.carbon.user.mgt.UserMgtConstants;

import javax.xml.namespace.QName;
import java.util.*;

public class EndpointMediaTypeHandler extends Handler {
    private static final Log log = LogFactory.getLog(EndpointMediaTypeHandler.class);

    private static final String LOCATION_TAG = "location";
    OMElement endpointLocationElement;

    public void setEndpointLocationConfiguration(OMElement endpointLocationElement) throws RegistryException {
        String endpointLocation = null;
        Iterator configElements = endpointLocationElement.getChildElements();
        while (configElements.hasNext()) {
            OMElement configElement = (OMElement)configElements.next();
            if (configElement.getQName().equals(new QName(LOCATION_TAG))) {
                endpointLocation = configElement.getText();
                if (!endpointLocation.startsWith(RegistryConstants.PATH_SEPARATOR)) {
                    endpointLocation = RegistryConstants.PATH_SEPARATOR + endpointLocation;
                }
                if (!endpointLocation.endsWith(RegistryConstants.PATH_SEPARATOR)) {
                    endpointLocation = endpointLocation + RegistryConstants.PATH_SEPARATOR;
                }
            }
        }
        EndpointUtils.setEndpointLocation(endpointLocation); 

        this.endpointLocationElement = endpointLocationElement;
    }

    public OMElement getEndpointLocationConfiguration() {
        return endpointLocationElement;
    }

    public void setEndpointMediaType(String endpointMediaType) throws RegistryException {
        EndpointUtils.setEndpointMediaType(endpointMediaType);
    }

    public String getEndpointMediaType() throws RegistryException {
        return EndpointUtils.getEndpointMediaType();   
    }

    public void put(RequestContext requestContext) throws RegistryException {
        if (!CommonUtil.isUpdateLockAvailable()) {
            return;
        }
        CommonUtil.acquireUpdateLock();
        try {
            // we are not allowing to update the resource content for any reason, but they can just update properties
            Registry registry = requestContext.getRegistry();
            Resource resource = requestContext.getResource();

            Object resourceContentObj = resource.getContent();
            String resourceContent; // here the resource content is url
            if (resourceContentObj instanceof String) {
                resourceContent = (String)resourceContentObj;
                resource.setContent(RegistryUtils.encodeString(resourceContent));
            } else {
                resourceContent = RegistryUtils.decodeBytes((byte[])resourceContentObj);
            }

            String endpointUrl = EndpointUtils.deriveEndpointFromContent(resourceContent);
            String urlToPath = EndpointUtils.deriveEndpointFromUrl(endpointUrl);

            // so here the absolute path.
            String basePath = RegistryUtils.getAbsolutePath(registry.getRegistryContext(),
                    org.wso2.carbon.registry.core.RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                    EndpointUtils.getEndpointLocation());
            if(basePath.endsWith(RegistryConstants.PATH_SEPARATOR)){
                if(urlToPath.startsWith(RegistryConstants.PATH_SEPARATOR)){
                    urlToPath = urlToPath.replaceFirst(RegistryConstants.PATH_SEPARATOR,"");
                }
            }else{
                if(!urlToPath.startsWith(RegistryConstants.PATH_SEPARATOR)){
                    urlToPath = RegistryConstants.PATH_SEPARATOR + urlToPath;
                }
            }
            String path = basePath + urlToPath;

            String endpointId = resource.getUUID();
            if (registry.resourceExists(path)) {
                Resource oldResource = registry.get(path);
                //Set the old resource properties to new resource
                //https://wso2.org/jira/browse/REGISTRY-799
                Properties properties = oldResource.getProperties();
                if (properties != null) {
                    Set keySet = properties.keySet();
                    if (keySet != null) {
                        for (Object keyObj : keySet) {
                            String key = (String) keyObj;
                            List values = (List) properties.get(key);
                            if (values != null) {
                                for (Object valueObj : values) {
                                    String value = (String) valueObj;
                                    resource.addProperty(key, value);
                                }
                            }
                        }
                    }
                }
                byte[] oldContent = (byte[])oldResource.getContent();
                if (oldContent != null && !RegistryUtils.decodeBytes(oldContent).equals(resourceContent)) {
                    // oops somebody trying to update the endpoint resource content. that should not happen
//                    String msg = "Endpoint content for endpoint resource is not allowed to change, " +
//                            "path: " + path + ".";
//                    log.error(msg);
//                    throw new RegistryException(msg);
                    //This is for fixing REGISTRY-785.
                    resource.setContent(RegistryUtils.encodeString(resourceContent));
                }
            } else if (endpointId == null) {
                endpointId = UUID.randomUUID().toString();
                resource.setUUID(endpointId);
            }

//            CommonUtil.addGovernanceArtifactEntryWithAbsoluteValues(
//                    CommonUtil.getUnchrootedSystemRegistry(requestContext),
//                    endpointId, path);

            String relativeArtifactPath = RegistryUtils.getRelativePath(registry.getRegistryContext(), path);
            // adn then get the relative path to the GOVERNANCE_BASE_PATH
            relativeArtifactPath = RegistryUtils.getRelativePathToOriginal(relativeArtifactPath,
                    RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);
            Registry systemRegistry = CommonUtil.getUnchrootedSystemRegistry(requestContext);
            if (!systemRegistry.resourceExists(basePath)) {
                systemRegistry.put(basePath, systemRegistry.newCollection());
            }
            registry.put(path, resource);

//            if (!(resource instanceof Collection) &&
//               ((ResourceImpl) resource).isVersionableChange()) {
//                registry.createVersion(path);
//            }
            ((ResourceImpl)resource).setPath(relativeArtifactPath);
            requestContext.setActualPath(path);
            requestContext.setProcessingComplete(true);
        } finally {
            CommonUtil.releaseUpdateLock();
        }
    }

    public String rename(RequestContext requestContext) throws RegistryException {
        return move(requestContext);
    }

    public String move(RequestContext requestContext) throws RegistryException {
        Registry registry = requestContext.getRegistry();
        String sourcePath = requestContext.getSourcePath();
        checkEndpointDependency(registry, sourcePath);
        return requestContext.getTargetPath();
    }

    public void delete(RequestContext requestContext) throws RegistryException {
        if (!CommonUtil.isDeleteLockAvailable()) {
            return;
        }
        CommonUtil.acquireDeleteLock();

        Registry registry = requestContext.getRegistry();
        String path = requestContext.getResourcePath().getPath();

        try {
            if (path == null) {
                throw new RegistryException("The resource path is not available.");
            }
            checkEndpointDependency(registry, path);
//            Resource resource = registry.get(path);
        } finally {
            CommonUtil.releaseDeleteLock();
        }
    }

    public void checkEndpointDependency(Registry registry, String path) throws RegistryException {

        // here we are getting the associations for the endpoint.
        Association[] endpointDependents = registry.getAssociations(path, CommonConstants.USED_BY);

        // for each endpoint we are checking what the resource type is, if it is service, we check the
        // endpoint service, if it is wsdl, we check the wsdl
        List<String> dependents = new ArrayList<String>();
        for (Association endpointDependent: endpointDependents) {
            String targetPath = endpointDependent.getDestinationPath();
            if (registry.resourceExists(targetPath)) {
                Resource targetResource = registry.get(targetPath);

                String mediaType = targetResource.getMediaType();
                if (CommonConstants.WSDL_MEDIA_TYPE.equals(mediaType)) {
                    // so there are dependencies for wsdl media
                    dependents.add(targetPath);
                } else if ((CommonConstants.SERVICE_MEDIA_TYPE.equals(mediaType))) {
                    dependents.add(targetPath);
                }
            }
        }
        if (dependents.size() > 0) {
            // so there are dependencies, we are not allowing to delete endpoints if there are dependents
            String msg = "Error in deleting the endpoint resource. Please make sure detach the associations " +
                    "to the services and wsdls manually before deleting the endpoint. " +
                    "endpoint path: " + path + ".";

            log.error(msg);
            throw new RegistryException(msg);
        }
    }

    // adding the association should have a different lock    

    public void addAssociation(RequestContext requestContext) throws RegistryException {
        if (!CommonUtil.isAddingAssociationLockAvailable()) {
            return;
        }
        CommonUtil.acquireAddingAssociationLock();
        try {
            // here whenever a service is associated to a endpoint it will add the endpoint entry
            String targetPath = requestContext.getTargetPath();
            String sourcePath = requestContext.getSourcePath();

            Registry registry = requestContext.getRegistry();

            // get the target resource.
            Resource targetResource = registry.get(targetPath);
            if (CommonConstants.SERVICE_MEDIA_TYPE.equals(targetResource.getMediaType()) &&
                    CommonConstants.USED_BY.equals(requestContext.getAssociationType())) {
                // if so we are getting the service and add the endpoint to the source
                Resource sourceResource = registry.get(sourcePath);
                byte[] sourceContent = (byte[])sourceResource.getContent();
                if (sourceContent == null) {
                    return;
                }
                String endpointUrl = EndpointUtils.
                        deriveEndpointFromContent(RegistryUtils.decodeBytes(sourceContent));
                String endpointEnv = sourceResource.getProperty(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR);
                if (endpointEnv == null) {
                    endpointEnv = "";
                }
                if (endpointEnv.indexOf(",") > 0) {
                    for (String env : endpointEnv.split(",")) {
                        EndpointUtils.addEndpointToService(registry, targetPath, endpointUrl, env);
                    }
                } else {
                    EndpointUtils.addEndpointToService(registry, targetPath, endpointUrl, endpointEnv);
                }
            }
        } finally {
            CommonUtil.releaseAddingAssociationLock();
        }
    }

    public void removeAssociation(RequestContext requestContext) throws RegistryException {
        if (!CommonUtil.isAddingAssociationLockAvailable()) {
            return;
        }
        CommonUtil.acquireAddingAssociationLock();
        try {
            // here whenever a service is associated to a endpoint it will add the endpoint entry
            String targetPath = requestContext.getTargetPath();
            String sourcePath = requestContext.getSourcePath();

            Registry registry = requestContext.getRegistry();

            // get the target resource.
            Resource targetResource = registry.get(targetPath);
            if (CommonConstants.SERVICE_MEDIA_TYPE.equals(targetResource.getMediaType()) &&
                    CommonConstants.USED_BY.equals(requestContext.getAssociationType())) {
                // if so we are getting the service and add the endpoint to the source
                Resource sourceResource = registry.get(sourcePath);
                byte[] sourceContent = (byte[])sourceResource.getContent();
                if (sourceContent == null) {
                    return;
                }
                String endpointUrl = EndpointUtils.
                        deriveEndpointFromContent(RegistryUtils.decodeBytes(sourceContent));
                String endpointEnv = sourceResource.getProperty(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR);
                if (endpointEnv == null) {
                    endpointEnv = "";
                }
                if (endpointEnv.indexOf(",") > 0) {
                    for (String env : endpointEnv.split(",")) {
                        EndpointUtils.removeEndpointFromService(registry, targetPath, endpointUrl, env);
                    }
                } else {
                    EndpointUtils.removeEndpointFromService(registry, targetPath, endpointUrl, endpointEnv);
                }
            }
        } finally {
            CommonUtil.releaseAddingAssociationLock();
        }
    }
}
