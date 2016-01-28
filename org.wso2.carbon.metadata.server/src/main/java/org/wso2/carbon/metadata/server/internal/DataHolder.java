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
