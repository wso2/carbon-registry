package org.wso2.carbon.metadata.server.impl;

import org.wso2.carbon.metadata.server.api.Collection;

/**
 * Created by chandana on 1/27/16.
 */
public class CollectionImpl extends Collection {

    /**
     * Path of the parent collection of the resource. If the resource path is
     * /servers/config/users.xml, parent path is /servers/config.
     */
    protected String parentPath;

    /**
     * Method to get the parent path.
     *
     * @return the parent path.
     */
    public String getParentPath() {
        if (parentPath != null) {
            return parentPath;
        }
        if ((this.getKey() == null) || this.getKey().length() == 1) {
            return null;
        }
        int i = this.getKey().lastIndexOf('/');
        return this.getKey().substring(0, i);
    }

    /**
     * Method to set the parent path.
     *
     * @param parentPath the parent path.
     */
    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }
}
