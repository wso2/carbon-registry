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

import javax.jcr.query.qom.Comparison;
import javax.jcr.query.qom.DynamicOperand;
import javax.jcr.query.qom.StaticOperand;

public class RegistryComparison implements Comparison {

    private DynamicOperand operand1 = null;
    private String operator = "";
    private StaticOperand operand2 = null;

    public RegistryComparison(DynamicOperand operand1, String operator, StaticOperand operand2) {

        this.operand1 = operand1;
        this.operator = operator;
        this.operand2 = operand2;

    }

    public DynamicOperand getOperand1() {

        return operand1;
    }

    public String getOperator() {

        return operator;
    }

    public StaticOperand getOperand2() {

        return operand2;
    }
}
