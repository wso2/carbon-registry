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

package org.wso2.carbon.registry.jcr.lock;

import org.wso2.carbon.registry.jcr.RegistryNode;
import org.wso2.carbon.registry.jcr.RegistryRepository;
import org.wso2.carbon.registry.jcr.RegistrySession;

import javax.jcr.*;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.lock.LockManager;
import java.util.*;


public class RegistryLockManager implements LockManager {

    private Map<String, Lock> allLocks = new HashMap<String, Lock>();
    private Set<String> lockTokens = new HashSet<String>();

    private Session session;

    public RegistryLockManager(Session session) {
        this.session = session;
    }

    public void addLockToken(String s) throws LockException, RepositoryException {
       lockTokens.add(s);
    }

    public Lock getLock(String s) throws PathNotFoundException, LockException, AccessDeniedException, RepositoryException {

        return allLocks.get(s);
    }

    public String[] getLockTokens() throws RepositoryException {

        if (lockTokens.size() != 0) {
            int i = 0;
            String[] arr = new String[lockTokens.size()];
            Iterator it = lockTokens.iterator();

            while (it.hasNext()) {
                Object temp = it.next();
                if (temp != null) {
                    arr[i] = temp.toString();
                    i++;
                }
            }

            return arr;

        } else {

            return new String[0];
        }

    }

    public boolean holdsLock(String s) throws PathNotFoundException, RepositoryException {
        return getAllLocks().containsKey(s);
//        boolean holdLock = false;
//
//        if (getLock(s) != null) {
//            holdLock = true;
//        }
//
//        return holdLock;
    }

    /*
    Here we are maintaining a Map in registry repository with the absoulute path and its lock as
    we have both session scoped and open scoped locks.But we can put each lock inside the node
    at given path a property which has a property values list
    */

    public Lock lock(String s, boolean b, boolean b1, long l, String s1) throws LockException, PathNotFoundException, AccessDeniedException, InvalidItemStateException, RepositoryException {
        Lock lock = null;

        try {

            if (!((Node) ((RegistrySession) session).getItem(s)).isNodeType("mix:lockable")) {
                throw new LockException();
            } else {

                lock = new RegistryLock(session, s, b, b1, l, s1);
                allLocks.put(s, lock);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return lock;
    }

    public boolean isLocked(String s) throws PathNotFoundException, RepositoryException {

        boolean islocked = false;
        String parentPath = ((RegistryNode) (session.getItem(s))).getParent().getPath();

        if ((allLocks.get(s) != null)
                || (getLock(parentPath) != null) && (getLock(parentPath).isDeep())) {

            islocked = true;

        }

        return islocked;
    }

    public void removeLockToken(String s) throws LockException, RepositoryException {
        lockTokens.remove(s);
    }

    public void unlock(String s) throws PathNotFoundException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
        allLocks.remove(s);
    }

    private Map<String, Lock> getAllLocks() {
        return allLocks;
    }

}
