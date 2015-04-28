/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.info.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.core.subscription.Subscription;
import org.wso2.carbon.registry.common.eventing.RegistryEvent;
import org.wso2.carbon.registry.common.services.RegistryAbstractAdmin;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Comment;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.info.Utils;
import org.wso2.carbon.registry.info.services.utils.*;
import org.wso2.carbon.registry.common.IInfoService;
import org.wso2.carbon.registry.common.beans.*;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.registry.core.utils.AccessControlConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InfoService extends RegistryAbstractAdmin implements IInfoService {

    private static final Log log = LogFactory.getLog(InfoService.class);

    public void setSession(String sessionId, HttpSession session) {
    }

    public void removeSession(String sessionId) {
    }

    public CommentBean getComments(String path, String sessionId) throws RegistryException {
        UserRegistry registry = (UserRegistry) getRootRegistry();
        return CommentBeanPopulator.populate(registry, path);
    }

    public void addComment(String comment, String path, String sessionId) throws RegistryException {
        UserRegistry registry = (UserRegistry) getRootRegistry();
        registry.addComment(path, new Comment(comment));
    }
    
    public void removeComment(String commentPath, String sessionId) throws RegistryException  {
    	 UserRegistry registry = (UserRegistry) getRootRegistry();
         registry.removeComment(commentPath);
    }

    public TagBean getTags(String path, String sessionId) throws RegistryException {
        UserRegistry registry = (UserRegistry) getRootRegistry();
        return TagBeanPopulator.populate(registry, path);
    }

    public void addTag(String tag, String path, String sessionId) throws RegistryException {
        UserRegistry registry = (UserRegistry) getRootRegistry();
        registry.applyTag(path, tag);
    }
    
    public void removeTag(String tag, String path, String sessionId)
    throws RegistryException {
        UserRegistry registry = (UserRegistry) getRootRegistry();
        registry.removeTag(path, tag);

    }

    public RatingBean getRatings(String path, String sessionId) throws RegistryException {
        UserRegistry registry = (UserRegistry) getRootRegistry();
        return RatingBeanPopulator.populate(registry, path);
    }

    public void rateResource(String rating, String path, String sessionId)
            throws RegistryException {
        int userRating = Integer.parseInt(rating);
        UserRegistry registry = (UserRegistry) getRootRegistry();
        registry.rateResource(path, userRating);
    }

    public EventTypeBean getEventTypes(String path, String sessionId) throws RegistryException {
        UserRegistry registry = (UserRegistry) getRootRegistry();
        Resource resource = null;
        if (!path.startsWith(SubscriptionBeanPopulator.RECURSE)) {
            if (registry.resourceExists(path)) {
                resource = registry.get(path);
            }
        }
        if (resource != null) {
            String isLink = resource.getProperty("registry.link");
            String mountPoint = resource.getProperty("registry.mountpoint");
            String targetPoint = resource.getProperty("registry.targetpoint");
            String actualPath = resource.getProperty("registry.actualpath");
            if (isLink != null && mountPoint != null && targetPoint != null) {
//                path = path.replace(mountPoint, targetPoint);
                path = actualPath;
            }
        }
        return EventTypeBeanPopulator.populate(registry, path);
    }

    public SubscriptionBean getSubscriptions(String path, String sessionId)
            throws RegistryException {
        UserRegistry registry = (UserRegistry) getRootRegistry();
        return SubscriptionBeanPopulator.populate(registry, path);
    }

    public SubscriptionBean subscribe(String path, String endpoint, String eventName,
                                      String sessionId) throws RegistryException {
        String tempPath = path.substring(0, path.lastIndexOf("/"));

        RegistryUtils.recordStatistics(tempPath, endpoint, eventName, sessionId);
        UserRegistry registry = (UserRegistry) getRootRegistry();
        if (RegistryUtils.isRegistryReadOnly(registry.getRegistryContext())) {
            return null;
        }

        return SubscriptionBeanPopulator.subscribeAndPopulate(registry, path, endpoint, eventName);
    }

    public SubscriptionBean subscribeREST(String path, String endpoint, String eventName,
                                          String sessionId) throws RegistryException {
        RegistryUtils.recordStatistics(path, endpoint, eventName, sessionId);
        UserRegistry registry = (UserRegistry) getRootRegistry();
        if (RegistryUtils.isRegistryReadOnly(registry.getRegistryContext())) {
            return null;
        }
        return SubscriptionBeanPopulator.subscribeAndPopulate(registry, path, endpoint,
                eventName, true);
    }

    public boolean isResource(String path, String sessionId) throws RegistryException {
        UserRegistry registry = (UserRegistry) getRootRegistry();
        if (!registry.resourceExists(path)) {
            // the only possible reason is that the resource was deleted and the subscription is
            // dangling. We have no way to determine whether this is a resource or collection. So,
            // simply treat as resource.
            return true;
        }
        Resource resource = registry.get(path);
        if (resource != null) {
            String isLink = resource.getProperty("registry.link");
            String mountPoint = resource.getProperty("registry.mountpoint");
            String targetPoint = resource.getProperty("registry.targetpoint");
            String actualPath = resource.getProperty("registry.actualpath");
            if (isLink != null && mountPoint != null && targetPoint != null) {
//                path = path.replace(mountPoint, targetPoint);
                path = actualPath;
                // This is a symbolic link
                resource = null;
            }
        }

        try {
            if (resource == null) {
                // Actual resource
                resource = registry.get(path);
            }
        } catch (Exception ignore) {
            // If we couldn't fetch the actual resource, we will continue to live with the logical
            // resource.
        }
        if (resource != null && (resource instanceof Collection)) {
            log.debug("Found Collection at path: " + path);
            return false;
        } else if (resource == null) {
            log.error("No resource was found at path: " + path);
        } else {
            log.debug("Found Resource at path: " + path);
        }
        return true;
    }

    public boolean isProfileExisting(String username, String sessionId) throws RegistryException {
        UserRegistry registry = (UserRegistry) getRootRegistry();
        try {
            if (registry != null && registry.getUserRealm() != null &&
                    registry.getUserRealm().getUserStoreManager() != null) {
                UserRealm realm = registry.getUserRealm();
                boolean isAdmin = false;
                String[] userRoles = realm.getUserStoreManager().getRoleListOfUser(
                        registry.getUserName());
                for (String userRole: userRoles) {
                    if (userRole.equals(realm.getRealmConfiguration().getAdminRoleName())) {
                        isAdmin = true;
                        break;
                    }
                }
                if (!username.equals(registry.getUserName()) && !isAdmin) {
                    return false;    
                }
                UserStoreManager reader = realm.getUserStoreManager();
                return (reader.getUserClaimValue(username,
                        UserCoreConstants.ClaimTypeURIs.EMAIL_ADDRESS,
                        UserCoreConstants.DEFAULT_PROFILE)) != null;
            }
        } catch (UserStoreException ignore) {
            return false;
        }
        return false;
    }

    public boolean isRoleProfileExisting(String role, String sessionId) throws RegistryException {
        UserRegistry registry = (UserRegistry) getRootRegistry();
        try {
            if (registry != null && registry.getUserRealm() != null &&
                    registry.getUserRealm().getUserStoreManager() != null) {
                UserRealm realm = registry.getUserRealm();
                boolean isAdmin = false;
                String[] userRoles = realm.getUserStoreManager().getRoleListOfUser(
                        registry.getUserName());
                for (String userRole: userRoles) {
                    if (userRole.equals(realm.getRealmConfiguration().getAdminRoleName())) {
                        isAdmin = true;
                        break;
                    }
                }
                return Arrays.asList(userRoles).contains(role) || isAdmin;
            }
        } catch (UserStoreException ignore) {
            return false;
        }
        return false;
    }

    public String getRemoteURL(String path, String sessionId) throws RegistryException {
        UserRegistry registry = (UserRegistry) getRootRegistry();
        if (registry.resourceExists(path)) {
            Resource resource = registry.get(path);
            if (resource != null) {
                String isLink = resource.getProperty("registry.link");
                String realPath = resource.getProperty("registry.realpath");
                String userName = resource.getProperty("registry.user");
                if (isLink != null && realPath != null && userName != null) {
                    log.debug("Found mounted resource at: " + realPath);
                    if (!realPath.contains("/registry/resourceContent?")) {
                        return null;
                    }
                    boolean isLocalMount = false;
                    try {
                        isLocalMount = ResourceUtil.isLocalMount(realPath);
                    } catch (RegistryException e) {
                        log.error("Unable to check whether resource is locally mounted", e);
                    }
                    if(!isLocalMount) {
                        return realPath.replace("/registry/resourceContent?", "/carbon/resources/resource.jsp?region=region3&item=resource_browser_menu&viewType=std&");
                    }
                }
            }
        }
        return null;
    }

    public String verifyEmail(String data, String sessionId) throws RegistryException {
        if (Utils.getSubscriptionEmailVerficationService() == null) {
            return null;
        }
        return Utils.getSubscriptionEmailVerficationService().verifyEmail(data);
    }

    public boolean unsubscribe(String path, String id, String sessionId) throws RegistryException {
        RegistryUtils.recordStatistics(path, id, sessionId);
        log.debug("Got unsubscribe request at path: " + path + " with id: " + id);
        UserRegistry registry = (UserRegistry) getRootRegistry();
        return InfoUtil.unsubscribe(registry, path, id, sessionId);
    }

    public boolean isUserValid(String username, String sessionId) throws RegistryException {
        UserRegistry registry = (UserRegistry) getRootRegistry();
        try {
            if (!registry.getUserRealm().getUserStoreManager().isExistingUser(username)) {
                return false;
            }
            // TODO: Add test for users in system role.
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRoleValid(String role, String sessionId) throws RegistryException {
        UserRegistry registry = (UserRegistry) getRootRegistry();
        try {
            return registry.getUserRealm().getUserStoreManager().isExistingRole(role);
        } catch (Exception e) {
            return false;
        }
    }

    
}
