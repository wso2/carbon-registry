/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.handler.beans;

import org.junit.Test;

import static org.junit.Assert.*;

public class HandlerExecutionStatusTest {

    private String handlerName = "org.wso2.carbon.registry.extensions.handlers.ServiceMediaTypeHandler";

    @Test
    public void getHandlerName() throws Exception {
        HandlerExecutionStatus executionStatus = new HandlerExecutionStatus();
        executionStatus.setHandlerName(handlerName);
        assertEquals(handlerName, executionStatus.getHandlerName());
    }

    @Test
    public void getExecutionStatus() throws Exception {
        HandlerExecutionStatus executionStatus = new HandlerExecutionStatus();
        executionStatus.setExecutionStatus("true");
        assertEquals("true", executionStatus.getExecutionStatus());
    }

}
