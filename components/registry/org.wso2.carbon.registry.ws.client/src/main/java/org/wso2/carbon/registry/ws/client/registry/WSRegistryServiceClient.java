/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.com) All Rights Reserved.
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

package org.wso2.carbon.registry.ws.client.registry;

import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.rampart.RampartMessageData;
import org.apache.rampart.policy.model.CryptoConfig;
import org.apache.rampart.policy.model.RampartConfig;
import org.apache.ws.security.components.crypto.Merlin;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.core.common.AuthenticationException;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.pagination.PaginationUtils;
import org.wso2.carbon.registry.ws.client.resource.OnDemandContentCollectionImpl;
import org.wso2.carbon.registry.ws.client.resource.OnDemandContentResourceImpl;
import org.wso2.carbon.registry.ws.stub.WSRegistryServiceStub;
import org.wso2.carbon.registry.ws.stub.xsd.*;
import org.wso2.carbon.utils.CarbonUtils;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.*;

public class WSRegistryServiceClient implements Registry {
	private static final Log log = LogFactory.getLog(WSRegistryServiceClient.class);
	private WSRegistryServiceStub stub;
    AuthenticationAdminStub authenticationAdminStub;
	private String cookie;
	private String epr;

    public WSRegistryServiceClient(String backendServerURL, String cookie, long timeoutInMilliSeconds)
            throws RegistryException{
        initWithCookie(backendServerURL, cookie, timeoutInMilliSeconds);
    }

	public WSRegistryServiceClient(String backendServerURL, String cookie)
    	throws RegistryException{
        initWithCookie(backendServerURL, cookie, 1000000);
	}


    private void initWithCookie(String backendServerURL, String cookie, long timeOutInMilliSeconds) throws RegistryException {
        epr = backendServerURL + "WSRegistryService";
                	/*Since user provided the cookie no need to authenticate with username and password*/
        setCookie(cookie);
        try{
            if (CarbonUtils.isRunningOnLocalTransportMode()) {
                stub = new WSRegistryServiceStub(
                        WSRegistryClientUtils.getConfigurationContext(), epr);
            } else {
                stub = new WSRegistryServiceStub(epr);
            }
            ServiceClient client = stub._getServiceClient();
            Options options = client.getOptions();
            options.setManageSession(true);
            options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
            stub._getServiceClient().getOptions().setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
            //Increase the time out when sending large attachments
            stub._getServiceClient().getOptions().setTimeOutInMilliSeconds(timeOutInMilliSeconds);
        }catch (Exception axisFault){
            String msg = "Failed to initiate WSRegistry Service client. " + axisFault.getMessage();
            log.error(msg, axisFault);
            throw new RegistryException(msg, axisFault);
        }
    }

    public WSRegistryServiceClient(String serverURL, String username, String password, long timeOutInMilliSeconds,
                                       ConfigurationContext configContext) throws RegistryException {
        initWithUserNameAndPassword(serverURL, username, password, timeOutInMilliSeconds, configContext);
    }

    public WSRegistryServiceClient(String serverURL, String username, String password,
                                   ConfigurationContext configContext) throws RegistryException {
        initWithUserNameAndPassword(serverURL, username, password, 100000, configContext);
    }

    public void initWithUserNameAndPassword(String serverURL, String username, String password,
                                            long timeOutInMilliSeconds, ConfigurationContext configContext)
	throws RegistryException {

		epr = serverURL + "WSRegistryService";
		try {
			authenticate(configContext, serverURL, username, password);
			stub = new WSRegistryServiceStub(configContext, epr);

			ServiceClient client = stub._getServiceClient();
			Options options = client.getOptions();

			options.setManageSession(true);
			options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

			stub._getServiceClient().getOptions().setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
			//Increase the time out when sending large attachments
			stub._getServiceClient().getOptions().setTimeOutInMilliSeconds(timeOutInMilliSeconds);

		} catch (Exception axisFault) {
			String msg = "Failed to initiate WSRegistry Service client. " + axisFault.getMessage();
			log.error(msg, axisFault);
			throw new RegistryException(msg, axisFault);
		}
	}

