/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.extensions.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.registry.extensions.ui.clients.ResourceServiceClient;
import org.wso2.carbon.ui.CarbonUIMessage;
import org.wso2.carbon.ui.transports.fileupload.AbstractFileUploadExecutor;
import org.wso2.carbon.utils.FileItemData;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;

public class AddExtensionProcessor extends AbstractFileUploadExecutor {

    private static final Log log = LogFactory.getLog(AddExtensionProcessor.class);

    public boolean execute(HttpServletRequest request, HttpServletResponse response)
            throws CarbonException, IOException {

        String webContext = (String) request.getAttribute(CarbonConstants.WEB_CONTEXT);
        String serverURL = (String) request.getAttribute(CarbonConstants.SERVER_URL);
        String cookie = (String) request.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        Map<String,ArrayList<FileItemData>> fileItemsMap =  getFileItemsMap();
        Map<String,ArrayList<String>> formFieldsMap =  getFormFieldsMap();

        if (fileItemsMap == null || fileItemsMap.isEmpty()) {
            String msg = "File uploading failed. Content is not set properly.";
            log.error(msg);

            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request);
            response.sendRedirect(
                    getContextRoot(request) + "/" + webContext + "/admin/error.jsp");

            return false;
        }
        String errorRedirect = null;
        try {
            ResourceServiceClient client =
                    new ResourceServiceClient(cookie, serverURL, configurationContext);

            String redirect = null;

            if (formFieldsMap.get("redirect") != null) {
                redirect = formFieldsMap.get("redirect").get(0);
            }

            if (formFieldsMap.get("errorRedirect") != null) {
                errorRedirect = formFieldsMap.get("errorRedirect").get(0);
            }

            FileItemData fileItemData = fileItemsMap.get("upload").get(0);

            if ((fileItemData == null) || (fileItemData.getFileItem().getSize() == 0)) {
                String msg = "Failed add resource. Resource content is empty.";
                log.error(msg);

                buildUIError(request, response, webContext, errorRedirect, msg);
                return false;
            }
            client.addExtension(formFieldsMap.get("filename").get(0),
                    fileItemData.getDataHandler());

            response.setContentType("text/html; charset=utf-8");
            String msg = "Successfully uploaded extension.";
            if (redirect == null) {
                CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.INFO, request);
                response.sendRedirect(getContextRoot(request) + "/" + webContext + "/admin/index.jsp");
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
                        getContextRoot(request) + "/" + webContext + "/admin/error.jsp");
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

    private void buildUIError(HttpServletRequest request, HttpServletResponse response,
                              String webContext, String errorRedirect, String msg)
            throws IOException {
        CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request);
        if (errorRedirect == null) {
            response.sendRedirect(
                    getContextRoot(request) + "/" + webContext + "/admin/error.jsp");
        } else {
            response.sendRedirect(
                    getContextRoot(request) + "/" + webContext + "/" + errorRedirect + (errorRedirect.indexOf("?")
                            == -1 ? "?" : "&")  + "msg=" + URLEncoder.encode(msg, "UTF-8"));
        }
    }
}
