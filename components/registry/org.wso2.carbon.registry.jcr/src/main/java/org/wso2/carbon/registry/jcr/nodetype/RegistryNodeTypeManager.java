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

package org.wso2.carbon.registry.jcr.nodetype;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.jcr.RegistryRepository;
import org.wso2.carbon.registry.jcr.RegistrySession;
import org.wso2.carbon.registry.jcr.util.RegistryJCRSpecificStandardLoderUtil;
import org.wso2.carbon.registry.jcr.util.RegistryNodeTypeUtil;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.nodetype.*;
import javax.jcr.version.OnParentVersionAction;
import java.util.HashSet;
import java.util.Set;


public class RegistryNodeTypeManager implements NodeTypeManager {

    private RegistrySession registrySession = null;
    private Set<NodeType> nodeTypesList = new HashSet<NodeType>();
    private Set<NodeType> primaryNodetypes = new HashSet<NodeType>();
    private Set<NodeType> mixinNodetypes = new HashSet<NodeType>();
    private static Log log = LogFactory.getLog(RegistryNodeTypeManager.class);
    private static final String pathToNodeTypes = "";

    public RegistryNodeTypeManager(RegistrySession registrySession) throws RepositoryException {
        this.registrySession = registrySession;
        RegistryNodeTypeUtil.locadJCRBuiltInNodeTypesToSystemFromXML(this);
        RegistryNodeTypeUtil.loadNodeTypesFromRegistry(this,registrySession);
    }

