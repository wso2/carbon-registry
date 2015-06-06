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

package org.wso2.carbon.registry.eventing.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.common.eventing.RegistryEvent;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourcePath;
import org.wso2.carbon.registry.core.config.Mount;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.eventing.events.*;
import org.wso2.carbon.registry.eventing.internal.EventingDataHolder;

import java.util.List;

public class RegistryEventingHandler extends Handler {
    private static final Log log = LogFactory.getLog(RegistryEventingHandler.class);

    public void put(RequestContext requestContext) throws RegistryException {
    	
        String path = requestContext.getResourcePath().getPath();
        String relativePath = RegistryUtils.getRelativePath(requestContext.getRegistryContext(),path);
        if (!sendNotifications(requestContext, relativePath)){
           return;
        }
    	boolean isNotCollection = !(requestContext.getResource() instanceof Collection);
        Resource resource = requestContext.getOldResource();
        RegistryEvent<String> event;
        if (resource == null) {
            if (isNotCollection) {
                event = new ResourceAddedEvent<String>("A resource was added at Path: " + relativePath);
                ((ResourceAddedEvent)event).setResourcePath(relativePath);
            } else {
                event = new CollectionAddedEvent<String>("A collection was added at Path: " + relativePath);
                ((CollectionAddedEvent)event).setResourcePath(relativePath);
            }
            event.setTenantId(CurrentSession.getCallerTenantId());
        } else {
            if (isNotCollection) {
                event = new ResourceUpdatedEvent<String>("The resource at path " + relativePath + " was updated.");
                ((ResourceUpdatedEvent)event).setResourcePath(relativePath);
            } else {
                event = new CollectionUpdatedEvent<String>("The collection at path " + relativePath + " was updated.");
                ((CollectionUpdatedEvent)event).setResourcePath(relativePath);
            }
            event.setParameter("RegistryOperation", "put");
            event.setTenantId(CurrentSession.getCallerTenantId());
        }
        try {
            notify(event, requestContext.getRegistry(), relativePath);
        } catch (Exception e) {
            handleException("Unable to send notification for Put Operation", e);
        }
    }

    public void delete(RequestContext requestContext) throws RegistryException {

        String path = requestContext.getResourcePath().getPath();
        String relativePath = RegistryUtils.getRelativePath(requestContext.getRegistryContext(),path);
        if (!sendNotifications(requestContext, relativePath)){
            return;
        }

        String parentPath = RegistryUtils.getParentPath(relativePath);
        boolean isNotCollection = !(requestContext.getResource() instanceof Collection);
        RegistryEvent<String> childDeletedEvent;
        RegistryEvent<String> parentEvent;
        RegistryEvent<String> event;
        if (isNotCollection) {
            childDeletedEvent = new ChildDeletedEvent<String>("A resource was removed from the collection " +
                    parentPath + " at Path: " + relativePath);
            ((ChildDeletedEvent)childDeletedEvent).setResourcePath(parentPath);
            childDeletedEvent.setParameter("ChildPath", relativePath);
            event = new ResourceDeletedEvent<String>("A resource at path " + relativePath + " was deleted.");
            ((ResourceDeletedEvent)event).setResourcePath(relativePath);
        } else {
            childDeletedEvent = new ChildDeletedEvent<String>("A collection was removed from the collection " +
                    parentPath + " at Path: " + relativePath);
            ((ChildDeletedEvent)childDeletedEvent).setResourcePath(parentPath);
            childDeletedEvent.setParameter("ChildPath", relativePath);
            event = new CollectionDeletedEvent<String>("A collection at path " + relativePath + " was deleted.");
            ((CollectionDeletedEvent)event).setResourcePath(relativePath);
        }
        childDeletedEvent.setTenantId(CurrentSession.getCallerTenantId());
        event.setTenantId(CurrentSession.getCallerTenantId());
        parentEvent = new CollectionUpdatedEvent<String>("The collection at path " + parentPath + " was updated.");
        ((CollectionUpdatedEvent)parentEvent).setResourcePath(parentPath);
        parentEvent.setTenantId(CurrentSession.getCallerTenantId());
        try {
            notify(childDeletedEvent, requestContext.getRegistry(), parentPath);
            notify(event, requestContext.getRegistry(), relativePath);
            notify(parentEvent, requestContext.getRegistry(), parentPath);
        } catch (Exception e) {
            handleException("Unable to send notification for Delete Operation", e);
        }
    }

