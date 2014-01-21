/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.resource.ui.processors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.common.ui.UIException;
import org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DeleteProcessor {

    private static final Log log = LogFactory.getLog(DeleteProcessor.class);

    public static void process(
            HttpServletRequest request, HttpServletResponse response, ServletConfig config)
            throws UIException {

        String pathToDelete = request.getParameter("pathToDelete");

         String cookie = (String) request.
                getSession().getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        try {
            ResourceServiceClient client =
                    new ResourceServiceClient(cookie, config, request.getSession());
            client.delete(pathToDelete);
        } catch (Exception e) {
            String msg = "Failed to delete " + pathToDelete + ". " + e.getMessage();
            log.error(msg, e);
            throw new UIException(e);
        }
    }
}
