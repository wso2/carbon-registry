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

import org.wso2.carbon.registry.jcr.util.security.RegistryPrivilegeDefinition;

import javax.jcr.security.Privilege;
import java.util.ArrayList;
import java.util.List;


public class RegistryPrivilege implements Privilege {

   private RegistryPrivilegeDefinition rpd;



    public RegistryPrivilege(RegistryPrivilegeDefinition registryPrivilegeDefinition) {
        this.rpd = registryPrivilegeDefinition;
    }

    public String getName() {
        return rpd.getName();
    }

    public boolean isAbstract() {
        return rpd.isAbstract();
    }

    public boolean isAggregate() {
        return rpd.isAggregate();
    }

    public Privilege[] getDeclaredAggregatePrivileges() {
        List<Privilege> privileges = new ArrayList<Privilege>();
        for (RegistryPrivilegeDefinition priv : rpd.getDeclaredAggregatePrivileges()) {
            privileges.add(new RegistryPrivilege(priv));
        }
        return privileges.toArray(new Privilege[privileges.size()]);
    }

    public Privilege[] getAggregatePrivileges() {
        List<Privilege> privileges = new ArrayList<Privilege>();
        for (RegistryPrivilegeDefinition priv : rpd.getAggregatePrivileges()) {
            privileges.add(new RegistryPrivilege(priv));
        }
        return privileges.toArray(new Privilege[privileges.size()]);
    }

        @Override
    public int hashCode() {
            return rpd.getHASH_CODE();
    }

          @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof RegistryPrivilege) {
                return hashCode() == ((RegistryPrivilege) obj).hashCode();
            }
            return false;
        }

}
