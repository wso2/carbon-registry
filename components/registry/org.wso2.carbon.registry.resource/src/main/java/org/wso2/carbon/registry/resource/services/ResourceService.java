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

package org.wso2.carbon.registry.resource.services;

import org.apache.commons.io.FileUtils;
import org.wso2.carbon.registry.admin.api.resource.IResourceService;
import org.wso2.carbon.registry.common.ResourceData;
import org.wso2.carbon.registry.common.services.RegistryAbstractAdmin;
import org.wso2.carbon.registry.common.utils.RegistryUtil;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.MediaTypesUtils;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.resource.beans.*;
import org.wso2.carbon.registry.resource.services.utils.*;

import javax.activation.DataHandler;
import java.io.*;

public class ResourceService extends RegistryAbstractAdmin implements IResourceService<MetadataBean, CollectionContentBean, ResourceData, ContentBean, PermissionBean, VersionsBean, ResourceTreeEntryBean, ContentDownloadBean> {

    public MetadataBean getMetadata(String path) throws Exception {
        RegistryUtil.setSessionResourcePath(path);
        UserRegistry registry = (UserRegistry)getRootRegistry();
        return MetadataPopulator.populate(path, registry);
    }

    public void setDescription(String path, String description) throws Exception {
        UserRegistry registry = (UserRegistry) getRootRegistry();
        if (RegistryUtils.isRegistryReadOnly(registry.getRegistryContext())) {
            return;
        }
        DescriptionUtil.setDescription(registry, path, description);
    }
    public void updateMediaType(String path ,String mediaType) throws Exception {
        UserRegistry registry = (UserRegistry) getRootRegistry();
        if (RegistryUtils.isRegistryReadOnly(registry.getRegistryContext())) {
            return;
        }
        MediaTypeUtil.updateMediaType(registry,path,mediaType);
    }

    public CollectionContentBean getCollectionContent(String path) throws Exception {
        UserRegistry registry = (UserRegistry)getRootRegistry();
        return ContentUtil.getCollectionContent(path, registry);
    }

    public ResourceData[] getResourceData(String[] paths) throws Exception {
        UserRegistry registry = (UserRegistry)getRootRegistry();
        return ContentUtil.getResourceData(paths, registry);
    }

    public ContentBean getContentBean(String path) throws Exception {

        UserRegistry registry = (UserRegistry)getRootRegistry();
        return ContentUtil.getContent(path, registry);
    }

    public String addCollection(
            String parentPath, String collectionName, String mediaType, String description)
            throws Exception {
        UserRegistry registry = (UserRegistry) getRootRegistry();
        if (RegistryUtils.isRegistryReadOnly(registry.getRegistryContext())) {
            return null;
        }
        return AddCollectionUtil.process(parentPath, collectionName, mediaType, description, registry);
    }

    // TODO: this method must be removed, we will only have addTextContent in the future. 
    // Look into proper deprecation procedure. - Senaka.
    public boolean addTextResource(
            String parentPath,
            String fileName,
            String mediaType,
            String description,
            String content) throws Exception {
        return addTextContent(parentPath, fileName, mediaType, description, content);
    }

    public boolean addTextContent(
            String parentPath,
            String fileName,
            String mediaType,
            String description,
            String content) throws Exception {
        UserRegistry registry = (UserRegistry) getRootRegistry(CommonUtil.getRegistryService());
        if (RegistryUtils.isRegistryReadOnly(registry.getRegistryContext())) {
            return false;
        }
        AddTextResourceUtil.addTextResource(parentPath, fileName, mediaType, description, content, registry);
        return true;
    }

    public void addSymbolicLink (String parentPath,
                                 String name,
                                 String targetPath) throws Exception {
        UserRegistry registry = (UserRegistry) getRootRegistry();
        if (RegistryUtils.isRegistryReadOnly(registry.getRegistryContext())) {
            return;
        }
        AddSymbolicLinkUtil.addSymbolicLink(registry, parentPath, name, targetPath);
    }

    public void addRemoteLink (String parentPath,
                                 String name,
                                 String instance,
                                 String targetPath) throws Exception {
        UserRegistry registry = (UserRegistry) getRootRegistry();
        if (RegistryUtils.isRegistryReadOnly(registry.getRegistryContext())) {
            return;
        }
        AddRemoteLinkUtil.addRemoteLink(registry, parentPath, name, instance, targetPath);
    }

    public boolean importResource(
            String parentPath,
            String resourceName,
            String mediaType,
            String description,
            String fetchURL,
            String symlinkLocation,
            String[][] properties) throws Exception {
        UserRegistry registry = (UserRegistry) getRootRegistry(CommonUtil.getRegistryService());
        if (RegistryUtils.isRegistryReadOnly(registry.getRegistryContext())) {
            return false;
        }
        ImportResourceUtil.
                importResource(parentPath, resourceName, mediaType, description, fetchURL,
                        symlinkLocation, registry, properties);
        return true;
    }

