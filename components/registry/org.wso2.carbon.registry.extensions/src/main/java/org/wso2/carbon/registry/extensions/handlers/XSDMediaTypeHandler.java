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
import org.apache.xerces.xni.parser.XMLInputSource;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.AuthorizationUtils;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.handlers.utils.SchemaProcessor;
import org.wso2.carbon.registry.extensions.handlers.utils.SchemaValidator;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;
import org.wso2.carbon.registry.extensions.utils.WSDLValidationInfo;
import org.wso2.carbon.user.mgt.UserMgtConstants;

import javax.xml.namespace.QName;
import java.io.*;
import java.util.*;

public class XSDMediaTypeHandler extends Handler {
    private static final Log log = LogFactory.getLog(XSDMediaTypeHandler.class);
    private String location = "/schema/";        // location will always has a leading '/' and trailing '/'
    private String locationTag = "location";
    private boolean disableSchemaValidation = false;
    private boolean disableSymlinkCreation = true;
    private String defaultSchemaVersion = CommonConstants.SCHEMA_VERSION_DEFAULT_VALUE;

    public OMElement getLocationConfiguration() {
        return locationConfiguration;
    }

    public void setLocationConfiguration(OMElement locationConfiguration) throws RegistryException {
        Iterator confElements = locationConfiguration.getChildElements();
        while (confElements.hasNext()) {
            OMElement confElement = (OMElement) confElements.next();
            if (confElement.getQName().equals(new QName(locationTag))) {
                location = confElement.getText();
                if (!location.startsWith(RegistryConstants.PATH_SEPARATOR)) {
                    location = RegistryConstants.PATH_SEPARATOR + location;
                }
                if (!location.endsWith(RegistryConstants.PATH_SEPARATOR)) {
                    location = location + RegistryConstants.PATH_SEPARATOR;
                }
            }
        }
        this.locationConfiguration = locationConfiguration;
    }

    public boolean isDisableSymlinkCreation() {
        return disableSymlinkCreation;
    }

    public void setDisableSymlinkCreation(String disableSymlinkCreation) {
        this.disableSymlinkCreation = Boolean.toString(true).equals(disableSymlinkCreation);
    }

    public void setDefaultServiceVersion(String defaultSchemaVersion) {
        this.defaultSchemaVersion = defaultSchemaVersion;
    }

    private OMElement locationConfiguration;

    public void put(RequestContext requestContext) throws RegistryException {
        if (!CommonUtil.isUpdateLockAvailable()) {
            return;
        }
        CommonUtil.acquireUpdateLock();
        try {
            Resource resource = requestContext.getResource();
            String resourcePath = requestContext.getResourcePath().getPath();
            String parentPath = RegistryUtils.getParentPath(resourcePath);
            // String sourceURL = requestContext.getSourceURL();
            Registry registry = requestContext.getRegistry();

            // This is to distinguish operations on xsd and wsdl on remote mounting.
            String remotePut = resource.getProperty(RegistryConstants.REMOTE_MOUNT_OPERATION);
            if (remotePut != null) {
                CommonUtil.releaseUpdateLock();
                resource.removeProperty(RegistryConstants.REMOTE_MOUNT_OPERATION);
                registry.put(resourcePath, resource);
                requestContext.setProcessingComplete(true);
                return;
            }

            String oldResourcePath = null;

            if (registry.resourceExists(resourcePath)) {
                // If the resource is already there and the content is not changed, perform the default processing.
                // logic to compare content, and return only if the content didn't change.
                Object newContent = resource.getContent();
                if (newContent instanceof String) {
                    newContent = RegistryUtils.encodeString(((String) newContent));
                }
                Resource oldResource = registry.get(resourcePath);
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
                oldResourcePath = resourcePath; // keep the old resource path.
            }
            WSDLValidationInfo validationInfo = null;
            String savedName;

            requestContext.setSourceURL(
                    requestContext.getResource().getProperty(CommonConstants.SOURCEURL_PARAMETER_NAME));
            String sourceURL = requestContext.getSourceURL();

            if (StringUtils.isNotBlank(sourceURL)) {
                if (requestContext.getSourceURL().toLowerCase()
                        .startsWith("file:")) {
                    String msg = "The source URL must not be file in the server's local file system";
                    throw new RegistryException(msg);
                }
                try {
                    if (!disableSchemaValidation) {
                        validationInfo = SchemaValidator.validate(new XMLInputSource(null, sourceURL, null));
                    }
                } catch (Exception e) {
                    // Since SchemaValidator.validate method is throwing Exception need to catch it here
                    throw new RegistryException("Exception occurred while validating the schema", e);
                }

                savedName = processSchemaImport(requestContext, resourcePath, validationInfo);

            } else {
                Object resourceContent = resource.getContent();
                if (resourceContent instanceof String) {
                    resourceContent = RegistryUtils.encodeString(((String) resourceContent));
                    resource.setContent(resourceContent);
                }
                if (resourceContent instanceof byte[]) {
                    try {
                        InputStream in = new ByteArrayInputStream((byte[]) resourceContent);
                        if (!disableSchemaValidation) {
                            validationInfo = SchemaValidator.
                                    validate(new XMLInputSource(null, null, null, in, null));
                        }
                    } catch (Exception e) {
                        // Since SchemaValidator.validate method is throwing Exception need to catch it here
                        throw new RegistryException("Exception occurred while validating the schema", e);
                    }
                }

                savedName = processSchemaUpload(requestContext, resourcePath, validationInfo);
            }
            if (parentPath.endsWith(RegistryConstants.PATH_SEPARATOR)) {
                requestContext.setActualPath(parentPath + RegistryUtils.getResourceName(savedName));
            } else {
                requestContext.setActualPath(parentPath + RegistryConstants.PATH_SEPARATOR +
                        RegistryUtils.getResourceName(savedName));
            }

            if (StringUtils.isNotBlank(savedName)) {
                onPutCompleted(resourcePath, Collections.singletonMap(sourceURL, savedName),
                        Collections.<String>emptyList(), requestContext);
            }

            requestContext.setProcessingComplete(true);
        } finally {
            CommonUtil.releaseUpdateLock();
        }
    }

