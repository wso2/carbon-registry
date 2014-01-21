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

package org.wso2.carbon.registry.jcr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.jcr.util.RegistryJCRSpecificStandardLoderUtil;

import javax.jcr.*;
import javax.jcr.lock.Lock;
import javax.jcr.nodetype.ConstraintViolationException;
import java.util.*;

public class RegistryRepository implements Repository {
    static Map keyMap = new HashMap();
    public static final Map credentialConstants = new LinkedHashMap();
    public RegistryService registryService;
    private Set workspaces = new HashSet();
    private Map<String, Workspace> workspaceMap = new HashMap<String, Workspace>();
    private static int versionCounter = 0;
    private static Log log = LogFactory.getLog(RegistryNode.class);
    private static String ADMIN_ROLE_NAME = "admin";
    private static String ANONYMOUS_USER = "anonymous";
    private static String SUPER_USER = "superuser";


  /**
     *  TODO It is better we can set the following in the registry DB
     */
//    private Set<NodeType> nodeTypesList = new HashSet<NodeType>();
//    private Set primaryNodetypes = new HashSet();
//    private Set mixinNodetypes = new HashSet();


    private static final String[] multivaluedKeys = {IDENTIFIER_STABILITY, NODE_TYPE_MANAGEMENT_INHERITANCE
            , QUERY_JOINS, LEVEL_1_SUPPORTED, LEVEL_2_SUPPORTED, QUERY_LANGUAGES};

    private static final String[] discriptorKeys = {
            IDENTIFIER_STABILITY, IDENTIFIER_STABILITY_INDEFINITE_DURATION, IDENTIFIER_STABILITY_METHOD_DURATION,
            IDENTIFIER_STABILITY_SAVE_DURATION, IDENTIFIER_STABILITY_SESSION_DURATION, NODE_TYPE_MANAGEMENT_AUTOCREATED_DEFINITIONS_SUPPORTED,
            NODE_TYPE_MANAGEMENT_INHERITANCE, NODE_TYPE_MANAGEMENT_INHERITANCE_MINIMAL, NODE_TYPE_MANAGEMENT_INHERITANCE_MULTIPLE,
            NODE_TYPE_MANAGEMENT_INHERITANCE_SINGLE, NODE_TYPE_MANAGEMENT_MULTIPLE_BINARY_PROPERTIES_SUPPORTED, NODE_TYPE_MANAGEMENT_MULTIVALUED_PROPERTIES_SUPPORTED,
            NODE_TYPE_MANAGEMENT_ORDERABLE_CHILD_NODES_SUPPORTED, NODE_TYPE_MANAGEMENT_OVERRIDES_SUPPORTED, NODE_TYPE_MANAGEMENT_PRIMARY_ITEM_NAME_SUPPORTED,
            NODE_TYPE_MANAGEMENT_PROPERTY_TYPES, NODE_TYPE_MANAGEMENT_RESIDUAL_DEFINITIONS_SUPPORTED, NODE_TYPE_MANAGEMENT_SAME_NAME_SIBLINGS_SUPPORTED,
            NODE_TYPE_MANAGEMENT_UPDATE_IN_USE_SUPORTED, NODE_TYPE_MANAGEMENT_VALUE_CONSTRAINTS_SUPPORTED, OPTION_ACCESS_CONTROL_SUPPORTED,
            OPTION_ACTIVITIES_SUPPORTED, OPTION_BASELINES_SUPPORTED, OPTION_JOURNALED_OBSERVATION_SUPPORTED,
            OPTION_LIFECYCLE_SUPPORTED, OPTION_LOCKING_SUPPORTED, OPTION_NODE_AND_PROPERTY_WITH_SAME_NAME_SUPPORTED,
            OPTION_NODE_TYPE_MANAGEMENT_SUPPORTED, OPTION_OBSERVATION_SUPPORTED, OPTION_RETENTION_SUPPORTED,
            OPTION_SHAREABLE_NODES_SUPPORTED, OPTION_SIMPLE_VERSIONING_SUPPORTED, OPTION_TRANSACTIONS_SUPPORTED,
            OPTION_UNFILED_CONTENT_SUPPORTED, OPTION_UPDATE_MIXIN_NODE_TYPES_SUPPORTED, OPTION_UPDATE_PRIMARY_NODE_TYPE_SUPPORTED,
            OPTION_VERSIONING_SUPPORTED, OPTION_WORKSPACE_MANAGEMENT_SUPPORTED, OPTION_XML_EXPORT_SUPPORTED,
            OPTION_XML_IMPORT_SUPPORTED, QUERY_FULL_TEXT_SEARCH_SUPPORTED, QUERY_JOINS_INNER,
            QUERY_JOINS_INNER_OUTER, QUERY_JOINS_NONE, QUERY_LANGUAGES,
            QUERY_STORED_QUERIES_SUPPORTED, REP_NAME_DESC, REP_VENDOR_DESC,
            REP_VENDOR_URL_DESC, REP_VERSION_DESC, SPEC_NAME_DESC,
            SPEC_VERSION_DESC, WRITE_SUPPORTED, LEVEL_2_SUPPORTED,
            LEVEL_1_SUPPORTED, OPTION_QUERY_SQL_SUPPORTED, QUERY_XPATH_POS_INDEX, QUERY_XPATH_DOC_ORDER, QUERY_JOINS
    };


