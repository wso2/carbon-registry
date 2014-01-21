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

import javax.jcr.query.qom.Join;
import javax.jcr.query.qom.JoinCondition;
import javax.jcr.query.qom.Source;


public class RegistryJoin implements Join {

    private Source left = null;
    private Source right = null;
    private String joinType = "";
    private JoinCondition joinCondition = null;

    public RegistryJoin(Source left, Source right, String joinType, JoinCondition joinCondition) {

        this.left = left;
        this.right = right;
        this.joinType = joinType;
        this.joinCondition = joinCondition;
    }

    public Source getLeft() {
        return left;
    }

    public Source getRight() {
        return right;
    }

    public String getJoinType() {
        return joinType;
    }

    public JoinCondition getJoinCondition() {
        return joinCondition;
    }

}