    public void importResource(RequestContext requestContext) throws RegistryException {
        if (!CommonUtil.isUpdateLockAvailable()) {
            return;
        }
        CommonUtil.acquireUpdateLock();
        try {
            String parentPath = RegistryUtils.getParentPath(requestContext.getResourcePath().getPath());
            String resourcePath = requestContext.getResourcePath().getCompletePath();

            String sourceURL = requestContext.getSourceURL();
            if (requestContext.getSourceURL() != null &&
                    requestContext.getSourceURL().toLowerCase().startsWith("file:")) {
                String msg = "The source URL must not be file in the server's local file system";
                throw new RegistryException(msg);
            }
            WSDLValidationInfo validationInfo = null;
            try {
                if (!disableSchemaValidation) {
                    validationInfo =
                            SchemaValidator.validate(new XMLInputSource(null, sourceURL, null));
                }
            } catch (Exception e) {
                throw new RegistryException("Exception occured while validating the schema", e);
            }

            String savedName = processSchemaImport(requestContext, resourcePath, validationInfo);

            if (parentPath.endsWith(RegistryConstants.PATH_SEPARATOR)) {
                requestContext.setActualPath(parentPath + RegistryUtils.getResourceName(savedName));
            } else {
                requestContext.setActualPath(parentPath + RegistryConstants.PATH_SEPARATOR +
                        RegistryUtils.getResourceName(savedName));
            }

            onPutCompleted(resourcePath,
                    Collections.singletonMap(sourceURL, savedName),
                    Collections.<String>emptyList(), requestContext);

            requestContext.setProcessingComplete(true);
        } finally {
            CommonUtil.releaseUpdateLock();
        }
    }

    /**
     * Method that runs the schema upload procedure.
     *
     * @param requestContext the request context for the put operation
     * @param resourcePath   the path of the resource
     * @param validationInfo the validation information
     * @return the path at which the schema was uploaded to
     * @throws RegistryException if the operation failed.
     */
    protected String processSchemaUpload(RequestContext requestContext, String resourcePath,
                                         WSDLValidationInfo validationInfo)
            throws RegistryException {

        String registryPath = null;
        List<File> tempFiles = makeTempDirStructure(requestContext);
        try {
            SchemaProcessor schemaProcessor =
                    buildSchemaProcessor(requestContext, validationInfo);

            registryPath = schemaProcessor
                    .putSchemaToRegistry(requestContext, resourcePath,
                            getChrootedLocation(requestContext.getRegistryContext()), true,disableSymlinkCreation);
        } finally {
            deleteTempFiles(tempFiles);
        }
        return registryPath;
    }

