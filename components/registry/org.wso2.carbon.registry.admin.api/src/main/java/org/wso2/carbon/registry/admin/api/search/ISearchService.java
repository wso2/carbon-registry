package org.wso2.carbon.registry.admin.api.search;

import org.wso2.carbon.registry.core.exceptions.RegistryException;

/**
 * This provides functionality to search resources available in the registry by meta-data.
 * <p>
 * <strong> Using service clients for SearchService</strong>
 *
 * <p>
 * When using generated clients to access SearchService, ArrayOfString objects have to be used as
 * demonstrated in the following example.
 *
 * <p>
 * <i>Example : Searching resources created by "admin" and having "foo" as a property name </i>
 * <p>
 *
 * <code>
 * CustomSearchParameterBean parameterBean = new CustomSearchParameterBean(); <br>
 * ArrayOfString[] aos = new ArrayOfString[2]; // No. of parameters to be passed is 2, namely author
 *                                               and property name <br>
 * <br>
 * aos[0] = new ArrayOfString();   <br>
 * aos[0].addArray("author"); // search parameter name          <br>
 * aos[0].addArray("admin"); // search parameter value    <br>
 * <br>
 * aos[1] = new ArrayOfString();       <br>
 * aos[1].addArray("propertyName"); // search parameter name     <br>
 * aos[1].addArray("foo"); // search parameter value     <br>
 * <br>
 * parameterBean.setParameterValues(aos);        <br>
 * <br>
 * SearchAdminServiceStub ssClient = createSearchService(cookie, serverURL, configContext);    <br>
 * AdvancedSearchResultsBean bean = ssClient.getAdvancedSearchResults(parameterBean);  <br>
 * </code>
 *
 * <p>
 * CustomParameterBean encapsulates an ArrayOfString array which will contain all the parameters to
 * be sent to SearchService. Altogether, this array can have up to 13 elements each representing
 * "resourcePath", "author", "updater", "createdAfter", "createdBefore", "updatedAfter",
 * "updatedBefore", "commentWords", "tags", "propertyName", "propertyValue", "content" or
 * "mediaType". In the above case only "author" and "propertyName" have been used.
 * <p>
 * Each element should be an ArrayOfString object which has the parameter name (one of the 13 listed
 *  above) as the zeroth element and corresponding parameter value (search key) as the 1st element.
 * <p>
 * <br />
 * <b>Statistics:</b>
 * <ul>
 * <li>getSearchResults</li>
 * <li>getAdvancedSearchResults</li>
 * </ul>
 *
 * @param <CustomSearchParameterBean> a bean containing search parameters.
 * @param <SearchResultsBean> a bean storing results of basic search (tag search)
 * @param <AdvancedSearchResultsBean> a bean storing results of advanced search
 *
 */

public interface ISearchService <SearchResultsBean, AdvancedSearchResultsBean, CustomSearchParameterBean, MediaTypeValueList> {

	/**
	 * This method generates a search query that runs against the Registry and returns matching results
	 * @param searchType - String which indicates whether the search is a 'tag' search or a 'content' search
	 * @param criteria - The actual tags or content that needs to be searched for
	 * @return a bean containing an array of resource data of the results
	 * @throws RegistryException - if Registry is not available
	 */
	public abstract SearchResultsBean getSearchResults(String searchType,
			String criteria) throws RegistryException;

	/**
	 * This method generates a search query according to parameters passed, and runs this query against the Registry and returns matching resutls 
	 * @param parameters - Bean which contains a 2D array with parameters and their respective values
	 * @return a bean containing an array of resource data of the results
	 * @throws RegistryException - if Registry is not available
	 */
	public abstract AdvancedSearchResultsBean getAdvancedSearchResults(
			CustomSearchParameterBean parameters) throws RegistryException;

	/**
	 * Returns the 
	 * @param mediaType - String containing the media type. Ex: "application/xml"
	 * @return
	 * @throws RegistryException
	 */
	public abstract MediaTypeValueList getMediaTypeSearch(String mediaType)
	throws RegistryException;

	/**
	 * Saves the advanced search fields as a filter for later retrieval
	 * @param queryBean - Bean with parameters and their respective values
	 * @param filterName - Name to be saved for this filter
	 * @throws RegistryException - if Registry is not available
	 */
	public abstract void saveAdvancedSearchFilter(
			CustomSearchParameterBean queryBean, String filterName)
	throws RegistryException;

	/**
	 * Returns the filter to populate advanced search fields
	 * @param filterName - Saved filter name
	 * @return - Bean with parameters and their respective values
	 * @throws RegistryException
	 */
	public abstract CustomSearchParameterBean getAdvancedSearchFilter(
			String filterName) throws RegistryException;

	/**
	 * Return names of all saved filters
	 * @return Array of String containing all saved filter names
	 * @throws RegistryException
	 */
	public abstract String[] getSavedFilters() throws RegistryException;

    /**
     * Delete a selected filter
     * @throws RegistryException
     */
    public void deleteFilter(String filterName) throws RegistryException;

}