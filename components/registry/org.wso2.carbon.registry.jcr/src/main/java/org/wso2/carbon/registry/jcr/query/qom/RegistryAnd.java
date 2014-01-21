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

package org.wso2.carbon.registry.jcr.query.qom;

import javax.jcr.query.qom.And;
import javax.jcr.query.qom.Constraint;
import java.util.Set;


public class RegistryAnd implements And {

    private Constraint constraint1;
    private Constraint constraint2;

    public RegistryAnd(Constraint constraint1, Constraint constraint2) {

        this.constraint1 = constraint1;
        this.constraint2 = constraint2;

    }


    public Constraint getConstraint1() {

        return constraint1;
    }

    public Constraint getConstraint2() {

        return constraint2;
    }

    public Set evalAnd(Set set) {

//        Set set1 = evalCons1(set);
//        Set set2 = evalCons2(set);


        return null;
    }

    private Set evalCons1(Set set) {


        return null;
    }

    private Set evalCons2(Set set) {


        return null;
    }
}
