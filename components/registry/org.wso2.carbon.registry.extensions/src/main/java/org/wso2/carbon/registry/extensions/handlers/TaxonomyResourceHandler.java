/*
 * Copyright (c) 20016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.extensions.handlers;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.w3c.dom.Document;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.wso2.carbon.registry.extensions.services.TaxonomyStorageService;
import org.wso2.carbon.registry.extensions.services.Utils;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class TaxonomyResourceHandler extends Handler {
    private static final Log log = LogFactory.getLog(TaxonomyResourceHandler.class);

    public void put(RequestContext requestContext) throws RegistryException {
        try {
            if (requestContext.getResourcePath().toString().equals("/_system/governance/taxonomy/taxonomy.xml")) {

                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                        .parse(requestContext.getResource().getContentStream());
                TaxonomyStorageService ins = new TaxonomyStorageService();
                ins.addParseDocument(doc);
                Utils.setTaxonomyService(ins);
            }
        } catch (SAXException e) {
            log.info(e.getMessage());
        } catch (IOException e) {
            log.info(e.getMessage());
        } catch (ParserConfigurationException e) {
            log.info(e.getMessage());
        }

    }

    @Override public String rename(RequestContext requestContext) throws RegistryException {
        log.info("------------Renamed---------------- Not Implemented yet");
        return super.rename(requestContext);
    }

    @Override public String copy(RequestContext requestContext) throws RegistryException {
        log.info("------------Copied---------------- Not Implemented yet");
        return super.copy(requestContext);
    }

    @Override public String move(RequestContext requestContext) throws RegistryException {
        log.info("------------Moved---------------- Not Implemented yet");
        return super.move(requestContext);
    }
}
