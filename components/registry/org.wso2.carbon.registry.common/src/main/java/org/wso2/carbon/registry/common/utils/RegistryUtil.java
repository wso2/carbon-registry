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

package org.wso2.carbon.registry.common.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.context.MessageContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.RegistryConstants;

import javax.servlet.http.HttpServletRequest;

public class RegistryUtil {

    private static final Log log = LogFactory.getLog(RegistryUtil.class);

    /**
     * Returns the resource path, which should be used to generate UIs. This will be set by the
     * metadata UI, when it is loading. So all other UIs in the resources page should load after
     * that.
     *
     * @param request this is used to get the current path set by the metadata UI.
     * @return current resource path
     */
    public static String getPath(HttpServletRequest request) {
        String path = request.getParameter("path");
        if (path == null || "".equals(path)) {
            path = (String) request.getAttribute("path");
        }

        return path;
    }

    public static String getSessionResourcePath() throws RegistryException {

        MessageContext messageContext = MessageContext.getCurrentMessageContext();
        if (messageContext == null) {
            String msg = "Could not get the user's Registry session. Message context not found.";
            log.error(msg);
            throw new RegistryException(msg);
        }

        HttpServletRequest request =
                (HttpServletRequest) messageContext.getProperty("transport.http.servletRequest");
        String resourcePath = (String) request.getSession().
                getAttribute(RegistryConstants.SESSION_RESOURCE_PATH);

        return resourcePath;
    }

    public static void setSessionResourcePath(String path) throws RegistryException {

        MessageContext messageContext = MessageContext.getCurrentMessageContext();
        if (messageContext == null) {
            String msg = "Could not get the user's Registry session. Message context not found.";
            log.error(msg);
            throw new RegistryException(msg);
        }

        HttpServletRequest request =
                (HttpServletRequest) messageContext.getProperty("transport.http.servletRequest");
        request.getSession().setAttribute(RegistryConstants.SESSION_RESOURCE_PATH, path);
    }

    public static String getResourcePath() throws RegistryException {

        MessageContext messageContext = MessageContext.getCurrentMessageContext();
        if (messageContext == null) {
            String msg = "Could not get the user's Registry session. Message context not found.";
            log.error(msg);
            throw new RegistryException(msg);
        }

        HttpServletRequest request =
                (HttpServletRequest) messageContext.getProperty("transport.http.servletRequest");
        String resourcePath = (String) request.getSession().
                getAttribute(RegistryConstants.SESSION_RESOURCE_PATH);
        if (resourcePath == null) {
            resourcePath = RegistryConstants.ROOT_PATH;
        }
        return resourcePath;
    }

    public static String generateOptionsFor(String value, String [] options) {
        StringBuffer ret = new StringBuffer();
        for (String option : options) {
            ret.append("<option value=\"");
            ret.append(option);
            ret.append("\"");
            if (option.equalsIgnoreCase(value)) {
                ret.append(" selected");
            }
            ret.append(">");
            ret.append(option);
            ret.append("</option>\n");
        }
        return ret.toString();
    }

    public static String getResourcePathFromVersionPath(String path) {
         return path.substring(0,path.indexOf(";version:")) ;
    }

}
