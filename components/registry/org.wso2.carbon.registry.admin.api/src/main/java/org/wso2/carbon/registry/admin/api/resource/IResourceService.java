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
package org.wso2.carbon.registry.admin.api.resource;

import javax.activation.DataHandler;

/**
 * This provides the functionality to manage resources and collections on the repository. The
 * resource browser on the WSO2 Carbon Management console uses the functionality exposed by this
 * interface.
 * <p/>
 * In addition to managing resources and collections, this also provides the capabilities of
 * managing resource permissions and also installing and un-installing extensions to the repository.
 *
 * @param <MetadataBean>          This bean can be used to access resource metadata, such as the
 *                                last modified time, the author, description and the whether this
 *                                resource is a collection or not. This also contains methods, to
 *                                obtain formatted representations of dates and times.
 * @param <CollectionContentBean> This bean can be used to access the content of a collection. A
 *                                collection on the repository is similar to a folder on a
 *                                filesystem. And, this bean can be used for operations such as
 *                                obtaining the child count of a collection or obtaining the list of
 *                                child resources under the given collection.
 * @param <ResourceData>          This contains the details of a resource, along with the various
 *                                permissions that are available on it. This also can be used to
 *                                obtain details of the ratings and tags that have been added to
 *                                this resource.
 * @param <ContentBean>           This bean is used to manage the content of this resource. This
 *                                has information on the type of resource and the various access
 *                                restrictions that have been made to it.
 * @param <PermissionBean>        This bean can be used to manage the permissions associated with
 *                                this resource or collection.
 * @param <VersionsBean>          This bean can be used to obtain details of versions that were
 *                                created for this resource or collections. The version path can be
 *                                used o browse through a version or restore back to it.
 * @param <ResourceTreeEntryBean> This bean is used in the process of generating the tree view of
 *                                the resource. This is used for the mere purpose of establishing a
 *                                parent-child relationship between elements of the resource tree.
 * @param <ContentDownloadBean>   This bean contains a data handler that can be used to access the
 *                                resource content. It also contains the media type and the last
 *                                updated time of the resource.
 */
