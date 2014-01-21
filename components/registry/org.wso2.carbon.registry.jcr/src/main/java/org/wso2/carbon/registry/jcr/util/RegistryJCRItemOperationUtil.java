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

import org.apache.commons.ssl.asn1.Strings;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.jcr.RegistryProperty;
import org.wso2.carbon.registry.jcr.RegistrySession;
import org.wso2.carbon.registry.jcr.RegistryValue;
import org.wso2.carbon.registry.jcr.retention.RegistryRetentionManager;
import org.wso2.carbon.registry.jcr.util.retention.EffectiveRetentionUtil;

import javax.jcr.*;
import javax.jcr.retention.Hold;
import javax.jcr.retention.RetentionPolicy;
import java.math.BigDecimal;
import java.util.*;

public class RegistryJCRItemOperationUtil {

    public static String replaceNameSpacePrefixURIS(String path) {
        for (String uri : RegistryJCRSpecificStandardLoderUtil.getJCRSystemNameSpaceURIList()) {
            path = path.replace("{" + uri + "}", RegistryJCRSpecificStandardLoderUtil.
                    getJCRSystemNameSpacePrefxMap().get(uri) + ":");
        }
        return path;
    }

    public static String getAncestorPathAtGivenDepth(RegistrySession registrySession,String path, int depth) throws ItemNotFoundException {
        String[] tmp = path.split("/");
        if ((depth > tmp.length - 4) || depth < 0) {
            throw new ItemNotFoundException("No such Ancestor exists. 0 < depth < n violated");
        }

        if (depth == 0) {
            return registrySession.getWorkspaceRootPath();
        } else {
            int index = nthOccurrence(path, '/', depth);
            return path.substring(0, index);
        }
    }

    private static int nthOccurrence(String str, char c, int n) {
        str = str + "/";
        int pos = str.indexOf(c, 0);
        while (n-- > 0 && pos != -1)
            pos = str.indexOf(c, pos + 1);
        return pos;
    }

    public static void validateReadOnlyItemOpr(RegistrySession registrySession) throws AccessDeniedException {
        if (RegistryJCRSpecificStandardLoderUtil.isSessionReadOnly(registrySession.getUserID())) {
            throw new AccessDeniedException("A read only session must not be allowed to modify a property value");
        }
    }

    public static void validateSessionSaved(RegistrySession registrySession) throws InvalidItemStateException {
        if (!registrySession.isSessionSaved()) {
            throw new InvalidItemStateException("Tried to access unsaved operations : Invalid item state");
        }

    }

    public static void validateSystemConfigPath(String path) throws RepositoryException {
        if (path == null) {
            throw new RepositoryException("Null is an invalid path expression");
        }

        if (path.contains(RegistryJCRSpecificStandardLoderUtil.JCR_SYSTEM_CONFIG)) {
            throw new RepositoryException(RegistryJCRSpecificStandardLoderUtil.
                    JCR_SYSTEM_CONFIG + " cannot be included in a path,it is a system specific config node");
        }

    }

    public static boolean isSystemConfigNode(String path) throws RepositoryException {
        if (path == null) {
            throw new RepositoryException("Null is an invalid path expression");
        }

        if (path.contains(RegistryJCRSpecificStandardLoderUtil.JCR_SYSTEM_CONFIG)) {
            return true;
        } else {
            return false;
        }

    }

    public static String getNodePathFromVersionName(String s) {
        if (!s.contains("/")) {
            return s;
        }
        return s.substring(0, s.lastIndexOf("/"))
                + "/"
                + s.split("/")[s.split("/").length - 1].split(";")[0];

    }

    public static RegistryProperty getRegistryProperty(String s, String property,
                                                       ResourceImpl res, String propQName, RegistrySession session) throws RepositoryException {
        Object prop = null;
        RegistryProperty regProp = null;

        if (s != null) {

            if (s.equals("boolean")) {
                regProp = new RegistryProperty(res.getPath(), session, propQName, Boolean.valueOf(property).booleanValue());
            } else if (s.equals("long")) {
                regProp = new RegistryProperty(res.getPath(), session, propQName, Long.valueOf(property).longValue());
            } else if (s.equals("double")) {
                regProp = new RegistryProperty(res.getPath(), session, propQName, Double.valueOf(property).longValue());
            } else if (s.equals("big_decimal")) {
                regProp = new RegistryProperty(res.getPath(), session, propQName, new BigDecimal(property));
            } else if (s.equals("value_type")) {
                regProp = new RegistryProperty(res.getPath(), session, propQName, new RegistryValue(property));
            } else if (s.equals("calendar")) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(Long.valueOf(property));
                regProp = new RegistryProperty(res.getPath(), session, propQName, new RegistryValue(cal));
            }
        }

