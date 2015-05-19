/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.indexing.filter;

import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourcePath;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.jdbc.handlers.filters.Filter;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.core.utils.VersionedPath;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MediaTypeFilter extends Filter {

    private String mediaTypeRegEx;
    private Pattern pattern;

    public String getMediaTypeRegEx() {
        return mediaTypeRegEx;
    }

    public void setMediaTypeRegEx(String mediaTypeRegEx) {
        this.mediaTypeRegEx = mediaTypeRegEx;
    }

    private static String defaultRegex = "application.*";

    public MediaTypeFilter() {
        this(defaultRegex);
    }

    public MediaTypeFilter(String mediaTypeRegEx) {
        this.mediaTypeRegEx = mediaTypeRegEx;
        pattern = Pattern.compile(mediaTypeRegEx);
    }

    public int hashCode() {
        return getEqualsComparator().hashCode();
    }

    // Method to generate a unique string that can be used to compare two objects of the same type
    // for equality.
    private String getEqualsComparator() {
        StringBuffer sb = new StringBuffer();
        sb.append(getClass().getName());
        sb.append("|");
        sb.append(mediaTypeRegEx);
        sb.append("|");
        sb.append(invert);
        return sb.toString();
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (other == null) {
            return false;
        }
        if (other instanceof MediaTypeFilter) {
            MediaTypeFilter otherMediaTypeMatcher = (MediaTypeFilter) other;
            return (getEqualsComparator().equals(otherMediaTypeMatcher.getEqualsComparator()));
        }
        return false;
    }

    @Override
    public boolean handleGet(RequestContext requestContext) throws RegistryException {
        // check if the request is for new resource
        ResourcePath resourcePath = requestContext.getResourcePath();
        if (resourcePath.parameterExists("new")) {
            String mediaType = resourcePath.getParameterValue("mediaType");
            if (mediaType != null){
                Matcher matcher = pattern.matcher(mediaType);
                return  matcher.matches();
            }
        }

        Resource resource = requestContext.getResource();
        if (resource == null) {
            VersionedPath versionedPath =
                    RegistryUtils.getVersionedPath(requestContext.getResourcePath());

            if (versionedPath.getVersion() == -1) {
                resource = requestContext.getRepository().
                        get(requestContext.getResourcePath().getPath());
                requestContext.setResource(resource);
            }
        }

        if (resource != null) {
            String mType = resource.getMediaType();
            if (mType != null){
                Matcher matcher = pattern.matcher(mType);
                return  matcher.matches();
            }
        }

        return false;
    }

    @Override
    public boolean handlePut(RequestContext requestContext) throws RegistryException {
        Resource resource = requestContext.getResource();
        if (resource == null) {
            return false;
        }

        String mType = resource.getMediaType();
        if (mType != null){
            Matcher matcher = pattern.matcher(mType);
            return  matcher.matches();
        }
        return false;

    }

    @Override
    public boolean handleImportResource(RequestContext requestContext) throws RegistryException {
        Resource resource = requestContext.getResource();
        if (resource == null) {
            return false;
        }

        String mType = resource.getMediaType();
        if (mType != null){
            Matcher matcher = pattern.matcher(mType);
            return  matcher.matches();
        }
        return false;
    }

    @Override
    public boolean handleDelete(RequestContext requestContext) throws RegistryException {
        Resource resource = requestContext.getResource();
        if (resource == null) {
            resource =
                    requestContext.getRepository().get(requestContext.getResourcePath().getPath());
            requestContext.setResource(resource);
        }

        if (resource != null) {
            String mType = resource.getMediaType();
            if (mType != null){
                Matcher matcher = pattern.matcher(mType);
                return  matcher.matches();
            }
        }

        return false;
    }

    @Override
    public boolean handlePutChild(RequestContext requestContext) throws RegistryException {

        Collection parentCollection = requestContext.getParentCollection();
        if (parentCollection == null) {
            String parentPath = requestContext.getParentPath();
            if (parentPath == null) {
                parentPath = RegistryUtils.getParentPath(requestContext.getResourcePath().getPath());
                requestContext.setParentPath(parentPath);
            }

            VersionedPath versionedPath =
                    RegistryUtils.getVersionedPath(requestContext.getResourcePath());

            if (versionedPath.getVersion() == -1) {
                Resource parentResource = requestContext.getRepository().get(parentPath);
                if (parentResource != null) {
                    if (parentResource instanceof Collection) {
                        parentCollection = (Collection) parentResource;
                        requestContext.setParentCollection(parentCollection);
                    } else {
                        // parent should be a collection, already exists a non-collection

                        String msg = "There already exist non collection resource." + parentPath +
                                     "Child can only be added to collections";
                        throw new RegistryException(msg);
                    }
                }
            }
        }

        if (parentCollection != null) {
            String parentMediaType = parentCollection.getMediaType();
            if (parentMediaType != null){
                Matcher matcher = pattern.matcher(parentMediaType);
                return  matcher.matches();
            }
        }

        return false;
    }

    @Override
    public boolean handleImportChild(RequestContext requestContext) throws RegistryException {
        Collection parentCollection = requestContext.getParentCollection();
        if (parentCollection == null) {
            String parentPath = requestContext.getParentPath();
            if (parentPath == null) {
                parentPath = RegistryUtils.
                                                  getParentPath(requestContext.getResourcePath().getPath());
                requestContext.setParentPath(parentPath);
            }

            VersionedPath versionedPath =
                    RegistryUtils.getVersionedPath(requestContext.getResourcePath());

            if (versionedPath.getVersion() == -1) {
                parentCollection = (Collection) requestContext.getRepository().get(parentPath);
                requestContext.setParentCollection(parentCollection);
            }
        }

        if (parentCollection != null) {
            String parentMediaType = parentCollection.getMediaType();
            if (parentMediaType != null){
                Matcher matcher = pattern.matcher(parentMediaType);
                return  matcher.matches();
            }
        }

        return false;
    }
}
