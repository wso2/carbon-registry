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
package org.wso2.carbon.registry.webdav;

import org.apache.jackrabbit.webdav.DavLocatorFactory;
import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavSessionProvider;

public class WebDavEnviorment {
	private DavSessionProvider sessionProvider;
	private DavResourceFactory resourceFactory;
	private DavLocatorFactory locatorFactory;
	
	public DavLocatorFactory getLocatorFactory() {
		return locatorFactory;
	}
	public void setLocatorFactory(DavLocatorFactory locatorFactory) {
		this.locatorFactory = locatorFactory;
	}
	public DavSessionProvider getSessionProvider() {
		return sessionProvider;
	}
	public void setSessionProvider(DavSessionProvider sessionProvider) {
		this.sessionProvider = sessionProvider;
	}
	public DavResourceFactory getResourceFactory() {
		return resourceFactory;
	}
	public void setResourceFactory(DavResourceFactory resourceFactory) {
		this.resourceFactory = resourceFactory;
	}
}
