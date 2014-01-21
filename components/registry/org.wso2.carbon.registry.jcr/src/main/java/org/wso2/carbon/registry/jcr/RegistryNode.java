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
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.jcr.lock.RegistryLockManager;
import org.wso2.carbon.registry.jcr.nodetype.RegistryNodeDefinition;
import org.wso2.carbon.registry.jcr.nodetype.RegistryNodeType;
import org.wso2.carbon.registry.jcr.util.RegistryJCRItemOperationUtil;
import org.wso2.carbon.registry.jcr.util.RegistryJCRSpecificStandardLoderUtil;

import javax.jcr.*;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.ActivityViolationException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

public class RegistryNode implements Node {

    public String nodePath = "";
    public CollectionImpl resource = null;
    private RegistrySession registrySession;
    private Property property;
    private boolean isModified = false;

    public RegistryNodeType nodeType = null;
    private static Log log = LogFactory.getLog(RegistryNode.class);
    private boolean isRemoved = false;//TODO use this when a node is removed (not currently using )

    public RegistryNode(String s, RegistrySession registrySession) {

        this.nodePath = s;
        this.registrySession = registrySession;
        initColl(s);
    }


    private NodeType getNodetype() throws RepositoryException {
        NodeType ntType = null;
        if (resource.getDescription() != null) {
            ntType = registrySession.getWorkspace().getNodeTypeManager().
                    getNodeType(resource.getDescription());
        } else {
            try {
                String parPath = "";

//                if(resource.getParentPath().equals(registrySession.getWorkspaceRootPath())) { // Map registry root node path to "/"
//                    parPath = "/";
//                } else {
                parPath = resource.getParentPath();
//                }
                String ntName = registrySession.getUserRegistry().get(parPath).getDescription();
                ntType = registrySession.getWorkspace().getNodeTypeManager().
                        getNodeType(ntName);
            } catch (RegistryException e) {
                log.error("Error occurred while getting node type");
            }
        }
        return ntType;
    }


    public void initColl(String np) {

        try {

            resource = (CollectionImpl) registrySession.getUserRegistry().newCollection();
            resource.setPath(np);
        } catch (RegistryException e) {
            e.printStackTrace();
        }
    }

    public void setCollection(String s) throws RegistryException { //Non JCR method  //s-abs path:assume

        try {
            if (s != null && registrySession.getUserRegistry().get(s) instanceof CollectionImpl) {
                resource = (CollectionImpl) registrySession.getUserRegistry().get(s);
            }
        } catch (RegistryException e) {
            String msg = "Exception occurred in registry collection creation " + this;
            log.debug(msg);
            throw new RegistryException(msg, e);
        }
    }


    public Node addNode(String s) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        RegistryJCRItemOperationUtil.checkRetentionPolicy(registrySession, getPath());
        RegistryJCRItemOperationUtil.checkRetentionHold(registrySession, getPath());

        String absPath = "";
        String ntName = "";
//        List<String> versionList = new ArrayList<String>();
//        versionList.add("jcr:default");

        // Check if node aleady exists
        if (hasNode(s)) {
            throw new ItemExistsException("Node " + s + " already exists");
        }
        // check if tries to add a node to a property path.
        if (validateNodePathRefersToAPropertyPath(s)) {
            throw new ConstraintViolationException("Relative path cannot be a property " + s);
        }

        //check Intermediate nodes existance before adding a node
        if (!validateIntermediateNodeExistance(s)) {
            throw new PathNotFoundException("Intermediate nodes doesn't exists under " + s);
        }


//        if ((nodePath != null) && (nodePath.equals("/"))) {
//            absPath = nodePath + s;
//        } else {
        absPath = nodePath + "/" + s;
//        }
        RegistryNode subNode = new RegistryNode(absPath, registrySession);

        if ((!nodePath.equals(registrySession.getWorkspaceRootPath())) && ((getNodetype() != null))) {
            ntName = getNodetype().getName();
        } else {
            ntName = "nt:base";
        }

        CollectionImpl subCollection = null;
        try {
            subCollection = (CollectionImpl) registrySession.getUserRegistry().newCollection();
            subCollection.setDescription(ntName);
            subCollection.setProperty("jcr:uuid", absPath);  //Here we use node's path as its identifier
//            subCollection.setProperty("wso2.registry.jcr.versions", versionList);

            if (ntName.equals("mix:simpleVersionable") || ntName.equals("mix:versionable")) {
                subCollection.setProperty("jcr:checkedOut", "true");
                subCollection.setProperty("jcr:isCheckedOut", "true");
                subCollection.setProperty("jcr:frozenPrimaryType",ntName);
            }
            registrySession.getUserRegistry().put(absPath, subCollection);
            subNode.setCollection(absPath);
            subNode.setPrimaryType(ntName);
            subNode.nodeType = (RegistryNodeType) (registrySession.getWorkspace().
                    getNodeTypeManager().getNodeType(ntName));

        } catch (RegistryException e) {
            String msg = "failed to resolve the path of the given node " + this;
            log.debug(msg);
            throw new PathNotFoundException(msg, e);
        }

