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

import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.jcr.RegistrySession;

import javax.jcr.nodetype.ConstraintViolationException;
import java.util.*;

public class RegistryJCRSpecificStandardLoderUtil {

    private static Set<String> nodeTypesList = new HashSet<String>();
    private static Set<String> primaryNodetypes = new HashSet<String>();
    private static Set<String> mixinNodetypes = new HashSet<String>();
    private static List<String> jcrSystemPropertyValues = new ArrayList<String>();
    private static final List<String> implicitPropertiyNames = new ArrayList<String>();
    private static Map<String, String> nameSpacePrefx = new HashMap<String, String>();
    private static Map<String, String> nameSpaceURI = new HashMap<String, String>();
    private static List<String> nameSpaceURIList = new ArrayList<String>();
    private static boolean initialized = false;
    private static final String DEFAULT_REGISTRY_WORKSPACE_NAME = "default_workspace";
    private static final String JCR_REGISTRY_WORKSPACE_ROOT = "/jcr_system/workspaces";
    public static final String WORKSPACE_ROOT_PRIMARY_NODETYPE_NAME = "system_config";
    public static final String WORKSPACE_ROOT_PRIMARY_ITEM_NAME = "greg";
    public static final String JCR_SYSTEM_PERSIS_PROP_DEFS = "prop_defs";
    public static final String JCR_SYSTEM_PERSIS_CHILDNODE_DEFS = "child_defs";
    public static final String JCR_SYSTEM_CONFIG = "greg_jcr_config";
    public static final String JCR_SYSTEM_VERSION_LABELS = "sys_ver_labels";
    private static final String JCR_SYSTEM_CONFIG_VERSION = "sys_versions";
    private static final String JCR_SYSTEM_CONFIG_NODE_TYPES = "sys_node_types";



    public static void init() throws ConstraintViolationException {
        if (!initialized) {
            loadNodeTypeList();
            loadImplicitPropertyNames();
            locadJCRSystemPropertyValues();
            loadNameSpacePrefixMap();
            loadNameSpaceURIMap();
            initialized = true;
        }
    }

