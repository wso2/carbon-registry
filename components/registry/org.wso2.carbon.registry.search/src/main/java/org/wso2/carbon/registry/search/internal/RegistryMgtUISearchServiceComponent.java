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

package org.wso2.carbon.registry.search.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.registry.common.ResourceData;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.queries.QueryProcessorManager;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.indexing.service.ContentBasedSearchService;
import org.wso2.carbon.registry.indexing.service.ContentSearchService;
import org.wso2.carbon.registry.search.Utils;
import org.wso2.carbon.registry.search.beans.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.beans.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.services.MetadataSearchService;
import org.wso2.carbon.registry.search.services.XPathQueryProcessor;
import org.wso2.carbon.registry.search.services.utils.AdvancedSearchResultsBeanPopulator;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;



import javax.swing.text.AbstractDocument;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @scr.component name="registry.search.dscomponent" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 *  @scr.reference name="registry.indexing"
 *  interface="org.wso2.carbon.registry.indexing.service.ContentSearchService" cardinality="1..1"
 *  policy="dynamic" bind="setIndexingService" unbind="unsetIndexingService"
 */

public class RegistryMgtUISearchServiceComponent {

    private static Log log = LogFactory.getLog(RegistryMgtUISearchServiceComponent.class);
    private ServiceRegistration serviceRegistration;

    protected void activate(ComponentContext context) {
        MetadataSearchServiceImpl metadataSearchService = new MetadataSearchServiceImpl();
        serviceRegistration = context.getBundleContext().registerService(
                MetadataSearchService.class.getName(), metadataSearchService, null);
        try {
            QueryProcessorManager queryProcessorManager =
                    Utils.getRegistryService().getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME)
                            .getRegistryContext().getQueryProcessorManager();
            if (queryProcessorManager.getQueryProcessor(
                    XPathQueryProcessor.XPATH_QUERY_MEDIA_TYPE) == null) {
                // users can extend the XPath query processor if they want to.
                queryProcessorManager.setQueryProcessor(XPathQueryProcessor.XPATH_QUERY_MEDIA_TYPE,
                        new XPathQueryProcessor(metadataSearchService));
            }
        } catch (RegistryException e) {
            log.error("Unable to registry query processors", e);
        }
        log.debug("******* Registry Search bundle is activated ******* ");
    }

    protected void deactivate(ComponentContext context) {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
            serviceRegistration = null;
        }
        log.debug("******* Registry Search bundle is deactivated ******* ");
    }

    protected void setRegistryService(RegistryService registryService) {
        Utils.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        Utils.setRegistryService(null);
    }

    protected void setIndexingService(ContentSearchService contentSearchService){
         Utils.setContentSearchService(contentSearchService);
    }

    protected void unsetIndexingService(ContentSearchService contentSearchService){
         Utils.setContentSearchService(null);
    }

    private static class MetadataSearchServiceImpl implements MetadataSearchService {

        public ResourceData[] search(UserRegistry registry, Map<String, String> parameters)
                throws RegistryException {
            List<String[]> params = new LinkedList<String[]>();
            for (Map.Entry<String, String> e : parameters.entrySet()) {
                params.add(new String[] {e.getKey(), e.getValue()});
            }
            CustomSearchParameterBean parameterBean = new CustomSearchParameterBean();
            parameterBean.setParameterValues(params.toArray(new String[params.size()][]));
            AdvancedSearchResultsBean resultsBean =
                    AdvancedSearchResultsBeanPopulator.populate(null, registry, parameterBean);
            String errorMessage = resultsBean.getErrorMessage();
            if (errorMessage != null) {
                throw new RegistryException(errorMessage);
            }
            return resultsBean.getResourceDataList();
        }

        public ResourceData[] search(int tenantId, Map<String, String> parameters)
                throws RegistryException {
            return search(Utils.getRegistryService().getRegistry(
                    CarbonConstants.REGISTRY_SYSTEM_USERNAME, tenantId), parameters);
        }

        public ResourceData[] search(Map<String, String> parameters) throws RegistryException {
            return search(MultitenantConstants.SUPER_TENANT_ID, parameters);
        }
    }
}
