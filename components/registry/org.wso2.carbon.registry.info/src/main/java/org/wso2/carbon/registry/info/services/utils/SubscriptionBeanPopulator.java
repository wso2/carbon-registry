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

package org.wso2.carbon.registry.info.services.utils;

import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.core.exception.EventBrokerException;
import org.wso2.carbon.event.core.subscription.Subscription;
import org.wso2.carbon.event.ws.internal.builders.exceptions.InvalidMessageException;
import org.wso2.carbon.event.ws.internal.builders.utils.BuilderUtils;
import org.wso2.carbon.registry.common.ResourceData;
import org.wso2.carbon.registry.common.beans.SubscriptionBean;
import org.wso2.carbon.registry.common.beans.utils.SubscriptionInstance;
import org.wso2.carbon.registry.common.eventing.RegistryEvent;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourcePath;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.pagination.PaginationUtils;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.AccessControlConstants;
import org.wso2.carbon.registry.eventing.RegistryEventingConstants;
import org.wso2.carbon.registry.info.Utils;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.*;

public class SubscriptionBeanPopulator {

    private static final Log log = LogFactory.getLog(SubscriptionBeanPopulator.class);

    public static final String RECURSE = "-R";


    private static SubscriptionInstance populate(String path, Subscription subscription) {

        String delimiter = null;
        if (subscription.getTopicName().contains("#") || subscription.getTopicName().contains("*")) {
            delimiter = subscription.getTopicName().substring(subscription.getTopicName().lastIndexOf("/") + 1);
        }
        return populate(path, subscription, delimiter);
    }

    private static SubscriptionInstance populate(String path, Subscription subscription, String delimiter) {

        SubscriptionInstance subscriptionInstance = new SubscriptionInstance();
        if (subscription != null) {
            if (subscription.getId() == null || subscription.getTopicName() == null) {
                log.error("Failed getting ID or Filter Value");
                return null;
            }

            String[] temp = subscription.getTopicName().split(RegistryEvent.TOPIC_SEPARATOR);
            String eventName = "";
            if (temp[0].equals("")) {
                eventName = temp[3];
            } else {
                eventName = temp[2];
            }

            String tempTopic = RegistryEventingConstants.TOPIC_PREFIX + RegistryEvent.TOPIC_SEPARATOR + eventName + path;
            if (delimiter != null) {
                if (delimiter.equals("#") || delimiter.equals("*")) {
                    if(tempTopic.endsWith("/")){
                        tempTopic = tempTopic + delimiter;
                    } else{
                        tempTopic = tempTopic + RegistryEvent.TOPIC_SEPARATOR + delimiter;
                    }
                }
            }

            if (!subscription.getTopicName().equals(tempTopic)) {
                log.debug("Filter name is: " + subscription.getTopicName() +
                        ". Expected: " + RegistryEventingConstants.TOPIC_PREFIX + eventName +
                        RegistryEvent.TOPIC_SEPARATOR + path + ".");
                return null;
            }

            subscriptionInstance.setId(subscription.getId());
            subscriptionInstance.setAddress(subscription.getEventSinkURL());
            subscriptionInstance.setTopic(subscription.getTopicName());
            subscriptionInstance.setEventName(eventName);
            String address = subscriptionInstance.getAddress();
            if (address.startsWith("digest://")) {
                subscriptionInstance.setDigestType(address.substring(9, 10));
                address = address.substring(11);
            } else {
                subscriptionInstance.setDigestType("");
            }
            if (address.startsWith("mailto:")) {
                subscriptionInstance.setNotificationMethod("email");
            } else if (address.startsWith("user://")) {
                subscriptionInstance.setNotificationMethod("username");
            } else if (address.startsWith("role://")) {
                subscriptionInstance.setNotificationMethod("role");
            } else if (address.startsWith("jmx://")) {
                subscriptionInstance.setNotificationMethod("jmx");
            } else if (address.startsWith("work://")) {
                subscriptionInstance.setNotificationMethod("work");
            } else if (subscription.getProperties() != null &&
                subscription.getProperties().get(
                        RegistryEventingConstants.DO_REST) != null &&
                ((String)subscription.getProperties().get(
                        RegistryEventingConstants.DO_REST)).equals(
                        Boolean.toString(Boolean.TRUE))) {
                subscriptionInstance.setNotificationMethod("html.plain.text");
            } else {
                subscriptionInstance.setNotificationMethod("soap");
            }
        } else {
            throw new IllegalStateException("A valid subscription was not present");
        }
        log.debug("Found subscription instance");
        return subscriptionInstance;
    }