    public WSRegistryServiceClient(String serverURL, String username, String password,
                                   String backendServerURL, long timeOutInMilliSeconds,
                                   ConfigurationContext configContext) throws RegistryException {
        initWithBackendURLUserNameAndPassword(serverURL, username, password, backendServerURL, timeOutInMilliSeconds, configContext);
    }

    public WSRegistryServiceClient(String serverURL, String username, String password,
                                   String backendServerURL, ConfigurationContext configContext) throws RegistryException {
        initWithBackendURLUserNameAndPassword(serverURL, username, password, backendServerURL, 1000000, configContext);
    }

    private void initWithBackendURLUserNameAndPassword(String serverURL, String username, String password,
                                                       String backendServerURL, long timeOutInMilliSeconds,
                                                       ConfigurationContext configContext) throws RegistryException {
        epr = backendServerURL + "WSRegistryService";
        String policyPath = "META-INF/policy.xml";
        try {
            authenticate(configContext, serverURL, username, password);
            stub = new WSRegistryServiceStub(configContext, epr);

            ServiceClient client = stub._getServiceClient();
            Options options = client.getOptions();

            options.setManageSession(true);
            options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

            stub._getServiceClient().getOptions().setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
            //Increase the time out when sending large attachments
            stub._getServiceClient().getOptions().setTimeOutInMilliSeconds(timeOutInMilliSeconds);

        } catch (Exception axisFault) {
            String msg = "Failed to initiate WSRegistry Service client. " + axisFault.getMessage();
            log.error(msg, axisFault);
            throw new RegistryException(msg, axisFault);
        }
    }

	public boolean authenticate(ConfigurationContext ctx, String serverURL, String username, String password)
            throws AxisFault, AuthenticationException {
		String serviceEPR = serverURL + "AuthenticationAdmin";

        authenticationAdminStub = new AuthenticationAdminStub(ctx, serviceEPR);
		ServiceClient client = authenticationAdminStub._getServiceClient();
		Options options = client.getOptions();
		options.setManageSession(true);
		try {
			boolean result = authenticationAdminStub.login(username, password, new URL(serviceEPR).getHost());
			if (result){
                setCookie((String) authenticationAdminStub._getServiceClient().getServiceContext().
                        getProperty(HTTPConstants.COOKIE_STRING));
			}
			return result;
		} catch (Exception e) {
			String msg = "Error occurred while logging in";
			throw new AuthenticationException(msg, e);
		}
	}

    public void logut () throws RemoteException, LogoutAuthenticationExceptionException {
        authenticationAdminStub.logout();
    }


	private static Policy loadPolicy(String xmlPath) throws FileNotFoundException, XMLStreamException {
		StAXOMBuilder builder = new StAXOMBuilder(xmlPath);
		return PolicyEngine.getPolicy(builder.getDocumentElement());
	}

	public void addSecurityOptions ( String policyPath, String keyStore,String userName, String password) throws AxisFault, FileNotFoundException, XMLStreamException {
		ServiceClient client = stub._getServiceClient();

		Policy policy = loadPolicy(policyPath);

		Properties merlinProp = new Properties();
		merlinProp.put("org.apache.ws.security.crypto.merlin.keystore.type", "JKS");
		merlinProp.put("org.apache.ws.security.crypto.merlin.file",
		               keyStore);

		merlinProp.put("org.apache.ws.security.crypto.merlin.keystore.password", "wso2carbon");

		CryptoConfig sigCryptoConfig = new CryptoConfig();
		sigCryptoConfig.setProvider(Merlin.class.getName());
		sigCryptoConfig.setProp(merlinProp);

		CryptoConfig encCryptoConfig = new CryptoConfig();
		encCryptoConfig.setProvider(Merlin.class.getName());
		encCryptoConfig.setProp(merlinProp);

		RampartConfig rampartConfig = new RampartConfig();
		rampartConfig.setEncryptionUser("wso2carbon");
		rampartConfig.setUserCertAlias("wso2carbon");
		rampartConfig.setPwCbClass(PWCBHandler.class.getName());
		rampartConfig.setSigCryptoConfig(sigCryptoConfig);
		rampartConfig.setEncrCryptoConfig(encCryptoConfig);

		policy.addAssertion(rampartConfig);

		Options options = client.getOptions();
		options.setProperty(RampartMessageData.KEY_RAMPART_POLICY, policy);
		options.setUserName(userName);
		options.setPassword(password);

		client.engageModule("rampart");
	}

