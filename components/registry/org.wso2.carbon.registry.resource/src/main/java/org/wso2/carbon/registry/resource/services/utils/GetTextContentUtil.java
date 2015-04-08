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

package org.wso2.carbon.registry.resource.services.utils;

import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.sql.SQLException;

public class GetTextContentUtil {

    private static final Log log = LogFactory.getLog(GetTextContentUtil.class);
    private static final String URL_SOURCE_REGEX = "(http|https|ftp|file)://[^\\s]*?.*";
    private static final String ENCODING = System.getProperty("carbon.registry.character.encoding");
    private static final String DECODE_TYPE = "text/plain; charset=UTF-8";

    public static String getTextContent(String path, Registry registry) throws Exception {

        try {

            Resource resource = registry.get(path);

            byte[] content = (byte[]) resource.getContent();
            String contentString = "";
            if (content != null) {
                contentString = RegistryUtils.decodeBytes(content);
            }
            resource.discard();

            return contentString;

        } catch (RegistryException e) {

            String msg = "Could not get the content of the resource " +
                    path + ". Caused by: " + ((e.getCause() instanceof SQLException) ?
                    "" : e.getMessage());
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }

    /**
     * return the byte content of the wsdl/wadl/xsd/xml source as a DataHandler
     *
     * @param fetchURL source URL
     * @return handler    byte DataHandler of the wsdl content
     * @throws RegistryException
     */
    public static DataHandler getByteContent(String fetchURL) throws RegistryException {
        StringBuilder sb = new StringBuilder();
        DataHandler handler = null;
        BufferedReader in = null;
        if (fetchURL.matches(URL_SOURCE_REGEX)) {
            try {
                URL sourceURL = new URL(fetchURL);
                in = new BufferedReader(
                        new InputStreamReader(sourceURL.openConnection().getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    sb.append(inputLine);
                }
                DataSource ds = new ByteArrayDataSource(
                        encodeString(sb.toString()), DECODE_TYPE);
                handler = new DataHandler(ds);
            } catch (IOException e) {
                String msg = "Wrong or unavailable source URL " + fetchURL + ".";
                throw new RegistryException(msg, e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        String msg = "Error occurred while trying to close the BufferedReader";
                        log.warn(msg, e);
                    }
                }
            }
        } else {
            String msg = "Invalid source URL format " + fetchURL + ".";
            log.error(msg);
            throw new RegistryException(msg);
        }
        return handler;
    }

    /**
     * return the byte array of converted string content.
     *
     * @param content string content of the resource
     * @return bytes
     * @throws RegistryException
     */
    private static byte[] encodeString(String content) throws RegistryException {
        byte[] bytes;
        try {
            if (ENCODING == null) {
                bytes = content.getBytes();
            } else {
                bytes = content.getBytes(ENCODING);
            }

        } catch (UnsupportedEncodingException e) {
            String msg = ENCODING + " is unsupported encoding type";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
        return bytes;
    }
}