    public boolean delete(String pathToDelete) throws Exception {
        UserRegistry registry = (UserRegistry) getRootRegistry();
        if (RegistryUtils.isRegistryReadOnly(registry.getRegistryContext())) {
            return false;
        }
        DeleteUtil.process(pathToDelete, registry);
        return true;
    }    

    public PermissionBean getPermissions(String path) throws Exception {
        try {
            return PermissionUtil.getPermissions((UserRegistry)getRootRegistry(), path);
        } catch (Exception e) {
            throw new ResourceServiceException(e.getMessage(),e);
        }
    }

    public boolean addUserPermission(
            String pathToAuthorize,
            String userToAuthorize,
            String actionToAuthorize,
            String permissionType) throws ResourceServiceException {

        throw new UnsupportedOperationException("This operation is no longer supported");

        /*try {
            AddUserPermissionUtil.addUserPermission(
                    pathToAuthorize, userToAuthorize, actionToAuthorize, permissionType);
        } catch (Exception e) {
            throw new ResourceServiceException(e.getMessage(),e);
        }
        return true;*/
    }

    public boolean addRolePermission(
            String pathToAuthorize,
            String roleToAuthorize,
            String actionToAuthorize,
            String permissionType) throws ResourceServiceException  {
        try {
            AddRolePermissionUtil.addRolePermission((UserRegistry)getRootRegistry(),
                    pathToAuthorize, roleToAuthorize, actionToAuthorize, permissionType);
            setPermissionUpdateTimestamp();
        } catch (Exception e) {
            throw new ResourceServiceException(e.getMessage(),e);
        }
        return true;
    }

    public boolean changeUserPermissions(String resourcePath, String permissionInput)
            throws Exception {

        throw new UnsupportedOperationException("This operation is no longer supported");

        /*try {
            ChangeUserPermissionsUtil.changeUserPermissions(resourcePath, permissionInput);
        } catch (Exception e) {
            throw new ResourceServiceException(e.getMessage(),e);
        }
        return true;*/
    }

    public boolean changeRolePermissions(String resourcePath, String permissionsInput)
            throws Exception {
        try {
            ChangeRolePermissionsUtil.changeRolePermissions((UserRegistry)getRootRegistry(),
                    resourcePath, permissionsInput);
            setPermissionUpdateTimestamp();
        } catch (Exception e) {
            throw new ResourceServiceException(e.getMessage(),e);
        }
        return true;
    }

    public String getTextContent(String path) throws Exception {
        Registry registry = (UserRegistry)getRootRegistry();
        return GetTextContentUtil.getTextContent(path, registry);
    }

    public boolean updateTextContent(String resourcePath, String contentText) throws Exception {
        UserRegistry registry = (UserRegistry) getRootRegistry();
        if (RegistryUtils.isRegistryReadOnly(registry.getRegistryContext())) {
            return false;
        }
        UpdateTextContentUtil.updateTextContent(resourcePath, contentText, registry);
        return true;
    }

    public boolean addResource(String path, String mediaType, String description, DataHandler content,
                            String symlinkLocation, String[][] properties)
            throws Exception {
        UserRegistry registry = (UserRegistry) getRootRegistry(CommonUtil.getRegistryService());
        if (RegistryUtils.isRegistryReadOnly(registry.getRegistryContext())) {
            return false;
        }
        AddResourceUtil.addResource(path, mediaType, description, content, symlinkLocation,
                registry, properties);
        return true;
    }

    public boolean addExtension(String name, DataHandler content) throws Exception {
        File extension = new File(RegistryUtils.getExtensionLibDirectoryPath() + File.separator +
                name);
        File extensionsDirectory = extension.getParentFile();
        if (extensionsDirectory.exists() || extensionsDirectory.mkdir()) {
            OutputStream os = new FileOutputStream(extension);
            try {
                content.writeTo(os);
            } finally {
                os.close();
            }
        }
        return true;
    }

