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
import org.wso2.carbon.registry.common.AttributeSearchService;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.metadata.provider.MetadataProvider;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Util {

 private static ConcurrentHashMap<String,MetadataProvider> providerMap = null;

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
     *
     * @param classificationURI classificationURI of the meta data to which a provider is bound to
     * @return Corresponding Metadata provider that matches with the given classificationURI
     */
    private static final Log log = LogFactory.getLog(Util.class);

    public static MetadataProvider getProvider(String classificationURI) throws RegistryException {
     return getProviderMap().get(classificationURI);
    }

    public static String getNewUUID(){
        return UUID.randomUUID().toString();
    }

    private static File getConfigFile() throws RegistryException {
        String configPath;
        if(Util.getProviderMapFilePath() == null) {
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
                throw new RegistryException(msg);
            }
            return registryXML;
        } else {
            String msg = "Cannot find registry.xml";
//            log.error(msg);
            throw new RegistryException(msg);
        }
    }

    private static Map<String,MetadataProvider> getProviderMap() throws RegistryException {
        if(providerMap != null) {
            return providerMap;
        }

        ConcurrentHashMap<String,MetadataProvider> providerMap = new ConcurrentHashMap<String, MetadataProvider>();
        try {
        FileInputStream fileInputStream = new FileInputStream(getConfigFile());
        StAXOMBuilder builder = new StAXOMBuilder(
                fileInputStream);
        OMElement configElement = builder.getDocumentElement();
        OMElement metadataProviders = configElement.getFirstChildWithName(
                new QName("metadataProviders"));
        Iterator<OMElement> itr = metadataProviders.getChildrenWithLocalName("metadataProvider");
        while (itr.hasNext()){
            OMElement metadataProvider = itr.next();
                String providerClass = metadataProvider.getAttributeValue(new QName("class")).trim();
                String classificationUri  = metadataProvider.getAttributeValue(new QName("mediaType"));
                ClassLoader loader = Thread.currentThread().getContextClassLoader();
                Class<MetadataProvider> classObj = (Class<MetadataProvider>) Class.forName(providerClass, true, loader);

                if(!providerMap.containsKey(classificationUri)) {
                    providerMap.put(classificationUri, classObj.newInstance());
                } else {
//                    log.error("Classification URI already exists")
                }
        }
        } catch (Exception e){
          throw new RegistryException(e.getMessage(),e);
        }

      return Util.providerMap = providerMap;
    }

    public static String getMetadataPath(String uuid,Registry registry)
            throws RegistryException {

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
            throw new RegistryException(msg, e);
        }
    }


    public static void createAssociation(Registry registry,String sourceUUID,String targetUUID,String type) throws RegistryException {
        registry.addAssociation(Util.getMetadataPath(sourceUUID,registry),
                Util.getMetadataPath(targetUUID,registry),
                type);
    }

    public static void setAttributeSearchService(AttributeSearchService attributeSearchService) {
        Util.attributeSearchService = attributeSearchService;
    }


}
