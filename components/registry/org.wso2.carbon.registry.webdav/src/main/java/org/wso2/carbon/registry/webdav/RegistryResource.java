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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavCompliance;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.DavResourceIteratorImpl;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.DavSession;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.lock.ActiveLock;
import org.apache.jackrabbit.webdav.lock.LockInfo;
import org.apache.jackrabbit.webdav.lock.LockManager;
import org.apache.jackrabbit.webdav.lock.Scope;
import org.apache.jackrabbit.webdav.lock.Type;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.property.ResourceType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

public class RegistryResource implements DavResource {
	private Resource underLineResource;
	private Registry registry;
	private RegistryWebDavContext resourceCache;
	private DavResourceLocator locator;
	private String path;
	private boolean doesNotExists = false;
	private Map<DavPropertyName,DavProperty> properties = new HashMap<DavPropertyName,DavProperty>();
	private boolean lockable = false;
	private LockManager lockManager;
	private DavSession session;

	private static final String COMPLIANCE_CLASSES = DavCompliance.concatComplianceClasses( new String[] {DavCompliance._1_});

	public RegistryResource(RegistryWebDavContext webdavContext,
			DavResourceLocator locator) throws DavException {
		this.registry = webdavContext.getRegistry();
		if(registry == null){
			throw new DavException(DavServletResponse.SC_FORBIDDEN, "Registry Not Found");
		}
		this.locator = locator;
		this.resourceCache = webdavContext;
		String path = locator.getResourcePath();
		if(path.startsWith("/registry/resourcewebdav")){
			path = path.substring("/registry/resourcewebdav".length());
		}
		if(path.trim().length() == 0){
			path = "/";
		}
		this.path = path.trim();
		//this.session = session;
	}

	private Resource getUnderlineResource(){
		try {
			this.underLineResource = registry.get(path);
	        // adding the properties requested from the webDAV client
			addRequiredProperties();
			return underLineResource;
		} catch (RegistryException e) {
			doesNotExists = true;
			throw new RuntimeException(e);
		}
	}

    // add the properties required by the webDAV client
    private void addRequiredProperties() {
        if (underLineResource instanceof Collection) {
            addDavProperty(DavPropertyName.RESOURCETYPE, new ResourceType(ResourceType.COLLECTION));
            // Windows XP support
            addDavProperty(DavPropertyName.ISCOLLECTION, "1");
        } else {
            addDavProperty(DavPropertyName.RESOURCETYPE, new ResourceType(ResourceType.DEFAULT_RESOURCE));
            // Windows XP support
            addDavProperty(DavPropertyName.ISCOLLECTION, "0");
            addDavProperty(DavPropertyName.GETCONTENTLENGTH, getContentLength());
            addDavProperty(DavPropertyName.GETCONTENTTYPE, underLineResource.getMediaType());
        }

        addDavProperty(DavPropertyName.create("author"), underLineResource.getAuthorUserName());
        addDavProperty(DavPropertyName.GETETAG, getETag());
        addDavProperty(DavPropertyName.DISPLAYNAME, getDisplayName());
        addDavProperty(DavPropertyName.CREATIONDATE, DavConstants.creationDateFormat.format(
                                                                                   underLineResource.getCreatedTime()));
        addDavProperty(DavPropertyName.GETLASTMODIFIED, DavConstants.modificationDateFormat.format(
                                                                                  underLineResource.getLastModified()));

    }

    //TODO: fix ETag
    // the proper ETag implementation should be applied in the future.
    private String getETag() {
        return path;
    }
    // returns the size -the number of bytes - of the content
    private int getContentLength() {
        int bytesRead = 0;
        try {

            Object o = this.underLineResource.getContent();
            if (null != o && o instanceof byte[]) {
                bytesRead = ((byte[]) o).length;
            }
        } catch (RegistryException e) {
            bytesRead = 0;
        }

        return bytesRead;
    }

    // creates and adds a dav property
    private void addDavProperty(DavPropertyName dName, Object value) {
        DavProperty<Object> davProp = new DefaultDavProperty<Object>(dName, value);
        properties.put(davProp.getName(), davProp);
    }

