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
package org.wso2.carbon.registry.ws.api;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.common.services.RegistryAbstractAdmin;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.ws.api.utils.CommonUtil;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@SuppressWarnings({"unused", "deprecation"})
public class WSRegistry extends RegistryAbstractAdmin implements Registry {

	private Log log = LogFactory.getLog(WSRegistry.class);
    private static final String REGISTRY_WS_API_TENANT = "registry.ws.api.tenant";

	private String workingDir = System.getProperty(ServerConstants.WORK_DIR);
	private File tempFile;

	/**
	 * Constructs a new Registry WS API instance. Only to be used by a web service client. This is used by
	 * the Registry Web Service client.
	 * @throws RegistryException
	 */
	public WSRegistry() throws RegistryException {
		try {
			tempFile = File.createTempFile("wsresource", ".tmp");
			tempFile.deleteOnExit();
		} catch (IOException e) {
			throw new RegistryException("Not able to create temp files. Check permissions", e);
		}
	}
    
    private Registry getRegistryForTenant() {
        Registry registry = getRootRegistry();
        HttpSession httpSession = ((HttpServletRequest) MessageContext.getCurrentMessageContext().getProperty(
                    HTTPConstants.MC_HTTP_SERVLETREQUEST)).getSession();
        RegistryService registryService = CommonUtil.getRegistryService();
        if (httpSession != null && registryService != null) {
            if (PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId() ==
                    MultitenantConstants.SUPER_TENANT_ID) {
                try {
                    Object tenantId = httpSession.getAttribute(REGISTRY_WS_API_TENANT);
                    if (tenantId != null) {
                        return registryService.getRegistry(
                                CarbonConstants.REGISTRY_SYSTEM_USERNAME, (Integer) tenantId);
                    }
                } catch (Exception ignored) {
                    // We are not bothered about any errors in here.
                }
            }
        }
        return registry;
    }

	public Resource newResource() throws RegistryException{
		return getRegistryForTenant().newResource();
	}
	
	public Collection newCollection() throws RegistryException{
		return getRegistryForTenant().newCollection();
	}
	
	/**
	 * Returns a new WSResource instance. This method is not used by the Registry Web Service client to avoid an unnecessary network call. It
	 * is only there for completeness of the Registry interface. A new Resource instance can be created directly from the client side.
	 * @return WSResource instance
	 * @throws RegistryException
	 */
	public WSResource WSnewResource()throws RegistryException{
		Resource resource = getRegistryForTenant().newResource();
		return CommonUtil.newResourcetoWSResource(resource);

	}
	
	/**
	 * Returns a new WSCollection instance. This method is not used by the Registry Web Service client to avoid an unnecessary network call. It
	 * is only there for completeness of the Registry interface. A new Collection instance can be created directly from the client side.
	 * 
	 * @return WSResource instance
	 * @throws RegistryException
	 */
	public WSCollection WSnewCollection()throws RegistryException{
		Collection collection = getRegistryForTenant().newCollection();
		return CommonUtil.newCollectiontoWSCollection(collection);
	}
	
	public Resource get(String path) throws RegistryException{
		return getRegistryForTenant().get(path);
	}
	
	/**
	 * Returns the resource at the given path.
	 *
	 * @param path Path of the resource. e.g. /project1/server/deployment.xml
	 * @return WSResource instance
	 * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
	 *          is thrown if the resource is not in the registry
	 */
	public WSResource WSget(String path) throws RegistryException {
		Resource resource = getRegistryForTenant().get(path);
		DataHandler dataHandler = null;
		if (resource instanceof Collection) {
			return CommonUtil.transformCollectiontoWSCollection((Collection) resource, dataHandler);
		}

		return CommonUtil.transformResourceToWSResource(resource, dataHandler);
	}

	public Resource getMetaData(String path) throws RegistryException{
		return getRegistryForTenant().getMetaData(path);
	}

    public String importResource(String s, String s1,
                                 org.wso2.carbon.registry.api.Resource resource)
            throws org.wso2.carbon.registry.api.RegistryException {
        return importResource(s, s1, (Resource) resource);
    }


    /**
	 * Returns the meta data of the resource at the given path.
	 *
	 * @param path Path of the resource. e.g. /project1/server/deployment.xml
	 * @return WSResource instance
	 * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
	 *          is thrown if the resource is not in the registry
	 */
	
	public WSResource WSgetMetaData(String path) throws RegistryException{
		Resource resource = getRegistryForTenant().getMetaData(path);
		DataHandler dataHandler = null;
		try {
			dataHandler = CommonUtil.makeDataHandler(resource, tempFile);
		} catch (IOException e) {
			log.error("WSGet failed - Unable to generate temp file", e);
		}
		return CommonUtil.transformResourceToWSResource(resource, dataHandler);
	}


	public Collection get(String path, int start, int pageSize) throws RegistryException{
		return getRegistryForTenant().get(path,start,pageSize);
	}

