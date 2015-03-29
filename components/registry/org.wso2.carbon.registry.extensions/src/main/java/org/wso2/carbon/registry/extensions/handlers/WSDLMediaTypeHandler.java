/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.extensions.handlers;

import org.apache.axiom.om.OMElement;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.uddi.api_v3.AuthToken;
import org.wso2.carbon.registry.common.utils.artifact.manager.ArtifactManager;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.beans.BusinessServiceInfo;
import org.wso2.carbon.registry.extensions.handlers.utils.*;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;
import org.wso2.carbon.registry.extensions.utils.WSDLValidationInfo;
import org.wso2.carbon.registry.uddi.utils.UDDIUtil;

import javax.xml.namespace.QName;
import java.io.*;
import java.util.*;

@SuppressWarnings("unused")
public class WSDLMediaTypeHandler extends Handler {
    private static final Log log = LogFactory.getLog(WSDLMediaTypeHandler.class);
    protected String locationTag = "location";
    
    private String wsdlLocation = "/wsdls/";        // location will always has a leading '/' and trailing '/'
    private OMElement wsdlLocationConfiguration;
    protected String schemaLocation = "/schema/";     // location will always has a leading '/' and trailing '/'
    private OMElement schemaLocationConfiguration;

    protected String policyLocation = "/policy/";     // location will always has a leading '/' and trailing '/'
    private OMElement policyLocationConfiguration;
    private boolean disableSymlinkCreation = true;

    private String defaultWsdlVersion = CommonConstants.WSDL_VERSION_DEFAULT_VALUE;

    public boolean getCreateService() {
        return createService;
    }

    public void setCreateService(String createService) {
        this.createService = Boolean.valueOf(createService);
    }

    private boolean createService = true;

    private boolean createSOAPService = true;

    public boolean isCreateSOAPService() {
        return createSOAPService;
    }

    public void setCreateSOAPService(String createSOAPService) {
        this.createSOAPService = Boolean.valueOf(createSOAPService);
    }

    private boolean disableWSDLValidation = false;

    public OMElement getWsdlLocationConfiguration() {
        return wsdlLocationConfiguration;
    }

    public boolean isDisableSymlinkCreation() {
        return disableSymlinkCreation;
    }

    public void setDisableSymlinkCreation(String disableSymlinkCreation) {
        this.disableSymlinkCreation = Boolean.toString(true).equals(disableSymlinkCreation);
    }

    public void setDefaultServiceVersion(String defaultWsdlVersion) {
        this.defaultWsdlVersion = defaultWsdlVersion;
    }

    public void setWsdlLocationConfiguration(OMElement locationConfiguration) throws RegistryException {
        Iterator confElements = locationConfiguration.getChildElements();
        while (confElements.hasNext()) {
            OMElement confElement = (OMElement)confElements.next();
            if (confElement.getQName().equals(new QName(locationTag))) {
                wsdlLocation = confElement.getText();
                if (!wsdlLocation.startsWith(RegistryConstants.PATH_SEPARATOR)) {
                    wsdlLocation = RegistryConstants.PATH_SEPARATOR + wsdlLocation;
                }
                if (!wsdlLocation.endsWith(RegistryConstants.PATH_SEPARATOR)) {
                    wsdlLocation = wsdlLocation + RegistryConstants.PATH_SEPARATOR;
                }
            }
        }
        WSDLProcessor.setCommonWSDLLocation(wsdlLocation);
        this.wsdlLocationConfiguration = locationConfiguration;
    }

    public OMElement getSchemaLocationConfiguration() {
        return schemaLocationConfiguration;
    }
    
