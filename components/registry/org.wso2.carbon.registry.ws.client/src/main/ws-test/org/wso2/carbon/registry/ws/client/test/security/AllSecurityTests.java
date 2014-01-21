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
package org.wso2.carbon.registry.ws.client.test.security;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllSecurityTests ../p{



	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.wso2.carbon.registry.ws.client.test.security");
		//$JUnit-BEGIN$
		suite.addTestSuite(TestContentStream.class);
		suite.addTestSuite(TestAssociation.class);
		suite.addTestSuite(TestPaths.class);
		suite.addTestSuite(TestCopy.class);
		suite.addTestSuite(UserSecurityTest.class);
//		suite.addTestSuite(SecurityTestSetup.class);
		suite.addTestSuite(QueryTest.class);
		suite.addTestSuite(RemotePerfTest.class);
		suite.addTestSuite(ContinuousOperations.class);
		suite.addTestSuite(RenameTest.class);
		suite.addTestSuite(VersionHandlingTest.class);
		suite.addTestSuite(TestResources.class);
		suite.addTestSuite(TestMove.class);
		suite.addTestSuite(TestTagging.class);
		suite.addTestSuite(RatingTest.class);
		suite.addTestSuite(ResourceHandling.class);
		suite.addTestSuite(PropertiesTest.class);
		suite.addTestSuite(CommentTest.class);
		//$JUnit-END$
		return suite;
	}

}
