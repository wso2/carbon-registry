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

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.MediaTypesUtils;
import org.wso2.carbon.registry.resource.ui.Utils;
import org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.server.admin.common.IServerAdmin;
import org.wso2.carbon.server.admin.common.ServerData;
import org.wso2.carbon.server.admin.ui.ServerAdminClient;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.CarbonConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletConfig;

public class ImportResourceProcessor {

    private static final Log LOG = LogFactory.getLog(ImportResourceProcessor.class);

    /**
     * process the form data and send it to ResourceServiceClient
     *
     * @param request HTML request
     * @param config  server configuration
     * @throws RegistryException
     */
    public static void process(
            HttpServletRequest request, ServletConfig config)
            throws RegistryException {

        String parentPath = request.getParameter("parentPath");
        String resourceName = request.getParameter("resourceName");
        String mediaType = MediaTypesUtils.getMimeTypeFromHumanReadableMediaType(
                request.getParameter("mediaType"));
        String description = request.getParameter("description");
        String fetchURL = request.getParameter("fetchURL");
        String isAsync = request.getParameter("isAsync");
        String symlinkLocation = request.getParameter("symlinkLocation");

        String cookie = (String) request.
                getSession().getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        HttpSession session = request.getSession();
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);

        try {
            ConfigurationContext configContext = (ConfigurationContext) config.
                    getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            IServerAdmin adminClient = new ServerAdminClient(configContext, serverURL, cookie, session);
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
                LOG.debug("Unable to determine chroot. Symbolic Link cannot be created");
            }
            if (symlinkLocation != null) {
                symlinkLocation = chroot + symlinkLocation;
            }
            // fetching custom properties from the request. eg: version
            String[][] newProperties = Utils.getProperties(request);
            // Adding Source URL as property to end of the properties array.
            String[][] properties = Utils.setProperties(newProperties,"sourceURL",fetchURL);

            ResourceServiceClient client =
                    new ResourceServiceClient(cookie, config, request.getSession());
            if (JavaUtils.isTrueExplicitly(isAsync)) {
                client.importResource(parentPath, resourceName, mediaType,
                        description, fetchURL, symlinkLocation, properties, true);
            } else {
                client.importResource(parentPath, resourceName, mediaType,
                        description, fetchURL, symlinkLocation, properties, false);
            }
        } catch (AxisFault axisFault) {
            String msg = "Failed to initiate Server Admin Client.";
            LOG.error(msg, axisFault);
            throw new RegistryException(msg, axisFault);
        } catch (Exception e) {
            // Since ResourceServiceClient.importResource has thrown a Exception need to handle that as well,
            // Therefore adding the original exception below.
            // having additional details will make the error message long
            String msg = "Unable to process (ResourceServiceClient.importResource) function." +
                    " Please check the network connection.";
            LOG.error(msg, e);
            // if we skip msg and put just e, error message contain the axis2 fault story
            throw new RegistryException(msg, e);
        }
    }
}
