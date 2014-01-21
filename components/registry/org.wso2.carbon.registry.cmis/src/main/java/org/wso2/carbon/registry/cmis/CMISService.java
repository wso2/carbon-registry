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

package org.wso2.carbon.registry.cmis;

import org.apache.chemistry.opencmis.commons.data.*;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.spi.Holder;

import java.math.BigInteger;
import java.util.List;

/**
 * Registry service implementation for CMIS.
 */
public class CMISService extends AbstractCmisService {
    private final CMISRepository gregRepository;
    //private final Map<String, Session> sessions = new HashMap<String, Session>();

    private CallContext context;

    public CMISService(CMISRepository gregRepository) {
        this.gregRepository = gregRepository;
    }

    /*
     *@Override
     *public void close() {
     *   gregRepository.getRegistry().;
     *
     *  super.close();
     *}
     */
    public void setCallContext(CallContext context) {
        this.context = context;
    }

    public CallContext getCallContext() {
        return context;
    }

    @Override
    public RepositoryInfo getRepositoryInfo(String repositoryId, ExtensionsData extension) {
        return gregRepository.getRepositoryInfo();
    }

    @Override
    public List<RepositoryInfo> getRepositoryInfos(ExtensionsData extension) {
        return gregRepository.getRepositoryInfos();
    }

