/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.social.api.utils;

import java.util.Date;

public interface FilterOptions {

    public String getSortBy();

    public void setSortBy(String sortBy);

    public SortOrder getSortOrder();

    public void setSortOrder(SortOrder sortOrder);

    public String getFilter();

    public void setFilter(String filter);

    public FilterOperation getFilterOperation();

    public void setFilterOperation(FilterOperation filterOperation);

    public String getFilterValue();

    public void setFilterValue(String filterValue);

    public int getFirst();

    public void setFirst(int first);

    public int getMax();

    public void setMax(int max);

    public Date getUpdatedSince();

    public void setUpdatedSince(Date updatedSince);


}