	public Collection getChildCollection(String path, int start, int pageSize) throws RegistryException{
		return getRegistryForTenant().get(path,start,pageSize);
	}
	
	/**
	 * Returns the WSCollection according to the Collection at the given path, with the content paginated according to
	 * the arguments.
	 *
	 * @param path the path of the collection.  MUST point to a collection!
	 * @param start the initial index of the child to return.  If there are fewer children than
	 *              the specified value, a RegistryException will be thrown.
	 * @param pageSize the maximum number of results to return
	 * @return a Collection containing the specified results in the content
	 * @throws RegistryException if the resource is not found, or if the path does not
	 *                           reference a Collection, or if the start index is greater than
	 *                           the number of children.
	 */
	public WSCollection WSgetWithPageSize(String path, int start, int pageSize) throws RegistryException{
		Collection collection = getRegistryForTenant().get(path,start,pageSize);
		DataHandler dataHandler = null;
//		try {
//			dataHandler = CommonUtil.makeDataHandler(collection, tempFile);
//		} catch (IOException e) {
//			log.error("WSGet failed - Unable to generate temp file", e);
//		}
		return CommonUtil.transformCollectiontoWSCollection(collection, dataHandler);
	}

	public WSCollection WSgetChildCollection(String path, int start, int pageSize) throws RegistryException{
		Collection collection = getRegistryForTenant().get(path,start,pageSize);
		DataHandler dataHandler = null;
//		try {
//			dataHandler = CommonUtil.makeDataHandler(collection, tempFile);
//		} catch (IOException e) {
//			log.error("WSGet failed - Unable to generate temp file", e);
//		}
		return CommonUtil.transformCollectiontoWSCollection(collection, dataHandler);
	}

	/**
	 * Check whether a resource exists at the given path
	 *
	 * @param path Path of the resource to be checked
	 * @return true if a resource exists at the given path, false otherwise.
	 * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
	 *          if an error occurs
	 */
	public boolean resourceExists(String path) throws RegistryException{
		return getRegistryForTenant().resourceExists(path);

	}

    public String put(String s, org.wso2.carbon.registry.api.Resource resource)
            throws org.wso2.carbon.registry.api.RegistryException {
        return put(s, (Resource) resource);
    }

    public String put(String suggestedPath, Resource resource) throws RegistryException{
		return getRegistryForTenant().put(suggestedPath,resource);
	}

	/**
	 * Adds or updates resources in the registry. If there is no resource at the given path,
	 * resource is added. If a resource already exist at the given path, it will be replaced with
	 * the new resource.
	 *
	 * @param suggestedPath the path which we'd like to use for the new resource.
	 * @param wsResource WSResource instance for the new resource. This will be converted to a Resource instance internally.
	 * @return the actual path that the server chose to use for our Resource
	 * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     * @throws java.io.IOException
     */
	public String WSput(String suggestedPath, WSResource wsResource) throws RegistryException, IOException{

		Object content = CommonUtil.makeBytesFromDataHandler(wsResource);
		Resource resource;
		if (wsResource.isCollection()) {
			resource = CommonUtil.transformWSCollectiontoCollection( wsResource, content);
		}
		else {
			resource = CommonUtil.transformWSResourcetoResource(wsResource, content);
		}
		return getRegistryForTenant().put(suggestedPath,resource);
	}

	/**
	 * Deletes the resource at the given path. If the path refers to a directory, all child
	 * resources of the directory will also be deleted.
	 *
	 * @param path Path of the resource to be deleted.
	 * @throws RegistryException is thrown depending on the implementation.
	 */
	public void delete(String path) throws RegistryException{
		getRegistryForTenant().delete(path);
	}
	
	public String importResource(String suggestedPath,
	                             String sourceURL,
	                             Resource metadata) throws RegistryException {
		return getRegistryForTenant().importResource(suggestedPath,sourceURL,metadata);
	}
	
	/**
	 * Creates a resource by fetching the resource content from the given URL.
	 *
	 * @param suggestedPath path where we'd like to add the new resource. Although this path is
	 *                      specified by the caller of the method, resource may not be actually
	 *                      added at this path.
	 * @param sourceURL     where to fetch the resource content
	 * @param metadata      a WSResource instance containing meta data parameters
	 * @return actual path to the new resource
	 * @throws RegistryException if we couldn't get or store the new resource
	 */
	public String WSimportResource(String suggestedPath,
	                               String sourceURL,
	                               WSResource metadata) throws RegistryException {
		return getRegistryForTenant().importResource(suggestedPath, sourceURL, CommonUtil.transformWSResourcetoResource(metadata, null));
	}
	
	/**
	 * Move or rename a resource in the registry.  This is equivalent to 1) delete the resource,
	 * then 2) add the resource to the new location.  The operation is atomic, so if it fails the
	 * old resource will still be there.
	 *
	 * @param currentPath current path of the resource
	 * @param newName     where we'd like to move the reosurce
	 * @return the actual path for the new resource
	 * @throws RegistryException if something went wrong
	 */
	public String rename(String currentPath, String newName) throws RegistryException{
		return getRegistryForTenant().rename(currentPath, newName);

	}

