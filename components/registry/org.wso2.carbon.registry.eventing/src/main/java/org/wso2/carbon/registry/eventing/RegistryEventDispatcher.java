/*
 *  Copyright (c) 2005-2008,2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.eventing;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.axis2.transport.mail.MailConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.core.Message;
import org.wso2.carbon.event.core.subscription.Subscription;
import org.wso2.carbon.event.ws.internal.notify.WSEventDispatcher;
import org.wso2.carbon.event.ws.internal.util.EventingConstants;
import org.wso2.carbon.governance.notifications.worklist.stub.WorkListServiceStub;
import org.wso2.carbon.registry.common.eventing.RegistryEvent;
import org.wso2.carbon.registry.common.eventing.WorkListConfig;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.eventing.events.DispatchEvent;
import org.wso2.carbon.registry.eventing.internal.EventingDataHolder;
import org.wso2.carbon.registry.eventing.internal.JMXEventsBean;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.utils.CarbonUtils;

import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.xml.namespace.QName;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class RegistryEventDispatcher extends WSEventDispatcher {

    private static final SimpleDateFormat EVENT_TIME =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private ConfigurationContext configContext = null;

    private Map<String, Queue<DigestEntry>> digestQueues;

    private static final Log log = LogFactory.getLog(RegistryEventDispatcher.class);

    private static Session session;
    private static ThreadPoolExecutor threadPoolExecutor;
    private InternetAddress smtpFromAddress = null;
    public static final int MIN_THREAD = 8;
    public static final int MAX_THREAD = 100;
    public static final String MIN_THREAD_NAME = "minThread";
    public static final String MAX_THREAD_NAME = "maxThread";
    public static final String DEFAULT_KEEP_ALIVE_TIME_NAME = "defaultKeepAliveTime";
    public static final long DEFAULT_KEEP_ALIVE_TIME = 20;


    private static WorkListConfig workListConfig = new WorkListConfig();

    public static final class DigestEntry implements Serializable {

        private static final long serialVersionUID = -1805410413253360172L;

        private String message;
        private String endpoint;
        private long time;

        public DigestEntry(String message, String endpoint, long time) {
            this.message = message;
            this.endpoint = endpoint;
            this.time = time;
        }

        public String getMessage() {
            return message;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public long getTime() {
            return time;
        }
    }

    public RegistryEventDispatcher() {
        digestQueues = new LinkedHashMap<String, Queue<DigestEntry>>();
        for (String s : new String[]{"h", "d", "w", "f", "m", "y"}) {
            //TODO: Identify Queuing mechanisms.
            digestQueues.put(s, new ConcurrentLinkedQueue<DigestEntry>());
        }
        final ScheduledExecutorService executorService =
                Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(new Runnable() {
            public void run() {
                GregorianCalendar utc = new GregorianCalendar(SimpleTimeZone.getTimeZone("UTC"));
                Map<String, List<DigestEntry>> digestEntries =
                        new HashMap<String, List<DigestEntry>>();
                try {
                    addToDigestEntryQueue(digestEntries, "h");
                    if (utc.get(Calendar.HOUR_OF_DAY) == 0) {
                        addToDigestEntryQueue(digestEntries, "d");
                        if (utc.get(Calendar.DAY_OF_WEEK) == 1) {
                            addToDigestEntryQueue(digestEntries, "w");
                            if (utc.get(Calendar.WEEK_OF_YEAR) % 2 != 0) {
                                addToDigestEntryQueue(digestEntries, "f");
                            }
                        }
                        if (utc.get(Calendar.DAY_OF_MONTH) == 1) {
                            addToDigestEntryQueue(digestEntries, "m");
                            if (utc.get(Calendar.DAY_OF_YEAR) == 1) {
                                addToDigestEntryQueue(digestEntries, "y");

                            }
                        }
                    }
                    for (Map.Entry<String, List<DigestEntry>> e : digestEntries.entrySet()) {
                        List<DigestEntry> value = e.getValue();
                        Collections.sort(value, new Comparator<DigestEntry>() {
                            public int compare(DigestEntry o1,
                                               DigestEntry o2) {
                                if (o1.getTime() > o2.getTime()) {
                                    return -1;
                                } else if (o1.getTime() < o2.getTime()) {
                                    return 1;
                                }
                                return 0;
                            }
                        });
                        StringBuffer buffer = new StringBuffer();
                        for (DigestEntry entry : value) {
                            buffer.append(entry.getMessage()).append("\n\n");
                        }
                        RegistryEvent<String> re = new RegistryEvent<String>(buffer.toString());
                        re.setTopic(RegistryEvent.TOPIC_SEPARATOR + "DigestEvent");
                        DispatchEvent de = new DispatchEvent(re, e.getKey(), true);
                        Subscription subscription = new Subscription();
                        subscription.setTopicName(re.getTopic());
                        publishEvent(de, subscription, e.getKey(), true);
                    }
                } catch (RuntimeException ignored) {
                    // Eat any runtime exceptions that occurred, we don't care if the message went
                    // or not.
                }
            }
        }, System.currentTimeMillis() % (1000 * 60 * 60), 1000 * 60 * 60, TimeUnit.MILLISECONDS);
        try {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    executorService.shutdownNow();
                }
            });
        } catch (IllegalStateException e) {
            executorService.shutdownNow();
            throw new IllegalStateException("Unable to create registry event dispatcher during " +
                    "shutdown process.");
        }
    }

    private void addToDigestEntryQueue(Map<String, List<DigestEntry>> digestEntries,
                                       String digestType) {
        Queue<DigestEntry> digestQueue = getDigestQueue(digestType);
        while (!digestQueue.isEmpty()) {
            DigestEntry entry = digestQueue.poll();
            String endpoint = entry.getEndpoint();
            List<DigestEntry> digestEntriesForEndpoint;
            if (!digestEntries.containsKey(endpoint)) {
                digestEntriesForEndpoint = new LinkedList<DigestEntry>();
                digestEntries.put(endpoint, digestEntriesForEndpoint);
            } else {
                digestEntriesForEndpoint = digestEntries.get(endpoint);
            }
            digestEntriesForEndpoint.add(entry);
        }
    }

    private Queue<DigestEntry> getDigestQueue(String digestType) {
        Queue<DigestEntry> entryQueue = digestQueues.get(digestType);
        if (entryQueue != null) {
            return entryQueue;
        }
        String msg = "Invalid Digest Type: " + digestType;
        log.error(msg);
        throw new RuntimeException(msg);
    }

    public void notify(Message event, Subscription subscription) {
        if (subscription == null) {
            return;
        }
        String endpoint = subscription.getEventSinkURL();
        if (endpoint == null) {
            return;
        }
        if (subscription.getProperties() != null) {
            int tenantId = subscription.getTenantId();
            /*int tenantIdOfEventObj = SuperTenantCarbonContext.getCurrentContext(
                    ((MessageContext)event.getMessage())).getTenantId();*/
            if (event instanceof DispatchEvent) {
                int tenantIdOfEventObj = ((DispatchEvent) event).getTenantId();
                if (tenantId != tenantIdOfEventObj) {
                    log.warn("TenantId for subscription doesn't match with the logged-in tenant");
                    return;
                }
            }
        }
        String topic = subscription.getTopicName();
        boolean doRest = (subscription.getProperties() != null &&
                subscription.getProperties().get(
                        RegistryEventingConstants.DO_REST) != null &&
                (subscription.getProperties().get(
                        RegistryEventingConstants.DO_REST)).equals(
                        Boolean.toString(Boolean.TRUE)));
        if (endpoint.toLowerCase().startsWith("digest://")) {
            String digestType = endpoint.substring(9, 10);
            endpoint = endpoint.substring(11);

            if(endpoint.toLowerCase().contains("user://")){
                try {
                    String username = endpoint.substring(7);
                    if (EventingDataHolder.getInstance().getRegistryService() != null) {
                        UserRegistry registry = EventingDataHolder.getInstance().getRegistryService().getConfigSystemRegistry();
                        if (registry != null && registry.getUserRealm() != null &&
                            registry.getUserRealm().getUserStoreManager() != null) {
                            UserStoreManager reader = registry.getUserRealm().getUserStoreManager();
                            endpoint = "mailto:" + reader.getUserClaimValue(username,
                                                                            UserCoreConstants.ClaimTypeURIs.EMAIL_ADDRESS,
                                                                            UserCoreConstants.DEFAULT_PROFILE);
                        }
                    }
                } catch (Exception e) {
                    log.error("Failed Sending Notification to: " + endpoint);
                    return;
                }
                this.addToEmailDigestQueue(event,topic,endpoint,digestType,subscription.getTopicName());
                return;
            }
            else if(endpoint.toLowerCase().contains("role://")){
                List<String> emails = new LinkedList<String>();
                try {
                    String roleName = endpoint.substring(7);
                    if (EventingDataHolder.getInstance().getRegistryService() != null) {
                        UserRegistry registry =
                                EventingDataHolder.getInstance().getRegistryService().getConfigSystemRegistry();
                        if (registry != null && registry.getUserRealm() != null &&
                            registry.getUserRealm().getUserStoreManager() != null) {
                            UserStoreManager reader = registry.getUserRealm().getUserStoreManager();
                            for (String username : reader.getUserListOfRole(roleName)) {
                                String temp = reader.getUserClaimValue(username,
                                                                       UserCoreConstants.ClaimTypeURIs.EMAIL_ADDRESS,
                                                                       UserCoreConstants.DEFAULT_PROFILE);
                                if (temp != null && temp.length() > 0) {
                                    emails.add("mailto:" + temp);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("Failed Sending Notification to: " + endpoint);
                    return;
                }
                for (String email : emails) {
                    log.debug("Sending Notification to: " + email);
                    this.addToEmailDigestQueue(event, topic, email, digestType,subscription.getTopicName());
                }
                return;
            }else {
                this.addToEmailDigestQueue(event,topic,endpoint,digestType,subscription.getTopicName());
                return;
            }
        }
        if (endpoint.toLowerCase().startsWith("mailto:")) {
            if (subscription.getProperties() != null &&
                    Boolean.toString(true).equals(subscription.getProperties().get(
                            RegistryEventingConstants.NOT_VERIFIED))) {
                String email = endpoint.toLowerCase().substring("mailto:".length());
                log.warn("Unable to send notification. The e-mail address " + email +
                        " has not been verified.");
                return;
            }
            log.debug("Sending Notification to: " + endpoint);
            publishEvent(event, subscription, endpoint, true);
        } else if (endpoint.toLowerCase().startsWith("user://")) {
            String email = null;
            try {
                String username = endpoint.substring(7);
                if (EventingDataHolder.getInstance().getRegistryService() != null) {
                    UserRegistry registry = EventingDataHolder.getInstance().getRegistryService()
                                              .getGovernanceSystemRegistry(PrivilegedCarbonContext
                                                                                   .getThreadLocalCarbonContext()
                                                                                   .getTenantId());
                    if (registry != null && registry.getUserRealm() != null &&
                            registry.getUserRealm().getUserStoreManager() != null) {
                        UserStoreManager reader = registry.getUserRealm().getUserStoreManager();
                        email = "mailto:" + reader.getUserClaimValue(username,
                                UserCoreConstants.ClaimTypeURIs.EMAIL_ADDRESS,
                                UserCoreConstants.DEFAULT_PROFILE);
                    }
                }
            } catch (Exception e) {
                log.error("Failed Sending Notification to: " + endpoint);
                return;
            }
            log.debug("Sending Notification to: " + email);
            publishEvent(event, subscription, email, true);
        } else if (endpoint.toLowerCase().startsWith("role://")) {
            List<String> emails = new LinkedList<String>();
            try {
                String roleName = endpoint.substring(7);
                if (EventingDataHolder.getInstance().getRegistryService() != null) {
                    UserRegistry registry = EventingDataHolder.getInstance().getRegistryService()
                                                    .getGovernanceSystemRegistry(PrivilegedCarbonContext
                                                                                         .getThreadLocalCarbonContext()
                                                                                         .getTenantId());
                    if (registry != null && registry.getUserRealm() != null &&
                            registry.getUserRealm().getUserStoreManager() != null) {
                        UserStoreManager reader = registry.getUserRealm().getUserStoreManager();
                        for (String username : reader.getUserListOfRole(roleName)) {
                            String temp = reader.getUserClaimValue(username,
                                    UserCoreConstants.ClaimTypeURIs.EMAIL_ADDRESS,
                                    UserCoreConstants.DEFAULT_PROFILE);
                            if (temp != null && temp.length() > 0) {
                                emails.add("mailto:" + temp);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Failed Sending Notification to: " + endpoint);
                return;
            }

            for (String email : emails) {
                log.debug("Sending Notification to: " + email);
                publishEvent(event, subscription, email, true);
            }
        } else if (endpoint.toLowerCase().startsWith("jmx://")) {
            log.debug("Sending Notification to JMX endpoint");
            JMXEventsBean eventsBean = EventingDataHolder.getInstance().getEventsBean();
            if (eventsBean == null) {
                log.warn("Unable to generate notification. The notification bean has not been " +
                        "registered.");
            } else {
                OMElement message = event.getMessage();
                OMElement firstElement = message.getFirstElement();
                String namespaceURI = firstElement.getNamespace().getNamespaceURI();
                OMElement timestamp = message.getFirstChildWithName(new QName(namespaceURI,
                        "Timestamp"));
                if (timestamp != null) {
                    try {
                        eventsBean.addNotification(EVENT_TIME.parse(timestamp.getText()),
                                firstElement.getText());
                    } catch (ParseException ignore) {
                        eventsBean.addNotification(new Date(), firstElement.getText());
                    }
                }
            }
        } else if (endpoint.toLowerCase().startsWith("work://")) {
            log.debug("Sending Notification to work-list");
            try {
                if (workListConfig.getServerURL() != null && workListConfig.getUsername() != null
                        && workListConfig.getPassword() != null) {
                    WorkListServiceStub stub = new WorkListServiceStub(configContext,
                            workListConfig.getServerURL() + "WorkListService");
                    ServiceClient client = stub._getServiceClient();
                    CarbonUtils.setBasicAccessSecurityHeaders(workListConfig.getUsername(),
                            workListConfig.getPassword(), client);
                    client.getOptions().setManageSession(true);
                    stub.addTask(endpoint.substring(7),
                            event.getMessage().getFirstElement().getText(), 5);
                } else {
                    log.warn("Unable to generate notification. The work list config has not been " +
                            "setup.");
                }
            } catch (RemoteException e) {
                log.error("Failed Sending Notification to work-list", e);
            }
        } else {
            log.debug("Sending Notification to: " + endpoint);
            publishEvent(event, subscription, endpoint, doRest);
        }
    }

    public void init(ConfigurationContext configContext) {
        this.configContext = configContext;
    }

    private OMElement buildPayload(OMFactory factory, Message event, boolean isEmail, String eventType) {
        OMElement messageElement = event.getMessage();
        if (!isEmail) {
            return event.getMessage();
        }
        try {
            // E-mail scenario
            OMElement payload = factory.createOMElement(BaseConstants.DEFAULT_TEXT_WRAPPER, null);
//            String poweredBy = "This message was automatically generated by WSO2 Carbon.";
//            String signature = "\n--\n" + poweredBy;
            String signature = "";
            String registryURL = EventingDataHolder.getInstance().getDefaultEventingServiceURL();
            if (registryURL != null && registryURL.indexOf(
                    "/services/RegistryEventingService") > -1) {
                registryURL = registryURL.substring(0, registryURL.length() -
                        "/services/RegistryEventingService".length()) +
                        "/carbon";
            }
            if (registryURL != null) {
                signature += " URL: " + registryURL;
            }
            OMElement firstElement = messageElement.getFirstElement();
            String namespaceURI = firstElement.getNamespace().getNamespaceURI();
            OMElement timestamp = messageElement.getFirstChildWithName(new QName(namespaceURI,
                    "Timestamp"));
            String time = null;
            if (timestamp != null) {
                time = timestamp.getText();
            }
            OMElement details = messageElement.getFirstChildWithName(new QName(namespaceURI,
                    "Details"));
            String serverName = null;
            if (details != null) {
                serverName = details.getFirstChildWithName(new QName(namespaceURI, "Server"))
                        .getFirstChildWithName(new QName(namespaceURI, "Product")).
                                getFirstChildWithName(new QName(namespaceURI, "Name")).getText();
            }
            if (time != null && serverName != null) {
                signature = "\n\nThis event was generated" + (eventType.equals("DigestEvent") ? "" : " by " + serverName) +
                        " at " + time + ". " + signature;
            }
            payload.setText(firstElement.getText() + signature);
            return payload;
        } catch (Exception e) {
            log.error("Unable to Build Payload for " + messageElement.getText(), e);
            return null;
        }
    }

    private OMElement buildTopic(OMFactory factory, String topic) {
        OMNamespace topicNs = factory.createOMNamespace(
                EventingConstants.NOTIFICATION_NS_URI,
                EventingConstants.NOTIFICATION_NS_PREFIX);
        OMElement topicEle = factory.createOMElement(RegistryEventingConstants.WSE_EN_TOPIC,
                topicNs);
        topicEle.setText(topic);
        return topicEle;
    }

    @SuppressWarnings("unchecked")
    public void publishEvent(Message message, Subscription subscription, String endpoint,
                             boolean doRest) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        String topicName = subscription.getTopicName();
        OMElement topicEle = buildTopic(factory, topicName);
        boolean isEmail = false;
        if (endpoint == null) {
            endpoint = this.getEndpoint();
        } else {
            isEmail = doRest && endpoint.startsWith("mailto:");
        }
        String[] temp = subscription.getTopicName().split(RegistryEvent.TOPIC_SEPARATOR);
        String eventName = "";
        if (temp[0].equals("")) {
            eventName = temp[3];
        } else {
            eventName = temp[2];
        }
        String emailMessage = null;
        MimeEmailMessageHandler handler =  new MimeEmailMessageHandler();
        if (message instanceof DispatchEvent && isEmail) {
            emailMessage = handler.getEmailMessage(message);
        }
        String topicText = topicEle.getText();
        if (emailMessage != null && !emailMessage.equals(message.getMessage().toString())){

            String[] tTemp = topicText.split(RegistryEvent.TOPIC_SEPARATOR);
            String event = "";
            if (tTemp[0].equals("")) {
                event = tTemp[3];
            } else {
                event = tTemp[2];
            }
            String path = "";
            if (topicText.endsWith(RegistryEvent.TOPIC_SEPARATOR) || topicText.endsWith("*") || topicText.endsWith("#")) {
                path = topicText.substring(RegistryEventingConstants.TOPIC_PREFIX.length()+event.length()+1, topicText.lastIndexOf(RegistryEvent.TOPIC_SEPARATOR));
            } else {
                path = topicText.substring(RegistryEventingConstants.TOPIC_PREFIX.length()+event.length()+1);
            }
            String mailSubject = handler.getEmailSubject(message, path, event);
            try{
                if (configContext == null) {
                    MessageContext messageContext = MessageContext.getCurrentMessageContext();
                    if (messageContext != null) {
                        configContext = messageContext.getConfigurationContext();
                    }
                }
                handler.sendMimeMessage(configContext, emailMessage, mailSubject,endpoint.replace("mailto:","") );
            } catch (RegistryException e) {
                log.error("Unable to send email notifications", e);
            }

        } else {
            OMElement payload = buildPayload(factory, message, isEmail, eventName);

            if (endpoint != null) {
                try {
                    if (doRest) {
                        if (configContext == null) {
                            MessageContext messageContext = MessageContext.getCurrentMessageContext();
                            if (messageContext != null) {
                                configContext = messageContext.getConfigurationContext();
                            }
                        }

                        ServiceClient serviceClient;
                        if (configContext != null) {
                            serviceClient = new ServiceClient(configContext, null);
                        } else {
                            serviceClient = new ServiceClient();
                        }
                        Options options = new Options();
                        serviceClient.engageModule("addressing");
                        options.setTo(new EndpointReference(endpoint));
                        options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
                        if (endpoint.toLowerCase().startsWith("mailto:")) {
                            Map headerMap = new HashMap();
                            if (topicText != null && topicText.lastIndexOf("/") > 0) {
                                String[] tTemp = topicText.split(RegistryEvent.TOPIC_SEPARATOR);
                                String event = "";
                                if (tTemp[0].equals("")) {
                                    event = tTemp[3];
                                } else {
                                    event = tTemp[2];
                                }
                                String path = "";
                                if (topicText.endsWith(RegistryEvent.TOPIC_SEPARATOR) || topicText.endsWith("*") || topicText.endsWith("#")) {
                                    path = topicText
                                            .substring(RegistryEventingConstants.TOPIC_PREFIX.length()+event.length()+1,
                                                       topicText.lastIndexOf(RegistryEvent.TOPIC_SEPARATOR));
                                } else {
                                    path = topicText
                                            .substring(RegistryEventingConstants.TOPIC_PREFIX.length()+event.length()+1);
                                }

                                String mailHeader = message.getProperty(MailConstants.MAIL_HEADER_SUBJECT);
                                if (mailHeader != null) {
                                    headerMap.put(MailConstants.MAIL_HEADER_SUBJECT, mailHeader);
                                } else if (path == null || path.length() == 0) {
                                    headerMap.put(MailConstants.MAIL_HEADER_SUBJECT,
                                                  "[" + event + "]");
                                } else {
                                    headerMap.put(MailConstants.MAIL_HEADER_SUBJECT,
                                                  "[" + event + "] at path: " + path);
                                }
                                headerMap.put(MailConstants.TRANSPORT_MAIL_FORMAT, "text/html");
                                headerMap.put(MailConstants.TRANSPORT_MAIL_CONTENT_TYPE, "text/html");

                            }
                            options.setProperty(MessageContext.TRANSPORT_HEADERS, headerMap);
                            //options.setProperty(MailConstants.TRANSPORT_MAIL_FORMAT,
                            //                    MailConstants.TRANSPORT_FORMAT_TEXT);
                            options.setProperty(MailConstants.TRANSPORT_MAIL_FORMAT, "text/html");
                            options.setProperty(MailConstants.TRANSPORT_MAIL_CONTENT_TYPE, "text/html");
                        }
                        options.setProperty(MessageContext.CLIENT_API_NON_BLOCKING, Boolean.TRUE);
                        options.setAction(RegistryEventingConstants.WSE_PUBLISH);
                        serviceClient.setOptions(options);
                        if(log.isDebugEnabled()){
                            log.debug("\nThe payload contains in the publishing  event is : \n" + payload.toString());
                        }
                        serviceClient.fireAndForget(payload);
                    } else {
                        if(log.isDebugEnabled()){
                            log.debug("\nThe payload contains in the publishing  event is : \n" + payload.toString());
                        }
                        super.sendNotification(topicEle, payload, endpoint);
                    }
                } catch (AxisFault e) {
                    log.error("Unable to send message", e);
                }
            }
        }
    }

    public String getEndpoint() {
        return EventingDataHolder.getInstance().getDefaultEventingServiceURL();
    }

    private void addToEmailDigestQueue(Message event, String topic, String endpoint, String digestType,
                                       String topicName){
        OMElement payload = event.getMessage();
        if (payload != null && payload.getFirstElement() != null) {
            String[] temp = topicName.split(RegistryEvent.TOPIC_SEPARATOR);
            String eventName = "";
            if (temp[0].equals("")) {
                eventName = temp[3];
            } else {
                eventName = temp[2];
            }

            String path = topic.substring(RegistryEventingConstants.TOPIC_PREFIX.length() + eventName.length(),
                                          topic.lastIndexOf("/"));

            String time = ((OMElement) payload.getFirstElement().getNextOMSibling()).getText();
            String message = time + ": [" + eventName + "] at path " + path + ":\n    " +
                             payload.getFirstElement().getText();
            getDigestQueue(digestType).add(
                    new DigestEntry(message, endpoint, System.currentTimeMillis()));
        }
    }
}
