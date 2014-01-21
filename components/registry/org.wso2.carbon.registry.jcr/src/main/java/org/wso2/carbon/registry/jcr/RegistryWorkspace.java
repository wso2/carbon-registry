/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.jcr;

import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.jcr.lock.RegistryLockManager;
import org.wso2.carbon.registry.jcr.nodetype.RegistryNodeTypeManager;
import org.wso2.carbon.registry.jcr.observation.RegistryObservationManager;
import org.wso2.carbon.registry.jcr.query.RegistryQueryManager;
import org.wso2.carbon.registry.jcr.util.RegistryJCRItemOperationUtil;
import org.wso2.carbon.registry.jcr.version.RegistryVersionManager;
import org.xml.sax.ContentHandler;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.lock.LockManager;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.QueryManager;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionManager;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class RegistryWorkspace implements Workspace {


    private Registry userRegistry;
    private RegistrySession registrySession;
    private String userId = null;
    private static HashMap sessions = new HashMap();
    private RegistryNodeTypeManager regNodeTypeMan = null;
    private ObservationManager observationMngr;
    private VersionManager versionMngr;
    private LockManager lockManager;
    private NamespaceRegistry registryNamespace;

    public RegistryWorkspace(RegistrySession registrySession) throws RepositoryException {

        this.registrySession = registrySession;
        this.userRegistry = this.registrySession.userRegistry;
        this.regNodeTypeMan = new RegistryNodeTypeManager(registrySession);
        this.observationMngr = new RegistryObservationManager();
        this.versionMngr = new RegistryVersionManager(this.registrySession);
        this.lockManager = new RegistryLockManager(this.registrySession);
        this.registryNamespace = new RegistryNamespace();
    }

    public RegistryWorkspace(String userID, RegistrySession registrySession) throws RepositoryException {

        this.registrySession = registrySession;
        this.userRegistry = this.registrySession.userRegistry;
        sessions.put(userID, registrySession);
        this.userId = userID;
        this.regNodeTypeMan = new RegistryNodeTypeManager(registrySession);
        this.observationMngr = new RegistryObservationManager();
        this.versionMngr = new RegistryVersionManager(this.registrySession);
        this.lockManager = new RegistryLockManager(this.registrySession);
        this.registryNamespace = new RegistryNamespace();

    }

    public RegistryWorkspace(String name) {
        // for Workspace.createWorkspace method
    }


    public Session getSession() {
        RegistrySession mySes = null;

        if (sessions != null) {

            mySes = (RegistrySession) sessions.get(userId);
        }

        return mySes;

    }

    public String getName() {

        String name = null;

        if (getSession() != null) {

            name = ((RegistrySession) getSession()).getWorkspaceName();

        }

        return name;
    }

    public void copy(String s, String s1) throws ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException {
//          A read only session must not be allowed to copy a node
        RegistryJCRItemOperationUtil.validateReadOnlyItemOpr(registrySession);

        try {

            if (userRegistry != null) {
                userRegistry.copy(s, s1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void copy(String s, String s1, String s2) throws NoSuchWorkspaceException, ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException {
        RegistryJCRItemOperationUtil.validateReadOnlyItemOpr(registrySession);


    }

    public void clone(String s, String s1, String s2, boolean b) throws NoSuchWorkspaceException, ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException {


    }

    public void move(String s, String s1) throws ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException {
//         A read only session must not be allowed to move a node
        RegistryJCRItemOperationUtil.validateReadOnlyItemOpr(registrySession);
        try {
            if (userRegistry.resourceExists(s)) {

                userRegistry.move(s, s1);
            }
        } catch (RegistryException e) {
           throw new RepositoryException("RegistryException occurred at Registry level");
        }

    }

    public void restore(Version[] versions, boolean b) throws ItemExistsException, UnsupportedRepositoryOperationException, VersionException, LockException, InvalidItemStateException, RepositoryException {

    }

    public LockManager getLockManager() throws UnsupportedRepositoryOperationException, RepositoryException {

        return lockManager;
    }

    public QueryManager getQueryManager() throws RepositoryException {

        return new RegistryQueryManager(registrySession);
    }

    public NamespaceRegistry getNamespaceRegistry() throws RepositoryException {

        return registryNamespace;
    }

    public NodeTypeManager getNodeTypeManager() throws RepositoryException {

        return regNodeTypeMan;

    }

    public ObservationManager getObservationManager() throws UnsupportedRepositoryOperationException, RepositoryException {

        return observationMngr;
    }

    public VersionManager getVersionManager() throws UnsupportedRepositoryOperationException, RepositoryException {

        return versionMngr;
    }

    public String[] getAccessibleWorkspaceNames() throws RepositoryException {


        String temp = "";
        String[] constant;

        for (int i = 0; i < RegistryRepository.credentialConstants.size(); i++) {

            constant = RegistryRepository.credentialConstants.get(i).toString().split(":");
            if (constant[0].equals(registrySession.getWorkspaceName())) {

                temp = temp + constant[1] + ":";
            }
            if (temp.length() > 1) {
                temp = temp.substring(0, temp.toCharArray().length - 1);

            }
        }


        return temp.split(":");
    }

    public ContentHandler getImportContentHandler(String s, int i) throws PathNotFoundException, ConstraintViolationException, VersionException, LockException, AccessDeniedException, RepositoryException {


        return null;
    }

    public void importXML(String s, InputStream inputStream, int i) throws IOException, VersionException, PathNotFoundException, ItemExistsException, ConstraintViolationException, InvalidSerializedDataException, LockException, AccessDeniedException, RepositoryException {


    }

    public void createWorkspace(String s) throws AccessDeniedException, UnsupportedRepositoryOperationException, RepositoryException {  //TODO

        //    Here we create a workspace bind to the same session that this particular workspace bound to.
        if(RegistryJCRItemOperationUtil.isWorkspaceExists(registrySession,s)){
         throw new RepositoryException("New workspace with the name already exists: "+s);
        }
        Workspace workspace = new RegistryWorkspace(s);           //TODO should provide a new root
        registrySession.getRepository().getWorkspaceMap().put(s, workspace);

    }

    public void createWorkspace(String s, String s1) throws AccessDeniedException, UnsupportedRepositoryOperationException, NoSuchWorkspaceException, RepositoryException {  //TODO
        try {
            registrySession.getRepository().getWorkspaceMap().put(s,
                    (Workspace) ((RegistryWorkspace) registrySession.getRepository().getWorkspaceMap().get(s1)).clone());
        } catch (CloneNotSupportedException e) {
            throw new UnsupportedRepositoryOperationException("Workspace clone not supported " + e.getMessage());
        }

    }

    public void deleteWorkspace(String s) throws AccessDeniedException, UnsupportedRepositoryOperationException, NoSuchWorkspaceException, RepositoryException { //TODO
        if(!RegistryJCRItemOperationUtil.isWorkspaceExists(registrySession,s)){
         throw new NoSuchWorkspaceException("Cannot remove non existing workspace: "+s);
        }
        registrySession.getRepository().getWorkspaceMap().remove(s);  //TODO
    }

}