	/**
	 * Move or rename a resource in the registry.  This is equivalent to 1) delete the resource,
	 * then 2) add the resource to the new location.  The operation is atomic, so if it fails the
	 * old resource will still be there.
	 *
	 * @param currentPath current path of the resource
	 * @param newPath     where we'd like to move the reosurce
	 * @return the actual path for the new resource
	 * @throws RegistryException if something went wrong
	 */
	public String move(String currentPath, String newPath) throws RegistryException{
		return getRegistryForTenant().move(currentPath,newPath);
	}

	/**
     * Copy a resource in the registry.  The operation is atomic, so if the resource was a
     * collection, all children and the collection would be copied in a single-go.
     *
     * @param sourcePath current path of the resource
     * @param targetPath where we'd like to copy the resource
     *
     * @return the actual path for the new resource
     * @throws RegistryException if something went wrong
     */
	public String copy(String sourcePath, String targetPath) throws RegistryException{
		return getRegistryForTenant().copy(sourcePath,targetPath);
	}

    /**
     * Creates a new version of the resource.
     *
     * @param path the resource path.
     *
     * @throws RegistryException if something went wrong.
     */
	public void createVersion(String path) throws RegistryException{
		getRegistryForTenant().createVersion(path);
	}
	
	/**
	 * Get a list of all versions of the resource located at the given path. Version paths are
	 * returned in the form /projects/myresource?v=12
	 *
	 * @param path path of a current version of a resource
	 * @return a String array containing the individual paths of versions
	 * @throws RegistryException if there is an error
	 */
	public String[] getVersions(String path) throws RegistryException{
		return getRegistryForTenant().getVersions(path);
	}

	/**
	 * Reverts a resource to a given version.
	 *
	 * @param versionPath path of the version to be reverted. It is not necessary to provide the
	 *                    path of the resource as it can be derived from the version path.
	 * @throws RegistryException if there is an error
	 */
	public void restoreVersion(String versionPath) throws RegistryException{
		getRegistryForTenant().restoreVersion(versionPath);
	}


	////////////////////////////////////////////////////////
	// Associations
	////////////////////////////////////////////////////////
	/**
	 * Adds an association stating that the resource at "associationPath" associate on the resource at
	 * "associationPath". Paths may be the resource paths of the current versions or paths of the old
	 * versions. If a path refers to the current version, it should contain the path in the form
	 * /c1/c2/r1. If it refers to an old version, it should be in the form /c1/c2/r1?v=2.
	 *
	 * @param sourcePath       Path of the source resource
	 * @param targetPath       Path of the target resource
	 * @param associationType  Type of the association
	 * @throws RegistryException Depends on the implementation
	 */
	public void addAssociation(String sourcePath,
	                           String targetPath,
	                           String associationType) throws RegistryException{
		getRegistryForTenant().addAssociation(sourcePath,targetPath,associationType);
	}
	
	/**
	 * To remove an association for a given resource
	 *
	 * @param sourcePath       Path of the source resource
	 * @param targetPath       Path of the target resource
	 * @param associationType  Type of the association
	 * @throws RegistryException Depends on the implementation
	 */
	public void removeAssociation(String sourcePath,
	                              String targetPath,
	                              String associationType) throws RegistryException{
		getRegistryForTenant().removeAssociation(sourcePath,targetPath,associationType);
	}
	
	/**
	 * Get all associations of the given resource. This is a chain of association starting from the
	 * given resource both upwards (source to destination) and downwards (destination to source). T
	 * his is useful to analyse how changes to other resources would affect the
	 * given resource.
	 *
	 * @param resourcePath Path of the resource to analyse associations.
	 * @return List of Association
	 * @throws RegistryException : If something went wrong
	 */
	public Association[] getAllAssociations(String resourcePath) throws RegistryException{
		return getRegistryForTenant().getAllAssociations(resourcePath);
	}

    /**
     * This is the web service friendly version of getAllAssociations(String resourcePath) method
     * @param resoucePath  path of resource
     * @return WSAssociation
     * @throws RegistryException
     */
    public WSAssociation[] WSgetAllAssociations(String resoucePath) throws RegistryException {
        Association[] associations = getRegistryForTenant().getAllAssociations(resoucePath);
        WSAssociation[] wsAssociations = new WSAssociation[associations.length];
        for (int i = 0; i < wsAssociations.length; i++) {
            wsAssociations[i] = CommonUtil.transformAssociationToWSAssociation(associations[i]);
        }
        return wsAssociations;
    }


