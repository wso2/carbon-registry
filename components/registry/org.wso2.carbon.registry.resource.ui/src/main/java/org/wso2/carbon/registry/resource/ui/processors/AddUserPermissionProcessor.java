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

import org.wso2.carbon.registry.common.ui.UIException;
import org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient;
import org.wso2.carbon.utils.ServerConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;

@Deprecated
public class AddUserPermissionProcessor {

    private static final Log log = LogFactory.getLog(AddUserPermissionProcessor.class);

    public static void process(
            HttpServletRequest request, HttpServletResponse response, ServletConfig config)
            throws UIException {

        throw new UnsupportedOperationException("This operation is no longer supported");
        
        /*String pathToAuthorize = request.getParameter("pathToAuthorize");
        String userToAuthorize = request.getParameter("userToAuthorize");
        String actionToAuthorize = request.getParameter("actionToAuthorize");
        String permissionType = request.getParameter("permissionType");

        String cookie = (String) request.
                getSession().getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        try {
            ResourceServiceClient client =
                    new ResourceServiceClient(cookie, config, request.getSession());
            client.addUserPermission(
                    pathToAuthorize, userToAuthorize, actionToAuthorize, permissionType);
        } catch (Exception e) {
            String msg = "Failed to add new user permission. " + e.getMessage();
            log.error(msg, e);
            throw new UIException(msg, e);
        }*/
    }
}