        isModified = true;
        return subNode;
    }


    private boolean validateIntermediateNodeExistance(String s) {   //assume s given as "testroot/abc under jcr conventions"
        boolean validPath;
        if (s.contains("/")) {
            String tmp[] = s.split("/");
            String interm_path = s.substring(0, s.length() - tmp[tmp.length - 1].length() - 1);
            try {
                String abspath = "";
                if (!nodePath.equals(registrySession.getWorkspaceRootPath())) {
                    abspath = nodePath + "/" + interm_path;
                } else {
                    abspath = nodePath + interm_path;
                }
                validPath = registrySession.getUserRegistry().resourceExists(abspath);
            } catch (RegistryException e) {
                validPath = false;
            }
        } else {
            validPath = true;
        }
        return validPath;
    }


    private boolean validateNodePathRefersToAPropertyPath(String s) throws RepositoryException, PathNotFoundException {   //assume s given as "testroot/abc under jcr conventions"
        boolean isAPropertyPath = false;
        String abspath = "";
        try {

            if (s.contains("/")) {
                String tmp[] = s.split("/");
                String interm_path = s.substring(0, s.length() - tmp[tmp.length - 1].length() - 1);
                if (!nodePath.equals(registrySession.getWorkspaceRootPath())) {
                    abspath = nodePath + "/" + interm_path;
                } else {
                    abspath = nodePath + interm_path;
                }
                if (registrySession.getItem(abspath) instanceof Property) {
                    isAPropertyPath = true;
                }
            } else if (hasProperty(s)) {
                isAPropertyPath = true;
            }
        } catch (PathNotFoundException e) {
            throw new PathNotFoundException("No such path exists to add the Item to " + abspath);

        } catch (RepositoryException e) {
            throw new RepositoryException("Error occurred while adding the Item to the " + abspath);
        }
        return isAPropertyPath;
    }

    public Node addNode(String s, String s1) throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, RepositoryException {
        // Validate whether the node type is valid
        registrySession.getWorkspace().getNodeTypeManager().getNodeType(s1);

        // Check if node aleady exists
        if (hasNode(s)) {
            throw new ItemExistsException("Node " + s + " already exists");
        }
        // check if tries to add a node to a property path.
        if (validateNodePathRefersToAPropertyPath(s)) {
            throw new ConstraintViolationException("Relative path cannot be a property " + s);
        }
        //check Intermediate nodes existance before adding a node
        if (!validateIntermediateNodeExistance(s)) {
            throw new PathNotFoundException("Intermediate nodes doesn't exists under " + s);
        }

        String absPath = "";
        String ntName = "";
//        List<String> versionList = new ArrayList<String>();
//        versionList.add("jcr:default");

        if ((nodePath != null) && (nodePath.equals(registrySession.getWorkspaceRootPath()))) {
            absPath = nodePath + s;
        } else {
            absPath = nodePath + "/" + s;
        }

        RegistryNode subNode = new RegistryNode(absPath, registrySession);
        ntName = s1;
        CollectionImpl subCollection = null;

//        if(ntName.startsWith("mix")) {
//         throw new ConstraintViolationException("Cannot add mixin type nodes, instead do addmixin for " + s1);
//        }

        try {
            subCollection = (CollectionImpl) registrySession.getUserRegistry().newCollection();
            subCollection.setDescription(ntName);   // sets the node type
            subCollection.setProperty("jcr:uuid", absPath);  //Here we use node's path as its identifier

            if (ntName.equals("mix:simpleVersionable") || ntName.equals("mix:versionable")) {
                subCollection.setProperty("jcr:checkedOut", "true");
                subCollection.setProperty("jcr:isCheckedOut", "true");
                subCollection.setProperty("jcr:frozenPrimaryType",ntName);
            }
            if (ntName.startsWith("mix")) {
                addMixin(s1);
            }

            registrySession.getUserRegistry().put(absPath, subCollection);
            subNode.setCollection(absPath);


        } catch (RegistryException e) {

            String msg = "failed to resolve the path of the given node " + this;
            log.debug(msg);
            throw new PathNotFoundException(msg, e);
        }
        subNode.setPrimaryType(s1);
        subNode.nodeType = (RegistryNodeType) (registrySession.getWorkspace().getNodeTypeManager().getNodeType(s1));
        isModified = true;
        return subNode;
    }

