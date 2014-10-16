/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.metadata.internal;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.registry.common.AttributeSearchService;
import org.wso2.carbon.registry.metadata.Util;

/**
 * @scr.component name="org.wso2.carbon.registry.metadata" immediate="true"
 * @scr.reference name="registry.search.component"
 * interface="org.wso2.carbon.registry.common.AttributeSearchService"
 * cardinality="1..1" policy="dynamic" bind="setAttributeSearchService" unbind="unsetAttributeSearchService"
 */
public class MetadataServiceComponent {

    private static final Log log = LogFactory.getLog(MetadataServiceComponent.class);

    /**
     * Activates the Governance API bundle.
     *
     * @param context the OSGi component context.
     */
    protected void activate(ComponentContext context) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Governance API bundle is activated ");
            }
        } catch (Exception e) {
            log.error("Failed to activate Metadata API bundle");
        }
    }

    /**
     * Deactivates the Governance API bundle.
     *
     * @param context the OSGi component context.
     */
    protected void deactivate(ComponentContext context) {
        log.debug("Governance API bundle is deactivated ");
    }


    protected void setAttributeSearchService(AttributeSearchService searchService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting ContentBasedSearchService");
        }
        Util.setAttributeSearchService(searchService);

    }

    protected void unsetAttributeSearchService(AttributeSearchService searchService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting ContentBasedSearchService");
        }
        Util.setAttributeSearchService(null);

    }

}
