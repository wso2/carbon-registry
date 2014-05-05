/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.info.ui.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.pagination.PaginationUtils;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;
import org.wso2.carbon.registry.info.stub.InfoAdminServiceStub;
import org.wso2.carbon.registry.info.ui.Utils;
import org.wso2.carbon.registry.common.IInfoService;
import org.wso2.carbon.registry.common.beans.*;
import org.wso2.carbon.registry.common.beans.utils.*;
import org.wso2.carbon.ui.CarbonUIUtil;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class InfoServiceClient implements IInfoService {

    private static final Log log = LogFactory.getLog(InfoServiceClient.class);

    private InfoAdminServiceStub stub;
    private IInfoService proxy;
    private HttpSession session;

    public InfoServiceClient(String cookie, ServletConfig config, HttpSession session)
            throws RegistryException {
        this.session =session;
        if (proxy == null) {
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(),
                    session);
            ConfigurationContext configContext = (ConfigurationContext) config.
                    getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            String epr = backendServerURL + "InfoAdminService";

            try {
                stub = new InfoAdminServiceStub(configContext, epr);

                ServiceClient client = stub._getServiceClient();
                Options option = client.getOptions();
                option.setManageSession(true);
                option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                        cookie);

            } catch (AxisFault axisFault) {
                String msg = "Failed to initiate comment service client. " + axisFault.getMessage();
                log.error(msg, axisFault);
                throw new RegistryException(msg, axisFault);
            }
            proxy = this;
        }
    }

    public void setSession(String sessionId, HttpSession session) {/* Not required at client-side.*/}

    public void removeSession(String sessionId) {/* Not required at client-side.*/}

    public CommentBean getComments(HttpServletRequest request) throws Exception {

        String sessionId = UUIDGenerator.generateUUID();

        String path = (String) Utils.getParameter(request, "path");

        try {
            return proxy.getComments(path, sessionId);
        } catch (Exception e) {
            String msg = "Failed to get comments from the comment service.";
            log.error(msg, e);
            throw new Exception(msg);
        }

    }

    public void addComment(HttpServletRequest request) throws Exception {
        String sessionId = UUIDGenerator.generateUUID();

        String path = (String) Utils.getParameter(request, "path");
        String comment = (String) Utils.getParameter(request, "comment");

        try {
            proxy.addComment(comment, path, sessionId);
        } catch (Exception e) {
            String msg = "Failed to get comments from the comment service.";
            log.error(msg, e);
            throw new Exception(msg);
        }
    }
    
    public void removeComment(HttpServletRequest request) throws Exception {
    	String sessionId = UUIDGenerator.generateUUID();

    	String mountedPath = getMountedCommentPath(request);
    	
//    	String comment = (String) Utils.getParameter(request, "comment");

    	try {
    		proxy.removeComment(mountedPath, sessionId);
    	} catch (Exception e) {
    		String msg = "Failed to remove the comment.";
    		log.error(msg, e);
    		throw new Exception(msg);
    	}
    }

	private String getMountedCommentPath(HttpServletRequest request) {
		String commentPath = (String) Utils.getParameter(request, "commentpath");
    	String path = (String) Utils.getParameter(request, "path");
    	
    	// To fix bug when registry is mounted
    	String[] commentParts = commentPath.split(";");
    	return path +";" + commentParts[1];
	}

    public TagBean getTags(HttpServletRequest request) throws Exception {
        String sessionId = UUIDGenerator.generateUUID();

        String path = (String) Utils.getParameter(request, "path");

        try {
            return proxy.getTags(path, sessionId);
        } catch (Exception e) {
            String msg = "Failed to get comments from the comment service.";
            log.error(msg, e);
            throw new Exception(msg);
        }
    }

    public void addTag(HttpServletRequest request) throws Exception {
        String sessionId = UUIDGenerator.generateUUID();

        String path = (String) Utils.getParameter(request, "path");
        String tag = (String) Utils.getParameter(request, "tag");

        try {
            proxy.addTag(tag, path, sessionId);
        } catch (Exception e) {
            String msg = "Failed to add the tag.";
            log.error(msg, e);
            throw new Exception(msg);
        }
    }
    
    public void removeTag(HttpServletRequest request) throws Exception {
        String sessionId = UUIDGenerator.generateUUID();

        String path = (String) Utils.getParameter(request, "path");
        String tag = (String) Utils.getParameter(request, "tag");

        try {
            proxy.removeTag(tag, path, sessionId);
        } catch (Exception e) {
            String msg = "Failed to remove the tag.";
            log.error(msg, e);
            throw new Exception(msg);
        }
    }

    public RatingBean getRatings(HttpServletRequest request) throws Exception {
        String sessionId = UUIDGenerator.generateUUID();

        String path = (String) Utils.getParameter(request, "path");

        try {
            return proxy.getRatings(path, sessionId);
        } catch (Exception e) {
            String msg = "Failed to get ratings.";
            log.error(msg, e);
            throw new Exception(msg);
        }
    }

    public void rateResource(HttpServletRequest request) throws Exception {
        String sessionId = UUIDGenerator.generateUUID();

        String path = (String) Utils.getParameter(request, "path");
        String rating = (String) Utils.getParameter(request, "rating");

        try {
            proxy.rateResource(rating, path, sessionId);
        } catch (Exception e) {
            String msg = "Failed to rate the resource.";
            log.error(msg, e);
            throw new Exception(msg);
        }
    }

    public EventTypeBean getEventTypes(HttpServletRequest request) throws Exception {
        String sessionId = UUIDGenerator.generateUUID();

        String path = (String) Utils.getParameter(request, "path");

        try {
            return proxy.getEventTypes(path, sessionId);
        } catch (Exception e) {
            String msg = "Failed to get Event Types.";
            log.error(msg, e);
            throw new Exception(msg);
        }
    }

    public SubscriptionBean getSubscriptions(HttpServletRequest request) throws Exception {
        String sessionId = UUIDGenerator.generateUUID();
        SubscriptionBean subscriptionBean;
        String path = (String) Utils.getParameter(request, "path");

        try {
            if (PaginationContext.getInstance() == null) {
                subscriptionBean = proxy.getSubscriptions(path, sessionId);
            } else {
                PaginationUtils.copyPaginationContext(stub._getServiceClient());
                subscriptionBean = proxy.getSubscriptions(path, sessionId);
                int rowCount = PaginationUtils.getRowCount(stub._getServiceClient());
                session.setAttribute("row_count", Integer.toString(rowCount));
            }
        } catch (Exception e) {
            String msg = "Failed to get Subscriptions.";
            log.error(msg, e);
            throw new Exception(msg);
        } finally {
            PaginationContext.destroy();
        }
        return subscriptionBean;
    }

    public SubscriptionBean subscribe(HttpServletRequest request) throws Exception {
        String sessionId = UUIDGenerator.generateUUID();

        String path = (String) Utils.getParameter(request, "path");
        String endpoint = (String) Utils.getParameter(request, "endpoint");
        String eventName = (String) Utils.getParameter(request, "eventName");
        String topicDelimiter= (String) Utils.getParameter(request, "delimiter");


        if (topicDelimiter.equals("#") || topicDelimiter.equals("*")) {
            if (path.endsWith("/")) {
                path = path + topicDelimiter;
            } else {
                path = path + "/" + topicDelimiter;
            }
        }

        try {
            return proxy.subscribe(path, endpoint, eventName, sessionId);
        } catch (Exception e) {
            String msg = "Failed to subscribe.";
            log.error(msg, e);
            throw new Exception(msg);
        }
    }

    public SubscriptionBean subscribeREST(HttpServletRequest request) throws Exception {
        String sessionId = UUIDGenerator.generateUUID();

        String path = (String) Utils.getParameter(request, "path");
        String endpoint = (String) Utils.getParameter(request, "endpoint");
        String eventName = (String) Utils.getParameter(request, "eventName");

        String topicDelimiter= (String) Utils.getParameter(request, "delimiter");

       if (topicDelimiter.equals("#") || topicDelimiter.equals("*")) {
            if (path.endsWith("/")) {
                   path = path + topicDelimiter;
            } else {
                  path = path + "/" + topicDelimiter;
            }
       }

        try {
            return proxy.subscribeREST(path, endpoint, eventName, sessionId);
        } catch (Exception e) {
            String msg = "Failed to subscribe.";
            log.error(msg, e);
            throw new Exception(msg);
        }
    }

    public boolean unsubscribe(HttpServletRequest request) throws Exception {
        String sessionId = UUIDGenerator.generateUUID();

        String path = (String) Utils.getParameter(request, "path");
        String id = (String) Utils.getParameter(request, "id");

        try {
            return proxy.unsubscribe(path, id, sessionId);
        } catch (Exception e) {
            String msg = "Failed to unsubscribe.";
            log.error(msg, e);
            throw new Exception(msg);
        }
    }

    public boolean isResource(HttpServletRequest request) throws Exception {
        String sessionId = UUIDGenerator.generateUUID();

        String path = (String) Utils.getParameter(request, "path");

        try {
            return proxy.isResource(path, sessionId);
        } catch (Exception e) {
            String msg = "Failed to get resource type.";
            log.error(msg, e);
            throw new Exception(msg);
        }
    }


    public String getRemoteURL(HttpServletRequest request) throws Exception {
        String sessionId = UUIDGenerator.generateUUID();

        String path = (String) Utils.getParameter(request, "path");

        try {
            return proxy.getRemoteURL(path, sessionId);
        } catch (Exception e) {
            String msg = "Failed to get remote URL.";
            log.error(msg, e);
            throw new Exception(msg);
        }
    }

    public String verifyEmail(HttpServletRequest request) throws Exception {
        String sessionId = UUIDGenerator.generateUUID();

        String data = (String)request.getSession().getAttribute("intermediate-data");

        try {
            return proxy.verifyEmail(data, sessionId);
        } catch (Exception e) {
            String msg = "Failed to verify e-mail address.";
            log.error(msg, e);
            throw new Exception(msg);
        }
    }

    public boolean isUserValid(HttpServletRequest request) throws Exception {
        String sessionId = UUIDGenerator.generateUUID();

        String username = (String) Utils.getParameter(request, "username");

        try {
            return proxy.isUserValid(username, sessionId);
        } catch (Exception e) {
            String msg = "Failed to check existence of user.";
            log.error(msg, e);
            throw new Exception(msg);
        }
    }

    public boolean isProfileExisting(HttpServletRequest request) throws Exception {
        String sessionId = UUIDGenerator.generateUUID();

        String username = (String) Utils.getParameter(request, "username");

        try {
            return proxy.isProfileExisting(username, sessionId);
        } catch (Exception e) {
            String msg = "Failed to check existence of user's profile.";
            log.error(msg, e);
            throw new Exception(msg);
        }
    }

    public boolean isRoleValid(HttpServletRequest request) throws Exception {
        String sessionId = UUIDGenerator.generateUUID();

        String role = (String) Utils.getParameter(request, "role");

        try {
            return proxy.isRoleValid(role, sessionId);
        } catch (Exception e) {
            String msg = "Failed to check existence of role.";
            log.error(msg, e);
            throw new Exception(msg);
        }
    }

    public boolean isRoleProfileExisting(HttpServletRequest request) throws Exception {
        String sessionId = UUIDGenerator.generateUUID();

        String role = (String) Utils.getParameter(request, "role");

        try {
            return proxy.isRoleProfileExisting(role, sessionId);
        } catch (Exception e) {
            String msg = "Failed to check existence of the profile for the role.";
            log.error(msg, e);
            throw new Exception(msg);
        }
    }

    public CommentBean getComments(String path, String sessionId) throws RegistryException {
        CommentBean result = new CommentBean();

        try {
            org.wso2.carbon.registry.info.stub.beans.xsd.CommentBean bean =
                    stub.getComments(path, sessionId);
            if (bean.getComments() == null) {
                bean.setComments(
                        new org.wso2.carbon.registry.info.stub.beans.utils.xsd.Comment[0]);
            }
            org.wso2.carbon.registry.info.stub.beans.utils.xsd.Comment[] comments =
                bean.getComments();
            Comment[] resultComments = new Comment[comments.length];
            int i = 0;
            for (org.wso2.carbon.registry.info.stub.beans.utils.xsd.Comment comment : comments) {
                if (comment == null) {
                    continue;
                }
                resultComments[i] = new Comment();
                resultComments[i].setAuthorUserName(comment.getAuthorUserName());
                resultComments[i].setCommentPath(comment.getCommentPath());
                resultComments[i].setContent(comment.getContent());
                resultComments[i].setCreatedTime(comment.getCreatedTime());
                resultComments[i].setDescription(comment.getDescription());
                resultComments[i].setLastModified(comment.getLastModified());
                resultComments[i].setResourcePath(comment.getResourcePath());
                resultComments[i].setText(comment.getText());
                resultComments[i].setTime(comment.getTime());
                resultComments[i].setUser(comment.getUser());
                i++;
            }
            result.setComments(resultComments);
            result.setErrorMessage(bean.getErrorMessage());
            result.setLoggedIn(bean.getLoggedIn());
            result.setPathWithVersion(bean.getPathWithVersion());
            result.setPutAllowed(bean.getPutAllowed());
            result.setVersionView(bean.getVersionView());
        } catch (Exception e) {
            String msg = e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }

        return result;

    }

    public void addComment(String comment, String path, String sessionId) throws RegistryException {
        try {
            stub.addComment(comment, path, sessionId);
        } catch (Exception e) {
            String msg = e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }
    
    public void removeComment(String commentPath, String sessionId)	throws RegistryException {
    	try {
    		stub.removeComment(commentPath, sessionId);
    	} catch (Exception e) {
    		String msg = e.getMessage();
    		log.error(msg, e);
    		throw new RegistryException(msg, e);
    	}
    }

    public TagBean getTags(String path, String sessionId) throws RegistryException {
        TagBean result = new TagBean();
        try {
            org.wso2.carbon.registry.info.stub.beans.xsd.TagBean bean = stub.getTags(path, sessionId);
            if (bean.getTags() == null) {
                bean.setTags(new org.wso2.carbon.registry.info.stub.beans.utils.xsd.Tag[0]);
            }
            org.wso2.carbon.registry.info.stub.beans.utils.xsd.Tag[] tags =
                bean.getTags();
            Tag[] resultTags = new Tag[tags.length];
            int i = 0;
            for (org.wso2.carbon.registry.info.stub.beans.utils.xsd.Tag tag : tags) {
                if (tag == null) {
                    continue;
                }
                resultTags[i] = new Tag();
                resultTags[i].setCategory(tag.getCategory());
                resultTags[i].setTagCount(tag.getTagCount());
                resultTags[i].setTagName(tag.getTagName());
                i++;
            }
            result.setTags(resultTags);
            result.setErrorMessage(bean.getErrorMessage());
            result.setLoggedIn(bean.getLoggedIn());
            result.setPathWithVersion(bean.getPathWithVersion());
            result.setPutAllowed(bean.getPutAllowed());
            result.setVersionView(bean.getVersionView());
        } catch (Exception e) {
            String msg = e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
        return result;
    }

    public void addTag(String tag, String path, String sessionId) throws RegistryException {
        try {
            stub.addTag(tag, path, sessionId);
        } catch (Exception e) {
            String msg = e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }
    
    public void removeTag(String tag, String path, String sessionId)
    throws RegistryException {
        try {
            stub.removeTag(tag, path, sessionId);
        } catch (Exception e) {
            String msg = e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }

    }

    public RatingBean getRatings(String path, String sessionId) throws RegistryException {
        RatingBean result = new RatingBean();
        try {
            org.wso2.carbon.registry.info.stub.beans.xsd.RatingBean bean =
                    stub.getRatings(path, sessionId);
            result.setAverageRating(bean.getAverageRating());
            result.setAverageStars(bean.getAverageStars());
            result.setUserRating(bean.getUserRating());
            result.setUserStars(bean.getUserStars());
            result.setErrorMessage(bean.getErrorMessage());
            result.setLoggedIn(bean.getLoggedIn());
            result.setPathWithVersion(bean.getPathWithVersion());
            result.setPutAllowed(bean.getPutAllowed());
            result.setVersionView(bean.getVersionView());
        } catch (Exception e) {
            String msg = e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
        return result;
    }

    public void rateResource(String rating, String path, String sessionId) throws RegistryException {
        try {
            stub.rateResource(rating, path, sessionId);
        } catch (Exception e) {
            String msg = e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }

    public EventTypeBean getEventTypes(String path, String sessionId) throws RegistryException {
        EventTypeBean result = new EventTypeBean();
        try {
            org.wso2.carbon.registry.info.stub.beans.xsd.EventTypeBean bean =
                    stub.getEventTypes(path, sessionId);
            if (bean.getEventTypes() == null) {
                bean.setEventTypes(
                        new org.wso2.carbon.registry.info.stub.beans.utils.xsd.EventType[0]);
            }
            org.wso2.carbon.registry.info.stub.beans.utils.xsd.EventType[] eventTypes =
                bean.getEventTypes();
            EventType[] resultEventTypes = new EventType[eventTypes.length];
            int i = 0;
            for (org.wso2.carbon.registry.info.stub.beans.utils.xsd.EventType eventType : eventTypes) {
                if (eventType == null) {
                    continue;
                }
                resultEventTypes[i] = new EventType();
                resultEventTypes[i].setCollectionEvent(eventType.getCollectionEvent());
                resultEventTypes[i].setId(eventType.getId());
                resultEventTypes[i].setResourceEvent(eventType.getResourceEvent());
                i++;
            }
            result.setEventTypes(resultEventTypes);
            result.setErrorMessage(bean.getErrorMessage());
        } catch (Exception e) {
            String msg = e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
        return result;
    }

    public SubscriptionBean getSubscriptions(String path, String sessionId) throws RegistryException {
        SubscriptionBean result = new SubscriptionBean();
        try {
            org.wso2.carbon.registry.info.stub.beans.xsd.SubscriptionBean bean =
                    stub.getSubscriptions(path, sessionId);
            if (bean.getSubscriptionInstances() == null) {
                bean.setSubscriptionInstances(
                        new org.wso2.carbon.registry.info.stub.beans.utils.xsd.SubscriptionInstance[0]);
            }
            org.wso2.carbon.registry.info.stub.beans.utils.xsd.SubscriptionInstance[] subscriptions =
                bean.getSubscriptionInstances();
            SubscriptionInstance[] resultSubscriptions =
                    new SubscriptionInstance[subscriptions.length];
            int i = 0;
            for (org.wso2.carbon.registry.info.stub.beans.utils.xsd.SubscriptionInstance subscription
                    : subscriptions) {
                if (subscription == null) {
                    continue;
                }
                resultSubscriptions[i] = new SubscriptionInstance();
                resultSubscriptions[i].setAddress(subscription.getAddress());
                resultSubscriptions[i].setEventName(subscription.getEventName());
                resultSubscriptions[i].setId(subscription.getId());
                resultSubscriptions[i].setNotificationMethod(subscription.getNotificationMethod());
                resultSubscriptions[i].setSubManUrl(subscription.getSubManUrl());
                resultSubscriptions[i].setDigestType(subscription.getDigestType());
                resultSubscriptions[i].setTopic(subscription.getTopic());
                i++;
            }
            result.setSubscriptionInstances(resultSubscriptions);
            result.setUserAccessLevel(bean.getUserAccessLevel());
            result.setRoleAccessLevel(bean.getRoleAccessLevel());
            result.setUserName(bean.getUserName());
            result.setRoles(bean.getRoles());
            result.setErrorMessage(bean.getErrorMessage());
            result.setLoggedIn(bean.getLoggedIn());
            result.setPathWithVersion(bean.getPathWithVersion());
            result.setVersionView(bean.getVersionView());
        } catch (Exception e) {
            String msg = e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
        return result;
    }

    public SubscriptionBean subscribe(String path, String endpoint, String eventName, String sessionId) throws RegistryException {
        SubscriptionBean result = new SubscriptionBean();
        try {
            org.wso2.carbon.registry.info.stub.beans.xsd.SubscriptionBean bean =
                    stub.subscribe(path, endpoint, eventName, sessionId);
            if (bean == null) {
                bean = new org.wso2.carbon.registry.info.stub.beans.xsd.SubscriptionBean();
            }
            if (bean.getSubscriptionInstances() == null) {
                bean.setSubscriptionInstances(
                        new org.wso2.carbon.registry.info.stub.beans.utils.xsd.SubscriptionInstance[0]);
            }
            org.wso2.carbon.registry.info.stub.beans.utils.xsd.SubscriptionInstance[] subscriptions =
                bean.getSubscriptionInstances();
            SubscriptionInstance[] resultSubscriptions =
                    new SubscriptionInstance[subscriptions.length];
            int i = 0;
            for (org.wso2.carbon.registry.info.stub.beans.utils.xsd.SubscriptionInstance subscription
                    : subscriptions) {
                if (subscription == null) {
                    continue;
                }
                resultSubscriptions[i] = new SubscriptionInstance();
                resultSubscriptions[i].setAddress(subscription.getAddress());
                resultSubscriptions[i].setEventName(subscription.getEventName());
                resultSubscriptions[i].setId(subscription.getId());
                resultSubscriptions[i].setNotificationMethod(subscription.getNotificationMethod());
                resultSubscriptions[i].setSubManUrl(subscription.getSubManUrl());
                resultSubscriptions[i].setDigestType(subscription.getDigestType());
                resultSubscriptions[i].setTopic(subscription.getTopic());
                i++;
            }
            result.setSubscriptionInstances(resultSubscriptions);
            result.setUserAccessLevel(bean.getUserAccessLevel());
            result.setRoleAccessLevel(bean.getRoleAccessLevel());
            result.setUserName(bean.getUserName());
            result.setRoles(bean.getRoles());
            result.setErrorMessage(bean.getErrorMessage());
            result.setLoggedIn(bean.getLoggedIn());
            result.setPathWithVersion(bean.getPathWithVersion());
            result.setVersionView(bean.getVersionView());
        } catch (Exception e) {
            String msg = e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
        return result;
    }

    public SubscriptionBean subscribeREST(String path, String endpoint, String eventName, String sessionId) throws RegistryException {
        SubscriptionBean result = new SubscriptionBean();
        try {
            org.wso2.carbon.registry.info.stub.beans.xsd.SubscriptionBean bean =
                    stub.subscribeREST(path, endpoint, eventName, sessionId);
            if (bean == null) {
                bean = new org.wso2.carbon.registry.info.stub.beans.xsd.SubscriptionBean();
            }
            if (bean.getSubscriptionInstances() == null) {
                bean.setSubscriptionInstances(
                        new org.wso2.carbon.registry.info.stub.beans.utils.xsd.SubscriptionInstance[0]);
            }
            org.wso2.carbon.registry.info.stub.beans.utils.xsd.SubscriptionInstance[] subscriptions =
                bean.getSubscriptionInstances();
            SubscriptionInstance[] resultSubscriptions =
                    new SubscriptionInstance[subscriptions.length];
            int i = 0;
            for (org.wso2.carbon.registry.info.stub.beans.utils.xsd.SubscriptionInstance subscription
                    : subscriptions) {
                if (subscription == null) {
                    continue;
                }
                resultSubscriptions[i] = new SubscriptionInstance();
                resultSubscriptions[i].setAddress(subscription.getAddress());
                resultSubscriptions[i].setEventName(subscription.getEventName());
                resultSubscriptions[i].setId(subscription.getId());
                resultSubscriptions[i].setNotificationMethod(subscription.getNotificationMethod());
                resultSubscriptions[i].setSubManUrl(subscription.getSubManUrl());
                resultSubscriptions[i].setDigestType(subscription.getDigestType());
                resultSubscriptions[i].setTopic(subscription.getTopic());
                i++;
            }
            result.setSubscriptionInstances(resultSubscriptions);
            result.setUserAccessLevel(bean.getUserAccessLevel());
            result.setRoleAccessLevel(bean.getRoleAccessLevel());
            result.setUserName(bean.getUserName());
            result.setRoles(bean.getRoles());
            result.setErrorMessage(bean.getErrorMessage());
            result.setLoggedIn(bean.getLoggedIn());
            result.setPathWithVersion(bean.getPathWithVersion());
            result.setVersionView(bean.getVersionView());
        } catch (Exception e) {
            String msg = e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
        return result;
    }

    public boolean isResource(String path, String sessionId) throws RegistryException {
        try {
            return stub.isResource(path, sessionId);
        } catch (Exception e) {
            String msg = e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }

    public String getRemoteURL(String path, String sessionId) throws RegistryException {
        try {
            return stub.getRemoteURL(path, sessionId);
        } catch (Exception e) {
            String msg = e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }

    public String verifyEmail(String data, String sessionId) throws RegistryException {
        try {
            return stub.verifyEmail(data, sessionId);
        } catch (Exception e) {
            String msg = e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }

    public boolean unsubscribe(String path, String id, String sessionId) throws RegistryException {
        try {
            return stub.unsubscribe(path, id, sessionId);
        } catch (Exception e) {
            String msg = e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }

    public boolean isUserValid(String username, String sessionId) throws RegistryException {
        try {
            return stub.isUserValid(username, sessionId);
        } catch (Exception e) {
            String msg = e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }


    public boolean isProfileExisting(String username, String sessionId) throws RegistryException {
        try {
            return stub.isProfileExisting(username, sessionId);
        } catch (Exception e) {
            String msg = e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }

    public boolean isRoleValid(String role, String sessionId) throws RegistryException {
        try {
            return stub.isRoleValid(role, sessionId);
        } catch (Exception e) {
            String msg = e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }


    public boolean isRoleProfileExisting(String role, String sessionId) throws RegistryException {
        try {
            return stub.isRoleProfileExisting(role, sessionId);
        } catch (Exception e) {
            String msg = e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }

	
	
}