    public void setSchemaLocationConfiguration(OMElement locationConfiguration) throws RegistryException {
        Iterator confElements = locationConfiguration.getChildElements();
        while (confElements.hasNext()) {
            OMElement confElement = (OMElement)confElements.next();
            if (confElement.getQName().equals(new QName(locationTag))) {
                schemaLocation = confElement.getText();
                if (!schemaLocation.startsWith(RegistryConstants.PATH_SEPARATOR)) {
                    schemaLocation = RegistryConstants.PATH_SEPARATOR + schemaLocation;
                }
                if (!schemaLocation.endsWith(RegistryConstants.PATH_SEPARATOR)) {
                    schemaLocation = schemaLocation + RegistryConstants.PATH_SEPARATOR;
                }
            }
        }
        
        WSDLProcessor.setCommonSchemaLocation(schemaLocation);
        this.schemaLocationConfiguration = locationConfiguration;
    }

     public OMElement getPolicyLocationConfiguration() {
        return policyLocationConfiguration;
    }
    public void setPolicyLocationConfiguration(OMElement locationConfiguration) throws RegistryException {
           Iterator confElements = locationConfiguration.getChildElements();
           while (confElements.hasNext()) {
               OMElement confElement = (OMElement)confElements.next();
               if (confElement.getQName().equals(new QName(locationTag))) {
                   policyLocation = confElement.getText();
                   if (!policyLocation.startsWith(RegistryConstants.PATH_SEPARATOR)) {
                       policyLocation = RegistryConstants.PATH_SEPARATOR + policyLocation;
                   }
                   if (!policyLocation.endsWith(RegistryConstants.PATH_SEPARATOR)) {
                       policyLocation = policyLocation + RegistryConstants.PATH_SEPARATOR;
                   }
               }
           }

           WSDLProcessor.setCommonPolicyLocation(policyLocation);
           this.policyLocationConfiguration = locationConfiguration;
       }



    public void makeDir(File file) throws IOException {
        if (file != null && !file.exists() && !file.mkdir()) {
            log.warn("Failed to create directory at path: " + file.getAbsolutePath());
        }
    }

    public void makeDirs(File file) throws IOException {
        if (file != null && !file.exists() && !file.mkdirs()) {
            log.warn("Failed to create directories at path: " + file.getAbsolutePath());
        }
    }

    public void delete(File file) throws IOException {
        if (file != null && file.exists() && !file.delete()) {
            log.warn("Failed to delete file/directory at path: " + file.getAbsolutePath());
        }
    }

    /**
     * Method that will executed after the put operation has been done.
     *
     * @param path the path of the resource.
     * @param addedResources the resources that have been added to the registry.
     * @param otherResources the resources that have not been added to the registry.
     * @param requestContext the request context for the put operation.
     * @throws RegistryException if the operation failed.
     */
    @SuppressWarnings("unused")
    protected void onPutCompleted(String path, Map<String, String> addedResources,
                                  List<String> otherResources, RequestContext requestContext)
            throws RegistryException {
    }

