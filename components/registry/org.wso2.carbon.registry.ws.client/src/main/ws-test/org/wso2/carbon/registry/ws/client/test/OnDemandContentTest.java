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
package org.wso2.carbon.registry.ws.client.test;

import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.ws.client.resource.OnDemandContentResourceImpl;


public class OnDemandContentTest extends TestSetup {

	public OnDemandContentTest(String text) {
	    super(text);
    }

	public void testOnDemandContent() {
		try {
			String testPath = "ondemand/test";
	        Resource r1 = registry.newResource();
	        r1.setContent(RegistryUtils.encodeString("This is test content. It should not be loaded unless getContent() is called."));
	        registry.put(testPath, r1);
	        
	        OnDemandContentResourceImpl r1_get = (OnDemandContentResourceImpl) registry.get(testPath);
	        r1_get.setClient(null);
	        Object content = null;
	        try {
	        	content = r1_get.getContent();
	        	assertNull("Resource content should not exist",content);
	        	fail("Content has not been pre-fetched, not on demand");
	        } catch (Exception e) {
	        	e.printStackTrace(); // printing stack trace for verification.
	        }
	        
	        Resource r1_get2 = registry.get(testPath);
	        content = r1_get2.getContent();
	        assertNotNull("Resource content should be fetched on demand", content);
        } catch (Exception e) {
	        e.printStackTrace();
        }
	}
}
