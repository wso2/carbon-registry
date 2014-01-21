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

package org.wso2.carbon.registry.extensions.test.utils;

import org.apache.abdera.protocol.server.servlet.AbderaServlet;
import org.apache.abdera.protocol.server.ServiceManager;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.app.RegistryProvider;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.registry.core.jdbc.realm.InMemoryRealmService;

/**
 * A very simple embeddable Registry server - handles the Registry wire protocol (APP) but has no UI
 * support built in.  Uses Jetty internally.
 */
public class RegistryServer {
    int port = 8081;
    String baseURL = "/wso2registry";
    //    Registry registry;
    Server server;

    public RegistryServer() {
    }

    public RegistryServer(int port) {
        this.port = port;
    }

    public RegistryServer(int port, String baseURL) {
        this.port = port;
        this.baseURL = baseURL;
    }

    public static void main(String[] args) throws Exception {
        int port = 8081;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        RegistryServer s = new RegistryServer(port);
        s.start();
    }

    public void start() throws Exception {

        RealmService realmService = new InMemoryRealmService();
        RegistryContext regContext = RegistryContext.getBaseInstance(realmService);
        regContext.selectDBConfig("in-memory");
        //RegistryContext.setSingleton(regContext);

        server = new Server(port);
        Context context = new Context(server, null/*RegistryProvider.baseURI*/, Context.SESSIONS);
        ServletHolder servletHolder = new ServletHolder(new AbderaServlet());
        servletHolder.setInitParameter(ServiceManager.PROVIDER,
                                       RegistryProvider.class.getName());
        context.addServlet(servletHolder, "/*");
        server.start();

//        root.addServlet(new ServletHolder(new RegistryServlet()), "/resources/*");
//        root.addServlet(new ServletHolder(new AbderaServlet()), "/atom/*");
//        server.start();
    }

    public void stop() throws Exception {
        if (server != null) server.stop();
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }
}

