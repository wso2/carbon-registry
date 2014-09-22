/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.metadata;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.api.Association;
import org.wso2.carbon.registry.common.AttributeSearchService;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.metadata.exception.MetadataException;
import org.wso2.carbon.registry.metadata.provider.BaseProvider;
import org.wso2.carbon.registry.metadata.provider.BaseProvider;
import org.wso2.carbon.registry.metadata.provider.version.VersionBaseProvider;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Util {

    private static ConcurrentHashMap<String, BaseProvider> baseProviderMap = null;

    private static ConcurrentHashMap<String, VersionBaseProvider> versionBaseProviderMap = null;

    private static String providerMapFilePath = null;

    public static AttributeSearchService getAttributeSearchService() {
        return attributeSearchService;
    }

    private static AttributeSearchService attributeSearchService;

    public static String getProviderMapFilePath() {
        return providerMapFilePath;
    }

    public static void setProviderMapFilePath(String providerMapFile) {
        Util.providerMapFilePath = providerMapFile;
    }

    /**
     * @param classificationURI classificationURI of the meta data to which a provider is bound to
     * @return Corresponding Metadata provider that matches with the given classificationURI
     */
    private static final Log log = LogFactory.getLog(Util.class);

    public static BaseProvider getBaseProvider(String mt) throws MetadataException {
        return getBaseProviderMap().get(mt);
    }

    public static VersionBaseProvider getVersionBaseProvider(String mt) throws MetadataException {
        return getVersionBaseProviderMap().get(mt);
    }

    public static String getNewUUID() {
        return UUID.randomUUID().toString();
    }

    private static File getConfigFile() throws MetadataException {
        String configPath;
        if (Util.getProviderMapFilePath() == null) {
            configPath = new StringBuilder(System.getProperty("carbon.home")).append(File.separator).
                    append("repository").append(File.separator).
                    append("conf").append(File.separator).append("registry.xml").toString();
        } else {
            configPath = Util.getProviderMapFilePath();
        }
        if (configPath != null) {
            File registryXML = new File(configPath);
            if (!registryXML.exists()) {
                String msg = "Registry configuration file (registry.xml) file does " +
                        "not exist in the path " + configPath;
//                log.error(msg);
                throw new MetadataException(msg);
            }
            return registryXML;
        } else {
            String msg = "Cannot find registry.xml";
//            log.error(msg);
            throw new MetadataException(msg);
        }
    }

    private static Map<String, BaseProvider> getBaseProviderMap() throws MetadataException {
        if (baseProviderMap != null) {
            return baseProviderMap;
        }

        ConcurrentHashMap<String, BaseProvider> providerMap = new ConcurrentHashMap<String, BaseProvider>();
        try {
            FileInputStream fileInputStream = new FileInputStream(getConfigFile());
            StAXOMBuilder builder = new StAXOMBuilder(
                    fileInputStream);
            OMElement configElement = builder.getDocumentElement();
            OMElement metadataProviders = configElement.getFirstChildWithName(
                    new QName("metadataProviders")).getFirstChildWithName(new QName("baseProviders"));
            Iterator<OMElement> itr = metadataProviders.getChildrenWithLocalName("provider");
            while (itr.hasNext()) {
                OMElement metadataProvider = itr.next();
                String providerClass = metadataProvider.getAttributeValue(new QName("class")).trim();
                String mediaType = metadataProvider.getAttributeValue(new QName(Constants.ATTRIBUTE_MEDIA_TYPE));
                String versionMediaType = metadataProvider.getAttributeValue(new QName(Constants.ATTRIBUTE_VERSION_MEDIA_TYPE));

                ClassLoader loader = Thread.currentThread().getContextClassLoader();
                Class<BaseProvider> classObj = (Class<BaseProvider>) Class.forName(providerClass, true, loader);

                if (!providerMap.containsKey(mediaType)) {
                    providerMap.put(mediaType, (BaseProvider)classObj.getConstructors()[0].newInstance(mediaType, versionMediaType));
                } else {
//                    log.error("Classification URI already exists")
                }
            }
        } catch (Exception e) {
            throw new MetadataException(e.getMessage(), e);
        }

        return Util.baseProviderMap = providerMap;
    }


    private static Map<String, VersionBaseProvider> getVersionBaseProviderMap() throws MetadataException {
        if (versionBaseProviderMap != null) {
            return versionBaseProviderMap;
        }

        ConcurrentHashMap<String, VersionBaseProvider> providerMap = new ConcurrentHashMap<String, VersionBaseProvider>();
        try {
            FileInputStream fileInputStream = new FileInputStream(getConfigFile());
            StAXOMBuilder builder = new StAXOMBuilder(
                    fileInputStream);
            OMElement configElement = builder.getDocumentElement();
            OMElement metadataProviders = configElement.getFirstChildWithName(
                    new QName("metadataProviders")).getFirstChildWithName(new QName("versionBaseProviders"));
            Iterator<OMElement> itr = metadataProviders.getChildrenWithLocalName("provider");
            while (itr.hasNext()) {
                OMElement metadataProvider = itr.next();
                String providerClass = metadataProvider.getAttributeValue(new QName("class")).trim();
                String mediaType = metadataProvider.getAttributeValue(new QName(Constants.ATTRIBUTE_MEDIA_TYPE));
                ClassLoader loader = Thread.currentThread().getContextClassLoader();
                Class<VersionBaseProvider> classObj = (Class<VersionBaseProvider>) Class.forName(providerClass, true, loader);

                if (!providerMap.containsKey(mediaType)) {
                    providerMap.put(mediaType, (VersionBaseProvider) classObj.getConstructors()[0].newInstance(mediaType));
                } else {
//                    log.error("Classification URI already exists")
                }
            }
        } catch (Exception e) {
            throw new MetadataException(e.getMessage(), e);
        }

        return Util.versionBaseProviderMap = providerMap;
    }

    public static String getMetadataPath(String uuid, Registry registry)
            throws MetadataException {

        try {
            String sql = "SELECT REG_PATH_ID, REG_NAME FROM REG_RESOURCE WHERE REG_UUID = ?";

            String[] result;
            Map<String, String> parameter = new HashMap<String, String>();
            parameter.put("1", uuid);
            parameter.put("query", sql);
            result = registry.executeQuery(null, parameter).getChildren();

            if (result != null && result.length == 1) {
                return result[0];
            }
            return null;
        } catch (RegistryException e) {
            String msg = "Error in getting the path from the registry. Execute query failed with message : "
                    + e.getMessage();
            log.error(msg, e);
            throw new MetadataException(msg, e);
        }
    }


    public static void createAssociation(Registry registry, String sourceUUID, String targetUUID, String type) throws MetadataException {
        try {
            registry.addAssociation(Util.getMetadataPath(sourceUUID, registry),
                    Util.getMetadataPath(targetUUID, registry),
                    type);
        } catch (RegistryException e) {
            throw new MetadataException(e.getMessage(), e);
        }
    }

    public static Association[] getAssociations(Registry registry, String sourceUUID, String type) throws MetadataException {
        Association[] associations = null;
        try {
            associations = registry.getAssociations(Util.getMetadataPath(sourceUUID, registry), type);
        } catch (RegistryException e) {
            throw new MetadataException(e.getMessage(), e);
        }
        return associations;
    }

    public static void setAttributeSearchService(AttributeSearchService attributeSearchService) {
        Util.attributeSearchService = attributeSearchService;
    }

}
