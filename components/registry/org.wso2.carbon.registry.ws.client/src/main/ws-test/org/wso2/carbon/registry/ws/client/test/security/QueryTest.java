/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.wso2.carbon.registry.ws.client.test.security;

import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

public class QueryTest extends SecurityTestSetup {
    public QueryTest(String text) {
        super(text);
    }

    public void testputRegistryQueries() throws RegistryException {

        String QUERY_EPR_BY_PATH = "/Queries1/EPRByPath";
        Resource resource1 = null;

        try {

            resource1 = registry.newResource();
            String sql = "SELECT PATH FROM REG_RESOURCE WHERE  REG_PATH LIKE ?";
            resource1.setContent(sql);
            resource1.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);
            resource1.addProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME,
                    RegistryConstants.RESOURCES_RESULT_TYPE);

            boolean exists = registry.resourceExists(QUERY_EPR_BY_PATH);

            if (!exists)
                registry.put(QUERY_EPR_BY_PATH, resource1);

            assertTrue("Resource doesn't exists", registry.resourceExists(QUERY_EPR_BY_PATH));

        } catch (Exception e) {
            e.printStackTrace();
        }

//        Resource r1 = registry.get(QUERY_EPR_BY_PATH);
//        assertEquals("File content is not matching", new String((byte[])resource1.getContent()),
//                    new String((byte[])r1.getContent()));
//
//        assertEquals("Media type doesn't match",RegistryConstants.SQL_QUERY_MEDIA_TYPE, r1.getMediaType());
//        assertEquals("Media type doesn't match",RegistryConstants.SQL_QUERY_MEDIA_TYPE, "application/vnd.sql.query");
    }
}
