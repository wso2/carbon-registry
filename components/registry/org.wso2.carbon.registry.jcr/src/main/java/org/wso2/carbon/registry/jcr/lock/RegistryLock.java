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

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;

public class RegistryLock implements Lock {

    private String absPath = "";
    private boolean isSessionScoped = false;
    private long timeoutHint = 0;
    private String ownerInfo = "";
    private Session session;

    public RegistryLock(Session session, String absPath, boolean isDeep, boolean isSessionScoped, long timeoutHint, String ownerInfo) {

        this.absPath = absPath;
        this.isSessionScoped = isSessionScoped;
        this.timeoutHint = timeoutHint;
        this.ownerInfo = ownerInfo;
        this.session = session;
    }

    public String getLockOwner() {
        return ownerInfo;
    }

    public boolean isDeep() {
        return false;
    }

    public Node getNode() {
        try {

            return (Node) session.getItem(absPath);

        } catch (RepositoryException e) {

            return null;

        }
    }


    public String getLockToken() {
        return null;
    }

    public long getSecondsRemaining() throws RepositoryException {
        return timeoutHint;
    }

    public boolean isLive() throws RepositoryException {
        return false;
    }

    public boolean isSessionScoped() {
        return isSessionScoped;
    }

    public boolean isLockOwningSession() {
        return false;

    }

    public void refresh() throws LockException, RepositoryException {

    }
}
