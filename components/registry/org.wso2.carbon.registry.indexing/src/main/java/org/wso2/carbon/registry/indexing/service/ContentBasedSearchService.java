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

package org.wso2.carbon.registry.indexing.service;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.TermsResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.admin.api.indexing.IContentBasedSearchService;
import org.wso2.carbon.registry.common.ResourceData;
import org.wso2.carbon.registry.common.TermData;
import org.wso2.carbon.registry.common.services.RegistryAbstractAdmin;
import org.wso2.carbon.registry.common.utils.CommonUtil;
import org.wso2.carbon.registry.common.utils.UserUtil;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.pagination.PaginationUtils;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.indexing.IndexingConstants;
import org.wso2.carbon.registry.indexing.IndexingManager;
import org.wso2.carbon.registry.indexing.indexer.IndexerException;
import org.wso2.carbon.registry.indexing.solr.SolrClient;
import org.wso2.carbon.registry.indexing.utils.IndexingUtils;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class ContentBasedSearchService extends RegistryAbstractAdmin 
        implements IContentBasedSearchService {
	private static final Log log = LogFactory.getLog(ContentBasedSearchService.class);

	private String solrServerUrl;


	public String getSolrUrl(int tenantId) throws IOException, FileNotFoundException, RegistryException {
		if(solrServerUrl == null){
			solrServerUrl = IndexingUtils.getSolrUrl();
		}	
		return solrServerUrl;
	}


	public SearchResultsBean getContentSearchResults(String searchQuery) throws AxisFault{
        try {
			UserRegistry registry = (UserRegistry) getRootRegistry();
            return searchContent(searchQuery, registry);
		} catch (Exception e) {
			log.error("Error " + e.getMessage() + "at the content search back end component.", e );
		}
        return new SearchResultsBean();
	}

    public SearchResultsBean getAttributeSearchResults(String[][] attributes) throws AxisFault{

        try {
            final Map<String, String> map = new HashMap<String, String>(attributes.length);
            UserRegistry registry = (UserRegistry) getRootRegistry();
            for (String[] mapping : attributes) {
                map.put(mapping[0], mapping[1]);
            }
            return searchByAttribute(map, registry);
        } catch (Exception e) {
            log.error("Error occurred while getting the attribute search result.", e );
        }
        return new SearchResultsBean();
    }

    private String[] sortByDateIfRequired(String[] authorizedPaths, final UserRegistry registry, PaginationContext paginationContext) throws RegistryException {
        if(paginationContext.getSortBy().equalsIgnoreCase("meta_created_date")) {
            if(paginationContext.getSortOrder().equalsIgnoreCase("ASC")) {
                Arrays.sort(authorizedPaths, new Comparator<String>() {
                    public int compare(String path1, String path2) {
                        try {
                            return registry.getMetaData(path1).getCreatedTime().compareTo(registry.getMetaData(path2).getCreatedTime());
                        } catch(RegistryException ex) {
                            return 0 ;
                        }
                    }
                });
            } else if(paginationContext.getSortOrder().equalsIgnoreCase("DES")) {
                Arrays.sort(authorizedPaths, new Comparator<String>() {
                    public int compare(String path1, String path2) {
                        try {
                            return registry.getMetaData(path2).getCreatedTime().compareTo(registry.getMetaData(path1).getCreatedTime());
                        } catch(RegistryException ex) {
                            return 0 ;
                        }
                    }
                });
            }
        } else if(paginationContext.getSortBy().equalsIgnoreCase("meta_last_updated_date")) {
            if(paginationContext.getSortOrder().equalsIgnoreCase("ASC")) {
                Arrays.sort(authorizedPaths, new Comparator<String>() {
                    public int compare(String path1, String path2) {
                        try {
                            return registry.getMetaData(path1).getLastModified().compareTo(registry.getMetaData(path2).getLastModified());
                        } catch(RegistryException ex) {
                            return 0 ;
                        }
                    }
                });
            } else if(paginationContext.getSortOrder().equalsIgnoreCase("DES")) {
                Arrays.sort(authorizedPaths, new Comparator<String>() {
                    public int compare(String path1, String path2) {
                        try {
                            return registry.getMetaData(path2).getLastModified().compareTo(registry.getMetaData(path1).getLastModified());
                        } catch(RegistryException ex) {
                            return 0 ;
                        }
                    }
                });
            }
        }

        return authorizedPaths;
    }

    /**
     * Method to get the SolrDocumentList
     * @param searchQuery search query
     * @param attributes search attributes map
     * @param registry Registry
     * @return SearchResultsBean
     * @throws IndexerException
     * @throws RegistryException
     */
    private SearchResultsBean searchContentInternal(String searchQuery, Map<String, String> attributes,
            UserRegistry registry) throws IndexerException, RegistryException {
        SearchResultsBean resultsBean = new SearchResultsBean();
        SolrClient client = SolrClient.getInstance();
        // To verify advance search and metadata search
        boolean isMetaDataSearch = true;
        String advanceSearchAttribute = attributes.get(IndexingConstants.ADVANCE_SEARCH);
        if (advanceSearchAttribute != null && advanceSearchAttribute.equals("true")) {
            isMetaDataSearch = false;
            attributes.remove(IndexingConstants.ADVANCE_SEARCH);
        }
        SolrDocumentList results = attributes.size() > 0 ? client.query(registry.getTenantId(), attributes) :
                client.query(searchQuery, registry.getTenantId());

        if (log.isDebugEnabled())
            log.debug("result received " + results);

        List<ResourceData> filteredResults = new ArrayList<ResourceData>();
        // TODO: Proper mechanism once authroizations are fixed - senaka
        //        for (SolrDocument solrDocument : results){
        //            String path = getPathFromId((String)solrDocument.getFirstValue("id"));
        //            if ((isAuthorized(registry, path, ActionConstants.GET)) && (registry.resourceExists(path))) {
        //                filteredResults.add(loadResourceByPath(registry, path));
        //            }
        //        }
        // -- end of proper mechanism

        MessageContext messageContext = MessageContext.getCurrentMessageContext();

        if (((messageContext != null && PaginationUtils.isPaginationHeadersExist(messageContext))
                || PaginationContext.getInstance() != null) && isMetaDataSearch) {
            try {
                PaginationContext paginationContext;
                if (messageContext != null) {
                    paginationContext = PaginationUtils.initPaginationContext(messageContext);
                } else {
                    paginationContext = PaginationContext.getInstance();
                }
                List<String> authorizedPathList = new ArrayList<String>();
                for (SolrDocument solrDocument : results) {
                    if (paginationContext.getLimit() > 0 && authorizedPathList.size() == paginationContext.getLimit()) {
                        break;
                    }
                    String path = getPathFromId((String) solrDocument.getFirstValue("id"));
                    if (registry.resourceExists(path) && isAuthorized(registry, path, ActionConstants.GET)) {
                        authorizedPathList.add(path);
                    }
                }
                String[] authorizedPaths = authorizedPathList.toArray(new String[authorizedPathList.size()]);

                sortByDateIfRequired(authorizedPaths, registry, paginationContext);

                String[] paginatedPaths;
                int start = paginationContext.getStart();
                int count = paginationContext.getCount();
                int rowCount = authorizedPaths.length;
                if (messageContext != null) {
                    PaginationUtils.setRowCount(messageContext, Integer.toString(rowCount));
                }
                paginationContext.setLength(authorizedPaths.length);
                int startIndex;
                if (start == 1) {
                    startIndex = 0;
                } else {
                    startIndex = start;
                }
                if (rowCount < start + count) {
                    paginatedPaths = new String[rowCount - startIndex];
                    System.arraycopy(authorizedPaths, startIndex, paginatedPaths, 0, (rowCount - startIndex));
                } else {
                    paginatedPaths = new String[count];
                    System.arraycopy(authorizedPaths, startIndex, paginatedPaths, 0, count);
                }
                for (String path : paginatedPaths) {
                    ResourceData resourceData = loadResourceByPath(registry, path);
                    if (resourceData != null) {
                        filteredResults.add(resourceData);
                    }
                }
            } finally {
                if (messageContext != null) {
                    PaginationContext.destroy();
                }
            }

        } else {
            for (SolrDocument solrDocument : results) {
                String path = getPathFromId((String) solrDocument.getFirstValue("id"));
                if ((isAuthorized(registry, path, ActionConstants.GET))) {
                    ResourceData resourceData = loadResourceByPath(registry, path);
                    if (resourceData != null) {
                        filteredResults.add(resourceData);
                    }
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("filtered results " + filteredResults + " for user " + registry.getUserName());
        }
        resultsBean.setResourceDataList(filteredResults.toArray(new ResourceData[filteredResults.size()]));
        return resultsBean;
    }

    public SearchResultsBean searchContent(String searchQuery,
                              UserRegistry registry) throws IndexerException, RegistryException {
        return searchContentInternal(searchQuery, Collections.<String, String>emptyMap(), registry);
    }

    public SearchResultsBean searchByAttribute(Map<String, String> attributes,
                                           UserRegistry registry) throws IndexerException, RegistryException {
        return searchContentInternal(null, attributes, registry);
    }

    public void restartIndexing() throws RegistryException {

        IndexingManager manager = IndexingManager.getInstance();
        manager.restartIndexing();
    }

	private String getPathFromId(String id) {
		return id.substring(0, id.lastIndexOf(IndexingConstants.FIELD_TENANT_ID));
	}

	private boolean isAuthorized(UserRegistry registry, String resourcePath, String action) throws RegistryException{
		UserRealm userRealm = registry.getUserRealm();
		String userName = getLoggedInUserName();

		try {
			if (!userRealm.getAuthorizationManager().isUserAuthorized(userName,
					resourcePath, action)) {
				return false;
			}
		} catch (UserStoreException e) {
			throw new RegistryException("Error at Authorizing " + resourcePath
					+ " with user " + userName + ":" + e.getMessage(), e);
		}

		return true;
	}

	private ResourceData loadResourceByPath(UserRegistry registry, String path) throws RegistryException {
		ResourceData resourceData = new ResourceData();
		resourceData.setResourcePath(path);

		if (path != null) {
			if (RegistryConstants.ROOT_PATH.equals(path)) {
				resourceData.setName("root");
			} else {
				String[] parts = path.split(RegistryConstants.PATH_SEPARATOR);
				resourceData.setName(parts[parts.length - 1]);
			}
		}
        Resource child;
        //---------------------------------------------------------------------------------------------------------Ajith
        //This fix is to improve the performance of the artifact search
        //When we delete the artifacts that goes to activity logs and Solr indexer delete that resource from indexed
        //files as well. Therefore no need an extra ResourceExist() check for the result path which is returned
        //from Solr search.
        try {
            child = registry.get(path);
        } catch (RegistryException e) {
            log.debug("Failed to load resource from path which is returned from Solr search" + e.getMessage());
            return null;
        }
        //---------------------------------------------------------------------------------------------------------Ajith
		resourceData.setResourceType(child instanceof Collection ? "collection"
				: "resource");
		resourceData.setAuthorUserName(child.getAuthorUserName());
		resourceData.setDescription(child.getDescription());
		resourceData.setAverageRating(registry
				.getAverageRating(child.getPath()));
		Calendar createdDateTime = Calendar.getInstance();
		createdDateTime.setTime(child.getCreatedTime());
		resourceData.setCreatedOn(createdDateTime);
		CommonUtil.populateAverageStars(resourceData);

		String user = child.getProperty("registry.user");

		if (registry.getUserName().equals(user)) {
			resourceData.setPutAllowed(true);
			resourceData.setDeleteAllowed(true);
			resourceData.setGetAllowed(true);
		} else {
			resourceData.setPutAllowed(
				UserUtil.isPutAllowed(registry.getUserName(), path, registry));
			resourceData.setDeleteAllowed(
				UserUtil.isDeleteAllowed(registry.getUserName(), path, registry));
			resourceData.setGetAllowed(
				UserUtil.isGetAllowed(registry.getUserName(), path, registry));
		}

		child.discard();

		return resourceData;
	}

	public static String getLoggedInUserName(){
        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
	}

    public SearchResultsBean searchTerms(Map<String, String> attributes, UserRegistry registry) throws IndexerException {
        SearchResultsBean resultsBean = new SearchResultsBean();
        SolrClient client = SolrClient.getInstance();
        List<FacetField.Count> results = client.facetQuery(registry.getTenantId(), attributes);

        if (log.isDebugEnabled()) {
            log.debug("result for the term search: " + results);
        }

        List<TermData> termDataList = new ArrayList<>();
        for (FacetField.Count count : results) {
            termDataList.add(new TermData(count.getName(),count.getCount()));
        }
        resultsBean.setTermDataList(termDataList.toArray(new TermData[termDataList.size()]));
        return resultsBean;
    }

}