    public void put(RequestContext requestContext) throws RegistryException {
        if (!CommonUtil.isUpdateLockAvailable()) {
            return;
        }
        CommonUtil.acquireUpdateLock();
                WSDLProcessor wsdlProcessor=null;
        try {
            Resource metadata = requestContext.getResource();
            String path = requestContext.getResourcePath().getPath();
            try {
                // If the WSDL is already there, we don't need to re-run this handler unless the content is changed.
                // Re-running this handler causes issues with downstream handlers and other behaviour (ex:- lifecycles).
                // If you need to do a replace pragmatically, delete-then-replace.
                if (metadata == null) {
                    // will go with the default processing
                    return;
                }
                Registry registry = requestContext.getRegistry();

                // This is to distinguish operations on xsd and wsdl on remote mounting.
                String remotePut = metadata.getProperty(RegistryConstants.REMOTE_MOUNT_OPERATION);
                if (remotePut != null) {
                    CommonUtil.releaseUpdateLock();
                    metadata.removeProperty(RegistryConstants.REMOTE_MOUNT_OPERATION);
                    registry.put(path, metadata);
                    requestContext.setProcessingComplete(true);
                    ArtifactManager.getArtifactManager().getTenantArtifactRepository().
                                    addArtifact(path);
                    return;
                }

                if (registry.resourceExists(path)) {
                    // logic to compare content, and return only if the content didn't change.
                    Object newContent = metadata.getContent();

                    Resource oldResource = registry.get(path);
                    //if the oldResource is a SymLink, then we need to obtain the actual resource path, rather than the
                    //path of the symlink,
                    if("true".equals(oldResource.getProperty("registry.link"))) {
                        path = oldResource.getProperty("registry.actualpath");
                    }
                    Object oldContent = oldResource.getContent();
                    String newContentString = null;
                    String oldContentString = null;
                    if (newContent != null) {
                        if (newContent instanceof String) {
                            newContentString = (String) newContent;
                        } else {
                            newContentString = RegistryUtils.decodeBytes((byte[]) newContent);
                        }
                    }
                    if (oldContent != null) {
                        if (oldContent instanceof String) {
                            oldContentString = (String) oldContent;
                        } else {
                            oldContentString = RegistryUtils.decodeBytes((byte[]) oldContent);
                        }
                    }
                    if ((newContent == null && oldContent == null) ||
                            (newContentString != null && newContentString.equals(oldContentString))) {
                        // this will continue adding from the default path.
                        return;
                    }

                    // so we creating temp files for the wsdl and all the dependencies.
                    Set<String> registryPaths = new LinkedHashSet<String>();
                    // the first path is the current resource path.
                    registryPaths.add(path);
                    // get the associations.
                    Association[] dependencies = CommonUtil.getDependenciesRecursively(registry, path);
                    if (dependencies != null) {
                        for (Association dependency: dependencies) {
                            String targetPath = dependency.getDestinationPath();
                            if (targetPath.startsWith(RegistryConstants.ROOT_PATH)) {
                                registryPaths.add(targetPath);
                            }
                        }
                    }

                    File referenceTempFile = File.createTempFile("wsdl", ".ref");
                    File tempDir = new File(referenceTempFile.getAbsolutePath().substring(0,
                            referenceTempFile.getAbsolutePath().length() - ".ref".length()));
                    String tempDirPath = tempDir.getAbsolutePath();
                    // now add each of the registry paths to the the tempDir
                    List<File> tempFiles = new ArrayList<File>();
                    for (String registryPath: registryPaths) {
                        if (!registryPath.startsWith(RegistryConstants.ROOT_PATH)) {
                            continue;
                        }
                        String filePath = tempDirPath + registryPath;
                        File tempFile = new File(filePath);
                        makeDirs(tempFile.getParentFile());

                        Object resourceContent;
                        if (registryPath.equals(path)) {
                            // this is the wsdl we want to update.
                            resourceContent = metadata.getContent();
                        } else {
                            if (!registry.resourceExists(registryPath)) {
                                continue;
                            }
                            Resource r = registry.get(registryPath);
                            if (r == null) {
                                continue;
                            }
                            resourceContent = r.getContent();
                        }
                        byte[] resourceContentBytes;

                        if (resourceContent == null) {
                            resourceContentBytes = new byte[0];
                        } else if (resourceContent instanceof byte[]) {
                            resourceContentBytes = (byte[])resourceContent;
                        } else if (resourceContent instanceof String) {
                            resourceContentBytes = RegistryUtils.encodeString(((String)resourceContent));
                        } else {
                            String msg = "Unknown type for the content path: " + path + ", content type: " +
                                    resourceContent.getClass().getName() + ".";
                            log.error(msg);
                            throw new RegistryException(msg);
                        }
                        InputStream in = new ByteArrayInputStream(resourceContentBytes);

                        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tempFile));
                        byte[] contentChunk = new byte[1024];
                        int byteCount;
                        while ((byteCount = in.read(contentChunk)) != -1) {
                            out.write(contentChunk, 0, byteCount);
                        }
                        out.flush();
                        out.close();
                        tempFiles.add(tempFile);
                    }

                    if (tempFiles.size() == 0) {
                        // unreachable state, anyway better log and return.
                        String msg = "Temporary files count is zero, when updating a wsdl. " +
                                "wsdl path: " + path + ".";
                        log.error(msg);
                        // we are just returning, as the put operation will continue in its default path.
                        return;
                    }

                    File tempFile = tempFiles.get(0);
                    String uri = tempFile.toURI().toString();
                    if (uri.startsWith("file:")) {
                        uri = uri.substring(5);
                    }
                    while (uri.startsWith("/")) {
                        uri = uri.substring(1);
                    }
                    uri = "file:///" + uri;
                    String wsdlPath = null;
                    if (uri != null) {
                        requestContext.setSourceURL(uri);
                        requestContext.setResource(metadata);

                        wsdlProcessor = buildWSDLProcessor(requestContext);
                        wsdlPath = processWSDLImport(requestContext, wsdlProcessor, metadata, uri);
                    }

                    // now we will delete each temp files, ref file and the temp directory.
                    for (File temp : tempFiles) {
                        FileUtils.forceDelete(temp);
                    }
                    FileUtils.deleteDirectory(tempDir);
                    FileUtils.forceDelete(referenceTempFile);

                    if (wsdlPath != null) {
                        onPutCompleted(path, Collections.singletonMap(uri, wsdlPath),
                                Collections.<String>emptyList(), requestContext);
                        requestContext.setActualPath(wsdlPath);
                    }
                    requestContext.setProcessingComplete(true);
                    ArtifactManager.getArtifactManager().getTenantArtifactRepository().
                            addArtifact(path);
                    return;
                }
            } catch (IOException e) {
                String msg = "Error in updating the wsdl. wsdl path: " + path + ".";
                log.error(msg, e);
                throw new RegistryException(msg, e);
            }

