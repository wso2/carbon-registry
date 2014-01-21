/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.extensions.handlers.utils;

import org.apache.ws.commons.schema.XmlSchema;
import java.util.ArrayList;

public class SchemaInfo {
    private String originalURL;
    private String proposedRegistryURL;
    private XmlSchema schema;
    private ArrayList<String> schemaDependencies;
    private SchemaInfo parentSchema = null;
    private boolean isMasterSchema = false;

    public String getProposedResourceName() {
        return proposedResourceName;
    }

    public void setProposedResourceName(String proposedResourceName) {
        this.proposedResourceName = proposedResourceName;
    }

    private String proposedResourceName;

    public SchemaInfo() {
        schemaDependencies = new ArrayList<String>();
    }

    public String getOriginalURL() {
        return originalURL;
    }

    public void setOriginalURL(String originalURL) {
        this.originalURL = originalURL;
    }

    public String getProposedRegistryURL() {
        return proposedRegistryURL;
    }

    public void setProposedRegistryURL(String proposedRegistryURL) {
        this.proposedRegistryURL = proposedRegistryURL;
    }

    public XmlSchema getSchema() {
        return schema;
    }

    public void setSchema(XmlSchema schema) {
        this.schema = schema;
    }

    public ArrayList<String> getSchemaDependencies() {
        return schemaDependencies;
    }

    public void setSchemaDependencies(ArrayList<String> schemaDependencies) {
        this.schemaDependencies = schemaDependencies;
    }

    public SchemaInfo getParentSchema() {
        return parentSchema;
    }

    public void setParentSchema(SchemaInfo parentSchema) {
        this.parentSchema = parentSchema;
    }

    public boolean isMasterSchema() {
        return isMasterSchema;
    }

    public void setMasterSchema(boolean masterSchema) {
        isMasterSchema = masterSchema;
    }
}