    public void createVersion(RequestContext requestContext) throws RegistryException {

        boolean isMountPath = false;
        String path = requestContext.getResourcePath().getPath();
        String relativePath = RegistryUtils.getRelativePath(requestContext.getRegistryContext(),path);
        if (!sendNotifications(requestContext, relativePath)){
            return;
        }

        boolean isNotCollection = !(requestContext.getRepository().get(path) instanceof Collection);
        RegistryEvent<String> event;
        if (isNotCollection) {
            event = new ResourceUpdatedEvent<String>("A Checkpoint was created for the resource at path " +
                    relativePath + ".");
            ((ResourceUpdatedEvent)event).setResourcePath(relativePath);
        } else {
            event = new CollectionUpdatedEvent<String>("A Checkpoint was created for the collection at path " +
                    relativePath + ".");
            ((CollectionUpdatedEvent)event).setResourcePath(relativePath);
        }
        event.setParameter("RegistryOperation", "createVersion");
        event.setTenantId(CurrentSession.getCallerTenantId());
        try {
            notify(event, requestContext.getRegistry(), relativePath);
        } catch (Exception e) {
            handleException("Unable to send notification for Create Version Operation", e);
        }
    }

    public void applyTag(RequestContext requestContext) throws RegistryException {

        String path = requestContext.getResourcePath().getPath();
        String relativePath = RegistryUtils.getRelativePath(requestContext.getRegistryContext(),path);
        if (!sendNotifications(requestContext, relativePath)){
            return;
        }

        String tag = requestContext.getTag();
        boolean isNotCollection = !(requestContext.getRepository().get(path) instanceof Collection);
        RegistryEvent<String> event;
        if (isNotCollection) {
            event = new ResourceUpdatedEvent<String>("The tag " + tag +
                    " was applied on resource " + relativePath + ".");
            ((ResourceUpdatedEvent)event).setResourcePath(relativePath);
        } else {
            event = new CollectionUpdatedEvent<String>("The tag " + tag +
                    " was applied on resource " + relativePath + ".");
            ((CollectionUpdatedEvent)event).setResourcePath(relativePath);
        }
        event.setParameter("RegistryOperation", "applyTag");
        event.setParameter("TagsAdded", tag);
        event.setTenantId(CurrentSession.getCallerTenantId());
        try {
            notify(event, requestContext.getRegistry(), relativePath);
        } catch (Exception e) {
            handleException("Unable to send notification for Apply Tag Operation", e);
        }
    }

    public void removeTag(RequestContext requestContext) throws RegistryException {

        String path = requestContext.getResourcePath().getPath();
        String relativePath = RegistryUtils.getRelativePath(requestContext.getRegistryContext(),path);
        if (!sendNotifications(requestContext, relativePath)){
            return;
        }

        String tag = requestContext.getTag();
        boolean isNotCollection = !(requestContext.getRepository().get(path) instanceof Collection);
        RegistryEvent<String> event;
        if (isNotCollection) {
            event = new ResourceUpdatedEvent<String>("An attempt was made to remove the tag " +
                    tag + ", applied on resource " + relativePath + ".");
            ((ResourceUpdatedEvent)event).setResourcePath(relativePath);
        } else {
            event = new CollectionUpdatedEvent<String>("An attempt was made to remove the tag " +
                    tag + ", applied on collection " + relativePath + ".");
            ((CollectionUpdatedEvent)event).setResourcePath(relativePath);
        }
        event.setParameter("RegistryOperation", "removeTag");
        event.setTenantId(CurrentSession.getCallerTenantId());
        try {
            notify(event, requestContext.getRegistry(), relativePath);
        } catch (Exception e) {
            handleException("Unable to send notification for Remove Tag Operation", e);
        }
    }

