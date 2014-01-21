/*
 *  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.wso2.carbon.registry.extensions.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;

import java.util.Properties;

@SuppressWarnings("unused")
public class EncryptionHandler extends Handler {

    private static final Log log = LogFactory.getLog(EncryptionHandler.class);
    
    private String[] propertyNames = new String[0];
    private boolean encryptContent = false;
    private boolean allProperties = false;

    public void setPropertyNames(String propertyNames) {
        this.allProperties = propertyNames.equals("all");
        if (!this.allProperties) {
            this.propertyNames = propertyNames.split(",");
            for (int i = 0; i < this.propertyNames.length; i++) {
                this.propertyNames[i] = this.propertyNames[i].trim();
            }
        }
    }

    public void setEncryptContent(String encryptContent) {
        this.encryptContent = Boolean.valueOf(encryptContent);
    }

    public Resource get(RequestContext requestContext) throws RegistryException {
        if (CommonUtil.isUpdateLockAvailable()) {
            CommonUtil.acquireUpdateLock();
        } else {
            return null;
        }
        try {
            Resource resource = requestContext.getRegistry().get(
                    requestContext.getResourcePath().getPath());
            if (resource != null) {
                if (allProperties) {
                    Properties props = resource.getProperties();
                    for (Object key : props.keySet()) {
                        String propKey = (String) key;
                        decodeProperty(resource, propKey);
                    }
                } else {
                    for (String propKey : propertyNames) {
                        decodeProperty(resource, propKey);
                    }
                }
                if (encryptContent) {
                    try {
                        Object content = resource.getContent();
                        if (content != null) {
                            if (content instanceof String) {
                                resource.setContent(new String(
                                        CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(
                                                ((String) content))));
                            } else if (content instanceof byte[]) {
                                resource.setContent(
                                        CryptoUtil.getDefaultCryptoUtil()
                                                .decrypt((byte[]) content));
                            } else {
                                log.warn(
                                        "Unable to decrypt unknown content type for resource " +
                                                "path: " + resource.getPath());
                            }
                        }
                    } catch (CryptoException e) {
                        log.error("Unable to decrypt content for resource path: " +
                                resource.getPath(), e);
                    }
                }
            }
            requestContext.setProcessingComplete(true);
            return resource;
        } finally {
            CommonUtil.releaseUpdateLock();
        }
    }

    public void put(RequestContext requestContext) throws RegistryException {
        Resource resource = requestContext.getResource();
        if (resource != null) {
            if (allProperties) {
                Properties props = resource.getProperties();
                for (Object key : props.keySet()) {
                    String propKey = (String) key;
                    encodeProperty(resource, propKey);
                }
            } else {
                for (String propKey : propertyNames) {
                    encodeProperty(resource, propKey);
                }
            }
            if (encryptContent) {
                try {
                    Object content = resource.getContent();
                    if (content != null) {
                        if (content instanceof String) {
                            resource.setContent(
                                    CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode(
                                            ((String) content).getBytes()));    
                        } else if (content instanceof byte[]) {
                            resource.setContent(
                                    CryptoUtil.getDefaultCryptoUtil().encrypt((byte[])content));
                        } else {
                            log.warn("Unable to encrypt unknown content type for resource path: " +
                                    resource.getPath());
                        }
                    }
                } catch (CryptoException e) {
                    log.error("Unable to encrypt content for resource path: " + 
                            resource.getPath(), e);
                }
            }
        }
    }

    private void encodeProperty(Resource resource, String propKey) {
        try {
            if (resource.getProperty(propKey) != null) {
                resource.setProperty(propKey,
                        CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode(
                                resource.getProperty(propKey).getBytes()));

            }
        } catch (CryptoException e) {
            log.error("Unable to encrypt property key: " + propKey + " for resource " +
                    "path: " + resource.getPath(), e);
        }
    }

    private void decodeProperty(Resource resource, String propKey) {
        try {
            if(resource.getProperty(propKey)!=null){
                resource.setProperty(propKey,
                        new String(CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(
                                resource.getProperty(propKey))));
            }
        } catch (CryptoException e) {
            log.error("Unable to decrypt property key: " + propKey + " for resource " +
                    "path: " + resource.getPath(), e);
        }
    }
}