            requestContext.setSourceURL(requestContext.
                    getResource().getProperty(CommonConstants.SOURCEURL_PARAMETER_NAME));

            if (StringUtils.isNotBlank(requestContext.getSourceURL())) {
                requestContext.setResource(metadata);
                String sourceURL = requestContext.getSourceURL();

                wsdlProcessor = buildWSDLProcessor(requestContext);
                String wsdlPath = processWSDLImport(requestContext, wsdlProcessor, metadata, sourceURL);

                onPutCompleted(path, Collections.singletonMap(sourceURL, wsdlPath),
                        Collections.<String>emptyList(), requestContext);
                requestContext.setActualPath(wsdlPath);
            } else {
                try {
                    Object resourceContent = metadata.getContent();
                    byte[] resourceContentBytes;

                    if (resourceContent == null) {
                        resourceContentBytes = new byte[0];
                    } else if (resourceContent instanceof byte[]) {
                        resourceContentBytes = (byte[]) resourceContent;
                    } else if (resourceContent instanceof String) {
                        resourceContentBytes = RegistryUtils.encodeString((String) resourceContent);
                    } else {
                        String msg = "Unknown type for the content path: " + path + ", content type: " +
                                resourceContent.getClass().getName() + ".";
                        log.error(msg);
                        throw new RegistryException(msg);
                    }
                    InputStream in = new ByteArrayInputStream(resourceContentBytes);
                    File tempFile = File.createTempFile("wsdl", ".wsdl");
                    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tempFile));