	/**
	 * Get all associations of the given resource for a give association type.
	 * This is a chain of association starting from the
	 * given resource both upwards (source to destination) and downwards (destination to source). T
	 * his is useful to analyse how changes to other resources would affect the
	 * given resource.
	 *
	 * @param resourcePath    Path of the resource to analyse associations.
	 * @param associationType : Type of the association , that could be dependecy , or some other type
	 * @return List of Association
	 * @throws RegistryException : If something went wrong
	 */
	public Association[] getAssociations(String resourcePath, String associationType)
	throws RegistryException {
		return getRegistryForTenant().getAssociations(resourcePath, associationType);
	}

    /**
     * Get all associations of the given resource for a give association type.
     * This is a chain of association starting from the
     * given resource both upwards (source to destination) and downwards (destination to source). T
     * his is useful to analyse how changes to other resources would affect the
     * given resource.
     *
     * @param resourcePath    Path of the resource to analyse associations.
     * @param associationType : Type of the association , that could be dependecy , or some other type
     * @return List of Association
     * @throws RegistryException : If something went wrong
     */
    public WSAssociation[] WSgetAssociations(String resourcePath, String associationType)
            throws RegistryException {
		Association[] associations = getRegistryForTenant().getAssociations(resourcePath, associationType);
        WSAssociation[] wsAssociations = new WSAssociation[associations.length];
        for (int i = 0; i < wsAssociations.length; i++) {
            wsAssociations[i] = CommonUtil.transformAssociationToWSAssociation(associations[i]);
        }
        return wsAssociations;
	}


	////////////////////////////////////////////////////////
	// Tagging
	////////////////////////////////////////////////////////

	/**
	 * Applies the given tag to the resource in the given path. If the given tag is not defined in
	 * the registry, it will be defined.
	 *
	 * @param resourcePath Path of the resource to be tagged.
	 * @param tag          Tag. Any string can be used for the tag.
	 * @throws RegistryException is thrown if a resource does not exist in the given path.
	 */
	public void applyTag(String resourcePath, String tag) throws RegistryException{
		getRegistryForTenant().applyTag(resourcePath,tag);
	}

	public TaggedResourcePath[] getResourcePathsWithTag(String tag) throws RegistryException{
		return getRegistryForTenant().getResourcePathsWithTag(tag);
	}
	
	/**
	 * Returns the paths of all Resources that are tagged with the given tag.
	 *
	 * @param tag the tag to search for
	 * @return an array of WSTaggedResourcePaths
	 * @throws RegistryException if an error occurs
	 */
	public WSTaggedResourcePath[] WSgetResourcePathsWithTag(String tag)throws RegistryException{
		return CommonUtil.exchangeTaggedResourcepath(getRegistryForTenant().getResourcePathsWithTag(tag));
	}

	/**
	 * Returns all tags used for tagging the given resource.
	 *
	 * @param resourcePath Path of the resource
	 * @return Tags tag names
	 * @throws RegistryException is thrown if a resource does not exist in the given path.
	 */
	public Tag[] getTags(String resourcePath) throws RegistryException{
		return getRegistryForTenant().getTags(resourcePath);
	}

    /**
     * Returns all tags used for tagging the given resource.
     *
     * @param resourcePath Path of the resource
     * @return Tags tag names
     * @throws RegistryException is thrown if a resource does not exist in the given path.
     */
    public WSTag[] WSgetTags(String resourcePath) throws RegistryException {
        Tag[] tags = getRegistryForTenant().getTags(resourcePath);
        WSTag[] wsTags = new WSTag[tags.length];
        for (int i = 0; i < wsTags.length; i++) {
            wsTags[i] = CommonUtil.transformTagToWSTag(tags[i]);
        }
        return wsTags;
    }

	/**
	 * Removes a tag on a resource. If the resource at the path is owned by the current user, all
	 * taggings done using the given tag will be removed. If the resource is not owned by the
	 * current user, only the tagging done by the current user will be removed.
	 *
	 * @param path Resource path tagged with the given tag.
	 * @param tag  Name of the tag to be removed.
	 * @throws RegistryException if there's a problem
	 */
	public void removeTag(String path, String tag) throws RegistryException{
		getRegistryForTenant().removeTag(path,tag);
	}

    public String addComment(String s, org.wso2.carbon.registry.api.Comment comment)
            throws org.wso2.carbon.registry.api.RegistryException {
        return addComment(s, (Comment) comment);
    }

    ////////////////////////////////////////////////////////
	// Comments
	////////////////////////////////////////////////////////

	public String addComment(String resourcePath, Comment comment) throws RegistryException{
		return getRegistryForTenant().addComment(resourcePath,comment);
	}
	
	/**
	 * Adds a comment to a resource.
	 *
	 * @param resourcePath Path of the resource to add the comment.
	 * @param comment      WSComment instance for the new comment. This will be converted to a Comment instance internally.
	 * @return the path of the new comment.
	 * @throws RegistryException is thrown if a resource does not exist in the given path.
	 */
	public String WSaddComment(String resourcePath, WSComment comment) throws RegistryException{
		return getRegistryForTenant().addComment(resourcePath,CommonUtil.WSCommenttoRegistryComment(comment));
	}