    public Set<NodeType> getNodeTypesList() {
        return nodeTypesList;
    }

//    private void loadJCRStandardNodeTypes() throws RepositoryException {
//
//        NodeTypeTemplate ntTemplate = null;
//        try {
//            for (String nodeTypeName : RegistryJCRSpecificStandardLoderUtil.getNodeTypeList()) {
//                ntTemplate = createNodeTypeTemplate();
//
//                // set node type name
//                ntTemplate.setName(nodeTypeName);
//
//                // set node type is mix or not
//                if (isNodeTypeNameIsMix(nodeTypeName)) {
//                    ntTemplate.setMixin(true);
//                } else {
//                    ntTemplate.setMixin(false);
//
//                    //if Not a mix (to all primary node types), set the jcr:primaryType as a mandatory property
//                    PropertyDefinitionTemplate propertyDefinitionTemplate = createPropertyDefinitionTemplate();
//                    propertyDefinitionTemplate.setName("jcr:primaryType");
//                    propertyDefinitionTemplate.setMandatory(true);
//
//                    //Must Set the corresponding nodetype name to the property template
//                    ((RegistryPropertyDefinitionTemplate) propertyDefinitionTemplate).setDeclaringNodeTypeName(ntTemplate.getName());
//                    ntTemplate.getPropertyDefinitionTemplates().add(propertyDefinitionTemplate);
//                }
//
//                //set root primary item type
//                if (nodeTypeName.equals(RegistryJCRSpecificStandardLoderUtil.
//                        WORKSPACE_ROOT_PRIMARY_NODETYPE_NAME)) {
//                    ntTemplate.setPrimaryItemName(RegistryJCRSpecificStandardLoderUtil.
//                            WORKSPACE_ROOT_PRIMARY_ITEM_NAME);
//                }
//
//                //set super type as nt:base for all except nt:base
//                if (!nodeTypeName.equals("nt:base")) {
//                    ntTemplate.setDeclaredSuperTypeNames(new String[]{"nt:base"});
//                }
//
//                registerNodeType(ntTemplate, true);
//            }
//            // Temp Fix which load the node type hierarchy for only five node types
//            loadToNodeTypeHierarchy();
//
//        } catch (RepositoryException e) {
//            throw new RepositoryException("Unable to load standard jcr node types" + ntTemplate.getName());
//        }
//    }

//    private void loadToNodeTypeHierarchy() throws RepositoryException {
//
//        // TODO NOTE: This is a temp fix and these node types should load in same order as if condition order
//        // should read these from a XML for property file and load to /jcr:system/jcr:nodetypes/
//
//        NodeTypeTemplate ntt0 = ((RegistryNodeType) getNodeType("mix:lifecycle")).getDefinition();
//        ntt0.setDeclaredSuperTypeNames(new String[]{});
//        ntt0.setMixin(true);
//        ntt0.setOrderableChildNodes(false);
//        ntt0.setPrimaryItemName(null);
//
//        PropertyDefinitionTemplate propertyDefinitionTemplate = createPropertyDefinitionTemplate();
//        propertyDefinitionTemplate.setName("jcr:currentLifecycleState");
//        propertyDefinitionTemplate.setRequiredType(PropertyType.STRING);
//        propertyDefinitionTemplate.setDefaultValues(null);
//        propertyDefinitionTemplate.setAutoCreated(false);
//        propertyDefinitionTemplate.setMandatory(false);
//        propertyDefinitionTemplate.setOnParentVersion(OnParentVersionAction.INITIALIZE);
//        propertyDefinitionTemplate.setProtected(true);
//        propertyDefinitionTemplate.setMultiple(false);
//        ((RegistryPropertyDefinitionTemplate) propertyDefinitionTemplate).setDeclaringNodeTypeName(ntt0.getName());
//        ntt0.getPropertyDefinitionTemplates().add(propertyDefinitionTemplate);
//
//        PropertyDefinitionTemplate propertyDefinitionTemplate1 = createPropertyDefinitionTemplate();
//        propertyDefinitionTemplate1.setName("jcr:lifecyclePolicy");
//        propertyDefinitionTemplate1.setRequiredType(PropertyType.REFERENCE);
//        propertyDefinitionTemplate1.setDefaultValues(null);
//        propertyDefinitionTemplate1.setAutoCreated(false);
//        propertyDefinitionTemplate1.setMandatory(false);
//        propertyDefinitionTemplate1.setOnParentVersion(OnParentVersionAction.INITIALIZE);
//        propertyDefinitionTemplate1.setProtected(true);
//        propertyDefinitionTemplate1.setMultiple(false);
//        ((RegistryPropertyDefinitionTemplate) propertyDefinitionTemplate1).setDeclaringNodeTypeName(ntt0.getName());
//        ntt0.getPropertyDefinitionTemplates().add(propertyDefinitionTemplate1);
//
//
//        NodeTypeTemplate ntt1 = ((RegistryNodeType) getNodeType("nt:file")).getDefinition();
//        ntt1.setDeclaredSuperTypeNames(new String[]{"nt:hierarchyNode", "nt:base"});
//        ntt1.setPrimaryItemName("jcr:content");
//
//        NodeTypeTemplate ntt2 = ((RegistryNodeType) getNodeType("nt:folder")).getDefinition();
//        ntt2.setDeclaredSuperTypeNames(new String[]{"nt:hierarchyNode", "nt:base"});
//
//        NodeTypeTemplate ntt3 = ((RegistryNodeType) getNodeType("nt:linkedFile")).getDefinition();
//        ntt3.setDeclaredSuperTypeNames(new String[]{"nt:hierarchyNode", "nt:base"});
//        ntt3.setPrimaryItemName("jcr:content");
//
//        NodeTypeTemplate ntt4 = ((RegistryNodeType) getNodeType("nt:resource")).getDefinition();
//        ntt4.setDeclaredSuperTypeNames(new String[]{"mix:mimeType", "mix:lastModified", "nt:base"});
//        ntt4.setPrimaryItemName("jcr:date");
//
//        NodeTypeTemplate ntt5 = ((RegistryNodeType) getNodeType("nt:hierarchyNode")).getDefinition();
//        ntt5.setDeclaredSuperTypeNames(new String[]{"mix:created", "nt:base"});
//
//        NodeDefinitionTemplate ndt1 = createNodeDefinitionTemplate();
//        NodeDefinitionTemplate ndt2 = createNodeDefinitionTemplate();
//
//        ndt1.setName("nt:file");
//        // Must set the corresponding node type to the node definition
//        ((RegistryNodeDefinitionTemplate) ndt1).setDeclaringNodeTypeName(ntt5.getName());
//
//        ndt2.setName("nt:folder");
//        // Must set the corresponding node type to the node definition
//        ((RegistryNodeDefinitionTemplate) ndt2).setDeclaringNodeTypeName(ntt5.getName());
//
//        ntt5.getNodeDefinitionTemplates().add(ndt1);
//        ntt5.getNodeDefinitionTemplates().add(ndt2);
//        ntt5.setAbstract(true);
//    }
//
//    private boolean isNodeTypeNameIsMix(String ntName) {
//
//        if (ntName.startsWith("mix")) {
//            return true;
//        } else {
//            return false;
//        }
//    }

