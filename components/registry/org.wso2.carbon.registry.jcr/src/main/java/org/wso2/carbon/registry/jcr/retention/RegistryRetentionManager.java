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

package org.wso2.carbon.registry.jcr.retention;

import org.apache.xalan.xsltc.dom.LoadDocument;
import org.wso2.carbon.registry.common.CommonConstants;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.jcr.RegistrySession;
import org.wso2.carbon.registry.jcr.util.RegistryJCRSpecificStandardLoderUtil;
import org.wso2.carbon.registry.jcr.util.retention.EffectiveRetentionUtil;
import org.wso2.carbon.registry.jcr.util.test.data.TCKTestDataLoader;

import javax.jcr.AccessDeniedException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;
import javax.jcr.retention.Hold;
import javax.jcr.retention.RetentionManager;
import javax.jcr.retention.RetentionPolicy;
import javax.jcr.version.VersionException;
import java.util.*;


public class RegistryRetentionManager implements RetentionManager {


    private Map<String, Set<Hold>> pendingHoldMap = new HashMap<String, Set<Hold>>();
    //    private Map<String, RetentionPolicy> retentionPolicies = new HashMap<String, RetentionPolicy>();
    private Map<String, RetentionPolicy> pendingRetentionPolicies = new HashMap<String, RetentionPolicy>();
    private List<String> pendingPolicyRemoveList = new ArrayList<String>();

    private RegistrySession session;


    public List<String> getPendingPolicyRemoveList() {
        return pendingPolicyRemoveList;
    }

    public RegistryRetentionManager(RegistrySession session) {
        this.session = session;
        loadTCKTestdata();
    }

    //    TODO REMOVE from svn, as this just need for TCK running
    private void loadTCKTestdata() {
        try {
            TCKTestDataLoader.loadRetentionPolicies(session);
        } catch (RepositoryException e) {
        }
    }

    public Map<String, Set<Hold>> getPendingRetentionHolds() {
        return pendingHoldMap;
    }

    public Map<String, RetentionPolicy> getPendingRetentionPolicies() {
        return pendingRetentionPolicies;
    }

    public Hold[] getHolds(String s) throws PathNotFoundException, AccessDeniedException, RepositoryException {

        if(RegistryJCRSpecificStandardLoderUtil.isSessionReadOnly(session.getUserID())){
            throw new AccessDeniedException("Read-only session doesn't have " +
                    "sufficient privileges to retrieve retention holds");
        }
        if (!isPathValid(s)) {
            throw new RepositoryException("Cannot apply invalid path for retention holds " + s);
        }
        if (!isPathExists(s)) {
            throw new PathNotFoundException("No such Path exists for getting hold: " + s);
        }
        return EffectiveRetentionUtil.getHoldsFromRegistry(session, s);
    }

    public Hold addHold(String s, String s1, boolean b) throws PathNotFoundException, AccessDeniedException, LockException, VersionException, RepositoryException {

        if (!isPathValid(s)) {
            throw new RepositoryException("Cannot apply invalid path for retention holds " + s);
        }

        if(!isValidJCRName(s1)) {
            throw new RepositoryException("Cannot apply invalid name for retention holds " + s);
        }

        return addHoldsToPending(s, s1, b);
//    return EffectiveRetentionUtil.addHoldsToRegistry(session,s,s1,b);
    }

    private Hold addHoldsToPending(String s, String s1, boolean b) {
        Hold aHold = new RegistryHold(s1, b);
        if (pendingHoldMap.get(s) == null) {
            Set<Hold> tempHd = new HashSet<Hold>();
            tempHd.add(aHold);
            pendingHoldMap.put(s, tempHd);
        } else {
            pendingHoldMap.get(s).add(aHold);
        }
        return aHold;

    }

    public void removeHold(String s, Hold hold) throws PathNotFoundException, AccessDeniedException, LockException, VersionException, RepositoryException {
        if(pendingHoldMap.containsKey(s)) {
            ((Set)pendingHoldMap.get(s)).remove(hold);
        }
        EffectiveRetentionUtil.removeHoldFromRegistry(session, s, hold);
    }