                    String uri = null;
                    try {
                        byte[] contentChunk = new byte[1024];
                        int byteCount;
                        while ((byteCount = in.read(contentChunk)) != -1) {
                            out.write(contentChunk, 0, byteCount);
                        }
                        uri = tempFile.toURI().toString();
                    } finally {
                        if (in != null) {
                            in.close();
                        }
                        if (out != null) {
                            out.flush();
                            out.close();
                        }
                    }
                    if (StringUtils.isNotBlank(uri) && uri.startsWith("file:")) {
                        uri = uri.substring(5);
                    }
                    while (uri.startsWith("/")) {
                        uri = uri.substring(1);
                    }
                    String wsdlPath = null;
                    if (StringUtils.isNotBlank(uri)) {
                        uri = "file:///" + uri;
                        requestContext.setSourceURL(uri);
                        requestContext.setResource(metadata);

                        wsdlProcessor = buildWSDLProcessor(requestContext);
                        wsdlPath = processWSDLImport(requestContext, wsdlProcessor, metadata, uri);
                    }
                    delete(tempFile);
                    if (wsdlPath != null) {
                        onPutCompleted(path, Collections.singletonMap(uri, wsdlPath),
                                Collections.<String>emptyList(), requestContext);
                        requestContext.setActualPath(wsdlPath);
                    }
                /*WSDLProcessor wsdlProcessor = buildWSDLProcessor(requestContext);
                wsdlProcessor
                       .addWSDLToRegistry(
                               requestContext, null,
                               metadata, true, true);*/
                } catch (IOException e) {
                    String msg = "An error occurred while uploading WSDL file or " +
                            "deleting the temporary files from local file system.";
                    log.error(msg, e);
                    throw new RegistryException(msg, e);
                }
            }

            requestContext.setProcessingComplete(true);
            if (wsdlProcessor != null && CommonConstants.ENABLE.equals(System.getProperty(CommonConstants.UDDI_SYSTEM_PROPERTY))
                                      && !org.wso2.carbon.registry.common.CommonConstants.isExternalUDDIInvoke.get()) {
                AuthToken authToken = UDDIUtil.getPublisherAuthToken();
                if(authToken ==null){
                    return;
                }
                BusinessServiceInfo businessServiceInfo = new BusinessServiceInfo();
                WSDLInfo wsdlInfo = wsdlProcessor.getMasterWSDLInfo();
                businessServiceInfo.setServiceWSDLInfo(wsdlInfo);
                UDDIPublisher publisher = new UDDIPublisher();
                publisher.publishBusinessService(authToken,businessServiceInfo);
            }
        } finally {
            CommonUtil.releaseUpdateLock();
        }
    }

    /**
     * Method to customize the WSDL Processor.
     * @param requestContext the request context for the import/put operation
     * @return the WSDL Processor instance.
     */
    @SuppressWarnings("unused")
    protected WSDLProcessor buildWSDLProcessor(RequestContext requestContext) {
        WSDLProcessor wsdlProcessor = new WSDLProcessor(requestContext);
        wsdlProcessor.setCreateService(getCreateService());
        wsdlProcessor.setCreateSOAPService(isCreateSOAPService());
        return wsdlProcessor;
    }

    /**
     * Method to customize the WSDL Processor.
     * @param requestContext the request context for the import/put operation
     * @param useOriginalSchema whether the schema to be original
     * @return the WSDL Processor instance.
     */
    @SuppressWarnings("unused")
    protected WSDLProcessor buildWSDLProcessor(RequestContext requestContext, boolean useOriginalSchema) {
        WSDLProcessor wsdlProcessor = new WSDLProcessor(requestContext, useOriginalSchema);
        wsdlProcessor.setCreateService(getCreateService());
        wsdlProcessor.setCreateSOAPService(isCreateSOAPService());
        return wsdlProcessor;
    }

    /**
     * Method to customize the Schema Processor.
     * @param requestContext the request context for the import/put operation.
     * @param validationInfo the WSDL validation information.
     * @return the Schema Processor instance.
     */
    @SuppressWarnings("unused")
    protected SchemaProcessor buildSchemaProcessor(RequestContext requestContext,
                                                 WSDLValidationInfo validationInfo) {
        return new SchemaProcessor(requestContext, validationInfo);
    }

    /**
     * Method to customize the Schema Processor.
     * @param requestContext the request context for the import/put operation.
     * @param validationInfo the WSDL validation information.
     * @param useOriginalSchema whether the schema to be original
     * @return the Schema Processor instance.
     */
    @SuppressWarnings("unused")
    protected SchemaProcessor buildSchemaProcessor(RequestContext requestContext,
                                                 WSDLValidationInfo validationInfo, boolean useOriginalSchema) {
        return new SchemaProcessor(requestContext, validationInfo, useOriginalSchema);
    }

    public void importResource(RequestContext requestContext) throws RegistryException {
        if (!CommonUtil.isUpdateLockAvailable()) {
            return;
        }
        CommonUtil.acquireUpdateLock();
        WSDLProcessor wsdlProcessor=null;
        try {
            Resource metadata = requestContext.getResource();
            String sourceURL = requestContext.getSourceURL();
            if (requestContext.getSourceURL() != null &&
                    requestContext.getSourceURL().toLowerCase().startsWith("file:")) {
                String msg = "The source URL must not be file in the server's local file system";
                throw new RegistryException(msg);
            }
            try {
                wsdlProcessor = buildWSDLProcessor(requestContext);
                String wsdlPath =
                        processWSDLImport(requestContext, wsdlProcessor, metadata, sourceURL);
                ResourcePath resourcePath = requestContext.getResourcePath();
                String path = null;
                if (resourcePath != null) {
                    path = resourcePath.getPath();
                }
                onPutCompleted(path,
                        Collections.singletonMap(sourceURL, wsdlPath),
                        Collections.<String>emptyList(), requestContext);
                requestContext.setActualPath(wsdlPath);
            } catch (Exception e) {
                throw new RegistryException(e.getMessage(), e);
            }

            requestContext.setProcessingComplete(true);
            if (wsdlProcessor != null && CommonConstants.ENABLE.equals(System.getProperty(CommonConstants.UDDI_SYSTEM_PROPERTY))
                                      && !org.wso2.carbon.registry.common.CommonConstants.isExternalUDDIInvoke.get()) {
                AuthToken authToken = UDDIUtil.getPublisherAuthToken();
                if(authToken == null){
                    return;
                }
                BusinessServiceInfo businessServiceInfo = new BusinessServiceInfo();
                businessServiceInfo.setServiceWSDLInfo(wsdlProcessor.getMasterWSDLInfo());
                UDDIPublisher publisher = new UDDIPublisher();
                publisher.publishBusinessService(authToken, businessServiceInfo);
            }
        } finally {
            CommonUtil.releaseUpdateLock();
        }
    }

    /**
     * Method that runs the WSDL import/upload procedure.
     *
     * @param requestContext the request context for the import/put operation
     * @param metadata the resource metadata
     * @param sourceURL the URL from which the WSDL is imported
     * @param wsdlProcessor the WSDL Processor instance, used for upload and validation
     *
     * @return the path at which the WSDL was uploaded to
     *
     * @throws RegistryException if the operation failed.
     */
    protected String processWSDLImport(RequestContext requestContext, WSDLProcessor wsdlProcessor,
                                       Resource metadata, String sourceURL)
            throws RegistryException {
        return wsdlProcessor.addWSDLToRegistry(requestContext, sourceURL, metadata, false, true,
                disableWSDLValidation,disableSymlinkCreation);
    }

    public void setDisableWSDLValidation(String disableWSDLValidation) {
        this.disableWSDLValidation = Boolean.toString(true).equals(disableWSDLValidation);
    }

    @Override
    public void delete(RequestContext requestContext) throws RegistryException {
        if (!CommonUtil.isUpdateLockAvailable()) {
            return;
        }
        CommonUtil.acquireUpdateLock();
        try {
            Registry registry = requestContext.getRegistry();
            ResourcePath resourcePath = requestContext.getResourcePath();
            if (resourcePath == null) {
                throw new RegistryException("The resource path is not available.");
            }
            Resource resource = registry.get(resourcePath.getPath());
        } finally {
            CommonUtil.releaseUpdateLock();
        }
    }
}