    public static int getVersionCounter() {
        return versionCounter++;
    }

    public RegistryRepository(RegistryService registryService) throws RegistryException, ConstraintViolationException {

        this.registryService = registryService;


        credentialConstants.put(0, "SYSTEM:LOCAL_REPOSITORY");
        credentialConstants.put(1, "USER:CONFIG_USER_REGISTRY");
        credentialConstants.put(2, "SYSTEM:CONFIG_SYSTEM_REGISTRY");
        credentialConstants.put(3, "SYSTEM:GOVERNANCE_SYSTEM_REGISTRY");
        credentialConstants.put(4, "USER:GOVERNANCE_USER_REGISTRY");
        credentialConstants.put(5, "ALL:ROOT_REGISTRY");


        keyMap.put(Repository.IDENTIFIER_STABILITY, "true");
        keyMap.put(Repository.IDENTIFIER_STABILITY_INDEFINITE_DURATION, "true");
        keyMap.put(Repository.IDENTIFIER_STABILITY_METHOD_DURATION, "true");
        keyMap.put(Repository.IDENTIFIER_STABILITY_SAVE_DURATION, "true");
        keyMap.put(Repository.IDENTIFIER_STABILITY_SESSION_DURATION, "true");
        keyMap.put(Repository.NODE_TYPE_MANAGEMENT_AUTOCREATED_DEFINITIONS_SUPPORTED, "true");
        keyMap.put(Repository.NODE_TYPE_MANAGEMENT_INHERITANCE, "true");
        keyMap.put(Repository.NODE_TYPE_MANAGEMENT_INHERITANCE_MINIMAL, "true");
        keyMap.put(Repository.NODE_TYPE_MANAGEMENT_INHERITANCE_MULTIPLE, "true");
        keyMap.put(Repository.NODE_TYPE_MANAGEMENT_INHERITANCE_SINGLE, "true");
        keyMap.put(Repository.NODE_TYPE_MANAGEMENT_MULTIPLE_BINARY_PROPERTIES_SUPPORTED, "true");
        keyMap.put(Repository.NODE_TYPE_MANAGEMENT_MULTIVALUED_PROPERTIES_SUPPORTED, "true");
        keyMap.put(Repository.NODE_TYPE_MANAGEMENT_ORDERABLE_CHILD_NODES_SUPPORTED, "true");
        keyMap.put(Repository.NODE_TYPE_MANAGEMENT_OVERRIDES_SUPPORTED, "true");
        keyMap.put(Repository.NODE_TYPE_MANAGEMENT_PRIMARY_ITEM_NAME_SUPPORTED, "true");
        keyMap.put(Repository.NODE_TYPE_MANAGEMENT_PROPERTY_TYPES, "true");
        keyMap.put(Repository.NODE_TYPE_MANAGEMENT_RESIDUAL_DEFINITIONS_SUPPORTED, "true");
        keyMap.put(Repository.NODE_TYPE_MANAGEMENT_SAME_NAME_SIBLINGS_SUPPORTED, "true");
        keyMap.put(Repository.NODE_TYPE_MANAGEMENT_UPDATE_IN_USE_SUPORTED, "true");        //
        keyMap.put(Repository.NODE_TYPE_MANAGEMENT_VALUE_CONSTRAINTS_SUPPORTED, "true");
        keyMap.put(Repository.OPTION_ACCESS_CONTROL_SUPPORTED, "true");
        keyMap.put(Repository.OPTION_ACTIVITIES_SUPPORTED, "true");
        keyMap.put(Repository.OPTION_BASELINES_SUPPORTED, "true");
        keyMap.put(Repository.OPTION_JOURNALED_OBSERVATION_SUPPORTED, "true");
        keyMap.put(Repository.OPTION_LIFECYCLE_SUPPORTED, "true");
        keyMap.put(Repository.OPTION_LOCKING_SUPPORTED, "true");
        keyMap.put(Repository.OPTION_NODE_AND_PROPERTY_WITH_SAME_NAME_SUPPORTED, "true");
        keyMap.put(Repository.OPTION_NODE_TYPE_MANAGEMENT_SUPPORTED, "true");
        keyMap.put(Repository.OPTION_OBSERVATION_SUPPORTED, "true");
        keyMap.put(Repository.OPTION_RETENTION_SUPPORTED, "true");
        keyMap.put(Repository.OPTION_SHAREABLE_NODES_SUPPORTED, "true");
        keyMap.put(Repository.OPTION_SIMPLE_VERSIONING_SUPPORTED, "true");
        keyMap.put(Repository.OPTION_TRANSACTIONS_SUPPORTED, "true");
        keyMap.put(Repository.OPTION_UNFILED_CONTENT_SUPPORTED, "true");
        keyMap.put(Repository.OPTION_UPDATE_MIXIN_NODE_TYPES_SUPPORTED, "true");
        keyMap.put(Repository.OPTION_UPDATE_PRIMARY_NODE_TYPE_SUPPORTED, "true");
        keyMap.put(Repository.OPTION_VERSIONING_SUPPORTED, "false");  //TODO support full versioning future
        keyMap.put(Repository.OPTION_WORKSPACE_MANAGEMENT_SUPPORTED, "true");
        keyMap.put(Repository.OPTION_XML_EXPORT_SUPPORTED, "true");
        keyMap.put(Repository.OPTION_XML_IMPORT_SUPPORTED, "true");
        keyMap.put(Repository.QUERY_FULL_TEXT_SEARCH_SUPPORTED, "true");
        keyMap.put(Repository.QUERY_JOINS_INNER, "true");
        keyMap.put(Repository.QUERY_JOINS_INNER_OUTER, "true");
        keyMap.put(Repository.QUERY_JOINS_NONE, "true");
        keyMap.put(Repository.QUERY_LANGUAGES, "true");
        keyMap.put(Repository.QUERY_STORED_QUERIES_SUPPORTED, "true");
        keyMap.put(Repository.REP_NAME_DESC, "");
        keyMap.put(Repository.REP_VENDOR_DESC, "");
        keyMap.put(Repository.REP_VENDOR_URL_DESC, "");
        keyMap.put(Repository.REP_VERSION_DESC, "");
        keyMap.put(Repository.SPEC_NAME_DESC, "");
        keyMap.put(Repository.SPEC_VERSION_DESC, "");
        keyMap.put(Repository.WRITE_SUPPORTED, "");
        keyMap.put(Repository.LEVEL_2_SUPPORTED, "true");

        //Deprecated
        keyMap.put(Repository.LEVEL_1_SUPPORTED, "true");
        keyMap.put(Repository.OPTION_QUERY_SQL_SUPPORTED, "true");
        keyMap.put(Repository.QUERY_XPATH_POS_INDEX, "true");
        keyMap.put(Repository.QUERY_XPATH_DOC_ORDER, "true");
        keyMap.put(Repository.QUERY_JOINS, "true");

        RegistryJCRSpecificStandardLoderUtil.init();
    }

