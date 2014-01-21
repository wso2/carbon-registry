/*
 *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.common.utils.artifact.manager;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ArtifactRepository {

    private final Set<String> artifacts;

    private ReadWriteLock lock;

    public ArtifactRepository() {
        this.artifacts = new HashSet<String>();
        this.lock = new ReentrantReadWriteLock();
    }

    public void addArtifact(String path) {
        lock.writeLock().lock();
        try {
            artifacts.add(path);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeArtifact(String path) {
        lock.writeLock().lock();
        try {
            artifacts.remove(path);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Set<String> getArtifacts() {
        lock.readLock().lock();
        try {
            return artifacts;
        } finally {
            lock.readLock().unlock();
        }
    }

}
