/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.indexing.utils;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URL;
import java.util.Properties;

public class IndexingUtils {
	
//	public static String getSolrCoreUrl(int tenantId) throws IOException {
//		String baseURL = IndexingUtils.getSolrUrl();
//		return baseURL + "/admin/cores/" + tenantId;
//		
//	}

    public static boolean isAuthorized(UserRegistry registry, String resourcePath, String action)
                            throws RegistryException{
        UserRealm userRealm = registry.getUserRealm();
        String userName = registry.getUserName();
        try {
            if (!userRealm.getAuthorizationManager().isUserAuthorized(userName,
                    resourcePath, action)) {
                return false;
            }
        } catch (UserStoreException e) {
            throw new RegistryException("Error at Authorizing " + resourcePath
                    + " with user " + userName + ":" + e.getMessage(), e);
        }
        return true;
    }

    public static byte[] readBytesFromInputSteam(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int read = in.read(buf);

		while (read != -1) {
			out.write(buf, 0, read);
			read = in.read(buf);
		}
		return out.toByteArray();
	}

	public static byte[] getByteContent(Resource resource, String sourceURL)
	                        throws RegistryException {
		try {
			InputStream is = null;
			if (sourceURL != null) {
				is = new URL(sourceURL).openStream();
			} else {
                Object content = resource.getContent();
                if( null == content) {
                    //returning an empty array, rather than 'null'.
                    return new byte[0];
                }
                is = resource.getContentStream();
				if (is == null) {
                    if (content instanceof byte[]) {
                        return (byte[]) content;
                    } else if (content instanceof String) {
                        return RegistryUtils.encodeString((String) content);
                    } else {
                        throw new RegistryException("Unknown type found as content " + content);
                    }
                }
			}
			return readBytesFromInputSteam(is);
		} catch (IOException e) {
			throw new RegistryException("Error at indexing", e);
		}
	}

    public static String getLoggedInUserName(){
        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
    }
}
