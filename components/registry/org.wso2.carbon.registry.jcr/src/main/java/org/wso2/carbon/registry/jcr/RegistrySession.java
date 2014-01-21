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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.jcr.nodetype.RegistryNodeType;
import org.wso2.carbon.registry.jcr.retention.RegistryRetentionManager;
import org.wso2.carbon.registry.jcr.retention.RegistryRetentionPolicy;
import org.wso2.carbon.registry.jcr.security.RegistryAccessControlManager;
import org.wso2.carbon.registry.jcr.util.RegistryJCRItemOperationUtil;
import org.wso2.carbon.registry.jcr.util.RegistryJCRSpecificStandardLoderUtil;
import org.wso2.carbon.registry.jcr.util.security.PrivilegeRegistry;
import org.wso2.carbon.registry.jcr.util.test.data.TCKTestDataLoader;
import org.wso2.carbon.user.core.UserStoreException;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.retention.RetentionManager;
import javax.jcr.security.AccessControlManager;
import javax.jcr.version.VersionException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.security.AccessControlException;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class RegistrySession implements Session {

    /*
     Here every session has one workspace.there is a map of workspaces in registry repository.
     It has corresponding sessions with d user id.so in RegistryWorkspace.createWorkspace we can
     create a another userregistry related to a new session
    */
    public UserRegistry userRegistry;

    private RegistrySimpleCredentials registrySimpleCredentials;
    private String workspaceName ="";
    private RegistryWorkspace registryWorkspace;
    private AccessControlManager regAccControlMngr;
    private RetentionManager regRetentionMngr;
    private RegistryRepository registryRepository;
    private String WORKSPACE_ROOT = "";
    private static Log log = LogFactory.getLog(RegistrySession.class);
    private String USER_ID = "";
    private boolean SESSION_SAVED = true;

    public void sessionPending() {
         SESSION_SAVED = false;
    }

    public void sessionSaved() {
        SESSION_SAVED = true;
    }

    public boolean isSessionSaved() {
     return SESSION_SAVED;
    }

    public RegistrySession(RegistryRepository registryRepository, String workspaceName,
                           RegistrySimpleCredentials registrySimpleCredentials, UserRegistry userReg, String userID) throws RepositoryException {

        this.workspaceName = workspaceName;
        this.USER_ID = userID;
        this.WORKSPACE_ROOT = RegistryJCRSpecificStandardLoderUtil.getJCRRegistryWorkspaceRoot() + "/" + this.workspaceName + "/";
        this.registryRepository = registryRepository;
        this.userRegistry = userReg;
        createRootNode();
        loadJCRSystemConfiguration(userRegistry,WORKSPACE_ROOT);
        this.registrySimpleCredentials = registrySimpleCredentials;
        this.registryWorkspace = new RegistryWorkspace(registrySimpleCredentials.getUserID(), this);
        this.regAccControlMngr = new RegistryAccessControlManager(this);
        this.regRetentionMngr = new RegistryRetentionManager(this);

    }

    public RegistrySession(RegistryRepository registryRepository, String workspaceName, UserRegistry userReg, String userID) throws RepositoryException {
        this.workspaceName = workspaceName;
        USER_ID = userID;
        this.WORKSPACE_ROOT = RegistryJCRSpecificStandardLoderUtil.getJCRRegistryWorkspaceRoot() + "/" + this.workspaceName + "/";
        this.registryRepository = registryRepository;
        this.userRegistry = userReg;
        createRootNode();
        loadJCRSystemConfiguration(userRegistry,WORKSPACE_ROOT);
        this.registryWorkspace = new RegistryWorkspace(this);
        this.regAccControlMngr = new RegistryAccessControlManager(this);
        this.regRetentionMngr = new RegistryRetentionManager(this);

    }
    //TODO workspace name is "" and have to handle properly
    public RegistrySession(RegistryRepository registryRepository, UserRegistry userReg, String userID) throws RepositoryException {
        this.USER_ID = userID;
        this.WORKSPACE_ROOT = RegistryJCRSpecificStandardLoderUtil.getJCRRegistryWorkspaceRoot() + "/" + this.workspaceName + "/";
        this.registryRepository = registryRepository;
        this.userRegistry = userReg;
        createRootNode();
        loadJCRSystemConfiguration(userRegistry,WORKSPACE_ROOT);
        this.registryWorkspace = new RegistryWorkspace(this);
        this.regAccControlMngr = new RegistryAccessControlManager(this);
        this.regRetentionMngr = new RegistryRetentionManager(this);
    }

    public String getWorkspaceRootPath() {
        return WORKSPACE_ROOT;
    }

    private void loadJCRSystemConfiguration(UserRegistry userReg,String workspaceRoot) throws RepositoryException{
        try {
            RegistryJCRSpecificStandardLoderUtil.loadJCRSystemConfigs(userReg,workspaceRoot);
        } catch (RegistryException e) {
            e.printStackTrace();
//            throw new RepositoryException("Registry Exception occurred while creating root node " + e.getMessage());
        }
    }

    private void createRootNode() throws RepositoryException {
        try {
            if (!userRegistry.resourceExists(RegistryJCRSpecificStandardLoderUtil.
                    getJCRRegistryWorkspaceRoot())) {
                Resource resource = (CollectionImpl) userRegistry.newCollection();
                userRegistry.put(RegistryJCRSpecificStandardLoderUtil.
                        getJCRRegistryWorkspaceRoot(), resource);
            }
            if(!userRegistry.resourceExists(WORKSPACE_ROOT)) {
            Resource resource = (CollectionImpl) userRegistry.newCollection();
            resource.setDescription("nt:base");
            resource.setProperty("jcr:primaryType", "nt:base");
            userRegistry.put(WORKSPACE_ROOT, resource);
            }
        } catch (RegistryException e) {
            throw new RepositoryException("Registry Exception occurred while creating root node " + e.getMessage());
        }
    }

    private void addLoggedinWorkSpace(RegistryWorkspace registryWorkspace) {
//    getRepository().getWorkspaceMap().put(registryWorkspace.getName(),)

    }

    public UserRegistry getUserRegistry() {  //my method
        return userRegistry;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public RegistryRepository getRepository() {
        return registryRepository;
    }

    public String getUserID() {
//        String user_id = null;
//
//        try {
//            user_id = userRegistry.getRegistryContext().getEmbeddedRegistryService()
//                    .getGovernanceUserRegistry().getUserName();
//        } catch (RegistryException e) {
//
//        }
        return USER_ID;

    }

    public String[] getAttributeNames() {
        return registrySimpleCredentials.getAttributeNames();
    }

    public Object getAttribute(String s) {
        return registrySimpleCredentials.getAttribute(s);
    }

    public RegistryWorkspace getWorkspace() {
        return registryWorkspace;
    }

    public Node getRootNode() throws RepositoryException {
        return getNode(WORKSPACE_ROOT);

    }

    public Session impersonate(Credentials credentials) throws LoginException, RepositoryException {
        return registryRepository.login(credentials);

    }

    public Node getNodeByUUID(String s) throws ItemNotFoundException, RepositoryException {  //deprecated
        return null;
    }

    public Node getNodeByIdentifier(String s) throws ItemNotFoundException, RepositoryException { //TODO
        return getNode(s);
    }


    public Item getItem(String s) throws PathNotFoundException, RepositoryException {
        if(!s.contains(getWorkspaceRootPath())){
            s = getWorkspaceRootPath().substring(0,getWorkspaceRootPath().length()-1) +s;
        }
//        // TODO : RMOVE THIS AFTER TCK TEST, A DUMMY FOR NODE DEF test
//        if (s.equals("/testroot")) {
//            return getRootNode().getNode("testroot");
//        }

        Item anItem = null;
        CollectionImpl collection = null;
        String[] tempArr = null;
        String tempPath = s; // example:tempP="/abc/post/test";

        if (tempPath.contains("[")) {
            tempPath = tempPath.substring(1, tempPath.length() - 1);
        }
        if (s.contains("[")) {
            s = s.substring(1, s.length() - 1);
        }


        if ((tempPath != null) && (tempPath.contains("/"))) {
            tempArr = tempPath.split("/");
//            if (tempArr.length == 2) {
//                tempPath = "/";
//            } else {
            tempPath = tempPath.substring(0, (tempPath.length()) -
                    (tempArr[tempArr.length - 1].length()) - 1); //parent path
//            }

            try {
                if (userRegistry.resourceExists(s)) {
                    if (userRegistry.get(s) instanceof CollectionImpl) {
                        anItem = getNode(s);
                    } else if (userRegistry.get(s) instanceof ResourceImpl) {
                        anItem = getNode(tempPath).getProperty(tempArr[tempArr.length - 1]);
                    }
                } else if (userRegistry.resourceExists(tempPath)) {
                    anItem = getNode(tempPath).getProperty(tempArr[tempArr.length - 1]);
                } else {
                    throw new PathNotFoundException("Item does not exists at path " + s);
                }
            } catch (RegistryException e) {
                String msg = "failed to resolve the path of the given item " + this;
                log.debug(msg);
                throw new PathNotFoundException(msg, e);
            }

        } else {
            throw new PathNotFoundException("Item does not exists at path " + s);
        }

        return anItem;
    }

    public Node getNode(String s) throws PathNotFoundException, RepositoryException {

        RegistryNode subNode = null;
        try {
            if (getUserRegistry().resourceExists(s)) {
                subNode = new RegistryNode(s, this);
                subNode.setCollection(s);
                subNode.nodeType = (RegistryNodeType) getWorkspace().getNodeTypeManager().getNodeType(subNode.resource.getDescription());
            } else {
                throw new PathNotFoundException("failed to resolve the path of the given node");
            }
        } catch (RegistryException e) {
            String msg = "failed to resolve the path of the given node or violation of repository syntax " + this;
            log.debug(msg);
            throw new RepositoryException(msg, e);
        }
        return subNode;
    }


    public Property getProperty(String s) throws PathNotFoundException, RepositoryException {

        String tempPath = s;
        String[] tempArr = tempPath.split("/");
        tempPath = tempPath.substring(0, (tempPath.length()) - (tempArr[tempArr.length - 1].length()) - 1);
        String propName = tempArr[tempArr.length - 1];

        Item item = getItem(tempPath);
        return ((Node) item).getProperty(propName);
    }

    public boolean itemExists(String s) throws RepositoryException {
        boolean itemEx = true;
        try {
            itemEx = userRegistry.resourceExists(s);
        } catch (RegistryException e) {
            e.printStackTrace();
        }
        return itemEx;
    }

    public boolean nodeExists(String s) throws RepositoryException {
        boolean nodeEx = false;
        try {
            if ((userRegistry != null) && userRegistry.resourceExists(s)) {
                nodeEx = true;
            }
        } catch (RegistryException e) {
            String msg = "failed to resolve the path of the given node " + this;
            log.debug(msg);
            throw new RepositoryException(msg, e);
        }
        return nodeEx;
    }

    public boolean propertyExists(String s) throws RepositoryException {
        return itemExists(s);
    }

    public void move(String s, String s1) throws ItemExistsException, PathNotFoundException, VersionException,
        ConstraintViolationException, LockException, RepositoryException {

//           A read only session must not be allowed to move a node
        RegistryJCRItemOperationUtil.validateReadOnlyItemOpr(this);

        try {
            String[] temps = s.split("/");
            String[] temps1 = s1.split("/");

            if (!userRegistry.resourceExists(s)) {

                throw new PathNotFoundException();

            } else if (userRegistry.resourceExists(s1) && (temps[temps.length - 1].equals(temps1[temps1.length - 1]))) {

                throw new ItemExistsException();

            } else {

                userRegistry.move(s, s1);

            }

        } catch (Exception e) {
            String msg = "failed to resolve the path " + this;
            log.debug(msg);
            throw new PathNotFoundException(msg, e);
        }

    }

    public void removeItem(String s) throws VersionException, LockException, ConstraintViolationException,
            AccessDeniedException, RepositoryException {

        try {

            if (userRegistry.resourceExists(s)) {
                userRegistry.delete(s);
            } else {
                throw new PathNotFoundException("No such path exists" + s);
            }

        } catch (RegistryException e) {
            e.printStackTrace();

        }

    }

    public void save() throws AccessDeniedException, ItemExistsException, ReferentialIntegrityException,
            ConstraintViolationException, InvalidItemStateException, VersionException, LockException,
            NoSuchNodeTypeException, RepositoryException {
         RegistryJCRItemOperationUtil.persistPendingChanges(this);
         sessionSaved();
    }

    public void refresh(boolean b) throws RepositoryException {   //TODO
        if(!b){
         removePendingChanges();
        }
    }

    private void removePendingChanges() throws RepositoryException {
        ((RegistryRetentionManager)getRetentionManager()).getPendingRetentionPolicies().clear();

        // revert transient deletions
        for(String s:((RegistryRetentionManager)getRetentionManager()).getPendingPolicyRemoveList()){
         getRetentionManager().setRetentionPolicy(s,new RegistryRetentionPolicy());
        }
        ((RegistryRetentionManager)getRetentionManager()).getPendingPolicyRemoveList().clear();
    }

    public boolean hasPendingChanges() throws RepositoryException {
        return false;
    }

    public ValueFactory getValueFactory() throws UnsupportedRepositoryOperationException, RepositoryException {

        return new RegistryValueFactory();
    }

    public boolean hasPermission(String s, String s1) throws RepositoryException {
        //s-absPAth ,s1-action

        boolean hasPer = false;
        try {

            hasPer = userRegistry.getUserRealm().getAuthorizationManager().isUserAuthorized(this.getUserID(), s, s1);

        } catch (UserStoreException e) {
            String msg = "failed to resolve the path of the given node " + this;
            log.debug(msg);
            throw new RepositoryException(msg, e);
        }

        return hasPer;
    }

    public void checkPermission(String s, String s1) throws AccessControlException, RepositoryException { //TODO

    }

    public boolean hasCapability(String s, Object o, Object[] objects) throws RepositoryException {  //TODO

        return false;
    }

    public ContentHandler getImportContentHandler(String s, int i) throws PathNotFoundException,
            ConstraintViolationException, VersionException, LockException, RepositoryException {  //TODO


        return null;
    }

    public void importXML(String s, InputStream inputStream, int i) throws IOException, PathNotFoundException,
            ItemExistsException, ConstraintViolationException, VersionException, InvalidSerializedDataException,
            LockException, RepositoryException { //TODO


    }

    public void exportSystemView(String s, ContentHandler contentHandler, boolean b, boolean b1) throws PathNotFoundException,
            SAXException, RepositoryException {  //TODO

    }

    public void exportSystemView(String s, OutputStream outputStream, boolean b, boolean b1) throws IOException,
            PathNotFoundException, RepositoryException { //TODO

    }

    public void exportDocumentView(String s, ContentHandler contentHandler, boolean b, boolean b1) throws PathNotFoundException,
            SAXException, RepositoryException {  //TODO

    }

    public void exportDocumentView(String s, OutputStream outputStream, boolean b, boolean b1) throws IOException,
            PathNotFoundException, RepositoryException { //TODO
    }

    public void setNamespacePrefix(String s, String s1) throws NamespaceException, RepositoryException {
        // s-prefix ,s1 = uri
        if(RegistryJCRSpecificStandardLoderUtil.getJCRSystemNameSpacePrefxMap().containsKey(s1)) {
            RegistryJCRSpecificStandardLoderUtil.getJCRSystemNameSpaceURIMap().
                    remove(RegistryJCRSpecificStandardLoderUtil.getJCRSystemNameSpacePrefxMap().get(s1));



        } else if(RegistryJCRSpecificStandardLoderUtil.getJCRSystemNameSpaceURIMap().containsKey(s)) {
            RegistryJCRSpecificStandardLoderUtil.getJCRSystemNameSpacePrefxMap().
                    remove(RegistryJCRSpecificStandardLoderUtil.getJCRSystemNameSpaceURIMap().get(s));
        }

        RegistryJCRSpecificStandardLoderUtil.getJCRSystemNameSpacePrefxMap().put(s1, s);
        RegistryJCRSpecificStandardLoderUtil.getJCRSystemNameSpaceURIMap().put(s, s1);

        PrivilegeRegistry privilegeRegistry= ((RegistryAccessControlManager)this.getAccessControlManager()).getPrivilegeRegistry();
        privilegeRegistry.refreshPrivRegistry();
    }

    public String[] getNamespacePrefixes() throws RepositoryException {
        String[] starr;
        Collection s = RegistryJCRSpecificStandardLoderUtil.getJCRSystemNameSpacePrefxMap().values();
        if (s != null) {

            Object[] objrr = s.toArray();
            starr = new String[objrr.length];
            for (int i = 0; i < objrr.length; i++) {
                starr[i] = objrr[i].toString();

            }
            return starr;
        }
        return new String[0];
    }

    public String getNamespaceURI(String s) throws NamespaceException, RepositoryException {

        if (RegistryJCRSpecificStandardLoderUtil.getJCRSystemNameSpaceURIMap().get(s) != null) {
            return RegistryJCRSpecificStandardLoderUtil.getJCRSystemNameSpaceURIMap().get(s).toString();
        } else {
            return null;
        }
    }

    public String getNamespacePrefix(String s) throws NamespaceException, RepositoryException {

        if (RegistryJCRSpecificStandardLoderUtil.getJCRSystemNameSpacePrefxMap().get(s) != null) {
            return RegistryJCRSpecificStandardLoderUtil.getJCRSystemNameSpacePrefxMap().get(s).toString();
        } else {
            return null;
        }
    }

    public void logout() {       //TODO
        try {
            TCKTestDataLoader.removeRetentionPolicies(this);
        } catch (RepositoryException e) {
          log.error("Cannot remove tck test data");
        }

    }

    public boolean isLive() {   //TODO

        return false;

    }

    public void addLockToken(String s) {  //TODO


    }

    public String[] getLockTokens() {   //TODO
        return new String[0];
    }

    public void removeLockToken(String s) {  //TODO

    }

    public AccessControlManager getAccessControlManager() throws UnsupportedRepositoryOperationException, RepositoryException {

        return regAccControlMngr;
    }

    public RetentionManager getRetentionManager() throws UnsupportedRepositoryOperationException, RepositoryException {

        return regRetentionMngr;
    }


}