	public void addMember(DavResource resource, InputContext inputContext)
			throws DavException {
//        if (isLocked(this) || isLocked(member)) {
//            throw new DavException(DavServletResponse.SC_LOCKED);
//        }
        try {
            if (resource instanceof RegistryResource) {
                if (getUnderlineResource() instanceof Collection) {
                    if (resource.getResourcePath().contains(path)) {

                        Resource resourceImpl = ((RegistryResource) resource).getUnderLineResource();
                        boolean isCollection = resourceImpl instanceof Collection;
                        // 'resourceImpl == null' indicates it's a new non-collection resource created by the client
                        // @see:  org.wso2.carbon.registry.webdav.RegistryServlet.doMkCol()
                        if (null == resourceImpl) {
                            resourceImpl = isCollection ? new CollectionImpl() : new ResourceImpl();
                            //setting path and underline resource only for newly created resources
                            ((ResourceImpl) resourceImpl).setPath(resource.getResourcePath());
                            ((RegistryResource) resource).setUnderLineResource(resourceImpl);
                            //if (!isCollection) {
                            resourceCache.updateDavResourceMimeType((RegistryResource) resource);
                            // setting the media type to text/plain as the default for newly created resources with
                            // no extensions.
                            if( null == resourceImpl.getMediaType() && resourceImpl.getPath().indexOf('.') == -1) {
                                resourceImpl.setMediaType("text/plain");
                            }
                            //}
                        }
                        if (!isCollection) {
                            resourceImpl.setContentStream(inputContext.getInputStream());
                        }
                        resourceCache.getRegistry().put(resource.getResourcePath(), resourceImpl);
					} else {
						throw new DavException(DavServletResponse.SC_BAD_REQUEST,
						"Internal Error, Parent and target path does not match");
					}
				} else {
					throw new DavException(DavServletResponse.SC_BAD_REQUEST,
							"Only add resources to Collections");
				}
			} else {
				throw new DavException(DavServletResponse.SC_BAD_REQUEST,
						"Only support " + RegistryResource.class + " as members");
			}
		} catch (RegistryException e) {
			e.printStackTrace();
			throw new DavException(DavServletResponse.SC_BAD_REQUEST,e);
		}

	}

	public boolean exists() {
		if(doesNotExists){
			return false;
		}else{
			try{
				getUnderlineResource();
				return true;
			} catch (RuntimeException e) {
				return false;
			}
		}
	}

	public boolean isCollection() {
        return exists() && getUnderlineResource() instanceof Collection;
	}

	public DavResource getCollection() {
		try {
			int index = path.lastIndexOf("/");
			if(index >= 0){
				String parentPath = path.substring(0,index+1);
				return resourceCache.getRegistryResource(parentPath);
			}else{
				throw new RuntimeException("Illegal Path "+ path);
			}
		} catch (DavException e) {
			throw new RuntimeException(e);
		}
	}

	public String getComplianceClass() {
		return COMPLIANCE_CLASSES;
	}

	public String getDisplayName() {
		String fullpath = path;
		if(fullpath.equals("/")){
			return "/";
		}
		String[] tokens = fullpath.split("/");
		return tokens[tokens.length-1]+"_";
	}

	// We do not do lock, at least not for now.
	public ActiveLock getLock(Type type, Scope scope) {
		if(lockable){
			ActiveLock lock = null;
	        if (exists() && Type.WRITE.equals(type) && Scope.EXCLUSIVE.equals(scope)) {
	        	lock = lockManager.getLock(type, scope, this);
	        }
	        return lock;
		}else{
			throw new UnsupportedOperationException();	
		}
	}

	public ActiveLock[] getLocks() {
		if(lockable){
			ActiveLock writeLock = getLock(Type.WRITE, Scope.EXCLUSIVE);
	        return (writeLock != null) ? new ActiveLock[]{writeLock} : new ActiveLock[0];
		}else{
			throw new UnsupportedOperationException();	
		}
	}

	public boolean hasLock(Type type, Scope scope) {
		if(lockable){
			return getLock(type, scope) != null;
		}else{
			throw new UnsupportedOperationException();	
		}
	}

	public boolean isLockable(Type type, Scope scope) {
		return lockable;
	}

	public void addLockManager(LockManager lockmgr) {
		this.lockManager = lockmgr;
	}

	public void unlock(String lockToken) throws DavException {
		if(lockable){
			ActiveLock lock = getLock(Type.WRITE, Scope.EXCLUSIVE);
	        if (lock == null) {
	            throw new DavException(DavServletResponse.SC_PRECONDITION_FAILED);
	        } else if (lock.isLockedByToken(lockToken)) {
                lockManager.releaseLock(lockToken, this);
	        } else {
	            throw new DavException(DavServletResponse.SC_LOCKED);
	        }
		}else{
			throw new UnsupportedOperationException();	
		}
	}

