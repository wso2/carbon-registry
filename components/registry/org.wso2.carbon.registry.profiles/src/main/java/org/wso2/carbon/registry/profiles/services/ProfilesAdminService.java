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
package org.wso2.carbon.registry.profiles.services;

import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.registry.admin.api.profiles.IProfilesAdminService;
import org.wso2.carbon.registry.common.services.RegistryAbstractAdmin;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.profiles.beans.ProfilesBean;
import org.wso2.carbon.registry.profiles.utils.CommonUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.core.*;
import org.wso2.carbon.user.core.claim.Claim;


public class ProfilesAdminService extends RegistryAbstractAdmin implements
        IProfilesAdminService<ProfilesBean> {

    /* public void addProfiles(String path, Map<String,String> properties,String username,String profilename) throws RegistryException,UserStoreException{
        DefaultUserStoreAdmin userstoreadmin = CommonUtil.getUserStoreAdmin();
        userstoreadmin.setUserProperties(username,properties,profilename);
    }
     public void setProfileProperty(String path,String attributeid,String attributevalue,String username,String profilename) throws RegistryException,UserStoreException{
        DefaultUserStoreAdmin userstoreadmin = CommonUtil.getUserStoreAdmin();
        userstoreadmin.setUserProperty(username,attributeid,attributevalue,profilename);
    }
    public void deleteUserProperty(String path,String property,String username,String profilename) throws RegistryException,UserStoreException{
        DefaultUserStoreAdmin userstoreadmin = CommonUtil.getUserStoreAdmin();
        userstoreadmin.deleteUserProperty(username,property,profilename);
    }
    public void deleteUserProperties(String path,String[] property,String username,String profilename) throws RegistryException,UserStoreException{
        DefaultUserStoreAdmin userstoreadmin = CommonUtil.getUserStoreAdmin();
        userstoreadmin.deleteUserProperties(username,property,profilename);
    }
     public void setUserBinaryContent(String path,String attributeid,String attributevalue,String username,String profilename) throws RegistryException,UserStoreException{
        DefaultUserStoreAdmin userstoreadmin = CommonUtil.getUserStoreAdmin();
        userstoreadmin.setUserProperty(username,attributeid,attributevalue,profilename);
    }*/
    public boolean putUserProfile(String path)throws RegistryException,UserStoreException{
        Registry registry = getConfigSystemRegistry();
        if (RegistryUtils.isRegistryReadOnly(registry.getRegistryContext())) {
            return false;
        }
        String [] allpath = path.split("/");
        String username = allpath[allpath.length - 2];
        UserRegistry userRegistry = (UserRegistry)getRootRegistry();
    	UserRealm realm = userRegistry.getUserRealm();
        boolean isAdmin = false;

        String[] userRoles = realm.getUserStoreManager().getRoleListOfUser(userRegistry.getUserName());

        for (String userRole: userRoles) {
            if (userRole.equals(realm.getRealmConfiguration().getAdminRoleName())) {
                isAdmin = true;
                break;
            }
        }

        if (username == null || (!username.equals(userRegistry.getUserName()) && !isAdmin)) {
            return false;
        }
        UserStoreManager userprofile = realm.getUserStoreManager();
        int total = 0;
        int empty = 0;
        if (!userprofile.isExistingUser(username)) {
            return false;
        }
        String [] profilenames = userprofile.getProfileNames(username);
        /*boolean isProfileAdmin = false;
        String[] adminUsers = realm.getUserStoreManager().getUserListOfRole(
                realm.getRealmConfiguration().getAdminRoleName());
        for (String admin: adminUsers) {
            if (username.equals(admin)) {
                isProfileAdmin = true;
                break;
            }
        }*/
        //find whether user is giving a profile name that profile is not filled. //
        for(String temp:profilenames){

                Claim[] claimobjs = userprofile.getUserClaimValues(username,temp);
                for(Claim tmpclaims:claimobjs){
                    total++;
                    if(tmpclaims.getValue() == null){
                        empty++;
                    }
                }
            }
        if(empty == total){
            return false;
        }
        Resource resource = null;
        if(registry.resourceExists(path)){
            return false;
        }
        resource = registry.newResource();
        resource.setMediaType(RegistryConstants.PROFILES_MEDIA_TYPE);
        registry.put(path,resource);
        /*if (!isProfileAdmin) {
            return true;
        }*/

        String everyoneRole = realm.getRealmConfiguration().getEveryOneRoleName();
        try {
            realm.getAuthorizationManager().denyRole(everyoneRole,
                    RegistryConstants.CONFIG_REGISTRY_BASE_PATH + path, ActionConstants.GET);
            realm.getAuthorizationManager().authorizeUser(username,
                    RegistryConstants.CONFIG_REGISTRY_BASE_PATH + path, ActionConstants.GET);
        } catch (UserStoreException e) {
            throw e;
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    public ProfilesBean getUserProfile(String path) throws RegistryException,UserStoreException{
        Registry registry = getConfigSystemRegistry();
        Resource resource = registry.get(path);
        String contentString = (String)resource.getContent();

        ProfilesBean profbean = new ProfilesBean();
        profbean.setMainDataString(contentString);
//        profilebean.calculatevalues();
        return profbean;
    }
}
