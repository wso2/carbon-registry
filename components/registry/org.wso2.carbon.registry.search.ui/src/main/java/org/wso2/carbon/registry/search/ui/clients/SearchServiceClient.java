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

package org.wso2.carbon.registry.search.ui.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.pagination.PaginationUtils;
import org.wso2.carbon.registry.search.ui.Utils;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceStub;
import org.wso2.carbon.registry.search.stub.beans.xsd.*;
import org.wso2.carbon.registry.search.stub.common.xsd.ResourceData;
import org.wso2.carbon.ui.CarbonUIUtil;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class SearchServiceClient {

    private static final Log log = LogFactory.getLog(SearchServiceClient.class);

    private SearchAdminServiceStub stub;
    private String epr;
    private HttpSession session;

    public SearchServiceClient(String cookie, ServletConfig config, HttpSession session)
            throws RegistryException {
        this.session =session;
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.
                getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        epr = backendServerURL + "SearchAdminService";

        try {
            stub = new SearchAdminServiceStub(configContext, epr);

            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        } catch (AxisFault axisFault) {
            String msg = "Failed to initiate search service client. " + axisFault.getMessage();
            log.error(msg, axisFault);
            throw new RegistryException(msg, axisFault);
        }
    }

    public SearchResultsBean getSearchResults(HttpServletRequest request) throws Exception {

        String searchType = (String) Utils.getParameter(request, "searchType");
        String criteria = (String) Utils.getParameter(request, "criteria");
        SearchResultsBean bean = null;
        try {
            bean = stub.getSearchResults(searchType, criteria);
            if (bean.getResourceDataList() == null) {
                bean.setResourceDataList(new ResourceData[0]);
            }
        } catch (Exception e) {
            String msg = "Failed to get search results from the search service. " +
                         e.getMessage();
            log.error(msg, e);
            throw new Exception(msg);
        }

        return bean;
    }

    public AdvancedSearchResultsBean getAdvancedSearchResults(HttpServletRequest request) throws Exception {    	
        CustomSearchParameterBean paramterBean = getSearchParameterBeanFromRequest(request);
//        String resourceName = request.getParameter("resourcePath");
//        String authorName = request.getParameter("author");
//        String updaterName = request.getParameter("updater");
//        String createdAfter = request.getParameter("createdAfter");
//        String createdBefore = request.getParameter("createdBefore");
//        String updatedAfter = request.getParameter("updatedAfter");
//        String updatedBefore = request.getParameter("updatedBefore");
//        String tags = request.getParameter("tags");
//        String commentWords = request.getParameter("commentWords");
//        String propertyName = request.getParameter("propertyName");
//        String propertyValue = request.getParameter("propertyValue");
//        String content = request.getParameter("content");
        AdvancedSearchResultsBean bean = null;


        try {
            if(PaginationContext.getInstance() == null){

                bean = stub.getAdvancedSearchResults(paramterBean);
            }       else {
                PaginationUtils.copyPaginationContext(stub._getServiceClient());
                bean = stub.getAdvancedSearchResults(paramterBean);
                int rowCount = PaginationUtils.getRowCount(stub._getServiceClient());
                session.setAttribute("row_count", Integer.toString(rowCount));
            }
//            bean = stub.getAdvancedSearchResults(resourceName, authorName, updaterName, createdAfter,
//                                                 createdBefore, updatedAfter, updatedBefore, tags, commentWords, propertyName,
//                                                 propertyValue, content);
            if (bean.getResourceDataList() == null) {
                bean.setResourceDataList(new ResourceData[0]);
            }
        } catch (Exception e) {
            String msg = "Failed to get advanced search results from the search service. " +
                         e.getMessage();
            log.error(msg, e);
            throw new Exception(msg);
        } finally {
            PaginationContext.destroy();
        }

        return bean;
    }

    private CustomSearchParameterBean getSearchParameterBeanFromRequest(
            HttpServletRequest request) {
        CustomSearchParameterBean paramterBean = new CustomSearchParameterBean();
        try {

            String s = request.getParameter("parameterList");

            String[] tempList = s.split("\\|");
            String[][] parameterList = new String[tempList.length][];

            ArrayOfString[] arrayOfStrings = new ArrayOfString[tempList.length];

            for (int i = 0; i < tempList.length; i++) {
                parameterList[i] = tempList[i].split("\\^", 2);
            }

//            System.out.println("sdfs");


            for (int i = 0; i < parameterList.length; i++) {
//                String[] temp = new String[2];
                ArrayOfString arr = new ArrayOfString();
                arr.addArray(parameterList[i][0]);

                if ("null".equals(parameterList[i][1])) {
                    arr.addArray("");

                } else {
                    arr.addArray(parameterList[i][1]);
                }
//                arrayOfStrings[i].setArray(temp);
                arrayOfStrings[i] = arr;
            } 
            paramterBean.setParameterValues(arrayOfStrings);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return paramterBean;
    }

    public MediaTypeValueList getMediaTypeParameterList(HttpServletRequest request) throws Exception {
        String mediaType = request.getParameter("mediaType");
        mediaType = mediaType.replace(" ", "+");
        return stub.getMediaTypeSearch(mediaType);
    }


    public void saveSearchFilter(HttpServletRequest request, String filterName) throws Exception {

        CustomSearchParameterBean bean = getSearchParameterBeanFromRequest(request);

        try {
            stub.saveAdvancedSearchFilter(bean, filterName);

        } catch (Exception e) {
            String msg = "Failed to save search filter using the search service. " +
                         e.getMessage();
            log.error(msg, e);
            throw new Exception(msg);
        }
    }

    public void deleteSearchFilter(String filterName) throws Exception {
            stub.deleteFilter(filterName);
    }


    public String[] getSavedFilters() throws Exception {
        try {
            return stub.getSavedFilters();
        } catch (Exception e) {
            String msg = "Failed to get search filter names from the search service. " +
                         e.getMessage();
            log.error(msg, e);
            throw new Exception(msg);
        }
    }

    public CustomSearchParameterBean getAdvancedSearchFilter(String filterName) throws Exception {
        try {
            return stub.getAdvancedSearchFilter(filterName);
        } catch (Exception e) {
            String msg = "Failed to get search filter from the search service. " +
                         e.getMessage();
            log.error(msg, e);
            throw new Exception(msg);
        }

    }
}
