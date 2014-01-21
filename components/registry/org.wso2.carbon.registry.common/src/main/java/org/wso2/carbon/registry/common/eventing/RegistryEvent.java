/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.common.eventing;

import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.NetworkUtils;

import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class RegistryEvent<T> {

    public static final String TOPIC_SEPARATOR = "/";
    public static final String TOPIC_PREFIX = "/registry/notifications";
    public static final java.lang.String REGISTRY_EVENT_NS =
            "http://wso2.org/ns/2011/01/eventing/registry/event";

    private String topic;
    private T message;

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public static enum ResourceType {
        COLLECTION,
        RESOURCE,
        @SuppressWarnings("unused")
        UNKNOWN

    }

    private Map<String, String> parameters = new LinkedHashMap<String, String>();
    private int tenantId = -1;
    private String timestamp;
    private RegistrySession registrySessionDetails = new RegistrySession();
    private Context contextDetails = new Context();
    private Operation operationDetails = new Operation();
    private static Server serverDetails = new Server();

    public static final class RegistrySession {
        private String username = null;
        private int tenantId = -1;
        private String chroot;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public int getTenantId() {
            return tenantId;
        }

        public void setTenantId(int tenantId) {
            this.tenantId = tenantId;
        }

        public String getChroot() {
            return chroot;
        }

        public void setChroot(String chroot) {
            this.chroot = chroot;
        }
    }

    public static final class Context {
        private String username = null;
        private int tenantId = -1;

        public int getTenantId() {
            return tenantId;
        }

        public void setTenantId(int tenantId) {
            this.tenantId = tenantId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }

    public static final class Operation {
        private String path = null;
        private String eventType = null;
        private String resourceType = null;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getEventType() {
            return eventType;
        }

        public void setEventType(String eventType) {
            this.eventType = eventType;
        }

        public String getResourceType() {
            return resourceType;
        }

        public void setResourceType(String resourceType) {
            this.resourceType = resourceType;
        }
    }

    public static final class Server {
        private String productName = null;
        private String productVersion = null;
        private String hostIPAddress = null;
        private OperatingSystem osDetails = new OperatingSystem();
        private OperatingSystemUser osUser = new OperatingSystemUser();
        private JVM jvmDetails = new JVM();

        public static final class OperatingSystem {
            private String operatingSystemName = null;
            private String operatingSystemVersion = null;
            private String operatingSystemArchitecture = null;

            public String getOperatingSystemName() {
                return operatingSystemName;
            }

            public void setOperatingSystemName(String operatingSystemName) {
                this.operatingSystemName = operatingSystemName;
            }

            public String getOperatingSystemVersion() {
                return operatingSystemVersion;
            }

            public void setOperatingSystemVersion(String operatingSystemVersion) {
                this.operatingSystemVersion = operatingSystemVersion;
            }

            public String getOperatingSystemArchitecture() {
                return operatingSystemArchitecture;
            }

            public void setOperatingSystemArchitecture(String operatingSystemArchitecture) {
                this.operatingSystemArchitecture = operatingSystemArchitecture;
            }
        }

        public static final class JVM {
            private String javaVersion = null;
            private String javaVMName = null;
            private String javaVMVersion = null;
            private String javaVendor = null;

            public String getJavaVersion() {
                return javaVersion;
            }

            public void setJavaVersion(String javaVersion) {
                this.javaVersion = javaVersion;
            }

            public String getJavaVMName() {
                return javaVMName;
            }

            public void setJavaVMName(String javaVMName) {
                this.javaVMName = javaVMName;
            }

            public String getJavaVMVersion() {
                return javaVMVersion;
            }

            public void setJavaVMVersion(String javaVMVersion) {
                this.javaVMVersion = javaVMVersion;
            }

            public String getJavaVendor() {
                return javaVendor;
            }

            public void setJavaVendor(String javaVendor) {
                this.javaVendor = javaVendor;
            }
        }

        public static final class OperatingSystemUser {
            private String username = null;
            private String language = null;
            private String country = null;
            private String timezone = null;

            public String getUsername() {
                return username;
            }

            public void setUsername(String username) {
                this.username = username;
            }

            public String getLanguage() {
                return language;
            }

            public void setLanguage(String language) {
                this.language = language;
            }

            public String getCountry() {
                return country;
            }

            public void setCountry(String country) {
                this.country = country;
            }

            public String getTimezone() {
                return timezone;
            }

            public void setTimezone(String timezone) {
                this.timezone = timezone;
            }
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public String getProductVersion() {
            return productVersion;
        }

        public void setProductVersion(String productVersion) {
            this.productVersion = productVersion;
        }

        public String getHostIPAddress() {
            return hostIPAddress;
        }

        public void setHostIPAddress(String hostIPAddress) {
            this.hostIPAddress = hostIPAddress;
        }

        public OperatingSystem getOSDetails() {
            return osDetails;
        }

        public OperatingSystemUser getOSUser() {
            return osUser;
        }

        public JVM getJVMDetails() {
            return jvmDetails;
        }
    }

    static {
        try {
            serverDetails.setHostIPAddress(NetworkUtils.getLocalHostname());
        } catch (SocketException ignored) {
            serverDetails.setHostIPAddress("unknown");
        }
        serverDetails.setProductName(CarbonUtils.getServerConfiguration().getFirstProperty("Name"));
        serverDetails.setProductVersion(
                CarbonUtils.getServerConfiguration().getFirstProperty("Version"));

        // JVM Details
        serverDetails.getJVMDetails().setJavaVersion(System.getProperty("java.version"));
        serverDetails.getJVMDetails().setJavaVMName(System.getProperty("java.vm.name"));
        serverDetails.getJVMDetails().setJavaVMVersion(System.getProperty("java.vm.version"));
        serverDetails.getJVMDetails().setJavaVendor(System.getProperty("java.vendor"));

        // System User Details
        serverDetails.getOSUser().setUsername(System.getProperty("user.name"));
        serverDetails.getOSUser().setCountry(System.getProperty("user.country"));
        serverDetails.getOSUser().setLanguage(System.getProperty("user.language"));
        serverDetails.getOSUser().setTimezone(System.getProperty("user.timezone"));

        // System Details
        serverDetails.getOSDetails().setOperatingSystemName(System.getProperty("os.name"));
        serverDetails.getOSDetails().setOperatingSystemVersion(System.getProperty("os.version"));
        serverDetails.getOSDetails().setOperatingSystemArchitecture(System.getProperty("os.arch"));
    }

    public RegistryEvent() {
        this(null);
    }

    /**
     * Construct the Registry Event by using the message
     * @param message any Object
     */
    public RegistryEvent(T message) {
        this.message = message;
        Date date = new Date();
        this.timestamp = DATE_FORMAT.format(date);
        CarbonContext carbonContext = CarbonContext.getThreadLocalCarbonContext();
        this.contextDetails.setUsername(carbonContext.getUsername());
        this.contextDetails.setTenantId(carbonContext.getTenantId());
        this.registrySessionDetails.setChroot(CurrentSession.getChroot());
        this.registrySessionDetails.setUsername(CurrentSession.getUser());
        this.registrySessionDetails.setTenantId(CurrentSession.getTenantId());
    }

    public void setOperationDetails(String path, String eventType,
                                    ResourceType resourceType) {
        if (path != null) {
            this.operationDetails.setPath(path);
        } else {
            this.operationDetails.setPath("unknown");
        }
        this.operationDetails.setEventType(eventType);
        switch (resourceType) {
            case RESOURCE:
                this.operationDetails.setResourceType("resource");
                break;
            case COLLECTION:
                this.operationDetails.setResourceType("collection");
                break;
            default:
                this.operationDetails.setResourceType("unknown");
        }
    }

    public int getTenantId() {
        return tenantId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public RegistrySession getRegistrySessionDetails() {
        return registrySessionDetails;
    }

    public Context getContextDetails() {
        return contextDetails;
    }

    public Operation getOperationDetails() {
        return operationDetails;
    }

    public static Server getServerDetails() {
        return serverDetails;
    }

    public void setParameter(String key, String value) {
        this.parameters.put(key, value);
    }

    public Map<String, String> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public void setTopic(String topic) {
        if (!topic.startsWith(TOPIC_PREFIX)) {
            this.topic = TOPIC_PREFIX + topic;
        } else {
            this.topic = topic;
        }
    }

    public T getMessage() {
        return message;
    }

    public void setMessage(T message) {
        this.message = message;
    }

    public String getTopic() {
        return topic;
    }
}
