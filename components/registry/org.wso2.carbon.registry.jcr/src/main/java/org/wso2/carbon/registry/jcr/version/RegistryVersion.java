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

package org.wso2.carbon.registry.jcr.version;

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
import java.util.Calendar;


public class RegistryVersion implements Version {

    private String versionName = "";
    private long createdTime = 0;
    private VersionHistory vh;
    private String fNodepath = "";
    private Session session;

    public RegistryVersion(String versionId, long createdTime, String nodepath, Session session) {
        this.versionName = versionId;
        this.createdTime = createdTime;
        this.fNodepath = nodepath;
        this.session = session;
    }

    public void setVersionHistory(VersionHistory vh) {
        this.vh = vh;
    }

    public VersionHistory getContainingHistory() throws RepositoryException {
        return vh;
    }

    public Calendar getCreated() throws RepositoryException {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(createdTime);
        return cal;
    }

    public Version getLinearSuccessor() throws RepositoryException { //TODO Under Full Versioning
        return null;
    }


    public Version[] getSuccessors() throws RepositoryException {    //TODO Under Full Versioning
        return new Version[0];
    }

    public Version getLinearPredecessor() throws RepositoryException {  //TODO Under Full Versioning

        return null;
    }

    public Version[] getPredecessors() throws RepositoryException { //TODO Under Full Versioning
        return new Version[0];
    }

    public Node getFrozenNode() throws RepositoryException {  //TODO Under Full Versioning

        return (Node) session.getItem(fNodepath);
    }

//   TODO  All following Node inherited methods will be apply under full versioning

    public Node addNode(String s) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        return null;
    }

    public Node addNode(String s, String s1) throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, RepositoryException {

        return null;
    }

    public void orderBefore(String s, String s1) throws UnsupportedRepositoryOperationException, VersionException, ConstraintViolationException, ItemNotFoundException, LockException, RepositoryException {

    }

    public Property setProperty(String s, Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {

        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Property setProperty(String s, Value value, int i) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Property setProperty(String s, Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Property setProperty(String s, Value[] values, int i) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Property setProperty(String s, String[] strings) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Property setProperty(String s, String[] strings, int i) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Property setProperty(String s, String s1) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Property setProperty(String s, String s1, int i) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Property setProperty(String s, InputStream inputStream) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Property setProperty(String s, Binary binary) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Property setProperty(String s, boolean b) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Property setProperty(String s, double v) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Property setProperty(String s, BigDecimal bigDecimal) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Property setProperty(String s, long l) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Property setProperty(String s, Calendar calendar) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Property setProperty(String s, Node node) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Node getNode(String s) throws PathNotFoundException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public NodeIterator getNodes() throws RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public NodeIterator getNodes(String s) throws RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public NodeIterator getNodes(String[] strings) throws RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Property getProperty(String s) throws PathNotFoundException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public PropertyIterator getProperties() throws RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public PropertyIterator getProperties(String s) throws RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public PropertyIterator getProperties(String[] strings) throws RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Item getPrimaryItem() throws ItemNotFoundException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getUUID() throws UnsupportedRepositoryOperationException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getIdentifier() throws RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getIndex() throws RepositoryException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public PropertyIterator getReferences() throws RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public PropertyIterator getReferences(String s) throws RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public PropertyIterator getWeakReferences() throws RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public PropertyIterator getWeakReferences(String s) throws RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean hasNode(String s) throws RepositoryException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean hasProperty(String s) throws RepositoryException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean hasNodes() throws RepositoryException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean hasProperties() throws RepositoryException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public NodeType getPrimaryNodeType() throws RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public NodeType[] getMixinNodeTypes() throws RepositoryException {
        return new NodeType[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isNodeType(String s) throws RepositoryException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setPrimaryType(String s) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addMixin(String s) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeMixin(String s) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean canAddMixin(String s) throws NoSuchNodeTypeException, RepositoryException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public NodeDefinition getDefinition() throws RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Version checkin() throws VersionException, UnsupportedRepositoryOperationException, InvalidItemStateException, LockException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void checkout() throws UnsupportedRepositoryOperationException, LockException, ActivityViolationException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void doneMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void cancelMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void update(String s) throws NoSuchWorkspaceException, AccessDeniedException, LockException, InvalidItemStateException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public NodeIterator merge(String s, boolean b) throws NoSuchWorkspaceException, AccessDeniedException, MergeException, LockException, InvalidItemStateException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getCorrespondingNodePath(String s) throws ItemNotFoundException, NoSuchWorkspaceException, AccessDeniedException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public NodeIterator getSharedSet() throws RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeSharedSet() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeShare() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isCheckedOut() throws RepositoryException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void restore(String s, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void restore(Version version, boolean b) throws VersionException, ItemExistsException, InvalidItemStateException, UnsupportedRepositoryOperationException, LockException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void restore(Version version, String s, boolean b) throws PathNotFoundException, ItemExistsException, VersionException, ConstraintViolationException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void restoreByLabel(String s, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public VersionHistory getVersionHistory() throws UnsupportedRepositoryOperationException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Version getBaseVersion() throws UnsupportedRepositoryOperationException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Lock lock(boolean b, boolean b1) throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Lock getLock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void unlock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean holdsLock() throws RepositoryException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isLocked() throws RepositoryException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void followLifecycleTransition(String s) throws UnsupportedRepositoryOperationException, InvalidLifecycleTransitionException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String[] getAllowedLifecycleTransistions() throws UnsupportedRepositoryOperationException, RepositoryException {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getPath() throws RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getName() throws RepositoryException {

        return versionName;
    }

    public Item getAncestor(int i) throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getDepth() throws RepositoryException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Session getSession() throws RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isNode() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isNew() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isModified() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isSame(Item item) throws RepositoryException {
        return this.getName().equals(item.getName());
    }

    public void accept(ItemVisitor itemVisitor) throws RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void save() throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void refresh(boolean b) throws InvalidItemStateException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void remove() throws VersionException, LockException, ConstraintViolationException, AccessDeniedException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
