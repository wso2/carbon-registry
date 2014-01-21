/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.jcr.security;

import org.wso2.carbon.registry.jcr.RegistrySession;
import org.wso2.carbon.registry.jcr.util.RegistryJCRSpecificStandardLoderUtil;
import org.wso2.carbon.registry.jcr.util.security.PrivilegeRegistry;

import javax.jcr.*;
import javax.jcr.security.*;
import java.util.*;


public class RegistryAccessControlManager implements AccessControlManager {
    private PrivilegeRegistry privilegeRegistry;
    private RegistrySession registrySession;

    public RegistryAccessControlManager(RegistrySession registrySession) {
        this.privilegeRegistry = new PrivilegeRegistry();
        this.registrySession = registrySession;
    }

    private Map<String, Set<AccessControlPolicy>> accessCtrlPolicies =
            new HashMap<String, Set<AccessControlPolicy>>();


    public PrivilegeRegistry getPrivilegeRegistry() {
        return privilegeRegistry;
    }

    public Privilege[] getSupportedPrivileges(String s) throws RepositoryException {
//        This method does not return the privileges held by the session. Instead, it returns the privileges that the repository supports.
//       TODO   checkValidNodePath(absPath);
        return privilegeRegistry.getRegisteredPrivileges();
    }

    public Privilege privilegeFromName(String s) throws RepositoryException {
        return privilegeRegistry.getPrivilegeFromName(s);
    }

    public boolean hasPrivileges(String s, Privilege[] privileges) throws RepositoryException {

        boolean hasPrivileges = true;
        Item item = registrySession.getItem(s);
        if (item instanceof Property) {
            throw new PathNotFoundException("No privilages can be added for Properties");
        }

        Set<Privilege> temp = new HashSet<Privilege>();
        temp.addAll(Arrays.asList(getPrivileges(s)));
        for (Privilege pv : privileges) {
            if (!temp.contains(pv)) {
                hasPrivileges = false;
            }
        }

        return hasPrivileges;
    }

    public Privilege[] getPrivileges(String s) throws RepositoryException {

        Item item = registrySession.getItem(s);
        if (item instanceof Property) {
            throw new PathNotFoundException("No privilages can be added for Properties");
        }

        Set<Privilege> privileges = new HashSet<Privilege>();

        if (accessCtrlPolicies.get(s) instanceof RegistryAccessControlList) {

            AccessControlEntry[] accessNtries = ((RegistryAccessControlList)
                                                accessCtrlPolicies.get(s)).getAccessControlEntries();

            for (AccessControlEntry ac : accessNtries) {
                if (ac != null) {
                    privileges.addAll(Arrays.asList(ac.getPrivileges()));
                }
            }
        } else {
            //TODO check how to apply NamedAccessControlPolicy
        }

        //Read-only session must have READ privilege on test node
        if(RegistryJCRSpecificStandardLoderUtil.isSessionReadOnly(registrySession.getUserID())
                && !privileges.contains(privilegeRegistry.getPrivilegeFromName(Privilege.JCR_READ))) {
          privileges.add(privilegeRegistry.getPrivilegeFromName(Privilege.JCR_READ));
        }


        if (privileges.size() != 0) {
            return privileges.toArray(new Privilege[privileges.size()]);
        } else return new Privilege[0];
    }

    public AccessControlPolicy[] getPolicies(String s) throws RepositoryException {
        if(RegistryJCRSpecificStandardLoderUtil.isSessionReadOnly(registrySession.getUserID())) {
            throw  new AccessDeniedException("Read only session may not read AC content");
        }

        if (accessCtrlPolicies.get(s) != null) {

            return accessCtrlPolicies.get(s).toArray(
                   new AccessControlPolicy[accessCtrlPolicies.get(s).size()]);
        } else {

            return new AccessControlPolicy[0];
        }
    }

    public AccessControlPolicy[] getEffectivePolicies(String s) throws RepositoryException {

        if(RegistryJCRSpecificStandardLoderUtil.isSessionReadOnly(registrySession.getUserID())) {
            throw  new AccessDeniedException("Read only session may not read AC content");
        }

        Item item = registrySession.getItem(s); // check the validity of the path
        if (item instanceof Property) {
            throw new PathNotFoundException("Cannot apply policies to a property path");
        }
        return getPolicies(s);
    }

    public AccessControlPolicyIterator getApplicablePolicies(String s) throws RepositoryException {

        if(RegistryJCRSpecificStandardLoderUtil.isSessionReadOnly(registrySession.getUserID())) {
            throw  new AccessDeniedException("Read only session may not read AC content");
        }

        if (accessCtrlPolicies.size() != 0) {

            return new RegistryAccessControlPolicyIterator(accessCtrlPolicies.get(s));
        } else {

            return new RegistryAccessControlPolicyIterator(new HashSet());
        }
    }


    public void setPolicy(String s, AccessControlPolicy accessControlPolicy) throws RepositoryException {
        boolean invalidPolicy = true;
        //Invalid policy may not be set by a READ-only session
        if(RegistryJCRSpecificStandardLoderUtil.isSessionReadOnly(registrySession.getUserID())) {
                 if ((accessControlPolicy instanceof RegistryAccessControlList)) {
                     invalidPolicy=false;
                 } else if((accessControlPolicy instanceof RegistryNamedAccessControlPolicy)) {
                     invalidPolicy=false;
                 }
            if(invalidPolicy) {
              throw new AccessControlException("Invalid policy may not be set by a READ-only session");
            }
        }


        if (accessCtrlPolicies.get(s) == null) {

            Set<AccessControlPolicy> policies = new HashSet<AccessControlPolicy>();
            policies.add(accessControlPolicy);
            accessCtrlPolicies.put(s, policies);
        } else {

            accessCtrlPolicies.get(s).add(accessControlPolicy);
//            Set<AccessControlPolicy> temp = accessCtrlPolicies.get(s);
//            temp.add(accessControlPolicy);
//            accessCtrlPolicies.remove(s);
//            accessCtrlPolicies.put(s, temp);

        }
    }

    public void removePolicy(String s, AccessControlPolicy accessControlPolicy) throws RepositoryException {

        Set<AccessControlPolicy> temp = accessCtrlPolicies.get(s);
        temp.remove(accessControlPolicy);
        accessCtrlPolicies.remove(s);
        accessCtrlPolicies.put(s, temp);
    }

}
