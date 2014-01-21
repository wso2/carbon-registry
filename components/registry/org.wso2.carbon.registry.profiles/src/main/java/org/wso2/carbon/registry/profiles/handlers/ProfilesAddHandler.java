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

package org.wso2.carbon.registry.profiles.handlers;

import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;
import org.wso2.carbon.user.core.*;
import org.wso2.carbon.user.core.claim.Claim;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Handler that will extract profile details of a user and present it as a resource on the registry.
 */
public class ProfilesAddHandler extends Handler {
    private static final Log log = LogFactory.getLog(ProfilesAddHandler.class);
     public void put(RequestContext requestContext) throws RegistryException {
//        String path = requestContext.getResourcePath().getPath();
//        UserStoreAdmin userprofile = null;
//        String [] allpath = path.split("/");
//        String username = allpath[allpath.length - 1];
//        RegistryContext registrycontext = requestContext.getRegistry().getRegistryContext();
//        try{
//            userprofile = registrycontext.getUserRealm().getUserStoreAdmin();
//            /* check the existense of the user in the user manager */
//            if(!userprofile.isExistingUser(username))
//            {
//                if(username.equals("users")){
//                    log.error("No user with the username:" +  username + " in the User Manager");
//                }
//                return;
//            }
//            if (requestContext.getRegistry().resourceExists(requestContext.getResourcePath().getCompletePath()))
//                return; // If the resource is already there perform the default processing.
//        }catch(UserStoreException e){
//            log.error(e.getMessage());
//        }
//
//        /*if resource is not created yet creating new resource */
////        CollectionImpl collection = new CollectionImpl();
////        collection.setPath(path);
//        Resource resource = requestContext.getRegistry().newResource();
//        resource.setMediaType(RegistryConstants.PROFILES_MEDIA_TYPE);
//        requestContext.getRepository().put(path, collection);
//        String resourcepath = path +  RegistryConstants.PROFILE_RESOURCE_NAME;
//        requestContext.getRepository().put(resourcepath,resource);
    }
    public Resource get(RequestContext requestContext) throws RegistryException{
        if (!CommonUtil.isUpdateLockAvailable()) {
            return null;
        }
        CommonUtil.acquireUpdateLock();
        try {
            String path = requestContext.getResourcePath().getPath();
            UserStoreManager userStoreManager;
            String [] pathParts = path.split("/");
            String username = pathParts[pathParts.length - 2];
            ResourceImpl col = null;
            UserRealm realm = CurrentSession.getUserRealm();

            try{
                userStoreManager = realm.getUserStoreManager();
                /* check the existence of the user in the user manager */
                if(!userStoreManager.isExistingUser(username))
                {

                    if(username.equals("users")){
                        log.error("No user with the username:" +  username + " in the User Manager");
                    }
                    return null;
                }
                StringBuffer resourceContent = new StringBuffer();
                String[] profileNames = userStoreManager.getProfileNames(username);
                for (String temp : profileNames) {
                    Claim[] claimValues = userStoreManager.getUserClaimValues(username, temp);
                    if (claimValues.length != 0) {
                        resourceContent.append(temp).append("%");
                        for (Claim claim : claimValues) {
                            resourceContent.append(claim.getDisplayTag()).append(";").append(
                                    claim.getValue()).append(";");
                        }
                    }
                    resourceContent.append("#");
                }
                Registry registry = requestContext.getRegistry();
                if (registry.resourceExists(path)) {
                    col = (ResourceImpl) registry.get(path);
                    col.setContent(resourceContent.toString());
                }
            } catch (UserStoreException e) {
                log.error("An error occurred while reading profile details", e);
            }
            return col;
        } finally {
            CommonUtil.releaseUpdateLock();
        }
    }
}
