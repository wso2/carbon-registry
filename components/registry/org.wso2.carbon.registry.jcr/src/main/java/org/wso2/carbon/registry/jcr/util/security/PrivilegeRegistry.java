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

package org.wso2.carbon.registry.jcr.util.security;

import org.wso2.carbon.registry.jcr.security.RegistryPrivilege;

import javax.jcr.security.AccessControlException;
import javax.jcr.security.Privilege;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PrivilegeRegistry {

    private static int HASH_COUNT = 0;
    private Map<String, Privilege> localPrivilegeCache;
    private static Set<RegistryPrivilegeDefinition> REGISTERED_JCR_PRIVELEDGES;

    public Map<String, Privilege> getLocalPrivilegeCache() {
        return localPrivilegeCache;
    }

    private static RegistryPrivilegeDefinition READ_PRIVILEGE;
    private static RegistryPrivilegeDefinition ADD_CHILD_NODES_PRIVILEGE;
    private static RegistryPrivilegeDefinition REMOVE_CHILD_NODES_PRIVILEGE;
    private static RegistryPrivilegeDefinition MODIFY_PROPERTIES_PRIVILEGE;
    private static RegistryPrivilegeDefinition REMOVE_NODE_PRIVILEGE;
    private static RegistryPrivilegeDefinition READ_AC_PRIVILEGE;
    private static RegistryPrivilegeDefinition MODIFY_AC_PRIVILEGE;
    private static RegistryPrivilegeDefinition NODE_TYPE_MANAGEMENT_PRIVILEGE;
    private static RegistryPrivilegeDefinition VERSION_MANAGEMENT_PRIVILEGE;
    private static RegistryPrivilegeDefinition LOCK_MANAGEMENT_PRIVILEGE;
    private static RegistryPrivilegeDefinition LIFECYCLE_MANAGEMENT_PRIVILEGE;
    private static RegistryPrivilegeDefinition RETENTION_MANAGEMENT_PRIVILEGE;
    private static RegistryPrivilegeDefinition WRITE_PRIVILEGE;
    private static RegistryPrivilegeDefinition ALL_PRIVILEGE;

    private static RegistryPrivilegeDefinition registerPrivilege(RegistryPrivilegeDefinition registryPrivilegeDefinition) {
        REGISTERED_JCR_PRIVELEDGES.add(registryPrivilegeDefinition);
        return registryPrivilegeDefinition;
    }

    public static void registerAllPrivileges() {

        READ_PRIVILEGE = registerPrivilege(new RegistryPrivilegeDefinition(Privilege.JCR_READ));

        ADD_CHILD_NODES_PRIVILEGE = registerPrivilege(
                new RegistryPrivilegeDefinition(Privilege.JCR_ADD_CHILD_NODES));
        REMOVE_CHILD_NODES_PRIVILEGE = registerPrivilege(
                new RegistryPrivilegeDefinition(Privilege.JCR_REMOVE_CHILD_NODES));
        MODIFY_PROPERTIES_PRIVILEGE = registerPrivilege(
                new RegistryPrivilegeDefinition(Privilege.JCR_MODIFY_PROPERTIES));

        REMOVE_NODE_PRIVILEGE = registerPrivilege(
                new RegistryPrivilegeDefinition(Privilege.JCR_REMOVE_NODE));
        READ_AC_PRIVILEGE = registerPrivilege(
                new RegistryPrivilegeDefinition(Privilege.JCR_READ_ACCESS_CONTROL));

        MODIFY_AC_PRIVILEGE = registerPrivilege(
                new RegistryPrivilegeDefinition(Privilege.JCR_MODIFY_ACCESS_CONTROL));
        NODE_TYPE_MANAGEMENT_PRIVILEGE = registerPrivilege(
                new RegistryPrivilegeDefinition(Privilege.JCR_NODE_TYPE_MANAGEMENT));
        VERSION_MANAGEMENT_PRIVILEGE = registerPrivilege(
                new RegistryPrivilegeDefinition(Privilege.JCR_VERSION_MANAGEMENT));
        LOCK_MANAGEMENT_PRIVILEGE = registerPrivilege(
                new RegistryPrivilegeDefinition(Privilege.JCR_LOCK_MANAGEMENT));
        LIFECYCLE_MANAGEMENT_PRIVILEGE = registerPrivilege(
                new RegistryPrivilegeDefinition(Privilege.JCR_LIFECYCLE_MANAGEMENT));

        RETENTION_MANAGEMENT_PRIVILEGE = registerPrivilege(
                new RegistryPrivilegeDefinition(Privilege.JCR_RETENTION_MANAGEMENT));

        WRITE_PRIVILEGE = registerPrivilege(
                new RegistryPrivilegeDefinition(Privilege.JCR_WRITE, new RegistryPrivilegeDefinition[]{
                        MODIFY_PROPERTIES_PRIVILEGE,
                        ADD_CHILD_NODES_PRIVILEGE,
                        REMOVE_CHILD_NODES_PRIVILEGE,
                        REMOVE_NODE_PRIVILEGE,
                }));

        ALL_PRIVILEGE = registerPrivilege(
                new RegistryPrivilegeDefinition(Privilege.JCR_ALL, new RegistryPrivilegeDefinition[]{
                        READ_PRIVILEGE,
                        WRITE_PRIVILEGE,
                        READ_AC_PRIVILEGE,
                        MODIFY_AC_PRIVILEGE,
                        NODE_TYPE_MANAGEMENT_PRIVILEGE,
                        VERSION_MANAGEMENT_PRIVILEGE,
                        LOCK_MANAGEMENT_PRIVILEGE,
                        LIFECYCLE_MANAGEMENT_PRIVILEGE,
                        RETENTION_MANAGEMENT_PRIVILEGE
                }));


    }

    public PrivilegeRegistry() {
        init();
    }

    public void refreshPrivRegistry() {
        init();
    }

    private void init() {
        REGISTERED_JCR_PRIVELEDGES = new HashSet<RegistryPrivilegeDefinition>();
        HASH_COUNT = 0;
        registerAllPrivileges();
        localPrivilegeCache = new HashMap<String, Privilege>(REGISTERED_JCR_PRIVELEDGES.size());
        for (RegistryPrivilegeDefinition rpd : REGISTERED_JCR_PRIVELEDGES) {
            Privilege priv = new RegistryPrivilege(rpd);
            localPrivilegeCache.put(priv.getName(), priv);
        }
    }

    public Privilege getPrivilegeFromName(String name) throws AccessControlException {
        String key = RegistryPrivilegeDefinition.getNameFromPrefix(name);
        if (localPrivilegeCache.containsKey(key)) {
            return localPrivilegeCache.get(key);
        } else {
            throw new AccessControlException(key + " isn't the name of a known privilege.");
        }

    }

    public Privilege[] getRegisteredPrivileges() {
        return localPrivilegeCache.values().toArray(new Privilege[localPrivilegeCache.size()]);
    }

    public static int getHashCounter() {
        return HASH_COUNT++;
    }


}
