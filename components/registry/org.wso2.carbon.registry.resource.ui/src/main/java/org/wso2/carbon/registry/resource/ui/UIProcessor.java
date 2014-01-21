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

package org.wso2.carbon.registry.resource.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.common.utils.CommonUtil;
import org.wso2.carbon.registry.common.ui.UIConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import java.io.PrintWriter;
import java.io.IOException;

public abstract class UIProcessor {

    protected static final Log log = LogFactory.getLog(UIProcessor.class);

    public abstract String process(
            HttpServletRequest request, HttpServletResponse response, ServletConfig config)
            throws Exception;

    protected void sendErrorContent(HttpServletResponse response, String msg) {
        CommonUtil.sendErrorContent(response, msg);
    }

    protected void sendContent(HttpServletResponse response, String msg) {

        try {
            PrintWriter out = response.getWriter();

            out.println(msg);

            out.flush();
            out.close();

        } catch (IOException e) {

            String sendError = "Failed to send message. Caused by " + e.getMessage() +
                    "\nFollowing message was not send to the UI\n" + msg;
            log.error(sendError, e);
        }
    }

    protected void redirect(HttpServletResponse response, String url) {

        try {
            response.sendRedirect(url);
        } catch (IOException e) {
            String msg = "Failed to redirect to the URL " + url + ". \nCaused by " +
                    e.getMessage();
            log.error(msg, e);
        }
    }

    protected void forward(HttpServletRequest request, HttpServletResponse response, String url) {
        try {
            request.getRequestDispatcher(url).forward(request, response);
        } catch (Exception e) {
            String msg = "Failed to forward the request to URL " +
                    url + ". Caused by " + e.getMessage();
            log.error(msg, e);
        }
    }

    protected void setSectionErrorMessage(HttpServletRequest request, String msg) {
        request.getSession().setAttribute(UIConstants.SECTION_ERROR_MESSAGE, msg);
    }

    protected void setSuccessMessage(HttpServletRequest request, String msg) {
        request.getSession().setAttribute(UIConstants.SUCCESS_MESSAGE, msg);
    }

    protected void addErrorMessage(HttpServletRequest request, String msg) {
        CommonUtil.addErrorMessage(request, msg);
    }
}
