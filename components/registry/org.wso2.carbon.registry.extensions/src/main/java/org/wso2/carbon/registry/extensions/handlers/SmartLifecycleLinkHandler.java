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
package org.wso2.carbon.registry.extensions.handlers;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.exceptions.ResourceNotFoundException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import javax.xml.namespace.QName;
import java.util.*;

@SuppressWarnings("unused")
public class SmartLifecycleLinkHandler extends Handler {

    private Map<String, String> states = new HashMap<String, String>();
    private String resourceKey = "service";

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public void setStates(OMElement locationConfiguration) throws RegistryException {
        Iterator confElements = locationConfiguration.getChildElements();
        while (confElements.hasNext()) {
            OMElement confElement = (OMElement)confElements.next();
            if (confElement.getQName().equals(new QName("state"))) {
                states.put(confElement.getAttributeValue(new QName("key")), confElement.getText());
            }
        }
    }

    public boolean resourceExists(RequestContext requestContext)
            throws RegistryException {
        try {
            get(requestContext);
        } catch (RegistryException e) {
            return false;
        }
        return true;
    }

    public void put(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
    }

    public void importResource(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
    }

    public void createLink(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
    }

    public void removeLink(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
    }

    public void delete(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
    }

    public void dump(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
    }

    public void restore(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
    }

    public String move(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
        return requestContext.getTargetPath();
    }

    public String copy(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
        return requestContext.getTargetPath();
    }

    public String rename(RequestContext requestContext) throws RegistryException {
        requestContext.setProcessingComplete(true);
        return requestContext.getTargetPath();
    }

    public Resource get(RequestContext requestContext) throws RegistryException {
        String path = requestContext.getResourcePath().getPath();
        String stateKey = RegistryUtils.getResourceName(path);
        if (!states.containsKey(stateKey)) {
            throw new ResourceNotFoundException("The given key, " + stateKey + " does not " +
                    "correspond to a lifecycle state.");
        }
        String servicePath = RegistryUtils.getParentPath(path);
        Registry registry = requestContext.getRegistry();
        for (String majorVersion : getSortedChildrenList(servicePath, registry)) {
            try {
                Integer.parseInt(RegistryUtils.getResourceName(majorVersion));
                for (String minorVersion : getSortedChildrenList(majorVersion, registry)) {
                    try {
                        Integer.parseInt(RegistryUtils.getResourceName(minorVersion));
                        for (String patchVersion : getSortedChildrenList(minorVersion, registry)) {
                            try {
                                Integer.parseInt(RegistryUtils.getResourceName(patchVersion));
                                String serviceResourcePath = patchVersion +
                                        RegistryConstants.PATH_SEPARATOR + resourceKey;
                                Resource resource = registry.get(serviceResourcePath);
                                for (Object propKey : resource.getProperties().keySet()) {
                                    if (((String)propKey).matches(
                                            "^registry[.]lifecycle.*[.]state$")) {
                                        if (states.get(stateKey).equals(
                                                resource.getProperty((String)propKey))) {
                                            resource.setProperty(RegistryConstants.REGISTRY_LINK,
                                                    "true");
                                            resource.setProperty(
                                                    RegistryConstants.REGISTRY_MOUNT_POINT,
                                                    path);
                                            resource.setProperty(
                                                    RegistryConstants.REGISTRY_TARGET_POINT,
                                                    serviceResourcePath);
                                            resource.setProperty(RegistryConstants.REGISTRY_AUTHOR,
                                                    resource.getAuthorUserName());
                                            resource.setProperty(
                                                    RegistryConstants.REGISTRY_ACTUAL_PATH,
                                                    serviceResourcePath);
                                            requestContext.setProcessingComplete(true);
                                            return resource;
                                        }
                                        break;
                                    }

                                }
                            } catch (NumberFormatException ignored) {
                                break;
                            }
                        }
                    } catch (NumberFormatException ignored) {
                        break;
                    }
                }
            } catch (NumberFormatException ignored) {
                break;
            }
        }
        throw new ResourceNotFoundException("No Resources found to be in the given " +
                "lifecycle state: " + stateKey);
    }

    private List<String> getSortedChildrenList(String servicePath, Registry registry)
            throws RegistryException {
        List<String> paths = Arrays.asList(((Collection) registry.get(servicePath)).getChildren());
        Collections.sort(paths, new Comparator<String>() {
            public int compare(String o1, String o2) {
                int n1, n2;
                try {
                    n1 = Integer.parseInt(RegistryUtils.getResourceName(o1));
                } catch (NumberFormatException ignored) {
                    return 1;
                }
                try {
                    n2 = Integer.parseInt(RegistryUtils.getResourceName(o2));
                } catch (NumberFormatException ignored) {
                    return -1;
                }
                return (n1 < n2) ? 1 : (n1 > n2) ? -1 : 0;
            }
        });
        return paths;
    }
}
