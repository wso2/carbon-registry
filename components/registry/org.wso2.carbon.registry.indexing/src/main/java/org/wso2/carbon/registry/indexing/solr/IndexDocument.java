/*
 *  Copyright (c) 2005-2015, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.wso2.carbon.registry.indexing.solr;

import java.util.List;
import java.util.Map;

public class IndexDocument {

    private String path;
    private String contentAsText;
    private String rawContent;
    private int tenantId;
    private Map<String, List<String>> fields;

    public IndexDocument() {
    }

    public IndexDocument(String path, String rawContent, String contentAsText) {
        this.path = path;
        this.contentAsText = contentAsText;
        this.rawContent = rawContent;
    }

    public IndexDocument(String path, String contentAsText, String rawContent, int tenantId) {
        this.path = path;
        this.contentAsText = contentAsText;
        this.rawContent = rawContent;
        this.tenantId = tenantId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getContentAsText() {
        return contentAsText;
    }

    public void setContentAsText(String contentAsText) {
        this.contentAsText = contentAsText;
    }

    public String getRawContent() {
        return rawContent;
    }

    public void setRawContent(String rawContent) {
        this.rawContent = rawContent;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public Map<String, List<String>> getFields() {
        return fields;
    }

    public void setFields(Map<String, List<String>> fields) {
        this.fields = fields;
    }
}