    public static SubscriptionBean populate(UserRegistry userRegistry, String path) {
        Resource resource = null;
        boolean recurse = false;
        String url = null;
        String userName = null;
        if (!path.startsWith(SubscriptionBeanPopulator.RECURSE)) {
            try {
                resource = userRegistry.get(path);
            } catch (Exception e) {
                log.warn("Unable to fetch Resource at path: " + path);
                resource = null;
            }
        } else {
            path = path.substring(RECURSE.length());
            recurse = true;
        }
        if (resource != null) {
            String isLink = resource.getProperty("registry.link");
            String mountPoint = resource.getProperty("registry.mountpoint");
            String targetPoint = resource.getProperty("registry.targetpoint");
            String realPath = resource.getProperty("registry.realpath");
            String actualPath = resource.getProperty("registry.actualpath");
            userName = resource.getProperty("registry.user");
            if (isLink != null && mountPoint != null && targetPoint != null) {
//                path = path.replace(mountPoint, targetPoint);
                path = actualPath;
            } else if (isLink != null && realPath != null && userName != null) {
                log.debug("Found mounted resource at: " + realPath);
                if (!realPath.contains("/registry/resourceContent?")) {
                    path = realPath;
                } else {
                	boolean isLocalMount = false;
                	try {
                		isLocalMount = ResourceUtil.isLocalMount(realPath);
					} catch (RegistryException e) {
						log.error("Unable to check whether resource is locally mounted", e);
					}
                	if(!isLocalMount) {
						url = realPath.substring(0, realPath.indexOf("/resourceContent?path="));
					}
                }
            }
        }
        SubscriptionBean subscriptionBean = new SubscriptionBean();
        ResourcePath resourcePath = new ResourcePath(path);
        try {
            if (url == null || userName == null) {
                if (Utils.getRegistryEventingService() == null) {
                    throw new IllegalStateException("Subscription Manager not found");
                }
            } else if (Utils.getRegistryEventingService() == null) {
                throw new IllegalStateException("Remote Subscription Manager not found at: " + url);
            }
            List<Subscription> subscriptions = null;
            if (url == null || userName == null) {
                subscriptions = Utils.getRegistryEventingService().getAllSubscriptions();
            } else {
                subscriptions = Utils.getRegistryEventingService().getAllSubscriptions(userName, url);
            }
            log.debug("Found " + subscriptions.size() + " subscriptions");
            List<SubscriptionInstance> subscriptionInstances = new LinkedList<SubscriptionInstance>();
            for (Subscription subscription : subscriptions) {
                String testPath;
                if (!recurse) {
                    testPath = path;
                } else {
                    testPath = subscription.getTopicName();
                    if (testPath == null || testPath.lastIndexOf(RegistryConstants.PATH_SEPARATOR) <= 0
                            || !testPath.contains(path)) {
                        log.debug("path (invalid): " + testPath);
                        continue;
                    }
                    if(testPath.contains("#")||testPath.contains("*")){
                        String tempTestPath = testPath.substring(RegistryEventingConstants.TOPIC_PREFIX.length()+1,
                                testPath.lastIndexOf(RegistryConstants.PATH_SEPARATOR));
                        if(tempTestPath.contains(RegistryConstants.PATH_SEPARATOR)){
                            testPath =tempTestPath.split(RegistryConstants.PATH_SEPARATOR,2)[1];
                        }else{
                            testPath="/"; //only when the resource is ROOT the tempTestPath would not contain any "/"
                        }

                    }else{
                        testPath = (testPath.substring(RegistryEventingConstants.TOPIC_PREFIX.length()+1,testPath.length()))
                                .split(RegistryConstants.PATH_SEPARATOR,2)[1];
                    }

                    if(!testPath.startsWith(RegistryConstants.PATH_SEPARATOR)){
                        testPath=RegistryConstants.PATH_SEPARATOR+testPath;
                    }

                }
                log.debug("path: " + testPath);
                String username = null;
                if (subscription.getProperties() != null) {
                    if (subscription.getTenantId() != userRegistry.getCallerTenantId()) {
                        log.debug("TenantId for subscription doesn't match with the logged-in tenant");
                        continue;
                    }
                    username = subscription.getOwner();
                    if (username.indexOf("@") > 0) {
                        username = username.split("@")[0];
                    }
                    log.debug("Current User is: " + userRegistry.getUserName() + ". Owner of subscription is: " + username + ".");

                    if (username == null || !username.equals(userRegistry.getUserName())) {
                        if (!isAuthorized(userRegistry, testPath, AccessControlConstants.AUTHORIZE)) {
                            log.debug("User does not have AUTHORIZE priviledge to see this subscription");
                            continue;
                        }
                    } else if (!isAuthorized(userRegistry, testPath, ActionConstants.GET)) {
                        log.debug("User does not have GET priviledge to see this subscription");
                        continue;
                    }
                }
                SubscriptionInstance subscriptionInstance = populate(testPath, subscription);
                if (subscriptionInstance != null) {
                    subscriptionInstance.setOwner(username);
                    subscriptionInstances.add(subscriptionInstance);
                }
            }
            subscriptionBean.setSubscriptionInstances(subscriptionInstances.toArray(new SubscriptionInstance[0]));
            log.debug("Returning " + subscriptionInstances.size() + " subscriptions");
            subscriptionBean.setPathWithVersion(resourcePath.getPathWithVersion());
            subscriptionBean.setVersionView(!resourcePath.isCurrentVersion());
            subscriptionBean.setLoggedIn(
                    !RegistryConstants.ANONYMOUS_USER.equals(userRegistry.getUserName()));
            subscriptionBean.setUserName(userRegistry.getUserName());
            subscriptionBean.setRoles(getRolesOfUser(userRegistry, userRegistry.getUserName()));
            if (isAuthorized(userRegistry, path, AccessControlConstants.AUTHORIZE)) {
                subscriptionBean.setUserAccessLevel(SubscriptionBean.UserAccessLevel.AUTHORIZE);
            } else if (isAuthorized(userRegistry, path, ActionConstants.DELETE)) {
                subscriptionBean.setUserAccessLevel(SubscriptionBean.UserAccessLevel.DELETE);
            } else if (isAuthorized(userRegistry, path, ActionConstants.GET)) {
                subscriptionBean.setUserAccessLevel(SubscriptionBean.UserAccessLevel.READ);
            } else {
                subscriptionBean.setUserAccessLevel(SubscriptionBean.UserAccessLevel.NONE);
            }
            if (isAdmin(userRegistry, getRolesOfUser(userRegistry, userRegistry.getUserName()))) {
                subscriptionBean.setRoleAccessLevel(1);
            } else {
                subscriptionBean.setRoleAccessLevel(0);
            }
        } catch (EventBrokerException e) {
            String msg = "Failed to get subscriptions information of the resource " +
                    resourcePath + ". " + e.getMessage();
            log.error("Failed to get subscriptions information of the resource " +
                    resourcePath + ".", e);
            subscriptionBean.setErrorMessage(msg);
        }
        return getPaginatedResult(subscriptionBean);
    }

