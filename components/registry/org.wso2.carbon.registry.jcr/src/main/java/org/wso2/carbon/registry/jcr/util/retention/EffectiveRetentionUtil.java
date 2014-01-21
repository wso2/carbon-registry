package org.wso2.carbon.registry.jcr.util.retention;

import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.jcr.RegistrySession;
import org.wso2.carbon.registry.jcr.retention.RegistryHold;
import org.wso2.carbon.registry.jcr.retention.RegistryRetentionPolicy;

import javax.jcr.RepositoryException;
import javax.jcr.retention.Hold;
import javax.jcr.retention.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

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
public class EffectiveRetentionUtil {

    public static final String WRITE_LOCKED = "writeLocked";
    public static final String DELETE_LOCKED = "deleteLocked";
    public static final String JCR_FULL_LOCKED = "fullLocked";
    public static final String JCR_FULL_HOLD = "fullHold";


    public static boolean checkEffectiveRetentionPolicyFullLocked(RegistrySession session, String path) throws RegistryException {
        try {
            Resource resource = session.getUserRegistry().get(path);
            String retention = resource.getProperty("org.wso2.carbon.registry.jcr.retention.policy");
            if (retention == null) {
                return false;
            } else if (retention.equalsIgnoreCase(JCR_FULL_LOCKED)) {
                return true;
            } else {
                return false;
            }

        } catch (RegistryException e) {
            throw new RegistryException("Registry level exception occurred while acquiring a retention lock on " + path);
        }
    }

    public static boolean checkEffectiveRetentionHoldFullLocked(RegistrySession session, String path) throws RegistryException, RepositoryException {
            for (Hold h : getHoldsFromRegistry(session, path)) {
                if (h.getName().equals(JCR_FULL_HOLD)) {
                     return true;
                    //TODO Should check deep lock
                }
            }
            return hasDeepHoldParent(session,path);
    }

    private static boolean hasDeepHoldParent(RegistrySession session, String path) throws RepositoryException {
        try {
            String tmp = session.getUserRegistry().get(path).getParentPath();
            if(!tmp.endsWith("/")) {
               tmp = tmp + "/";
            }
            while(tmp.contains(session.getWorkspaceRootPath()) && !tmp.equals(session.getWorkspaceRootPath())){
                for( Hold hold : getHoldsFromRegistry(session,tmp)){
                  if(hold.isDeep()){
                   return true;
                  }
                }
                tmp = session.getUserRegistry().get(tmp).getParentPath();
                if(!tmp.endsWith("/")) {
                   tmp = tmp + "/";
                }
            }

        } catch (RegistryException e) {
            throw new RepositoryException("Registry level exception occurred while " +
                    "acquiring prent retention holds " + path);
        }
      return false;
    }

    public static boolean checkEffectiveWriteLocked(RegistrySession session, String path) throws RegistryException {
        try {
            Resource resource = session.getUserRegistry().get(path);
            String retention = resource.getProperty("org.wso2.carbon.registry.jcr.retention.policy");
            if (retention == null) {
                return false;
            } else if (retention.equalsIgnoreCase(WRITE_LOCKED)) {
                return true;
            } else {
                return false;
            }

        } catch (RegistryException e) {
            throw new RegistryException("Registry level exception occurred while acquiring a retention lock on " + path);
        }

    }

    public static boolean checkEffectiveDeleteLocked(RegistrySession session, String path) throws RegistryException {
        try {
            Resource resource = session.getUserRegistry().get(path);
            String retention = resource.getProperty("org.wso2.carbon.registry.jcr.retention.policy");
            if (retention == null) {
                return false;
            } else if (retention.equalsIgnoreCase(DELETE_LOCKED)) {
                return true;
            } else {
                return false;
            }

        } catch (RegistryException e) {
            throw new RegistryException("Registry level exception occurred while acquiring a retention lock on " + path);
        }

    }

