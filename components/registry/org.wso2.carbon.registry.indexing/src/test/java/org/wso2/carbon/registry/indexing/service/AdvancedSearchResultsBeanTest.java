/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.registry.indexing.service;

import org.junit.Test;
import org.wso2.carbon.registry.common.ResourceData;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class AdvancedSearchResultsBeanTest {
    @Test
    public void getErrorMessage() throws Exception {
        AdvancedSearchResultsBean bean = new AdvancedSearchResultsBean();
        String errMsg = "Error generating search results";
        bean.setErrorMessage(errMsg);
        assertEquals(errMsg, bean.getErrorMessage());
    }

    @Test
    public void getResourceDataList() throws Exception {
        AdvancedSearchResultsBean bean = new AdvancedSearchResultsBean();
        ResourceData[] resourceDatas = new ResourceData[] {new ResourceData()};
        bean.setResourceDataList(resourceDatas);
        assertArrayEquals(resourceDatas, bean.getResourceDataList());
    }

    @Test
    public void getErrorMessageInSearchResultsBean() {
        SearchResultsBean bean = new SearchResultsBean();
        String errMsg = "Error generating search results";
        bean.setErrorMessage(errMsg);
        assertEquals(errMsg, bean.getErrorMessage());
    }
}