    private  static SubscriptionBean getPaginatedResult( SubscriptionBean subscriptionBean) {
        SubscriptionInstance[] paginatedResult;
        MessageContext messageContext = MessageContext.getCurrentMessageContext();
        if (messageContext != null && PaginationUtils.isPaginationHeadersExist(messageContext)) {

            int rowCount = subscriptionBean.getSubscriptionInstances().length;
            try {
                PaginationUtils.setRowCount(messageContext, Integer.toString(rowCount));
                PaginationContext paginationContext = PaginationUtils.initPaginationContext(messageContext);

                int start = paginationContext.getStart();
                int count = paginationContext.getCount();

                int startIndex;
                if (start == 1) {
                    startIndex = 0;
                } else {
                    startIndex = start;
                }
                if (rowCount < start + count) {
                    paginatedResult = new SubscriptionInstance[rowCount - startIndex];
                    System.arraycopy(subscriptionBean.getSubscriptionInstances(), startIndex, paginatedResult, 0,
                            (rowCount - startIndex));
                } else {
                    paginatedResult = new SubscriptionInstance[count];
                    System.arraycopy(subscriptionBean.getSubscriptionInstances(), startIndex, paginatedResult, 0,
                            count);
                }
                subscriptionBean.setSubscriptionInstances(paginatedResult);
                return subscriptionBean;

            } finally {
                PaginationContext.destroy();
            }
        }else {
            return subscriptionBean;
        }
    }
    public static SubscriptionBean subscribeAndPopulate(UserRegistry userRegistry, String path, String endpoint, String eventName) {
        return subscribeAndPopulate(userRegistry, path, endpoint, eventName, false);
    }

    public static boolean isAuthorized(UserRegistry userRegistry, String path, String action) {
        try {
            UserRealm realm = userRegistry.getUserRealm();
            if (realm.getAuthorizationManager() != null) {
                return realm.getAuthorizationManager().isUserAuthorized(userRegistry.getUserName(), path, action);
            }
            return false;
        } catch (UserStoreException e) {
            return false;
        }
    }