    public String addComment(RequestContext requestContext) throws RegistryException {

        String path = requestContext.getResourcePath().getPath();
        String relativePath = RegistryUtils.getRelativePath(requestContext.getRegistryContext(),path);
        if (!sendNotifications(requestContext, relativePath)){
            return null;
        }

        if (requestContext.getComment() == null) {
            return null;
        }
        String comment = requestContext.getComment().getText();
        boolean isNotCollection = !(requestContext.getRepository().get(path) instanceof Collection);
        RegistryEvent<String> event;
        if (isNotCollection) {
            event = new ResourceUpdatedEvent<String>("A comment was added to the resource at " +
                    relativePath + ". Comment: " + comment);
            ((ResourceUpdatedEvent)event).setResourcePath(relativePath);
        } else {
            event = new CollectionUpdatedEvent<String>("A comment was added to the collection at " +
                    relativePath + ". Comment: " + comment);
            ((CollectionUpdatedEvent)event).setResourcePath(relativePath);
        }
        event.setParameter("RegistryOperation", "addComment");
        event.setTenantId(CurrentSession.getCallerTenantId());
        try {
            notify(event, requestContext.getRegistry(), relativePath);
        } catch (Exception e) {
            handleException("Unable to send notification for Add Comment Operation", e);
        }
        return null;
    }

    public void rateResource(RequestContext requestContext) throws RegistryException {

        String path = requestContext.getResourcePath().getPath();
        String relativePath = RegistryUtils.getRelativePath(requestContext.getRegistryContext(),path);
        if (!sendNotifications(requestContext, relativePath)){
            return;
        }

        int rating = requestContext.getRating();
        boolean isNotCollection = !(requestContext.getRepository().get(path) instanceof Collection);
        RegistryEvent<String> event;
        if (isNotCollection) {
            event = new ResourceUpdatedEvent<String>("A rating of " + Integer.toString(rating) +
                    " was given to the resource at " + relativePath + ".");
            ((ResourceUpdatedEvent)event).setResourcePath(relativePath);
        } else {
            event = new CollectionUpdatedEvent<String>("A rating of " + Integer.toString(rating) +
                    " was given to the collection at " + relativePath + ".");
            ((CollectionUpdatedEvent)event).setResourcePath(relativePath);
        }
        event.setParameter("RegistryOperation", "rateResource");
        event.setParameter("Rating", Integer.toString(rating));
        event.setTenantId(CurrentSession.getCallerTenantId());
        try {
            notify(event, requestContext.getRegistry(), relativePath);
        } catch (Exception e) {
            handleException("Unable to send notification for Rate Resource Operation", e);
        }
    }

    private String getPathWithoutVersion(String pathWithVersion) {
        ResourcePath path = new ResourcePath(pathWithVersion);
        return path.getPath();
    }

    public void addAssociation(RequestContext requestContext) throws RegistryException {

        String path = getPathWithoutVersion(requestContext.getSourcePath());
        String relativePath = RegistryUtils.getRelativePath(requestContext.getRegistryContext(), path);
        if (!sendNotifications(requestContext, relativePath)){
            return;
        }

        String targetPath = getPathWithoutVersion(requestContext.getTargetPath());
        String relativeTargetPath = RegistryUtils.getRelativePath(requestContext.getRegistryContext(),
                targetPath);
        String type = requestContext.getAssociationType();
        String targetType;
        boolean isNotCollection = true;
        if (requestContext.getRepository().get(targetPath) == null) {
            return;
        } else if (!(requestContext.getRepository().get(targetPath) instanceof Collection)) {
            targetType = "resource";
        } else {
            targetType = "collection";
            isNotCollection = false;
        }
        RegistryEvent<String> event;
        if (isNotCollection) {
            event = new ResourceUpdatedEvent<String>("An association of type " + type + " to the " + targetType +
                    " at " + relativeTargetPath + " was added to the resource at " + relativePath + ".");
            ((ResourceUpdatedEvent)event).setResourcePath(relativePath);
        } else {
            event = new CollectionUpdatedEvent<String>("An association of type " + type + " to the " + targetType +
                    " at " + relativeTargetPath + " was added to the collection at " + relativePath + ".");
            ((CollectionUpdatedEvent)event).setResourcePath(relativePath);
        }
        event.setParameter("RegistryOperation", "addAssociation");
        event.setTenantId(CurrentSession.getCallerTenantId());
        try {
            notify(event, requestContext.getRegistry(), relativePath);
        } catch (Exception e) {
            handleException("Unable to send notification for Add Association Operation", e);
        }
    }

