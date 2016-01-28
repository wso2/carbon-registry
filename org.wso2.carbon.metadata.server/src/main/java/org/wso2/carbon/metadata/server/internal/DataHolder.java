/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.metadata.server.internal;

import org.osgi.framework.BundleContext;
import org.wso2.carbon.metadata.server.api.MetadataStore;

/**
 * Carbon Metadata DataHolder.
 *
 * @since 1.0.0
 */
public class DataHolder {

    private static DataHolder instance = new DataHolder();
    private BundleContext bundleContext;
    private MetadataStore metadataStore;


    public static DataHolder getInstance() {
        return instance;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public MetadataStore getMetadataStore() {
        return metadataStore;
    }

    public void setMetadataStore(MetadataStore metadataStore) {
        this.metadataStore = metadataStore;
    }

    public void setBundleContext(BundleContext bundleContext) {

        this.bundleContext = bundleContext;
    }
}