	public ActiveLock lock(LockInfo lockInfo) throws DavException {
		if(lockable){
			 if (isLockable(lockInfo.getType(), lockInfo.getScope())) {
	                return lockManager.createLock(lockInfo, this);
	        } else {
	            throw new DavException(DavServletResponse.SC_PRECONDITION_FAILED, "Unsupported lock type or scope.");
	        }
		}else{
			throw new UnsupportedOperationException();	
		}
       
	}

	public ActiveLock refreshLock(LockInfo lockInfo, String lockToken) throws DavException{
		if(lockable){
			if (!exists()) {
	            throw new DavException(DavServletResponse.SC_NOT_FOUND);
	        }
	        ActiveLock lock = getLock(lockInfo.getType(), lockInfo.getScope());
	        if (lock == null) {
	            throw new DavException(DavServletResponse.SC_PRECONDITION_FAILED, "No lock with the given type/scope present on resource " + getResourcePath());
	        }
	        lock = lockManager.refreshLock(lockInfo, lockToken, this);
	        /* since lock has infinite lock (simple) or undefined timeout (jcr)
	           return the lock as retrieved from getLock. */
	        return lock;
		}else{
			throw new UnsupportedOperationException();	
		}
	}

	public void removeMember(DavResource member) throws DavException {
		try {
			String path = member.getResourcePath();
            resourceCache.saveDavResourceMimeType((RegistryResource) member);
			resourceCache.removeRegistryResource(path);
			registry.delete(path);
		} catch (RegistryException e) {
			throw new DavException(DavServletResponse.SC_BAD_REQUEST);
		}
	}

	public void removeProperty(DavPropertyName propertyName)
			throws DavException {
		getUnderlineResource().removeProperty(propertyName.getName());

	}

	public void setProperty(DavProperty property) throws DavException {
		getUnderlineResource().setProperty(property.getName().getName(),
				(String) property.getValue());
	}

	public String getResourcePath() {
		return path;
	}

	public void move(DavResource destination) throws DavException {
		try {
			registry.move(path, destination
					.getResourcePath());
            resourceCache.saveDavResourceMimeType(this);
		} catch (RegistryException e) {
			throw new DavException(DavServletResponse.SC_BAD_REQUEST);
		}
	}

	public void copy(DavResource destination, boolean shallow)
			throws DavException {
		try {
			if(shallow){
				throw new DavException(DavServletResponse.SC_BAD_REQUEST,"Shallow copy/move not supported");
			}
			registry.copy(path, destination
					.getResourcePath());
		} catch (RegistryException e) {
			throw new DavException(DavServletResponse.SC_BAD_REQUEST,e);
		}		
	}
	
	

	public MultiStatusResponse alterProperties(List changeList)
			throws DavException {
	        if (!exists()) {
	            throw new DavException(DavServletResponse.SC_NOT_FOUND);
	        }
	        
	        MultiStatusResponse msr = new MultiStatusResponse(getHref(), null);
	        
	        Iterator it = changeList.iterator();
	        while(it.hasNext()){
	        	DavProperty property = (DavProperty)it.next();
	        	try{
	        		getUnderlineResource().setProperty(property.getName().getName(), (String)property.getValue());
	        		msr.add(property, DavServletResponse.SC_OK);
	        	}catch (Exception e) {
	        		e.printStackTrace();
	        		msr.add(property, DavServletResponse.SC_BAD_REQUEST);
				}
	        }
	        return msr;
	        
	}

	public MultiStatusResponse alterProperties(DavPropertySet setProperties,
			DavPropertyNameSet removePropertyNames) throws DavException {
		if (!exists()) {
            throw new DavException(DavServletResponse.SC_NOT_FOUND);
        }
        
        MultiStatusResponse msr = new MultiStatusResponse(getHref(), null);
        
        Iterator it = setProperties.iterator();
        while(it.hasNext()){
        	DavProperty property = (DavProperty)it.next();
        	try{
        		getUnderlineResource().setProperty(property.getName().getName(), (String)property.getValue());
        		msr.add(property, DavServletResponse.SC_OK);
        	}catch (Exception e) {
        		e.printStackTrace();
        		msr.add(property, DavServletResponse.SC_BAD_REQUEST);
			}
        }
        
        it = setProperties.iterator();
        while(it.hasNext()){
        	DavProperty property = (DavProperty)it.next();
        	try{
        		getUnderlineResource().setProperty(property.getName().getName(), (String)property.getValue());
        		msr.add(property, DavServletResponse.SC_OK);
        	}catch (Exception e) {
        		e.printStackTrace();
        		msr.add(property, DavServletResponse.SC_BAD_REQUEST);
			}
        }
        return msr;
	}

	public DavResourceFactory getFactory() {
		return resourceCache.getEnviorment().getResourceFactory();
	}

