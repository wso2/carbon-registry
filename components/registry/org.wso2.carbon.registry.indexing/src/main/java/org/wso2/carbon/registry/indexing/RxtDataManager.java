/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.registry.indexing;

import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.indexing.internal.RxtDataServiceDataHolder;

import java.util.HashMap;
import java.util.List;

/**
 * This class is used to set rxt unbounded filed details to the memory.
 */
public class RxtDataManager extends AbstractAdmin {

    private static RxtDataManager rxtDataManagerInstance = new RxtDataManager();

    public static RxtDataManager getInstance() {
        return rxtDataManagerInstance;
    }

    /**
     * This method is used to get unbounded rxt filed values from memory.
     *
     * @return unbounded rxt filed values.
     */
    public HashMap<String, List<String>> getRxtDetails() {
        return RxtDataServiceDataHolder.getInstance().getRxtDetails();
    }

    /**
     * This method is used to set unbounded rxt filed values to memory.
     *
     * @param rxtDetails unbounded rxt filed values.
     * @throws RegistryException
     */
    public void setRxtDetails(HashMap<String, List<String>> rxtDetails) throws RegistryException {
        RxtDataServiceDataHolder.getInstance().setRxtDetails(rxtDetails);
    }
}
