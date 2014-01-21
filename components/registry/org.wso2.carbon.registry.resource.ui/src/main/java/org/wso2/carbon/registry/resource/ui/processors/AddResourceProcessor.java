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
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.registry.core.utils.MediaTypesUtils;
import org.wso2.carbon.registry.resource.ui.Utils;
import org.wso2.carbon.server.admin.common.IServerAdmin;
import org.wso2.carbon.server.admin.common.ServerData;
import org.wso2.carbon.server.admin.ui.ServerAdminClient;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient;
import org.wso2.carbon.ui.transports.fileupload.AbstractFileUploadExecutor;
import org.wso2.carbon.ui.CarbonUIMessage;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.ui.transports.fileupload.FileSizeLimitExceededException;
import org.wso2.carbon.ui.transports.fileupload.FileUploadFailedException;
import org.wso2.carbon.utils.FileItemData;
import org.wso2.carbon.utils.ServerConstants;

import javax.activation.DataHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;

public class AddResourceProcessor extends AbstractFileUploadExecutor {

    private static final Log log = LogFactory.getLog(AddResourceProcessor.class);

    public boolean execute(HttpServletRequest request, HttpServletResponse response)
            throws CarbonException, IOException {

        String webContext = (String) request.getAttribute(CarbonConstants.WEB_CONTEXT);
        String serverURL = (String) request.getAttribute(CarbonConstants.SERVER_URL);
        String cookie = (String) request.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        HttpSession session = request.getSession();
        Map<String,ArrayList<FileItemData>> fileItemsMap =  getFileItemsMap();
        Map<String,ArrayList<String>> formFieldsMap =  getFormFieldsMap();

        if (fileItemsMap == null || fileItemsMap.isEmpty()) {
            String msg = "File upload failed. Content is not set properly.";
            log.error(msg);

            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request);
            response.sendRedirect(
                    getContextRoot(request) + "/" + webContext + "/resources/resource.jsp?region=region3&item=resource_browser_menu&errorMsg=" + msg);

            return false;
        }
        String errorRedirect = null;
        try {
            //if (getFormFieldsMap().get().get("errorRedirect") != null) {
            if (formFieldsMap.get("errorRedirect") != null) {
                errorRedirect = formFieldsMap.get("errorRedirect").get(0);
            }
            if (session != null) {
                Object errorAttribute = session.getAttribute("fileSizeExceededError");
                if (errorAttribute != null) {
                    session.removeAttribute("fileSizeExceededError");
                    String msg = "File upload failed. " + errorAttribute;
                    if (errorRedirect == null) {
                        CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request);
                        response.sendRedirect(
                                getContextRoot(request) + "/" + webContext + "/resources/resource.jsp?region=region3&item=resource_browser_menu&errorMsg=" + msg);
                    } else {
                        response.sendRedirect(
                                getContextRoot(request) + "/" + webContext + "/" + errorRedirect +
                                        (errorRedirect.indexOf("?")
                                                == -1 ? "?" : "&") + "msg=" +
                                        URLEncoder.encode(msg, "UTF-8"));
                    }
                    return false;
                }
            }
            ResourceServiceClient client =
                    new ResourceServiceClient(cookie, serverURL, configurationContext);

            String parentPath = null;
            if (formFieldsMap.get("path") != null) {
                parentPath = formFieldsMap.get("path").get(0);
            }
            String resourceName = null;
            if (formFieldsMap.get("filename") != null) {
                resourceName = formFieldsMap.get("filename").get(0);
            }
            String mediaType = null;
            if (formFieldsMap.get("mediaType") != null) {
                mediaType = MediaTypesUtils.getMimeTypeFromHumanReadableMediaType(formFieldsMap.get("mediaType").get(0));
            }
            String description = null;
            if (formFieldsMap.get("description") != null) {
                description = formFieldsMap.get("description").get(0);
            }
            String redirect = null;
            if (formFieldsMap.get("redirect") != null) {
                redirect = formFieldsMap.get("redirect").get(0);
            }
            String symlinkLocation = null;
            if (formFieldsMap.get("symlinkLocation") != null) {
                symlinkLocation = formFieldsMap.get("symlinkLocation").get(0);
            }
            String properties = null;
            if (formFieldsMap.get("properties") != null) {
                properties = formFieldsMap.get("properties").get(0);
            }
            IServerAdmin adminClient =
                    (IServerAdmin) CarbonUIUtil.
                            getServerProxy(new ServerAdminClient(configurationContext,
                                    serverURL, cookie, session), IServerAdmin.class, session);
            ServerData data = null;
            String chroot = "";
            try {
                data = adminClient.getServerData();
            } catch (Exception ignored) {
                // If we can't get server data the chroot cannot be determined.
                chroot = null;
            }
            if (data != null && data.getRegistryType() != null &&
                    data.getRegistryType().equals("remote") &&
                    data.getRemoteRegistryChroot() != null &&
                    !data.getRemoteRegistryChroot().equals(RegistryConstants.PATH_SEPARATOR)) {
                chroot = data.getRemoteRegistryChroot();
                if (!chroot.startsWith(RegistryConstants.PATH_SEPARATOR)) {
                    chroot = RegistryConstants.PATH_SEPARATOR + chroot;
                }
                if (chroot.endsWith(RegistryConstants.PATH_SEPARATOR)) {
                    chroot = chroot.substring(0, chroot.length() - RegistryConstants.PATH_SEPARATOR.length());
                }
            }
            if (chroot == null) {
                symlinkLocation = null;
                log.debug("Unable to determine chroot. Symbolic Link cannot be created");
            }
            if (symlinkLocation != null) {
                symlinkLocation = chroot + symlinkLocation;
            }

