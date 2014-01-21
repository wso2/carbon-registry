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
package org.wso2.carbon.registry.handler.util;

import org.apache.axiom.om.*;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.SimulationService;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.jdbc.handlers.HandlerLifecycleManager;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.filters.Filter;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.config.RegistryConfigurationProcessor;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.handler.beans.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.namespace.QName;
import java.io.*;
import java.util.*;

public class CommonUtil {

    private static final Log log = LogFactory.getLog(CommonUtil.class);
    private static String contextRoot = null;
    private static RegistryService registryService;
    private static SimulationService simulationService;

    public static synchronized void setRegistryService(RegistryService service) {
        if (registryService == null) {
            registryService = service;
        }
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    /**
     * Simulates a configSystemRegistry operation.
     *
     * Operation criteria:
     *     get - path
     *     put - path, resourcePath (existing resource), optional : mediaType
     *     resourceExists - path
     *     delete - path
     *     importResource - path, param1 (URL: source URL), optional : mediaType
     *     copy - path, param1 (target path)
     *     move - path, param1 (target path)
     *     rename - path, param1 (target path)
     *     removeLink - path
     *     createLink - path, param1 (target path), optional : param2 (target sub-path)
     *     invokeAspect - path, param1 (aspect name), param2 (action)
     *     addAssociation - path, param1 (target path), param2 (association type)
     *     removeAssociation - path, param1 (target path), param2 (association type)
     *     getAssociations - path, param1 (association type)
     *     getAllAssociations - path
     *     createVersion - path
     *     restoreVersion - path
     *     getVersions - path
     *     applyTag - path, param1 (tag)
     *     removeTag - path, param1 (tag)
     *     getTags - path
     *     getResourcePathsWithTag - param1 (tag)
     *     rateResource - path, param1 (Number: rating)
     *     getRating - path, param1 (username)
     *     getAverageRating - path
     *     addComment - path, param1 (comment)
     *     removeComment - path
     *     editComment - path, param1 (comment)
     *     getComments - path
     *     searchContent - param1 (keywords)
     *     executeQuery - param1 (Map: parameters, ex:- key1:val1,key2:val2,...), optional: path
     *
     * Operations not-supported
     *     dump
     *     restore
     *
     * @param  simulationRequest the simulation request.
     * @throws Exception if an exception occurs while executing any operation, or if an invalid
     *         parameter was entered.
     */
    public static void simulateRegistryOperation(Registry rootRegistry, SimulationRequest simulationRequest)
            throws Exception {
        String operation = simulationRequest.getOperation();
        if (operation == null) {
            return;
        }
        if (operation.toLowerCase().equals("get")) {
            String path = simulationRequest.getPath();
            if (isInvalidateValue(path)) {
                return;
            }
            simulationService.setSimulation(true);
            rootRegistry.get(path);
            simulationService.setSimulation(false);
        } else if (operation.toLowerCase().equals("resourceexists")) {
            String path = simulationRequest.getPath();
            if (isInvalidateValue(path)) {
                return;
            }
            simulationService.setSimulation(true);
            rootRegistry.resourceExists(path);
            simulationService.setSimulation(false);
        } else if (operation.toLowerCase().equals("put")) {
            String path = simulationRequest.getPath();
            String resourcePath = simulationRequest.getResourcePath();
            String type = null;
            String[] params = simulationRequest.getParameters();
            if (params != null && params.length >= 1) {
                type = params[0];
            }

            if (isInvalidateValue(path) || isInvalidateValue(type)) {
                return;
            }
            Resource resource;
            if (!isInvalidateValue(resourcePath) && rootRegistry.resourceExists(resourcePath)) {
                resource = rootRegistry.get(resourcePath);
            } else if (type.toLowerCase().equals("collection")) {
                resource = rootRegistry.newCollection();
            } else {
                resource = rootRegistry.newResource();
            }
            simulationService.setSimulation(true);
            resource.setMediaType(simulationRequest.getMediaType());
            rootRegistry.put(path, resource);
            simulationService.setSimulation(false);
        } else if (operation.toLowerCase().equals("delete")) {
            String path = simulationRequest.getPath();
            if (isInvalidateValue(path)) {
                return;
            }
            simulationService.setSimulation(true);
            rootRegistry.delete(path);
            simulationService.setSimulation(false);
        } else if (operation.toLowerCase().equals("importresource")) {
            String path = simulationRequest.getPath();
            String sourceURL = null;
            String[] params = simulationRequest.getParameters();
            if (params != null && params.length >= 1) {
                sourceURL = params[0];
            }
            if (isInvalidateValue(path) || isInvalidateValue(sourceURL)) {
                return;
            }
            simulationService.setSimulation(true);
            Resource resource = rootRegistry.newResource();
            resource.setMediaType(simulationRequest.getMediaType());
            rootRegistry.importResource(path, sourceURL, resource);
            simulationService.setSimulation(false);
        } else if (operation.toLowerCase().equals("rename")) {
            String path = simulationRequest.getPath();
            String target = null;
            String[] params = simulationRequest.getParameters();
            if (params != null && params.length >= 1) {
                target = params[0];
            }
            if (isInvalidateValue(path) || isInvalidateValue(target)) {
                return;
            }
            simulationService.setSimulation(true);
            rootRegistry.rename(path, target);
            simulationService.setSimulation(false);
        } else if (operation.toLowerCase().equals("move")) {
            String path = simulationRequest.getPath();
            String target = null;
            String[] params = simulationRequest.getParameters();
            if (params != null && params.length >= 1) {
                target = params[0];
            }
            if (isInvalidateValue(path) || isInvalidateValue(target)) {
                return;
            }
            simulationService.setSimulation(true);
            rootRegistry.move(path, target);
            simulationService.setSimulation(false);
        } else if (operation.toLowerCase().equals("copy")) {
            String path = simulationRequest.getPath();
            String target = null;
            String[] params = simulationRequest.getParameters();
            if (params != null && params.length >= 1) {
                target = params[0];
            }
            if (isInvalidateValue(path) || isInvalidateValue(target)) {
                return;
            }
            simulationService.setSimulation(true);
            rootRegistry.copy(path, target);
            simulationService.setSimulation(false);
        } else if (operation.toLowerCase().equals("removelink")) {
            String path = simulationRequest.getPath();
            if (isInvalidateValue(path)) {
                return;
            }
            simulationService.setSimulation(true);
            rootRegistry.removeLink(path);
            simulationService.setSimulation(false);
        } else if (operation.toLowerCase().equals("createlink")) {
            String path = simulationRequest.getPath();
            String target = null;
            String targetSubPath = null;
            String[] params = simulationRequest.getParameters();
            if (params != null && params.length > 0) {
                target = params[0];
                if (params.length > 1) {
                    targetSubPath = params[1];
                }
            }
            if (isInvalidateValue(path) || isInvalidateValue(target)) {
                return;
            }
            simulationService.setSimulation(true);
            if (isInvalidateValue(targetSubPath)) {
                rootRegistry.createLink(path, target);
            } else {
                rootRegistry.createLink(path, target, targetSubPath);
            }
            simulationService.setSimulation(false);
        } else if (operation.toLowerCase().equals("invokeaspect")) {
            String path = simulationRequest.getPath();
            String aspectName = null;
            String action = null;
            String[] params = simulationRequest.getParameters();
            if (params != null && params.length >= 2) {
                aspectName = params[0];
                action = params[1];
            }
            if (isInvalidateValue(path) || isInvalidateValue(aspectName) || isInvalidateValue(action)) {
                return;
            }
            simulationService.setSimulation(true);
            rootRegistry.invokeAspect(path, aspectName, action);
            simulationService.setSimulation(false);
        } else if (operation.toLowerCase().equals("addassociation")) {
            String path = simulationRequest.getPath();
            String target = null;
            String associationType = null;
            String[] params = simulationRequest.getParameters();
            if (params != null && params.length >= 2) {
                target = params[0];
                associationType = params[1];
            }
            if (isInvalidateValue(path) || isInvalidateValue(target) || isInvalidateValue(associationType) ) {
                return;
            }
            simulationService.setSimulation(true);
            rootRegistry.addAssociation(path, target, associationType);
            simulationService.setSimulation(false);
        } else if (operation.toLowerCase().equals("removeassociation")) {
            String path = simulationRequest.getPath();
            String target = null;
            String associationType = null;
            String[] params = simulationRequest.getParameters();
            if (params != null && params.length >= 2) {
                target = params[0];
                associationType = params[1];
            }
            if (isInvalidateValue(path) || isInvalidateValue(target) || isInvalidateValue(associationType) ) {
                return;
            }
            simulationService.setSimulation(true);
            rootRegistry.removeAssociation(path, target, associationType);
            simulationService.setSimulation(false);
        } else if (operation.toLowerCase().equals("getassociations")) {
            String path = simulationRequest.getPath();
            String associationType = null;
            String[] params = simulationRequest.getParameters();
            if (params != null && params.length >= 1) {
                associationType = params[0];
            }
            if (isInvalidateValue(path) || isInvalidateValue(associationType)) {
                return;
            }
            simulationService.setSimulation(true);
            rootRegistry.getAssociations(path, associationType);
            simulationService.setSimulation(false);
        } else if (operation.toLowerCase().equals("getallassociations")) {
            String path = simulationRequest.getPath();
            if (isInvalidateValue(path)) {
                return;
            }
            simulationService.setSimulation(true);
            rootRegistry.getAllAssociations(path);
            simulationService.setSimulation(false);
        } else if (operation.toLowerCase().equals("createversion")) {
            String path = simulationRequest.getPath();
            if (isInvalidateValue(path)) {
                return;
            }
            simulationService.setSimulation(true);
            rootRegistry.createVersion(path);
            simulationService.setSimulation(false);
        } else if (operation.toLowerCase().equals("restoreversion")) {
            String path = simulationRequest.getPath();
            if (isInvalidateValue(path)) {
                return;
            }
            simulationService.setSimulation(true);
            rootRegistry.restoreVersion(path);
            simulationService.setSimulation(false);
        } else if (operation.toLowerCase().equals("getversions")) {
            String path = simulationRequest.getPath();
            if (isInvalidateValue(path)) {
                return;
            }
            simulationService.setSimulation(true);
            rootRegistry.getVersions(path);
            simulationService.setSimulation(false);
        } else if (operation.toLowerCase().equals("applytag")) {
            String path = simulationRequest.getPath();
            String tag = null;
            String[] params = simulationRequest.getParameters();
            if (params != null && params.length >= 1) {
                tag = params[0];
            }
            if (isInvalidateValue(path) || isInvalidateValue(tag)) {
                return;
            }
            simulationService.setSimulation(true);
            rootRegistry.applyTag(path, tag);
            simulationService.setSimulation(false);
        } else if (operation.toLowerCase().equals("removetag")) {
            String path = simulationRequest.getPath();
            String tag = null;
            String[] params = simulationRequest.getParameters();
            if (params != null && params.length >= 1) {
                tag = params[0];
            }
            if (isInvalidateValue(path) || isInvalidateValue(tag)) {
                return;
            }
            simulationService.setSimulation(true);
            rootRegistry.removeTag(path, tag);
            simulationService.setSimulation(false);
        } else if (operation.toLowerCase().equals("gettags")) {
            String path = simulationRequest.getPath();
            if (isInvalidateValue(path)) {
                return;
            }
            simulationService.setSimulation(true);
            rootRegistry.getTags(path);
            simulationService.setSimulation(false);
        } else if (operation.toLowerCase().equals("getresourcepathswithtag")) {
            String tag = null;
            String[] params = simulationRequest.getParameters();
            if (params != null && params.length >= 1) {
                tag = params[0];
            }
            if (isInvalidateValue(tag)) {
                return;
            }
            simulationService.setSimulation(true);
            rootRegistry.getResourcePathsWithTag(tag);
            simulationService.setSimulation(false);
        } else if (operation.toLowerCase().equals("rateresource")) {
            String path = simulationRequest.getPath();
            int rating = -1;
            String[] params = simulationRequest.getParameters();
            if (params != null && params.length >= 1) {
                try {
                    rating = Integer.parseInt(params[0]);
                } catch (NumberFormatException ignored) {
                    return;
                }
            }
            if (isInvalidateValue(path) || rating == -1) {
                return;
            }
            simulationService.setSimulation(true);
            rootRegistry.rateResource(path, rating);
            simulationService.setSimulation(false);
        } else if (operation.toLowerCase().equals("getrating")) {
            String path = simulationRequest.getPath();
            String username = null;
            String[] params = simulationRequest.getParameters();
            if (params != null && params.length >= 1) {
                username = params[0];
            }
            if (isInvalidateValue(path) || isInvalidateValue(username)) {
                return;
            }
            simulationService.setSimulation(true);
            rootRegistry.getRating(path, username);
            simulationService.setSimulation(false);
        } else if (operation.toLowerCase().equals("getaveragerating")) {
            String path = simulationRequest.getPath();
            if (isInvalidateValue(path)) {
                return;
            }
            simulationService.setSimulation(true);
            rootRegistry.getAverageRating(path);
            simulationService.setSimulation(false);
        } else if (operation.toLowerCase().equals("addcomment")) {
            String path = simulationRequest.getPath();
            String comment = null;
            String[] params = simulationRequest.getParameters();
            if (params != null && params.length >= 1) {
                comment = params[0];
            }
            if (isInvalidateValue(path) || isInvalidateValue(comment)) {
                return;
            }
            simulationService.setSimulation(true);
            rootRegistry.addComment(path, new Comment(comment));
            simulationService.setSimulation(false);
        } else if (operation.toLowerCase().equals("editcomment")) {
            String path = simulationRequest.getPath();
            String comment = null;
            String[] params = simulationRequest.getParameters();
            if (params != null && params.length >= 1) {
                comment = params[0];
            }
            if (isInvalidateValue(path) || isInvalidateValue(comment)) {
                return;
            }
            simulationService.setSimulation(true);
            rootRegistry.editComment(path, comment);
            simulationService.setSimulation(false);
        } else if (operation.toLowerCase().equals("removeComment")) {
            String path = simulationRequest.getPath();
            if (isInvalidateValue(path)) {
                return;
            }
            simulationService.setSimulation(true);
            rootRegistry.removeComment(path);
            simulationService.setSimulation(false);
        } else if (operation.toLowerCase().equals("getcomments")) {
            String path = simulationRequest.getPath();
            if (isInvalidateValue(path)) {
                return;
            }
            simulationService.setSimulation(true);
            rootRegistry.getComments(path);
            simulationService.setSimulation(false);
        } else if (operation.toLowerCase().equals("searchcontent")) {
            String keywords = null;
            String[] params = simulationRequest.getParameters();
            if (params != null && params.length >= 1) {
                keywords = params[0];
            }
            if (isInvalidateValue(keywords)) {
                return;
            }
            simulationService.setSimulation(true);
            rootRegistry.searchContent(keywords);
            simulationService.setSimulation(false);
        } else if (operation.toLowerCase().equals("executequery")) {
            String path = simulationRequest.getPath();
            String queryParams = null;
            String[] params = simulationRequest.getParameters();
            if (params != null && params.length >= 1) {
                queryParams = params[0];
            }
            Map<String, String> paramMap = new LinkedHashMap<String, String>();
            if(isInvalidateValue(queryParams)) {
                return;
            }
            String[] entries = queryParams.split(",");
            if (entries != null) {
                for (String entry : entries) {
                    String[] keyValPair = entry.split(":");
                    if (keyValPair != null && keyValPair.length == 2) {
                        paramMap.put(keyValPair[0], keyValPair[1]);
                    }
                }
            }
            simulationService.setSimulation(true);
            rootRegistry.executeQuery(path, paramMap);
            simulationService.setSimulation(false);
        } else {
            throw new Exception("Unsupported Registry Operation: " + operation);
        }
    }

    /**
     * An utility method to check for an invalid parameter value.
     * @param paramValue parameter value.
     * @return    status.
     */
    private static boolean isInvalidateValue(String paramValue) {
        return null == paramValue || "".equals(paramValue);
    }

    public static SimulationResponse getSimulationResponse() {
        Map<String, List<String[]>> status = simulationService.getSimulationStatus();
        int entryCount = 0;
        Map<Integer, HandlerExecutionStatus> executionStatusMap =
                new HashMap<Integer, HandlerExecutionStatus>();
        if (status != null) {
            for (Map.Entry<String, List<String[]>> e : status.entrySet()) {
                if (e.getKey() != null && e.getValue() != null) {
                    if (e.getValue().size() > 0) {
                        for (String[] v : e.getValue()) {
                            if (v != null && v.length == 2) {
                                HandlerExecutionStatus executionStatus = new HandlerExecutionStatus();
                                executionStatus.setHandlerName(e.getKey());
                                executionStatus.setExecutionStatus(v[0]);
                                int order = Integer.parseInt(v[1]);
                                executionStatusMap.put(order, executionStatus);
                                if (order > entryCount) {
                                    entryCount = order;
                                }
                            }
                        }
                    }
                }
            }
        }
        List<HandlerExecutionStatus> executionStatusList = new LinkedList<HandlerExecutionStatus>();
        int i = 0;
        while (i < entryCount) {
            i++;
            HandlerExecutionStatus executionStatus = executionStatusMap.get(i);
            if (executionStatus != null) {
                executionStatusList.add(executionStatus);
            }
        }
        SimulationResponse response = new SimulationResponse();
        response.setStatus(executionStatusList.toArray(
                new HandlerExecutionStatus[executionStatusList.size()]));
        return response;
    }

    public static boolean updateHandler(Registry configSystemRegistry, String oldName, String payload) throws RegistryException,
            XMLStreamException {
        if (isHandlerNameInUse(oldName))
            throw new RegistryException("Could not update handler since it is already in use!");

        String newName = null;
        OMElement element = AXIOMUtil.stringToOM(payload);
        if (element != null) {
            newName = element.getAttributeValue(new QName("class"));
        }

        if (newName == null || newName.equals(""))
            return false; // invalid configuration

        if (oldName == null || oldName.equals("")) {
            String path = getContextRoot() + newName;
            Resource resource;
            if (handlerExists(configSystemRegistry, newName)) {
                return false; // we are adding a new handler
            }
            else {
                resource = new ResourceImpl();
            }
            resource.setContent(payload);
            try {
                configSystemRegistry.beginTransaction();
                configSystemRegistry.put(path, resource);
                generateHandler(configSystemRegistry, path);
                configSystemRegistry.commitTransaction();
            } catch (Exception e) {
                configSystemRegistry.rollbackTransaction();
                throw new RegistryException("Unable to generate handler", e);
            }
            return true;
        }

        if (newName.equals(oldName)) {
            //updating the rest of the content
            String oldPath = getContextRoot() + oldName;
            Resource resource;
            if (handlerExists(configSystemRegistry, oldName)) {
                resource = configSystemRegistry.get(oldPath);
            }
            else {
                resource = new ResourceImpl(); // will this ever happen?
            }
            resource.setContent(payload);
            try {
                configSystemRegistry.beginTransaction();
                removeHandler(configSystemRegistry, oldName);
                configSystemRegistry.put(oldPath, resource);
                generateHandler(configSystemRegistry, oldPath);
                configSystemRegistry.commitTransaction();
            } catch (Exception e) {
                configSystemRegistry.rollbackTransaction();
                throw new RegistryException("Unable to generate handler", e);
            }
            return true;
        }
        else {
            String oldPath = getContextRoot() + oldName;
            String newPath = getContextRoot() + newName;

            if (handlerExists(configSystemRegistry, newName)) {
                return false; // we are trying to use the name of a existing handler
            }

            Resource resource;
            if (handlerExists(configSystemRegistry, oldName)) {
                resource = configSystemRegistry.get(oldPath);
            }
            else {
                resource = new ResourceImpl(); // will this ever happen?
            }

            resource.setContent(payload);
            try {
                configSystemRegistry.beginTransaction();
                configSystemRegistry.put(newPath, resource);
                generateHandler(configSystemRegistry, newPath);
                removeHandler(configSystemRegistry, oldName);
                configSystemRegistry.delete(oldPath);
                configSystemRegistry.commitTransaction();
            } catch (Exception e) {
                configSystemRegistry.rollbackTransaction();
                throw new RegistryException("Unable to renew handler", e);
            }
            return true;
        }
    }

    public static boolean addHandler(Registry configSystemRegistry, String payload) throws RegistryException, XMLStreamException {
        String name;
        OMElement element = AXIOMUtil.stringToOM(payload);
        if (element != null) {
            name = element.getAttributeValue(new QName("class"));
        }
        else
            return false;

        if (isHandlerNameInUse(name))
            throw new RegistryException("The added handler name is already in use!");

        String path = getContextRoot() + name;
        Resource resource;
        if (!handlerExists(configSystemRegistry, name)) {
            resource = new ResourceImpl();
        }
        else {
            throw new RegistryException("The added handler name is already in use!");
        }
        resource.setContent(payload);
        try {
            configSystemRegistry.beginTransaction();
            configSystemRegistry.put(path, resource);
            generateHandler(configSystemRegistry, path);
            configSystemRegistry.commitTransaction();
        } catch (Exception e) {
            configSystemRegistry.rollbackTransaction();
            throw new RegistryException("Unable to generate handler", e);
        }
        return true;
    }

    public static boolean handlerExists(Registry configSystemRegistry, String name) throws RegistryException {
        return configSystemRegistry.resourceExists(getContextRoot() + name);
    }

    public static boolean deleteHandler(Registry configSystemRegistry, String name) throws RegistryException, XMLStreamException {
        if (isHandlerNameInUse(name))
            throw new RegistryException("Handler could not be deleted, since it is already in use!");

        String path = getContextRoot() + name;
        if (configSystemRegistry.resourceExists(path)) {
            try {
                configSystemRegistry.beginTransaction();
                removeHandler(configSystemRegistry, name);
                configSystemRegistry.delete(path);
                configSystemRegistry.commitTransaction();
            } catch (Exception e) {
                configSystemRegistry.rollbackTransaction();
                throw new RegistryException("Unable to remove handler", e);
            }
            return true;
        }
        return false;
    }

    public static String getHandlerConfiguration(Registry configSystemRegistry, String name) throws RegistryException,
            XMLStreamException {
        String path = getContextRoot() + name;
        Resource resource;
        if (handlerExists(configSystemRegistry, name)) {
            resource = configSystemRegistry.get(path);
            return RegistryUtils.decodeBytes((byte[])resource.getContent());
        }
        return null;
    }

    public static String[] getHandlerList(Registry configSystemRegistry) throws RegistryException{
        Collection collection;
        try {
            collection = (Collection)configSystemRegistry.get(getContextRoot());
        } catch (Exception e) {
            return null;
        }

        if (collection == null) {
            CollectionImpl handlerCollection = new CollectionImpl();
            configSystemRegistry.put(getContextRoot(), handlerCollection);
            return null;
        }
        else {
            if (collection.getChildCount() == 0) {
                return null;
            }

            String[] childrenList = collection.getChildren();
            String[] handlerNameList = new String[collection.getChildCount()];
            for (int i = 0; i < childrenList.length; i++) {
                String path = childrenList[i];
                handlerNameList[i] = path.substring(
                        path.lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1);
            }
            return handlerNameList;
        }
    }


    public static String getContextRoot() {
        if (contextRoot == null) {
            return RegistryConstants.HANDLER_CONFIGURATION_PATH;
        }
        return contextRoot;
    }

    public static void setContextRoot(String contextRoot) {
        if (!contextRoot.endsWith(RegistryConstants.PATH_SEPARATOR))
            contextRoot += RegistryConstants.PATH_SEPARATOR;
        CommonUtil.contextRoot = contextRoot;
    }

    public static boolean generateHandler(Registry configSystemRegistry, String resourceFullPath) throws RegistryException,
            XMLStreamException {
        RegistryContext registryContext = configSystemRegistry.getRegistryContext();
        if (registryContext == null) {
            return false;
        }
        Resource resource = configSystemRegistry.get(resourceFullPath);
        if (resource != null) {
            String content = null;
            if (resource.getContent() != null) {
                content = RegistryUtils.decodeBytes((byte[])resource.getContent());
            }
            if (content != null) {
                OMElement handler = AXIOMUtil.stringToOM(content);
                if (handler != null) {
                    OMElement dummy = OMAbstractFactory.getOMFactory().createOMElement("dummy",
                            null);
                    dummy.addChild(handler);
                    try {
                        configSystemRegistry.beginTransaction();
                        boolean status = RegistryConfigurationProcessor.updateHandler(dummy,
                                configSystemRegistry.getRegistryContext(),
                                HandlerLifecycleManager.USER_DEFINED_HANDLER_PHASE);
                        configSystemRegistry.commitTransaction();
                        return status;
                    } catch (Exception e) {
                        configSystemRegistry.rollbackTransaction();
                        throw new RegistryException("Unable to add handler", e);
                    }
                }
            }
        }
        return false;
    }

    public static boolean removeHandler(Registry configSystemRegistry, String handlerName) throws RegistryException,
            XMLStreamException {
        String handlerConfiguration = getHandlerConfiguration(configSystemRegistry, handlerName);
        if (handlerConfiguration != null) {
            OMElement element = AXIOMUtil.stringToOM(handlerConfiguration);
            try {
                try {
                    configSystemRegistry.beginTransaction();
                    RegistryConfigurationProcessor.HandlerDefinitionObject handlerDefinitionObject =
                            new RegistryConfigurationProcessor.HandlerDefinitionObject(
                                    null, element).invoke();
                    String[] methods = handlerDefinitionObject.getMethods();
                    Filter filter = handlerDefinitionObject.getFilter();
                    Handler handler = handlerDefinitionObject.getHandler();
                    if (handlerDefinitionObject.getTenantId() != -1) {
                        CurrentSession.setCallerTenantId(handlerDefinitionObject.getTenantId());
                        // We need to swap the tenant id for this call, if the handler has overriden the
                        // default value.
                        configSystemRegistry.getRegistryContext().getHandlerManager().removeHandler(methods, filter,
                            handler, HandlerLifecycleManager.USER_DEFINED_HANDLER_PHASE);
                        CurrentSession.removeCallerTenantId();
                    } else {
                        configSystemRegistry.getRegistryContext().getHandlerManager().removeHandler(methods, filter,
                            handler, HandlerLifecycleManager.USER_DEFINED_HANDLER_PHASE);
                    }

                    configSystemRegistry.commitTransaction();
                    return true;
                } catch (Exception e) {
                    configSystemRegistry.rollbackTransaction();
                    throw e;
                }
            } catch (Exception e) {
                if (e instanceof RegistryException) {
                    throw (RegistryException)e;
                } else if (e instanceof XMLStreamException) {
                    throw (XMLStreamException)e;
                }
                throw new RegistryException("Unable to build handler configuration", e);
            }
        }
        return false;
    }

    public static boolean isHandlerNameInUse(String name)
            throws RegistryException, XMLStreamException {
        // It really doesn't matter whether a handler is in-use or not, as we don't keep a track of
        // it internally.
        return false;
    }

    public static boolean addDefaultHandlersIfNotAvailable(Registry configSystemRegistry)
            throws RegistryException, FileNotFoundException, XMLStreamException {

        if (!configSystemRegistry.resourceExists(RegistryConstants.HANDLER_CONFIGURATION_PATH)) {
            Collection handlerConfigurationCollection = new CollectionImpl();
            String description = "Handler configurations are stored here.";
            handlerConfigurationCollection.setDescription(description);
            configSystemRegistry.put(RegistryConstants.HANDLER_CONFIGURATION_PATH,
                    handlerConfigurationCollection);
            // We don't have any default handler configuration as in lifecycles.
        }
        else {
            // configue all handlers
            Resource handlerRoot = configSystemRegistry.get(getContextRoot());
            if (!(handlerRoot instanceof Collection)) {
                String msg = "Failed to continue as the handler configuration root: " +
                        getContextRoot() + " is not a collection.";
                log.error(msg);
                throw new RegistryException(msg);
            }
            Collection handlerRootCol = (Collection)handlerRoot;
            String[] handlerConfigPaths = handlerRootCol.getChildren();
            if (handlerConfigPaths != null) {
                for (String handlerConfigPath: handlerConfigPaths) {
                    generateHandler(configSystemRegistry, handlerConfigPath);
                }
            }
        }

        return true;
    }

    public static SimulationService getSimulationService() {
        return simulationService;
    }

    public static void setSimulationService(SimulationService simulationService) {
        CommonUtil.simulationService = simulationService;
    }

    public static HandlerConfigurationBean deserializeHandlerConfiguration(OMElement configurationElement) {
        if (configurationElement == null ||
                !"handler".equals(configurationElement.getLocalName())) {
            return null;
        }
        HandlerConfigurationBean handlerConfigurationBean = new HandlerConfigurationBean();
        handlerConfigurationBean.setHandlerClass(
                configurationElement.getAttributeValue(new QName("class")));
        handlerConfigurationBean.setTenant(configurationElement.getAttributeValue(new QName("tenant")));
        String methodsAttribute = configurationElement.getAttributeValue(new QName("methods"));
        if (methodsAttribute != null && methodsAttribute.length() != 0) {
            handlerConfigurationBean.setMethods(methodsAttribute.split(","));
        }
        @SuppressWarnings("unchecked")
        Iterator<OMElement> handlerProps =
                configurationElement.getChildrenWithName(new QName("property"));
        while (handlerProps.hasNext()) {
            OMElement propElement = handlerProps.next();
            String propName = propElement.getAttributeValue(new QName("name"));
            String propType = propElement.getAttributeValue(new QName("type"));

            if (propType != null && "xml".equals(propType)) {
                handlerConfigurationBean.getXmlProperties().put(propName, propElement);
            } else {
                handlerConfigurationBean.getNonXmlProperties().put(propName,
                        propElement.getText());
            }
            handlerConfigurationBean.getPropertyList().add(propName);
        }
        FilterConfigurationBean filterConfigurationBean = new FilterConfigurationBean();
        OMElement filterElement =
                configurationElement.getFirstChildWithName(new QName("filter"));
        filterConfigurationBean.setFilterClass(
                filterElement.getAttributeValue(new QName("class")));
        @SuppressWarnings("unchecked")
        Iterator<OMElement> filterProps =
                filterElement.getChildrenWithName(new QName("property"));
        while (filterProps.hasNext()) {
            OMElement propElement = filterProps.next();
            String propName = propElement.getAttributeValue(new QName("name"));
            String propType = propElement.getAttributeValue(new QName("type"));

            if (propType != null && "xml".equals(propType)) {
                filterConfigurationBean.getXmlProperties().put(propName, propElement);
            } else {
                filterConfigurationBean.getNonXmlProperties().put(propName,
                        propElement.getText());
            }
            filterConfigurationBean.getPropertyList().add(propName);
        }
        handlerConfigurationBean.setFilter(filterConfigurationBean);
        return handlerConfigurationBean;
    }

    public static OMElement serializeHandlerConfiguration(HandlerConfigurationBean bean) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement handler = factory.createOMElement("handler", null);
        handler.addAttribute(factory.createOMAttribute("class", null, bean.getHandlerClass()));
        if (bean.getTenant() != null) {
            handler.addAttribute(factory.createOMAttribute("tenant", null, bean.getTenant()));
        }
        StringBuilder sb = new StringBuilder();
        for (String method : bean.getMethods()) {
            if (method != null && method.length() > 0) {
                sb.append(method).append(",");
            }
        }
        // Remove last ","
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
            handler.addAttribute(factory.createOMAttribute("methods", null, sb.toString()));
        }
        for (String property : bean.getPropertyList()) {
            OMElement temp = factory.createOMElement("property", null);
            temp.addAttribute(factory.createOMAttribute("name", null, property));
            OMElement xmlProperty = bean.getXmlProperties().get(property);
            if (xmlProperty != null) {
//                The serialization happens by adding the whole XML property value to the bean.
//                Therefore if it is a XML property, we take that whole element.
                handler.addChild(xmlProperty);
            } else {
                String nonXMLProperty = bean.getNonXmlProperties().get(property);
                if (nonXMLProperty != null) {
                    temp.setText(nonXMLProperty);
                    handler.addChild(temp);
                }
            }
        }
        OMElement filter = factory.createOMElement("filter", null);
        filter.addAttribute(factory.createOMAttribute("class", null,
                bean.getFilter().getFilterClass()));
        for (String property : bean.getFilter().getPropertyList()) {
            OMElement temp = factory.createOMElement("property", null);
            temp.addAttribute(factory.createOMAttribute("name", null, property));
            OMElement xmlProperty = bean.getFilter().getXmlProperties().get(property);
            if (xmlProperty != null) {
                temp.addAttribute(factory.createOMAttribute("type", null, "xml"));
                temp.addChild(xmlProperty);
                filter.addChild(temp);
            } else {
                String nonXMLProperty = bean.getFilter().getNonXmlProperties().get(property);
                if (nonXMLProperty != null) {
                    temp.setText(nonXMLProperty);
                    filter.addChild(temp);
                }
            }
        }
        handler.addChild(filter);
        return handler;
    }
}