    @Override
    public TypeDefinitionList getTypeChildren(String repositoryId, String typeId, Boolean includePropertyDefinitions,
                                              BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {

        return gregRepository.getTypeChildren(typeId, includePropertyDefinitions, maxItems, skipCount);
    }

    @Override
    public TypeDefinition getTypeDefinition(String repositoryId, String typeId, ExtensionsData extension) {
        return gregRepository.getTypeDefinition(typeId);
    }

    @Override
    public List<TypeDefinitionContainer> getTypeDescendants(String repositoryId, String typeId, BigInteger depth,
                                                            Boolean includePropertyDefinitions, ExtensionsData extension) {

        return gregRepository.getTypesDescendants(typeId, depth, includePropertyDefinitions);
    }

    // navigation service

    @Override
    public ObjectInFolderList getChildren(String repositoryId, String folderId, String filter, String orderBy,
                                          Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
                                          Boolean includePathSegment, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {

        return gregRepository.getChildren(folderId, filter, includeAllowableActions,
                includePathSegment, maxItems, skipCount, this, context.isObjectInfoRequired());
    }

    @Override
    public List<ObjectInFolderContainer> getDescendants(String repositoryId, String folderId, BigInteger depth,
                                                        String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
                                                        String renditionFilter, Boolean includePathSegment, ExtensionsData extension) {

        return gregRepository.getDescendants(folderId, depth, filter, includeAllowableActions,
                includePathSegment, this, context.isObjectInfoRequired(), false);
    }

    @Override
    public ObjectData getFolderParent(String repositoryId, String folderId, String filter, ExtensionsData extension) {
        return gregRepository.getFolderParent(folderId, filter, this, context.isObjectInfoRequired());
    }

    @Override
    public List<ObjectInFolderContainer> getFolderTree(String repositoryId, String folderId, BigInteger depth,
                                                       String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
                                                       String renditionFilter, Boolean includePathSegment, ExtensionsData extension) {

        return gregRepository.getDescendants(folderId, depth, filter, includeAllowableActions,
                includePathSegment, this, context.isObjectInfoRequired(), true);
    }

    @Override
    public List<ObjectParentData> getObjectParents(String repositoryId, String objectId, String filter,
                                                   Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
                                                   Boolean includeRelativePathSegment, ExtensionsData extension) {

        return gregRepository.getObjectParents(objectId, filter, includeAllowableActions,
                includeRelativePathSegment, this, context.isObjectInfoRequired());
    }

    @Override
    public ObjectList getCheckedOutDocs(String repositoryId, String folderId, String filter, String orderBy,
                                        Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
                                        BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {

        return gregRepository.getCheckedOutDocs(folderId, filter, orderBy, includeAllowableActions,
                maxItems, skipCount);
    }

    //object service

    @Override
    public String createDocument(String repositoryId, Properties properties, String folderId,
                                 ContentStream contentStream, VersioningState versioningState, List<String> policies, Acl addAces,
                                 Acl removeAces, ExtensionsData extension) {

        return gregRepository.createDocument(properties, folderId, contentStream, versioningState);
    }

    @Override
    public String createDocumentFromSource(String repositoryId, String sourceId, Properties properties,
                                           String folderId, VersioningState versioningState, List<String> policies, Acl addAces, Acl removeAces,
                                           ExtensionsData extension) {

        return gregRepository.createDocumentFromSource(sourceId, properties, folderId, versioningState);
    }

    @Override
    public void setContentStream(String repositoryId, Holder<String> objectId, Boolean overwriteFlag,
                                 Holder<String> changeToken, ContentStream contentStream, ExtensionsData extension) {

        gregRepository.setContentStream(objectId, overwriteFlag, contentStream);
    }

    @Override
    public void deleteContentStream(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
                                    ExtensionsData extension) {

        gregRepository.setContentStream(objectId, true, null);
    }

    @Override
    public String createFolder(String repositoryId, Properties properties, String folderId, List<String> policies,
                               Acl addAces, Acl removeAces, ExtensionsData extension) {

        return gregRepository.createFolder(properties, folderId);
    }

    @Override
    public void deleteObjectOrCancelCheckOut(String repositoryId, String objectId, Boolean allVersions,
                                             ExtensionsData extension) {

        gregRepository.deleteObject(objectId, allVersions);
    }

    @Override
    public FailedToDeleteData deleteTree(String repositoryId, String folderId, Boolean allVersions,
                                         UnfileObject unfileObjects, Boolean continueOnFailure, ExtensionsData extension) {

        return gregRepository.deleteTree(folderId);
    }

    @Override
    public AllowableActions getAllowableActions(String repositoryId, String objectId, ExtensionsData extension) {
        return gregRepository.getAllowableActions(objectId);
    }

    @Override
    public ContentStream getContentStream(String repositoryId, String objectId, String streamId, BigInteger offset,
                                          BigInteger length, ExtensionsData extension) {

        return gregRepository.getContentStream(objectId, offset, length);
    }

    @Override
    public ObjectData getObject(String repositoryId, String objectId, String filter, Boolean includeAllowableActions,
                                IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
                                Boolean includeAcl, ExtensionsData extension) {

        return gregRepository.getObject(objectId, filter, includeAllowableActions, this,
                context.isObjectInfoRequired());
    }

    @Override
    public ObjectData getObjectByPath(String repositoryId, String path, String filter, Boolean includeAllowableActions,
                                      IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
                                      Boolean includeAcl, ExtensionsData extension) {

        return gregRepository.getObjectByPath(path, filter, includeAllowableActions, includeAcl,
                this, context.isObjectInfoRequired());
    }

    @Override
    public Properties getProperties(String repositoryId, String objectId, String filter, ExtensionsData extension) {
        return gregRepository.getProperties(objectId, filter, false, this,
                context.isObjectInfoRequired());
    }

    @Override
    public void moveObject(String repositoryId, Holder<String> objectId, String targetFolderId, String sourceFolderId,
                           ExtensionsData extension) {

        gregRepository.moveObject(objectId, targetFolderId, this, context.isObjectInfoRequired());
    }

    @Override
    public void updateProperties(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
                                 Properties properties, ExtensionsData extension) {

        gregRepository.updateProperties(objectId, properties, this, context.isObjectInfoRequired());
    }

    //versioning service

    @Override
    public void checkOut(String repositoryId, Holder<String> objectId, ExtensionsData extension,
                         Holder<Boolean> contentCopied) {

        gregRepository.checkOut(objectId, contentCopied);
    }

    @Override
    public void cancelCheckOut(String repositoryId, String objectId, ExtensionsData extension) {
        gregRepository.cancelCheckout(objectId);
    }

    @Override
    public void checkIn(String repositoryId, Holder<String> objectId, Boolean major, Properties properties,
                        ContentStream contentStream, String checkinComment, List<String> policies, Acl addAces, Acl removeAces,
                        ExtensionsData extension) {

        gregRepository.checkIn(objectId, major, properties, contentStream, checkinComment);
    }

    @Override
    public List<ObjectData> getAllVersions(String repositoryId, String objectId, String versionSeriesId, String filter,
                                           Boolean includeAllowableActions, ExtensionsData extension) {

        return gregRepository.getAllVersions(versionSeriesId == null ? objectId : versionSeriesId,
                filter, includeAllowableActions, this, context.isObjectInfoRequired());
    }

    @Override
    public ObjectData getObjectOfLatestVersion(String repositoryId, String objectId, String versionSeriesId,
                                               Boolean major, String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
                                               String renditionFilter, Boolean includePolicyIds, Boolean includeAcl, ExtensionsData extension) {

        return gregRepository.getObject(versionSeriesId == null ? objectId : versionSeriesId,
                filter, includeAllowableActions, this, context.isObjectInfoRequired());
    }

    @Override
    public Properties getPropertiesOfLatestVersion(String repositoryId, String objectId, String versionSeriesId,
                                                   Boolean major, String filter, ExtensionsData extension) {

        ObjectData object = getObjectOfLatestVersion(repositoryId, objectId, versionSeriesId, major, filter, false,
                null, null, false, false, extension);

        return object.getProperties();
    }

    //discovery service

    @Override
    public ObjectList query(String repositoryId, String statement, Boolean searchAllVersions,
                            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
                            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {

        return gregRepository.query(statement, searchAllVersions, includeAllowableActions,
                maxItems, skipCount);
    }

}

