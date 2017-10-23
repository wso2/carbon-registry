package org.wso2.carbon.registry.resource.services.utils;

import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.test.base.BaseTestCase;


public class AddResourceUtilTest extends BaseTestCase {

    @Override
    public void setUp() throws RegistryException {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        registry.delete("/sample/resource");
        super.tearDown();
    }

    public void testAddResource() throws Exception {

        String[][] properties = new String[][]{{"key1", "val1"}, {"key2", "val2"}};

        AddResourceUtil.addResource("/sample/resource", "application/xml", "Sample description", null, null,
                                    (UserRegistry) registry, properties);

        assertTrue(registry.resourceExists("/sample/resource"));

    }
}