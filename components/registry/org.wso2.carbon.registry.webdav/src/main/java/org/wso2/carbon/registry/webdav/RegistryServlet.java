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

import java.io.IOException;

import javax.jcr.Repository;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.server.BasicCredentialsProvider;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavServletRequest;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.DavSession;
import org.apache.jackrabbit.webdav.WebdavRequest;
import org.apache.jackrabbit.webdav.WebdavResponse;
import org.apache.jackrabbit.webdav.jcr.JCRWebdavServerServlet;
import org.apache.jackrabbit.webdav.simple.SimpleWebdavServlet;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

public class RegistryServlet extends SimpleWebdavServlet {
    public static final Log log = LogFactory.getLog(SimpleWebdavServlet.class);

	public static final String WEBDAV_CONTEXT = "WebDavContext";

	private static ThreadLocal<RegistryWebDavContext> threadLocalData = new ThreadLocal<RegistryWebDavContext>();
//	private Repository repository;
	private WebDavEnviorment enviorment = new WebDavEnviorment();

	public void init() throws ServletException {
		super.init();
		log.debug("Starting Registry WebDAV Servlet");
		setResourceFactory(new DavResourceFactory() {
			public DavResource createResource(DavResourceLocator locator,
					DavServletRequest request, DavServletResponse response)
					throws DavException {
				return createResource(locator);

			}

			public DavResource createResource(DavResourceLocator locator,
					DavSession session) throws DavException {
				return createResource(locator);
			}
			
			private RegistryResource createResource(DavResourceLocator locator) throws DavException{
				try {
					RegistryWebDavContext webDavContext = threadLocalData.get();
					RegistryResource registryResource = new RegistryResource( webDavContext, locator);
					webDavContext.setRegistryResource(registryResource.getLocator().getResourcePath(), registryResource);
					return registryResource;
				} catch (DavException e) {
					e.printStackTrace();
					throw e;
				}
			}
		});
		enviorment.setResourceFactory(getResourceFactory());
		enviorment.setLocatorFactory(getLocatorFactory());
		
		
		setDavSessionProvider(new RegistrySessionProvider(
				new BasicCredentialsProvider(JCRWebdavServerServlet.INIT_PARAM_MISSING_AUTH_MAPPING),threadLocalData, enviorment));
		//setLockManager(lockManager);
//		repository = new UserRegistry(threadLocalData);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			log.debug("doGet() Invoked by " + req.getRemoteUser());
			findAndSetWebdavContext2ThreadLocal(req);
			super.doGet(req, resp);
		} catch (RegistryException e) {
			throw new ServletException(e);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			log.debug("doPost() Invoked by " + req.getRemoteUser());
			findAndSetWebdavContext2ThreadLocal(req);
			super.doPost(req, resp);
		} catch (RegistryException e) {
			throw new ServletException(e);
		}
	}

	@Override
    protected void doMkCol(WebdavRequest request, WebdavResponse response, DavResource resource)
            throws IOException, DavException {

        //  This piece of code was added as fix for the CARBON-9041 BUG, and proper code refactoring
        //  needs to happen in the RegistryResource.java
        //  TODO: This is a CARBON-9041 fix and remove with proper code refactoring.
        if (resource instanceof RegistryResource) {
            ((RegistryResource) resource).setUnderLineResource(new CollectionImpl());
        }
        super.doMkCol(request, response, resource);
    }

	/**
	 * This method retrieves the webdavContext from the session, and if not presents, do authenticate user, create new,
	 *  and sets the webdavContext to sessions.
	 * @param req    the http servlet request object.
	 * @throws IOException  the IO exception.
	 * @throws RegistryException the Registry exception.
	 */
	private void findAndSetWebdavContext2ThreadLocal(HttpServletRequest req)
			throws IOException, RegistryException {
		RegistryWebDavContext webdavContext;
		HttpSession session = req.getSession(true);
		webdavContext = (RegistryWebDavContext) session
				.getAttribute(WEBDAV_CONTEXT);

		if (webdavContext == null) {
			String[] credentails = retriveUserNameAndPassword(req);
			Registry registry;
			if (credentails != null) {
				registry = WebdavServiceComponet.getRegistryInstance(
						credentails[0], credentails[1]);
			} else {
				registry = WebdavServiceComponet
						.getRegistryInstance(null, null);
			}
			if (registry == null) {
				throw new RegistryException(
						"No Registry can be found for the registry");
			} else {

				webdavContext = new RegistryWebDavContext(registry, req.getContextPath());
				session.setAttribute(WEBDAV_CONTEXT, webdavContext);
			}
		}
		threadLocalData.set(webdavContext);
	}

	private String[] retriveUserNameAndPassword(HttpServletRequest request)
			throws IOException {
		String auth = request.getHeader("Authorization");

		// Do we allow that user?

		if (!auth.toUpperCase().startsWith("BASIC "))
			return null; // we only do BASIC

		// Get encoded user and password, comes after "BASIC "
		String userpassEncoded = auth.substring(6);

		// Decode it, using any base 64 decoder
		sun.misc.BASE64Decoder dec = new sun.misc.BASE64Decoder();
		String userpassDecoded = new String(dec.decodeBuffer(userpassEncoded));
		return userpassDecoded.split(":");
	}

	@Override
	public Repository getRepository() {
		return null;
	}
}