    public void removeAssociation(RequestContext requestContext) throws RegistryException {
    	
        String path = getPathWithoutVersion(requestContext.getSourcePath());
        String relativePath = RegistryUtils.getRelativePath(requestContext.getRegistryContext(), path);
        if (!sendNotifications(requestContext, relativePath)){
            return;
        }
        String targetPath = getPathWithoutVersion(requestContext.getTargetPath());
        String relativeTargetPath = RegistryUtils.getRelativePath(requestContext.getRegistryContext(),
                targetPath);
        String type = requestContext.getAssociationType();
        String targetType;
        boolean isNotCollection = true;
        if (requestContext.getRepository().get(targetPath) == null) {
            return;
        } else if (!(requestContext.getRepository().get(targetPath) instanceof Collection)) {
            targetType = "resource";
        } else {
            targetType = "collection";
            isNotCollection = false;
        }
        RegistryEvent<String> event;
        if (isNotCollection) {
            event = new ResourceUpdatedEvent<String>("The association of type " + type + " to the " + targetType +
                    " at " + relativeTargetPath + " was removed from the resource at " + relativePath + ".");
            ((ResourceUpdatedEvent)event).setResourcePath(relativePath);
        } else {
            event = new CollectionUpdatedEvent<String>("The association of type " + type + " to the " + targetType +
                    " at " + relativeTargetPath + " was removed from the collection at " + relativePath + ".");
            ((CollectionUpdatedEvent)event).setResourcePath(relativePath);
        }
        event.setParameter("RegistryOperation", "removeAssociation");
        event.setTenantId(CurrentSession.getCallerTenantId());
        try {
            notify(event, requestContext.getRegistry(), relativePath);
        } catch (Exception e) {
            handleException("Unable to send notification for Remove Association Operation", e);
        }
    }

    public void createLink(RequestContext requestContext) throws RegistryException {

        String path = requestContext.getResourcePath().getPath();
        String relativePath = RegistryUtils.getRelativePath(requestContext.getRegistryContext(),
                path);
        if (!sendNotifications(requestContext, relativePath)){
            return;
        }
        String parentPath = RegistryUtils.getParentPath(relativePath);
        String target = requestContext.getTargetPath();
        String relativeTarget = RegistryUtils.getRelativePath(requestContext.getRegistryContext(),
                target);
        RegistryEvent<String> event = new CollectionUpdatedEvent<String>("A link to " + relativeTarget +
                " was created at " + relativePath + ".");
        event.setParameter("Destination", relativeTarget);
        event.setParameter("SymbolicLink", relativePath);
        event.setParameter("RegistryOperation", "createLink");
        ((CollectionUpdatedEvent)event).setResourcePath(parentPath);
        event.setTenantId(CurrentSession.getCallerTenantId());
        try {
            notify(event, requestContext.getRegistry(), parentPath);
        } catch (Exception e) {
            handleException("Unable to send notification for Create Link Operation", e);
        }
    }

    public void removeLink(RequestContext requestContext) throws RegistryException {
    	

        String path = requestContext.getResourcePath().getPath();
        String relativePath = RegistryUtils.getRelativePath(requestContext.getRegistryContext(), path);
        if (!sendNotifications(requestContext, relativePath)){
            return;
        }
        String parentPath = RegistryUtils.getParentPath(relativePath);
        RegistryEvent<String> event = new CollectionUpdatedEvent<String>("The link at " + relativePath + " was removed.");
        ((CollectionUpdatedEvent)event).setResourcePath(parentPath);
        event.setParameter("SymbolicLink", relativePath);
        event.setParameter("RegistryOperation", "removeLink");
        event.setTenantId(CurrentSession.getCallerTenantId());
        try {
            notify(event, requestContext.getRegistry(), parentPath);
        } catch (Exception e) {
            handleException("Unable to send notification for Remove Link Operation", e);
        }
    }

