/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.eventing.handlers.erbsm;

import org.wso2.carbon.event.core.util.EventBrokerConstants;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.core.config.RemoteConfiguration;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.eventing.handlers.SubscriptionManagerHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.MalformedURLException;
import java.util.Map;
import java.util.Properties;
import java.util.List;

public class EmbeddedRegistryBasedSubscriptionManagerMountHandler extends SubscriptionManagerHandler {

    //TODO: Add Support for Get/Delete

    private static Log log = LogFactory.getLog(EmbeddedRegistryBasedSubscriptionManagerMountHandler.class);

    /*public void put(RequestContext requestContext) throws RegistryException {
        String path = requestContext.getResourcePath().getPath();
        log.debug("Got put request for path: " + RegistryUtils.getAbsolutePath(requestContext.getRegistryContext(), path));
        if (!path.startsWith(getSubStoreContext())) {
            return;
        }
        if (path.equals(getSubStoreContext() + EmbeddedRegistryBasedSubscriptionManager.TOPIC_INDEX)) {
            Resource resource = requestContext.getRepository().get(path);
            if (resource != null) {
            *//* if (resource == null) {
                Resource newResource = requestContext.getResource();
                Properties properties = newResource.getProperties();
                if (properties != null && properties.size() != 0) {
                    for (Object key : properties.keySet()) {
                        Object propertyList = properties.get(key);
                        if (propertyList == null || !(propertyList instanceof List)) {
                            continue;
                        }
                        Object name =  ((List)propertyList).get(0);
                        if (name == null || !(name instanceof String)) {
                            continue;
                        }
                        String topic = (String)name;
                        String trailer = topic.substring(topic.length() - RegistryConstants.PATH_SEPARATOR.length() -
                                EventBrokerConstants.EB_CONF_WS_SUBSCRIPTION_COLLECTION_NAME.length());
                        topic = topic.substring(0, topic.length() - RegistryConstants.PATH_SEPARATOR.length() -
                                EventBrokerConstants.EB_CONF_WS_SUBSCRIPTION_COLLECTION_NAME.length());
                        log.debug("Found topic: " + topic);
                        trailer = topic.substring(topic.lastIndexOf(RegistryConstants.PATH_SEPARATOR)) + trailer;
                        String relPath = topic.substring(0, topic.lastIndexOf(RegistryConstants.PATH_SEPARATOR));
                        Resource subscribedResource = requestContext.getRegistry().get(relPath);
                        if (subscribedResource != null) {
                            String isLink = subscribedResource.getProperty("registry.link");
                            String realPath = subscribedResource.getProperty("registry.realpath");
                            String userName = subscribedResource.getProperty("registry.user");
                            if (isLink != null && userName != null && realPath != null &&
                                    realPath.indexOf("/resourceContent?path=") > 0) {
                                String url = realPath.substring(0, realPath.indexOf("/resourceContent?path="));
                                String remoteName = realPath.substring(realPath.indexOf("/resourceContent?path=") +
                                    "/resourceContent?path=".length()) + trailer;
                                log.debug("Connecting to Remote Registry at: " + url);
                                log.debug("Remote Registry User: " + userName);
                                updateRemoteTopicIndex(requestContext, key, remoteName, userName, url);
                                log.debug("Updated Remote Topic Index with entry: " + remoteName);
                            }
                        }
                    }
                }
            }  else { *//*
                Resource newResource = requestContext.getResource();
                Properties properties = newResource.getProperties();
                Properties oldProperties = resource.getProperties();
                if (properties != null && properties.size() != 0) {
                    for (Map.Entry<Object, Object> e : properties.entrySet()) {
                        if (oldProperties.get(e.getKey()) != null) {
                            continue;
                        }
                        Object propertyList = e.getValue();
                        if (propertyList == null || !(propertyList instanceof List)) {
                            continue;
                        }
                        Object name =  ((List)propertyList).get(0);
                        if (name == null || !(name instanceof String)) {
                            continue;
                        }
                        String topic = (String)name;
                        String trailer = topic.substring(topic.length() - RegistryConstants.PATH_SEPARATOR.length() -
                                EventBrokerConstants.EB_CONF_WS_SUBSCRIPTION_COLLECTION_NAME.length());
                        topic = topic.substring(0, topic.length() - RegistryConstants.PATH_SEPARATOR.length() -
                                EventBrokerConstants.EB_CONF_WS_SUBSCRIPTION_COLLECTION_NAME.length());
                        log.debug("Found topic: " + topic);
                        trailer = topic.substring(topic.lastIndexOf(RegistryConstants.PATH_SEPARATOR)) + trailer;
                        String relPath = topic.substring(0, topic.lastIndexOf(RegistryConstants.PATH_SEPARATOR));
                        Resource subscribedResource = requestContext.getRegistry().get(relPath);
                        if (subscribedResource != null) {
                            String isLink = subscribedResource.getProperty("registry.link");
                            String realPath = subscribedResource.getProperty("registry.realpath");
                            String userName = subscribedResource.getProperty("registry.user");
                            if (isLink != null && userName != null && realPath != null &&
                                    realPath.indexOf("/resourceContent?path=") > 0) {
                                String url = realPath.substring(0, realPath.indexOf("/resourceContent?path="));
                                String remoteName = realPath.substring(realPath.indexOf("/resourceContent?path=") +
                                    "/resourceContent?path=".length()) + trailer;
                                log.debug("Connecting to Remote Registry at: " + url);
                                log.debug("Remote Registry User: " + userName);
                                updateRemoteTopicIndex(requestContext,
                                        e.getKey(), remoteName, userName, url);
                                log.debug("Updated Remote Topic Index with entry: " + remoteName);
                            }
                        }
                    }
                }
            }
        } else {
            try {
                Resource existingResource = requestContext.getRegistry().get(path);
                if (existingResource != null) {
                    return;
                }
            } catch (RegistryException e) {}
            // Do nothing, as if exception occurs - the resource is not there.
            if (!path.contains(EventBrokerConstants.EB_CONF_WS_SUBSCRIPTION_COLLECTION_NAME)) {
                return;
            }            
            String resourcePath = path.substring(getSubStoreContext().length());
            String trailer = resourcePath.substring(resourcePath.indexOf(RegistryConstants.PATH_SEPARATOR +
                    EventBrokerConstants.EB_CONF_WS_SUBSCRIPTION_COLLECTION_NAME));
            resourcePath = resourcePath.substring(0, resourcePath.indexOf(RegistryConstants.PATH_SEPARATOR +
                    EventBrokerConstants.EB_CONF_WS_SUBSCRIPTION_COLLECTION_NAME));
            trailer = resourcePath.substring(resourcePath.lastIndexOf(RegistryConstants.PATH_SEPARATOR)) + trailer;
            resourcePath = resourcePath.substring(0, resourcePath.lastIndexOf(RegistryConstants.PATH_SEPARATOR)); 
            Resource subscribedResource = requestContext.getRegistry().get(resourcePath);
            if (subscribedResource != null) {
                String isLink = subscribedResource.getProperty("registry.link");
                String realPath = subscribedResource.getProperty("registry.realpath");
                String userName = subscribedResource.getProperty("registry.user");
                if (isLink != null && userName != null && realPath != null &&
                        realPath.indexOf("/resourceContent?path=") > 0) {
                    String url = realPath.substring(0, realPath.indexOf("/resourceContent?path="));
                    String remotePath = realPath.substring(realPath.indexOf("/resourceContent?path=") +
                            "/resourceContent?path=".length()) + trailer;
                    if (!remotePath.startsWith(RegistryConstants.PATH_SEPARATOR)) {
                        remotePath = RegistryConstants.PATH_SEPARATOR + remotePath;
                    }
                    log.debug("Connecting to Remote Registry at: " + url);
                                log.debug("Remote Registry User: " + userName);
                    addResource(requestContext, remotePath, userName, url);
                }
            }
        }
    }

    public void delete(RequestContext requestContext) throws RegistryException {
        
    }

    private void updateRemoteTopicIndex(RequestContext requestContext, Object key, Object value,
                                        String userName, String url) throws RegistryException {
        List remoteInstances = requestContext.getRegistry().getRegistryContext().getRemoteInstances();
        boolean instanceFound = false;
        RemoteConfiguration config = null;
        for (int i = 0; i < remoteInstances.size(); i++) {
            config = (RemoteConfiguration) remoteInstances.get(i);
            if (config.getUrl().equals(url) && config.getTrustedUser().equals(userName)) {
                instanceFound = true;
                break;
            }
        }
        if (!instanceFound) {
            String msg = "Target mount point not found.";
            log.error(msg);
            throw new RegistryException(msg);
        }
        RegistryUtils.setTrustStoreSystemProperties();
        try {
            RemoteRegistry remoteRegistry = new RemoteRegistry(url, userName, config.getTrustedPwd());
            String remoteSubStoreContext = getSubStoreContext();
            Resource tempResource = remoteRegistry.get(remoteSubStoreContext +
                    EmbeddedRegistryBasedSubscriptionManager.TOPIC_INDEX);
            if (tempResource != null) {
                tempResource.setProperty((String)key, (String)value);
                remoteRegistry.put(remoteSubStoreContext +
                    EmbeddedRegistryBasedSubscriptionManager.TOPIC_INDEX, tempResource);
            }
        } catch (RegistryException e) {
            if(log.isWarnEnabled()) {
                log.warn("Could not mount remote instance: " + url, e);
            }
        } catch (MalformedURLException e) {
            if(log.isWarnEnabled()) {
                log.warn("Could not mount remote instance: " + url, e);
            }
        }
    }

    private void addResource(RequestContext requestContext, String remotePath,
                             String userName, String url) throws RegistryException {
        List remoteInstances = requestContext.getRegistry().getRegistryContext().getRemoteInstances();
        boolean instanceFound = false;
        RemoteConfiguration config = null;
        for (int i = 0; i < remoteInstances.size(); i++) {
            config = (RemoteConfiguration) remoteInstances.get(i);
            if (config.getUrl().equals(url) && config.getTrustedUser().equals(userName)) {
                instanceFound = true;
                break;
            }
        }
        if (!instanceFound) {
            String msg = "Target mount point not found.";
            log.error(msg);
            throw new RegistryException(msg);
        }
        RegistryUtils.setTrustStoreSystemProperties();
        try {
            RemoteRegistry remoteRegistry = new RemoteRegistry(url, userName, config.getTrustedPwd());
            String remoteSubStoreContext = getSubStoreContext();
            //No need to bother about Notifications, as these will be handled by the remote instance.
            remotePath = remoteSubStoreContext + remotePath;
            remoteRegistry.put(remotePath, requestContext.getResource());
            log.debug("Added Resource at Remote Path: " + remotePath);
            //TODO: Link Won't Show up in UI??
            String requestPath = requestContext.getResourcePath().getPath();
            log.debug("Creating link to: " + remotePath + " at: " + requestPath);
            requestContext.getRegistry().createLink(requestPath, config.getId(), remotePath);
            requestContext.setProcessingComplete(true);
        } catch (Exception e) {
            if(log.isWarnEnabled()) {
                log.warn("Could not mount remote instance: " + e.getMessage());
            }

        }
    }*/

}
