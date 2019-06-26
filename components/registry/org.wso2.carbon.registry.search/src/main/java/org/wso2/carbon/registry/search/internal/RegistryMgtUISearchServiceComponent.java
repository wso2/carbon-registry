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
import org.wso2.carbon.registry.common.AttributeSearchService;
import org.wso2.carbon.registry.common.ResourceData;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.queries.QueryProcessorManager;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.indexing.service.ContentSearchService;
import org.wso2.carbon.registry.search.beans.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.beans.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.services.MetadataSearchService;
import org.wso2.carbon.registry.search.services.XPathQueryProcessor;
import org.wso2.carbon.registry.search.services.utils.AdvancedSearchResultsBeanPopulator;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
         name = "registry.search.dscomponent", 
         immediate = true)
public class RegistryMgtUISearchServiceComponent {

    private static Log log = LogFactory.getLog(RegistryMgtUISearchServiceComponent.class);

    private SearchDataHolder dataHolder = SearchDataHolder.getInstance();

    private ServiceRegistration serviceRegistration;

    @Activate
    protected void activate(ComponentContext context) {
        MetadataSearchServiceImpl metadataSearchService = new MetadataSearchServiceImpl();
        serviceRegistration = context.getBundleContext().registerService(MetadataSearchService.class.getName(), metadataSearchService, null);
        try {
            QueryProcessorManager queryProcessorManager = dataHolder.getRegistryService().getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME).getRegistryContext().getQueryProcessorManager();
            if (queryProcessorManager.getQueryProcessor(XPathQueryProcessor.XPATH_QUERY_MEDIA_TYPE) == null) {
                // users can extend the XPath query processor if they want to.
                queryProcessorManager.setQueryProcessor(XPathQueryProcessor.XPATH_QUERY_MEDIA_TYPE, new XPathQueryProcessor(metadataSearchService));
            }
        } catch (RegistryException e) {
            log.error("Unable to registry query processors", e);
        }
        log.debug("******* Registry Search bundle is activated ******* ");
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
            serviceRegistration = null;
        }
        log.debug("******* Registry Search bundle is deactivated ******* ");
    }

    @Reference(
             name = "registry.service", 
             service = org.wso2.carbon.registry.core.service.RegistryService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {
        dataHolder.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        dataHolder.setRegistryService(null);
    }

    @Reference(
             name = "registry.indexing", 
             service = org.wso2.carbon.registry.indexing.service.ContentSearchService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetIndexingService")
    protected void setIndexingService(ContentSearchService contentSearchService) {
        dataHolder.setContentSearchService(contentSearchService);
    }

    protected void unsetIndexingService(ContentSearchService contentSearchService) {
        dataHolder.setContentSearchService(null);
    }

    @Reference(
             name = "registry.attribute.indexing", 
             service = org.wso2.carbon.registry.common.AttributeSearchService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetAttributeIndexingService")
    protected void setAttributeIndexingService(AttributeSearchService attributeIndexingService) {
        dataHolder.setAttributeIndexingService(attributeIndexingService);
    }

    protected void unsetAttributeIndexingService(AttributeSearchService attributeIndexingService) {
        dataHolder.setAttributeIndexingService(null);
    }


    private static class MetadataSearchServiceImpl implements MetadataSearchService {

        public ResourceData[] search(UserRegistry registry, Map<String, String> parameters) throws RegistryException {
            List<String[]> params = new LinkedList<String[]>();
            for (Map.Entry<String, String> e : parameters.entrySet()) {
                params.add(new String[] { e.getKey(), e.getValue() });
            }
            CustomSearchParameterBean parameterBean = new CustomSearchParameterBean();
            parameterBean.setParameterValues(params.toArray(new String[params.size()][]));
            AdvancedSearchResultsBean resultsBean = AdvancedSearchResultsBeanPopulator.populate(null, registry, parameterBean);
            String errorMessage = resultsBean.getErrorMessage();
            if (errorMessage != null) {
                throw new RegistryException(errorMessage);
            }
            return resultsBean.getResourceDataList();
        }

        public ResourceData[] search(int tenantId, Map<String, String> parameters) throws RegistryException {
            return search(SearchDataHolder.getInstance().getRegistryService().getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME, tenantId), parameters);
        }

        public ResourceData[] search(Map<String, String> parameters) throws RegistryException {
            return search(MultitenantConstants.SUPER_TENANT_ID, parameters);
        }
    }
}

