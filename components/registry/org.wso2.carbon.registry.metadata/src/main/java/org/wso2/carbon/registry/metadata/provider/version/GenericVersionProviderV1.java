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
package org.wso2.carbon.registry.metadata.provider.version;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.metadata.Constants;
import org.wso2.carbon.registry.metadata.exception.MetadataException;
import org.wso2.carbon.registry.metadata.models.version.GenericVersionV1;
import org.wso2.carbon.registry.metadata.provider.util.Util;
import org.wso2.carbon.registry.metadata.VersionBase;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.List;
import java.util.Map;

public class GenericVersionProviderV1 implements VersionBaseProvider {

    private static final Log log = LogFactory.getLog(GenericVersionProviderV1.class);

    private String mediaType;


    public GenericVersionProviderV1(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getMediaType() {
        return mediaType;
    }

    @Override
    public Resource buildResource(VersionBase metadata, Resource resource) throws MetadataException {
        OMElement root = Util.getContentRoot();
        OMElement attributes = Util.getAttributeRoot();
        OMElement properties = Util.getPropertyRoot();

        createAttributesContent((GenericVersionV1) metadata, attributes);
        createPropertiesContent((GenericVersionV1) metadata, properties);
        root.addChild(properties);
        root.addChild(attributes);
        try {
            String content = root.toStringWithConsume();
            resource.setContent(content);
            resource.setMediaType(metadata.getMediaType());
            resource.setUUID(metadata.getUUID());
        } catch (XMLStreamException e) {
            log.error("Xml stream exception occurred while building resource content " + e.getMessage());
            throw new MetadataException("Xml stream exception occurred while building resource content", e);
        } catch (RegistryException e) {
            throw new MetadataException(e.getMessage(), e);
        }
        return resource;
    }

    @Override
    public Resource updateResource(VersionBase newMetadata, Resource resource) throws MetadataException {
        return buildResource(newMetadata, resource);
    }

    @Override
    public GenericVersionV1 get(Resource resource, Registry registry) throws MetadataException {
        try {
            byte[] contentBytes = (byte[]) resource.getContent();
            OMElement root = Util.buildOMElement(contentBytes);
            Map<String, List<String>> propBag = Util.getPropertyBag(root);
            return getFilledBean(root, propBag, registry);
        } catch (RegistryException e) {
            throw new MetadataException("Error occurred while obtaining resource metadata content uuid = " + resource.getUUID(), e);
        }
    }

    private GenericVersionV1 getFilledBean(OMElement root, Map<String, List<String>> propBag, Registry registry) throws MetadataException {
        Map<String, List<String>> attributeMap;
        OMElement attributes = root.getFirstChildWithName(new QName(Constants.CONTENT_ATTRIBUTE_EL_ROOT_NAME));
        attributeMap = Util.getAttributeMap(attributes);
        String uuid = attributeMap.get(Constants.ATTRIBUTE_UUID).get(0);
        String name = attributeMap.get((Constants.ATTRIBUTE_METADATA_NAME)).get(0);
        String baseName = attributeMap.get(Constants.ATTRIBUTE_METADATA_BASE_NAME).get(0);
        String baseUUID = attributeMap.get(Constants.ATTRIBUTE_BASE_UUID).get(0);
        return new GenericVersionV1(registry, name, uuid, baseName, baseUUID, propBag, attributeMap);
    }


    private void createAttributesContent(GenericVersionV1 genericVersionV1, OMElement element) throws MetadataException {

        OMFactory factory = OMAbstractFactory.getOMFactory();

        OMElement uuid = factory.createOMElement(new QName(Constants.ATTRIBUTE_UUID));
        uuid.setText(genericVersionV1.getUUID());

        OMElement name = factory.createOMElement(new QName(Constants.ATTRIBUTE_METADATA_NAME));
        name.setText(genericVersionV1.getName());

        OMElement baseName = factory.createOMElement(new QName(Constants.ATTRIBUTE_METADATA_BASE_NAME));
        baseName.setText(genericVersionV1.getBaseName());

        OMElement baseUUID = factory.createOMElement(new QName(Constants.ATTRIBUTE_BASE_UUID));
        baseUUID.setText(genericVersionV1.getBaseUUID());

        element.addChild(uuid);
        element.addChild(name);
        element.addChild(baseName);
        element.addChild(baseUUID);
    }

    private void createPropertiesContent(GenericVersionV1 serviceV1, OMElement element) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        for (Map.Entry<String, List<String>> entry : serviceV1.getPropertyBag().entrySet()) {
            if (entry.getValue() == null) continue;
            OMElement attribute = factory.createOMElement(new QName(entry.getKey()));
            attribute.setText(entry.getValue().get(0));
            element.addChild(attribute);
        }

    }

}
