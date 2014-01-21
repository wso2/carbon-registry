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

import org.wso2.carbon.registry.jcr.util.RegistryJCRItemOperationUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class RegistryPrivilegeDefinition {


    private String name;
    private boolean isAbstract;
    private boolean isAggregate;
    private RegistryPrivilegeDefinition[] declaredAggregatePrivileges;
    private Set<RegistryPrivilegeDefinition> aggregatePrivileges;
    private String URI_NAME="";

    public int getHASH_CODE() {
        return HASH_CODE;
    }

    private int HASH_CODE = 0;

    public RegistryPrivilegeDefinition(String name, RegistryPrivilegeDefinition[] declaredAggregatePrivileges) {
        this.HASH_CODE=PrivilegeRegistry.getHashCounter();
        this.URI_NAME=name;
        this.name = getNameFromPrefix(name);
        this.isAbstract=false;
        this.isAggregate=true;
        this.declaredAggregatePrivileges = Arrays.copyOf(declaredAggregatePrivileges, declaredAggregatePrivileges.length);
        Set<RegistryPrivilegeDefinition> tempAggregates = new HashSet<RegistryPrivilegeDefinition>();
        for(RegistryPrivilegeDefinition priv:declaredAggregatePrivileges){
               if(priv.isAggregate()) {
                 tempAggregates.addAll(priv.getAggregatePrivileges());
                 tempAggregates.add(priv);
               }else {
                   tempAggregates.add(priv);
               }
        }
       aggregatePrivileges = Collections.unmodifiableSet(tempAggregates);
    }

    public RegistryPrivilegeDefinition(String name) {
        this.HASH_CODE=PrivilegeRegistry.getHashCounter();
        this.URI_NAME=name;
        this.name = getNameFromPrefix(name);
        this.isAbstract=false;
        this.isAggregate=false;
        this.declaredAggregatePrivileges=null;
        this.aggregatePrivileges=null;

    }

    public String getURI_NAME() {
        return URI_NAME;
    }

    public static String getNameFromPrefix(String _name){
       if(_name.contains("{")) {
           return RegistryJCRItemOperationUtil.replaceNameSpacePrefixURIS(_name);
       } else {
         return _name;
       }
    }



    public String getName() {
        return name;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public Set<RegistryPrivilegeDefinition> getAggregatePrivileges() {
        return aggregatePrivileges;
    }

    public boolean isAggregate() {
        return isAggregate;
    }

    public RegistryPrivilegeDefinition[] getDeclaredAggregatePrivileges() {
        return declaredAggregatePrivileges;
    }

}