    public static void setRetentionPolicyToRegistry(RegistrySession session, String s, RetentionPolicy retentionPolicy) throws RepositoryException {
        Resource resource = null;
        try {
            resource = session.getUserRegistry().get(s);
            resource.setProperty("org.wso2.carbon.registry.jcr.retention.policy", retentionPolicy.getName());
            session.getUserRegistry().put(s, resource);
        } catch (RegistryException e) {
            throw new RepositoryException("Registry level exception when setting retention policy at " + s);
        }
    }

    public static void removeRetentionPolicyFromRegistry(RegistrySession session, String s) throws RepositoryException {
        Resource resource = null;
        try {
            resource = session.getUserRegistry().get(s);
            resource.removeProperty("org.wso2.carbon.registry.jcr.retention.policy");
            session.getUserRegistry().put(s, resource);
        } catch (RegistryException e) {
            throw new RepositoryException("Registry level exception when setting retention policy at " + s);
        }
    }


    public static RetentionPolicy getRetentionPolicyFromRegistry(RegistrySession session, String s) throws RepositoryException {
        Resource resource = null;
        try {
            resource = session.getUserRegistry().get(s);
        } catch (RegistryException e) {
            throw new RepositoryException("Registry level exception when setting retention policy at " + s);
        }
        String name = resource.getProperty("org.wso2.carbon.registry.jcr.retention.policy");
        if (name != null) {
            return new RegistryRetentionPolicy(name);
        } else {
            return null;
        }
    }

    public static Hold addHoldsToRegistry(RegistrySession session, String s, String s1, boolean b) throws RepositoryException {
        Resource resource = null;
        try {
            resource = session.getUserRegistry().get(s);
            if (resource.getPropertyValues("org.wso2.carbon.registry.jcr.retention.holds") == null) {
                List list = new ArrayList();
                list.add(s1 + ";" + String.valueOf(b));
                resource.setProperty("org.wso2.carbon.registry.jcr.retention.holds", list);
            } else {
                resource.getPropertyValues("org.wso2.carbon.registry.jcr.retention.holds").
                        add(s1 + ";" + String.valueOf(b));
            }
            session.getUserRegistry().put(s, resource);
        } catch (RegistryException e) {
            throw new RepositoryException("Registry level exception when setting retention policy at " + s);
        }

        return new RegistryHold(s1, b);

    }

    public static Hold[] getHoldsFromRegistry(RegistrySession session, String s) throws RepositoryException {
        Resource resource = null;
        List<Hold> holdList = new ArrayList<Hold>();
        try {
            resource = session.getUserRegistry().get(s);
            List holds = resource.getPropertyValues("org.wso2.carbon.registry.jcr.retention.holds");
            if (holds != null) {
                for (Object hold : holds) {
                    String[] vals = hold.toString().split(";");
                    holdList.add(new RegistryHold(vals[0], Boolean.valueOf(vals[1])));
                }
            }
        } catch (RegistryException e) {
            throw new RepositoryException("Registry level exception when setting retention policy at " + s);
        }

        return holdList.toArray(new RegistryHold[0]);
    }

    public static void removeHoldFromRegistry(RegistrySession session, String s, Hold hold) throws RepositoryException {
        Resource resource = null;
        try {
            resource = session.getUserRegistry().get(s);
            List holds = resource.getPropertyValues("org.wso2.carbon.registry.jcr.retention.holds");
            List<Hold> holdList = new ArrayList<Hold>();
            String refHold = hold.getName() + ";" + hold.isDeep();
            if (holds != null) {
                for (Object _hold : holds) {
                    if (_hold.equals(refHold)) {
                        resource.getPropertyValues("org.wso2.carbon.registry.jcr.retention.holds").remove(_hold);
                    }
                }
            }
            session.getUserRegistry().put(s, resource);

        } catch (RegistryException e) {
            throw new RepositoryException("Registry level exception when setting retention policy at " + s);
        }

    }


}
