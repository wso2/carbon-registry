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
package org.wso2.carbon.registry.ws.client.resource;

import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.io.InputStream;

public class OnDemandContentResourceImpl extends ResourceImpl {
	
	private String pathWithVersion;

	public String getPathWithVersion() {
		return pathWithVersion;
	}

	public void setPathWithVersion(String pathWithVersion) {
		this.pathWithVersion = pathWithVersion;
	}

	public OnDemandContentResourceImpl(WSRegistryServiceClient client) {
		this.client = client;
	}

	public WSRegistryServiceClient getClient() {
		return client;
	}

	public void setClient(WSRegistryServiceClient client) {
		this.client = client;
	}

	private WSRegistryServiceClient client;

    @Override
    public InputStream getContentStream() throws RegistryException {
        // If there is no content, try to fetch it.
        if (content == null && !contentModified) {
            getContent();
        }
        return super.getContentStream();
    }

    @Override
	public Object getContent() throws RegistryException {
		try {
			if (content == null && !contentModified) { // fetch data only if it hasn't been fetched previously
				content = getClient().getContent(getPathWithVersion());
			}
			return content;
		} catch (Exception e) {
			throw new RegistryException("Failed to get resource content", e);
		}
	}

}