    public void putChild(RequestContext requestContext) throws RegistryException {

        String path = requestContext.getResourcePath().getPath();
        String relativePath = RegistryUtils.getRelativePath(requestContext.getRegistryContext(), path);
        if (!sendNotifications(requestContext, relativePath)){
            return;
        }
        boolean isNotCollection = !(requestContext.getResource() instanceof Collection);
        String parentPath = RegistryUtils.getParentPath(relativePath);
        Resource resource = requestContext.getOldResource();
        RegistryEvent<String> parentEvent = null;
        RegistryEvent<String> childCreatedEvent = null;
        if (resource == null) {
            if (isNotCollection) {
                childCreatedEvent = new ChildCreatedEvent<String>("A resource was added to the collection " +
                        parentPath + " at Path: " + relativePath);
                childCreatedEvent.setParameter("ChildPath", relativePath);
                ((ChildCreatedEvent)childCreatedEvent).setResourcePath(parentPath);
            } else {
                childCreatedEvent = new ChildCreatedEvent<String>(
                        "A collection was added to the collection " + parentPath + " at Path: " + relativePath);
                childCreatedEvent.setParameter("ChildPath", relativePath);
                ((ChildCreatedEvent)childCreatedEvent).setResourcePath(parentPath);
            }
            childCreatedEvent.setTenantId(CurrentSession.getCallerTenantId());
            parentEvent = new CollectionUpdatedEvent<String>("The collection at path " + parentPath + " was updated.");
            parentEvent.setParameter("RegistryOperation", "putChild");
            ((CollectionUpdatedEvent)parentEvent).setResourcePath(parentPath);
            parentEvent.setTenantId(CurrentSession.getCallerTenantId());
        }
        try {
            if (childCreatedEvent != null) {
                notify(childCreatedEvent, requestContext.getRegistry(), parentPath);
            }
            if (parentEvent != null) {
                notify(parentEvent, requestContext.getRegistry(), parentPath);
            }
        } catch (Exception e) {
            handleException("Unable to send notification for Put Operation", e);
        }
    }

    public String move(RequestContext requestContext) throws RegistryException {

        String path = requestContext.getSourcePath();
        String relativePath = RegistryUtils.getRelativePath(requestContext.getRegistryContext(), path);
        if (!sendNotifications(requestContext, relativePath)){
            return null;
        }
        String targetPath = requestContext.getTargetPath();
        String relativeTargetPath = RegistryUtils.getRelativePath(requestContext.getRegistryContext(),
                targetPath);
        boolean isNotCollection = !(requestContext.getResource() instanceof Collection);
        
        String sourceParentPath = RegistryUtils.getParentPath(relativePath);
        String targetParentPath = RegistryUtils.getParentPath(relativeTargetPath);
        RegistryEvent<String> sourceEvent = null;
        RegistryEvent<String> targetEvent = null;
        RegistryEvent<String> event = null;
        RegistryEvent<String> childDeletedEvent = null;
        RegistryEvent<String> childCreatedEvent = null;
        if (isNotCollection) {
            if (sourceParentPath != null) {
                sourceEvent = new CollectionUpdatedEvent<String>("A resource was moved from the collection " +
                        sourceParentPath + " at Path: " + relativePath);
                ((CollectionUpdatedEvent)sourceEvent).setResourcePath(sourceParentPath);
                sourceEvent.setParameter("RegistryOperation", "moveFrom");
                sourceEvent.setTenantId(CurrentSession.getCallerTenantId());
                childDeletedEvent = new ChildDeletedEvent<String>("A resource was removed from the collection " +
                        sourceParentPath + " at Path: " + relativePath);
                ((ChildDeletedEvent)childDeletedEvent).setResourcePath(sourceParentPath);
                childDeletedEvent.setParameter("ChildPath", relativePath);
                childDeletedEvent.setTenantId(CurrentSession.getCallerTenantId());
            }
            if (targetParentPath != null) {
                event = new ResourceUpdatedEvent<String>("The resource at Path: " + relativePath +
                        " was moved to: " + targetParentPath);
                ((ResourceUpdatedEvent)event).setResourcePath(relativePath);
                event.setParameter("RegistryOperation", "move");
                event.setTenantId(CurrentSession.getCallerTenantId());
                targetEvent = new CollectionUpdatedEvent<String>("A resource was moved to the collection " +
                        targetParentPath + " at Path: " + relativeTargetPath);
                ((CollectionUpdatedEvent)targetEvent).setResourcePath(targetParentPath);
                targetEvent.setParameter("RegistryOperation", "moveTo");
                targetEvent.setTenantId(CurrentSession.getCallerTenantId());
                childCreatedEvent = new ChildCreatedEvent<String>("A resource was added to the collection " +
                        targetParentPath + " at Path: " + relativeTargetPath);
                ((ChildCreatedEvent)childCreatedEvent).setResourcePath(targetParentPath);
                childCreatedEvent.setParameter("ChildPath", relativeTargetPath);
                childCreatedEvent.setTenantId(CurrentSession.getCallerTenantId());
            }
        } else {
            if (sourceParentPath != null) {
                sourceEvent = new CollectionUpdatedEvent<String>("A collection was moved from the collection " +
                        sourceParentPath + " at Path: " + relativePath);
                ((CollectionUpdatedEvent)sourceEvent).setResourcePath(sourceParentPath);
                sourceEvent.setParameter("RegistryOperation", "moveFrom");
                sourceEvent.setTenantId(CurrentSession.getCallerTenantId());
                childDeletedEvent = new ChildDeletedEvent<String>("A collection was removed from the collection " +
                        sourceParentPath + " at Path: " + relativePath);
                ((ChildDeletedEvent)childDeletedEvent).setResourcePath(sourceParentPath);
                childDeletedEvent.setParameter("ChildPath", relativePath);
                childDeletedEvent.setTenantId(CurrentSession.getCallerTenantId());
            }
            if (targetParentPath != null) {
                event = new CollectionUpdatedEvent<String>("The collection at Path: " + relativePath +
                        " was moved to: " + targetParentPath);
                ((CollectionUpdatedEvent)event).setResourcePath(relativePath);
                event.setParameter("RegistryOperation", "move");
                event.setTenantId(CurrentSession.getCallerTenantId());
                targetEvent = new CollectionUpdatedEvent<String>("A collection was moved to the collection " +
                        targetParentPath + " at Path: " + relativeTargetPath);
                ((CollectionUpdatedEvent)targetEvent).setResourcePath(targetParentPath);
                targetEvent.setParameter("RegistryOperation", "moveTo");
                targetEvent.setTenantId(CurrentSession.getCallerTenantId());
                childCreatedEvent = new ChildCreatedEvent<String>("A collection was added to the collection " +
                        targetParentPath + " at Path: " + relativeTargetPath);
                ((ChildCreatedEvent)childCreatedEvent).setResourcePath(targetParentPath);
                childCreatedEvent.setParameter("ChildPath", relativeTargetPath);
                childCreatedEvent.setTenantId(CurrentSession.getCallerTenantId());
            }
        }
        try {
            if (sourceEvent != null) {
                notify(sourceEvent, requestContext.getRegistry(), sourceParentPath);
            }
            if (event != null) {
                notify(event, requestContext.getRegistry(), relativePath);
            }
            if (targetEvent != null) {
                notify(targetEvent, requestContext.getRegistry(), targetParentPath);
            }
            if (childDeletedEvent != null) {
                notify(childDeletedEvent, requestContext.getRegistry(), sourceParentPath);
            }
            if (childCreatedEvent != null) {
                notify(childCreatedEvent, requestContext.getRegistry(), targetParentPath);
            }
        } catch (Exception e) {
            handleException("Unable to send notification for Move Operation", e);
        }
        return null;
    }

