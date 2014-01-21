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

import java.util.HashSet;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.SimpleCredentials;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import org.apache.jackrabbit.server.CredentialsProvider;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.DavSession;
import org.apache.jackrabbit.webdav.DavSessionProvider;
import org.apache.jackrabbit.webdav.WebdavRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

public class RegistrySessionProvider implements DavSessionProvider{
    private static Logger log = LoggerFactory.getLogger(RegistrySessionProvider.class);
    
    private CredentialsProvider credentialsProvider;
    private ThreadLocal<RegistryWebDavContext> threadLocal;
    private WebDavEnviorment enviorment;

	public RegistrySessionProvider(CredentialsProvider credentialsProvider, ThreadLocal<RegistryWebDavContext> threadLocal, WebDavEnviorment enviorment) {
		this.credentialsProvider = credentialsProvider;
		this.threadLocal = threadLocal;
		this.enviorment = enviorment;
		this.enviorment.setSessionProvider(this);
	}

	public boolean attachSession(WebdavRequest request) throws DavException {
		try {
			HttpSession session = request.getSession(true);
			RegistryWebDavContext webdavContext = (RegistryWebDavContext) session
					.getAttribute(RegistryServlet.WEBDAV_CONTEXT);
			
			if(webdavContext == null){
				Credentials credentials = credentialsProvider.getCredentials(request);
				SimpleCredentials simpleCredentials = (SimpleCredentials)credentials;
				
				
				
				final String userID = simpleCredentials.getUserID();
				String password = new String(simpleCredentials.getPassword());
				
				if(userID == null || password == null || userID.length() == 0 || password.length() == 0){
					throw new DavException(DavServletResponse.SC_UNAUTHORIZED,"Bassic HTTP Autentication is required");
				}
				//TODOD remove this
//				String userID = "admin";
//				String password = "admin";
				
				Registry registry = WebdavServiceComponet.getRegistryInstance(
						userID, password);

				webdavContext = new RegistryWebDavContext(registry, request.getContextPath());
				webdavContext.setEnviorment(enviorment);
				session.setAttribute(RegistryServlet.WEBDAV_CONTEXT, webdavContext);
			}
			threadLocal.set(webdavContext);
			
			request.setDavSession(new DavSession() {
				private final HashSet lockTokens = new HashSet();
				public void removeReference(Object reference) {
				}
			
				public void removeLockToken(String token) {
					lockTokens.remove(token);
				}
			
				public String[] getLockTokens() {
					 return (String[]) lockTokens.toArray(new String[lockTokens.size()]);
				}
			
				public void addReference(Object reference) {
						
				}
			
				public void addLockToken(String token) {
					lockTokens.add(token);
				}
			});
			webdavContext.setSession(request.getDavSession());
			return true;
		} catch (LoginException e) {
			e.printStackTrace();
			throw new DavException(DavServletResponse.SC_BAD_REQUEST,e);
		} catch (ServletException e) {
			e.printStackTrace();
			throw new DavException(DavServletResponse.SC_BAD_REQUEST,e);
		} catch (RegistryException e) {
			e.printStackTrace();
			throw new DavException(DavServletResponse.SC_BAD_REQUEST,e);
		}
	}

	public void releaseSession(WebdavRequest request) {
		// TODO We use http session here, hence nothing to be done
	}
}
