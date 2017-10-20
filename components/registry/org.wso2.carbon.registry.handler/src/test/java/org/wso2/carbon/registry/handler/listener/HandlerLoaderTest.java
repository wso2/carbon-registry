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
package org.wso2.carbon.registry.handler.listener;

import org.junit.Test;
import org.wso2.carbon.core.services.callback.LoginEvent;
import org.wso2.carbon.registry.handler.base.BaseTestCase;
import org.wso2.carbon.registry.handler.util.CommonUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HandlerLoaderTest extends BaseTestCase{

    private String handlerName = "org.wso2.carbon.registry.extensions.handlers.ServiceMediaTypeHandler";
    private String handlerConfiguration = "<handler class=\"org.wso2.carbon.registry.extensions.handlers.ServiceMediaTypeHandler\">" +
            "<filter class=\"org.wso2.carbon.registry.core.jdbc.handlers.filters.MediaTypeMatcher\">" +
            "<property name=\"mediaType\">application/vnd.wso2-service+xml</property>" +
            "</filter>" +
            "</handler>";

    @Test
    public void onLogin() throws Exception {
        CommonUtil.addHandler(configRegistry, handlerConfiguration);
        LoginEvent loginEvent = new LoginEvent();
        loginEvent.setUsername("admin");
        loginEvent.setTenantDomain("carbon.super");
        loginEvent.setTenantId(-1234);

        HandlerLoader handlerLoader = new HandlerLoader();
        handlerLoader.onLogin(configRegistry, loginEvent);

        String[] handlers = CommonUtil.getHandlerList(configRegistry);
        assertNotNull(handlers);
        assertEquals(1, handlers.length);
        assertEquals(handlerName, handlers[0]);
        CommonUtil.deleteHandler(configRegistry, handlerName);
    }

}