    public String copy(RequestContext requestContext) throws RegistryException {
    	
 	    String path = requestContext.getSourcePath();
        String relativePath = RegistryUtils.getRelativePath(requestContext.getRegistryContext(), path);
        if (!sendNotifications(requestContext, relativePath)){
            return null;
        }
        String targetPath = requestContext.getTargetPath();
        String relativeTargetPath = RegistryUtils.getRelativePath(requestContext.getRegistryContext(),
                targetPath);
        boolean isNotCollection = !(requestContext.getResource() instanceof Collection);
        String sourceParentPath = RegistryUtils.getParentPath(relativePath);
        String targetParentPath = RegistryUtils.getParentPath(relativeTargetPath);
        RegistryEvent<String> sourceEvent = null;
        RegistryEvent<String> targetEvent = null;
        RegistryEvent<String> childCreatedEvent = null;
        if (isNotCollection) {
            if (sourceParentPath != null) {
                sourceEvent = new CollectionUpdatedEvent<String>("A resource was copied from the collection " +
                        sourceParentPath + " at Path: " + relativePath);
                ((CollectionUpdatedEvent)sourceEvent).setResourcePath(sourceParentPath);
                sourceEvent.setParameter("RegistryOperation", "copyFrom");
                sourceEvent.setTenantId(CurrentSession.getCallerTenantId());
            }
            if (targetParentPath != null) {
                targetEvent = new CollectionUpdatedEvent<String>("A resource was copied to the collection " +
                        targetParentPath + " at Path: " + relativeTargetPath);
                ((CollectionUpdatedEvent)targetEvent).setResourcePath(targetParentPath);
                targetEvent.setParameter("RegistryOperation", "copyTo");
                targetEvent.setTenantId(CurrentSession.getCallerTenantId());
                childCreatedEvent = new ChildCreatedEvent<String>("A resource was added to the collection " +
                        targetParentPath + " at Path: " + relativeTargetPath);
                ((ChildCreatedEvent)childCreatedEvent).setResourcePath(targetParentPath);
                childCreatedEvent.setParameter("ChildPath", relativeTargetPath);
                childCreatedEvent.setTenantId(CurrentSession.getCallerTenantId());
            }
        } else {
            if (sourceParentPath != null) {
                sourceEvent = new CollectionUpdatedEvent<String>("A collection was copied from the collection " +
                        sourceParentPath + " at Path: " + relativePath);
                ((CollectionUpdatedEvent)sourceEvent).setResourcePath(sourceParentPath);
                sourceEvent.setParameter("RegistryOperation", "copyFrom");
                sourceEvent.setTenantId(CurrentSession.getCallerTenantId());
            }
            if (targetParentPath != null) {
                targetEvent = new CollectionUpdatedEvent<String>("A collection was copied to the collection " +
                        targetParentPath + " at Path: " + relativeTargetPath);
                ((CollectionUpdatedEvent)targetEvent).setResourcePath(targetParentPath);
                targetEvent.setParameter("RegistryOperation", "copyTo");
                targetEvent.setTenantId(CurrentSession.getCallerTenantId());
                childCreatedEvent = new ChildCreatedEvent<String>("A collection was added to the collection " +
                        targetParentPath + " at Path: " + relativeTargetPath);
                ((ChildCreatedEvent)childCreatedEvent).setResourcePath(targetParentPath);
                childCreatedEvent.setParameter("ChildPath", relativeTargetPath);
                childCreatedEvent.setTenantId(CurrentSession.getCallerTenantId());
            }
        }
        try {
            if (sourceEvent != null) {
                notify(sourceEvent, requestContext.getRegistry(), sourceParentPath);
            }
            if (targetEvent != null) {
                notify(targetEvent, requestContext.getRegistry(), targetParentPath);
            }
            if (childCreatedEvent != null) {
                notify(childCreatedEvent, requestContext.getRegistry(), targetParentPath);
            }
        } catch (Exception e) {
            handleException("Unable to send notification for Copy Operation", e);
        }
        return null;
    }