    // white spaces are replaced by '%20' to avoid href space issue with resource names.
	public String getHref() {
        return new StringBuffer(resourceCache.getContextPath()).append("/registry/resourcewebdav").append(path).toString().replace(" ","%20");
    }

	public DavResourceLocator getLocator() {
		return locator;
	}

	public DavResourceIterator getMembers() {
		try {
			String[] childrenNames = ((CollectionImpl)getUnderlineResource()).getChildren();
			
			List childrenList = new ArrayList();
			
			for(String name:childrenNames){
				RegistryResource registryResource = resourceCache.getRegistryResource(name);
                if(!"true".equals(registryResource.getUnderlineResource().getProperty("registry.link"))) {
				    childrenList.add(registryResource);
                }
			}
			
			return new DavResourceIteratorImpl(childrenList);
		} catch (RegistryException e) {
			throw new RuntimeException(e);
		} catch (DavException e) {
			throw new RuntimeException(e);
		}
	}

	public long getModificationTime() {
		return getUnderlineResource().getLastModified().getTime();
	}

	public DavPropertySet getProperties() {
		final Properties properties = getUnderlineResource().getProperties();
		DavPropertySet davproperties = new DavPropertySet();
		
		Iterator it = properties.keySet().iterator();
		while(it.hasNext()){
			final Object key = it.next();
			davproperties.add(new DavProperty() {
				public Element toXml(Document document) {
					return null;
				}
				public boolean isInvisibleInAllprop() {
					return false;
				}
				public Object getValue() {
					return properties.get(key);
				}
				public DavPropertyName getName() {
					return DavPropertyName.create((String)key);
				}
			});
		}
        for (DavProperty p : this.properties.values()) {
            davproperties.add(p);
        }
		return davproperties; 
	}

	public DavProperty getProperty(final DavPropertyName name) {
		DavProperty property = properties.get(name);
		if(property != null){
			return property;
		}else{
			return new DavProperty() {
				public Element toXml(Document document) {
					return null;
				}
				public boolean isInvisibleInAllprop() {
					return false;
				}
				public Object getValue() {
					return getUnderlineResource().getProperty(name.getName());
				}
				public DavPropertyName getName() {
					return name;
				}
			};
		}
	}

	public DavPropertyName[] getPropertyNames() {
		List<DavPropertyName> list = new ArrayList<DavPropertyName>();
		Iterator it  = getUnderlineResource().getProperties().keySet().iterator();
		while(it.hasNext()){
			list.add(DavPropertyName.create((String)it.next()));
		}
		
		Iterator it2 = properties.keySet().iterator(); 
		while(it2.hasNext()){
			list.add(((DavPropertyName)it2.next()));	
		}
		return list.toArray(new DavPropertyName[0]);
	}

	public DavSession getSession() {
		return resourceCache.getSession();
	}

	public String getSupportedMethods() {
		return "OPTIONS, GET, HEAD, POST, TRACE, PROPFIND, PROPPATCH, MKCOL, COPY,PUT, DELETE, MOVE, LOCK, UNLOCK, BIND, REBIND, UNBIND";
	}

    public void spool(OutputContext outputContext) throws IOException {
        try {
            if (exists()) {
                if (!isCollection()) {
                    final InputStream in = getUnderLineResource().getContentStream();
                    final OutputStream out = outputContext.getOutputStream();

                    if (null != out) {
                        byte[] buf = new byte[1024];
                        int read;
                        while ((read = in.read(buf)) >= 0) {
                            out.write(buf, 0, read);
                        }
                    }
                }
            } else {
                throw new IOException("Resource " + path + " does not exists");
            }
        } catch (RegistryException e) {
            throw new IOException("Error writing resource " + path + ":" + e.getMessage());
        }
    }

	public Resource getUnderLineResource() {
		return underLineResource;
	}


	public void setUnderLineResource(Resource underLineResource) {
		this.underLineResource = underLineResource;
	}
	
	/**
     * Return true if this resource cannot be modified due to a write lock
     * that is not owned by the given session.
     *
     * @return true if this resource cannot be modified due to a write lock
     */
    private boolean isLocked(DavResource res) {
        ActiveLock lock = res.getLock(Type.WRITE, Scope.EXCLUSIVE);
        if (lock == null) {
            return false;
        } else {
            String[] sLockTokens = session.getLockTokens();
            for (int i = 0; i < sLockTokens.length; i++) {
                if (sLockTokens[i].equals(lock.getToken())) {
                    return false;
                }
            }
            return true;
        }
    }

}
