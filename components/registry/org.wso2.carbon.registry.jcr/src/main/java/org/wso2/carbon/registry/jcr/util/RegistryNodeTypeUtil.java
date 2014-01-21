/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.jcr.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.jcr.RegistrySession;
import org.wso2.carbon.registry.jcr.nodetype.RegistryNodeType;
import org.wso2.carbon.registry.jcr.nodetype.RegistryNodeTypeManager;
import org.wso2.carbon.registry.jcr.nodetype.RegistryPropertyDefinitionTemplate;
import org.wso2.carbon.registry.jcr.util.nodetype.xml.NodeTypeReader;

import javax.jcr.*;
import javax.jcr.nodetype.*;
import javax.jcr.version.OnParentVersionAction;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class RegistryNodeTypeUtil {


//    public static void loadNodeTypesToJCRSystem(RegistrySession registrySession) {
//
//        Node nodeTypes, jcrSystem;
//        try {
//            jcrSystem = registrySession.getNode("/jcr:system");
//
//            if (jcrSystem.hasNode("jcr:nodeTypes")) {
//                nodeTypes = jcrSystem.getNode("jcr:nodeTypes");
//            } else {
//                nodeTypes = jcrSystem.addNode("jcr:nodeTypes", "nt:nodeType");
//            }
//        } catch (RepositoryException e) {
//
//        }
//    }

    public static PropertyDefinitionTemplate createJCRPrimaryTypeProperty(NodeTypeManager nodeTypeManager, String name) throws RepositoryException {
        PropertyDefinitionTemplate propertyDefinitionTemplate1 = nodeTypeManager.createPropertyDefinitionTemplate();
        propertyDefinitionTemplate1.setName("jcr:primaryType");
        propertyDefinitionTemplate1.setRequiredType(PropertyType.NAME);
        propertyDefinitionTemplate1.setDefaultValues(null);
        propertyDefinitionTemplate1.setAutoCreated(true);
        propertyDefinitionTemplate1.setMandatory(true);
        propertyDefinitionTemplate1.setOnParentVersion(OnParentVersionAction.COMPUTE);
        propertyDefinitionTemplate1.setProtected(true);
        propertyDefinitionTemplate1.setMultiple(false);
        ((RegistryPropertyDefinitionTemplate) propertyDefinitionTemplate1).setDeclaringNodeTypeName(name);
        return propertyDefinitionTemplate1;
    }

    public static void locadJCRBuiltInNodeTypesToSystemFromXML(NodeTypeManager nodeTypeManager) throws RepositoryException {

        String streamPath = System.getProperty("wso2.registry.nodetype.xml");

        OMElement processInfoElement;
        InputStream is = null;
        try {
            is = new FileInputStream(streamPath);
            XMLStreamReader reader = XMLInputFactory.newInstance().
                    createXMLStreamReader(is);
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            processInfoElement = builder.getDocumentElement();

            AXIOMXPath expression = new AXIOMXPath("/nodeTypes/nodeType");
            List attributes = expression.selectNodes(processInfoElement);

            for (Object o : attributes) {
                OMElement omNode = (OMElement) o;
                NodeTypeTemplate nodeTypeTemplate = new NodeTypeReader(nodeTypeManager).buildNodeType(omNode);
                ((RegistryNodeTypeManager) nodeTypeManager).registerNodeTypeFromXML(nodeTypeTemplate, false); // allowUpdates - false
            }

            is.close();

        } catch (IOException e) {
            throw new RepositoryException("Exception occurred while reading from : " + streamPath);
        } catch (JaxenException e) {
            throw new RepositoryException("Exception occurred while reading from : " + streamPath);
        } catch (XMLStreamException e) {
            throw new RepositoryException("Exception occurred while reading from : " + streamPath);
        }
    }

    public static void persistNodeTypeToRegistry(NodeTypeDefinition nodeTypeDefinition, RegistrySession registrySession) {
        try {

//            NodeTypeTemplate nodeTypeTemplate = (NodeTypeTemplate) nodeTypeDefinition;
            NodeTypeDefinition nodeTypeTemplate =  nodeTypeDefinition;

            String nodeTypePath = RegistryJCRSpecificStandardLoderUtil.getSystemConfigNodeTypePath(registrySession)
                    + "/" + nodeTypeTemplate.getName();
            CollectionImpl nodetype = (CollectionImpl) registrySession.getUserRegistry().newCollection();
            nodetype.setName(nodeTypeTemplate.getName());


            //set primary attributes of node type
            nodetype.setProperty("name", nodeTypeTemplate.getName());
            nodetype.setProperty("primaryItemName", nodeTypeTemplate.getPrimaryItemName());
            if(nodeTypeTemplate.getDeclaredSupertypeNames() != null){
                nodetype.setProperty("declaredSuperTypes", Arrays.asList(nodeTypeTemplate.getDeclaredSupertypeNames()));
            }
            nodetype.setProperty("hasOrderableChildNodes", String.valueOf(nodeTypeTemplate.hasOrderableChildNodes()));
            nodetype.setProperty("isAbstract", String.valueOf(nodeTypeTemplate.isAbstract()));
            nodetype.setProperty("isMixin", String.valueOf(nodeTypeTemplate.isMixin()));
            nodetype.setProperty("isQueryable", String.valueOf(nodeTypeTemplate.isQueryable()));

            nodetype.setParentPath(RegistryJCRSpecificStandardLoderUtil.getSystemConfigNodeTypePath(registrySession));
            registrySession.getUserRegistry().put(nodeTypePath, nodetype);

            // add property defs
            for (PropertyDefinition _pd : nodeTypeTemplate.getDeclaredPropertyDefinitions()) {
                PropertyDefinitionTemplate pd = (PropertyDefinitionTemplate) _pd;
                String propDefPath = nodeTypePath + "/" + RegistryJCRSpecificStandardLoderUtil.JCR_SYSTEM_PERSIS_PROP_DEFS
                        + "/" + pd.getName();

                Resource pdNode = registrySession.getUserRegistry().newResource();
                pdNode.setProperty("name", pd.getName());
                pdNode.setProperty("autoCreated", String.valueOf(pd.isAutoCreated()));
                pdNode.setProperty("mandatory", String.valueOf(pd.isMandatory()));
                pdNode.setProperty("protected", String.valueOf(pd.isProtected()));
                pdNode.setProperty("multiple", String.valueOf(pd.isMultiple()));
                pdNode.setProperty("isFullTextSearchable", String.valueOf(pd.isFullTextSearchable()));
                pdNode.setProperty("isQueryOrderable", String.valueOf(pd.isQueryOrderable()));
                if(pd.getAvailableQueryOperators() != null){
                    pdNode.setProperty("availableQueryOperators", Arrays.asList(pd.getAvailableQueryOperators()));
                }
                pdNode.setProperty("requiredType", String.valueOf(pd.getRequiredType())); //type is integer
                if(pd.getValueConstraints() != null) {
                    pdNode.setProperty("valueConstraints", Arrays.asList(pd.getValueConstraints())); //type is string[]
                }
                pdNode.setProperty("onParentVersion", String.valueOf(pd.getOnParentVersion())); //type int

                //Adding default values
                if(pd.getDefaultValues() != null) {
                    List<String> defaultValList = new ArrayList<String>();
                    for (Value value : pd.getDefaultValues()) {
                        defaultValList.add(value.getString());    // TODO supports only for String type Values
                    }
                    pdNode.setProperty("defaultValues", defaultValList); // type is string []
                }

                registrySession.getUserRegistry().put(propDefPath, pdNode);
            }

            //Adding child node defs
            for (NodeDefinition nodeDefinition : nodeTypeTemplate.getDeclaredChildNodeDefinitions()) {
                NodeDefinitionTemplate nd = (NodeDefinitionTemplate) nodeDefinition;

                String childDefPath = nodeTypePath + "/" + RegistryJCRSpecificStandardLoderUtil.JCR_SYSTEM_PERSIS_CHILDNODE_DEFS
                        + "/" + nd.getName();
                Resource childNode = registrySession.getUserRegistry().newResource();

                childNode.setProperty("name", nd.getName());
                childNode.setProperty("autoCreated", String.valueOf(nd.isAutoCreated()));
                childNode.setProperty("mandatory", String.valueOf(nd.isMandatory()));
                childNode.setProperty("protected", String.valueOf(nd.isProtected()));
                childNode.setProperty("onParentVersion", String.valueOf(nd.getOnParentVersion())); //type is int
                childNode.setProperty("sameNameSiblings", String.valueOf(nd.allowsSameNameSiblings()));
                childNode.setProperty("defaultPrimaryType", nd.getDefaultPrimaryTypeName());
                if(nd.getRequiredPrimaryTypeNames() != null) {
                    childNode.setProperty("requiredPrimaryTypes", Arrays.asList(nd.getRequiredPrimaryTypeNames()));
                }
                registrySession.getUserRegistry().put(childDefPath, childNode);
            }


        } catch (RegistryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ValueFormatException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static void loadNodeTypesFromRegistry(RegistryNodeTypeManager registryNodeTypeManager, RegistrySession registrySession) {
        NodeTypeTemplate nodeTypeTemplate = null;
        try {
            nodeTypeTemplate = registryNodeTypeManager.createNodeTypeTemplate();
            String[] paths = ((CollectionImpl) registrySession.getUserRegistry().get(
                    RegistryJCRSpecificStandardLoderUtil.
                            getSystemConfigNodeTypePath(registrySession))).getChildren();

            for (String path : paths) {
                CollectionImpl nodeType = (CollectionImpl) registrySession.getUserRegistry().get(path);
//                String nodeTypePat = RegistryJCRSpecificStandardLoderUtil.getSystemConfigNodeTypePath(registrySession) + "/" +
//                        nodeType.getProperty("name").replaceAll(":","-");

                nodeTypeTemplate.setName(nodeType.getProperty("name"));

                if(nodeType.getPropertyValues("declaredSuperTypes") != null) {
                    nodeTypeTemplate.setDeclaredSuperTypeNames(nodeType.getPropertyValues("declaredSuperTypes").toArray(new String[0]));
                }
                nodeTypeTemplate.setMixin(Boolean.valueOf(nodeType.getProperty("isMixin")));
                nodeTypeTemplate.setOrderableChildNodes(Boolean.valueOf(nodeType.getProperty("hasOrderableChildNodes")));
                nodeTypeTemplate.setAbstract(Boolean.valueOf(nodeType.getProperty("isAbstract")));
                nodeTypeTemplate.setQueryable(Boolean.valueOf(nodeType.getProperty("isQueryable")));
                nodeTypeTemplate.setPrimaryItemName(nodeType.getProperty("primaryItemName"));

                //node defs loading
                String childDefRootPath = path + "/" + RegistryJCRSpecificStandardLoderUtil.JCR_SYSTEM_PERSIS_CHILDNODE_DEFS;
                if(registrySession.getUserRegistry().resourceExists(childDefRootPath)) {
                String[] childDefPaths = ((CollectionImpl) registrySession.getUserRegistry().get(childDefRootPath)).getChildren();

                for (String childPath : childDefPaths) {
                    Resource childDef = registrySession.getUserRegistry().get(childPath);

                    NodeDefinitionTemplate nodeDefinitionTemplate = registryNodeTypeManager.createNodeDefinitionTemplate();
                    nodeDefinitionTemplate.setName(childDef.getProperty("name"));
                    nodeDefinitionTemplate.setAutoCreated(Boolean.valueOf(childDef.getProperty("autoCreated")));
                    nodeDefinitionTemplate.setMandatory(Boolean.valueOf(childDef.getProperty("mandatory")));
                    nodeDefinitionTemplate.setProtected(Boolean.valueOf(childDef.getProperty("protected")));
                    nodeDefinitionTemplate.setOnParentVersion(Integer.valueOf(childDef.getProperty("onParentVersion")));
                    nodeDefinitionTemplate.setSameNameSiblings(Boolean.valueOf(childDef.getProperty("sameNameSiblings")));
                    nodeDefinitionTemplate.setDefaultPrimaryTypeName(childDef.getProperty("defaultPrimaryType"));
                    if(childDef.getPropertyValues("requiredPrimaryTypes") != null) {
                        nodeDefinitionTemplate.setRequiredPrimaryTypeNames(childDef.
                                getPropertyValues("requiredPrimaryTypes").toArray(new String[0]));
                    }
                    nodeTypeTemplate.getNodeDefinitionTemplates().add(nodeDefinitionTemplate);
                }
                }

                //load prop defs
                String propDefRootPath = path + "/" + RegistryJCRSpecificStandardLoderUtil.JCR_SYSTEM_PERSIS_PROP_DEFS;
                if(registrySession.getUserRegistry().resourceExists(propDefRootPath)) {
                String[] propDefPaths = ((CollectionImpl) registrySession.getUserRegistry().get(propDefRootPath)).getChildren();

                for (String propPath : propDefPaths) {
                    Resource propdDef = registrySession.getUserRegistry().get(propPath);
                    PropertyDefinitionTemplate propertyDefinitionTemplate = registryNodeTypeManager.createPropertyDefinitionTemplate();
                    propertyDefinitionTemplate.setName(propdDef.getProperty("name"));
                    propertyDefinitionTemplate.setAutoCreated(Boolean.valueOf(propdDef.getProperty("autoCreated")));
                    propertyDefinitionTemplate.setMandatory(Boolean.valueOf(propdDef.getProperty("mandatory")));
                    propertyDefinitionTemplate.setProtected(Boolean.valueOf(propdDef.getProperty("protected")));
                    propertyDefinitionTemplate.setMultiple(Boolean.valueOf(propdDef.getProperty("multiple")));
                    propertyDefinitionTemplate.setFullTextSearchable(Boolean.valueOf(propdDef.getProperty("isFullTextSearchable")));
                    propertyDefinitionTemplate.setQueryOrderable(Boolean.valueOf(propdDef.getProperty("isQueryOrderable")));

                    if(propdDef.getPropertyValues("availableQueryOperators") != null) {
                        propertyDefinitionTemplate.setAvailableQueryOperators(propdDef.
                                getPropertyValues("availableQueryOperators").toArray(new String[0]));
                    }

                    propertyDefinitionTemplate.setOnParentVersion(Integer.valueOf(propdDef.getProperty("onParentVersion")));
                    propertyDefinitionTemplate.setOnParentVersion(Integer.valueOf(propdDef.getProperty("requiredType")));

                    if(propdDef.getPropertyValues("valueConstraints") != null) {
                        propertyDefinitionTemplate.setValueConstraints(propdDef.
                                getPropertyValues("valueConstraints").toArray(new String[0]));
                    }

                    if(propdDef.getPropertyValues("defaultValues") != null) {
                        propertyDefinitionTemplate.setValueConstraints(propdDef.
                                getPropertyValues("defaultValues").toArray(new String[0]));
                    }
                    nodeTypeTemplate.getPropertyDefinitionTemplates().add(propertyDefinitionTemplate);
                }
                }

                // Creating the node type
                NodeType nodeTypeBean = new RegistryNodeType(nodeTypeTemplate, registryNodeTypeManager);
                if (nodeTypeTemplate.getName() != null && nodeTypeTemplate.getName().startsWith("mix")) {
                    registryNodeTypeManager.getMixinNodetypes().add(nodeTypeBean);
                } else {
                    registryNodeTypeManager.getPrimaryNodetypes().add(nodeTypeBean);
                }
                registryNodeTypeManager.getNodeTypesList().add(nodeTypeBean);

            }

        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (RegistryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


    public static void unregisterNodeTypeFromRegistry(RegistrySession registrySession,
                                                      RegistryNodeTypeManager registryNodeTypeManager,
                                                      String nodeType) {

        try {
            String[] paths = ((CollectionImpl) registrySession.getUserRegistry().get(
                    RegistryJCRSpecificStandardLoderUtil.
                            getSystemConfigNodeTypePath(registrySession))).getChildren();
            for (String path : paths) {
                String name = path.split("/")[path.split("/").length - 1];
                if (name.equals(nodeType)) {
                    registrySession.getUserRegistry().delete(path);
                }
            }


        } catch (RegistryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }

}