    public String rename(RequestContext requestContext) throws RegistryException {

        String path = requestContext.getSourcePath();
        String relativePath = RegistryUtils.getRelativePath(requestContext.getRegistryContext(), path);
        if (!sendNotifications(requestContext, relativePath)){
            return null;
        }
        String targetPath = requestContext.getTargetPath();
        String relativeTargetPath = RegistryUtils.getRelativePath(requestContext.getRegistryContext(),
                targetPath);
        boolean isNotCollection = !(requestContext.getResource() instanceof Collection);
        String parentPath = RegistryUtils.getParentPath(relativePath);
        RegistryEvent<String> parentEvent = null;
        RegistryEvent<String> event;
        if (isNotCollection) {
            event = new ResourceUpdatedEvent<String>("The resource at Path: " + relativePath +
                    " was renamed to: " + relativeTargetPath);
            ((ResourceUpdatedEvent)event).setResourcePath(relativePath);
            event.setParameter("RegistryOperation", "rename");
            if (parentPath != null) {
                parentEvent = new CollectionUpdatedEvent<String>("A resource in the collection " + parentPath +
                        " at Path: " + relativePath + " was renamed to: " + relativeTargetPath);
                ((CollectionUpdatedEvent)parentEvent).setResourcePath(parentPath);
                parentEvent.setParameter("RegistryOperation", "childRenamed");
                parentEvent.setTenantId(CurrentSession.getCallerTenantId());
            }
        } else {
            event = new CollectionUpdatedEvent<String>("The collection at Path: " + relativePath +
                    " was renamed to: " + relativeTargetPath);
            ((CollectionUpdatedEvent)event).setResourcePath(relativePath);
            event.setParameter("RegistryOperation", "rename");
            if (parentPath != null) {
                parentEvent = new CollectionUpdatedEvent<String>("A collection in the collection " + parentPath +
                        " at Path: " + relativePath + " was renamed to: " + relativeTargetPath);
                ((CollectionUpdatedEvent)parentEvent).setResourcePath(parentPath);
                parentEvent.setParameter("RegistryOperation", "childRenamed");
                parentEvent.setTenantId(CurrentSession.getCallerTenantId());
            }
        }
        event.setTenantId(CurrentSession.getCallerTenantId());
        try {
            notify(event, requestContext.getRegistry(), relativePath);
            if (parentEvent != null) {
                notify(parentEvent, requestContext.getRegistry(), parentPath);
            }
        } catch (Exception e) {
            handleException("Unable to send notification for Rename Operation", e);
        }
        return null;
    }