    public NodeType getNodeType(String s) throws NoSuchNodeTypeException, RepositoryException {

        NodeType matchNt = null;
        NodeType nt = null;
        boolean matchNTFound = false;

//        TODO hack to support all nodes in greg other than jcr repo
       if(s != null && s.equals("")) {
         return  getNodeType("default");
       }

        NodeTypeIterator it = getAllNodeTypes();
        while (it.hasNext()) {
            nt = (NodeType) it.next();
            if ((nt != null) && (nt.getName() != null) && (nt.getName().equals(s))) {
                matchNt = nt;
                matchNTFound = true;
                break;
            }
        }

        if (!matchNTFound) {
            throw new NoSuchNodeTypeException("Invalid suffix for a Node Type :" + s);
        }

        return matchNt;
    }

    public boolean hasNodeType(String s) {

        boolean hasNtype = true;

        try {
            getNodeType(s);
        } catch (NoSuchNodeTypeException e) {
            hasNtype = false;
        } catch (RepositoryException e) {
            log.error("Error while registering node type " + e.getMessage());
        }


        return hasNtype;
    }

    public NodeTypeIterator getAllNodeTypes() throws RepositoryException {

        return new RegistryNodeTypeIterator(nodeTypesList);

    }

    public NodeTypeIterator getPrimaryNodeTypes() throws RepositoryException {

        return new RegistryNodeTypeIterator(primaryNodetypes);
    }

    public NodeTypeIterator getMixinNodeTypes() throws RepositoryException {

        return new RegistryNodeTypeIterator(mixinNodetypes);
    }

    public NodeTypeTemplate createNodeTypeTemplate() throws UnsupportedRepositoryOperationException, RepositoryException {

        NodeTypeTemplate ntTmpl = new RegistryNodeTypeTemplate();
        return ntTmpl;
    }

    public NodeTypeTemplate createNodeTypeTemplate(NodeTypeDefinition nodeTypeDefinition) throws UnsupportedRepositoryOperationException, RepositoryException {
        return new RegistryNodeTypeTemplate(nodeTypeDefinition);
    }

    public NodeDefinitionTemplate createNodeDefinitionTemplate() throws UnsupportedRepositoryOperationException, RepositoryException {

        return new RegistryNodeDefinitionTemplate(this);

    }

    public PropertyDefinitionTemplate createPropertyDefinitionTemplate() throws UnsupportedRepositoryOperationException, RepositoryException {

        return new RegistryPropertyDefinitionTemplate(this);
    }

    public Set<NodeType> getMixinNodetypes() {
        return mixinNodetypes;
    }

    public Set<NodeType> getPrimaryNodetypes() {
        return primaryNodetypes;
    }