	/**
	 * Change the text of an existing comment.
	 *
	 * @param commentPath path to comment resource ("..foo/r1;comment:1")
	 * @param text        new text for the comment.
	 * @throws RegistryException Registry implementations may handle exceptions and throw
	 *                           RegistryException if the exception has to be propagated to the
	 *                           client.
	 */
	public void editComment(String commentPath, String text) throws RegistryException{
		getRegistryForTenant().editComment(commentPath,text);
	}

	public Comment[] getComments(String resourcePath) throws RegistryException{
		return getRegistryForTenant().getComments(resourcePath);
	}
	
	/**
	 * Get all comments for the given resource.
	 *
	 * @param resourcePath path of the resource.
	 * @return an array of WSComment objects.
	 * @throws RegistryException Registry implementations may handle exceptions and throw
	 *                           RegistryException if the exception has to be propagated to the
	 *                           client.
	 */
	public WSComment[] WSgetComments(String resourcePath) throws RegistryException{
		Comment[] rcomment = getRegistryForTenant().getComments(resourcePath);
		WSComment[] wscomment = new WSComment[rcomment.length];
		for(int i=0;i<rcomment.length;i++){
			wscomment[i] = new WSComment();
			wscomment[i] = CommonUtil.RegistryCommenttoWSComment(rcomment[i]);
		}
		return wscomment;
	}

	/**
	 * Get the comment for the given comment path.
	 *
	 * @param commentPath path of the resource.
	 * @return a WSComment.
	 * @throws RegistryException Registry implementations may handle exceptions and throw
	 *                           RegistryException if the exception has to be propagated to the
	 *                           client.
	 */
	public WSComment WSgetSingleComment(String commentPath) throws RegistryException {
		Comment comment = (Comment) getRegistryForTenant().get(commentPath);
		return CommonUtil.RegistryCommenttoWSComment(comment);
	}

	////////////////////////////////////////////////////////
	// Ratings
	////////////////////////////////////////////////////////

	/**
	 * Rate the given resource.
	 *
	 * @param resourcePath Path of the resource.
	 * @param rating       Rating value between 1 and 5.
	 * @throws RegistryException Registry implementations may handle exceptions and throw
	 *                           RegistryException if the exception has to be propagated to the
	 *                           client.
	 */
	public void rateResource(String resourcePath, int rating) throws RegistryException{
		getRegistryForTenant().rateResource(resourcePath,rating);
	}

	/**
	 * Returns the average rating for the given resource. This is the average of all ratings done by
	 * all users for the given resource.
	 *
	 * @param resourcePath Path of the resource.
	 * @return Average rating between 1 and 5.
	 * @throws RegistryException if an error occurs
	 */
	public float getAverageRating(String resourcePath) throws RegistryException{
		return getRegistryForTenant().getAverageRating(resourcePath);
	}

	/**
	 * Returns the rating given to the specified resource by the given user
	 *
	 * @param path     Path of the resource
	 * @param userName username of the user
	 * @return rating given by the given user
	 * @throws RegistryException if there is a problem
	 */
	public int getRating(String path, String userName) throws RegistryException{
		return getRegistryForTenant().getRating(path,userName);
	}

	public Collection executeQuery(String path, Map parameters) throws RegistryException{
		return getRegistryForTenant().executeQuery(path,parameters);
	}

	/**
	 * Executes a custom query which lives at the given path in the Registry.
	 *
	 * @param path Path of the query to execute.
	 * @param key an array of String containing key parameters. These should correspond with the value parameters (key -> value)
	 * @param value an array of String containing value parameters to the corresponding key parameters (key -> value)
	 * @return a Collection containing any resource paths which match the query
	 * @throws RegistryException depends on the implementation.
	 */
	public WSCollection WSexecuteQuery(String path, String[] key,String[] value) throws RegistryException{
		Collection collection = getRegistryForTenant().executeQuery(path, CommonUtil.createMap(key,value));
		DataHandler dataHandler = null;
//		try {
//			dataHandler = CommonUtil.makeDataHandler(collection, tempFile);
//		} catch (IOException e) {
//			log.error("WSGet failed - Unable to generate temp file", e);
//		}
        WSCollection wsCollection =
                CommonUtil.transformCollectiontoWSCollection(collection, dataHandler);
        wsCollection.setChildCount(collection.getChildCount());
        wsCollection.setChildren(collection.getChildren());
        return wsCollection;
	}