            FileItemData fileItemData = fileItemsMap.get("upload").get(0);
//  //allow to upload a file with empty content
//            if ((fileItemData == null) || (fileItemData.getFileItem().getSize() == 0)) {
//                String msg = "Failed add resource. Resource content is empty.";
//                log.error(msg);
//
//                buildUIError(request, response, webContext, errorRedirect, msg);
//                return false;
//            }
            DataHandler dataHandler = fileItemData.getDataHandler();

            if (parentPath == null || resourceName == null) {
                String msg = "File upload failed. The parent path and resource name must be " +
                        "provided.";
                log.error(msg);

                buildUIError(request, response, webContext, errorRedirect, msg);
                return false;
            }

            client.addResource(
                    calculatePath(parentPath, resourceName), mediaType, description, dataHandler,
                    symlinkLocation, Utils.getProperties(properties));

            response.setContentType("text/html; charset=utf-8");
            String msg = "Successfully uploaded content.";
            if (redirect == null) {
                CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.INFO, request);
                if ("/".equals(parentPath)) {
                    parentPath += "&viewType=std";
                } else {
                    parentPath = URLEncoder.encode(parentPath, "UTF-8");
                }
                response.sendRedirect(getContextRoot(request) + "/" + webContext + "/resources/resource.jsp?region=region3&item=resource_browser_menu&path=" +
                        parentPath);
            } else {
                response.sendRedirect(getContextRoot(request) + "/" + webContext + "/" + redirect);
            }
            return true;

        } catch (IOException e) {
            // This happens if an error occurs while sending the UI Error Message.
            String msg = "File upload failed. " + e.getMessage();
            log.error("File upload failed. ", e);
            if (errorRedirect == null) {
                CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request);
                response.sendRedirect(
                        getContextRoot(request) + "/" + webContext + "/resources/resource.jsp?region=region3&item=resource_browser_menu&errorMsg=" + msg);
            } else {
                response.sendRedirect(
                        getContextRoot(request) + "/" + webContext + "/" + errorRedirect + (errorRedirect.indexOf("?")
                                == -1 ? "?" : "&")  + "msg=" + URLEncoder.encode(msg, "UTF-8"));
            }
            return false;
        } catch (RuntimeException e) {
            // we explicitly catch runtime exceptions too, since we want to make them available as
            // UI Errors in this scenario.
            String msg = "File upload failed. " + e.getMessage();
            log.error("File upload failed. ", e);

            buildUIError(request, response, webContext, errorRedirect, msg);
            return false;
        } catch (Exception e) {
            String msg = "File upload failed. " + e.getMessage();
            log.error("File upload failed. ", e);

            buildUIError(request, response, webContext, errorRedirect, msg);
            return false;
        }
    }

    protected void parseRequest(HttpServletRequest request)
            throws FileUploadFailedException, FileSizeLimitExceededException {
        try {
            super.parseRequest(request);
        } catch (FileSizeLimitExceededException e) {
            request.getSession().setAttribute("fileSizeExceededError", e.getMessage());
            log.error("You have exceeded the allowed maximum file upload size", e);
        }
    }

    private void buildUIError(HttpServletRequest request, HttpServletResponse response,
                              String webContext, String errorRedirect, String msg)
            throws IOException {
        CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request);
        if (errorRedirect == null) {
            response.sendRedirect(
                    getContextRoot(request) + "/" + webContext + "/resources/resource.jsp?region=region3&item=resource_browser_menu&errorMsg=" + msg);
        } else {
            response.sendRedirect(
                    getContextRoot(request) + "/" + webContext + "/" + errorRedirect + (errorRedirect.indexOf("?")
                            == -1 ? "?" : "&")  + "msg=" + URLEncoder.encode(msg, "UTF-8"));
        }
    }

    private static String calculatePath(String parentPath, String resourceName) {
        String resourcePath;
        if (!parentPath.startsWith(RegistryConstants.PATH_SEPARATOR)) {
            parentPath = RegistryConstants.PATH_SEPARATOR + parentPath;
        }
        if (parentPath.endsWith(RegistryConstants.PATH_SEPARATOR)) {
            resourcePath = parentPath + resourceName;
        } else {
            resourcePath = parentPath + RegistryConstants.PATH_SEPARATOR + resourceName;
        }
        return resourcePath;
    }
}
