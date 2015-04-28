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

package org.wso2.carbon.registry.resource.ui.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.registry.common.utils.CommonUtil;
import org.wso2.carbon.registry.common.utils.RegistryUtil;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.exceptions.ResourceNotFoundException;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.pagination.PaginationUtils;
import org.wso2.carbon.registry.core.utils.MediaTypesUtils;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceCallbackHandler;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;
import org.wso2.carbon.registry.resource.stub.beans.xsd.*;
import org.wso2.carbon.registry.resource.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.resource.stub.services.ArrayOfString;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import javax.activation.DataHandler;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class 
        ResourceServiceClient {

    private static final Log log = LogFactory.getLog(ResourceServiceClient.class);
    private HttpSession session;
    private ResourceAdminServiceStub stub;
    private String epr;
    private static final String ADDRESSING_MODULE = "addressing";

    private static class ResourceAdminServiceCallbackData {


        private boolean isComplete = false;
        private Exception exception = null;

        public void setComplete() {
            isComplete = true;
        }

        public void setException(Exception exception) {
            this.exception = exception;
        }

        public void handleCallback() throws Exception {
            int i = 0;
            try {
                while (!isComplete && exception == null) {
                    Thread.sleep(500);
                    i++;
                    if (i > 120 * 2400) {
                        throw new Exception("Response not received within 4 hours");
                    }
                }
                if (!isComplete) {
                    throw exception;
                }
            } finally {
                isComplete = false;
                exception = null;
            }
        }

    }

    private ResourceAdminServiceCallbackData callbackData = new ResourceAdminServiceCallbackData();

    private ResourceAdminServiceCallbackHandler callback =
            new ResourceAdminServiceCallbackHandler(callbackData) {

                private ResourceAdminServiceCallbackData getData() {
                    return (ResourceAdminServiceCallbackData) getClientData();
                }

                public void receiveResultaddTextResource(boolean result) {
                    getData().setComplete();
                }

                public void receiveErroraddTextResource(Exception e) {
                    getData().setException(e);
                }

                public void receiveErrorimportResource(Exception e) {
                    getData().setException(e);
                }

                public void receiveResultimportResource(boolean result) {
                    getData().setComplete();
                }

                public void receiveResultaddResource(boolean result) {
                    getData().setComplete();
                }

                public void receiveErroraddResource(Exception e) {
                    getData().setException(e);
                }
            };

    public ResourceServiceClient(
            String cookie, String backendServerURL, ConfigurationContext configContext)
            throws RegistryException {

        epr = backendServerURL + "ResourceAdminService";

        try {
            stub = new ResourceAdminServiceStub(configContext, epr);

            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        } catch (AxisFault axisFault) {
            String msg = "Failed to initiate resource service client. " + axisFault.getMessage();
            log.error(msg, axisFault);
            throw new RegistryException(msg, axisFault);
        }
    }

    public ResourceServiceClient(String cookie, ServletConfig config, HttpSession session)
            throws RegistryException {
        this.session = session;
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.
                getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        epr = backendServerURL + "ResourceAdminService";

        try {
            stub = new ResourceAdminServiceStub(configContext, epr);

            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        } catch (AxisFault axisFault) {
            String msg = "Failed to initiate resource service client. " + axisFault.getMessage();
            log.error(msg, axisFault);
            throw new RegistryException(msg, axisFault);
        }
    }

    public ResourceServiceClient(ServletConfig config, HttpSession session)
            throws RegistryException {
        this.session =session;
        String cookie = (String)session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.
                getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        epr = backendServerURL + "ResourceAdminService";

        try {
            stub = new ResourceAdminServiceStub(configContext, epr);

            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        } catch (AxisFault axisFault) {
            String msg = "Failed to initiate resource service client. " + axisFault.getMessage();
            log.error(msg, axisFault);
            throw new RegistryException(msg, axisFault);
        }
    }

    public MetadataBean getMetadata(HttpServletRequest request) throws Exception {

        String path = RegistryUtil.getPath(request);
        if (path == null) {
            path = getSessionResourcePath();
            if (path == null) {
                path = RegistryConstants.ROOT_PATH;
            }

            request.setAttribute("path", path);
        }

        MetadataBean bean = null;
        try {
            bean = stub.getMetadata(path);
        } catch (Exception e) {
            String msg = "Failed to get resource metadata from the resource service. " +
                    e.getMessage();
            log.error(msg, e);
            throw e;
        }

        return bean;
    }

    public MetadataBean getMetadata(String path) throws Exception {

        if (path == null) {
            path = RegistryConstants.ROOT_PATH;
        }

        MetadataBean bean = null;
        try {
            bean = stub.getMetadata(path);
        } catch (Exception e) {
            String msg = "Failed to get resource metadata from the resource service. " +
                    e.getMessage();
            log.error(msg, e);
            throw e;
        }

        return bean;
    }

    public MetadataBean getMetadata(HttpServletRequest request,String root) throws Exception {

        String path = RegistryConstants.ROOT_PATH;
        request.setAttribute("path", path);
        if (path == null) {
            path = getSessionResourcePath();
            if (path == null) {
                path = RegistryConstants.ROOT_PATH;
            }
        }

        MetadataBean bean = null;
        try {
            bean = stub.getMetadata(path);
        } catch (Exception e) {
            String msg = "Failed to get resource metadata from the resource service. " +
                    e.getMessage();
            log.error(msg, e);
            throw e;
        }

        return bean;
    }
    public void setDescription(String path, String description) throws Exception {

        try {
            stub.setDescription(path, description);
        } catch (Exception e) {
            String msg = "Failed to set description of the resource " + path +
                    ". Description: " + description + ". Caused by: " + e.getMessage();
            log.error(msg, e);
            throw e;
        }
    }

    public CollectionContentBean getCollectionContent(String path) throws Exception {

        CollectionContentBean bean = null;
        try {
            bean = stub.getCollectionContent(path);
        } catch (Exception e) {
            String msg = "Failed to get collection content from the resource service. " +
                    e.getMessage();
            log.error(msg, e);
            throw e;
        }

        return bean;
    }

    public CollectionContentBean getCollectionContent(HttpServletRequest request) throws Exception {

        String path = RegistryUtil.getPath(request);
        CollectionContentBean bean = null;
        try {
            if(PaginationContext.getInstance() ==null){
                bean = stub.getCollectionContent(path);
            }else {
                PaginationUtils.copyPaginationContext(stub._getServiceClient());
                bean = stub.getCollectionContent(path);
                int rowCount = PaginationUtils.getRowCount(stub._getServiceClient());
                session.setAttribute("row_count", Integer.toString(rowCount));
            }
        } catch (Exception e) {
            String msg = "Failed to get collection content from the resource service. " +
                    e.getMessage();
            log.error(msg, e);
            throw e;
        } finally {
            PaginationContext.destroy();
        }
        return bean;
    }

     public ContentBean getContent(HttpServletRequest request) throws Exception {

        String path = RegistryUtil.getPath(request);
        ContentBean bean = null;
        try {
            bean = stub.getContentBean(path);
        } catch (Exception e) {
            String msg = "Failed to get content from the resource service. " +
                    e.getMessage();
            log.error(msg, e);
            throw e;
        }

        return bean;
    }

    public ResourceData[] getResourceData(String[] paths) throws Exception {

        ResourceData[] resourceData;
        try {
            resourceData = stub.getResourceData(paths);
        } catch (Exception e) {
            String msg = "Failed to get resource data from the resource service. " +
                    e.getMessage();
            log.error(msg, e);
            throw e;
        }

        return resourceData;
    }

    public String addCollection(
            String parentPath, String collectionName, String mediaType, String description)
            throws Exception {

        try {
            parentPath = stub.addCollection(parentPath, collectionName,
                    MediaTypesUtils.getMimeTypeFromHumanReadableMediaType(mediaType), description);
        } catch (Exception e) {
            String msg = "Failed to add collection from the resource service. " +
                    e.getMessage();
            log.error(msg, e);
            throw e;
        }

        return parentPath;
    }

    public void addTextResource(
            String parentPath,
            String fileName,
            String mediaType,
            String description,
            String content) throws Exception {

        try {
            /*stub._getServiceClient().engageModule(ADDRESSING_MODULE); // IMPORTANT
            Options options = stub._getServiceClient().getOptions();
            options.setUseSeparateListener(true);
            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
            stub.startaddTextResource(parentPath, fileName, mediaType, description, content,
                    callback);
            callbackData.handleCallback();*/
            stub.addTextResource(parentPath, fileName, MediaTypesUtils.getMimeTypeFromHumanReadableMediaType(mediaType)
                    , description, content);
        } catch (Exception e) {
            String msg = "Failed to add new text resource with name " + fileName +
                    " to the parent collection " + parentPath + ". " + e.getMessage();
            log.error(msg, e);
            throw e;
        }
    }

    public void addSymbolicLink(String parentPath,
                                String name,
                                String targetPath) throws Exception{
        try {
            stub.addSymbolicLink(parentPath, name, targetPath);
        } catch (Exception e) {
            String msg = "Failed to add symbolic link with name " + name +
                    " to the parent collection " + parentPath + ". " + e.getMessage();
            log.error(msg, e);
            throw e;
        }
    }

    public void addRemoteLink(String parentPath,
                                String name,
                                String instance,
                                String targetPath) throws Exception{
        try {
            stub.addRemoteLink(parentPath, name, instance, targetPath);
        } catch (Exception e) {
            String msg = "Failed to add remote link with name " + name +
                    " to the parent collection " + parentPath + ". " + e.getMessage();
            log.error(msg, e);
            throw e;
        }
    }

    public void importResource(
            String parentPath,
            String resourceName,
            String mediaType,
            String description,
            String fetchURL,
            String symlinkLocation,
            String[][] properties,
            boolean isAsync) throws Exception {

        try {
            // This is used by the add wsdl UI. WSDL validation takes long when there are wsdl
            // imports to prevent this we make a async call.
            if (isAsync) {
                stub._getServiceClient().getOptions().setProperty(
                        MessageContext.CLIENT_API_NON_BLOCKING,Boolean.TRUE);
                stub.importResource(parentPath, resourceName,
                        MediaTypesUtils.getMimeTypeFromHumanReadableMediaType(mediaType), description, fetchURL,
                        symlinkLocation, buildPropertiesArray(properties));
            } else {
                /*stub._getServiceClient().engageModule(ADDRESSING_MODULE); // IMPORTANT
                Options options = stub._getServiceClient().getOptions();
                options.setUseSeparateListener(true);
                options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
                stub.startimportResource(parentPath, resourceName, mediaType, description, fetchURL,
                        symlinkLocation, callback);
                callbackData.handleCallback();*/
                stub.importResource(parentPath, resourceName,
                        MediaTypesUtils.getMimeTypeFromHumanReadableMediaType(mediaType), description, fetchURL,
                        symlinkLocation, buildPropertiesArray(properties));
            }

        } catch (Exception e) {
            String msg = "Failed to import resource with name " + resourceName +
                    " to the parent collection " + parentPath + ". " + e.getMessage();
            log.error(msg, e);
            throw e;
        }
    }

    private ArrayOfString[] buildPropertiesArray(String[][] properties) {
        if (properties == null) {
            return new ArrayOfString[0];
        }
        ArrayOfString[] props = new ArrayOfString[properties.length+1];
        for (int i = 0; i < props.length-1; i++) {
            ArrayOfString arrayOfString = new ArrayOfString();
            arrayOfString.setArray(new String[]{properties[i][0], properties[i][1]});
            props[i] = arrayOfString;
        }
        ArrayOfString arrayOfString2 = new ArrayOfString();
        arrayOfString2.setArray(new String[]{ CommonConstants.SOURCE_PROPERTY, CommonConstants.SOURCE_ADMIN_CONSOLE});
        props[properties.length]  = arrayOfString2;
        return props;
    }

    public void delete(String pathToDelete) throws Exception {

        try {
            stub.delete(pathToDelete);
        } catch (Exception e) {
            String msg = "Failed to delete " + pathToDelete + ". " + e.getMessage();
            log.error(msg, e);
            throw e;
        }
    }
    
    public void removeVersionHistory(String path, String snapshotId) throws Exception {

        try {        	
            stub.deleteVersionHistory(path, snapshotId);
        } catch (Exception e) {
            String msg = "Failed to delete the snapshot with the ID: " + snapshotId + ". " + e.getMessage();
            log.error(msg, e);
            throw e;
        }
    }

    public void renameResource(
            String parentPath, String oldResourcePath, String newResourceName)
            throws Exception {

        try {
            stub.renameResource(parentPath, oldResourcePath, newResourceName);
        } catch (Exception e) {
            String msg = "Failed to rename resource with name " + oldResourcePath +
                    " to the new name " + newResourceName + ". " + e.getMessage();
            log.error(msg, e);
            throw e;
        }
    }

    public void copyResource(
            String parentPath, String oldResourcePath, String destinationPath, String resourceName)
            throws Exception {

        try {
            stub.copyResource(parentPath, oldResourcePath, destinationPath, resourceName);
        } catch (Exception e) {
            String msg = "Failed to copy resource " + oldResourcePath +
                    " to the path " + destinationPath + ". " + e.getMessage();
            log.error(msg, e);
            throw e;
        }
    }

    public void moveResource(
            String parentPath, String oldResourcePath, String destinationPath, String resourceName)
            throws Exception {

        try {
            stub.moveResource(parentPath, oldResourcePath, destinationPath, resourceName);
        } catch (Exception e) {
            String msg = "Failed to move resource " + oldResourcePath +
                    " to the path " + destinationPath + ". " + e.getMessage();
            log.error(msg, e);
            throw e;
        }
    }

    public PermissionBean getPermissions(HttpServletRequest request) throws Exception {

        String path = RegistryUtil.getPath(request);

        PermissionBean bean;
        try {
            bean = stub.getPermissions(path);
        } catch (Exception e) {
            String msg = "Failed to get permissions of the resource " +
                    path + ". " + e.getMessage();
            log.error(msg, e);
            throw e;
        }

        return bean;
    }

    public void addUserPermission(
            String pathToAuthorize,
            String userToAuthorize,
            String actionToAuthorize,
            String permissionType) throws Exception {

        try {
            stub.addUserPermission(
                    pathToAuthorize, userToAuthorize, actionToAuthorize, permissionType);
        } catch (Exception e) {
            String msg = "Failed add " + actionToAuthorize + " permission for user " +
                    userToAuthorize + " on resource " + pathToAuthorize + ". " + e.getMessage();
            log.error(msg, e);
            throw e;
        }
    }

     public void addRolePermission(
            String pathToAuthorize,
            String roleToAuthorize,
            String actionToAuthorize,
            String permissionType) throws Exception {

        try {
            stub.addRolePermission(
                    pathToAuthorize, roleToAuthorize, actionToAuthorize, permissionType);
        } catch (Exception e) {
            String msg = "Failed add " + actionToAuthorize + " permission for role " +
                    roleToAuthorize + " on resource " + pathToAuthorize + ". " + e.getMessage();
            log.error(msg, e);
            throw e;
        }
    }

    public void changeUserPermissions(String resourcePath, String permissionsInput)
            throws Exception {

        try {
            stub.changeUserPermissions(resourcePath, permissionsInput);
        } catch (Exception e) {

            String msg = "Failed to change user permissions of the resource " +
                    resourcePath + ". " + e.getMessage();
            log.error(msg, e);
            throw e;
        }
    }

    public void changeRolePermissions(String resourcePath, String permissionsInput)
            throws Exception {

        try {
            stub.changeRolePermissions(resourcePath, permissionsInput);
        } catch (Exception e) {

            String msg = "Failed to change role permissions of the resource " +
                    resourcePath + ". " + e.getMessage();
            log.error(msg, e);
            throw e;
        }
    }

    public String getTextContent(HttpServletRequest request) throws Exception {

        String path = RegistryUtil.getPath(request);

        String textContent = null;
        try {
            textContent = stub.getTextContent(path);
            MetadataBean metadataBean = stub.getMetadata(path);
            String resourceVersion = metadataBean.getResourceVersion();
            request.getSession().setAttribute("resourceVersion",resourceVersion);

        } catch (Exception e) {

            String msg = "Failed get text content of the resource " +
                    path + ". " + e.getMessage();
            log.error(msg, e);
            throw e;
        }
        return textContent;
    }

    public String getExternalURL(HttpServletRequest request) throws Exception {

        String path = (String)request.getAttribute("path");

        String url = null;
        try {
            url = stub.getTextContent(path);
        } catch (Exception e) {

            String msg = "Failed get content of the resource " +
                    path + ". " + e.getMessage();
            log.error(msg, e);
            throw e;
        }
        return url;
    }

     public void updateTextContent(String resourcePath, String contentText,String updateOverride,String resourceVersion) throws Exception {

        try {
            MetadataBean metadataBean = stub.getMetadata(resourcePath);

            if(CommonUtil.isLatestVersion(resourceVersion, metadataBean.getResourceVersion()) || updateOverride.equals("true")){
                stub.updateTextContent(resourcePath, contentText);
            }else{
                throw new RegistryException("Another user has already modified this resource");
            }

        } catch (Exception e) {

            String msg = "Failed to update text content of the resource " +
                    resourcePath + ". " + e.getMessage();
            log.error(msg, e);
            throw e;
        }
    }

    public void addResource(String path, String mediaType, String description, DataHandler content,
                            String symlinkLocation, String[][] properties)
            throws Exception {

        try {
            /*stub._getServiceClient().engageModule(ADDRESSING_MODULE); // IMPORTANT
            Options options = stub._getServiceClient().getOptions();
            //options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
            options.setUseSeparateListener(true);
            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
            stub.startaddResource(path, mediaType, description, content, symlinkLocation, callback);
            callbackData.handleCallback();*/
            stub.addResource(path, MediaTypesUtils.getMimeTypeFromHumanReadableMediaType(mediaType)
                    , description, content, symlinkLocation, buildPropertiesArray(properties));

        } catch (Exception e) {

            String msg = "Failed to add resource " + path + ". " + e.getMessage();
            log.error(msg, e);
            throw e;
        }
    }

    public ResourceTreeEntryBean getResourceTreeEntry(String resourcePath)
            throws Exception {

        ResourceTreeEntryBean entryBean = null;
        try {
            Options options = stub._getServiceClient().getOptions();
            options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
            entryBean = stub.getResourceTreeEntry(resourcePath);
        } catch (Exception e) {
            String msg = "Failed to get resource tree entry for resource " +
                    resourcePath + ". " + e.getMessage();
            log.error(msg, e);
            throw e;
        }
        if (entryBean == null) {
            throw new ResourceNotFoundException("The resource does not exist");
        }
        return entryBean;
    }

    public String getSessionResourcePath() throws Exception {

        String sessionResourcePath;
        try {
            sessionResourcePath = stub.getSessionResourcePath();
        } catch (Exception e) {

            String msg = "Failed to get the session resource path. " + e.getMessage();
            log.error(msg, e);
            throw e;
        }
        return sessionResourcePath;
    }

    public void setTextContent(String sessionResourcePath) throws Exception {

        try {
            stub.setSessionResourcePath(sessionResourcePath);
        } catch (Exception e) {

            String msg = "Failed to set session resource path to " +
                    sessionResourcePath + ". " + e.getMessage();
            log.error(msg, e);
            throw e;
        }
    }

    public void createVersion(String resourcePath) throws Exception {

        try {
            stub.createVersion(resourcePath);
        } catch (Exception e) {

            String msg = "Failed to create version of the resource " +
                    resourcePath + ". " + e.getMessage();
            log.error(msg, e);
            throw e;
        }
    }

    public void restoreVersion(String versionPath) throws Exception {

        try {
            stub.restoreVersion(versionPath);
        } catch (Exception e) {

            String msg = "Failed to version version of the resource " +
                    versionPath + ". " + e.getMessage();
            log.error(msg, e);
            throw e;
        }
    }

    public VersionsBean getVersionsBean(String path) throws Exception {

        VersionsBean versionsBean;
        try {
            versionsBean = stub.getVersionsBean(path);
        } catch (Exception e) {

            String msg = "Failed to get versions of the resource " +
                    path + ". " + e.getMessage();
            log.error(msg, e);
            throw e;
        }

        if (versionsBean.getVersionPaths() == null) {
            versionsBean.setVersionPaths(new VersionPath[0]);
        }

        return versionsBean;
    }

    public String getMediatypeDefinitions() throws Exception {

        try {
            String mime = stub.getMediatypeDefinitions();
            return mime;
            
        } catch (Exception e) {
            String msg = "Failed to get media type definitions from the back end server. Limited set of media types will be populated. " + e.getMessage();
            log.error(msg, e);
            return "txt:text/plain,jpg:iage/jpeg,gif:image/gif";
        }
    }

    public String getCollectionMediatypeDefinitions() throws Exception {

        try {
            String mime = stub.getCollectionMediatypeDefinitions();
            return mime;

        } catch (Exception e) {
            String msg = "Failed to get media type definitions from the back end server. Limited set of media types will be populated. " + e.getMessage();
            log.error(msg, e);
            return "";
        }
    }

    public String getCustomUIMediatypeDefinitions() throws Exception {

        try {
            String mime = stub.getCustomUIMediatypeDefinitions();
            return mime;

        } catch (Exception e) {
            String msg = "Failed to get custom UI media type definitions from the back end server. Limited set of media types will be populated. " + e.getMessage();
            log.error(msg, e);
            return "mex:application/vnd.wso2-mex+xml";
        }
    }


    public String getProperty(String path, String key) throws Exception {
        
        try {
            return stub.getProperty(path, key);
        } catch (Exception e) {

            String msg = "Failed to get property with key :" + key + " form the resource in path " +
                    path + ". Error :" + e.getMessage();
            log.error(msg, e);
            throw e;
        }
    }
    
    public ContentDownloadBean getContentDownloadBean(String path) throws Exception {

        ContentDownloadBean bean = stub.getContentDownloadBean(path);
        return bean;
    }

    public String getHumanReadableMediaType() throws Exception{
        return stub.getHumanReadableMediaTypes();
    }
    
    public void  updateMediaType(String resourcePath, String mediaType) throws Exception{
        try {
            stub.updateMediaType(resourcePath,mediaType);
        } catch (Exception e) {
            String msg = "Failed update media type of  resource " +
                    resourcePath + ". " + e.getMessage();
            log.error(msg, e);
            throw e;
        }
    }

    public ContentDownloadBean getZipWithDependencies(String path) throws Exception {
        try {
         return stub.getZipWithDependencies(path);
        } catch (Exception e) {
            String msg = "Failed to stream the zip with dependencies " +
                    path + ". " + e.getMessage();
            log.error(msg, e);
            throw e;
        }
    }

    public boolean hasAssociations(String path,String type) throws Exception {
         return stub.hasAssociations(path,type);
    }
}
