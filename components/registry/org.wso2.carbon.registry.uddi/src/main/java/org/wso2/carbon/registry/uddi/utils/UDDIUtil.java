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

package org.wso2.carbon.registry.uddi.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.juddi.api.impl.JUDDIApiImpl;
import org.apache.juddi.api.impl.UDDIInquiryImpl;
import org.apache.juddi.api.impl.UDDIPublicationImpl;
import org.apache.juddi.api.impl.UDDISecurityImpl;
import org.apache.juddi.api_v3.Publisher;
import org.apache.juddi.api_v3.SavePublisher;
import org.uddi.api_v3.*;
import org.uddi.v3_service.DispositionReportFaultMessage;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.CurrentSession;

import java.rmi.RemoteException;
import java.util.Map;


public class UDDIUtil {

    private static final Log log = LogFactory.getLog(UDDIUtil.class);


    private static final String PUBLISHER_USER_ID = "wso2";
    private static final String PUBLISHER_PASSWORD = "wso2carbon";
    private static final String PUBLISHER_NAME = "wso2";

    private static final String ROOT_USER_ID = "root";
    private static final String ROOT_PASSWORD = "root";

    //This contains keys of business entities
    public static Map<String,String> businessKeyMap;

    private static UDDISecurityImpl security = null;
    private static UDDIPublicationImpl publish = null;
    private static UDDIInquiryImpl inquiry = null;




    /**
     * Publish businessEntity to UDDI registry
     *
     * @param businessEntity BusinessEntity object.
     * @return BusinessDetail of the published BusinessEntity.
     * @throws RegistryException
     */
    public static BusinessDetail publishBusiness(BusinessEntity businessEntity, AuthToken authToken) throws RegistryException {
        BusinessDetail businessDetail;
        try {
            SaveBusiness saveBusiness = new SaveBusiness();
            saveBusiness.getBusinessEntity().add(businessEntity);
            saveBusiness.setAuthInfo(authToken.getAuthInfo());
            businessDetail = getUDDIPublishService().saveBusiness(saveBusiness);
        } catch (Exception e) {
            String msg = "Unable to publish the business entity";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
        return businessDetail;
    }


    /**
     * Publish business service into UDDI registry
     *
     * @param businessService BusinessService object to save
     * @return ServiceDetail of the saved business service
     * @throws RegistryException
     */
    public static ServiceDetail publishBusinessService(BusinessService businessService, AuthToken authToken) throws RegistryException {

        SaveService saveService = new SaveService();
        saveService.getBusinessService().add(businessService);
        ServiceDetail serviceDetail;

        try {
            saveService.setAuthInfo(authToken.getAuthInfo());
            serviceDetail = getUDDIPublishService().saveService(saveService);
        } catch (Exception e) {
            String msg = "Unable to publish the business service";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
        return serviceDetail;
    }

    /**
     * Save TModel in UDDI registry.
     *
     * @param tModel TModel object to save
     * @return TModelDetail of the TModel that is saved. It includes TModel key and etc..
     * @throws RegistryException
     */
    public static TModelDetail saveTModel(TModel tModel) throws RegistryException {
        SaveTModel saveTModel = new SaveTModel();
        saveTModel.getTModel().add(tModel);
        TModelDetail tModelDetail;
        try {
            AuthToken authToken = getPublisherAuthToken();
            saveTModel.setAuthInfo(authToken.getAuthInfo());
            tModelDetail = getUDDIPublishService().saveTModel(saveTModel);
        } catch (Exception e) {
            String msg = "Unable to save TModel in UDDI";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
        return tModelDetail;
    }


    /**
     * Get list of businessInfo for a business.
     *
     * @param business Business that is going to find
     * @return List of businessInfo for matching business
     * @throws RegistryException
     */
    public static BusinessList findBusiness(FindBusiness business) throws RegistryException {
        BusinessList businessList;
        try {
            businessList = getUDDIInquiryService().findBusiness(business);
        } catch (RemoteException e) {
            String msg = "Error occurred while finding a business";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
        return businessList;
    }



    /**
     * Get UDDI security service
     *
     * @return UDDI security service object
     * @throws RegistryException
     */
    private static UDDISecurityImpl getUDDISecurityService() throws Exception {
        if (security == null) {
                security = new UDDISecurityImpl();
        }
        return security;
    }


    /**
     * Get UDDI publication service that is used for publishing business entities, business services, TModels and etc..
     *
     * @return UDDI publication service object
     * @throws RegistryException
     */
    private static UDDIPublicationImpl getUDDIPublishService() throws RegistryException {
        if (publish == null) {
                publish = new UDDIPublicationImpl();
        }
        return publish;
    }

    /**
     * Get UDDI inquiry service object that can be used to find and retrieve UDDI info
     *
     * @return UDDI inquiry service object
     * @throws RegistryException
     */
    private static UDDIInquiryImpl getUDDIInquiryService() throws RegistryException {
        if (inquiry == null) {
                inquiry = new UDDIInquiryImpl();
        }
        return inquiry;
    }


    /**
     * Save publisher to UDDI using root authentication token
     * <p>Others use this publisher to publish services to UDDI
     *
     * @throws RegistryException
     */
    private static boolean savePublisher() throws RegistryException {

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(CurrentSession.getUser());
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(CurrentSession.getTenantId(), true);
            //Get Authentication token for root user('root' user has admin privileges and can save other publishers)
            GetAuthToken authTokenRoot = new GetAuthToken();
            authTokenRoot.setUserID(ROOT_USER_ID);
            authTokenRoot.setCred(ROOT_PASSWORD);
            AuthToken rootAuthToken = null;
            try {
                rootAuthToken = getUDDISecurityService().getAuthToken(authTokenRoot);
                //IF user not authorize to publish to UDDI repository throws DispositionReportFaultMessage
            } catch (Exception e) {
                log.error("Failed to get token", e);
                throw new Exception(e);

            }
            if (rootAuthToken == null) {
                return false;
            }

            //Creating a new publisher that can be used to publish business entities
            Publisher publisher = new Publisher();
            publisher.setAuthorizedName(PUBLISHER_USER_ID);
            publisher.setPublisherName(PUBLISHER_NAME);

            SavePublisher savePublisher = new SavePublisher();
            savePublisher.getPublisher().add(publisher);

            savePublisher.setAuthInfo(rootAuthToken.getAuthInfo());

            JUDDIApiImpl juddiApi = new JUDDIApiImpl();
            juddiApi.savePublisher(savePublisher);

        } catch (Exception e) {
            String msg = "Unable to save the publisher";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }  finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return true;
    }


    /**
     * Retrieve authentication token of saved publisher
     *
     * @return publisher authentication token
     * @throws RegistryException
     */
    public static AuthToken getPublisherAuthToken() throws RegistryException {
        AuthToken publisherAuthToken = null;
                //save the publisher
                if(savePublisher()){
                    //Get publisher authentication token
                    GetAuthToken publisherGetAuthToken = new GetAuthToken();
                    publisherGetAuthToken.setUserID(PUBLISHER_USER_ID);
                    publisherGetAuthToken.setCred(PUBLISHER_PASSWORD);
                    UDDISecurityImpl security = new UDDISecurityImpl();
                    try {
                        publisherAuthToken = security.getAuthToken(publisherGetAuthToken);
                     //IF user not authorize to publish to UDDI repository throws DispositionReportFaultMessage
                    } catch (DispositionReportFaultMessage e) {
                        if(log.isDebugEnabled()){
                            log.debug("Failed to get token",e);
                        }
                    }
                } else {
                    return null;
                }
        return publisherAuthToken;
    }

}