    public String[] listExtensions() throws Exception {
        File extensionsDirectory = new File(RegistryUtils.getExtensionLibDirectoryPath());
        if (extensionsDirectory.exists()) {
            String[] names = extensionsDirectory.list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name != null && name.endsWith(".jar");
                }
            });
            if (names != null && names.length > 0) {
                return names;
            }
        }
        return new String[0];
    }

    public boolean removeExtension(String name) throws Exception {
        File extension = new File(RegistryUtils.getExtensionLibDirectoryPath() + File.separator +
                name);
        FileUtils.forceDelete(extension);
        return true;
    }

    public boolean renameResource(
            String parentPath, String oldResourcePath, String newResourceName)
            throws Exception {
        UserRegistry registry = (UserRegistry) getRootRegistry();
        if (RegistryUtils.isRegistryReadOnly(registry.getRegistryContext())) {
            return false;
        }
        RenameResourceUtil.renameResource(parentPath, oldResourcePath, newResourceName, registry);
        return true;
    }

    public boolean copyResource(
            String parentPath, String oldResourcePath, String destinationPath, String resourceName)
            throws Exception {
        UserRegistry registry = (UserRegistry) getRootRegistry();
        if (RegistryUtils.isRegistryReadOnly(registry.getRegistryContext())) {
            return false;
        }
        CopyResourceUtil.copyResource(registry, parentPath,
                oldResourcePath, destinationPath, resourceName);
        return true;
    }

    public boolean moveResource(
            String parentPath, String oldResourcePath, String destinationPath, String resourceName)
            throws Exception {
        UserRegistry registry = (UserRegistry) getRootRegistry();
        if (RegistryUtils.isRegistryReadOnly(registry.getRegistryContext())) {
            return false;
        }
        MoveResourceUtil.moveResource(registry, parentPath,
                oldResourcePath, destinationPath, resourceName);
        return true;
    }

    public String getSessionResourcePath() throws Exception {
        return RegistryUtil.getSessionResourcePath();
    }

    public void setSessionResourcePath(String resourcePath) throws Exception {
        RegistryUtil.setSessionResourcePath(resourcePath);
    }

    public ResourceTreeEntryBean getResourceTreeEntry(String resourcePath) throws Exception {
        UserRegistry registry = (UserRegistry)getRootRegistry();
        return GetResourceTreeEntryUtil.getResourceTreeEntry(resourcePath, registry);
    }

    public boolean createVersion(String resourcePath) throws Exception {
        UserRegistry registry = (UserRegistry) getRootRegistry();
        if (RegistryUtils.isRegistryReadOnly(registry.getRegistryContext())) {
            return false;
        }
        CreateVersionUtil.createVersion(registry, resourcePath);
        return true;
    }

    public boolean restoreVersion(String versionPath) throws Exception {
        UserRegistry registry = (UserRegistry) getRootRegistry();
        if (RegistryUtils.isRegistryReadOnly(registry.getRegistryContext())) {
            return false;
        }
        RestoreVersionUtil.restoreVersion(registry, versionPath);
        return true;
    }

    public VersionsBean getVersionsBean(String path) throws Exception {
        return GetVersionsUtil.getVersionsBean((UserRegistry)getRootRegistry(), path);
    }

    public String getMediatypeDefinitions() throws Exception {
        Registry configSystemRegistry = getConfigSystemRegistry();
        return MediaTypesUtils.getResourceMediaTypeMappings(configSystemRegistry);
    }

    public String getCollectionMediatypeDefinitions() throws Exception {
        Registry configSystemRegistry = getConfigSystemRegistry();
        return MediaTypesUtils.getCollectionMediaTypeMappings(configSystemRegistry);
    }

    public String getCustomUIMediatypeDefinitions() throws Exception {
        Registry configSystemRegistry = getConfigSystemRegistry();
        return MediaTypesUtils.getCustomUIMediaTypeMappings(configSystemRegistry);
    }

    public String getProperty(String resourcePath, String key) throws Exception {
        return GetPropertyUtil.getProperty((UserRegistry)getRootRegistry(), resourcePath, key);
    }


    public ContentDownloadBean getContentDownloadBean(String path) throws Exception {
        UserRegistry userRegistry = (UserRegistry)getRootRegistry();
        return GetDownloadContentUtil.getContentDownloadBean(path, userRegistry);
    }

    public String getHumanReadableMediaTypes() throws Exception{
        return MediaTypesUtils.getAllHumanTypes();
    }

    public String getMimeTypeFromHuman(String mediaType) throws Exception{
        return MediaTypesUtils.getMimeTypeFromHumanReadableMediaType(mediaType);
    }
    
    public boolean deleteVersionHistory(String path, String snapshotId) throws Exception {
        UserRegistry registry = (UserRegistry) getRootRegistry();       
        
        long snapshotID = Long.parseLong(snapshotId);        
        DeleteVersionUtil.process(path, snapshotID, registry);
        return true;
    }

    public ContentDownloadBean getZipWithDependencies(String path) throws Exception {
        UserRegistry registry = (UserRegistry) getRootRegistry();
        return ContentUtil.getContentWithDependencies(path,registry);
    }

    public boolean hasAssociations(String path,String type) throws Exception {
        UserRegistry registry = (UserRegistry) getRootRegistry();
        return ContentUtil.hasAssociations(path,type,registry);
    }

}