    public static boolean isSessionReadWrite(String userId) {
        if ("user".equals(userId)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isSessionReadOnly(String userId) {
        if ("anonymous".equals(userId)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isSessionSuperUser(String userId) {
        if ("superuser".equals(userId)) {
            return true;
        } else {
            return false;
        }
    }

    public static String getDefaultRegistryWorkspaceName() {
        return DEFAULT_REGISTRY_WORKSPACE_NAME;
    }

    public static String getJCRRegistryWorkspaceRoot() {
        return JCR_REGISTRY_WORKSPACE_ROOT;
    }

    public static String getJcrSystemConfigNodeTypes() {
        return JCR_SYSTEM_CONFIG_NODE_TYPES;
    }


    public static Map<String, String> getJCRSystemNameSpacePrefxMap() {
        return nameSpacePrefx;
    }

    public static List<String> getJCRSystemNameSpaceURIList() {
        return nameSpaceURIList;
    }

    public static Map<String, String> getJCRSystemNameSpaceURIMap() {
        return nameSpaceURI;
    }

    private static void locadJCRSystemPropertyValues() {
        jcrSystemPropertyValues.add("wso2.system.property.value.ismultiple");
    }

    public static List<String> getJCRSystemPropertyValues() {
        return jcrSystemPropertyValues;
    }

    public static List<String> getimplicitPropertiyNames() {
        return implicitPropertiyNames;
    }

    private static void loadImplicitPropertyNames() {

        implicitPropertiyNames.add("wso2.registry.jcr.versions");
        implicitPropertiyNames.add("registry.jcr.property.type");
//        implicitPropertiyNames.add("jcr:uuid");
//        implicitPropertiyNames.add("jcr:checkedOut");
//        implicitPropertiyNames.add("jcr:isCheckedOut");
//        implicitPropertiyNames.add("jcr:primaryType");

    }

    private static void loadNameSpacePrefixMap() {
        nameSpacePrefx.put("http://www.jcp.org/jcr/sv/1.0", "sv");
        nameSpacePrefx.put("http://www.jcp.org/jcr/mix/1.0", "mix");
        nameSpacePrefx.put("http://www.jcp.org/jcr/nt/1.0", "nt");
        nameSpacePrefx.put("http://www.jcp.org/jcr/1.0", "jcr");

        nameSpaceURIList.add("http://www.jcp.org/jcr/sv/1.0");
        nameSpaceURIList.add("http://www.jcp.org/jcr/mix/1.0");
        nameSpaceURIList.add("http://www.jcp.org/jcr/nt/1.0");
        nameSpaceURIList.add("http://www.jcp.org/jcr/1.0");

    }

    private static void loadNameSpaceURIMap() {
        nameSpaceURI.put("sv", "http://www.jcp.org/jcr/sv/1.0");
        nameSpaceURI.put("mix", "http://www.jcp.org/jcr/mix/1.0");
        nameSpaceURI.put("nt", "http://www.jcp.org/jcr/nt/1.0");
        nameSpaceURI.put("jcr", "http://www.jcp.org/jcr/1.0");
    }

    public static boolean isValidJCRName(String name) {
        boolean isValid = true;

        if ((name != null) && (name.startsWith(":"))) {
            isValid = false;
        }
        //TODO identify new conditions which makes a JCR name invalid

        return isValid;
    }


    private static void loadNodeTypeList() throws ConstraintViolationException {

        Set<String> set = new HashSet<String>();
        set.add("mix:lifecycle");
        set.add("mix:lockable");
        set.add("mix:referenceable");
        set.add("mix:shareable");
        set.add("mix:versionable");
        set.add("mix:simpleVersionable");
        set.add("mix:created");
        set.add("mix:lastModified");
        set.add("mix:etag");
        set.add("mix:title");
        set.add("mix:language");
        set.add("mix:mimeType");
        set.add("nt:address");
        set.add("nt:base");
        set.add("nt:unstructured");
        set.add("nt:file");
        set.add("nt:linkedFile");
        set.add("nt:folder");
        set.add("nt:hierarchyNode");
        set.add("nt:nodeType");
        set.add("nt:propertyDefinition");
        set.add("nt:childNodeDefinition");
        set.add("nt:versionHistory");
        set.add("nt:versionLabels");
        set.add("nt:version");
        set.add("nt:activity");
        set.add("nt:configuration");
        set.add("nt:frozenNode");
        set.add("nt:versionedChild");
        set.add("nt:query");
        set.add("nt:resource");
        set.add("jcr:system");


        for (String ntNAme : set) {
//            NodeTypeTemplate ntd = new RegistryNodeTypeTemplate();
//            ntd.setName(ntNAme);
//            NodeType nt = new RegistryNodeType(ntd);
            nodeTypesList.add(ntNAme);
            if (ntNAme.startsWith("mix")) {
                mixinNodetypes.add(ntNAme);
            }
        }

    }

    public static Set<String> getNodeTypeList() {
        return nodeTypesList;
    }

    public static Set<String> getPrimaryNodeTypes() {
        return primaryNodetypes;
    }

    public static Set<String> getMixinNodeTypes() {
        return mixinNodetypes;
    }

    public static void loadJCRSystemConfigs(UserRegistry userReg,String workspaceRoot) throws RegistryException {
        createSysBaseNodes(userReg,workspaceRoot);
        createSystemConfigVersion(userReg,workspaceRoot);
        createSystemConfigVersionLabelStore(userReg,workspaceRoot);
//        createSystemConfigNodeTypes(userReg,workspaceRoot);
    }

    private static void createSysBaseNodes(UserRegistry userReg, String workspaceRoot) throws RegistryException {
        if(!userReg.resourceExists(workspaceRoot+JCR_SYSTEM_CONFIG)) {
           CollectionImpl sysConf = (CollectionImpl) userReg.newCollection();
           userReg.put(workspaceRoot+JCR_SYSTEM_CONFIG,sysConf);
        }
        if(!userReg.resourceExists(workspaceRoot+JCR_SYSTEM_CONFIG+"/"+JCR_SYSTEM_CONFIG_NODE_TYPES)) {
           CollectionImpl sysConf = (CollectionImpl) userReg.newCollection();
           userReg.put(workspaceRoot+JCR_SYSTEM_CONFIG+"/"+JCR_SYSTEM_CONFIG_NODE_TYPES,sysConf);
        }
    }

//    private static void createSysBaseNodes() {
//       if()
//        greg:jcr_sys_config/sys_node_types
//    }

//    private static void createSystemConfig(UserRegistry userReg,String workspaceRoot) throws RegistryException {
//        String confPath = workspaceRoot
//                + JCR_SYSTEM_CONFIG;
//        if (!userReg.resourceExists(confPath)) {
//            Resource resource = (CollectionImpl)userReg.newCollection();
//            resource.setDescription("sys:config-jcr-storage");
//            resource.setProperty("sys:config","true");
//            userReg.put(confPath, resource);
//        }
//    }

    private static void createSystemConfigVersion(UserRegistry userReg,String workspaceRoot) throws RegistryException {
        String confVerPath = workspaceRoot
                + JCR_SYSTEM_CONFIG
                + "/"
                + JCR_SYSTEM_CONFIG_VERSION;
          if (!userReg.resourceExists(confVerPath)) {
            Resource resource = (CollectionImpl)userReg.newCollection();
            resource.setDescription("sys:config-jcr-storage");
            resource.setProperty("sys:config","true");
            userReg.put(confVerPath, resource);
        }
    }

     private static void createSystemConfigVersionLabelStore(UserRegistry userReg,String workspaceRoot) throws RegistryException {
        String confVerPath = workspaceRoot
                + JCR_SYSTEM_CONFIG
                + "/"
                + JCR_SYSTEM_VERSION_LABELS;
          if (!userReg.resourceExists(confVerPath)) {
            Resource resource = (CollectionImpl)userReg.newCollection();
            resource.setDescription("sys:config-jcr-storage");
            resource.setProperty("sys:config","true");
            userReg.put(confVerPath, resource);
        }
    }

    private static void createSystemConfigNodeTypes(UserRegistry userReg,String workspaceRoot) throws RegistryException {
          String confVerPath = workspaceRoot
                + JCR_SYSTEM_CONFIG
                + "/"
                + JCR_SYSTEM_CONFIG_NODE_TYPES;
          if (!userReg.resourceExists(confVerPath)) {
            Resource resource = (CollectionImpl)userReg.newCollection();
            resource.setDescription("sys:config-jcr-storage");
            resource.setProperty("sys:config","true");
            userReg.put(confVerPath, resource);
          }
    }

    public static String getSystemConfigVersionPath(RegistrySession registrySession){
       return   registrySession.getWorkspaceRootPath()
                + JCR_SYSTEM_CONFIG
                + "/"
                + JCR_SYSTEM_CONFIG_VERSION;
    }

    public static String getSystemConfigNodeTypePath(RegistrySession registrySession){
       return   registrySession.getWorkspaceRootPath()
                + JCR_SYSTEM_CONFIG
                + "/"
                + JCR_SYSTEM_CONFIG_NODE_TYPES;
    }

    public static String getSystemConfigVersionLabelPath(RegistrySession registrySession){
       return   registrySession.getWorkspaceRootPath()
                + JCR_SYSTEM_CONFIG
                + "/"
                + JCR_SYSTEM_VERSION_LABELS;
    }

}