        return regProp;
    }

    public static boolean isValidJCRName(String s) {
        if (s == null ||
                s.contains("*"))
        // TODO  add more to validate JCR names
        {
            return false;
        } else {
            return true;
        }
    }

    public static void persistPendingChanges(RegistrySession registrySession) throws RepositoryException {
        persistRententionPolicies(registrySession);
        persistRetentionHolds(registrySession);

    }

    private static void persistRetentionHolds(RegistrySession registrySession) throws RepositoryException {

        Map<String, Set<Hold>> pending = ((RegistryRetentionManager) registrySession.getRetentionManager()).
                getPendingRetentionHolds();
        for (String s : pending.keySet()) {
            Set<Hold> holds = pending.get(s);
            for (Hold h : holds) {
                EffectiveRetentionUtil.addHoldsToRegistry(registrySession, s, h.getName(), h.isDeep());
            }
        }
        ((RegistryRetentionManager) registrySession.getRetentionManager()).
                getPendingRetentionHolds().clear();
    }

    private static void persistRententionPolicies(RegistrySession registrySession) throws RepositoryException {
        Map<String, RetentionPolicy> pending = ((RegistryRetentionManager) registrySession.getRetentionManager()).
                getPendingRetentionPolicies();
        for (String s : pending.keySet()) {
            EffectiveRetentionUtil.setRetentionPolicyToRegistry(registrySession, s, pending.get(s));
        }
        ((RegistryRetentionManager) registrySession.getRetentionManager()).
                getPendingRetentionPolicies().clear();

//       Removing pending policies
        for(String s:((RegistryRetentionManager) registrySession.getRetentionManager()).getPendingPolicyRemoveList()){
          EffectiveRetentionUtil.removeRetentionPolicyFromRegistry(registrySession,s);
        }
       ((RegistryRetentionManager) registrySession.getRetentionManager()).
               getPendingPolicyRemoveList().clear();

    }

    public static Property persistStringPropertyValues(RegistrySession registrySession, String nodePath, String s, String[] strings) throws RepositoryException {
        boolean allNullInSide = true;
        List<String> properties = new ArrayList<String>();
        Property property;
        Resource resource;
        try {
            resource = (CollectionImpl) registrySession.getUserRegistry().get(nodePath);

            if (strings != null) {
                for (String val : strings) {

                    if (val != null) {
                        properties.add(val);
                        allNullInSide = false;
                    }
                }

                // if all values inside are null, returns a empty string
                if (allNullInSide) {

                    resource.setProperty(s, "");
                    registrySession.getUserRegistry().put(nodePath, resource);
                    property = new RegistryProperty(resource.getPath(), registrySession, s, new String[0]);
                    return property;
                } else {
                    resource.setProperty(s, properties);
                }
                registrySession.getUserRegistry().put(nodePath, resource);

            } else {
                resource.removeProperty(s);
                registrySession.getUserRegistry().put(nodePath, resource);
            }

        } catch (RegistryException e) {
            String msg = "failed to resolve the path of the given node or violation of repository syntax ";
            throw new RepositoryException(msg, e);
        }
        property = new RegistryProperty(resource.getPath(), registrySession, s, properties.toArray(new String[0]));
        return property;
    }

    public static void checkRetentionPolicyWithParent(RegistrySession session, String path) throws RepositoryException {

        try {
            if (EffectiveRetentionUtil.checkEffectiveRetentionPolicyFullLocked(
                    session, session.getUserRegistry().get(path).getParentPath())) {
                throw new RepositoryException("Cannot remove a node under retention policy" + path);
            }
            if (EffectiveRetentionUtil.checkEffectiveRetentionPolicyFullLocked(session, path)) {
                throw new RepositoryException("Cannot remove a node under retention policy" + path);
            }
        } catch (RegistryException e) {
            throw new RepositoryException("Cannot remove a node under retention policy" + path);
        }

    }

    public static void checkRetentionPolicy(RegistrySession session, String path) throws RepositoryException {
        try {
            if (EffectiveRetentionUtil.checkEffectiveRetentionPolicyFullLocked(
                    session, path)) {
                throw new RepositoryException("Cannot remove a node under retention policy" + path);
            }
        } catch (RegistryException e) {
            throw new RepositoryException("Cannot remove a node under retention policy" + path);
        }
    }

        public static void checkRetentionHoldWithParent(RegistrySession session, String path) throws RepositoryException {

        try {
            if (EffectiveRetentionUtil.checkEffectiveRetentionHoldFullLocked(
                    session, session.getUserRegistry().get(path).getParentPath())) {
                throw new RepositoryException("Cannot remove a node under retention hold" + path);
            }
            if (EffectiveRetentionUtil.checkEffectiveRetentionHoldFullLocked(session, path)) {
                throw new RepositoryException("Cannot remove a node under retention hold" + path);
            }
        } catch (RegistryException e) {
            throw new RepositoryException("Cannot remove a node under retention hold" + path);
        }

    }

    public static void checkRetentionHold(RegistrySession session, String path) throws RepositoryException {
        try {
            if (EffectiveRetentionUtil.checkEffectiveRetentionHoldFullLocked(
                    session, path)) {
                throw new RepositoryException("Cannot remove a node under retention hold" + path);
            }
        } catch (RegistryException e) {
            throw new RepositoryException("Cannot remove a node under retention hold" + path);
        }
    }

     public static boolean isWorkspaceExists(RegistrySession registrySession,String s){
        Set sessions = registrySession.getRepository().getWorkspaces();
        boolean matchFound = false;
           for(Object ss:sessions) {
               RegistrySession session = (RegistrySession)ss;
                   if (session.getWorkspaceName() != null) {
                       if (session.getWorkspaceName().equals(s)) {
                           matchFound = true;
                       }
                   }
          }
     return matchFound;
    }

}
