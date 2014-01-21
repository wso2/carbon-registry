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

package org.wso2.carbon.registry.relations.ui.clients;


import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.relations.stub.RelationAdminServiceStub;
import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationBean;
import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationTreeBean;
import org.wso2.carbon.registry.relations.stub.beans.xsd.DependenciesBean;
import org.wso2.carbon.ui.CarbonUIUtil;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class RelationServiceClient {

    private static final Log log = LogFactory.getLog(RelationServiceClient.class);

    private RelationAdminServiceStub stub;
    private String epr;

    public RelationServiceClient(String cookie, ServletConfig config, HttpSession session)
            throws RegistryException {

        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.
                getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        epr = backendServerURL + "RelationAdminService";

        try {
            stub = new RelationAdminServiceStub(configContext, epr);

            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        } catch (AxisFault axisFault) {
            String msg = "Failed to initiate comment service client. " + axisFault.getMessage();
            log.error(msg, axisFault);
            throw new RegistryException(msg, axisFault);
        }
    }

    public DependenciesBean getDependencies(HttpServletRequest request) throws Exception {

        String path = request.getParameter("path");

        DependenciesBean bean = null;
        try {
            bean = stub.getDependencies(path);
            if (bean.getAssociationBeans() == null) {
                bean.setAssociationBeans(new AssociationBean[0]);
            }
        } catch (Exception e) {
            String msg = "Failed to get associations from the service. " +
                    e.getMessage();
            log.error(msg, e);
            throw new Exception(msg);
        }

        return bean;
    }

    public void addAssociation(HttpServletRequest request) throws Exception {
        String path = request.getParameter("path");
        String type = request.getParameter("type");
        String associationPaths = request.getParameter("associationPaths");
        String todo = request.getParameter("todo");

        try{
            stub.addAssociation(path, type, associationPaths, todo);
        } catch (Exception e) {
            String msg = "Failed to add associations. " +
                    e.getMessage();
            log.error(msg, e);
            throw new Exception(msg);
        }
    }

    public AssociationTreeBean getAssociationTree(HttpServletRequest request) throws Exception {
        String path = request.getParameter("path");
        String type = request.getParameter("type");
        AssociationTreeBean bean = null;

        try{
             bean = stub.getAssociationTree(path, type);
        } catch(Exception e) {
            String msg = "Failed to get association tree. " +
                    e.getMessage();
            log.error(msg, e);
            throw new Exception(msg);
        }
        return bean;
    }
}
