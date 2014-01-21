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
package org.wso2.carbon.registry.reporting;

import org.wso2.carbon.registry.core.Registry;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * An abstract report generator implementation that can be used to produce reports.
 */
@SuppressWarnings("unused")
public abstract class AbstractReportGenerator {
    
    private Registry registry = null;

    /**
     * Method to set an instance of the registry.
     * @param registry the instance of the registry.
     */
    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    /**
     * Method to obtain an instance of the registry.
     * @return the instance of the registry.
     */
    public Registry getRegistry() {
        return registry;
    }

    /**
     * Executes the report generator and retrieves the generated report as a stream of bytes.
     * @param template the template to use.
     * @param type the type of report required.
     * @return generated report as a stream of bytes
     * @throws IOException if the operation failed.
     */
    public abstract ByteArrayOutputStream execute(String template, String type) throws IOException;
}
