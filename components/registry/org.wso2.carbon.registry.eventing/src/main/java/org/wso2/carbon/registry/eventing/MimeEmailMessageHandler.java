/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.transport.mail.MailConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.core.Message;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.eventing.events.DispatchEvent;
import org.wso2.carbon.registry.eventing.internal.EventingDataHolder;
import org.wso2.carbon.registry.eventing.template.NotificationTemplate;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MimeEmailMessageHandler {

    public static final int MIN_THREAD = 8;
    public static final int MAX_THREAD = 100;
    public static final String MIN_THREAD_NAME = "minThread";
    public static final String MAX_THREAD_NAME = "maxThread";
    public static final String DEFAULT_KEEP_ALIVE_TIME_NAME = "defaultKeepAliveTime";
    public static final long DEFAULT_KEEP_ALIVE_TIME = 20;
    private static final Log log = LogFactory.getLog(MimeEmailMessageHandler.class);
    private static Session session;
    private static ThreadPoolExecutor threadPoolExecutor;
    private InternetAddress smtpFromAddress = null;

    /**
     *
     * @param configContext ConfigurationContext
     * @param message Mail body
     * @param subject Mail Subject
     * @param toAddress email to address
     * @throws RegistryException Registry exception
     */
    public void sendMimeMessage(ConfigurationContext configContext, String message, String subject, String toAddress) throws  RegistryException {
        Properties props = new Properties();
        if (configContext != null && configContext.getAxisConfiguration().getTransportOut("mailto") != null) {
            List<Parameter> params = configContext.getAxisConfiguration().getTransportOut("mailto").getParameters();
            for (Parameter parm: params) {
                props.put(parm.getName(), parm.getValue());
            }
        }
        if (threadPoolExecutor == null) {
            threadPoolExecutor = new ThreadPoolExecutor(MIN_THREAD, MAX_THREAD, DEFAULT_KEEP_ALIVE_TIME, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1000));
        }
        String smtpFrom = props.getProperty(MailConstants.MAIL_SMTP_FROM);

        try {
            smtpFromAddress = new InternetAddress(smtpFrom);
        } catch (AddressException e) {
            log.error("Error in retrieving smtp address");
            throw new RegistryException("Error in transforming smtp address");
        }
        final String smtpUsername = props.getProperty(MailConstants.MAIL_SMTP_USERNAME);
        final String smtpPassword = props.getProperty(MailConstants.MAIL_SMTP_PASSWORD);
        if (smtpUsername != null && smtpPassword != null) {
            MimeEmailMessageHandler.session = Session.getInstance(props, new Authenticator() {
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(smtpUsername, smtpPassword);
                }
            });
        } else {
            MimeEmailMessageHandler.session = Session.getInstance(props);
        }
        threadPoolExecutor.submit(new EmailSender(toAddress, subject, message.toString(), "text/html"));
    }

    /**
     *
     * @param message Message Object
     * @param path  Registry path
     * @param event Event name
     * @return  Mail Subject
     */
    public String getEmailSubject(Message message, String path,String event){
        String mailHeader = message.getProperty(MailConstants.MAIL_HEADER_SUBJECT);
        if (mailHeader == null && (path == null || path.length() == 0)) {
            mailHeader =  "[" + event + "]";
        } else if (mailHeader == null) {
            mailHeader ="[" + event + "] at path: " + path;
        }
        return mailHeader;
    }

    public String getEmailMessage(Message message){
        String emailMessage = null;
        DispatchEvent event = (DispatchEvent) message;
        String eventType = event.getEvent().getClass().getSimpleName();
        String resourcePath = event.getEvent().getOperationDetails().getPath();
        if (EventingDataHolder.getInstance().getRegistryService() != null) {
            try {
                UserRegistry registry = EventingDataHolder.getInstance().getRegistryService().getConfigSystemRegistry();
                String mesageContent = event.getEvent().getMessage().toString();
                String className = EventingDataHolder.getInstance().getNotificationConfig().getConfigurationClass();
                Class clazz = Class.forName(className);
                Object instance = clazz.newInstance();
                if (instance instanceof NotificationTemplate){
                    NotificationTemplate template = (NotificationTemplate) instance;
                    emailMessage = template.populateEmailMessage(registry,resourcePath,mesageContent,eventType);
                }
            } catch (InstantiationException e) {
                log.warn("Error while instantiating template");
            } catch (IllegalAccessException e) {
                log.warn("Error while instantiating template");
            } catch (ClassNotFoundException e) {
                log.warn("Error while instantiating template");
            } catch (RegistryException e) {
                log.warn("Error while instantiating template");
            }

        }
        return emailMessage;
    }

    class EmailSender implements Runnable {
        String to;
        String subject;
        String body;
        String type;

        EmailSender(String to, String subject, String body, String type) {
            this.to = to;
            this.subject = subject;
            this.body = body;
            this.type = type;
        }

        /**
         * Sending emails to the corresponding Email IDs'.
         */

        @Override
        public void run() {

            if (log.isDebugEnabled()) {
                log.debug("Format of the email:" + " " + to + "->" + type);
            }
            //Creating MIME object using initiated session.

            MimeMessage message = new MimeMessage(session);

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(body);
            String finalString = stringBuilder.toString();
            //Setting up the Email attributes and Email payload.

            try {
                message.setFrom(smtpFromAddress);
                message.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
                message.setSubject(subject);
                message.setSentDate(new Date());
                message.setContent(finalString, type);

                if (log.isDebugEnabled()) {
                    log.debug("Meta data of the email configured successfully");
                }
                Transport.send(message);

                if (log.isDebugEnabled()) {
                    log.debug("Mail sent to the EmailID" + " " + to + " " + "Successfully");
                }
            } catch (MessagingException e) {
                log.error("Error in sending the Email : " + smtpFromAddress.toString() + "::" +e.getMessage(), e);
            }
        }

    }
}
