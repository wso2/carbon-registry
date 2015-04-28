/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.core.subscription.Subscription;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.AccessControlConstants;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.info.Utils;


public class InfoUtil {

    private static final Log log = LogFactory.getLog(InfoUtil.class);

    /**
     * This method used to unsubscribe from registry notifications
     * @param registry
     * @param path
     * @param id
     * @param sessionId
     * @return
     * @throws RegistryException
     */
    public static boolean unsubscribe(UserRegistry registry, String path, String id, String sessionId) throws RegistryException {
        if (RegistryUtils.isRegistryReadOnly(registry.getRegistryContext())) {
            return false;
        }
        String url = null;
        String userName = null;
        if (registry.resourceExists(path)) {
            Resource resource = registry.get(path);
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
        }
        log.debug("got path: " + path);
        if (RegistryConstants.ANONYMOUS_USER.equals(registry.getUserName())) {
            log.warn("User is anonymous, can't unsubscribe");
            return false;
        }
        if (Utils.getRegistryEventingService() == null) {
            log.warn("No event source found, can't unsubscribe");
            return false;
        }
        try {
            Subscription subscription = null;
            if (url == null || userName == null) {
                subscription = Utils.getRegistryEventingService().getSubscription(id);
            } else  {
                subscription = Utils.getRegistryEventingService().getSubscription(id, userName,
                                                                                  url);
            }

            if (subscription == null) {
                log.warn("Subscription not found, can't unsubscribe");
                return false;
            }
            if (subscription.getTenantId() != registry.getCallerTenantId()) {
                log.warn("TenantId for subscription doesn't match with the logged-in tenant");
                return false;
            }
            String username = subscription.getOwner();
            if (username.indexOf("@") > 0) {
                username = username.split("@")[0];
            }
            if (username == null || !username.equals(registry.getUserName())) {
                if (!SubscriptionBeanPopulator.isAuthorized(registry, path,
                                                            AccessControlConstants.AUTHORIZE)) {
                    log.warn("User doesn't have AUTHORIZE priviledges, can't unsubscribe");
                    return false;
                }
            } else if (!SubscriptionBeanPopulator.isAuthorized(registry, path,
                                                               ActionConstants.GET)) {
                return false;
            }
            if (url == null || userName == null) {
                return Utils.getRegistryEventingService().unsubscribe(id);
            } else  {
                return Utils.getRegistryEventingService().unsubscribe(id, userName, url);
            }

        } catch (Exception e) {
            return false;
        }
    }

}
