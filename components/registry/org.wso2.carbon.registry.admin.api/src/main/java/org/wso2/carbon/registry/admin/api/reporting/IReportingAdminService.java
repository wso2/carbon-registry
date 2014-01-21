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
package org.wso2.carbon.registry.admin.api.reporting;

import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

/**
 * Provides functionality to produce (on-demand and schedule) reports for resources stored on
 * this repository and other repositories (using URLs).
 *
 * @param <ReportConfigurationBean> This bean contains details of a single report's configuration.
 */
public interface IReportingAdminService<ReportConfigurationBean> {

    /**
     * Method to obtain a stream of bytes of the generated report. This is returned to the client
     * as a DataHandler.
     *
     * @param configuration the report configuration
     *
     * @return byte array of the generated report
     *
     * @throws Exception if the operation failed
     */
    byte[] getReportBytes(ReportConfigurationBean configuration)
            throws Exception;

    /**
     * Method to schedule the generation of a report
     *
     * @param configuration the report configuration
     *
     * @throws Exception if the operation failed
     */
    void scheduleReport(ReportConfigurationBean configuration)
            throws Exception;

    /**
     * Method to stop the generation of a scheduled report
     *
     * @param name the name of the report
     *
     * @throws Exception if the operation failed
     */
    void stopScheduledReport(String name) throws Exception;

    /**
     * Method to save a report
     *
     * @param configuration the report's configuration
     *
     * @throws RegistryException if the failure was from the registry
     * @throws CryptoException if the failure was when performing data encryption
     */
    void saveReport(ReportConfigurationBean configuration)
            throws RegistryException, CryptoException;

    /**
     * Method to retrieve saved report
     *
     * @throws RegistryException if the failure was from the registry
     * @throws CryptoException if the failure was when performing data encryption
     * @throws TaskException if the failure was when retrieving details of the scheduled tasks.
     */
    ReportConfigurationBean[] getSavedReports()
            throws RegistryException, CryptoException, TaskException;

    /**
     * Method to retrieve a saved report
     *
     * @param name the name of the report
     *
     * @throws RegistryException if the failure was from the registry
     * @throws CryptoException if the failure was when performing data encryption
     * @throws TaskException if the failure was when retrieving details of the scheduled tasks.
     */
    ReportConfigurationBean getSavedReport(String name)
            throws RegistryException, CryptoException, TaskException;

    /**
     * Method to delete saved report
     *
     * @param name the name of the report
     *
     * @throws RegistryException if the failure was from the registry
     */
    void deleteSavedReport(String name) throws RegistryException;

    /**
     * Method to copy saved report
     *
     * @param name    the name of the report
     * @param newName the new name for the copy
     *
     * @throws RegistryException if the failure was from the registry
     */
    void copySavedReport(String name, String newName) throws RegistryException;

    /**
     * Method to retrieve the list of properties that can be passed to the report generator class
     *
     * @param className the fully qualified name of the
     *
     * @return a list of property names
     *
     * @throws Exception if the operation failed
     */
    String[] getAttributeNames(String className) throws Exception;

    /**
     * Method to retrieve the list of mandatory properties that can be passed to the report
     * generator class
     *
     * @param className the fully qualified name of the
     *
     * @return the subset of property names that are mandatory
     *
     * @throws Exception if the operation failed
     */
    String[] getMandatoryAttributeNames(String className) throws Exception;
}
