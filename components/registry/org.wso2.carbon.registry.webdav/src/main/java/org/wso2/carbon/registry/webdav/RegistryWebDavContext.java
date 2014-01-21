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

import java.util.HashMap;
import java.util.Map;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavSession;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;

public class RegistryWebDavContext {
	private final Registry registry;
    private final String contextPath;
	private Map<String, RegistryResource> resourceMap = new HashMap<String, RegistryResource>();
    private Map<String, Metadata> metadataMap = new HashMap<String, Metadata>();
	private org.wso2.carbon.registry.webdav.WebDavEnviorment enviorment;
	private DavSession session;

    private static class Metadata {
        private String value;
        private long lastUpdatedTime;

        private Metadata(String value) {
            this.value = value;
            this.lastUpdatedTime = System.currentTimeMillis();
        }

        public String getValue() {
            return value;
        }

        public long getLastUpdatedTime() {
            return lastUpdatedTime;
        }
    }

	public RegistryWebDavContext(Registry registry, String contextPath) {
		this.registry = registry;
        this.contextPath = contextPath;
	}

	public RegistryResource getRegistryResource(String path) throws DavException {
		RegistryResource resource =  resourceMap.get(path);
		if(resource == null){
			DavResourceLocator locatorVal = getEnviorment().getLocatorFactory().createResourceLocator(
					"/registry/resourcewebdav", "", path);
			resource = new RegistryResource(this,locatorVal);
			setRegistryResource(path, resource);
		}
		return resource;

	}

    public void saveDavResourceMimeType(RegistryResource resource) {
        if(!resource.isCollection()) {
            Resource r = resource.getUnderLineResource();
            metadataMap.put(r.getPath(), new Metadata(r.getMediaType()));
        }
    }

    public void updateDavResourceMimeType(RegistryResource resource) {
        Resource r = resource.getUnderLineResource();
        if (r.getMediaType() == null) {
            Metadata metadata = metadataMap.get(r.getPath());
            if (null != metadata) {
                final String mimeType = metadata.getValue();
                long currentSysTime = System.currentTimeMillis();
                //fetch the media type from the session, ONLY if the time laps is less than 15s, not for newly created files
                // with no extensions.
                if ((currentSysTime - metadata.getLastUpdatedTime()) < 15000 && null != mimeType) {
                    r.setMediaType(mimeType);
                }
                metadataMap.remove(r.getPath());
            }
        }
    }

	public void setRegistryResource(String path, RegistryResource resource) {
		resourceMap.put(path,resource);
	}

	public void removeRegistryResource(String path) {
		resourceMap.remove(path);
	}

	public Registry getRegistry() {
		return registry;
	}

    public String getContextPath() {
        return contextPath;
    }

	public void setEnviorment(WebDavEnviorment enviorment) {
		this.enviorment = enviorment;
	}

	public WebDavEnviorment getEnviorment() {
		return enviorment;
	}

	public DavSession getSession() {
		return session;
	}

	public void setSession(DavSession session) {
		this.session = session;
	}
}
