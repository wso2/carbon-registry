/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.extensions.handlers.utils;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.parsers.StandardParserConfiguration;
import org.apache.xerces.xs.XSModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.wst.wsdl.validation.internal.Constants;
import org.eclipse.wst.wsdl.validation.internal.ControllerValidationInfo;
import org.eclipse.wst.wsdl.validation.internal.IValidationMessage;
import org.eclipse.wst.wsdl.validation.internal.ValidationInfoImpl;
import org.eclipse.wst.wsdl.validation.internal.exception.ValidateWSDLException;
import org.eclipse.wst.wsdl.validation.internal.resolver.URIResolver;
import org.eclipse.wst.wsdl.validation.internal.util.MessageGenerator;
import org.eclipse.wst.wsdl.validation.internal.wsdl11.*;
import org.eclipse.wst.wsdl.validation.internal.wsdl11.http.HTTPValidator;
import org.eclipse.wst.wsdl.validation.internal.wsdl11.mime.MIMEValidator;
import org.eclipse.wst.wsdl.validation.internal.wsdl11.soap.SOAPValidator;
import org.eclipse.wst.wsdl.validation.internal.xml.LineNumberDOMParser;
import org.eclipse.wst.wsi.internal.WSITestToolsPlugin;
import org.eclipse.wst.wsi.internal.WSITestToolsProperties;
import org.eclipse.wst.wsi.internal.core.WSIException;
import org.eclipse.wst.wsi.internal.core.profile.validator.BaseValidator;
import org.eclipse.wst.wsi.internal.core.profile.validator.impl.ProfileValidatorFactoryImpl;
import org.eclipse.wst.wsi.internal.core.util.ArtifactType;
import org.w3c.dom.Document;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.extensions.utils.WSDLValidationInfo;
import org.xml.sax.SAXException;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.util.*;

public class WSDLUtils {

    public static final String WSDL_VALIDATION_MESSAGE = "WSDL Validation Message ";
    public static final String WSI_VALIDATION_MESSAGE = "WSI Validation Message ";
    public static final String WSDL_STATUS = "WSDL Validation";
    public static final String WSI_STATUS = "WSI Validation";
    public static final String VALID = "Valid";
    public static final String INVALID = "Invalid";

    private static Log log = LogFactory.getLog(WSDLUtils.class);