	/**
	 * Returns the logs of the activities occurred in the Registry.
	 *
	 * @see LogEntry Accepted values for action parameter
	 *
	 * @param resourcePath If given, only the logs related to the resource path will be returned. If
	 *                     null, logs for all resources will be returned.
	 * @param action       Only the logs pertaining to this action will be returned.  For
	 *                     acceptable values, see LogEntry.
	 * @param userName     If given, only the logs for activities done by the given user will be
	 *                     returned. If null, logs for all users will be returned.
	 * @param from         If given, logs for activities occurred after the given date will be
	 *                     returned. If null, there will not be a bound for the starting date.
	 * @param to           If given, logs for activities occurred before the given date will be
	 *                     returned. If null, there will not be a bound for the ending date.
	 * @param recentFirst  If true, returned activities will be most-recent first. If false,
	 *                     returned activities will be oldest first.
	 * @return Array of LogEntry objects representing the logs
	 * @throws RegistryException if there is a problem
	 */
	public LogEntry[] getLogs(String resourcePath,
	                          int action,
	                          String userName,
	                          Date from,
	                          Date to,
	                          boolean recentFirst) throws RegistryException{
		return getRegistryForTenant().getLogs(resourcePath,action,userName,from,to,recentFirst);
	}

    /**
     * Returns the logs of the activities occurred in the Registry.
     *
     * @see LogEntry Accepted values for action parameter
     *
     * @param resourcePath If given, only the logs related to the resource path will be returned. If
     *                     null, logs for all resources will be returned.
     * @param action       Only the logs pertaining to this action will be returned.  For
     *                     acceptable values, see LogEntry.
     * @param userName     If given, only the logs for activities done by the given user will be
     *                     returned. If null, logs for all users will be returned.
     * @param from         If given, logs for activities occurred after the given date will be
     *                     returned. If null, there will not be a bound for the starting date.
     * @param to           If given, logs for activities occurred before the given date will be
     *                     returned. If null, there will not be a bound for the ending date.
     * @param recentFirst  If true, returned activities will be most-recent first. If false,
     *                     returned activities will be oldest first.
     * @return Array of LogEntry objects representing the logs
     * @throws RegistryException if there is a problem
     */
    public WSLogEntry[] WSgetLogs(String resourcePath,
	                          int action,
	                          String userName,
	                          Date from,
	                          Date to,
	                          boolean recentFirst) throws RegistryException{
		LogEntry[] entries =
                getRegistryForTenant().getLogs(resourcePath,action,userName,from,to,recentFirst);
        WSLogEntry[] wsEntries = new WSLogEntry[entries.length];
        for (int i = 0; i < wsEntries.length; i++) {
            wsEntries[i] = CommonUtil.transformLogEntryToWSLogEntry(entries[i]);
        }
        return wsEntries;
	}

	/**
	 * This method is not supported in WS-API
	 */
    public LogEntryCollection getLogCollection(String resourcePath,
	                                           int action,
	                                           String userName,
	                                           Date from,
	                                           Date to,
	                                           boolean recentFirst) throws RegistryException{
		return getRegistryForTenant().getLogCollection(resourcePath,action,userName,from,to,recentFirst);
	}

	/**
	 * Get a list of the available Aspects for this Registry
	 * @return a String array containing available Aspect names
	 */
	public String [] getAvailableAspects(){
		return getRegistryForTenant().getAvailableAspects();
	}

	/**
	 * Associate an Aspect with a resource.
	 *
	 * @param resourcePath Path of the resource
	 * @param aspect       Name of the aspect
	 * @throws RegistryException : If some thing went wrong while doing associating the phase
	 */
	public void associateAspect(String resourcePath, String aspect) throws RegistryException{
		getRegistryForTenant().associateAspect(resourcePath,aspect);
	}

    /**
	 * This invokes an action on a specified Aspect, which must be associated with the Resource
	 * at the given path.
	 *
	 * @param resourcePath Path of the resource
	 * @param aspectName   Name of the aspect
	 * @param action       Which action was selected - actions are aspect-specific
	 * @throws RegistryException if the Aspect isn't associated with the Resource, or the action
	 *                           isn't valid, or an Aspect-specific problem occurs.
	 */
	public void invokeAspectNoParam(String resourcePath, String aspectName, String action)
	throws RegistryException{
		invokeAspect(resourcePath,aspectName,action);
	}

	/**
	 * This invokes an action on a specified Aspect, which must be associated with the Resource
	 * at the given path.
	 *
	 * @param resourcePath Path of the resource
	 * @param aspectName   Name of the aspect
	 * @param action       Which action was selected - actions are aspect-specific
	 * @throws RegistryException if the Aspect isn't associated with the Resource, or the action
	 *                           isn't valid, or an Aspect-specific problem occurs.
	 */
	public void invokeAspect(String resourcePath, String aspectName, String action)
	throws RegistryException{
		getRegistryForTenant().invokeAspect(resourcePath,aspectName,action);
	}

