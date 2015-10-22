/*
 *  Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.registry.eventing.services;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.event.core.exception.EventBrokerException;
import org.wso2.carbon.registry.event.core.subscription.EventDispatcher;
import org.wso2.carbon.registry.event.core.subscription.Subscription;
import org.wso2.carbon.registry.common.eventing.RegistryEvent;
import org.wso2.carbon.registry.common.utils.RegistryUtil;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.eventing.RegistryEventDispatcher;
import org.wso2.carbon.registry.eventing.RegistryEventingConstants;
import org.wso2.carbon.registry.eventing.events.ChildCreatedEvent;
import org.wso2.carbon.registry.eventing.events.ChildDeletedEvent;
import org.wso2.carbon.registry.eventing.events.CollectionAddedEvent;
import org.wso2.carbon.registry.eventing.events.CollectionCreatedEvent;
import org.wso2.carbon.registry.eventing.events.CollectionDeletedEvent;
import org.wso2.carbon.registry.eventing.events.CollectionUpdatedEvent;
import org.wso2.carbon.registry.eventing.events.DispatchEvent;
import org.wso2.carbon.registry.eventing.events.ResourceAddedEvent;
import org.wso2.carbon.registry.eventing.events.ResourceCreatedEvent;
import org.wso2.carbon.registry.eventing.events.ResourceDeletedEvent;
import org.wso2.carbon.registry.eventing.events.ResourceUpdatedEvent;
import org.wso2.carbon.registry.eventing.internal.EventingDataHolder;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class EventingServiceImpl implements EventingService, SubscriptionEmailVerficationService {

    private Map<String, String[]> eventTypeMap;
    private Map<String, List<String>> eventTypeExclusionMap;
    private static boolean initialized = false;
    private static EventDispatcher dispatcher = null;
    private ExecutorService executor = null;
    private String emailIndexStoragePath;
    public static List<String> listOfMediaTypes = new ArrayList<String>();
    private static final String MEDIA_TYPE_MATCHER_FILTER_CLASS = "org.wso2.carbon.registry.core.jdbc.handlers.filters.MediaTypeMatcher";
    private static final Log log = LogFactory.getLog(EventingServiceImpl.class);

    public String verifyEmail(String data) {
        String subscriptionId = null;
        String username = null;
        String remoteURL = null;
        String email = null;

        if (data != null) {
            try {
                OMElement dataElement = AXIOMUtil.stringToOM(data);
                Iterator it = dataElement.getChildElements();
                while (it.hasNext()) {
                    OMElement element = (OMElement) it.next();
                    if ("subscriptionId".equals(element.getLocalName())) {
                        subscriptionId = element.getText();
                    } else if ("username".equals(element.getLocalName())) {
                        username = element.getText();
                    } else if ("remoteURL".equals(element.getLocalName())) {
                        remoteURL = element.getText();
                    } else if ("email".equals(element.getLocalName())) {
                        email = element.getText();
                    }
                }
            } catch (XMLStreamException e) {
                log.error("Unable to verify e-mail address", e);
                return null;
            }
        }

        try {
            if (username != null || remoteURL != null) {
               throw new UnsupportedOperationException("This method is no longer supported");
            } else {
                Subscription subscription = getSubscription(subscriptionId);
                Map<String, String> subscriptionData = subscription.getProperties();
                subscriptionData.remove(RegistryEventingConstants.NOT_VERIFIED);
                subscription.setProperties(subscriptionData);
                // The updated subscription should be directly done at the subMgr to avoid
                // generating another verification request
                subscription.setId(subscriptionId);
                EventingDataHolder.getInstance().getRegistryEventBrokerService().renewSubscription(subscription);
            }
            return email;
        } catch (Exception e) {
            log.error("Unable to verify e-mail address", e);
            return null;
        }
    }

    public EventingServiceImpl() {
        try {
            if (initialized) {
                return;
            } else {
                initialized = true;
            }
            eventTypeMap = new TreeMap<String, String[]>();
            eventTypeExclusionMap = new HashMap<String, List<String>>();
            emailIndexStoragePath="/repository/components/org.wso2.carbon.email-verification/emailIndex";
            registerBuiltInEventTypes();
            storeHandlerConfig();
        } catch (Exception e) {
            log.error("Error initializing Registry Event Broker");
        }
    }

    public void registerBuiltInEventTypes() {
        registerEventType("update", ResourceUpdatedEvent.EVENT_NAME, CollectionUpdatedEvent.EVENT_NAME);
        registerEventType("delete", ResourceDeletedEvent.EVENT_NAME, CollectionDeletedEvent.EVENT_NAME);
        registerEventType("child.deleted", null, ChildDeletedEvent.EVENT_NAME);
        registerEventType("child.created", null, ChildCreatedEvent.EVENT_NAME);
    }

    private static void storeHandlerConfig() {
        String configPath = CarbonUtils.getRegistryXMLPath();
        Set<String> hashSet = new HashSet<>();
        if (configPath != null) {
            File registryXML = new File(configPath);
            try {
                InputStream configInputStream = new FileInputStream(registryXML);
                StAXOMBuilder builder = new StAXOMBuilder(
                        CarbonUtils.replaceSystemVariablesInXml(configInputStream));
                OMElement configElement = builder.getDocumentElement();
                Iterator<OMElement> handlerConfigs =
                        configElement.getChildrenWithName(new QName("handler"));
                while (handlerConfigs.hasNext()) {
                    OMElement handlerConfigElement = handlerConfigs.next();
                    OMElement filter = handlerConfigElement.getFirstChildWithName(new QName("filter"));
                    String filterClass = filter.getAttributeValue(new QName("class"));
                    OMElement property = filter.getFirstChildWithName(new QName("property"));
                    if (MEDIA_TYPE_MATCHER_FILTER_CLASS.equals(filterClass) &&
                        "mediaType".equals(property.getAttributeValue(new QName("name")))) {
                        listOfMediaTypes.add(property.getText());
                    }
                }
                hashSet.addAll(listOfMediaTypes);
                listOfMediaTypes.clear();
                listOfMediaTypes.addAll(hashSet);
            } catch (FileNotFoundException e) {
                log.error("registry.xml file not found", e);
            } catch (CarbonException e) {
                log.error("Error in converting registry xml inputstream", e);
            } catch (XMLStreamException e) {
                log.error("Error in registry xml stream", e);
            }
        }

    }

    public void notify(RegistryEvent event) throws Exception {
        notify(event, null);
    }

    public void notify(RegistryEvent event, String endpoint) throws Exception {
        notify(event, endpoint, false);
    }

    private synchronized static void initializeDispatcher() {
        if (dispatcher == null) {
            dispatcher = new RegistryEventDispatcher();
            ((RegistryEventDispatcher)dispatcher).init(EventingDataHolder.getInstance().getConfigurationContext());
        }
    }

    public void notify(RegistryEvent event, String endpoint, boolean doRest)
            throws Exception {
        if (!initialized) {
            return;
        }
        if (executor == null) {
            setupExecutorService();
        }
        if (dispatcher == null) {
            try {
                initializeDispatcher();
            } catch (IllegalStateException ignored) {
                // We throw an Illegal State Exception only if the dispatcher failed to initialize.
                // There is no point in continuing after that. Such a scenario can happen if the
                // very first event was generated during shutdown and we are unable to create
                // corresponding dispatch queues.
                return;
            }
        }
        executor.submit(new Publisher(new DispatchEvent(event, endpoint, doRest)));
    }

    public void registerEventType(String typeId, String resourceEvent, String collectionEvent) {
        String[] eventNames = new String[2];
        eventNames[0] = resourceEvent;
        eventNames[1] = collectionEvent;
        eventTypeMap.put(typeId, eventNames);
    }

    public List<Subscription> getAllSubscriptions() throws EventBrokerException {
        return EventingDataHolder.getInstance().getRegistryEventBrokerService().getAllSubscriptions(null);
    }

    public List<Subscription> getAllSubscriptions(String userName, String remoteURL)
            throws EventBrokerException {
        throw new UnsupportedOperationException("This method is no longer supported");
    }

    public Map getEventTypes() {
        return eventTypeMap;
    }

    public String getSubscriptionManagerUrl() {
        return EventingDataHolder.getInstance().getDefaultEventingServiceURL();
    }

    public Subscription getSubscription(String id) {
        try {
            if (id != null && id.contains(";version:")) {
                log.warn(
                        "Versioned resources cannot have subscriptions, instead returns the subscription from the actual resource");
                return EventingDataHolder.getInstance().getRegistryEventBrokerService()
                                         .getSubscription(RegistryUtil.getResourcePathFromVersionPath(id));
            } else {
                return EventingDataHolder.getInstance().getRegistryEventBrokerService().getSubscription(id);
            }
        } catch (EventBrokerException e) {
            log.error("Unable to get subscription for given id: " + id, e);
            return null;
        }
    }

    private void requestEmailVerification(Subscription subscription,
                                          String userName, String remoteURL) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (executor == null) {
            setupExecutorService();
        }
        if(!subscription.getProperties().isEmpty()){
            executor.submit(new EmailVerifier(subscription, userName, remoteURL, tenantId));
        }
    }

    private synchronized void setupExecutorService() {
        if (executor == null) {
            executor = new ThreadPoolExecutor(25, 150,
            1000, TimeUnit.NANOSECONDS, new ArrayBlockingQueue<Runnable>(100));
        }
    }

    public String subscribe(Subscription subscription, String userName, String remoteURL) {
        throw new UnsupportedOperationException("This method is no longer supported");
    }

    public String subscribe(Subscription subscription) {
        try {
            boolean emailAvailability = isEmailAlreadyAvailable(subscription);
            if ((subscription.getEventSinkURL().startsWith("mailto:") || subscription.getEventSinkURL().startsWith("digest:"))
                    && !emailAvailability) {
                Map<String, String> subscriptionData = subscription.getProperties();
                subscriptionData.put(RegistryEventingConstants.NOT_VERIFIED,
                        Boolean.toString(true));
                subscription.setProperties(subscriptionData);
            }
            String subscribe = EventingDataHolder.getInstance().getRegistryEventBrokerService().subscribe(subscription);
            requestEmailVerification(subscription, null, null);
            return subscribe;
        } catch (EventBrokerException e) {
            log.error("Unable to add subscription", e);
            return null;
        }
    }

    private boolean isEmailAlreadyAvailable(Subscription subscription) {
        // if one-time email verification is false or not defined, we do not need to check resource, and we will treat it as the resource was
        // not available.
        if (Boolean.parseBoolean(System.getProperty("onetime.email.verification", Boolean.toString(false)))) {
            String email = subscription.getEventSinkURL().substring(subscription.getEventSinkURL().indexOf(":") + 1
                    , subscription.getEventSinkURL().length());
            try {
                UserRegistry registry = EventingDataHolder.getInstance().getRegistryService().getConfigSystemRegistry();
                String emailIndexPath = this.emailIndexStoragePath;
                Resource emailIndexResource;
                if (registry.resourceExists(emailIndexPath)) {
                    emailIndexResource = registry.get(emailIndexPath);
                    Collection<Object> values = emailIndexResource.getProperties().values();
                    for (Iterator it = values.iterator(); it.hasNext(); ) {
                        String value = (((ArrayList) (it.next())).toArray())[0].toString();
                        if (value.equals(email)) {
                            return true;
                        }
                    }
                } else {
                    emailIndexResource = registry.newResource();
                    registry.put(emailIndexPath, emailIndexResource);
                }
            } catch (RegistryException e) {
                log.error("Unable to check email verification resource", e);
            }
        }
        return false;
    }

    public Subscription getSubscription(String id, String userName, String remoteURL) {
        throw new UnsupportedOperationException("This method is no longer supported");
    }

    public boolean unsubscribe(String subscriptionID) {
        try {
            EventingDataHolder.getInstance().getRegistryEventBrokerService().unsubscribe(subscriptionID);
            return true;
        } catch (EventBrokerException e) {
            log.error("Unable to unsubscribe using given id: " + subscriptionID, e);
            return false;
        }
    }

    public boolean unsubscribe(String subscriptionID, String userName, String remoteURL) {
        throw new UnsupportedOperationException("This method is no longer supported");
    }

    public void registerEventTypeExclusion(String typeId, String path) {
        if (path == null) {
            return;
        }
        List<String> list = eventTypeExclusionMap.get(typeId);
        if (list == null) {
            list = new LinkedList<String>();
        }
        list.add(path);
        eventTypeExclusionMap.put(typeId, list);
    }

    public boolean isEventTypeExclusionRegistered(String typeId, String path) {
        List<String> list = eventTypeExclusionMap.get(typeId);
        if (list == null) {
            return false;
        }
        for(String s: list) {
            if (path.matches(s)) {
                return true;
            }
        }
        return false;
    }

    private static class EmailVerifier implements Runnable {

        private Subscription subscription;
        private String userName;
        private String remoteURL;
        private int tenantId;
        private String tenantDomain;


        public EmailVerifier(Subscription subscription, String userName, String remoteURL,
                             int tenantId) {
            this.subscription = subscription;
            this.userName = userName;
            this.remoteURL = remoteURL;
            this.tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            this.tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        }

        @SuppressWarnings("unchecked")
        public void run() {
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
            if (EventingDataHolder.getInstance().getEmailVerificationSubscriber() == null) {
                return;
            }
            String eventUrl = subscription.getEventSinkURL();
            String lowerCasedEventUrl=eventUrl.toLowerCase();

            String email = "";
            if (lowerCasedEventUrl.startsWith("digest:")&& lowerCasedEventUrl.contains("mailto")) {
                // extracting the mailto: address form eventsink url like digest://h/mailto:email
                email = eventUrl.substring("digest:".length() +4);
            }    // user verification for emails.
            else if(lowerCasedEventUrl.contains("mailto:")) {
                email = eventUrl;
            }
            // we are not requesting verifications for user and roles.
            else{
                return;
            }
            email = email.substring("mailto:".length());
            Map<String,String> data = new HashMap<String, String>();
            data.put("email", email);
            data.put("subscriptionId", subscription.getId());
            if (userName != null) {
                data.put("username", userName);
            }
            if (remoteURL != null) {
                data.put("remoteURL", remoteURL);
            }
            try {
                EventingDataHolder.getInstance().getEmailVerificationSubscriber().requestUserVerification(data,
                                                     EventingDataHolder.getInstance().getEmailVerifierConfig());
            } catch (Exception e) {
                log.error("Unable to create e-mail verification request", e);
            }
        }
    }

    private static class Publisher implements Runnable {

        private DispatchEvent event;

        public Publisher(DispatchEvent event) {
            this.event = event;
        }

        public void run() {
            try {
                // Setup the Carbon Context so that downstream logic can make use of it.
                // At present, the Event Component makes use of the information stored on the Carbon
                // Context when publishing events.
                PrivilegedCarbonContext.startTenantFlow();
                try {
                    PrivilegedCarbonContext context =
                            PrivilegedCarbonContext.getThreadLocalCarbonContext();
                    int tenantId = event.getTenantId();
                    if (tenantId != -1) {
                        context.setTenantId(tenantId, true);
                    }
                    RegistryEvent.RegistrySession registrySessionDetails =
                            event.getRegistrySessionDetails();
                    if (registrySessionDetails != null) {
                        String username = registrySessionDetails.getUsername();
                        if (username != null) {
                            context.setUsername(username);
                        }
                    }
                    // For each resource-event also generate a corresponding collection event, so that any hierarchical subscriptions would
                    // work.
                    if (event.getTopic().contains(ResourceUpdatedEvent.EVENT_NAME)) {
                        EventingDataHolder.getInstance().getRegistryEventBrokerService()
                                          .publish(event, event.getTopic());
                        EventingDataHolder.getInstance().getRegistryEventBrokerService().publish(event, event.getTopic()
                                          .replace(ResourceUpdatedEvent.EVENT_NAME, CollectionUpdatedEvent.EVENT_NAME));
                    } else if (event.getTopic().contains(ResourceAddedEvent.EVENT_NAME)) {
                        EventingDataHolder.getInstance().getRegistryEventBrokerService()
                                          .publish(event, event.getTopic());
                        EventingDataHolder.getInstance().getRegistryEventBrokerService().publish(event, event.getTopic()
                                          .replace(ResourceAddedEvent.EVENT_NAME, CollectionAddedEvent.EVENT_NAME));
                    } else if (event.getTopic().contains(ResourceCreatedEvent.EVENT_NAME)) {
                        EventingDataHolder.getInstance().getRegistryEventBrokerService()
                                          .publish(event, event.getTopic());
                        EventingDataHolder.getInstance().getRegistryEventBrokerService().publish(event, event.getTopic()
                                          .replace(ResourceCreatedEvent.EVENT_NAME, CollectionCreatedEvent.EVENT_NAME));
                    } else if (event.getTopic().contains(ResourceDeletedEvent.EVENT_NAME)) {
                        EventingDataHolder.getInstance().getRegistryEventBrokerService().publish(event, event.getTopic());
                        EventingDataHolder.getInstance().getRegistryEventBrokerService().publish(event, event.getTopic()
                                          .replace(ResourceDeletedEvent.EVENT_NAME, CollectionDeletedEvent.EVENT_NAME));
                    } else {
                        EventingDataHolder.getInstance().getRegistryEventBrokerService()
                                          .publish(event, event.getTopic());
                    }
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            } catch (EventBrokerException e) {
                log.error("Unable to send notification", e);
            }
        }
    }

}