//    private Node loadCollectionPropertiesToNode(){
//    }

    public void orderBefore(String s, String s1) throws UnsupportedRepositoryOperationException, VersionException, ConstraintViolationException, ItemNotFoundException, LockException, RepositoryException {


    }

    public Property setProperty(String s, Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        RegistryJCRItemOperationUtil.checkRetentionPolicy(registrySession,getPath());
        RegistryJCRItemOperationUtil.checkRetentionHold(registrySession, getPath());

        registrySession.sessionPending();
        validatePropertyModifyPrivilege(s);

        Resource res = null;
        if ((resource != null) && (value != null)) {

            if (value.getType() == 1) {

                try {
                    res = registrySession.getUserRegistry().newResource();
                    res.setContent(value.getString());
                    res.setProperty("registry.jcr.property.type", "value_type");

                    registrySession.getUserRegistry().put(nodePath + "/" + s, res);
                    property = new RegistryProperty(nodePath + "/" + s, registrySession, s,value);

                } catch (RegistryException e) {
                    String msg = "failed to resolve the path of the given node " + this;
                    log.debug(msg);
                    throw new RepositoryException(msg, e);
                }
            }
            isModified = true;
//            TODO call setproperty based on value.getType and support all value types;
            return property;

        } else if (value == null) {

            try {
                Resource resource = registrySession.getUserRegistry().get(nodePath);
                resource.removeProperty(s);
                registrySession.getUserRegistry().put(nodePath, resource);
                isModified = true;
            } catch (RegistryException e) {
                String msg = "failed to resolve the path of the given node or violation of repository syntax " + this;
                log.debug(msg);
                throw new RepositoryException(msg, e);
            }

            return null;
        } else {
            return null;
        }
    }

    public Property setProperty(String s, Value value, int i) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        RegistryJCRItemOperationUtil.checkRetentionPolicy(registrySession,getPath());
        RegistryJCRItemOperationUtil.checkRetentionHold(registrySession, getPath());

        validatePropertyModifyPrivilege(s);

        isModified = true;
        //TODO consider TYPE "i" when set the value
        return setProperty(s, value);
    }

    public Property setProperty(String s, Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        RegistryJCRItemOperationUtil.checkRetentionPolicy(registrySession,getPath());
        RegistryJCRItemOperationUtil.checkRetentionHold(registrySession, getPath());

        registrySession.sessionPending();
        validatePropertyModifyPrivilege(s);

        List<String> properties = new ArrayList<String>();   //Here we can set the Values which has String   content only.

        if (values != null) {

            for (Value val : values) {
                if (val != null) {
                    properties.add(val.getString());
                }
            }
            Resource res = null;
            try {
                res = registrySession.getUserRegistry().newResource();
                res.setProperty(s, properties);
                res.setProperty("registry.jcr.property.type", "values_type");
                registrySession.getUserRegistry().put(nodePath + "/" + s, res);
                property = new RegistryProperty(nodePath + "/" + s, registrySession, s,values);

            } catch (RegistryException e) {
                String msg = "failed to resolve the path of the given node or violation of repository syntax " + this;
                log.debug(msg);
                throw new RepositoryException(msg, e);
            }
            isModified = true;
            return property;

        } else {
            return null;
        }
    }


    public Property setProperty(String s, Value[] values, int i) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        RegistryJCRItemOperationUtil.checkRetentionPolicy(registrySession,getPath());
        RegistryJCRItemOperationUtil.checkRetentionHold(registrySession, getPath());

        validatePropertyModifyPrivilege(s);
        isModified = true;
        return setProperty(s, values);
    }

    private Property setPropertyToNode(String s, String[] strings) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        RegistryJCRItemOperationUtil.checkRetentionPolicy(registrySession,getPath());
        RegistryJCRItemOperationUtil.checkRetentionHold(registrySession, getPath());

        isModified = true;
        return RegistryJCRItemOperationUtil.persistStringPropertyValues(
                   registrySession,nodePath,s,strings);
    }

    public Property setProperty(String s, String[] strings) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        RegistryJCRItemOperationUtil.checkRetentionPolicy(registrySession,getPath());
        RegistryJCRItemOperationUtil.checkRetentionHold(registrySession, getPath());

        registrySession.sessionPending();
        validatePropertyModifyPrivilege(s);

        if (hasProperty(s) && (!getProperty(s).isMultiple())) {
            throw new ValueFormatException("Cannot overrride the initial value format " + s);
        } else {
            isModified = true;
            return setPropertyToNode(s, strings);
        }

    }

    public Property setProperty(String s, String[] strings, int i) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        RegistryJCRItemOperationUtil.checkRetentionPolicy(registrySession,getPath());
        RegistryJCRItemOperationUtil.checkRetentionHold(registrySession, getPath());

        validatePropertyModifyPrivilege(s);

        isModified = true;
        return setProperty(s, strings);
    }

    public Property setProperty(String s, String s1) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        RegistryJCRItemOperationUtil.checkRetentionPolicy(registrySession,getPath());
        RegistryJCRItemOperationUtil.checkRetentionHold(registrySession, getPath());

        registrySession.sessionPending();
        validatePropertyModifyPrivilege(s);

        if ((s1 != null) && s1.contains("{")) {
            s1 = RegistryJCRItemOperationUtil.replaceNameSpacePrefixURIS(s1);
        }

        // Restrict manually set jcr:primary and mix node types
        if ((s != null) && (s.equals("jcr:primaryType") || s.equals("jcr:mixinTypes"))) {
            throw new ConstraintViolationException("Cannot mannually set jcr:primaryType/jcr:mixinTypes node types");
        }

        try {
            resource = (CollectionImpl)registrySession.getUserRegistry().get(nodePath);
            if (s1 != null) {
                List<String> lis = new ArrayList<String>();
                lis.add(s1);
                resource.setProperty(s, lis);
                registrySession.getUserRegistry().put(nodePath, resource);
            } else {
//                resource = registrySession.getUserRegistry().get(nodePath);
                resource.removeProperty(s);
                registrySession.getUserRegistry().put(nodePath, resource);
            }

        } catch (RegistryException e) {
            String msg = "failed to resolve the path of the given node or violation of repository syntax " + this;
            log.debug(msg);
            throw new RepositoryException(msg, e);
        }

        property = new RegistryProperty(resource.getPath(), registrySession, s,s1);
        isModified = true;
        return property;
    }

    public Property setProperty(String s, String s1, int i) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        RegistryJCRItemOperationUtil.checkRetentionPolicy(registrySession,getPath());
        RegistryJCRItemOperationUtil.checkRetentionHold(registrySession, getPath());

        validatePropertyModifyPrivilege(s);
        isModified = true;
        return setProperty(s, s1);
    }

    public Property setProperty(String s, InputStream inputStream) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        RegistryJCRItemOperationUtil.checkRetentionPolicy(registrySession,getPath());
        RegistryJCRItemOperationUtil.checkRetentionHold(registrySession, getPath());

        registrySession.sessionPending();
        validatePropertyModifyPrivilege(s);

        Resource res = null;
        try {
            res = registrySession.getUserRegistry().newResource();
            if (inputStream != null) {
                res.setContentStream(inputStream);
                res.setProperty("registry.jcr.property.type", "input_stream");
                registrySession.getUserRegistry().put(nodePath + "/" + s, res);
                property = new RegistryProperty(nodePath + "/" + s, registrySession, s,inputStream);
            }
        } catch (RegistryException e) {
            String msg = "failed to resolve the path of the given node or violation of repository syntax " + this;
            log.debug(msg);
            throw new RepositoryException(msg, e);
        }

        isModified = true;
        return property;
    }

    public Property setProperty(String s, Binary binary) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        RegistryJCRItemOperationUtil.checkRetentionPolicy(registrySession,getPath());
        RegistryJCRItemOperationUtil.checkRetentionHold(registrySession, getPath());

        //TODO finish Impl of set binary type
        registrySession.sessionPending();
        validatePropertyModifyPrivilege(s);

        //still we can return a property.But we actually have only string to set
        property = new RegistryProperty(this.resource.getPath(), registrySession, s,binary);
        isModified = true;
        return property;
    }

    public Property setProperty(String s, boolean b) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        RegistryJCRItemOperationUtil.checkRetentionPolicy(registrySession,getPath());
        RegistryJCRItemOperationUtil.checkRetentionHold(registrySession, getPath());

        registrySession.sessionPending();
        validatePropertyModifyPrivilege(s);

        Resource res = null;
        try {
            res = registrySession.getUserRegistry().newResource();
            res.setContent(String.valueOf(b));
            res.setProperty("registry.jcr.property.type", "boolean");
            registrySession.getUserRegistry().put(nodePath + "/" + s, res);
            property = new RegistryProperty(nodePath + "/" + s, registrySession, s,b);

        } catch (RegistryException e) {
            String msg = "failed to resolve the path of the given node or violation of repository syntax " + this;
            log.debug(msg);
            throw new RepositoryException(msg, e);
        }

        isModified = true;
//        property.setValue(b);
        return property;
    }

    public Property setProperty(String s, double v) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        RegistryJCRItemOperationUtil.checkRetentionPolicy(registrySession,getPath());
        RegistryJCRItemOperationUtil.checkRetentionHold(registrySession, getPath());

        registrySession.sessionPending();
        validatePropertyModifyPrivilege(s);

        Resource res = null;
        try {
            res = registrySession.getUserRegistry().newResource();
            res.setContent(String.valueOf(v));
            res.setProperty("registry.jcr.property.type", "double");
            registrySession.getUserRegistry().put(nodePath + "/" + s, res);
            property = new RegistryProperty(nodePath + "/" + s, registrySession, s,v);

        } catch (RegistryException e) {
            String msg = "failed to resolve the path of the given node or violation of repository syntax " + this;
            log.debug(msg);
            throw new RepositoryException(msg, e);
        }
        isModified = true;