public interface IResourceService<MetadataBean, CollectionContentBean, ResourceData, ContentBean,
        PermissionBean, VersionsBean, ResourceTreeEntryBean, ContentDownloadBean>
        extends ITextResourceManagementService {

    /**
     * Method to obtain resource metadata.
     *
     * @param path the resource path.
     *
     * @return a bean containing the metadata of this resource or collection.
     * @throws Exception if the operation failed.
     */
    MetadataBean getMetadata(String path) throws Exception;

    /**
     * Method to set a description to this resource or collection.
     *
     * @param path        the resource path.
     * @param description the description to set.
     *
     * @throws Exception if the operation failed.
     */
    void setDescription(String path, String description) throws Exception;

    /**
     * Method to obtain the content of a collection.
     *
     * @param path the collection path.
     *
     * @return bean used to access the content of the given collection.
     * @throws Exception if the operation failed.
     */
    CollectionContentBean getCollectionContent(String path) throws Exception;

    /**
     * Method to obtain the resource data for the given list of resource paths.
     *
     * @param paths the list of resource paths for which we need to obtain resource data.
     *
     * @return the requested resource data.
     *
     * @throws Exception if the operation failed.
     */
    ResourceData[] getResourceData(String[] paths) throws Exception;

    /**
     * Method to obtain a bean that can be used to manage the content of this resource.
     *
     * @param path the resource path.
     *
     * @return a bean that can be used to manage the resource content.
     * @throws Exception if the operation failed.
     */
    ContentBean getContentBean(String path) throws Exception;

    /**
     * Method to add a new collection to the repository.
     *
     * @param parentPath     the parent path (or the path at which we are adding this collection).
     * @param collectionName the name of the collection.
     * @param collectionType the type of the collection. Collections unlike resources do not have
     *                       a concept of media type. However, to create special types of
     *                       collections, we specify a media type which will be used to trigger a
     *                       handler which will create a collection of a specific type (ex:- Axis2
     *                       Repository collection).
     * @param description    the description for the newly added collection.
     *
     * @return the path at which the collection was added.
     * @throws Exception if the operation failed.
     */
    String addCollection(
            String parentPath, String collectionName, String collectionType, String description)
            throws Exception;

    /**
     * Method to create a symbolic link.
     *
     * @param parentPath the parent path (or the path at which we are adding this link). Please note
                         that the parent path should not have trailing slashes.
     * @param name       the name of the symbolic link.
     * @param targetPath the actual resource to which this link points to.
     *
     * @throws Exception if the operation failed.
     */
    void addSymbolicLink(String parentPath,
                         String name,
                         String targetPath) throws Exception;

    /**
     * Method to create a link to a remote resource (a resource on another repository that has been
     * mounted into this repository). All mounted resources will also show-up as remote links.
     *
     * @param parentPath the parent path (or the path at which we are adding this link).
     * @param name       the name of the symbolic link.
     * @param instance   the identifier of the remote instance.
     * @param targetPath the actual resource to which this link points to.
     *
     * @throws Exception if the operation failed.
     */
    void addRemoteLink(String parentPath,
                       String name,
                       String instance,
                       String targetPath) throws Exception;

    /**
     * Method to import a resource (available on a specified remote URL) in to the repository.
     *
     * @param parentPath      the parent path (or the path at which we are adding this resource).
     * @param resourceName    the name of the resource.
     * @param mediaType       the media type of the resource.
     * @param description     the description for the newly added resource.
     * @param fetchURL        the remote URL at which the resource is available for download.
     * @param symlinkLocation the location of the symbolic link to be created. This parameter is
     *                        used when importing WSDL and Schema files, which will optionally
     *                        create a symbolic link that points to the uploaded WSDL or Schema.
     * @param properties      list of properties to be added along with the resource.
     *
     * @return whether the operation was successful or not.
     * @throws Exception if the operation failed due to an unexpected error.
     */
    boolean importResource(
            String parentPath,
            String resourceName,
            String mediaType,
            String description,
            String fetchURL,
            String symlinkLocation,
            String[][] properties) throws Exception;

    /**
     * Method to delete a resource (or collection) at the given path.
     *
     * @param pathToDelete the path of the resource (or collection) to delete.
     *
     * @return whether the operation was successful or not.
     * @throws Exception if the operation failed due to an unexpected error.
     */
    boolean delete(String pathToDelete) throws Exception;

    /**
     * Method to obtain a bean that can be used to manage and manipulate resource permissions.
     *
     * @param path the path of the resource (or collection).
     *
     * @return the permissions bean for the given resource path.
     * @throws Exception if the operation failed.
     */
    PermissionBean getPermissions(String path) throws Exception;

    /**
     * Method to add a role permission to the given resource (or collection) path.
     *
     * @param pathToAuthorize   the resource path for which the permission is added to do.
     * @param roleToAuthorize   the role for which the permission is granted to.
     * @param actionToAuthorize the action that is authorized. The following actions correspond to
     *                          following resource permissions:
     *                          <ul>
     *                          <li>2 - Read Permission</li>
     *                          <li>3 - Write Permission</li>
     *                          <li>4 - Delete Permission</li>
     *                          <li>5 - Authorize Permission</li>
     *                          </ul>
     * @param permissionType    the type of permission to be granted. The following types are
     *                          available:
     *                          <ul>
     *                          <li>0 - Deny permission</li>
     *                          <li>1 - Allow (or grant) permission</li>
     *                          </ul>
     *
     * @return whether the operation was successful or not.
     * @throws Exception if the operation failed due to an unexpected error.
     */
    boolean addRolePermission(
            String pathToAuthorize,
            String roleToAuthorize,
            String actionToAuthorize,
            String permissionType) throws Exception;

    /**
     * Method to change the permissions that have been granted to the given resource.
     *
     * @param resourcePath     the resource path for which the permissions are changed.
     * @param permissionsInput the permission input is a string in the following format:
     *                         <p/>
     *                         &lt;permission-string&gt; :- &lt;role-permissions&gt;
     *                         <p/>
     *                         &lt;role-permissions&gt; :- &lt;role-permission&gt;|&lt;role-permissions&gt;
     *                         <p/>
     *                         &lt;role-permission&gt; :- &lt;role-name&gt;:&lt;permissions&gt;
     *                         <p/>
     *                         &lt;permissions&gt; :- &lt;permission&gt;:&lt;permissions&gt;
     *                         <p/>
     *                         &lt;permission&gt; :- &lt;action&gt;^&lt;type&gt;
     *                         <p/>
     *                         &lt;role-name&gt; :- a name of a valid role on the system
     *                         <p/>
     *                         &lt;action&gt; :- <b>ra</b>, <b>rd</b>, <b>wa</b>, <b>wd</b>,
     *                         <b>da</b>, <b>dd</b>, <b>aa</b> or <b>ad</b>.
     *                         <p/>
     *                         &lt;type&gt; :- <b>true</b> or <b>false</b>
     *                         <p/>
     *                         where the following defines the actions:
     *                         <ul>
     *                         <li>ra - Read allowed</li>
     *                         <li>rd - Read denied</li>
     *                         <li>wa - Write allowed</li>
     *                         <li>wd - Write denied</li>
     *                         <li>da - Delete allowed</li>
     *                         <li>dd - Delete denied</li>
     *                         <li>aa - Authorize allowed</li>
     *                         <li>ad - Authorize denied</li>
     *                         </ul>
     *
     * @return whether the operation was successful or not.
     * @throws Exception if the operation failed due to an unexpected error.
     */
    boolean changeRolePermissions(String resourcePath, String permissionsInput)
            throws Exception;

    /**
     * Method to add a new resource from the filesystem into the repository. This will provide the
     * data handler which was obtained from the file uploader to the repository in the case of the
     * Management Console.
     *
     * @param path            the path to which the resource would be added.
     * @param mediaType       the media type of the resource.
     * @param description     the description for the newly added resource.
     * @param content         the data handler containing the resource's content.
     * @param symlinkLocation the location of the symbolic link to be created. This parameter is
     *                        used when importing WSDL and Schema files, which will optionally
     *                        create a symbolic link that points to the uploaded WSDL or Schema.
     * @param properties      list of properties to be added along with the resource.
     *
     * @return whether the operation was successful or not.
     * @throws Exception if the operation failed due to an unexpected error.
     */
    boolean addResource(String path, String mediaType, String description, DataHandler content,
                        String symlinkLocation, String[][] properties)
            throws Exception;

    /**
     * Method to upload an extension to the registry. These extensions will get uploaded to the
     * extensions directory for the given tenant.
     *
     * @param name    the name of the extension library (which should be a jar).
     * @param content the content of the jar.
     *
     * @return whether the operation was successful or not.
     * @throws Exception if the operation failed due to an unexpected error.
     */
    boolean addExtension(String name, DataHandler content) throws Exception;

    /**
     * This method lists all of the installed extensions for the given tenant.
     *
     * @return list of extensions installed.
     * @throws Exception if the operation failed.
     */
    String[] listExtensions() throws Exception;

    /**
     * Method to remove the named extension from the registry.
     *
     * @param name the name of the extension library (which should be a jar).
     *
     * @return whether the operation was successful or not.
     * @throws Exception if the operation failed due to an unexpected error.
     */
    boolean removeExtension(String name) throws Exception;

    /**
     * Method to rename resource (or collection) on the repository.
     * 
     * @param parentPath      The parent path of the new resource. If this parameter is to be used,
     *                        the new resource name should not start with a '/' character.
     * @param oldResourcePath The complete path of the old resource.
     * @param newResourceName The complete or path relative to parent path of the new resource.
     *
     * @return whether the operation was successful or not.
     * @throws Exception if the operation failed due to an unexpected error.
     */
    boolean renameResource(
            String parentPath, String oldResourcePath, String newResourceName)
            throws Exception;

    /**
     * Method to copy a resource on the repository from one location to another.
     *
     * @param oldResourcePath the path of the existing resource.
     * @param parentPath      the path at which the new resource would be added.
     * @param resourceName    the name of the new resource.
     * @param optional        this parameter is not being used at the moment.
     *
     * @return whether the operation was successful or not.
     * @throws Exception if the operation failed due to an unexpected error.
     */
    boolean copyResource(
            String optional, String oldResourcePath, String parentPath, String resourceName)
            throws Exception;

    /**
     * Method to move a resource on the repository from one location to another.
     *
     * @param oldResourcePath the path of the old resource.
     * @param parentPath      the path at which the new resource would be added.
     * @param resourceName    the name of the new resource.
     * @param optional        this parameter is not being used at the moment.
     *
     * @return whether the operation was successful or not.
     * @throws Exception if the operation failed due to an unexpected error.
     */
    boolean moveResource(
            String optional, String oldResourcePath, String parentPath, String resourceName)
            throws Exception;

    /**
     * Method to obtain the resource path which has been set on the servlet session.
     *
     * @return the resource path saved on the session.
     *
     * @throws Exception if the operation failed.
     */
    String getSessionResourcePath() throws Exception;

    /**
     * Method to set a resource path to the servlet session, to be used later.
     *
     * @param resourcePath the resource path saved on the session.
     * 
     * @throws Exception if the operation failed.
     */
    void setSessionResourcePath(String resourcePath) throws Exception;

    /**
     * Method to obtain a bean for an entry on the resource tree. This method is used to generate
     * the tree view of resources on the Management Console.
     *
     * @param resourcePath the path of the resource.
     *
     * @return a bean containing details of the resource tree entry.
     *
     * @throws Exception if the operation failed.
     */
    ResourceTreeEntryBean getResourceTreeEntry(String resourcePath) throws Exception;

    /**
     * Method to create a new version at the given path.
     *
     * @param resourcePath the path of the resource.
     *
     * @return whether the operation was successful or not.
     * @throws Exception if the operation failed due to an unexpected error.
     */
    boolean createVersion(String resourcePath) throws Exception;

    /**
     * Method to restore a resource to the given version. The resource to be restored will be
     * determined from the version path itself.
     *
     * @param versionPath the path of the resource along with the version number to be restored to.
     *
     * @return whether the operation was successful or not.
     * @throws Exception if the operation failed due to an unexpected error.
     */
    boolean restoreVersion(String versionPath) throws Exception;

    /**
     * Method to obtain a list of versions of the given resource.
     *
     * @param path the resource path.
     *
     * @return a bean containing the versions of the resource (or collection) at the given path.
     *
     * @throws Exception if the operation failed.
     */
    VersionsBean getVersionsBean(String path) throws Exception;

    /**
     * Method to obtain a list of media types for resources stored on the repository. These media
     * types will initially be populated from the mime.types file.
     *
     * @return the list of media types for resources. This will have a format as follows:
     *         <p/>
     *         &lt;media-types&gt; :- &lt;media-type-entry&gt;,&lt;media-type&gt;
     *         <p/>
     *         &lt;media-type-entry&gt; :- &lt;extension&gt;:&lt;mime-type&gt;
     * @throws Exception if the operation failed.
     */
    String getMediatypeDefinitions() throws Exception;

    /**
     * Method to obtain a list of media types against which handlers that are responsible for
     * creating various collection types are registered.
     *
     * @return the list of media types for collections. This will have a format as follows:
     *         <p/>
     *         &lt;media-types&gt; :- &lt;media-type-entry&gt;,&lt;media-type&gt;
     *         <p/>
     *         &lt;media-type-entry&gt; :- &lt;collection-type&gt;:&lt;mime-type&gt;
     * @throws Exception if the operation failed.
     */
    String getCollectionMediatypeDefinitions() throws Exception;

    /**
     * Method to obtain a list of media types against which custom user interfaces that are
     * which will show up on the management console in place of the standard user interfaces for
     * resources are registered
     *
     * @return the list of media types for custom user interfaces. This will have a format as
     *         follows:
     *         <p/>
     *         &lt;media-types&gt; :- &lt;media-type-entry&gt;,&lt;media-type&gt;
     *         <p/>
     *         &lt;media-type-entry&gt; :- &lt;custom-ui-name&gt;:&lt;mime-type&gt;
     * @throws Exception if the operation failed.
     */
    String getCustomUIMediatypeDefinitions() throws Exception;

    /**
     * Method to obtain a named property of the given resource.
     *
     * @param resourcePath the path of the resource (or collection).
     * @param key          the property key.
     *
     * @return the property value. In the case of a multi-valued property the first property value
     *         will be returned.
     * @throws Exception if the operation failed.
     */
    String getProperty(String resourcePath, String key) throws Exception;

    /**
     * Method to obtain a bean from with the content of the given resource can be downloaded.
     *
     * @param path the resource path.
     *
     * @return the bean having the resource content as a data handler.
     *
     * @throws Exception if the operation failed.
     */
    ContentDownloadBean getContentDownloadBean(String path) throws Exception;

    /**
     * Method to obtain human readable Media Types.
     *
     * @return the string media type
     *
     * @throws Exception if the operation failed.
     */
    String getHumanReadableMediaTypes() throws Exception;

    /**
     * Method to obtain mime types for the corresponding Media Type.
     *
     * @return the string mime type.
     *
     * @throws Exception if the operation failed.
     */
    String getMimeTypeFromHuman(String mediaType) throws Exception;
}