    /**
     * creates all the tmp dirs/files created in the tmp location in the file system to perform the XML Schema update.
     *
     * @param requestContext
     * @return
     * @throws RegistryException
     */
    private List<File> makeTempDirStructure(RequestContext requestContext) throws RegistryException {
        final String resourcePath = requestContext.getResource().getPath();
        final Registry registry = requestContext.getRegistry();

        List<File> tempFiles = new ArrayList<File>();

        if (resourcePath == null) {
            return tempFiles;
        }

        try {
            // creating temp files for the wsdl and all the dependencies.
            Set<String> registryPaths = new LinkedHashSet<String>();
            // the first resourcePath is the current resource resourcePath.
            registryPaths.add(resourcePath);
            // get the associations.
            Association[] dependencies = CommonUtil.getDependenciesRecursively(registry, resourcePath);
            if (dependencies != null) {
                for (Association dependency : dependencies) {
                    String targetPath = dependency.getDestinationPath();
                    if (targetPath.startsWith(RegistryConstants.ROOT_PATH)) {
                        registryPaths.add(targetPath);
                    }
                }
            }

            File referenceTempFile = File.createTempFile("xsd", ".ref");
            File tempDir = new File(referenceTempFile.getAbsolutePath().substring(0,
                    referenceTempFile.getAbsolutePath().length() - ".ref".length()));
            String tempDirPath = tempDir.getAbsolutePath();
            // now add each of the registry paths to the the tempDir
            for (String registryPath : registryPaths) {
                if (!registryPath.startsWith(RegistryConstants.ROOT_PATH)) {
                    continue;
                }
                String filePath = tempDirPath + registryPath;
                File tempFile = new File(filePath);
                makeDirs(tempFile.getParentFile());

                Object resourceContent;
                if (registryPath.equals(resourcePath)) {
                    // this is the xsd we want to update.
                    resourceContent = requestContext.getResource().getContent();
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
                    resourceContentBytes = (byte[]) resourceContent;
                } else if (resourceContent instanceof String) {
                    resourceContentBytes = RegistryUtils.encodeString(((String) resourceContent));
                } else {
                    String msg = "Unknown type for the content resourcePath: " + registryPath + ", content type: " +
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
                String msg = "Temporary files count is zero, when updating a xsd. " +
                        "xsd resourcePath: " + resourcePath + ".";
                log.error(msg);

            }

            File tempFile = tempFiles.get(0);
            String uri = tempFile.toURI().toString();
            if (uri != null) {
                if (uri.startsWith("file:")) {
                    uri = uri.substring(5);
                }
                while (uri.startsWith("/")) {
                    uri = uri.substring(1);
                }
                uri = "file:///" + uri;

                requestContext.setSourceURL(uri);
            }
            //adding the tmp dir for delete purposes.
            tempFiles.add(tempDir);
            //adding the root tmp dir created for delete purposes.
            tempFiles.add(referenceTempFile);
        } catch (IOException ioe) {
            String msg = "Error in updating the XML Schema. XML Schema resourcePath: " + resourcePath + ".";
            log.error(msg, ioe);
            throw new RegistryException(msg, ioe);
        }

        return tempFiles;
    }


    /**
     * deletes all the tmp dirs/files created in the tmp location in the file system to perform the XML Schema update.
     *
     * @param tempFiles
     * @throws IOException
     */
    private void deleteTempFiles(List<File> tempFiles) throws RegistryException {

        try {
            // now we will delete each temp files, ref file and the temp directory.
            final int fileSize = tempFiles.size();
            if (fileSize >= 2) {
                for (int i = 0; i < (fileSize - 2); i++) {
                    FileUtils.forceDelete(tempFiles.get(i));
                }
                //deleting the root tmp dir
                FileUtils.deleteDirectory(tempFiles.get(fileSize - 2));
                //deleting the tmp file
                FileUtils.forceDelete(tempFiles.get(fileSize - 1));
            }
        } catch (IOException ioe) {
            String msg = "Error in updating the XML Schema. XML Schema resourcePath: " + tempFiles.get(0) + ".";
            log.error(msg, ioe);
            throw new RegistryException(msg, ioe);
        }
    }

    /**
     * creates the parent directory structure for a given resource at a temp location in the file system.
     *
     * @param file
     * @throws IOException
     */
    private void makeDirs(File file) throws IOException {
        if (file != null && !file.exists() && !file.mkdirs()) {
            log.warn("Failed to create directories at path: " + file.getAbsolutePath());
        }
    }

    /**
     * Method to customize the Schema Processor.
     *
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
     * Method that runs the schema import procedure.
     *
     * @param requestContext the request context for the import operation
     * @param resourcePath   the path of the resource
     * @param validationInfo the validation information
     * @return the path at which the schema was uploaded to
     * @throws RegistryException if the operation failed.
     */
    protected String processSchemaImport(RequestContext requestContext, String resourcePath,
                                         WSDLValidationInfo validationInfo) throws RegistryException {
        SchemaProcessor schemaProcessor =
                buildSchemaProcessor(requestContext, validationInfo);

        return schemaProcessor
                .importSchemaToRegistry(requestContext, resourcePath,
                        getChrootedLocation(requestContext.getRegistryContext()), true,disableSymlinkCreation);
    }

    /**
     * Method that will executed after the put operation has been done.
     *
     * @param path           the path of the resource.
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

    private String getChrootedLocation(RegistryContext registryContext) {
        return RegistryUtils.getAbsolutePath(registryContext,
                RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + location);
    }

    public void setDisableSchemaValidation(String disableSchemaValidation) {
        this.disableSchemaValidation = Boolean.toString(true).equals(disableSchemaValidation);
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
