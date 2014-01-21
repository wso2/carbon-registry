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

import javax.jcr.security.AccessControlPolicy;
import javax.jcr.security.AccessControlPolicyIterator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class RegistryAccessControlPolicyIterator implements AccessControlPolicyIterator {

    private Set policies = new HashSet();
    private Iterator pilicyIter;
    private int counter = 0;

    public RegistryAccessControlPolicyIterator(Set policies) {

        this.policies = policies;
        pilicyIter = this.policies.iterator();
    }

    public AccessControlPolicy nextAccessControlPolicy() {

        return (AccessControlPolicy) next();
    }

    public void skip(long l) {

    }

    public long getSize() {

        return policies.size();
    }

    public long getPosition() {

        return counter;
    }

    public boolean hasNext() {


        return pilicyIter.hasNext();
    }

    public Object next() {

        counter++;
        return pilicyIter.next();

    }

    public void remove() {

        pilicyIter.remove();
    }
}
