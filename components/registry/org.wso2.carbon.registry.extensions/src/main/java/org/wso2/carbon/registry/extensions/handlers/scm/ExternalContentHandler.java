/*
 *  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.wso2.carbon.registry.extensions.handlers.scm;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class ExternalContentHandler extends Handler {

    private static final Log log = LogFactory.getLog(ExternalContentHandler.class);

    private FilesystemManager filesystemManager;
    private String mountPath;
    private volatile boolean busy;

    public void setFilePath(String filePath) {
        this.filesystemManager = new FilesystemManager(filePath);
    }

    public void setMountPath(String mountPath) {
        this.mountPath = mountPath;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    private void validateUpdateInProgress() throws RegistryException {
        if (busy) {
            throw new RegistryException("An update is currently in progress. Please try again " +
                    "later.");
        }
    }

    // Operations: CRUD
    // New Repo: Create File/Folder based on type of resource. If files can't be created throw an
    //           exception.
    // Existing Repo: If the destination is a folder, build a collection at that location, and
    //                recursively call get for each child. If the destination is a file, just load
    //                the content and create a resource at that location.
    // Moving, Copying: Compare source and destination with Base (need some way to specify base).
    //                  Call create/delete depending on whether the src/dest is on repo.
    // Dump, Restore needs to be handled properly.
    // Try to use ContentStreaming with an OnDemand resource to avoid caching overhead.

    // Updating the repo is a responsibility of the implementation and should use a task/thread for
    // that. Same applies for sync'ing.

    // Create a OperationContext to pass info (ex:- Username, Time) to the implementation. This
    // class will provide constants for key names.
    // This will always use a file-system back-up irrespective of the implementation.
    // Try to use a SCM configuration in registry.xml to make life easy (copy from maven).
    // http://svn.apache.org/repos/asf/maven/scm/trunk/maven-scm-client/src/main/java/org/apache/maven/scm/client/cli/MavenScmCli.java

    public Resource get(RequestContext requestContext) throws RegistryException {
        if (!CommonUtil.isSCMLockAvailable()) {
            return null;
        }
        CommonUtil.acquireSCMLock();
        try {
            Registry registry = requestContext.getRegistry();
            Resource resource = registry.get(requestContext.getResourcePath().getPath());
            String path = RegistryUtils.getRelativePathToOriginal(
                    requestContext.getResourcePath().getPath(), mountPath);
            if (resource instanceof Collection) {
                CollectionImpl collection = (CollectionImpl) resource;
                String[] children = filesystemManager.getDirectoryContent(path);
                List<String> temp = new LinkedList<String>();
                for (String child : children) {
                    if (path.equals(RegistryConstants.PATH_SEPARATOR)) {
                        temp.add(mountPath + RegistryConstants.PATH_SEPARATOR + child);
                    } else {
                        temp.add(mountPath + path + RegistryConstants.PATH_SEPARATOR + child);
                    }
                }
                children = temp.toArray(new String[temp.size()]);
                if (collection.getChildCount() != children.length) {
                    // if there is a difference in child counts, fix it.
                    if (collection.getChildCount() > 0) {
                        List<String> pathsToDelete = new ArrayList<String>(
                                Arrays.asList(collection.getChildren()));
                        pathsToDelete.removeAll(Arrays.asList(children));
                        for (String pathToDelete : pathsToDelete) {
                            registry.delete(pathToDelete);
                            filesystemManager.delete(RegistryUtils.getRelativePathToOriginal(
                                    pathToDelete, mountPath));
                        }
                    }
                    loadRegistryResources(registry,
                            new File(filesystemManager.getBaseDir(),
                                    RegistryUtils.getRelativePathToOriginal(path, mountPath)),
                            filesystemManager.getBaseDir().getAbsolutePath(), mountPath);
                }
                collection.setChildren(children);
            } else {
                return new OnDemandResourceImpl(filesystemManager, path, (ResourceImpl) resource);
            }
            requestContext.setProcessingComplete(true);
            return resource;
        } finally {
            CommonUtil.releaseSCMLock();
        }
    }

    public void put(RequestContext requestContext) throws RegistryException {
        if (!CommonUtil.isSCMLockAvailable()) {
            return;
        }
        validateUpdateInProgress();
        String path = RegistryUtils.getRelativePathToOriginal(
                requestContext.getResourcePath().getPath(), mountPath);
        preparePut(path, requestContext);
    }

    private void preparePut(String path, RequestContext requestContext) throws RegistryException {
        Resource resource = requestContext.getResource();
        if (resource instanceof Collection) {
            filesystemManager.createDirectory(path);
        } else {
            Object content = resource.getContent();
            if (content instanceof String) {
                filesystemManager.createOrUpdateFile(path, RegistryUtils.encodeString(((String)content)));
            } else {
                filesystemManager.createOrUpdateFile(path, (byte[])content);
            }
            //resource.setContent("");
        }
    }

    public void delete(RequestContext requestContext) throws RegistryException {
        if (!CommonUtil.isSCMLockAvailable()) {
            return;
        }
        validateUpdateInProgress();
        String path = RegistryUtils.getRelativePathToOriginal(
                requestContext.getResourcePath().getPath(), mountPath);
        filesystemManager.delete(path);
    }

    public void importResource(RequestContext requestContext) throws RegistryException {
        validateUpdateInProgress();
        try {
            URL sourceURL = new URL(requestContext.getSourceURL());
            InputStream inputStream = sourceURL.openStream();
            try {
                requestContext.getResource().setContent(IOUtils.toByteArray(inputStream));
            } finally {
                inputStream.close();
            }
        } catch (MalformedURLException e) {
            throw new RegistryException("Unable to connect to URL", e);
        } catch (IOException e) {
            throw new RegistryException("Unable to download URL content", e);
        }
        put(requestContext);
    }

    public String move(RequestContext requestContext) throws RegistryException {
        validateUpdateInProgress();
        String sourcePath = requestContext.getSourcePath();
        String targetPath = requestContext.getTargetPath();
        if (sourcePath.startsWith(mountPath) && targetPath.startsWith(mountPath)) {
            String source = RegistryUtils.getRelativePathToOriginal(sourcePath, mountPath);
            filesystemManager.copy(source,
                    RegistryUtils.getRelativePathToOriginal(targetPath, mountPath));
            filesystemManager.delete(source);
        } else if (targetPath.startsWith(mountPath)) {
            preparePut(RegistryUtils.getRelativePathToOriginal(targetPath, mountPath),
                    requestContext);
        } else if (sourcePath.startsWith(mountPath)) {
            filesystemManager.delete(
                    RegistryUtils.getRelativePathToOriginal(sourcePath, mountPath));
        }
        return null;
    }

    public String copy(RequestContext requestContext) throws RegistryException {
        validateUpdateInProgress();
        String sourcePath = requestContext.getSourcePath();
        String targetPath = requestContext.getTargetPath();
        if (sourcePath.startsWith(mountPath) && targetPath.startsWith(mountPath)) {
            filesystemManager.copy(RegistryUtils.getRelativePathToOriginal(sourcePath, mountPath),
                    RegistryUtils.getRelativePathToOriginal(targetPath, mountPath));
        } else if (targetPath.startsWith(mountPath)) {
            preparePut(RegistryUtils.getRelativePathToOriginal(targetPath, mountPath),
                    requestContext);
        }
        return null;
    }

    public String rename(RequestContext requestContext) throws RegistryException {
        validateUpdateInProgress();
        String sourcePath = requestContext.getSourcePath();
        String targetPath = requestContext.getTargetPath();
        String source = RegistryUtils.getRelativePathToOriginal(sourcePath, mountPath);
        filesystemManager.copy(source,
                RegistryUtils.getRelativePathToOriginal(targetPath, mountPath));
        filesystemManager.delete(source);
        return null;
    }

    public void restore(RequestContext requestContext) throws RegistryException {
        log.warn("Skipping restoration to path: " + requestContext.getResourcePath().getPath());
    }

    private static class OnDemandResourceImpl extends ResourceImpl {
        private String filePath;
        private FilesystemManager filesystemManager;

        public OnDemandResourceImpl(FilesystemManager filesystemManager, String filePath,
                                    ResourceImpl resource) {
            this.filePath = filePath;
            this.filesystemManager = filesystemManager;
            if (resource.getDescription() != null) {
                setDescription(resource.getDescription());
            }
            if (resource.getMediaType() != null) {
                setMediaType(resource.getMediaType());
            }
            if (resource.getProperties() != null) {
                setProperties(new Properties(resource.getProperties()));
            }
            setAuthorUserName(resource.getAuthorUserName());
            setCreatedTime(new Date(resource.getCreatedTime().getTime()));
            setId(resource.getId());
            setLastModified(new Date(resource.getLastModified().getTime()));
            setLastUpdaterUserName(resource.getLastUpdaterUserName());
            setParentPath(resource.getParentPath());
            setPath(resource.getPath());
            setMatchingSnapshotID(resource.getMatchingSnapshotID());
        }

        public Object getContent() throws RegistryException {
            try {
                if (content == null) { // fetch data only if it hasn't been fetched previously
                    content = filesystemManager.getFileContent(filePath);
                }
                return content;
            } catch (Exception e) {
                throw new RegistryException("Failed to get resource content", e);
            }
        }
    }

    private void loadRegistryResources(Registry registry, File directory, String workingDir,
                                             String mountPoint) throws RegistryException {
        File[] files = directory.listFiles((FileFilter) new AndFileFilter(HiddenFileFilter.VISIBLE,
                new OrFileFilter(DirectoryFileFilter.INSTANCE, FileFileFilter.FILE)));
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                loadRegistryResources(registry, file, workingDir, mountPoint);
            } else {
                // convert windows paths so that it fits into the Unix-like registry path structure.
                String path = mountPoint +
                        file.getAbsolutePath().substring(workingDir.length()).replace("\\", "/");
                if (!registry.resourceExists(path)) {
                    registry.put(path, registry.newResource());
                }
            }
        }
    }
}