    public static String[] validateForWSI(String uri) throws RegistryException {
        List<String> errorMesage = new LinkedList<String>();
        boolean validationFailed = false;
        try {
            log.trace("Initializing WSI Validator");
            if (WSITestToolsPlugin.getPlugin() == null) {
                WSITestToolsPlugin.getInstance();
                WSITestToolsProperties.setEclipseContext(false);
                BaseValidator[] validators = new BaseValidator[4];

                ProfileValidatorFactoryImpl factory = new ProfileValidatorFactoryImpl();
                validators[0] = factory.newWSDLValidator();
                validators[1] = factory.newUDDIValidator();
                validators[2] = factory.newEnvelopeValidator();
                validators[3] = factory.newMessageValidator();
                WSITestToolsPlugin.getPlugin().setBaseValidators(validators);
            }
            org.eclipse.wst.wsi.internal.validate.wsdl.WSDLValidator validator =
                    new org.eclipse.wst.wsi.internal.validate.wsdl.WSDLValidator();
            ResourceBundle rb = ResourceBundle.getBundle("validatewsdl");
            MessageGenerator messagegenerator = new MessageGenerator(rb);

            ControllerValidationInfo info = new ValidationInfoImpl(uri, messagegenerator);
            URIResolver uriResolver = new URIResolver();
            ((ValidationInfoImpl) info).setURIResolver(uriResolver);
            StandardParserConfiguration configuration = new StandardParserConfiguration();
            DOMParser builder = new LineNumberDOMParser(configuration);
            builder.parse(uri);

            DocumentBuilder db;
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);

            try {
                db = dbf.newDocumentBuilder();
            } catch (Exception e) {
                dbf = DocumentBuilderFactory.newInstance();
                db = dbf.newDocumentBuilder();
            }
            log.trace("Finished initializing WSI Validator");
            Document doc = db.parse(uri);
            log.trace("Finished parsing document");
            log.trace("Invoking WSI Validate method on WSDL");
            validator.validate(doc, info);
            log.trace("Finished invoking WSI Validate method on WSDL");
            if (!validator.isValid()) {
                validationFailed = true;
            }
            IValidationMessage messages[] = info.getValidationMessages();
            if (messages != null && messages.length > 0) {
                log.trace("Finished retrieving " + messages.length + " validation messages");
                for (int i = 0; i < messages.length; i++) {
                    IValidationMessage message = messages[i];
                    errorMesage.add(message.getMessage());
                }
                log.trace("Finished building error message list");
            }
        } catch (WSIException e) {
            String message = e.getMessage();
            return new String[]{"Exception occurred while building WS-I Validator" +
                    (message != null ? ": " + message : "")};
        } catch (ValidateWSDLException e) {
            String message = e.getMessage();
            return new String[]{"Exception occurred while performing WS-I Validation" +
                    (message != null ? ": " + message : "")};
        } catch (SAXException e) {
            String message = e.getMessage();
            return new String[]{"Exception occurred while parsing WSDL document" +
                    (message != null ? ": " + message : "")};
        } catch (IOException e) {
            String message = e.getMessage();
            return new String[]{"Exception occurred while reading WSDL document from given " +
                    "location" + (message != null ? ": " + message : "")};
        } catch (ParserConfigurationException e) {
            String message = e.getMessage();
            return new String[]{"Exception occurred while building parser to parse thr WSDL" +
                    (message != null ? ": " + message : "")};
        } catch (RuntimeException e) {
            // The Eclipse WSDL Validator does not handle runtime exceptions in some paths, and
            // simply propagate them to higher levels. This should not cause a failure in the WSDL
            // Media Type Handler.
            String message = e.getMessage();
            return new String[]{"Exception occurred while validating WSDL document" +
                    (message != null ? ": " + message : "")};
        }
        if (errorMesage.size() == 0 && validationFailed) {
            return new String[]{"WS-I Validation failed"};
        }
        return errorMesage.toArray(new String[errorMesage.size()]);
    }

    public static WSDLValidationInfo validateWSI(RequestContext requestContext) throws RegistryException {
        /*Resource resource = requestContext.getResource();
        Object resourceContent = resource.getContent();*/
        try {
            /*if (resourceContent instanceof byte[]) {

                InputStream in = new ByteArrayInputStream((byte[]) resourceContent);

                File tempFile = File.createTempFile("reg", ".bin");
                tempFile.deleteOnExit();

                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tempFile));
                byte[] contentChunk = new byte[1024];
                int byteCount;
                while ((byteCount = in.read(contentChunk)) != -1) {
                    out.write(contentChunk, 0, byteCount);
                }
                out.flush();

                String uri = tempFile.toURI().toString();
                return validateWSI(uri);


            } else*/ if (requestContext.getSourceURL() != null) {
                return validateWSI(requestContext.getSourceURL());
            }
            return null;
        } catch (Exception e) {
            throw new RegistryException(e.getMessage(), e);
        }
    }

    private static WSDLValidationInfo validateWSI(String uri) throws Exception {
        String message[] = validateForWSI(uri);
        WSDLValidationInfo wsdlValidationInfo = new WSDLValidationInfo();
        if (message.length > 0) {
            wsdlValidationInfo.setStatus(INVALID);
        } else {
            wsdlValidationInfo.setStatus(VALID);
        }
        for (int i = 0; i < message.length; i++) {
            wsdlValidationInfo.addValidationMessage(message[i]);
        }
        return wsdlValidationInfo;
    }

    public static WSDLValidationInfo validateWSDL(RequestContext requestContext) throws RegistryException {
        /*Resource resource = requestContext.getResource();
        Object resourceContent = resource.getContent(); */
        try {
            /*if (resourceContent instanceof byte[]) {

                InputStream in = new ByteArrayInputStream((byte[]) resourceContent);

                File tempFile = File.createTempFile("reg", ".bin");
                tempFile.deleteOnExit();

                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tempFile));
                byte[] contentChunk = new byte[1024];
                int byteCount;
                while ((byteCount = in.read(contentChunk)) != -1) {
                    out.write(contentChunk, 0, byteCount);
                }
                out.flush();

                String uri = tempFile.toURI().toString();
                return validaWSDLFromURI(uri);


            } else*/ if (requestContext.getSourceURL() != null) {
                return validaWSDLFromURI(requestContext.getSourceURL());
            }
            return null;
        } catch (Exception e) {
            throw new RegistryException(e.getMessage());
        }
    }

    public static WSDLValidationInfo[] validate(RequestContext requestContext,
                                              Definition wsdlDefinition) throws Exception {
        Resource resource = requestContext.getResource();
        Object resourceContent = resource.getContent();
        String sourceURL = requestContext.getSourceURL();
        InputStream inputStream = null;
        try {
            if (resourceContent instanceof byte[]) {

                inputStream = new ByteArrayInputStream((byte[]) resourceContent);
            } else {
                if (sourceURL != null && sourceURL.toLowerCase().startsWith("file:")) {
                    String msg = "The source URL must not be file in the server's local file system";
                    throw new RegistryException(msg);
                }
                inputStream = new URL(sourceURL).openStream();
            }
            ResourceBundle rb = ResourceBundle.getBundle("validatewsdl");
            MessageGenerator messagegenerator = new MessageGenerator(rb);
            DocumentBuilder db;
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);

            try {
                db = dbf.newDocumentBuilder();
            } catch (Exception e) {
                dbf = DocumentBuilderFactory.newInstance();
                db = dbf.newDocumentBuilder();
            }

            Document doc = db.parse(inputStream);
            ControllerValidationInfo validateInfo = new ValidationInfoImpl(sourceURL, messagegenerator);

            URIResolver uriResolver = new URIResolver();
            ((ValidationInfoImpl) validateInfo).setURIResolver(uriResolver);
            /*java.util.Hashtable attributes = new java.util.Hashtable();
            ((ValidationInfoImpl) validateInfo).setAttributes(attributes);*/

            WSDL11ValidationInfoImpl info = new WSDL11ValidationInfoImpl(validateInfo);
            info.setElementLocations(new java.util.Hashtable());
            WSDL11BasicValidator validator = new WSDL11BasicValidator();
            registerExtensionValidators(validator.getClass().getClassLoader());
            IValidationMessage[] messages;

            ExtendedWSDL11ValidatorController wsdl11ValidatorController =
                    new ExtendedWSDL11ValidatorController();
            WSDLDocument[] wsdlDocs = wsdl11ValidatorController.readWSDLDocument(doc,
                    validateInfo.getFileURI(),
                    messagegenerator,
                    info);
            WSDLDocument document = wsdlDocs[0];
            List schema = document.getSchemas();
            Iterator xsdIter = schema.iterator();
            while (xsdIter.hasNext()) {
                info.addSchema((XSModel) xsdIter.next());
            }
            // Set the element locations table.
            info.setElementLocations(document.getElementLocations());
            validator.validate(wsdlDefinition, new ArrayList(), info);
            messages = validateInfo.getValidationMessages();

            WSDLValidationInfo wsdlValidationInfo = new WSDLValidationInfo();
            if (messages.length > 0) {
                wsdlValidationInfo.setStatus(INVALID);
            } else {
                wsdlValidationInfo.setStatus(VALID);
            }
            for (IValidationMessage message : messages) {
                String messageString =
                        "[" + message.getLine() + "][" + message.getColumn() + "]"
                                + message.getMessage();
                wsdlValidationInfo.addValidationMessage(messageString);
            }
            if (WSITestToolsPlugin.getPlugin() == null) {
                WSITestToolsPlugin.getInstance();
                WSITestToolsProperties.setEclipseContext(false);
                BaseValidator[] validators = new BaseValidator[4];

                ProfileValidatorFactoryImpl factory = new ProfileValidatorFactoryImpl();
                validators[0] = factory.newWSDLValidator();
                validators[1] = factory.newUDDIValidator();
                validators[2] = factory.newEnvelopeValidator();
                validators[3] = factory.newMessageValidator();
                WSITestToolsPlugin.getPlugin().setBaseValidators(validators);
            }
            org.eclipse.wst.wsi.internal.validate.wsdl.WSDLValidator wsiValidator =
                    new org.eclipse.wst.wsi.internal.validate.wsdl.WSDLValidator();

            ControllerValidationInfo validationInfo = new ValidationInfoImpl(sourceURL, messagegenerator);
            ((ValidationInfoImpl) validationInfo).setURIResolver(uriResolver);

            WSDLValidationInfo wsiValidationInfo = new WSDLValidationInfo();

            wsiValidator.validate(doc, validationInfo);
            messages = validationInfo.getValidationMessages();
            if (messages.length > 0) {
                wsiValidationInfo.setStatus(INVALID);
            } else {
                wsiValidationInfo.setStatus(VALID);
            }
            for (int i = 0; i < messages.length; i++) {
                IValidationMessage message = messages[i];
                wsiValidationInfo.addValidationMessage(message.getMessage());
            }

            return new WSDLValidationInfo[]{wsdlValidationInfo, wsiValidationInfo};

        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    private static void registerExtensionValidators(ClassLoader classLoader) {
        WSDL11ValidatorDelegate delegate1 = new ClassloaderWSDL11ValidatorDelegate(
                WSDL11BasicValidator.class.getName(), classLoader);
        ValidatorRegistry.getInstance().registerValidator(com.ibm.wsdl.Constants.NS_URI_WSDL,
                delegate1);
        delegate1 = new ClassloaderWSDL11ValidatorDelegate(HTTPValidator.class.getName(),
                classLoader);
        ValidatorRegistry.getInstance().registerValidator(Constants.NS_HTTP, delegate1);
        delegate1 = new ClassloaderWSDL11ValidatorDelegate(SOAPValidator.class.getName(),
                classLoader);
        ValidatorRegistry.getInstance().registerValidator(Constants.NS_SOAP11, delegate1);
        delegate1 = new ClassloaderWSDL11ValidatorDelegate(MIMEValidator.class.getName(),
                classLoader);
        ValidatorRegistry.getInstance().registerValidator(Constants.NS_MIME, delegate1);
    }

    private static WSDLValidationInfo validaWSDLFromURI(String uri) throws Exception {
        InputStream inputStream = null;
        try {
            inputStream = new URL(uri).openStream();

            ResourceBundle rb = ResourceBundle.getBundle("validatewsdl");
            MessageGenerator messagegenerator = new MessageGenerator(rb);
            DocumentBuilder db;
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);

            try {
                db = dbf.newDocumentBuilder();
            } catch (Exception e) {
                dbf = DocumentBuilderFactory.newInstance();
                db = dbf.newDocumentBuilder();
            }

            Document doc = db.parse(inputStream);
            WSDLReader reader = new ExWSDLReaderImpl(
                    (com.ibm.wsdl.xml.WSDLReaderImpl) WSDLFactory.newInstance().newWSDLReader());
            reader.setFeature("javax.wsdl.importDocuments", true);
            reader.setFeature("javax.wsdl.verbose", log.isDebugEnabled());
            Definition wsdlDefinition = reader.readWSDL(uri);
            ControllerValidationInfo validateInfo = new ValidationInfoImpl(uri, messagegenerator);

            URIResolver uriResolver = new URIResolver();
            ((ValidationInfoImpl) validateInfo).setURIResolver(uriResolver);
            /*java.util.Hashtable attributes = new java.util.Hashtable();
            ((ValidationInfoImpl) validateInfo).setAttributes(attributes);*/

            WSDL11ValidationInfoImpl info = new WSDL11ValidationInfoImpl(validateInfo);
            info.setElementLocations(new java.util.Hashtable());
            WSDL11BasicValidator validator = new WSDL11BasicValidator();
            registerExtensionValidators(validator.getClass().getClassLoader());
            /*validator.setResourceBundle(rb);*/
            IValidationMessage[] messages;

            ExtendedWSDL11ValidatorController wsdl11ValidatorController =
                    new ExtendedWSDL11ValidatorController();
            WSDLDocument[] wsdlDocs = wsdl11ValidatorController.readWSDLDocument(doc,
                    validateInfo.getFileURI(),
                    messagegenerator,
                    info);
            WSDLDocument document = wsdlDocs[0];
            List schema = document.getSchemas();
            Iterator xsdIter = schema.iterator();
            while (xsdIter.hasNext()) {
                info.addSchema((XSModel) xsdIter.next());
            }
            // Set the element locations table.
            info.setElementLocations(document.getElementLocations());
            validator.validate(wsdlDefinition, new ArrayList(), info);
            messages = validateInfo.getValidationMessages();

            WSDLValidationInfo wsdlValidationInfo = new WSDLValidationInfo();
            if (messages.length > 0) {
                wsdlValidationInfo.setStatus(INVALID);
            } else {
                wsdlValidationInfo.setStatus(VALID);
            }
            for (IValidationMessage message : messages) {
                String messageString =
                        "[" + message.getLine() + "][" + message.getColumn() + "]"
                                + message.getMessage();
                wsdlValidationInfo.addValidationMessage(messageString);
            }
            return wsdlValidationInfo;
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    private static class ExtendedWSDL11ValidatorController extends WSDL11ValidatorController {

        @Override
        protected WSDLDocument[] readWSDLDocument(Document domModel, String file,
                                                  MessageGenerator messagegenerator,
                                                  IWSDL11ValidationInfo wsdlvalinfo)
                throws ValidateWSDLException {
            return super.readWSDLDocument(domModel, file, messagegenerator, wsdlvalinfo);
        }
    }

}
