/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.info.services.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.Utils;
import org.wso2.carbon.registry.info.services.InfoService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.NetworkUtils;

import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;

public class ResourceUtil {

    private static final Log log = LogFactory.getLog(InfoService.class);

    /**
     * Check a resource is locally mounted.
     *
     * @param realPath
     * @return
     * @throws RegistryException
     */
    public static boolean isLocalMount(String realPath) throws RegistryException {
        try {
            URL realPathUrl = new URL(realPath);
            log.debug("Real path URL: " + realPathUrl.toString());

            String host = findServerHost();
            int port = findTransportPort(realPathUrl.getProtocol());
            return (realPathUrl.getHost().equals(host) && (realPathUrl.getPort() == port));
        } catch (MalformedURLException e) {
            throw new RegistryException(String.format("Malformed real path URL found: %s", realPath), e);
        }
    }

    /**
     * Find hostname of this server instance.
     *
     * @return
     * @throws RegistryException
     */
    private static String findServerHost() throws RegistryException {
        String host;
        try {
            host = NetworkUtils.getLocalHostname();
        } catch (SocketException e) {
            host = null;
            log.warn("An error occured while determining server host", e);
        }
        if (host == null) {
            host = System.getProperty("carbon.local.ip");
            log.warn("Unable to obtain server host, using the carbon.local.ip system "
                    + "property to determine the ip address");
        }
        if (host == null) {
            throw new RegistryException("Unable to find server host");
        }
        log.debug("Found server host: " + host);
        return host;
    }

    /**
     * Find port of a given transport.
     *
     * @param protocol
     * @return
     * @throws RegistryException
     */
    private static int findTransportPort(String protocol) throws RegistryException {
        String port = null;
        try {
            port = Integer.toString(CarbonUtils.getTransportPort(Utils.getConfigurationContext(), protocol));
        } catch (Exception e) {
            port = null;
            log.warn(String.format("Unable to get %s port from server Axis configuration, using carbon.%s.port" +
                    " to determine the %s port", protocol, protocol, protocol));
        }
        if (port == null) {
            port = System.getProperty(String.format("carbon.%s.port", protocol));
        }
        if (port == null) {
            throw new RegistryException(String.format("Unable to find %s port", protocol));
        }
        log.debug(String.format("Found %s port: %s", protocol, port));
        return Integer.parseInt(port);
    }
}
