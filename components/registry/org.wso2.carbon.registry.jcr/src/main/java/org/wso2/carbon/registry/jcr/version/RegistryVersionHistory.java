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

import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.jcr.RegistrySession;
import org.wso2.carbon.registry.jcr.util.RegistryJCRItemOperationUtil;
import org.wso2.carbon.registry.jcr.util.RegistryJCRSpecificStandardLoderUtil;

import javax.jcr.*;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.*;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

//TODO Should persist versions and version histories
public class RegistryVersionHistory implements VersionHistory {

    private List<Version> versions = new ArrayList<Version>();
    private Map<String, List> versionLabels = new HashMap<String, List>();
    private Session session;
    private String nodePath="";

    public RegistryVersionHistory(Session session,String nodePath) {
        this.session = session;
        this.nodePath = nodePath;
    }

    public List<Version> getVersionList() {
        return versions;
    }

    public String getVersionableUUID() throws RepositoryException {

        return null;
    }

    public String getVersionableIdentifier() throws RepositoryException {

        return null;
    }

    public Version getRootVersion() throws RepositoryException {

        return createRootVersion();
    }

    public VersionIterator getAllLinearVersions() throws RepositoryException {

        return new RegistryVersionIterator(versions);
    }

    public VersionIterator getAllVersions() throws RepositoryException {

        return new RegistryVersionIterator(versions);
    }

    public NodeIterator getAllLinearFrozenNodes() throws RepositoryException {

        return null;
    }

    public NodeIterator getAllFrozenNodes() throws RepositoryException {

        return null;
    }

    public Version getVersion(String s) throws VersionException, RepositoryException {
        Version correctVersion = null;
        for(Version ver:getVersionList()){
            if (ver.getName().equals(s)) {
                correctVersion = ver;
                break;
            }
        }
        return correctVersion;
    }

    public Version getVersionByLabel(String s) throws VersionException, RepositoryException {

          try {
            Resource res = ((RegistrySession) session).getUserRegistry().get(
                    RegistryJCRSpecificStandardLoderUtil.
                            getSystemConfigVersionLabelPath((RegistrySession) session));
                 String versionId = res.getProperty(s);
              if(versionId!= null) {
                return getVersion(versionId);
              } else {
                 throw new VersionException("Version ID cannot be null..!!" + s);
              }

        } catch (RegistryException e) {
            throw new RepositoryException("Exception occurred in registry level " + s);

        }
    }

    public void addVersionLabel(String s, String s1, boolean b) throws LabelExistsVersionException, VersionException, RepositoryException {

        try {
            Resource res = ((RegistrySession) session).getUserRegistry().get(
                    RegistryJCRSpecificStandardLoderUtil.
                            getSystemConfigVersionLabelPath((RegistrySession) session));
            res.setProperty(s1, s);
            ((RegistrySession) session).getUserRegistry().put(RegistryJCRSpecificStandardLoderUtil.
                    getSystemConfigVersionLabelPath((RegistrySession) session), res);
        } catch (RegistryException e) {
            throw new RepositoryException("Exception occurred in registry level " + s);
        }
    }

    public void removeVersionLabel(String s) throws VersionException, RepositoryException {
        try {
            Resource res = ((RegistrySession) session).getUserRegistry().get(
                    RegistryJCRSpecificStandardLoderUtil.
                            getSystemConfigVersionLabelPath((RegistrySession) session));
            if (res.getProperty(s) != null) {
                res.removeProperty(s);
            }
            ((RegistrySession) session).getUserRegistry().put(RegistryJCRSpecificStandardLoderUtil.
                    getSystemConfigVersionLabelPath((RegistrySession) session), res);
        } catch (RegistryException e) {
            throw new RepositoryException("Exception occurred in registry level " + s);
        }
    }

    public boolean hasVersionLabel(String s) throws RepositoryException {

        try {
            Resource res = ((RegistrySession) session).getUserRegistry().get(
                    RegistryJCRSpecificStandardLoderUtil.
                            getSystemConfigVersionLabelPath((RegistrySession) session));
            if (res.getProperty(s) != null) {
                return true;
            } else {
                return false;
            }
        } catch (RegistryException e) {
            throw new RepositoryException("Exception occurred in registry level " + s);
        }
    }

    private Version createRootVersion() {
        return new RegistryVersion(null, System.currentTimeMillis(), null, session);
    }

    public boolean hasVersionLabel(Version version, String s) throws VersionException, RepositoryException {

        try {
            Resource res = ((RegistrySession) session).getUserRegistry().get(
                    RegistryJCRSpecificStandardLoderUtil.
                            getSystemConfigVersionLabelPath((RegistrySession) session));
            if (res.getProperty(s) != null && res.getProperty(s).equals(version.getName())) {
                return true;
            } else {
                return false;
            }

        } catch (RegistryException e) {
            throw new RepositoryException("Exception occurred in registry level " + s);
        }
    }

    public String[] getVersionLabels() throws RepositoryException {

          try {
            Resource res = ((RegistrySession) session).getUserRegistry().get(
                    RegistryJCRSpecificStandardLoderUtil.
                            getSystemConfigVersionLabelPath((RegistrySession) session));
             Enumeration<String> labels = (Enumeration<String>)res.getProperties().propertyNames();
            return Arrays.asList(labels).toArray(new String[0]);
        } catch (RegistryException e) {
            throw new RepositoryException("Exception occurred in registry level");
        }
    }

    public String[] getVersionLabels(Version version) throws VersionException, RepositoryException {
        return new String[0];
    }

    public void removeVersion(String s) throws ReferentialIntegrityException, AccessDeniedException, UnsupportedRepositoryOperationException, VersionException, RepositoryException {
         versions.remove(getVersion(s));
    }

    public Node addNode(String s) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Node addNode(String s, String s1) throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void orderBefore(String s, String s1) throws UnsupportedRepositoryOperationException, VersionException, ConstraintViolationException, ItemNotFoundException, LockException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
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
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
        return false;  //To change body of implemented methods use File | Settings | File Templates.
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
