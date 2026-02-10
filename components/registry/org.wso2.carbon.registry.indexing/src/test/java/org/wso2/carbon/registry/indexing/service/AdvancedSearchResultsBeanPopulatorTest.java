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
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Properties;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AdvancedSearchResultsBeanPopulatorTest {
    @Test
    public void testPopulate() throws Exception {

        ResourceImpl resource = mock(ResourceImpl.class);
        UserRegistry registry = mock(UserRegistry.class);
        when(resource.getAspects()).thenReturn(Arrays.asList("ServiceLifecycle"));
        when(resource.getAuthorUserName()).thenReturn("admin");
        when(resource.getCreatedTime()).thenReturn(Calendar.getInstance().getTime());
        when(resource.getLastModified()).thenReturn(Calendar.getInstance().getTime());
        when(resource.getContent()).thenReturn(null);
        when(resource.getDescription()).thenReturn("Testing Resource");
        when(resource.getMediaType()).thenReturn("application/test");
        when(resource.getPath()).thenReturn("/_system/local/temp");
        when(resource.getContent()).thenReturn(new String[]{"/_system/local/temp"});
        Properties properties = new Properties();
        properties.put("key1", Arrays.asList("val1"));
        properties.put("key2", Arrays.asList("val12"));
        when(resource.getProperties()).thenReturn(properties);

        Collection collection = mock(Collection.class);
        when(collection.getContent()).thenReturn(new String[]{"/_system/local/temp"});
        when(registry.executeQuery(any(),any())).thenReturn(collection);
        when(registry.get("/_system/local/temp")).thenReturn(resource);
        CollectionImpl coll = new CollectionImpl();
        coll.setAuthorUserName("admin");
        when(registry.newCollection()).thenReturn(coll);

        AdvancedSearchResultsBean resultsBean = AdvancedSearchResultsBeanPopulator.populate(registry, "testResource",
                "admin",
                "admin", "09/01/2017",
                "09/01/2017", "09/01/2017","09/27/2017", "test,unit", "test", "prop1", "propval1",
                "Testing Search Result Bean Populator");


    }

    @Test
    public void testPopulateWithoutQuery() {
        UserRegistry registry = mock(UserRegistry.class);
        AdvancedSearchResultsBean resultsBean = AdvancedSearchResultsBeanPopulator.populate(registry, null,
                null,
                null, null,
                null, null,null, null, null, null, null,
                null);
        assertEquals("Failed to get advanced search results. No parameters are specified for the query.",
                resultsBean.getErrorMessage());
    }
}
