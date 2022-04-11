/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


package org.wso2.carbon.registry.resource.test;

import junit.framework.TestCase;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.resource.services.utils.CommonUtil;
import org.wso2.carbon.registry.resource.services.utils.GetTextContentUtil;

public class Registry2395Test extends TestCase {

    private static final String MOCK_FOLDER_1 = "_system";
    private static final String MOCK_FOLDER_2 = "trunk";
    private static final String MOCK_RESOURCE_NAME = "ResourceName";

    private static final String WSDL_URL = "https://raw.githubusercontent.com/wso2/product-greg/v5.4.0/" +
            "modules/integration/tests-integration/tests-permissions/tests-permissions-resource/" +
            "src/test/resources/artifacts/GREG/wsdl/Imports_with_imports.wsdl";
    private static final String XSD_URL = "https://raw.githubusercontent.com/wso2/product-greg/v5.4.0/" +
            "modules/integration/tests-integration/tests-permissions/tests-permissions-resource/" +
            "src/test/resources/artifacts/GREG/xsd/purchasing.xsd";
    private static final String INVALID_URL = "invalid.url.test.string";
    private static final String RESOURCE_DOES_NOT_EXIST = "https://svn.wso2.org/repos/wso2/carbon/platform/" +
            "trunk/platform-integration/platform-automated-test-suite/org.wso2.carbon.automation.test.repo/src/" +
            "main/resources/artifacts/GREG/xsd/purchasing123.xsd";

    public void testCalculatePath() throws RegistryException {

        String testPathOutput1 = RegistryConstants.PATH_SEPARATOR + MOCK_FOLDER_1 + RegistryConstants.PATH_SEPARATOR
                + MOCK_RESOURCE_NAME;
        String testPathInput2 = RegistryConstants.PATH_SEPARATOR + MOCK_FOLDER_1;
        String testPathInput3 = MOCK_FOLDER_1 + RegistryConstants.PATH_SEPARATOR;
        String testPathInput4 = RegistryConstants.PATH_SEPARATOR + MOCK_FOLDER_1 + RegistryConstants.PATH_SEPARATOR;
        String testPathInput5 = MOCK_FOLDER_1 + RegistryConstants.PATH_SEPARATOR + MOCK_FOLDER_2;
        String testPathOutput5 =
                RegistryConstants.PATH_SEPARATOR + MOCK_FOLDER_1 + RegistryConstants.PATH_SEPARATOR + MOCK_FOLDER_2
                        + RegistryConstants.PATH_SEPARATOR + MOCK_RESOURCE_NAME;
        String testPathInput6 =
                RegistryConstants.PATH_SEPARATOR + MOCK_FOLDER_1 + RegistryConstants.PATH_SEPARATOR + MOCK_FOLDER_2
                        + RegistryConstants.PATH_SEPARATOR;

        assertEquals(testPathOutput1, CommonUtil.calculatePath(MOCK_FOLDER_1, MOCK_RESOURCE_NAME));
        assertEquals(testPathOutput1, CommonUtil.calculatePath(testPathInput2, MOCK_RESOURCE_NAME));
        assertEquals(testPathOutput1, CommonUtil.calculatePath(testPathInput3, MOCK_RESOURCE_NAME));
        assertEquals(testPathOutput1, CommonUtil.calculatePath(testPathInput4, MOCK_RESOURCE_NAME));

        assertEquals(testPathOutput5, CommonUtil.calculatePath(testPathInput5, MOCK_RESOURCE_NAME));
        assertEquals(testPathOutput5, CommonUtil.calculatePath(testPathInput6, MOCK_RESOURCE_NAME));

    }

    public void testGetByteContent() throws RegistryException {

        assertEquals(true, GetTextContentUtil.getByteContent(WSDL_URL) != null);
        assertEquals(true, GetTextContentUtil.getByteContent(XSD_URL) != null);
    }

    public void testGetByteContentInvalidUrl() {

        boolean thrown = false;
        try {
            GetTextContentUtil.getByteContent(INVALID_URL);
        } catch (RegistryException e) {
            if (e.getMessage().contains("Invalid source URL format")) {
                thrown = true;
            }
        }
        assertTrue(thrown);
    }

    public void testGetByteContentResourceDoesNotExist() {

        boolean thrown = false;
        try {
            GetTextContentUtil.getByteContent(RESOURCE_DOES_NOT_EXIST);
        } catch (RegistryException e) {
            if (e.getMessage().contains("Wrong or unavailable source URL")) {
                thrown = true;
            }
        }
        assertTrue(thrown);
    }

    public void testSetProperties() {

        String[][] testProperties = new String[2][2];
        testProperties[0][0] = "testKey1";
        testProperties[0][1] = "testVal1";
        testProperties[1][0] = "testKey2";
        testProperties[1][1] = "testVal2";

        String[][] newProperties = CommonUtil.setProperties(testProperties, "newKey", "newVal");
        if (newProperties[(newProperties.length - 1)].length == 2
                && newProperties[(newProperties.length - 1)][0] == "newKey"
                && newProperties[(newProperties.length - 1)][1] == "newVal") {
            assertTrue("Property array set successfully!", true);
        } else {
            assertTrue("Property array setting unsuccessful!", false);
        }
    }
}