    public void restore(RequestContext requestContext) throws RegistryException {
        String path = requestContext.getResourcePath().getPath();
        String relativePath = RegistryUtils.getRelativePath(requestContext.getRegistryContext(), path);
        if (!sendNotifications(requestContext, relativePath)){
            return;
        }
        boolean isNotCollection = !(requestContext.getResource() instanceof Collection);
        RegistryEvent<String> event;
        if (isNotCollection) {
            event = new ResourceUpdatedEvent<String>(
                    "The resource at path " + relativePath + " was restored.");
            ((ResourceUpdatedEvent) event).setResourcePath(relativePath);
        } else {
            event = new CollectionUpdatedEvent<String>(
                    "The collection at path " + relativePath + " was restored.");
            ((CollectionUpdatedEvent) event).setResourcePath(relativePath);
        }
        event.setParameter("RegistryOperation", "restore");
        event.setTenantId(CurrentSession.getCallerTenantId());
        try {
            notify(event, requestContext.getRegistry(), relativePath);
        } catch (Exception e) {
            handleException("Unable to send notification for Restore Operation", e);
        }
    }

    protected void notify(RegistryEvent event, Registry registry, String path)
            throws Exception {
        try {
            if (EventingDataHolder.getInstance().getRegistryEventingService() == null) {
                log.debug("Eventing service is unavailable.");
                return;
            }
            if (registry == null || registry.getEventingServiceURL(path) == null) {
                EventingDataHolder.getInstance().getRegistryEventingService().notify(event);
                return;
            } else if (EventingDataHolder.getInstance().getDefaultEventingServiceURL() == null) {
                log.error("Registry Eventing Handler is not properly initialized");
            } else if (registry.getEventingServiceURL(path)
                               .equals(EventingDataHolder.getInstance().getDefaultEventingServiceURL())) {
                EventingDataHolder.getInstance().getRegistryEventingService().notify(event);
                return;
            } else {
                EventingDataHolder.getInstance().getRegistryEventingService()
                                  .notify(event, registry.getEventingServiceURL(path));
                return;
            }
        } catch (RegistryException e) {
            log.error("Unable to send notification", e);
            return;
        }
        log.error("Unable to send notification");
    }

    private void handleException(String message, Exception e) {
        log.error(message, e);
    }

    /**
     * Method to get the actual depth of the request
     * @param requestContext Request Context
     */
    private int getRequestDepth(RequestContext requestContext){
        int requestDepth = -1;
        if (requestContext.getRegistry().getRegistryContext() != null &&
            requestContext.getRegistry().getRegistryContext().getDataAccessManager() != null &&
            requestContext.getRegistry().getRegistryContext().getDataAccessManager().getDatabaseTransaction() != null) {
            requestDepth =
                    requestContext.getRegistry().getRegistryContext().getDataAccessManager().getDatabaseTransaction()
                                  .getNestedDepth();
        }
        return requestDepth;
    }

    private boolean sendNotifications(RequestContext requestContext, String relativePath){

        boolean isMountPath = false;
        List<Mount> mounts = requestContext.getRegistry().getRegistryContext().getMounts();
        for (Mount mount: mounts) {
            String mountPath = mount.getPath();
            if (relativePath.startsWith(mountPath)){
                isMountPath = true;
            }
        }
        if (isMountPath){
            if(getRequestDepth(requestContext) != 1){
                return false;
            } else{
                return true;
            }
        }  else {
            int requestDepth = getRequestDepth(requestContext);
            if(!(requestDepth == 1 || requestDepth == 3)){
                return false;
            } else {
                return true;
            }
        }

    }
}
