/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.registry.indexing;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.indexing.indexer.Indexer;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegistryConfigLoader {

    private static volatile RegistryConfigLoader registryConfigLoaderInstance = new RegistryConfigLoader();

    private static Log log = LogFactory.getLog(RegistryConfigLoader.class);

    private long startingDelayInSecs;

    private long indexingFreqInSecs;

    private String lastAccessTimeLocation;

    private Map<String, Indexer> indexerMap = new LinkedHashMap<String, Indexer>();

    private List<Pattern> exclusionList = new ArrayList<Pattern>();

    private long batchSize = 50;

    public int getIndexerPoolSize() {
        return indexerPoolSize;
    }

    private  int indexerPoolSize = 50;

    public long getBatchSize() {
        return batchSize;
    }

    // solr server url for initiate the solr server	
    private String solrServerUrl;

    public String getSolrServerUrl(){
    	return solrServerUrl;
    }

    private RegistryConfigLoader() {
        startingDelayInSecs = IndexingConstants.STARTING_DELAY_IN_SECS_DEFAULT_VALUE;
        indexingFreqInSecs = IndexingConstants.INDEXING_FREQ_IN_SECS_DEFAULT_VALUE;
        lastAccessTimeLocation = IndexingConstants.LAST_ACCESS_TIME_LOCATION;
        try {
            FileInputStream fileInputStream = new FileInputStream(getConfigFile());
            StAXOMBuilder builder = new StAXOMBuilder(
                    CarbonUtils.replaceSystemVariablesInXml(fileInputStream));
            OMElement configElement = builder.getDocumentElement();
            OMElement indexingConfig = configElement.getFirstChildWithName(
                    new QName("indexingConfiguration"));
            if (indexingConfig != null) { //registry.xml need an <indexingConfiguration> </indexingConfiguration> entry to continue
                 loadIndexingConfiguration(indexingConfig);
            }

        } catch (FileNotFoundException e) {
            // This virtually cannot happen as registry.xml is necessary to start up the registry
            log.error("registry.xml has not been found", e);
        } catch (RegistryException e) {
            log.error(e.getMessage(),e);
        } catch (XMLStreamException e) {
            String msg = "error building registry.xml, check for badly formed xml";
            log.error(msg, e);
        } catch (CarbonException e) {
            log.error("An error occurred during system variable replacement", e);
        }

    }

    public static RegistryConfigLoader getInstance(){
    	return registryConfigLoaderInstance;
    }
    
    public long getIndexingFreqInSecs() {
        return indexingFreqInSecs;
    }

    public String getLastAccessTimeLocation() {
        return lastAccessTimeLocation;
    }

    public Map<String, Indexer> getIndexerMap() {
        return indexerMap;
    }

    public Pattern[] getExclusionPatterns() {
        return exclusionList.toArray(new Pattern[exclusionList.size()]);
    }

    public long getStartingDelayInSecs() {
        return startingDelayInSecs;
    }

    // Get registry.xml instance.
    private static File getConfigFile() throws RegistryException {
        String configPath = CarbonUtils.getRegistryXMLPath();
        if (configPath != null) {
            File registryXML = new File(configPath);
            if (!registryXML.exists()) {
                String msg = "Registry configuration file (registry.xml) file does " +
                        "not exist in the path " + configPath;
                log.error(msg);
                throw new RegistryException(msg);
            }
            return registryXML;
        } else {
            String msg = "Cannot find registry.xml";
            log.error(msg);
            throw new RegistryException(msg);
        }
    }

    private void loadIndexingConfiguration(OMElement indexingConfig){
        try {
            startingDelayInSecs = Long.parseLong(indexingConfig.getFirstChildWithName(
                    new QName("startingDelayInSeconds")).getText());
        } catch (OMException e) {
            // we can use default value and continue if no OMElement found in indexingConfig
            log.error("Error occurred when retriving startingDelayInSeconds, hence using the default value", e);
        }

        try {
            indexingFreqInSecs = Long.parseLong(indexingConfig.getFirstChildWithName(
                    new QName("indexingFrequencyInSeconds")).getText());
        } catch (OMException e) {
            // we can use default value and continue if no OMElement found in indexingConfig
            log.error("Error occurred when retriving indexingFrequencyInSeconds, hence using the default value", e);
        }

        try {
            lastAccessTimeLocation = indexingConfig.getFirstChildWithName(
                    new QName("lastAccessTimeLocation")).getText();
        } catch (OMException e) {
            // we can use default value and continue if no OMElement found in indexingConfig
            log.error("Error occurred when retriving lastAccessTimeLocation, hence using the default value", e);
        }
        
        // solr server url for initiate the solr server	
        if(indexingConfig.getFirstChildWithName(new QName("solrServerUrl")) != null){
        	solrServerUrl = indexingConfig.getFirstChildWithName(new QName("solrServerUrl")).getText();
        }

        batchSize =  Long.parseLong(indexingConfig.getFirstChildWithName(new QName("batchSize")).getText());
        indexerPoolSize =  Integer.parseInt(indexingConfig.getFirstChildWithName(new QName("indexerPoolSize")).getText());

        Iterator exclusions = indexingConfig.getFirstChildWithName(new QName("exclusions")).
                getChildrenWithName(new QName("exclusion"));
        while (exclusions.hasNext()) {
            OMElement indexerEl = (OMElement) exclusions.next();
            String pathRegEx =
                    indexerEl.getAttribute(new QName("pathRegEx")).getAttributeValue();
            if (pathRegEx != null) {
                try {
                    exclusionList.add(Pattern.compile(pathRegEx));
                } catch (PatternSyntaxException ignore) {
                    log.error("Error occured when compiling the RegEx pattern: " + pathRegEx, ignore);
                }
            }
        }
        Iterator indexers = indexingConfig.getFirstChildWithName(new QName("indexers")).
                getChildrenWithName(new QName("indexer"));
        String currentProfile = System.getProperty("profile", "default");
        while (indexers.hasNext()) {
            OMElement indexerEl = (OMElement) indexers.next();
            boolean isValidConfigurationForProfile = false;
            String profileStr = indexerEl.getAttributeValue(new QName("profiles"));
            if (profileStr != null) {
                String[] profiles = profileStr.split(",");
                for (String profile : profiles) {
                    if (profile.trim().equals(currentProfile)) {
                        isValidConfigurationForProfile = true;
                    }
                }
            } else {
                //when no profile is defined
                isValidConfigurationForProfile = true;
            }

            if(isValidConfigurationForProfile){
                String clazz = indexerEl.getAttribute(new QName("class")).getAttributeValue();
                try {
                    Object indexerObj = this.getClass().getClassLoader().loadClass(clazz).newInstance();
                    if (!(indexerObj instanceof Indexer)) {
                    	log.error(clazz + " has not implemented Indexer interface");
                    }
                    String mediaPattern = indexerEl.getAttribute(
                            new QName("mediaTypeRegEx")).getAttributeValue();
                    indexerMap.put(mediaPattern, (Indexer) indexerObj);
                } catch (InstantiationException e) {
                    log.error(clazz + " cannot be instantiated.", e);
                } catch (IllegalAccessException e) {
                    log.error(clazz + " constructor cannot be accessed", e);
                } catch (ClassNotFoundException e) {
                    log.error(clazz + " is not found in classpath. Please check whether the class " +
                            "is exported in your OSGI bundle.", e);
                }
            }
        }
    }
}
