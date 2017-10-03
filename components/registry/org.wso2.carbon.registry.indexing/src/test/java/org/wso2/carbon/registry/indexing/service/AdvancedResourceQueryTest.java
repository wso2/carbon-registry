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

import junit.framework.TestCase;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.wso2.carbon.registry.common.utils.CommonUtil;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.util.*;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;

public class AdvancedResourceQueryTest extends TestCase {
    public void testExecute() throws Exception {
        AdvancedResourceQuery query = new AdvancedResourceQuery();
        query.setResourceName("testResource");
        query.setAuthorName("admin");
        query.setUpdaterName("admin");
        query.setCreatedAfter(CommonUtil.computeDate("09/01/2017"));
        query.setCreatedBefore(CommonUtil.computeDate("09/27/2017"));
        query.setUpdatedAfter(CommonUtil.computeDate("09/01/2017"));
        query.setUpdatedBefore(CommonUtil.computeDate("09/27/2017"));
        query.setCommentWords("test");
        query.setTags("test,unit");
        query.setPropertyName("prop1");
        query.setPropertyValue("propval1");
        query.setContent(null);

        Collection resource = PowerMockito.mock(Collection.class);
        UserRegistry registry = PowerMockito.mock(UserRegistry.class);
        Mockito.when(resource.getAspects()).thenReturn(Arrays.asList("ServiceLifecycle"));
        Mockito.when(resource.getAuthorUserName()).thenReturn("admin");
        Mockito.when(resource.getCreatedTime()).thenReturn(Calendar.getInstance().getTime());
        Mockito.when(resource.getLastModified()).thenReturn(Calendar.getInstance().getTime());
        Mockito.when(resource.getContent()).thenReturn(null);
        Mockito.when(resource.getDescription()).thenReturn("Testing Resource");
        Mockito.when(resource.getMediaType()).thenReturn("application/test");
        Mockito.when(resource.getPath()).thenReturn("/_system/local/temp");
        Mockito.when(resource.getContent()).thenReturn(new String[]{"/_system/local/temp"});
        Properties properties = new Properties();
        properties.put("key1", Arrays.asList("val1"));
        properties.put("key2", Arrays.asList("val12"));
        Mockito.when(resource.getProperties()).thenReturn(properties);

        Mockito.when(registry.executeQuery(anyString(),(Map) anyObject())).thenReturn(resource);
        CollectionImpl coll = new CollectionImpl();
        coll.setAuthorUserName("admin");
        Mockito.when(registry.newCollection()).thenReturn(coll);

        Resource resource1 = query.execute(registry);
        assertNotNull(resource1.getContent());
        String[] results = (String[]) resource1.getContent();
        assertEquals(1, results.length);
        assertEquals("/_system/local/temp", results[0]);
    }

    public void testExecuteWithoutQuery() throws RegistryException {
        try {
            AdvancedResourceQuery query = new AdvancedResourceQuery();
            UserRegistry registry = PowerMockito.mock(UserRegistry.class);
            query.execute(registry);
            fail("Operation invalid exception is missing");
        } catch (RegistryException e) {
            assertEquals("No parameters are specified for the query.", e.getMessage());
        }
    }

    public void testParseTags() throws Exception {
        AdvancedResourceQuery query = new AdvancedResourceQuery();
        String tags = "tag1,tag2,tag3";
        Set<String> tagsSet = query.parseTags(tags);
        assertNotNull(tagsSet);
        assertEquals(3, tagsSet.size());
    }

}