    public static String[] getRolesOfUser(UserRegistry userRegistry, String username) {
        try {
            UserRealm realm = userRegistry.getUserRealm();
            if (realm.getUserStoreManager() != null) {
                return realm.getUserStoreManager().getRoleListOfUser(username);
            }
            return new String[0];
        } catch (UserStoreException e) {
            return new String[0];
        }
    }

    public static boolean isAdmin(UserRegistry userRegistry, String[] rolesOfUser) {
        try {
            UserRealm realm = userRegistry.getUserRealm();
            if (realm.getRealmConfiguration() != null) {
                String adminRole = realm.getRealmConfiguration().getAdminRoleName();
                return Arrays.asList(rolesOfUser).contains(adminRole);
            }
            return false;
        } catch (UserStoreException e) {
            return false;
        }
    }

    private static boolean hasPermissionToSubscribeViaEmail(UserRegistry userRegistry, String path, String endpoint) {
        if (endpoint != null) {
            String address = endpoint;
            if (address.toLowerCase().startsWith("digest://")) {
                address = address.substring(11);
            }
            if (address.toLowerCase().startsWith("role://")) {
                String roleToSubscribe = address.substring(7).trim();
                String[] rolesOfUser = getRolesOfUser(userRegistry, userRegistry.getUserName());
                return Arrays.asList(rolesOfUser).contains(roleToSubscribe) ||
                       isAdmin(userRegistry, rolesOfUser);
            }
        }
        return true;
    }

