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

import javax.jcr.RepositoryException;
import javax.jcr.security.AccessControlEntry;
import javax.jcr.security.AccessControlException;
import javax.jcr.security.AccessControlList;
import javax.jcr.security.Privilege;
import java.security.Principal;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class RegistryAccessControlList implements AccessControlList {

    private Set<AccessControlEntry> accessCtrList = new HashSet<AccessControlEntry>();

    public AccessControlEntry[] getAccessControlEntries() throws RepositoryException {

        return accessCtrList.toArray(new AccessControlEntry[accessCtrList.size()]);
    }

    public boolean addAccessControlEntry(Principal principal, Privilege[] privileges) throws AccessControlException, RepositoryException {

        boolean duplicatePrincilple = false;
        Iterator it = accessCtrList.iterator();

        while (it.hasNext()) {
            AccessControlEntry tempRegAccEntry = (RegistryAccessControlEntry) it.next();
            accessCtrList.remove(tempRegAccEntry);

            if ((it.next() != null) && ((tempRegAccEntry.getPrincipal().equals(principal)))) {
                Privilege[] tempPrev = new RegistryPrivilege[tempRegAccEntry.getPrivileges().length + privileges.length];
                int j = 0;

                for (int i = 0; i < tempRegAccEntry.getPrivileges().length; i++) {

                    tempPrev[i] = tempRegAccEntry.getPrivileges()[i];
                }

                for (int i = tempRegAccEntry.getPrivileges().length; i < tempPrev.length; i++) {

                    tempPrev[i] = privileges[j];
                    j++;
                }

                AccessControlEntry accEntry = new RegistryAccessControlEntry(principal, tempPrev);
                accessCtrList.add(accEntry);
                duplicatePrincilple = true;
                break;

            }
        }

        if (!duplicatePrincilple) {
            AccessControlEntry accEntry = new RegistryAccessControlEntry(principal, privileges);
            accessCtrList.add(accEntry);
        }
        return duplicatePrincilple;
    }

    public void removeAccessControlEntry(AccessControlEntry accessControlEntry) throws AccessControlException, RepositoryException {

        accessCtrList.remove(accessControlEntry);

    }
}
