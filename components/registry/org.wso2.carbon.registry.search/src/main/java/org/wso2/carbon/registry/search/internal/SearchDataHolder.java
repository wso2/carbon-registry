/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.registry.search.internal;

import org.wso2.carbon.registry.common.AttributeSearchService;
import org.wso2.carbon.registry.common.eventing.NotificationService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.indexing.service.ContentSearchService;

public class SearchDataHolder {

    private RegistryService registryService;

    private NotificationService registryNotificationService;

    private AttributeSearchService attributeIndexingService;

    private ContentSearchService contentSearchService;

    private static SearchDataHolder holder = new SearchDataHolder();
    private AttributeSearchService attributeSearchService;

    private SearchDataHolder() {
    }

    public static SearchDataHolder getInstance() {
        return holder;
    }

    public RegistryService getRegistryService() {
        return registryService;
    }

    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    public void setRegistryNotificationService(NotificationService registryNotificationService) {
        this.registryNotificationService = registryNotificationService;
    }

    public NotificationService getRegistryNotificationService() {
        return registryNotificationService;
    }

    public void setContentSearchService(ContentSearchService contentSearchService) {
        this.contentSearchService = contentSearchService;
    }

    public ContentSearchService getContentSearchService() {
        return contentSearchService;
    }

    public void setAttributeIndexingService(AttributeSearchService attributeIndexingService) {
        this.attributeIndexingService = attributeIndexingService;
    }

    public AttributeSearchService getAttributeIndexingService() {
        return attributeIndexingService;
    }

    public AttributeSearchService getAttributeSearchService() {
        return attributeSearchService;
    }

    public void setAttributeSearchService(AttributeSearchService attributeSearchService) {
        this.attributeSearchService = attributeSearchService;
    }
}