    public static SubscriptionBean subscribeAndPopulate(UserRegistry userRegistry, String path, String endpoint, String eventName, boolean doRest) {
        SubscriptionBean subscriptionBean = new SubscriptionBean();
        Resource resource = null;
        String url = null;
        String userName = null;
        String delimiter="";

        String[] temp = path.split("/");
        if (path.lastIndexOf("/") != 0) {
            path = path.substring(0, path.lastIndexOf("/"));
            if (temp != null && temp.length != 0) {
                if (temp[temp.length - 1].equals("#") || temp[temp.length - 1].equals("*")) {
                    delimiter = temp[temp.length - 1];
                } else {
                    if (!path.endsWith("/")) {
                        path = path + "/" + temp[temp.length - 1];
                    }
                }
            }
        } else {
            if (path.contains("*") || path.contains("#")) {
                path = path.substring(0, path.length() - 1);
                if (temp != null && temp.length != 0) {
                    if (temp[temp.length - 1].equals("#") || temp[temp.length - 1].equals("*")) {
                        delimiter = temp[temp.length - 1];
                    }
                }
            }
        }

        try {
            resource = userRegistry.get(path);
        } catch (Exception e) {
            log.warn("Unable to fetch Resource at path: " + path);
            resource = null;
        }
        if (resource != null) {
            String isLink = resource.getProperty("registry.link");
            String mountPoint = resource.getProperty("registry.mountpoint");
            String targetPoint = resource.getProperty("registry.targetpoint");
            String realPath = resource.getProperty("registry.realpath");
            String actualPath = resource.getProperty("registry.actualpath");
            userName = resource.getProperty("registry.user");
            if (isLink != null && mountPoint != null && targetPoint != null) {
//                path = path.replace(mountPoint, targetPoint);
                path = actualPath;
            } else if (isLink != null && realPath != null && userName != null) {
                log.debug("Found mounted resource at: " + realPath);
                if (!realPath.contains("/registry/resourceContent?")) {
                    path = realPath;
                } else {
                	boolean isLocalMount = false;
                	try {
                		isLocalMount = ResourceUtil.isLocalMount(realPath);
					} catch (RegistryException e) {
						log.error("Unable to check whether resource is locally mounted", e);
					}
                	if(!isLocalMount) {
						url = realPath.substring(0, realPath.indexOf("/resourceContent?path="));
					}
                }
            }
        }
        ResourcePath resourcePath = new ResourcePath(path);
        try {
            subscriptionBean.setLoggedIn(!RegistryConstants.ANONYMOUS_USER.equals(userRegistry.getUserName()));
            List<SubscriptionInstance> subscriptionInstances = new LinkedList<SubscriptionInstance>();
            if (!subscriptionBean.getLoggedIn()) {
                throw new SecurityException("User is not logged in");
            } else if (!isAuthorized(userRegistry, path, ActionConstants.GET)) {
                throw new SecurityException("User does not have enough priviledges to subscribe");
            } else if (!hasPermissionToSubscribeViaEmail(userRegistry, path, endpoint)) {
                throw new SecurityException("User does not have enough priviledges to subscribe another user");
            } else if (Utils.getRegistryEventingService() == null) {
                throw new IllegalStateException("Registry Eventing Service Not Found");
            } else {
                String topic = RegistryEventingConstants.TOPIC_PREFIX + RegistryEvent.TOPIC_SEPARATOR + eventName + path;
                if (delimiter.equals("#") || delimiter.equals("*")) {
                    if (topic.endsWith("/")) {
                        topic = topic + delimiter;
                    } else {
                        topic = topic + RegistryEvent.TOPIC_SEPARATOR + delimiter;
                    }
                }

                Subscription subscription =
                        BuilderUtils.createSubscription(endpoint,
                                "http://wso2.org/registry/eventing/dialect/topicFilter", topic);

                subscription.setEventDispatcherName(RegistryEventingConstants.TOPIC_PREFIX);
                int callerTenantId = userRegistry.getCallerTenantId();
                subscription.setTenantId(callerTenantId);
                String name = userRegistry.getUserName();
                if (callerTenantId != MultitenantConstants.SUPER_TENANT_ID &&
                        callerTenantId > -1) {
                    try {
                        PrivilegedCarbonContext.startTenantFlow();
                        PrivilegedCarbonContext currentContext =
                                PrivilegedCarbonContext.getThreadLocalCarbonContext();
                        currentContext.setTenantId(callerTenantId, true);
                        String tenantDomain = currentContext.getTenantDomain();
                        if (tenantDomain != null &&
                                !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                            name = name + "@" + tenantDomain;
                        }
                    } finally {
                        PrivilegedCarbonContext.endTenantFlow();
                    }
                }
                subscription.setOwner(name);
                Map<String, String> data = new HashMap<String, String>();
                if (doRest) {
                    data.put(RegistryEventingConstants.DO_REST, Boolean.toString(Boolean.TRUE));
                }
                subscription.setProperties(data);
                String subscriptionId;
                if (url == null || userName == null) {
                    subscriptionId =
                            Utils.getRegistryEventingService().subscribe(subscription);
                } else {
                    throw new UnsupportedOperationException("You cannot directly subscribe to a " +
                            "Remote Resource. Use the Registry Browser User Interface to add a " +
                            "Remote Subscription.");
                }
                if (subscriptionId == null) {
                    throw new IllegalStateException("Subscription Id invalid");
                }
                subscription.setId(subscriptionId);
                SubscriptionInstance subscriptionInstance = populate(path,subscription,delimiter);
                if (subscriptionInstance != null) {
                    subscriptionInstance.setOwner(userRegistry.getUserName());
                    subscriptionInstances.add(subscriptionInstance);
                }
            }
            subscriptionBean.setSubscriptionInstances(subscriptionInstances.toArray(new SubscriptionInstance[0]));
            subscriptionBean.setPathWithVersion(resourcePath.getPathWithVersion());
            subscriptionBean.setVersionView(!resourcePath.isCurrentVersion());
            subscriptionBean.setUserName(userRegistry.getUserName());
            subscriptionBean.setRoles(getRolesOfUser(userRegistry, userRegistry.getUserName()));
            if (isAuthorized(userRegistry, path, AccessControlConstants.AUTHORIZE)) {
                subscriptionBean.setUserAccessLevel(SubscriptionBean.UserAccessLevel.AUTHORIZE);
            } else if (isAuthorized(userRegistry, path, ActionConstants.DELETE)) {
                subscriptionBean.setUserAccessLevel(SubscriptionBean.UserAccessLevel.DELETE);
            } else if (isAuthorized(userRegistry, path, ActionConstants.GET)) {
                subscriptionBean.setUserAccessLevel(SubscriptionBean.UserAccessLevel.READ);
            } else {
                subscriptionBean.setUserAccessLevel(SubscriptionBean.UserAccessLevel.NONE);
            }
            if (isAdmin(userRegistry, getRolesOfUser(userRegistry, userRegistry.getUserName()))) {
                subscriptionBean.setRoleAccessLevel(1);
            } else {
                subscriptionBean.setRoleAccessLevel(0);
            }
        } catch (RuntimeException e) {
            String msg = "Failed to subscribe to information of the resource " +
                    resourcePath + ". " + e.getMessage();
            log.error("Failed to subscribe to information of the resource " +
                    resourcePath + ".", e);
            subscriptionBean.setErrorMessage(msg);
        } catch (InvalidMessageException e) {
            String msg = "Failed to subscribe to information of the resource " +
                    resourcePath + ". " + e.getMessage();
            log.error("Failed to subscribe to information of the resource " +
                    resourcePath + ".", e);
            subscriptionBean.setErrorMessage(msg);
        }
        return subscriptionBean;
    }

}