    /**
	 * This invokes an action on a specified Aspect, which must be associated with the Resource
	 * at the given path.
	 *
	 * @param resourcePath Path of the resource
	 * @param aspectName   Name of the aspect
	 * @param action       Which action was selected - actions are aspect-specific
     * @param keys         Parameters keys to be used for the operation
     * @param values       Parameters values to be used for the operation
	 * @throws RegistryException if the Aspect isn't associated with the Resource, or the action
	 *                           isn't valid, or an Aspect-specific problem occurs.
	 */
	public void invokeAspectWithParam(String resourcePath, String aspectName, String action,
                             String[] keys, String[] values)
	throws RegistryException{
        Map<String, String> parameters = new HashMap<String, String>();
        for (int i = 0; i < keys.length; i++) {
            parameters.put(keys[i], values[i]);
        }
		invokeAspect(resourcePath, aspectName, action, parameters);
	}

	/**
	 * This invokes an action on a specified Aspect, which must be associated with the Resource
	 * at the given path.
	 *
	 * @param resourcePath Path of the resource
	 * @param aspectName   Name of the aspect
	 * @param action       Which action was selected - actions are aspect-specific
     * @param parameters   Parameters to be used for the operation
	 * @throws RegistryException if the Aspect isn't associated with the Resource, or the action
	 *                           isn't valid, or an Aspect-specific problem occurs.
	 */
	public void invokeAspect(String resourcePath, String aspectName, String action,
                             Map<String, String> parameters)
	throws RegistryException{
		getRegistryForTenant().invokeAspect(resourcePath,aspectName,action, parameters);
	}

	/**
	 * Obtain a list of the available actions on a given resource for a given Aspect.  The
	 * Aspect must be associated with the Resource (@see associateAspect).  The actions are
	 * determined by asking the Aspect itself, so they may change depending on the state of the
	 * Resource, the user who's asking, etc)
	 *
	 * @param resourcePath path of the Resource
	 * @param aspectName   name of the Aspect to query for available actions
	 * @return a String[] of action names
	 * @throws RegistryException if the Aspect isn't associated or an Aspect-specific problem occurs
	 */
	public String[] getAspectActions(String resourcePath, String aspectName)
	throws RegistryException{
		return getRegistryForTenant().getAspectActions(resourcePath,aspectName);
	}

	public void beginTransaction() throws RegistryException{
		getRegistryForTenant().beginTransaction();
	}

	public void commitTransaction() throws RegistryException{
		getRegistryForTenant().commitTransaction();
	}

	public void rollbackTransaction() throws RegistryException{
		getRegistryForTenant().rollbackTransaction();
	}

	public RegistryContext getRegistryContext(){
		return getRegistryForTenant().getRegistryContext();
	}

	public Collection searchContent(String keywords) throws RegistryException{
		throw new UnsupportedOperationException("Content search operation not supported");
	}
	
	/**
	 * Search the content of resources
	 *
	 * @param keywords keywords to look for
	 * @return the result set as a WSCollection
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
	 */
	public WSCollection WSsearchContent(String keywords) throws RegistryException{
		Collection collection = getRegistryForTenant().searchContent(keywords);
		DataHandler dataHandler = null;
//		try {
//			dataHandler = CommonUtil.makeDataHandler(collection, tempFile);
//		} catch (IOException e) {
//			log.error("WSGet failed - Unable to generate temp file", e);
//		}
		WSCollection wsCollection =
                CommonUtil.transformCollectiontoWSCollection(collection, dataHandler);
        wsCollection.setChildCount(collection.getChildCount());
        wsCollection.setChildren(collection.getChildren());
        return wsCollection;
	}
	
	/**
	 * Create a symbolic link or mount a registry
	 *
	 * @param path the mount path
	 * @param target the point to be mounted
	 */
	public void createLink(String path, String target) throws RegistryException{
		getRegistryForTenant().createLink(path, target);
	}


	public void createLink(String path, String target, String subTargetPath)
	throws RegistryException{
		getRegistryForTenant().createLink(path, target, subTargetPath);
	}

	/**
	 * Create a symbolic link or mount a registy
	 *
	 * @param path the mount path
	 * @param target the point to be mounted
	 * @param subTargetPath sub path in the remote instance to be mounted
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
	 */
	public void createLinkWithSubTarget(String path, String target, String subTargetPath)
	throws RegistryException{
		if (subTargetPath == null) {
			getRegistryForTenant().createLink(path, target);
		}
		getRegistryForTenant().createLink(path, target, subTargetPath);
	}

	/**
	 * Remove a symbolic link or mount point created
	 *
	 * @param path the mount path
	 */
	public void removeLink(String path) throws RegistryException{
		getRegistryForTenant().removeLink(path);
	}

	public void restore(String path, Reader reader) throws RegistryException {
		getRegistryForTenant().restore(path, reader);
	}