    public NodeType registerNodeType(NodeTypeDefinition nodeTypeDefinition, boolean b) throws InvalidNodeTypeDefinitionException, NodeTypeExistsException, UnsupportedRepositoryOperationException, RepositoryException {

        if (hasNodeType(nodeTypeDefinition.getName())) {
            throw new NodeTypeExistsException("Node type already exists :" + nodeTypeDefinition.getName());
        }

        NodeType nt = null;
        boolean proceed = false;

        if (!hasNodeType(nodeTypeDefinition.getName())) {
            proceed = true;
        } else if ((b) && (hasNodeType(nodeTypeDefinition.getName()))) {
            proceed = true;
        }

        if (proceed) {
            nt = new RegistryNodeType(nodeTypeDefinition, this);
        }
        if (nodeTypeDefinition.getName().startsWith("mix")) {
            mixinNodetypes.add(nt);      // TODO should persist to registry tree
        } else {
            primaryNodetypes.add(nt);
        }

        nodeTypesList.add(nt);
        persistNodeTypeToRegistry(nt);
        return nt;
    }


 public NodeType registerNodeTypeFromXML(NodeTypeDefinition nodeTypeDefinition, boolean b) throws InvalidNodeTypeDefinitionException, NodeTypeExistsException, UnsupportedRepositoryOperationException, RepositoryException {

        if (hasNodeType(nodeTypeDefinition.getName())) {
            throw new NodeTypeExistsException("Node type already exists :" + nodeTypeDefinition.getName());
        }

        NodeType nt = null;
        boolean proceed = false;

        if (!hasNodeType(nodeTypeDefinition.getName())) {
            proceed = true;
        } else if ((b) && (hasNodeType(nodeTypeDefinition.getName()))) {
            proceed = true;
        }

        if (proceed) {
            nt = new RegistryNodeType(nodeTypeDefinition, this);
        }
        if (nodeTypeDefinition.getName().startsWith("mix")) {
            mixinNodetypes.add(nt);      // TODO should persist to registry tree
        } else {
            primaryNodetypes.add(nt);
        }

        nodeTypesList.add(nt);
        return nt;
    }



    private void persistNodeTypeToRegistry(NodeTypeDefinition nodeTypeDefinition) {
        RegistryNodeTypeUtil.persistNodeTypeToRegistry(nodeTypeDefinition,registrySession);
    }

    public NodeTypeIterator registerNodeTypes(NodeTypeDefinition[] nodeTypeDefinitions, boolean b) throws InvalidNodeTypeDefinitionException, NodeTypeExistsException, UnsupportedRepositoryOperationException, RepositoryException {

        Set ntlist = new HashSet();
        NodeTypeIterator nti = null;

        for (NodeTypeDefinition ntd : nodeTypeDefinitions) {
            ntlist.add(registerNodeType(ntd, b));
        }
        nti = new RegistryNodeTypeIterator(ntlist);
        return nti;
    }

    public void unregisterNodeType(String s) throws UnsupportedRepositoryOperationException, NoSuchNodeTypeException, RepositoryException {

        if (!hasNodeType(s)) {
            throw new NoSuchNodeTypeException("No such node type exists to un register : " + s);
        }

        if ((s != null) && ("nt:base".equals(s))) {
            throw new RepositoryException("Cannot remove the base node type");
        }

        unRegisterNodeTypeAt(s);
//        NodeType nt = null;
//        NodeTypeIterator it = getAllNodeTypes();

//        while (it.hasNext()) {
//            nt = (NodeType) it.next();
//            if ((nt != null) && (nt.getName().equals(s))) {
//            }
//        }
    }

    public void unregisterNodeTypes(String[] strings) throws UnsupportedRepositoryOperationException, NoSuchNodeTypeException, RepositoryException {

        for (String s : strings) {
            unregisterNodeType(s);
        }
    }

    private void unRegisterNodeTypeAt(String nodeType) throws RepositoryException {   // Non JCR custom method
        NodeType tempNT = getNodeType(nodeType);
        if (tempNT.isMixin()) {
            mixinNodetypes.remove(tempNT);
        } else {
            primaryNodetypes.remove(tempNT);
        }
        nodeTypesList.remove(tempNT);
        RegistryNodeTypeUtil.unregisterNodeTypeFromRegistry(registrySession,this,nodeType);
    }
}