    public RetentionPolicy getRetentionPolicy(String s) throws PathNotFoundException, AccessDeniedException, RepositoryException {

        if(RegistryJCRSpecificStandardLoderUtil.isSessionReadOnly(session.getUserID())){
            throw new AccessDeniedException("Read-only session doesn't have " +
                    "sufficient privileges to retrieve retention policy.");
        }
        //Invalid path check
        if (!isPathValid(s)) {
            throw new RepositoryException("Cannot apply invalid path for retention policies " + s);
        }
        // Node existance check
        if (!isPathExists(s)) {
            throw new PathNotFoundException("No such Path exists for apply retention: " + s);
        }

        RetentionPolicy persistedPolicy = EffectiveRetentionUtil.getRetentionPolicyFromRegistry(session, s);
        if(persistedPolicy != null) {
          return persistedPolicy;
        } else {
         return pendingRetentionPolicies.get(s);
        }
    }

    private boolean isPathExists(String s) throws RepositoryException {
        try {
            return session.getUserRegistry().resourceExists(s)
                    && session.getUserRegistry().get(s) instanceof CollectionImpl;
        } catch (RegistryException e) {
            throw new RepositoryException("Registry level exception occurred when applying retention to " + s);
        }
    }

    public void setRetentionPolicy(String s, RetentionPolicy retentionPolicy) throws PathNotFoundException, AccessDeniedException, LockException, VersionException, RepositoryException {

        if(session.getWorkspace().getLockManager().holdsLock(s)) {
           throw new LockException("Cannot set retention policy on a locked node");
        }
        if(RegistryJCRSpecificStandardLoderUtil.isSessionReadOnly(session.getUserID())){
            throw new AccessDeniedException("Read-only session doesn't have " +
                    "sufficient privileges to retrieve retention policy.");
        }
        //Invalid path check
        if (!isPathValid(s)) {
            throw new RepositoryException("Cannot apply invalid path for retention policies " + s);
        }

        if (!isValidJCRName(retentionPolicy.getName())) {
            throw new RepositoryException("Cannot apply invalid name for retention policies " + s);
        }

        if (!isPathExists(s)) {
            throw new PathNotFoundException("No such Path exists for apply retention: " + s);
        }
        pendingRetentionPolicies.put(s, retentionPolicy);
//        EffectiveRetentionUtil.setRetentionPolicyToRegistry(session,s,retentionPolicy);
    }

    public void removeRetentionPolicy(String s) throws PathNotFoundException, AccessDeniedException, LockException, VersionException, RepositoryException {

        if(session.getWorkspace().getLockManager().holdsLock(s)) {
           throw new LockException("Cannot set retention policy on a locked node");
        }
        if (!isPathValid(s)) {
            throw new RepositoryException("Cannot apply invalid path for retention policies " + s);
        }

        if (!isPathExists(s)) {
            throw new PathNotFoundException("No such Path exists for apply retention: " + s);
        }

        try {
            if ((getRetentionPolicy(s) == null) && getRetentionPolicy(
                    session.getUserRegistry().get(s).getParentPath()) != null) {
                throw new RepositoryException("Cannot remove retention from other nodes" + s);
            }
        } catch (RegistryException e) {
            throw new RepositoryException("Cannot remove retention from other nodes" + s);
        }

//        if(getRetentionPolicy(s) != null) {
//            EffectiveRetentionUtil.removeRetentionPolicyFromRegistry(session, s);
//            return;
//        }
        if(pendingRetentionPolicies.get(s) != null) {
            pendingRetentionPolicies.remove(s);
        } else if(getRetentionPolicy(s) != null){
            EffectiveRetentionUtil.removeRetentionPolicyFromRegistry(session, s);
            pendingPolicyRemoveList.add(s);
        }
    }

    private boolean isPathValid(String path) {
        if (path == null || !path.contains("/")
                || path.contains("*")
                || path.contains("[")
                || path.contains("]"))
//            TODO add more to validate jcr path naming or use regex
        {
            return false;
        } else {
            return true;
        }
    }

    private boolean isValidJCRName(String name) {
        if (name == null || name.contains("/")
                || name.contains("*"))
//            TODO add more to validate jcr naming or use regex
        {
            return false;
        } else {
            return true;
        }
    }


}