//        property.setValue(v);
        return property;
    }

    public Property setProperty(String s, BigDecimal bigDecimal) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        RegistryJCRItemOperationUtil.checkRetentionPolicy(registrySession,getPath());
        RegistryJCRItemOperationUtil.checkRetentionHold(registrySession, getPath());

        registrySession.sessionPending();
        validatePropertyModifyPrivilege(s);

        if (bigDecimal != null) {

            Resource res = null;
            try {
                res = registrySession.getUserRegistry().newResource();
                res.setContent(bigDecimal.toString());
                res.setProperty("registry.jcr.property.type", "big_decimal");
                registrySession.getUserRegistry().put(nodePath + "/" + s, res);
                property = new RegistryProperty(nodePath + "/" + s, registrySession, s,bigDecimal);

            } catch (RegistryException e) {
                String msg = "failed to resolve the path of the given node or violation of repository syntax " + this;
                log.debug(msg);
                throw new RepositoryException(msg, e);
            }
            isModified = true;
            return property;

        } else {
            isModified = true;
            return null;
        }
    }

    private void validatePropertyModifyPrivilege(String name) throws RepositoryException {
     if(RegistryJCRSpecificStandardLoderUtil.isSessionReadOnly(registrySession.getUserID())
             && (hasProperty(name))) {
        throw new AccessDeniedException("A read only session must not be allowed to modify a property value");
     }
    }


    public Property setProperty(String s, long l) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        RegistryJCRItemOperationUtil.checkRetentionPolicy(registrySession,getPath());
        RegistryJCRItemOperationUtil.checkRetentionHold(registrySession, getPath());

        registrySession.sessionPending();
        validatePropertyModifyPrivilege(s);

        Resource res = null;
        try {
            res = registrySession.getUserRegistry().newResource();
            res.setContent(String.valueOf(l));
            res.setProperty("registry.jcr.property.type", "long");
            registrySession.getUserRegistry().put(nodePath + "/" + s, res);
            property = new RegistryProperty(nodePath + "/" + s, registrySession, s,l);

        } catch (RegistryException e) {
            String msg = "failed to resolve the path of the given node or violation of repository syntax " + this;
            log.debug(msg);
            throw new RepositoryException(msg, e);
        }
        isModified = true;
        return property;
    }

    public Property setProperty(String s, Calendar calendar) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        RegistryJCRItemOperationUtil.checkRetentionPolicy(registrySession,getPath());
        RegistryJCRItemOperationUtil.checkRetentionHold(registrySession, getPath());

        registrySession.sessionPending();
        validatePropertyModifyPrivilege(s);
         String _propertyPath = nodePath + "/" + s;
        try {
        if (calendar != null) {
            Resource res = null;
            Property _property;
                res = registrySession.getUserRegistry().newResource();
                res.setContent(String.valueOf(calendar.getTimeInMillis()));
                res.setProperty("registry.jcr.property.type", "calendar");
                registrySession.getUserRegistry().put(_propertyPath, res);
                _property = new RegistryProperty(_propertyPath, registrySession, s,calendar);
            isModified = true;
            return _property;

        } else {
            isModified = true;
            registrySession.getUserRegistry().delete(_propertyPath);
            return null;
        }
        } catch (RegistryException e) {
            String msg = "failed to resolve the path of the given node or violation of repository syntax " + this;
            log.debug(msg);
            throw new RepositoryException(msg, e);
        }

    }

    public Property setProperty(String s, Node node) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        RegistryJCRItemOperationUtil.checkRetentionPolicy(registrySession,getPath());
        RegistryJCRItemOperationUtil.checkRetentionHold(registrySession, getPath());

       //TODO finish impl set NODE
        registrySession.sessionPending();
        validatePropertyModifyPrivilege(s);

        if (node != null) {
            property = new RegistryProperty(this.resource.getPath(), registrySession, s,node);
        }
        isModified = true;
        return property;

    }

    public Node getNode(String s) throws PathNotFoundException, RepositoryException {
        //s-rel path
        String abs = "";

        if (!(nodePath.endsWith("/")) && (!(s.startsWith("/")))) {
            abs = nodePath + "/" + s;
        } else {
            abs = nodePath + s;
        }
        RegistryNode subNode = null;
        try {
            if (registrySession.getUserRegistry().resourceExists(abs)) {
                subNode = new RegistryNode(abs, registrySession);
                subNode.resource = (CollectionImpl) registrySession.getUserRegistry().get(abs);
                subNode.nodeType = (RegistryNodeType) (registrySession.getWorkspace().getNodeTypeManager().getNodeType(subNode.resource.getDescription()));

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

    public NodeIterator getNodes() throws RepositoryException {

        List<Node> nodes = new ArrayList<Node>();
        CollectionImpl tempCollection = null;
        try {

            if (registrySession.getUserRegistry().get(nodePath) instanceof CollectionImpl) {
                tempCollection = (CollectionImpl) registrySession.getUserRegistry().get(nodePath);
                String[] children = tempCollection.getChildren();
                for (int i = 0; i < children.length; i++) {
                    if(isARegistryCollection(children[i])) {
                        if(RegistryJCRItemOperationUtil.isSystemConfigNode(children[i])) {
                            continue;
                        }
                        nodes.add(registrySession.getNode(children[i]));
                    }
                }
            }
        } catch (RegistryException e) {
            String msg = "failed to resolve the path of the given node or violation of repository syntax " + this;
            log.debug(msg);
            throw new RepositoryException(msg, e);
        }
        RegistryNodeIterator nodeIterator = new RegistryNodeIterator(nodes);
        return (NodeIterator) nodeIterator;
    }

    private boolean isARegistryCollection(String path) throws RegistryException {
      return registrySession.getUserRegistry().get(path)  instanceof CollectionImpl;
    }

    public NodeIterator getNodes(String s) throws RepositoryException {
        List<Node> nodes = new ArrayList<Node>();
        try {
            CollectionImpl coll = (CollectionImpl) registrySession.getUserRegistry().get(nodePath);
            String[] childpaths = coll.getChildren();
            for (int i = 0; i < childpaths.length; i++) {
                if(RegistryJCRItemOperationUtil.isSystemConfigNode(childpaths[i])){
                 continue;
                }
                Node node = new RegistryNode(childpaths[i], registrySession);
                nodes.add(node);
            }

        } catch (RegistryException e) {
            String msg = "failed to resolve the path of the given node or violation of repository syntax " + this;
            log.debug(msg);
            throw new RepositoryException(msg, e);
        }
        return new RegistryNodeIterator(nodes);
    }

    public NodeIterator getNodes(String[] strings) throws RepositoryException {


        return null;
    }

//   private List<String> filterIsMultipleAttr(List<String> list) {
//       List<String> tmp = new ArrayList<String>();
//       for(String val : list) {
//         if((val != null) && !val.equals("wso2.system.property.value.ismultiple")) {
//           tmp.add(val);
//         }
//       }
//       return tmp;
//   }

    public Property getProperty(String s) throws PathNotFoundException, RepositoryException {

        String absPath = nodePath + "/" + s;
        String[] tempArr;
        String tempPath = absPath;
        tempArr = tempPath.split("/");
        tempPath = tempPath.substring(0, (tempPath.length()) - (tempArr[tempArr.length - 1].length()) - 1);

        RegistryProperty regProp = null;
        String prop = "";
        List<String> propList = null;
        String propName = tempArr[tempArr.length - 1];
        ResourceImpl res = null;
        boolean resFlag = false;
        String propQName = tempArr[tempArr.length - 1];

        try {
            CollectionImpl collecImpl = (CollectionImpl)registrySession.getUserRegistry().get(tempPath);

//            regProp = new RegistryProperty((CollectionImpl)registrySession.getUserRegistry().get(tempPath), registrySession, tempArr[tempArr.length - 1]);

            if ((registrySession.getUserRegistry().resourceExists(absPath))
                    && (registrySession.getUserRegistry().get(absPath) != null)) {
                res = (ResourceImpl) registrySession.getUserRegistry().get(absPath);
                resFlag = true;
            }

            if ((!resFlag) && (registrySession.getUserRegistry().resourceExists(tempPath))
                           && (registrySession.getUserRegistry().get(tempPath) != null)) {

                propList = registrySession.getUserRegistry().get(tempPath).getPropertyValues(propName); // remove added isMultiple value for the property

                if ((propList != null) && (propList.size() == 1) && !isSystemMultiValuedProperty(propName)) {

                    prop = propList.get(0);
                    regProp = new RegistryProperty(collecImpl.getPath(), registrySession, propQName,prop);
//                    regProp.setValue(prop);

                } else if (propList != null) {

                    String[] arr = new String[propList.size()];
                    int i = 0;

                    for (Object ob : propList) {

                        arr[i] = ob.toString();
                        i++;
                    }
                    regProp = new RegistryProperty(collecImpl.getPath(), registrySession, propQName,arr);
//                    regProp.setValue(arr);

                } else  {
                    throw new PathNotFoundException("No such property exists at given path " + absPath);
                }


            } else if (res != null) {

                if ((res.getProperty("registry.jcr.property.type") != null)
                        && (res.getProperty("registry.jcr.property.type").equals("input_stream"))) {

                    regProp = new RegistryProperty(res.getPath(), registrySession, propQName,res.getContentStream());
//                    regProp.setValue(res.getContentStream());

                } else if ((res.getProperty("registry.jcr.property.type") != null)
                        && (res.getProperty("registry.jcr.property.type").equals("values_type"))) {
                    List<String> valuesProp = res.getPropertyValues(propName);
                    if (valuesProp != null) {
                        int i = 0;
                        Value[] values = new RegistryValue[valuesProp.size()];

                        for (int j = 0; j < values.length; j++) {

                            values[j] = new RegistryValue(valuesProp.get(j));

                        }
                        regProp = new RegistryProperty(res.getPath(), registrySession, propQName,values);
//                        regProp.setValue(values);
                    }
                } else if (res.getContent() instanceof String) {
                    prop = res.getContent().toString();

                } else if (res.getContent() instanceof byte[]) {
                    prop = RegistryUtils.decodeBytes((byte[]) res.getContent());

                } else if (prop.equals("")) {

                    throw new PathNotFoundException();
                }


                regProp = RegistryJCRItemOperationUtil.getRegistryProperty(
                          res.getProperty("registry.jcr.property.type"), prop, res,propQName,registrySession);
                resFlag = false;
            }

        } catch (RegistryException e) {
            String msg = "failed to resolve the path of the given node or violation of repository syntax " + this;
            log.debug(msg);
            throw new RepositoryException(msg, e);
        }
        return regProp;
    }

    private boolean isSystemMultiValuedProperty(String name){
        //TODO add system specific multivalued values in future
        if(name.equals("jcr:mixinTypes")) {
          return true;
        } else {
          return false;
        }

    }


    public PropertyIterator getProperties() throws RepositoryException {

        Set<String> propNamesList = new HashSet<String>();
        Set<Property> properties = new HashSet<Property>();
        Resource resource = null;
        try {
            resource = registrySession.getUserRegistry().get(nodePath);

            if (resource instanceof org.wso2.carbon.registry.core.Collection) {
                String[] childPaths = ((org.wso2.carbon.registry.core.Collection) resource).getChildren();

                for (int i = 0; i < childPaths.length; i++) {
                    Resource res = registrySession.getUserRegistry().get(childPaths[i]);

                    if ((res instanceof ResourceImpl)
                             && (res.getProperty("registry.jcr.property.type") != null)) {
                        String[] temp = childPaths[i].split("/");
                        propNamesList.add(temp[temp.length - 1]);

                    }
                }
            }

            Properties propyList = resource.getProperties();
            Enumeration en = propyList.propertyNames();
            while (en.hasMoreElements()) {
                String pName = en.nextElement().toString();
                if ((pName != null) && (!isImplicitProperty(pName))) {
                    propNamesList.add(pName);

                }
            }

            Iterator it = propNamesList.iterator();
            while (it.hasNext()) {
                properties.add(getProperty(it.next().toString()));
            }
        } catch (Exception e) {
            String msg = "failed to resolve the path of the given node or violation of repository syntax " + this;
            log.debug(msg);
            throw new RepositoryException(msg, e);
        }
        RegistryPropertyIterator propertyIterator = new RegistryPropertyIterator(properties, this);
        return propertyIterator;

    }

    public PropertyIterator getProperties(String s) throws RepositoryException {

        boolean isMatch = false;
        Properties propyList = resource.getProperties();
        Iterator nameIt = getCollectionProperties(propyList).iterator();
        Set propValList = new HashSet();

        String regex = s + "*";
        String input = "";

        while (nameIt.hasNext()) {
            input = nameIt.next().toString();
            isMatch = Pattern.matches(regex, input);
            propValList.add(resource.getProperty(input));
        }
        RegistryPropertyIterator propertyIterator = new RegistryPropertyIterator(propValList, this);
        return propertyIterator;
    }

    public PropertyIterator getProperties(String[] strings) throws RepositoryException {
        boolean isMatch = false;

        Properties propyList = resource.getProperties();
        Set<String> propNames = getCollectionProperties(propyList);
        Iterator nameIt = propNames.iterator();
        Set propValList = new HashSet();

        String[] regex = new String[strings.length];
        String input = "";
        String reg = "";

        while (nameIt.hasNext()) {

            input = nameIt.next().toString();
            for (int i = 0; i < regex.length; i++) {
                reg = regex[i] + "*";
                isMatch = Pattern.matches(reg, input);
                if (isMatch) break;
            }
            propValList.add(resource.getProperty(input));
        }

        RegistryPropertyIterator propertyIterator = new RegistryPropertyIterator(propValList, this);
        return propertyIterator;
    }

    public Item getPrimaryItem() throws ItemNotFoundException, RepositoryException {
        Item item;
        if (nodeType.getPrimaryItemName() != null) {
            String name = nodeType.getPrimaryItemName();
            try {
                item = getNode(name);
            } catch (PathNotFoundException e) {
                item = addNode(name, nodeType.getName());
            }
        } else {
            throw new ItemNotFoundException("Primary item not found ");
        }
        return item;

    }

    public String getUUID() throws UnsupportedRepositoryOperationException, RepositoryException {
        List<String> list = resource.getPropertyValues("jcr:mixinTypes");
        if ((list != null) && list.contains("mix:referenceable")) {
            return resource.getProperty("jcr:uuid");
        } else {
            throw new UnsupportedRepositoryOperationException("To acquire the uuid, the nodetype must have mix:referenceable mixin ");
        }
    }

    public String getIdentifier() throws RepositoryException {
        return nodePath;
    }

    public int getIndex() throws RepositoryException {  // TODO
//        getIndex() of a node without same name siblings must return 1
        return 0;
    }

    public PropertyIterator getReferences() throws RepositoryException {

        return getProperties();

    }

    public PropertyIterator getReferences(String s) throws RepositoryException {
        return getProperties(s);
    }

    public PropertyIterator getWeakReferences() throws RepositoryException {
        return getProperties();
    }

    public PropertyIterator getWeakReferences(String s) throws RepositoryException {
        return getProperties(s);
    }

    public boolean hasNode(String s) throws RepositoryException {    //s-relative path
        String subnodepath = nodePath + s;
        boolean hasNode = false;
        if (nodePath.equals(registrySession.getWorkspaceRootPath())) {
            subnodepath = nodePath + s;
        } else {
            subnodepath = nodePath + "/" + s;
        }
        try {
            if (registrySession.getUserRegistry().resourceExists(subnodepath)) {

                if ((registrySession.getUserRegistry().get(subnodepath)) instanceof CollectionImpl) {
                    hasNode = true;
                }
            }
        } catch (RegistryException e) {
            String msg = "failed to resolve the path of the given node or violation of repository syntax " + this;
            log.error(msg,e);
            e.printStackTrace();
            throw new RepositoryException(msg, e);
        }
        return hasNode;
    }

    public boolean hasProperty(String s) throws RepositoryException {

        boolean hasProperty = false;
        String absPath = nodePath + "/" + s;

        String[] tempArr;
        String tempPath = absPath;
        tempArr = tempPath.split("/");
        tempPath = tempPath.substring(0, (tempPath.length()) - (tempArr[tempArr.length - 1].length()) - 1);
        String propName = tempArr[tempArr.length - 1];

        try {

            if (registrySession.getUserRegistry().resourceExists(tempPath)) {
                Resource resource = registrySession.getUserRegistry().get(tempPath);

                if (resource.getProperty(propName) != null) {
                    hasProperty = true;
                }

            }
            if (registrySession.getUserRegistry().resourceExists(absPath) &&
                (!((registrySession.getUserRegistry().get(absPath)) instanceof CollectionImpl))) {
                    hasProperty = true;
            }

        } catch (RegistryException e) {
            String msg = "failed to resolve the path of the given node or violation of repository syntax " + this;
            log.debug(msg);
            throw new RepositoryException(msg, e);
        }
        return hasProperty;
    }

    public boolean hasNodes() throws RepositoryException {

        boolean hasNodes = true;
        CollectionImpl collec = null;
        try {

            if ((registrySession.getUserRegistry().resourceExists(nodePath))
                    && ((registrySession.getUserRegistry().get(nodePath)
                    instanceof CollectionImpl))) {
                collec = (CollectionImpl) registrySession.getUserRegistry().get(nodePath);
            }

            if (collec != null) {
                String[] children = collec.getChildren();
                if (children != null) {
                    if (children.length == 0) hasNodes = false;
                }
            }

        } catch (RegistryException e) {
            e.printStackTrace();
        }

        return hasNodes;
    }

    public boolean hasProperties() throws RepositoryException {

        boolean hasProperties = true;
        Resource resource = null;
        try {
            resource = registrySession.getUserRegistry().get(nodePath);

        } catch (RegistryException e) {

            String msg = "failed to resolve the path of the given node or violation of repository syntax " + this;
            log.debug(msg);
            throw new RepositoryException(msg, e);

        }

        Properties propyList = resource.getProperties();
        Set<String> propNames = getCollectionProperties(propyList);

        if (propNames.size() == 0) {

            hasProperties = false;
        }

        return hasProperties;
    }

    public NodeType getPrimaryNodeType() throws RepositoryException {

        return getNodetype();

    }

    public NodeType[] getMixinNodeTypes() throws RepositoryException {
        List<NodeType> nodeTypeList = new ArrayList<NodeType>();
        try {
            Resource resource = registrySession.getUserRegistry().get(nodePath);
            List<String> mixNames = resource.getPropertyValues("jcr:mixinTypes");
            if (mixNames != null) {
                for (String name : mixNames) {
                    NodeType nt = registrySession.getWorkspace().getNodeTypeManager().getNodeType(name);
                    nodeTypeList.add(nt);
                }
            }

        } catch (RegistryException e) {
            throw new RepositoryException("Error while getting  mix node types from registry " + e.getMessage());
        }

        if (nodeTypeList.size() == 0) {
            return new NodeType[0];
        } else {
            return nodeTypeList.toArray(new NodeType[0]);
        }
    }

    public boolean isNodeType(String s) throws RepositoryException {
        boolean isNodeType = false;

        if(getNodetype().isNodeType(s)){
            return true;
        }
        if ((resource.getPropertyValues("jcr:mixinTypes") != null) && (resource.getPropertyValues("jcr:mixinTypes").contains(s))) {
            return true;
        }

    return false;
    }

    public void setPrimaryType(String s) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {

        // Check if node type already exists
        registrySession.getWorkspace().getNodeTypeManager().getNodeType(s);

//        if(s!= null && s.startsWith("mix")) {
//         throw  new ConstraintViolationException("Cannpot set mixin as primary types");
//        }
        try {

            Resource resource = registrySession.getUserRegistry().get(nodePath);
            resource.setProperty("jcr:primaryType", s);
            registrySession.getUserRegistry().put(nodePath, resource);

        } catch (RegistryException e) {
            String msg = "failed to resolve the path of the given node or violation of repository syntax " + this;
            log.debug(msg);
            throw new RepositoryException(msg, e);
        }


    }
    //TODO overall, always do a registry get on a resource and do stuff other than using the ref
    public void addMixin(String s) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        try {
            registrySession.getWorkspace().getNodeTypeManager().getNodeType(s);
//            Resource resource = registrySession.getUserRegistry().get(nodePath);

            if (resource.getPropertyValues("jcr:mixinTypes") == null) {
                List list = new ArrayList();
                list.add(s);
                resource.setProperty("jcr:mixinTypes", list);
            } else {
                resource.getPropertyValues("jcr:mixinTypes").add(s);
            }

            if (s.equals("mix:simpleVersionable") || s.equals("mix:versionable")) {
                resource.setProperty("jcr:checkedOut", "true");
                resource.setProperty("jcr:isCheckedOut", "true");
            }

            if(s.equals("mix:referenceable")) {
             resource.setProperty("jcr:frozenUuid",nodePath);
            }

//                   validateNTPropertyDefs();
            registrySession.getUserRegistry().put(nodePath, resource);

        }


        catch (NoSuchNodeTypeException e) {
            String msg = "No such node type exists " + this;
            log.debug(msg);
            throw new NoSuchNodeTypeException(msg, e);
        } catch (RegistryException e) {
            String msg = "failed to resolve the path of the given node or violation of repository syntax " + this;
            log.debug(msg);
            throw new RepositoryException(msg, e);
        }
        isModified = true;
    }

    private void validateNTPropertyDefs(String  nPath,String nodeType) throws RepositoryException {
        //TODO add looking at nodetype.getPropDefs
//        if(nodeType.equals("mix:simpleVersionable")) {
//        Node node= (Node) registrySession.getItem(nPath);

 }

    public void removeMixin(String s) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        RegistryJCRItemOperationUtil.checkRetentionHold(registrySession, getPath());

        try {
            Resource resource = registrySession.getUserRegistry().get(nodePath);
            if (resource.getPropertyValues("jcr:mixinTypes").contains(s)) {
                resource.getPropertyValues("jcr:mixinTypes").remove(s);
            } else {
                throw new NoSuchNodeTypeException("No such mix node type to remove");
            }
            registrySession.getUserRegistry().put(nodePath, resource);

        } catch (RegistryException e) {
            String msg = "failed to resolve the path of the given node or violation of repository syntax " + this;
            log.debug(msg);
            throw new RepositoryException(msg, e);
        }
       isModified = true;
    }

    public boolean canAddMixin(String s) throws NoSuchNodeTypeException, RepositoryException {
        try {
            registrySession.getWorkspace().getNodeTypeManager().getNodeType(s);
        } catch (NoSuchNodeTypeException e) {
            throw new NoSuchNodeTypeException("No such mix node type exists " + e.getMessage());
        }

        return true; // TODO  :check other facts for add a mix : [returns true for testing versioning]
    }

    public NodeDefinition getDefinition() throws RepositoryException {

        return new RegistryNodeDefinition();
    }

    public Version checkin() throws VersionException, UnsupportedRepositoryOperationException, InvalidItemStateException, LockException, RepositoryException {
        return registrySession.getWorkspace().getVersionManager().checkin(nodePath);
    }

    public void checkout() throws UnsupportedRepositoryOperationException, LockException, ActivityViolationException, RepositoryException {
        registrySession.getWorkspace().getVersionManager().checkout(nodePath);

    }

    public void doneMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {

    }

    public void cancelMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {

    }

    public void update(String s) throws NoSuchWorkspaceException, AccessDeniedException, LockException, InvalidItemStateException, RepositoryException {
         if(!RegistryJCRItemOperationUtil.isWorkspaceExists(registrySession,s)) {
                 throw new NoSuchWorkspaceException("No such workspace named "+s);
         }
        //Else  TODO: DO UPDATE
    }

    public NodeIterator merge(String s, boolean b) throws NoSuchWorkspaceException, AccessDeniedException, MergeException, LockException, InvalidItemStateException, RepositoryException {

        return null;
    }

    public String getCorrespondingNodePath(String s) throws ItemNotFoundException, NoSuchWorkspaceException, AccessDeniedException, RepositoryException {

        String npath = null;
        Set sessions = registrySession.getRepository().getWorkspaces();

        RegistrySession session;
        boolean matchFound = false;
        try {

            for (Object _s : sessions) {
                session = (RegistrySession)_s;
                if (session.getWorkspaceName() != null) {
                    if (session.getWorkspaceName().equals(s)) {
                        npath = session.getUserRegistry().get(nodePath).getPath();
                        matchFound = true;
                    }
                }
            }
            if(!matchFound) {
                throw new NoSuchWorkspaceException("There is no such workspace named " + s);
            }

        } catch (RegistryException e) {
            throw new RepositoryException("Registry level exception " +
                    "occurred while get corresponding Node Path for "+s);
        }
        return npath;
    }

    public NodeIterator getSharedSet() throws RepositoryException {
        return null;
    }

    public void removeSharedSet() throws VersionException, LockException, ConstraintViolationException, RepositoryException {

    }

    public void removeShare() throws VersionException, LockException, ConstraintViolationException, RepositoryException {

    }

    public boolean isCheckedOut() throws RepositoryException {

        return registrySession.getWorkspace().getVersionManager().isCheckedOut(nodePath);
    }

       public void restore(String s, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
       try {
             registrySession.getWorkspace().getVersionManager().
             restore(nodePath, s, true); // Assume: No identifier collision occurs


        }  catch (VersionException e) {
            throw new VersionException("No such version in node's version history" + s);
        } catch (UnsupportedRepositoryOperationException e) {
            throw new UnsupportedRepositoryOperationException("Node type not Versionable : " + s);
        } catch (InvalidItemStateException e) {
            throw new InvalidItemStateException("Invalid Item state: operations are still unsaved");
        }catch (RepositoryException e) {
            throw new RepositoryException("Excepion occurred in registry level while restoring");
        }
    }


    public void restore(Version version, boolean b) throws VersionException, ItemExistsException, InvalidItemStateException, UnsupportedRepositoryOperationException, LockException, RepositoryException {
        try {
             registrySession.getWorkspace().getVersionManager().
             restore(nodePath, version.getName(), true); // Assume: No identifier collision occurs
        }  catch (VersionException e) {
            throw new VersionException("No such version in node's version history" );
        } catch (UnsupportedRepositoryOperationException e) {
            throw new UnsupportedRepositoryOperationException("Node type not Versionable  ");
        } catch (InvalidItemStateException e) {
            throw new InvalidItemStateException("Invalid Item state: operations are still unsaved");
        }catch (RepositoryException e) {
            throw new RepositoryException("Excepion occurred in registry level while restoring");
        }
    }

    public void restore(Version version, String s, boolean b) throws PathNotFoundException, ItemExistsException, VersionException, ConstraintViolationException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
          try {
             registrySession.getWorkspace().getVersionManager().
             restore(getNode(s).getPath(), version.getName(), true); // Assume: No identifier collision occurs
        }  catch (VersionException e) {
            throw new VersionException("No such version in node's version history" + s);
        } catch (UnsupportedRepositoryOperationException e) {
            throw new UnsupportedRepositoryOperationException("Node type not Versionable : " + s);
        } catch (InvalidItemStateException e) {
            throw new InvalidItemStateException("Invalid Item state: operations are still unsaved");
        } catch (RepositoryException e) {
            throw new RepositoryException("Excepion occurred in registry level while restoring");
        }
    }

    public void restoreByLabel(String s, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        registrySession.getWorkspace().getVersionManager().restoreByLabel(nodePath,s,true);
    }

    public VersionHistory getVersionHistory() throws UnsupportedRepositoryOperationException, RepositoryException {

        return registrySession.getWorkspace().getVersionManager().getVersionHistory(nodePath);
    }

    public Version getBaseVersion() throws UnsupportedRepositoryOperationException, RepositoryException {

        return registrySession.getWorkspace().getVersionManager().getBaseVersion(nodePath);
    }

    public Lock lock(boolean b, boolean b1) throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {

        return registrySession.getWorkspace().getLockManager().lock(nodePath, b, b1, 10, this.getName());

    }

    public Lock getLock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, RepositoryException {

        return registrySession.getWorkspace().getLockManager().getLock(nodePath);
    }

    public void unlock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {

        new RegistryLockManager(registrySession).unlock(nodePath);
    }

    public boolean holdsLock() throws RepositoryException {

        return registrySession.getWorkspace().getLockManager().holdsLock(nodePath);
    }

    public boolean isLocked() throws RepositoryException {

        return registrySession.getWorkspace().getLockManager().isLocked(nodePath);
    }

    public void followLifecycleTransition(String s) throws UnsupportedRepositoryOperationException, InvalidLifecycleTransitionException, RepositoryException {

    }

    public String[] getAllowedLifecycleTransistions() throws UnsupportedRepositoryOperationException, RepositoryException {
        return new String[0];
    }

    public String getPath() throws RepositoryException {
        return nodePath;
    }

    public String getName() throws RepositoryException {

        String[] nodeName = nodePath.split("/");  //assume that we give the path including its name when creating a node
        int nameIndex = nodeName.length - 1;
//        if (nameIndex == -1) { // check root node
//            return "";
//        } else {
        return nodeName[nameIndex];
//        }

    }


    public Item getAncestor(int i) throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return registrySession.getItem(RegistryJCRItemOperationUtil
                .getAncestorPathAtGivenDepth(registrySession,getPath(), i));
    }

    public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        Node par = null;
        String parent = "";
        try {

            if (isNodeRoot()) {
                throw new ItemNotFoundException("Node is already root: "+nodePath);
            }
            if (!registrySession.getUserRegistry().resourceExists(nodePath)) {
                throw new InvalidItemStateException();
            } else {
                    parent = ((CollectionImpl) registrySession.getUserRegistry().get(nodePath)).getParentPath();
                    par = (Node)registrySession.getItem(parent);
            }

        } catch (RegistryException e) {
            e.printStackTrace();

        } catch (InvalidItemStateException e) {
            throw new InvalidItemStateException();
        }
        return par;
    }

    private boolean isNodeRoot() throws RegistryException{
            String tmp =  nodePath;
            if(!tmp.endsWith("/")) {
               tmp=tmp+"/";
            }
        if(tmp.equals(registrySession.getWorkspaceRootPath())) {
          return true;
        } else {
           return false;
        }
    }


    public int getDepth() throws RepositoryException {

        if (nodePath.equals(registrySession.getWorkspaceRootPath())) {
            return 0;
        } else {
            return nodePath.split("/").length - 4;
        }
    }

    public RegistrySession getSession() throws RepositoryException {
        return registrySession;
    }

    public boolean isNode() {
        return true;
    }

    public boolean isNew() {
        boolean isNew = false;
        try {
            if (resource != null) {
                isNew = !(resource instanceof Collection) || (((CollectionImpl) resource).getChildren() != null &&
                        ((CollectionImpl) resource).getChildren().length == 0);
            }
        } catch (Exception e) {
            isNew = true;
        }
        return isNew;
    }

    public boolean isModified() {
        return isModified;
    }

    public boolean isSame(Item item) throws RepositoryException {
     return nodePath.equals(item.getPath());
        /*
        The ability to address the same piece of data via more than one path is a
        common feature of many content storage systems. In JCR this feature is
        supported through shareable nodes.
        */
//        return this.item.isSame(item);
    }

    public void accept(ItemVisitor itemVisitor) throws RepositoryException {

    }

    public void save() throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
        try {
            if (!(registrySession.getUserRegistry().resourceExists(nodePath))) {
                throw new InvalidItemStateException("Unable to save the node at" + nodePath);
            }
        } catch (RegistryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public void refresh(boolean b) throws InvalidItemStateException, RepositoryException {
        try {
            if (!(registrySession.getUserRegistry().resourceExists(nodePath))) {
                throw new InvalidItemStateException("Cannot refresh on non existing nodes ..!! ");
            }
        } catch (RegistryException e) {
            throw new RepositoryException("Problem occurred while refresh on node " + nodePath);
        }
    }

    public void remove() throws VersionException, LockException, ConstraintViolationException, AccessDeniedException, RepositoryException {
        //TODO  if nodetype is a mandatory one should throw ConstraintViolationException

//           A read only session must not be allowed to remove a node

        RegistryJCRItemOperationUtil.validateReadOnlyItemOpr(registrySession);

        try {
            RegistryJCRItemOperationUtil.checkRetentionPolicyWithParent(registrySession,getPath());
            RegistryJCRItemOperationUtil.checkRetentionHoldWithParent(registrySession,getPath());

            if (registrySession.getUserRegistry().resourceExists(nodePath)) {
                registrySession.removeItem(nodePath);
            } else {
                throw new InvalidItemStateException("Node " + nodePath + " has already removed from another session");
            }
        } catch (RegistryException e) {
            log.error("Error occured while removing the node at" + nodePath);
        }
        isRemoved = true;
    }

    private boolean isImplicitProperty(String s) {

        if ((s != null) && (RegistryJCRSpecificStandardLoderUtil.getimplicitPropertiyNames().contains(s))) {

            return true;
        } else {

            return false;
        }
    }

    private Set getCollectionProperties(Properties propyList) {

        Set propNamesList = new HashSet();

        Enumeration en = propyList.propertyNames();
        while (en.hasMoreElements()) {
            String pName = en.nextElement().toString();
            if ((pName != null) && (!isImplicitProperty(pName))) {
                propNamesList.add(pName);

            }
        }
        return propNamesList;
    }
}