    public Set getWorkspaces() { // will be deprecated
        return workspaces;
    }

    public Map<String, Workspace> getWorkspaceMap() {
        return workspaceMap;
    }

    public String[] getDescriptorKeys() {
        return Arrays.copyOf(discriptorKeys, discriptorKeys.length);
    }

    public boolean isStandardDescriptor(String s) {

        if (keyMap.containsKey(s)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isSingleValueDescriptor(String s) {
        boolean isSingle = true;
        for (String a : multivaluedKeys) {                //check whether this is a valid key
            if (a.equals(s)) {
                isSingle = false;
                break;
            }
        }
        return isSingle;
    }

    public Value getDescriptorValue(String s) {
        return new RegistryValue(keyMap.get(s).
                toString(), PropertyType.BOOLEAN);
    }

    public Value[] getDescriptorValues(String s) {

        String[] multiValKeys = keyMap.get(s).toString().split("-");
        RegistryValue[] values = new RegistryValue[multiValKeys.length];
        for (int i = 0; i < multiValKeys.length; i++) {
            values[i] = new RegistryValue(multiValKeys[i], RegistryPropertyType.BOOLEAN);
        }
        return values;
    }

    public String getDescriptor(String s) {
        String temp = null;
        if (keyMap.get(s) != null) {
            temp = keyMap.get(s).toString();
        }
        return temp;

    }

    public Session login(Credentials credentials, String s) throws LoginException, NoSuchWorkspaceException, RepositoryException {

        //TODO login with a not available workspace name must throw NoSuchWorkspaceException, commented codes are not yet removed as those might be used in future JCR tasks when refactoring login scenario properly
        // Obtain default workspace
        Credentials tmpCredentials = credentials;
        if(credentials instanceof SimpleCredentials) {
        credentials = new RegistrySimpleCredentials();
        ((RegistrySimpleCredentials)credentials).setRegistrySimpleCredentials((SimpleCredentials)tmpCredentials);
        }

        UserRegistry userRegistry = null;
        String userID = "";
        if(credentials == null && s == null) {
            s= RegistryJCRSpecificStandardLoderUtil.getDefaultRegistryWorkspaceName();
            try {
                userRegistry = registryService.getRegistry();
            } catch (RegistryException e) {
                throw new RepositoryException("Exception occurred when obtaining registry " +
                       "from registry service :" + e.getMessage());
            }
        } else if (s == null) {
            s = RegistryJCRSpecificStandardLoderUtil.getDefaultRegistryWorkspaceName();
            userID = ((RegistrySimpleCredentials)credentials).getUserID();
            try {
                if(((RegistrySimpleCredentials)credentials).getUserID().equals(SUPER_USER)) {
                    userRegistry = registryService.getRegistry(ADMIN_ROLE_NAME,
                            new String(((RegistrySimpleCredentials)credentials).getPassword()));
                } else if((((RegistrySimpleCredentials)credentials).getUserID()).equals(ANONYMOUS_USER)) {
                    userRegistry = registryService.getRegistry();
                }
                else {
             userRegistry = getRegistry(((RegistrySimpleCredentials)credentials).getUserID(),
                                   new String(((RegistrySimpleCredentials)credentials).getPassword()));
                }
            } catch (RegistryException e) {
                throw new RepositoryException("Exception occurred when obtaining registry " +
                       "from registry service :" + e.getMessage());
            }
        } else if(credentials == null) {
            try {
                userRegistry = registryService.getRegistry();
            } catch (RegistryException e) {
                throw new RepositoryException("Exception occurred when obtaining registry " +
                       "from registry service :" + e.getMessage());
            }
        } else {
            userID = ((RegistrySimpleCredentials)credentials).getUserID();
            try {
                if(((RegistrySimpleCredentials)credentials).getUserID().equals(SUPER_USER)) {
                    userRegistry = registryService.getRegistry(ADMIN_ROLE_NAME,
                            new String(((RegistrySimpleCredentials)credentials).getPassword()));
                } else {
            userRegistry = getRegistry(((RegistrySimpleCredentials)credentials).getUserID(),
                        new String(((RegistrySimpleCredentials)credentials).getPassword()));
                }
            } catch (RegistryException e) {
                throw new RepositoryException("Exception occurred when obtaining registry " +
                       "from registry service :" + e.getMessage());
            }
        }

        RegistrySession registrySession = null;

        synchronized (this) {
            registrySession = new RegistrySession(this, s,(RegistrySimpleCredentials)credentials, userRegistry,userID);
            workspaces.add(registrySession);
        }


        return registrySession;
    }

     public Session loginOriginal(Credentials credentials, String s) throws LoginException, NoSuchWorkspaceException, RepositoryException {

        //TODO login with a not available workspace name must throw NoSuchWorkspaceException, commented codes are not yet removed as those might be used in future JCR tasks when refactoring login scenario properly
        // Obtain default workspace
        Credentials tmpCredentials = credentials;
        if(credentials instanceof SimpleCredentials) {
        credentials = new RegistrySimpleCredentials();
        ((RegistrySimpleCredentials)credentials).setRegistrySimpleCredentials((SimpleCredentials)tmpCredentials);
        }

        UserRegistry userRegistry = null;
        String userID = "";
        if(credentials == null && s == null) {
            s= RegistryJCRSpecificStandardLoderUtil.getDefaultRegistryWorkspaceName();
            try {
                userRegistry = registryService.getRegistry();
            } catch (RegistryException e) {
                throw new RepositoryException("Exception occurred when obtaining registry " +
                       "from registry service :" + e.getMessage());
            }
        } else if (s == null) {
            s = RegistryJCRSpecificStandardLoderUtil.getDefaultRegistryWorkspaceName();
            userID = ((RegistrySimpleCredentials)credentials).getUserID();
            try {
//                if(((RegistrySimpleCredentials)credentials).getUserID().equals(SUPER_USER)) {
//                    userRegistry = registryService.getRegistry(ADMIN_ROLE_NAME,
//                            new String(((RegistrySimpleCredentials)credentials).getPassword()));
//                } else if((((RegistrySimpleCredentials)credentials).getUserID()).equals(ANONYMOUS_USER)) {
//                    userRegistry = registryService.getRegistry();
//                }
//                else {
             userRegistry = getRegistry(((RegistrySimpleCredentials)credentials).getUserID(),
                                   new String(((RegistrySimpleCredentials)credentials).getPassword()));
//                }
            } catch (RegistryException e) {
                throw new RepositoryException("Exception occurred when obtaining registry " +
                       "from registry service :" + e.getMessage());
            }
        } else if(credentials == null) {
            try {
                userRegistry = registryService.getRegistry();
            } catch (RegistryException e) {
                throw new RepositoryException("Exception occurred when obtaining registry " +
                       "from registry service :" + e.getMessage());
            }
        } else {
            userID = ((RegistrySimpleCredentials)credentials).getUserID();
            try {
//                if(((RegistrySimpleCredentials)credentials).getUserID().equals(SUPER_USER)) {
//                    userRegistry = registryService.getRegistry(ADMIN_ROLE_NAME,
//                            new String(((RegistrySimpleCredentials)credentials).getPassword()));
//                } else {
            userRegistry = getRegistry(((RegistrySimpleCredentials)credentials).getUserID(),
                        new String(((RegistrySimpleCredentials)credentials).getPassword()));
//                }
            } catch (RegistryException e) {
                throw new RepositoryException("Exception occurred when obtaining registry " +
                       "from registry service :" + e.getMessage());
            }
        }

        RegistrySession registrySession = null;

        synchronized (this) {
            registrySession = new RegistrySession(this, s,(RegistrySimpleCredentials)credentials, userRegistry,userID);
            workspaces.add(registrySession);
        }


        return registrySession;
    }

    public Session login(Credentials credentials) throws LoginException, RepositoryException {
       return login(credentials,null);
    }

  /**
     *Here this login is used when ,we need to log to a workspace which is created as Workspace.createWorkspace
     */
    public Session login(String s) throws LoginException, NoSuchWorkspaceException, RepositoryException {
     return login(null,s);
    }

    public Session login() throws LoginException, RepositoryException {
        return login(null,null);
    }

    private UserRegistry getRegistry(String username, String password) throws RegistryException {
       if (username == null) {
           return registryService.getRegistry();
       }
       String[] nameComponents = username.split("@");
       if (nameComponents.length > 1) {
           return registryService.getUserRegistry(username, password);
       } else {
           return registryService.getRegistry(username, password);
       }
   }

}
