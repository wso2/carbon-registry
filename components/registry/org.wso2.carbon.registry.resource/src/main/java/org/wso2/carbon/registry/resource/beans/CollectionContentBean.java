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

package org.wso2.carbon.registry.resource.beans;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

@SuppressWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class CollectionContentBean {

    private String pathWithVersion;

    private String[] collectionTypes;

    private int childCount;

    private String[] childPaths;

    private String[] remoteInstances;

    private boolean versionView;

    public String getPathWithVersion() {
        return pathWithVersion;
    }

    public void setPathWithVersion(String pathWithVersion) {
        this.pathWithVersion = pathWithVersion;
    }

    public String[] getCollectionTypes() {
        return collectionTypes;
    }

    public void setCollectionTypes(String[] collectionTypes) {
        this.collectionTypes = collectionTypes;
    }

    public int getChildCount() {
        return childCount;
    }

    public void setChildCount(int childCount) {
        this.childCount = childCount;
    }

    public String[] getChildPaths() {
        return childPaths;
    }

    public void setChildPaths(String[] childPaths) {
        this.childPaths = childPaths;
    }

    public String[] getRemoteInstances() {
        return remoteInstances;
    }

    public void setRemoteInstances(String[] remoteInstances) {
        this.remoteInstances = remoteInstances;
    }

    public boolean isVersionView() {
        return versionView;
    }

    public void setVersionView(boolean versionView) {
        this.versionView = versionView;
    }
}
