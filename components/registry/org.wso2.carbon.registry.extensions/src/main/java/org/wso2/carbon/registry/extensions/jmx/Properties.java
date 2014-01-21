/*
 *  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.wso2.carbon.registry.extensions.jmx;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Properties implements PropertiesMBean {

    private static final Log log = LogFactory.getLog(Activities.class);

    private Registry registry;

    public Properties(Registry registry) {
        this.registry = registry;
    }

    public String[] getProperties(String path) {
        List<String> output = new LinkedList<String>();
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            java.util.Properties properties = registry.get(path).getProperties();
            for (Map.Entry<Object, Object> e : properties.entrySet()) {
                output.add(e.getKey() + ":" + e.getValue());
            }
        } catch (RegistryException e) {
            String msg = "Unable to fetch all properties.";
            log.error(msg, e);
            // we are unable to throw a customized exception or an exception with the cause or if
            // not, JConsole needs additional Jars to marshall exceptions.
            throw new RuntimeException(Utils.buildMessageForRuntimeException(e, msg));
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return output.toArray(new String[output.size()]);
    }

    public String getProperty(String path, String key) {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            return registry.get(path).getProperty(key);
        } catch (RegistryException e) {
            String msg = "Unable to fetch property value.";
            log.error(msg, e);
            // we are unable to throw a customized exception or an exception with the cause or if
            // not, JConsole needs additional Jars to marshall exceptions.
            throw new RuntimeException(Utils.buildMessageForRuntimeException(e, msg));
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public void setProperty(String path, String key, String value) {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            Resource resource = registry.get(path);
            resource.setProperty(key, value);
            registry.put(path, resource);
        } catch (RegistryException e) {
            String msg = "Unable to set property value.";
            log.error(msg, e);
            // we are unable to throw a customized exception or an exception with the cause or if
            // not, JConsole needs additional Jars to marshall exceptions.
            throw new RuntimeException(Utils.buildMessageForRuntimeException(e, msg));
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public void removeProperty(String path, String key) {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            Resource resource = registry.get(path);
            resource.removeProperty(key);
            registry.put(path, resource);
        } catch (RegistryException e) {
            String msg = "Unable to remove property.";
            log.error(msg, e);
            // we are unable to throw a customized exception or an exception with the cause or if
            // not, JConsole needs additional Jars to marshall exceptions.
            throw new RuntimeException(Utils.buildMessageForRuntimeException(e, msg));
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }
}
