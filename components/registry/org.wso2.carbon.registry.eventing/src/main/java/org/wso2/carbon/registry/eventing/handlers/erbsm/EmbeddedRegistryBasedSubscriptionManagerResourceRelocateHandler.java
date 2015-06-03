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

import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.eventing.RegistryEventingConstants;
import org.wso2.carbon.registry.eventing.handlers.SubscriptionManagerHandler;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

public class EmbeddedRegistryBasedSubscriptionManagerResourceRelocateHandler extends SubscriptionManagerHandler {

    //TODO: Add Support to Handle Remotely Mounted Registries

    /*private static Log log = LogFactory.getLog(EmbeddedRegistryBasedSubscriptionManagerResourceRelocateHandler.class);

    public String move(RequestContext requestContext) throws RegistryException {
        log.debug("Started Moving subscription detail");
        String sourcePath = RegistryUtils.getRelativePath(requestContext.getRegistryContext(),
                requestContext.getSourcePath());
        String targetPath = RegistryUtils.getRelativePath(requestContext.getRegistryContext(),
                requestContext.getTargetPath());
        if (Utils.getRegistryService() == null) {
            return null;
        }
        Registry registry = Utils.getRegistryService().getConfigSystemRegistry();
        //Registry registry = requestContext.getRegistry();
        try {
            if (sourcePath == null || targetPath == null || registry == null ||
                    !registry.resourceExists(getSubStoreContext() +
                            RegistryEventingConstants.TOPIC_PREFIX + sourcePath)) {
                return null;
            }
        } catch (RegistryException e) {
            log.warn("Failed to Move subscription detail " + e.getMessage());
            return null;
        }
        Resource topicIndex = registry.get(getSubStoreContext() +
                EmbeddedRegistryBasedSubscriptionManager.TOPIC_INDEX);
        if (topicIndex == null) {
            log.warn("Topic Index Not Found");
            return null;
        }
        registry.move(getSubStoreContext() +
                RegistryEventingConstants.TOPIC_PREFIX + sourcePath,
                getSubStoreContext() +
                        RegistryEventingConstants.TOPIC_PREFIX + targetPath);
        Map<String, String> addMap = new HashMap<String, String>();
        boolean propertiesModified = false;
        Properties properties = topicIndex.getProperties();
        if (properties != null && properties.size() != 0) {
            for (Map.Entry<Object, Object> e : properties.entrySet()) {
                Object propertyList = e.getValue();
                if (propertyList == null || !(propertyList instanceof List)) {
                    continue;
                }
                Object name =  ((List)propertyList).get(0);
                if (name == null || !(name instanceof String) || !(e.getKey() instanceof String)) {
                    continue;
                }
                String key = (String)e.getKey();
                if (RegistryUtils.isHiddenProperty(key)) {
                    continue;
                }
                String topic = (String)name;
                if (topic.startsWith(RegistryEventingConstants.TOPIC_PREFIX + sourcePath)) {
                    if (log.isDebugEnabled()) {
                        String newTopic = RegistryEventingConstants.TOPIC_PREFIX + targetPath + topic.substring(
                                (RegistryEventingConstants.TOPIC_PREFIX + sourcePath).length());
                        log.debug("Changed topic from: " + topic + " to: " + newTopic);
                    }
                    topic = RegistryEventingConstants.TOPIC_PREFIX + targetPath + topic.substring(
                            (RegistryEventingConstants.TOPIC_PREFIX + sourcePath).length());
                    String uuid = (String)key;
                    String subPath = getSubStoreContext() + topic + RegistryConstants.PATH_SEPARATOR + uuid;
                    Resource newResource = registry.get(subPath);
                    if (newResource != null) {
                        String filterValue = newResource.getProperty(EmbeddedRegistryBasedSubscriptionManager.FILTER_VALUE);
                        filterValue = filterValue.replace(sourcePath, targetPath);
                        newResource.setProperty(EmbeddedRegistryBasedSubscriptionManager.FILTER_VALUE, filterValue);
                        registry.put(subPath, newResource);
                    }
                    addMap.put((String)key, topic);
                    propertiesModified = true;
                }
            }
        }
        if (!propertiesModified) {
            log.warn("Stale entry found at path: " + getSubStoreContext() +
                    RegistryEventingConstants.TOPIC_PREFIX + sourcePath + " with no index");
            return null;
        }
        if (addMap.size() > 0) {
            Set<Map.Entry<String, String>> entries = addMap.entrySet();
            for (Map.Entry<String, String> e : entries) {
                topicIndex.setProperty(e.getKey(), e.getValue());
            }
        }
        registry.put(getSubStoreContext() +
                EmbeddedRegistryBasedSubscriptionManager.TOPIC_INDEX, topicIndex);
        if (log.isDebugEnabled()) {
            log.debug("Moved subscription detail from: " + getSubStoreContext() +
                    RegistryEventingConstants.TOPIC_PREFIX + sourcePath +
                    " to: " + getSubStoreContext() +
                    RegistryEventingConstants.TOPIC_PREFIX + targetPath);
        }
        return null;
    }

    public String copy(RequestContext requestContext) throws RegistryException {
        log.debug("Started Copying subscription detail");
        String uuid = UUIDGenerator.getUUID();
        String sourcePath = RegistryUtils.getRelativePath(requestContext.getRegistryContext(),
                requestContext.getSourcePath());
        String targetPath = RegistryUtils.getRelativePath(requestContext.getRegistryContext(),
                requestContext.getTargetPath());
        if (Utils.getRegistryService() == null) {
            return null;
        }
        Registry registry = Utils.getRegistryService().getConfigSystemRegistry();
        //Registry registry = requestContext.getRegistry();
        try {
            if (sourcePath == null || targetPath == null || registry == null ||
                    !registry.resourceExists(getSubStoreContext() +
                            RegistryEventingConstants.TOPIC_PREFIX + sourcePath)) {
                return null;
            }
        } catch (RegistryException e) {
            log.warn("Failed to Copy subscription detail " + e.getMessage());
            return null;
        }
        Resource topicIndex = registry.get(getSubStoreContext() +
                EmbeddedRegistryBasedSubscriptionManager.TOPIC_INDEX);
        if (topicIndex == null) {
            log.warn("Topic Index Not Found");
            return null;
        }
        registry.copy(getSubStoreContext() +
                RegistryEventingConstants.TOPIC_PREFIX + sourcePath,
                getSubStoreContext() +
                        RegistryEventingConstants.TOPIC_PREFIX + targetPath);
        if (log.isDebugEnabled()) {
            log.debug("Copied subscription detail from: " + getSubStoreContext() +
                    RegistryEventingConstants.TOPIC_PREFIX + sourcePath +
                    " to: " + getSubStoreContext() +
                    RegistryEventingConstants.TOPIC_PREFIX + targetPath);
        }
        Map<String, String> addMap = new HashMap<String, String>();
        boolean propertiesModified = false;
        Properties properties = topicIndex.getProperties();
        if (properties != null && properties.size() != 0) {
            for (Map.Entry<Object, Object> e : properties.entrySet()) {
                Object propertyList = e.getValue();
                if (propertyList == null || !(propertyList instanceof List)) {
                    continue;
                }
                Object name =  ((List)propertyList).get(0);
                if (name == null || !(name instanceof String) || !(e.getKey() instanceof String)) {
                    continue;
                }
                String key = (String)e.getKey();
                if (RegistryUtils.isHiddenProperty(key)) {
                    continue;
                }
                String topic = (String)name;
                if (topic.startsWith(RegistryEventingConstants.TOPIC_PREFIX + sourcePath)) {
                    topic = RegistryEventingConstants.TOPIC_PREFIX + targetPath + topic.substring(
                            (RegistryEventingConstants.TOPIC_PREFIX + sourcePath).length());
                    String oldUUID = (String)key;
                    String oldSubPath = getSubStoreContext() + topic + RegistryConstants.PATH_SEPARATOR + oldUUID;
                    String newSubPath = getSubStoreContext() + topic + RegistryConstants.PATH_SEPARATOR + uuid;
                    registry.move(oldSubPath, newSubPath);
                    addMap.put(uuid, topic);
                    log.debug("Added subscription entry under key: " + uuid);
                    Resource newResource = registry.get(newSubPath);
                    if (newResource != null) {
                        String filterValue = newResource.getProperty(EmbeddedRegistryBasedSubscriptionManager.FILTER_VALUE);
                        filterValue = filterValue.replace(sourcePath, targetPath);
                        newResource.setProperty(EmbeddedRegistryBasedSubscriptionManager.FILTER_VALUE, filterValue);
                        registry.put(newSubPath, newResource);
                    }
                    uuid = UUIDGenerator.getUUID();
                    propertiesModified = true;
                }
            }
        }
        if (!propertiesModified) {
            log.warn("Stale entry found at path: " + getSubStoreContext() +
                            RegistryEventingConstants.TOPIC_PREFIX + sourcePath + " with no index");
            return null;
        }
        if (addMap.size() > 0) {
            Set<Map.Entry<String, String>> entries = addMap.entrySet();
            for (Map.Entry<String, String> e : entries) {
                topicIndex.setProperty(e.getKey(), e.getValue());
            }
        }
        return null;
    }

    public String rename(RequestContext requestContext) throws RegistryException {
        log.debug("Started Renaming subscription detail");
        String sourcePath = RegistryUtils.getRelativePath(requestContext.getRegistryContext(),
                requestContext.getSourcePath());
        String targetPath = RegistryUtils.getRelativePath(requestContext.getRegistryContext(),
                requestContext.getTargetPath());
        if (Utils.getRegistryService() == null) {
            return null;
        }
        Registry registry = Utils.getRegistryService().getConfigSystemRegistry();
        //Registry registry = requestContext.getRegistry();
        try {
            if (sourcePath == null || targetPath == null || registry == null ||
                    !registry.resourceExists(getSubStoreContext() +
                            RegistryEventingConstants.TOPIC_PREFIX + sourcePath)) {
                return null;
            }
        } catch (RegistryException e) {
            log.warn("Failed to Rename subscription detail " + e.getMessage());
            return null;
        }
        Resource topicIndex = registry.get(getSubStoreContext() +
                EmbeddedRegistryBasedSubscriptionManager.TOPIC_INDEX);
        if (topicIndex == null) {
            log.warn("Topic Index Not Found");
            return null;
        }
        registry.rename(getSubStoreContext() +
                RegistryEventingConstants.TOPIC_PREFIX + sourcePath,
                getSubStoreContext() +
                        RegistryEventingConstants.TOPIC_PREFIX + targetPath);
        Map<String, String> addMap = new HashMap<String, String>();
        boolean propertiesModified = false;
        Properties properties = topicIndex.getProperties();
        if (properties != null && properties.size() != 0) {
            for (Map.Entry<Object, Object> e : properties.entrySet()) {
                Object propertyList = e.getValue();
                if (propertyList == null || !(propertyList instanceof List)) {
                    continue;
                }
                Object name =  ((List)propertyList).get(0);
                if (name == null || !(name instanceof String) || !(e.getKey() instanceof String)) {
                    continue;
                }
                String key = (String)e.getKey();
                if (RegistryUtils.isHiddenProperty(key)) {
                    continue;
                }
                String topic = (String)name;
                if (topic.startsWith(RegistryEventingConstants.TOPIC_PREFIX + sourcePath)) {
                    if (log.isDebugEnabled()) {
                        String newTopic = RegistryEventingConstants.TOPIC_PREFIX + targetPath + topic.substring(
                                (RegistryEventingConstants.TOPIC_PREFIX + sourcePath).length());
                        log.debug("Changed topic from: " + topic + " to: " + newTopic);
                    }
                    topic = RegistryEventingConstants.TOPIC_PREFIX + targetPath + topic.substring(
                            (RegistryEventingConstants.TOPIC_PREFIX + sourcePath).length());
                    String uuid = (String)key;
                    String subPath = getSubStoreContext() + topic + RegistryConstants.PATH_SEPARATOR + uuid;
                    Resource newResource = registry.get(subPath);
                    if (newResource != null) {
                        String filterValue = newResource.getProperty(EmbeddedRegistryBasedSubscriptionManager.FILTER_VALUE);
                        filterValue = filterValue.replace(sourcePath, targetPath);
                        newResource.setProperty(EmbeddedRegistryBasedSubscriptionManager.FILTER_VALUE, filterValue);
                        registry.put(subPath, newResource);
                    }
                    addMap.put((String)key, topic);
                    propertiesModified = true;
                }
            }
        }
        if (!propertiesModified) {
            log.warn("Stale entry found at path: " + getSubStoreContext() +
                    RegistryEventingConstants.TOPIC_PREFIX + sourcePath + " with no index");
            return null;
        }
        if (addMap.size() > 0) {
            Set<Map.Entry<String, String>> entries = addMap.entrySet();
            for (Map.Entry<String, String> e : entries) {
                topicIndex.setProperty(e.getKey(), e.getValue());
            }
        }
        registry.put(getSubStoreContext() +
                EmbeddedRegistryBasedSubscriptionManager.TOPIC_INDEX, topicIndex);
        if (log.isDebugEnabled()) {
            log.debug("Renamed subscription detail from: " + getSubStoreContext() +
                    RegistryEventingConstants.TOPIC_PREFIX + sourcePath +
                    " to: " + getSubStoreContext() +
                    RegistryEventingConstants.TOPIC_PREFIX + targetPath);
        }
        return null;
    }
    
    public void delete(RequestContext requestContext) throws RegistryException {
        log.debug("Started Deleting subscription detail");
        String path = RegistryUtils.getRelativePath(requestContext.getRegistryContext(),
                requestContext.getResourcePath().getPath());
        if (Utils.getRegistryService() == null) {
            return;
        }
        Registry registry = Utils.getRegistryService().getConfigSystemRegistry();
        //Registry registry = requestContext.getRegistry();
        try {
            if (path == null || registry == null ||
                    !registry.resourceExists(getSubStoreContext() +
                            RegistryEventingConstants.TOPIC_PREFIX + path)) {
                return;                                                              
            }
        } catch (RegistryException e) {
            log.warn("Failed to Delete subscription detail " + e.getMessage());
            return;
        }
        Resource topicIndex = registry.get(getSubStoreContext() +
                EmbeddedRegistryBasedSubscriptionManager.TOPIC_INDEX);
        if (topicIndex == null) {
            log.warn("Topic Index Not Found");
            return;
        }
        boolean propertiesModified = false;
        Properties properties = topicIndex.getProperties();
        List<String> removeList = new LinkedList<String>();
        if (properties != null && properties.size() != 0) {
            for (Map.Entry<Object, Object> e : properties.entrySet()) {
                Object propertyList = e.getValue();
                if (propertyList == null || !(propertyList instanceof List)) {
                    continue;
                }
                Object name =  ((List)propertyList).get(0);
                if (name == null || !(name instanceof String) || !(e.getKey() instanceof String)) {
                    continue;
                }
                String key = (String)e.getKey();
                if (RegistryUtils.isHiddenProperty(key)) {
                    continue;
                }
                String topic = (String)name;
                if (topic.startsWith(RegistryEventingConstants.TOPIC_PREFIX + path)) {
                    log.debug("Deleted topic: " + topic);
                    removeList.add((String)key);
                    propertiesModified = true;
                }
            }
        }
        if (!propertiesModified) {
            log.warn("Stale entry found at path: " + getSubStoreContext() +
                            RegistryEventingConstants.TOPIC_PREFIX + path + " with no index");
            return;
        }
        if (removeList.size() > 0) {
            for(String key : removeList) {
                topicIndex.removeProperty(key);
            }
        }
        registry.put(getSubStoreContext() +
                EmbeddedRegistryBasedSubscriptionManager.TOPIC_INDEX, topicIndex);
        registry.delete(getSubStoreContext() +
                RegistryEventingConstants.TOPIC_PREFIX + path);
        if (log.isDebugEnabled()) {
            log.debug("Removed subscription detail from: " + getSubStoreContext() +
                    RegistryEventingConstants.TOPIC_PREFIX + path);
        }
    }*/
}