    public void wsRestore(String path, DataHandler dataHandler) throws RegistryException {

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            dataHandler.writeTo(output);
        } catch (IOException e) {
            String msg = "Failed to read the input";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }

        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        restore(path, reader);
    }

	public void dump(String path, Writer writer) throws RegistryException {
		getRegistryForTenant().dump(path, writer);
	}

    public DataHandler wsDump(String path) throws RegistryException {
        DataHandler dataHandler = null;
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
            Writer writer = new OutputStreamWriter(fileOutputStream);
            dump(path, writer);
            writer.flush();
            fileOutputStream.close();
            return new DataHandler(new FileDataSource(tempFile));

        } catch (IOException e) {
            String msg = "get Content failed - Unable to generate temp file";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }

	/**
     * Gets the URL of the WS-Eventing Service.
     *
     * @param path the path to which the WS-Eventing Service URL is required
     *
     * @return the URL of the WS-Eventing Service
     * @throws RegistryException throws if the operation fail
     */
	public String getEventingServiceURL(String path) throws RegistryException {
		return getRegistryForTenant().getEventingServiceURL(path);
	}

	 /**
     * Sets the URL of the WS-Eventing Service.
     *
     * @param path               the path to which the WS-Eventing Service URL is associated
     * @param eventingServiceURL the URL of the WS-Eventing Service
     *
     * @throws RegistryException throws if the operation fail
     */	
	public void setEventingServiceURL(String path, String eventingServiceURL) throws RegistryException {
		getRegistryForTenant().setEventingServiceURL(path, eventingServiceURL);
	}
	
    /**
     * Remove the given aspect from registry context.
     *
     * @param name the name of the aspect to be removed
     *
     * @return return true if the operation finished successful, false otherwise.
     * @throws RegistryException throws if the operation fail
     */
	public boolean removeAspect(String name)throws RegistryException{
		return getRegistryForTenant().removeAspect(name);
	}
	
	public boolean addAspect(String name, Aspect aspect)throws RegistryException{
		return getRegistryForTenant().removeAspect(name);
	}

	/**
     * Delete an existing comment.
     *
     * @param commentPath path to comment resource ("..foo/r1;comment:1")
     *
     * @throws RegistryException Registry implementations may handle exceptions and throw
     *                           RegistryException if the exception has to be propagated to the
     *                           client.
     */
	public void removeComment(String commentPath) throws RegistryException {
		getRegistryForTenant().removeComment(commentPath);
	}
	
	/**
	 * Returns content of a Resource at the specified path. Used by the Registry Web Service client to fetch resource content only when requested.
	 * @param path Path of the resource
	 * @return data handler that is compatible with web services
	 * @throws RegistryException
	 */
	// Used to provide content on demand - for resources
	public DataHandler getContent(String path) throws RegistryException {
		try {
            String[] parts = path.split("[;]start[=]");
            if (parts.length == 2) {
                String[] subParts = parts[1].split("[;]pageSize[=]");
                if (subParts.length == 2) {
                    return CommonUtil.makeDataHandler(getRegistryForTenant().get(parts[0],
                            Integer.parseInt(subParts[0]), Integer.parseInt(subParts[1])),
                            tempFile);
                }
            }
			return CommonUtil.makeDataHandler(getRegistryForTenant().get(path), tempFile);
		} catch (IOException e) {
			log.error("get Content failed - Unable to generate temp file", e);
		}
		return null;
	}
	
	/**
	 * Returns content of a specific Collection. Used by the Registry Web Service client to fetch collection content only when requested.
	 * @param path Path of the resource
	 * @return String array with the content 
	 * @throws RegistryException
	 */
	// Used to provide content on demand - for Collection
	public String[] getCollectionContent(String path) throws RegistryException {
		Object collectionContent = getRegistryForTenant().get(path).getContent();
		if (!(collectionContent instanceof String[])) {
			throw new RegistryException("This method only provides content for a Collection. It has been accessed by a Resource");
		}
		return (String[])collectionContent;
	}

    /**
     * @param path resource path
     * @return child count inside the resource
     * @throws RegistryException
     */
    public int getChildCount(String path) throws RegistryException {
        int count = 0;
        Collection collection = (Collection) getRegistryForTenant().get(path);
        if (collection != null) {
            count = collection.getChildCount();
        }
        return count;
    }

    public WSResourceData getAll(String path) throws RegistryException {
        WSResourceData resourceData = new WSResourceData();
        resourceData.setResource(WSget(path));
        UserRegistry configUserRegistry = (UserRegistry)getConfigUserRegistry();
        if (configUserRegistry != null) {
            resourceData.setRating(getRating(path, configUserRegistry.getUserName()));
        } else {
            resourceData.setRating(-1);
        }
        resourceData.setAverageRating(getAverageRating(path));
        resourceData.setTags(WSgetTags(path));
        resourceData.setComments(WSgetComments(path));
        resourceData.setAssociations(WSgetAllAssociations(path));
        return resourceData;
    }


    /**
     *
     * @param path                Path of the resource
     * @param snapshotId          Version ID
     * @return                    Succeed or not
     * @throws RegistryException  If operation fails
     */
    public boolean removeVersionHistory(String path, long snapshotId)
    		throws RegistryException {
        return getRegistryForTenant().removeVersionHistory(path, snapshotId);
    }
    
    public void dumpLite(String s, Writer writer) throws RegistryException {
        // Implementation needs to be added
    }
}