	public Resource newResource() {
		return new ResourceImpl();
	}

	public Collection newCollection() {
		return new CollectionImpl(new String[0]);
	}

	public Resource get(String path) throws RegistryException {
		WSResource wsResource;
		try {

			if (path.contains(";comments")) {
				String[] pathParts = path.split(";comments");
				if (pathParts.length < 2) {
					Comment[] comments = getComments(pathParts[0]);
					Resource resource = new CollectionImpl();
					resource.setContent(comments);
					return resource;
				}
				else {
					return getSingleComment(path);
				}
			} else if (path.contains(";ratings")) {
				String[] pathParts = path.split(";ratings:");
				if (pathParts.length > 1) {
					int rating = getRating(pathParts[0], pathParts[1]);
					Resource resource = getMetaData(pathParts[0]);
					resource.setContent(rating);
					return resource;
				}
			}

			wsResource =  stub.wSget(path);
			byte[] content = null;
			Resource resource;
			if (wsResource.getCollection()) {
				resource = WSRegistryClientUtils.transformWSCollectiontoCollection(this, (WSCollection) wsResource, content);
                ((OnDemandContentCollectionImpl) resource).setPathWithVersion(path);
			}
			else {
				resource = WSRegistryClientUtils.transformWSResourcetoResource(this, wsResource, content);
				((OnDemandContentResourceImpl) resource).setPathWithVersion(path);
			}
			return resource;
		} catch (Exception e) {
			String msg = "Failed to perform get operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}

	private Comment getSingleComment(String commentPath) throws RegistryException {
		try {
			WSComment wsComment =  stub.wSgetSingleComment(commentPath);
			return WSRegistryClientUtils.WSCommenttoRegistryComment(wsComment);

		} catch (Exception e) {
			String msg = "Failed to perform the operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}

    public void setTenantId(int tenantId) throws RegistryException {
        try {
            stub.setTenantId(tenantId);
        } catch (Exception e) {
            String msg = "Failed to perform the operation.";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
	}

	public  Resource getMetaData(String path) throws RegistryException {
		Resource resource;
		try {
			WSResource wsResource = stub.wSgetMetaData(path);
			byte[] content = null;
			resource = WSRegistryClientUtils.transformWSResourcetoResource(this, wsResource, content);
			return resource;
		} catch (Exception e) {
			String msg = "Failed to perform getMetaData operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}

	}

	public Collection get(String path, int start, int pageSize) throws RegistryException {
		Collection collection;
		try {
			WSCollection wsCollection = stub.wSgetWithPageSize(path,start,pageSize);
			byte[] content = null;
			collection = WSRegistryClientUtils.transformWSCollectiontoCollection(this, wsCollection, content);
            ((OnDemandContentCollectionImpl) collection).setPathWithVersion(path + ";start=" + start + ";pageSize=" + pageSize);
			return collection;
		} catch (Exception e) {
			String msg = "Failed to perform get operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}

	public  boolean resourceExists(String path) throws RegistryException {
		try {
			return stub.resourceExists(path);
		} catch (Exception e) {
			String msg = "Failed to perform resourceExists operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}
	
	public String put(String path, Resource resource) throws RegistryException {
		try {
            File tempFile = File.createTempFile("wsResource","tmp");
            tempFile.deleteOnExit();
            DataHandler dataHandler = WSRegistryClientUtils.makeDataHandler(resource, tempFile);
            String string;
            if (resource instanceof Collection) {
				WSResource wsCollection = WSRegistryClientUtils.transformResourceToWSResource(resource, dataHandler);
                wsCollection.setCollection(true);
				string =  stub.wSput(path,wsCollection);
			}
			else {
				WSResource wsResource = WSRegistryClientUtils.transformResourceToWSResource(resource, dataHandler);
				string =  stub.wSput(path,wsResource);
			}

			return string;
		} catch (Exception e) {
			String msg = "Failed to perform put operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}

    public void delete(String path) throws RegistryException {
		try {
			stub.delete(path);
		} catch (Exception e) {
			String msg = "Failed to perform delete operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}

    public String importResource(String suggestedPath, String sourceURL, Resource resource) throws RegistryException {
		try {

			DataHandler dataHandler = null;
			File tempFile = File.createTempFile("wsResource","tmp");
			tempFile.deleteOnExit();
			try {
				dataHandler = WSRegistryClientUtils.makeDataHandler(resource, tempFile);
			} catch (IOException e) {
				log.error("WSGet failed - Unable to generate temp file",e);
			}
			WSResource wsResource = WSRegistryClientUtils.transformResourceToWSResource(resource, dataHandler);
			return stub.wSimportResource(suggestedPath,sourceURL,wsResource);
		} catch (Exception e) {
			String msg = "Failed to perform importResource operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}
	
	public  String rename(String currentPath, String newName) throws RegistryException {
		try {
			return stub.rename(currentPath,newName);
		} catch (Exception e) {
			String msg = "Failed to perform rename operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}
	public  String move(String currentPath, String newPath) throws RegistryException {
		try {
			return stub.move(currentPath,newPath);
		} catch (Exception e) {
			String msg = "Failed to perform move operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}
	
	public  String copy(String currentPath, String targetPath) throws RegistryException {
		try {
			return stub.copy(currentPath,targetPath);
		} catch (Exception e) {
			String msg = "Failed to perform copy operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}
	
	public  void createVersion(String path) throws RegistryException {
		try {
			stub.createVersion(path);
		} catch (Exception e) {
			String msg = "Failed to perform createVersion operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}
	
	public  String[] getVersions(String path) throws RegistryException {
		try {
			return stub.getVersions(path);
		} catch (Exception e) {
			String msg = "Failed to perform getVersions operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}
	
	public  void restoreVersion(String path) throws RegistryException {
		try {
			stub.restoreVersion(path);
		} catch (Exception e) {
			String msg = "Failed to perform restoreVersion operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}
	
	public  void addAssociation(String resourcePath, String targetPath, String associationType) throws RegistryException {
		try {
			stub.addAssociation(resourcePath,targetPath,associationType);
		} catch (Exception e) {
			String msg = "Failed to perform addAssociation operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}
	
	public  void removeAssociation(String resourcePath, String targetPath, String associationType) throws RegistryException {
		try {
			stub.removeAssociation(resourcePath,targetPath,associationType);
		} catch (Exception e) {
			String msg = "Failed to perform removeAssociation operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}
	
	public  Association[] getAllAssociations(String path) throws RegistryException {
		try {
            WSAssociation[] wsAssociations = stub.wSgetAllAssociations(path);
            if(null == wsAssociations) {
                return  new Association[0];
            }
            Association[] associations = new Association[wsAssociations.length];
            for (int i = 0; i < associations.length; i++) {
                associations[i] = WSRegistryClientUtils.transformWSAssociationToAssociation(
                                                            wsAssociations[i]);
            }
            return associations;
		} catch (Exception e) {
			String msg = "Failed to perform getAllAssociations operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}

	public Association[] getAssociations(String resourcePath, String associationType) throws RegistryException {
		try {
            WSAssociation[] wsAssociations = stub.wSgetAssociations(resourcePath, associationType);
            if( null == wsAssociations) {
               return new Association[0];
            }
            Association[] associations = new Association[wsAssociations.length];
            for (int i = 0; i < associations.length; i++) {
                associations[i] = WSRegistryClientUtils.transformWSAssociationToAssociation(
                                                            wsAssociations[i]);
            }
            return associations;
		} catch (Exception e) {
			String msg = "Failed to perform getAssociations operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}
	public  void applyTag(String resourcePath, String tag) throws RegistryException {
		try {
			stub.applyTag(resourcePath,tag);
		} catch (Exception e) {
			String msg = "Failed to perform applyTag operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}
	public  TaggedResourcePath[] getResourcePathsWithTag(String path) throws RegistryException {
		try {
			WSTaggedResourcePath[] wsTaggedResourcePaths = stub.wSgetResourcePathsWithTag(path);
			return WSRegistryClientUtils.exchangeWSResourcePath(wsTaggedResourcePaths);
		} catch (Exception e) {
			String msg = "Failed to perform getResourcePathsWithTag operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}
	public Tag[] getTags(String resourcePath) throws RegistryException {
		try {
            WSTag[] wsTags;
            if(PaginationContext.getInstance() == null){
                wsTags = stub.wSgetTags(resourcePath);
            }else {
                PaginationUtils.copyPaginationContext(stub._getServiceClient());
                wsTags = stub.wSgetTags(resourcePath);
                int rowCount = PaginationUtils.getRowCount(stub._getServiceClient());
            }
            if(null == wsTags) {
                return new Tag[0];
            }
            Tag[] tags = new Tag[wsTags.length];
            for (int i = 0; i < tags.length; i++) {
                tags[i] = WSRegistryClientUtils.transformWSTagToTag(wsTags[i]);
            }
            return tags;
		} catch (Exception e) {
			String msg = "Failed to perform getTags operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}finally {
            PaginationContext.destroy();
        }
    }
	public  void removeTag(String path,String tag) throws RegistryException {
		try {
			stub.removeTag(path,tag);
		} catch (Exception e) {
			String msg = "Failed to perform removeTag operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}
	public  String addComment(String resourcePath, Comment comment) throws RegistryException {
		try {
			WSComment wsComment = WSRegistryClientUtils.RegistryCommenttoWSComment(comment);
			return stub.wSaddComment(resourcePath,wsComment);
		} catch (Exception e) {
			String msg = "Failed to perform addComment operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}
	public  void editComment(String commentPath, String text) throws RegistryException {
		try {
			stub.editComment(commentPath,text);
		} catch (Exception e) {
			String msg = "Failed to perform editComment operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}
	public  Comment[] getComments(String resourcePath) throws RegistryException {
		try {
            WSComment[] wsComment;
            if(PaginationContext.getInstance() == null){
                wsComment =  stub.wSgetComments(resourcePath);
            }else {
                PaginationUtils.copyPaginationContext(stub._getServiceClient());
                wsComment =  stub.wSgetComments(resourcePath);
                int rowCount = PaginationUtils.getRowCount(stub._getServiceClient());
            }
            if(null == wsComment) {
                return  new Comment[0];
            }
			Comment[] comment = new Comment[wsComment.length];
			for (int i = 0;i < wsComment.length; i++) {
				comment[i] = WSRegistryClientUtils.WSCommenttoRegistryComment(wsComment[i]); 
			}
			return comment;

		} catch (Exception e) {
			String msg = "Failed to perform getComments operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}
	public void rateResource(String resourcePath, int rating) throws RegistryException {
		try {
			stub.rateResource(resourcePath,rating);
		} catch (Exception e) {
			String msg = "Failed to perform rateResource operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}
	public  float getAverageRating(String resourcePath) throws RegistryException {
		try {
			return stub.getAverageRating(resourcePath);
		} catch (Exception e) {
			String msg = "Failed to perform getAverageRating operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}

	public int getRating(String path, String username) throws RegistryException {
		try {
			return stub.getRating(path,username);
		} catch (Exception e) {
			String msg = "Failed to perform getRating operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}
	public Collection executeQuery(String path, Map parameters) throws RegistryException {
		try {
			List<String> value = new LinkedList<String>();
			List<String> key = new LinkedList<String>();
			for (Object e : parameters.entrySet()) {
				Map.Entry entry = (Map.Entry)e;
				value.add((String)entry.getValue());
				key.add((String)entry.getKey());
			}
			WSCollection wsCollection =  stub.wSexecuteQuery(path,
			                                                 key.toArray(new String[key.size()]),value.toArray(new String[value.size()]));
			byte[] content = null;
			return WSRegistryClientUtils.transformWSCollectiontoCollection(this, wsCollection, content);

		} catch (Exception e) {
			String msg = "Failed to perform executeQuery operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}
	public LogEntry[] getLogs(String resourcePath,
	                          int action,
	                          String userName,
	                          Date from,
	                          Date to,
	                          boolean recentFirst) throws RegistryException {
		try {
            WSLogEntry[] wsLogEntries;

            if(PaginationContext.getInstance() == null){
                 wsLogEntries = stub.wSgetLogs(resourcePath, action, userName, from, to,
                        recentFirst);

            }else {
                PaginationUtils.copyPaginationContext(stub._getServiceClient());
                wsLogEntries = stub.wSgetLogs(resourcePath, action, userName, from, to,
                        recentFirst);
                int rowCount = PaginationUtils.getRowCount(stub._getServiceClient());
            }
            if(null == wsLogEntries) {
                return  new LogEntry[0];
            }
            LogEntry[] logEntries = new LogEntry[wsLogEntries.length];
            for (int i = 0; i < logEntries.length; i++) {
                logEntries[i] = WSRegistryClientUtils.transformWSLogEntryToLogEntry(wsLogEntries[i]);
            }
            return logEntries;
		} catch (Exception e) {
			String msg = "Failed to perform getLogs operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}

	public  String[] getAvailableAspects() {
		try {
			return stub.getAvailableAspects();
		} catch (Exception e) {
			String msg = "Failed to perform getAvailableAspects operation.";
			log.error(msg, e);
            return new String[0];
		}
	}

	public void associateAspect(String resourcePath, String aspect) throws RegistryException {
		try {
			stub.associateAspect(resourcePath,aspect);
		} catch (Exception e) {
			String msg = "Failed to perform associateAspect operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}
	public  void invokeAspect(String resourcePath, String aspectName, String action) throws RegistryException {
		try {
			stub.invokeAspectNoParam(resourcePath,aspectName,action);
		} catch (Exception e) {
			String msg = "Failed to perform invokeAspect operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}
	public  void invokeAspect(String resourcePath, String aspectName, String action,
                              Map<String, String> parameters) throws RegistryException {
		try {
			Set<String> keys = parameters.keySet();
            java.util.Collection<String> values = parameters.values();
            stub.invokeAspectWithParam(resourcePath,aspectName,action,
                    keys.toArray(new String[keys.size()]),
                    values.toArray(new String[values.size()]));
		} catch (Exception e) {
			String msg = "Failed to perform invokeAspect operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}
	public String[] getAspectActions(String resourcePath, String aspectName) throws RegistryException {
		try {
			return stub.getAspectActions(resourcePath,aspectName);
		} catch (Exception e) {
			String msg = "Failed to perform getAspectActions operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}

	public Collection searchContent(String keyword) throws RegistryException {
		try {
			WSCollection wsCollection =  stub.wSsearchContent(keyword);
			byte[] content = null;
			return WSRegistryClientUtils.transformWSCollectiontoCollection(this, wsCollection, content);
		} catch (Exception e) {
			String msg = "Failed to perform searchContent operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}
	
	public void createLink(String path, String target) throws RegistryException {
		try {
			stub.createLinkWithSubTarget(path, target, null);
		} catch (Exception e) {
			String msg = "Failed to perform createLink operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}

	public void createLink(String path, String target, String subTargetPath) throws RegistryException {
		try {
			stub.createLinkWithSubTarget(path, target, subTargetPath);
		} catch (Exception e) {
			String msg = "Failed to perform createLink operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}

	public void removeLink(String path) throws RegistryException {
		try {
			stub.removeLink(path);
		} catch (Exception e) {
			String msg = "Failed to perform removeLink operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}
	public String getEventingServiceURL(String path) throws RegistryException {
		try {
			return stub.getEventingServiceURL(path);
		} catch (Exception e) {
			String msg = "Failed to perform getEventingServiceURL operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}
	public void setEventingServiceURL(String path, String serviceURL) throws RegistryException {
		try {
			stub.setEventingServiceURL(path,serviceURL);
		} catch (Exception e) {
			String msg = "Failed to perform setEventingServiceURL operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}

	public boolean removeAspect(String name) throws RegistryException {
		try {
			return stub.removeAspect(name);
		} catch (Exception e) {
			String msg = "Failed to perform removeAspect operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}

    public void removeComment(String comment) throws RegistryException {
        try {
            stub.removeComment(comment);
        } catch (Exception e) {
            String msg = "Failed to perform removeComment operation.";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }


    /**
     *
     * @param path                Path of the resource
     * @param regVersion          Version ID
     * @return                    Succeed or not
     * @throws RegistryException  If operation fails
     */
    public boolean removeVersionHistory(String path, long regVersion)
    		throws RegistryException {
        try {
            return stub.removeVersionHistory(path, regVersion);
        } catch (Exception e) {
            String msg = "Failed to remove version: " + regVersion + " of the resource " + path;
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }

    public void dumpLite(String s, Writer writer) throws RegistryException {
        // Implementation needs to be added
    }

    public void beginTransaction() throws RegistryException {
    }

    public void commitTransaction() throws RegistryException {
    }

    public void rollbackTransaction() throws RegistryException {
    }

    public String importResource(String suggestedPath, String sourceURL,
                                 org.wso2.carbon.registry.api.Resource resource)
            throws org.wso2.carbon.registry.api.RegistryException {
        return importResource(suggestedPath, sourceURL, (Resource) resource);
    }

    public String addComment(String resourcePath, org.wso2.carbon.registry.api.Comment comment)
            throws org.wso2.carbon.registry.api.RegistryException {
        return addComment(resourcePath, (org.wso2.carbon.registry.core.Comment) comment);
    }

    public String put(String suggestedPath, org.wso2.carbon.registry.api.Resource resource)
            throws org.wso2.carbon.registry.api.RegistryException {
        return put(suggestedPath, (Resource) resource);
    }

    public boolean addAspect(String s, Aspect aspect) throws RegistryException {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    public void dump(String s, Writer writer) throws RegistryException {
        try {
            DataHandler dataHandler = stub.wsDump(s);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(WSRegistryClientUtils.makeBytesFromDataHandler(dataHandler));
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                writer.append(inputLine);
            }
            writer.flush();
            reader.close();

        } catch (Exception e) {
            String msg = "Failed to perform dump operation.";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }

    public void restore(String path, Reader reader) throws RegistryException {
        try {

            BufferedReader bufferedReader = new BufferedReader(reader);

            File tempFile = new File("tempFile");
            tempFile.deleteOnExit();

            FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
            Writer writer = new OutputStreamWriter(fileOutputStream);

            String inputLine;
            while ((inputLine = bufferedReader.readLine()) != null) {
                writer.append(inputLine);
            }

            writer.flush();
            fileOutputStream.close();
            writer.close();
            bufferedReader.close();

            DataHandler handler = new DataHandler(new FileDataSource(tempFile));

            stub.wsRestore(path, handler);

            @SuppressWarnings("unused")
            boolean ignored = tempFile.delete();
        } catch (Exception e) {
            String msg = "Failed to perform restore operation.";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }

    public LogEntryCollection getLogCollection(String s, int i, String s1, Date date, Date date1,
                                               boolean b) throws RegistryException {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    public RegistryContext getRegistryContext() {
        return RegistryContext.getBaseInstance();
    }

    // Used to fetch resource content, on-demand
	public byte[] getContent(String path) throws Exception {
		DataHandler dataHandler = stub.getContent(path);
		return WSRegistryClientUtils.makeBytesFromDataHandler(dataHandler);
	}
	
	public String[] getCollectionContent(String path) throws Exception {
		return stub.getCollectionContent(path);
	}

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public void setStub(WSRegistryServiceStub stub) {
        this.stub = stub;
    }

    public WSRegistryServiceStub getStub() {
        return stub;
    }

    public void setEpr(String epr) {
        this.epr = epr;
    }
}
