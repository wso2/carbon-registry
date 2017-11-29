/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.registry.handler.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.junit.Before;
import org.junit.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.SimulationService;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.aspects.DefaultLifecycle;
import org.wso2.carbon.registry.handler.base.BaseTestCase;
import org.wso2.carbon.registry.handler.beans.FilterConfigurationBean;
import org.wso2.carbon.registry.handler.beans.HandlerConfigurationBean;
import org.wso2.carbon.registry.handler.beans.SimulationRequest;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class CommonUtilTest extends BaseTestCase {

    private String handlerConfiguration = "<handler class=\"org.wso2.carbon.registry.extensions.handlers.ServiceMediaTypeHandler\">\n" +
            "\t<filter class=\"org.wso2.carbon.registry.core.jdbc.handlers.filters.MediaTypeMatcher\">\n" +
            "\t\t<property name=\"mediaType\">application/vnd.wso2-service+xml</property>\n" +
            "\t</filter>\n" +
            "</handler>";

    private String updateHandlerConfig = "    <handler class=\"org.wso2.carbon.registry.extensions.handlers.DeleteSubscriptionHandler\">\n" +
            "         <filter class=\"org.wso2.carbon.registry.core.jdbc.handlers.filters.URLMatcher\">\n" +
            "                 <property name=\"pattern\">.*</property>\n" +
            "         </filter>\n" +
            "    </handler>";

    @Before
    public void setUp() throws Exception {
        super.setUp();
        Resource resource = registry.newResource();
        byte[] r1content = RegistryUtils.encodeString("Resource 17 content");
        resource.setContent(r1content);
        resource.setMediaType("application/test");
        resource.setDescription("Sample 17 Description");
        resource.setVersionableChange(true);
        resource.addAspect("Servicelifecycle");
        registry.put("/test/2017/10/18", resource);
        registry.addAspect("Servicelifecycle", new DefaultLifecycle());
        SimulationService simulationService = new RegistryCoreServiceComponent.DefaultSimulationService();
        simulationService.setSimulation(false);
        CommonUtil.setSimulationService(simulationService);

    }

    @Test
    public void testGetRegistryService() throws Exception {
        RegistryService service = mock(RegistryService.class);
        CommonUtil.setRegistryService(service);
        assertEquals(service, CommonUtil.getRegistryService());
    }

    @Test
    public void testSimulateRegistryForNullOperation() throws Exception {
        SimulationRequest simulationRequest = new SimulationRequest();
        CommonUtil.simulateRegistryOperation(registry, simulationRequest);
    }

    @Test
    public void testSimulateRegistryForGetOperation() throws Exception {
        final SimulationRequest simulationRequest = new SimulationRequest();
        simulationRequest.setOperation("get");
        simulationRequest.setPath("/test/2017/10/18");
        try {
            CommonUtil.simulateRegistryOperation(registry, simulationRequest);
        } catch (Exception igorned){}
        assertNotNull(CommonUtil.getSimulationResponse().getStatus());
        assertEquals(3, CommonUtil.getSimulationResponse().getStatus().length);
    }

    @Test
    public void testSimulateRegistryForGetOperationWithoutPath() throws Exception {
        final SimulationRequest simulationRequest = new SimulationRequest();
        simulationRequest.setOperation("get");
        new Thread() {
            public void run() {
                try {
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin");
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234, true);
                    CommonUtil.simulateRegistryOperation(registry, simulationRequest);
                    assertNotNull(CommonUtil.getSimulationResponse().getStatus());
                    assertEquals(0, CommonUtil.getSimulationResponse().getStatus().length);
                } catch (Exception e) {
                    fail("Error while simulating the handler operations");
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }
        }.start();
    }

    @Test
    public void testSimulateRegistryForResourceExistsOperation() throws Exception {
        final SimulationRequest simulationRequest = new SimulationRequest();
        simulationRequest.setOperation("resourceexists");
        simulationRequest.setPath("/test/2017/10/18");

        CommonUtil.simulateRegistryOperation(registry, simulationRequest);
        assertNotNull(CommonUtil.getSimulationResponse().getStatus());
        assertEquals(3, CommonUtil.getSimulationResponse().getStatus().length);
    }

    @Test
    public void testSimulateRegistryForResourceExistsOperationWithoutPath() throws Exception {
        final SimulationRequest simulationRequest = new SimulationRequest();
        simulationRequest.setOperation("resourceexists");
        new Thread() {
            public void run() {
                try {
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin");
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234, true);
                    CommonUtil.simulateRegistryOperation(registry, simulationRequest);
                    assertNotNull(CommonUtil.getSimulationResponse().getStatus());
                    assertEquals(0, CommonUtil.getSimulationResponse().getStatus().length);
                } catch (Exception e) {
                    fail("Error while simulating the handler operations");
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }
        }.start();
    }

    @Test
    public void testSimulateRegistryForPutOperation() throws Exception {
        final SimulationRequest simulationRequest = new SimulationRequest();
        simulationRequest.setOperation("put");
        simulationRequest.setPath("/test/2017/10/19");
        simulationRequest.setResourcePath("/test/2017/10/18");
        simulationRequest.setParameters(new String[]{"resource"});

        CommonUtil.simulateRegistryOperation(registry, simulationRequest);
        assertNotNull(CommonUtil.getSimulationResponse().getStatus());
        assertEquals(3, CommonUtil.getSimulationResponse().getStatus().length);
    }

    @Test
    public void testSimulateRegistryForPutOperationwithoutResourcePath() throws Exception {
        registry.get("/test/2017/10/18");
        final SimulationRequest simulationRequest = new SimulationRequest();
        simulationRequest.setOperation("put");
        simulationRequest.setPath("/test/2017/10/19");
/*        simulationRequest.setResourcePath("/test/2017/10/18");*/
        simulationRequest.setParameters(new String[]{"resource"});

        CommonUtil.simulateRegistryOperation(registry, simulationRequest);
        assertNotNull(CommonUtil.getSimulationResponse().getStatus());
        assertEquals(3, CommonUtil.getSimulationResponse().getStatus().length);
    }

    @Test
    public void testSimulateRegistryForDeleteOperation() throws Exception {
        final SimulationRequest simulationRequest = new SimulationRequest();
        simulationRequest.setOperation("delete");
        simulationRequest.setPath("/test/2017/10/18");
        new Thread() {
            public void run() {
                try {
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin");
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234, true);
                    CommonUtil.simulateRegistryOperation(registry, simulationRequest);
                    assertNotNull(CommonUtil.getSimulationResponse().getStatus());
                    assertEquals(3, CommonUtil.getSimulationResponse().getStatus().length);
                } catch (Exception e) {
                    fail("Error while simulating the handler operations");
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }
        }.start();
    }

    @Test
    public void testSimulateRegistryForImportOperation() throws Exception {
        final SimulationRequest simulationRequest = new SimulationRequest();
        simulationRequest.setOperation("importresource");
        simulationRequest.setParameters(new String[]{"https://wso2.org"});
        simulationRequest.setPath("/test/2017/10/18");
        simulationRequest.setMediaType("application/xml");
        new Thread() {
            public void run() {
                try {
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin");
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234, true);
                    CommonUtil.simulateRegistryOperation(registry, simulationRequest);
                    assertNotNull(CommonUtil.getSimulationResponse().getStatus());
                    assertEquals(3, CommonUtil.getSimulationResponse().getStatus().length);
                } catch (Exception e) {
                    fail("Error while simulating the handler operations");
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }
        }.start();
    }

    @Test
    public void testSimulateRegistryForRenameOperation() throws Exception {
        final SimulationRequest simulationRequest = new SimulationRequest();
        simulationRequest.setOperation("rename");
        simulationRequest.setParameters(new String[]{"/test/2017/10/18"});
        simulationRequest.setPath("/test/2017/10/18");
        simulationRequest.setMediaType("application/xml");
        new Thread() {
            public void run() {
                try {
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin");
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234, true);
                    CommonUtil.simulateRegistryOperation(registry, simulationRequest);
                    assertNotNull(CommonUtil.getSimulationResponse().getStatus());
                    assertEquals(4, CommonUtil.getSimulationResponse().getStatus().length);
                } catch (Exception e) {
                    fail("Error while simulating the handler operations");
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }
        }.start();
    }

    @Test
    public void testSimulateRegistryForMoveOperation() throws Exception {
        final SimulationRequest simulationRequest = new SimulationRequest();
        simulationRequest.setOperation("move");
        simulationRequest.setParameters(new String[]{"/test/2017/10/18"});
        simulationRequest.setPath("/test/2017/10/18");
        simulationRequest.setMediaType("application/xml");
        new Thread() {
            public void run() {
                try {
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin");
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234, true);
                    CommonUtil.simulateRegistryOperation(registry, simulationRequest);
                    assertNotNull(CommonUtil.getSimulationResponse().getStatus());
                    assertEquals(4, CommonUtil.getSimulationResponse().getStatus().length);
                } catch (Exception e) {
                    fail("Error while simulating the handler operations");
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }
        }.start();
    }

    @Test
    public void testSimulateRegistryForCopyOperation() throws Exception {
        final SimulationRequest simulationRequest = new SimulationRequest();
        simulationRequest.setOperation("copy");
        simulationRequest.setParameters(new String[]{"/test/2017/10/18"});
        simulationRequest.setPath("/test/2017/10/18");
        simulationRequest.setMediaType("application/xml");

        CommonUtil.simulateRegistryOperation(registry, simulationRequest);
        assertNotNull(CommonUtil.getSimulationResponse().getStatus());
        assertEquals(3, CommonUtil.getSimulationResponse().getStatus().length);
    }

    @Test
    public void testSimulateRegistryForRemoveLinkOperation() throws Exception {
        final SimulationRequest simulationRequest = new SimulationRequest();
        simulationRequest.setOperation("removelink");
        simulationRequest.setPath("/test/2017/10/18");

        CommonUtil.simulateRegistryOperation(registry, simulationRequest);
        assertNotNull(CommonUtil.getSimulationResponse().getStatus());
        assertEquals(3, CommonUtil.getSimulationResponse().getStatus().length);
    }

    @Test
    public void testSimulateRegistryForCreateLinkOperation() throws Exception {
        final SimulationRequest simulationRequest = new SimulationRequest();
        simulationRequest.setParameters(new String[]{"/test/2017/10/19", "/test/2017/10/20"});
        simulationRequest.setOperation("createlink");
        simulationRequest.setPath("/test/2017/10/18");

        CommonUtil.simulateRegistryOperation(registry, simulationRequest);
        assertNotNull(CommonUtil.getSimulationResponse().getStatus());
        assertEquals(3, CommonUtil.getSimulationResponse().getStatus().length);
    }

    @Test
    public void testSimulateRegistryForInvokeAspectOperation() throws Exception {
        final SimulationRequest simulationRequest = new SimulationRequest();

        simulationRequest.setParameters(new String[]{"Servicelifecycle", "promote"});
        simulationRequest.setOperation("invokeaspect");
        simulationRequest.setPath("/test/2017/10/18");
        try {
            CommonUtil.simulateRegistryOperation(registry, simulationRequest);
        } catch (Exception ignored) {
        }

        assertNotNull(CommonUtil.getSimulationResponse().getStatus());
        assertEquals(3, CommonUtil.getSimulationResponse().getStatus().length);
    }

    @Test
    public void testSimulateRegistryForAddAssociationOperation() throws Exception {
        final SimulationRequest simulationRequest = new SimulationRequest();
        simulationRequest.setParameters(new String[]{"/test/2017/10/19", "dependson"});
        simulationRequest.setOperation("addassociation");
        simulationRequest.setPath("/test/2017/10/18");

        CommonUtil.simulateRegistryOperation(registry, simulationRequest);
        assertNotNull(CommonUtil.getSimulationResponse().getStatus());
        assertEquals(3, CommonUtil.getSimulationResponse().getStatus().length);
    }

    @Test
    public void testSimulateRegistryForRemoveAssociationOperation() throws Exception {
        final SimulationRequest simulationRequest = new SimulationRequest();
        simulationRequest.setParameters(new String[]{"/test/2017/10/19", "dependson"});
        simulationRequest.setOperation("removeassociation");
        simulationRequest.setPath("/test/2017/10/18");

        CommonUtil.simulateRegistryOperation(registry, simulationRequest);
        assertNotNull(CommonUtil.getSimulationResponse().getStatus());
        assertEquals(3, CommonUtil.getSimulationResponse().getStatus().length);
    }

    @Test
    public void testSimulateRegistryForGetAssociationOperation() throws Exception {
        final SimulationRequest simulationRequest = new SimulationRequest();
        simulationRequest.setParameters(new String[]{"dependson"});
        simulationRequest.setOperation("getassociations");
        simulationRequest.setPath("/test/2017/10/18");

        CommonUtil.simulateRegistryOperation(registry, simulationRequest);
        assertNotNull(CommonUtil.getSimulationResponse().getStatus());
        assertEquals(3, CommonUtil.getSimulationResponse().getStatus().length);
    }

    @Test
    public void testSimulateRegistryForGetAllAssociationsOperation() throws Exception {
        final SimulationRequest simulationRequest = new SimulationRequest();
        simulationRequest.setOperation("getallassociations");
        simulationRequest.setPath("/test/2017/10/18");

        CommonUtil.simulateRegistryOperation(registry, simulationRequest);
        assertNotNull(CommonUtil.getSimulationResponse().getStatus());
        assertEquals(3, CommonUtil.getSimulationResponse().getStatus().length);
    }

    @Test
    public void testSimulateRegistryForCreateVersionOperation() throws Exception {
        final SimulationRequest simulationRequest = new SimulationRequest();
        simulationRequest.setOperation("createversion");
        simulationRequest.setPath("/test/2017/10/18");

        CommonUtil.simulateRegistryOperation(registry, simulationRequest);
        assertNotNull(CommonUtil.getSimulationResponse().getStatus());
        assertEquals(3, CommonUtil.getSimulationResponse().getStatus().length);
    }

    @Test
    public void testSimulateRegistryForRestoreVersionOperation() throws Exception {
        final SimulationRequest simulationRequest = new SimulationRequest();
        simulationRequest.setOperation("restoreversion");
        simulationRequest.setPath("/test/2017/10/18");

        CommonUtil.simulateRegistryOperation(registry, simulationRequest);
        assertNotNull(CommonUtil.getSimulationResponse().getStatus());
        assertEquals(3, CommonUtil.getSimulationResponse().getStatus().length);
    }

    @Test
    public void testSimulateRegistryForGetVersionsOperation() throws Exception {
        final SimulationRequest simulationRequest = new SimulationRequest();
        simulationRequest.setOperation("getversions");
        simulationRequest.setPath("/test/2017/10/18");

        CommonUtil.simulateRegistryOperation(registry, simulationRequest);
        assertNotNull(CommonUtil.getSimulationResponse().getStatus());
        assertEquals(3, CommonUtil.getSimulationResponse().getStatus().length);
    }

    @Test
    public void testSimulateRegistryForApplyTagsOperation() throws Exception {
        final SimulationRequest simulationRequest = new SimulationRequest();
        simulationRequest.setParameters(new String[]{"tag1"});
        simulationRequest.setOperation("applytag");
        simulationRequest.setPath("/test/2017/10/18");

        CommonUtil.simulateRegistryOperation(registry, simulationRequest);
        assertNotNull(CommonUtil.getSimulationResponse().getStatus());
        assertEquals(3, CommonUtil.getSimulationResponse().getStatus().length);
    }

    @Test
    public void testSimulateRegistryForRemoveTagsOperation() throws Exception {
        final SimulationRequest simulationRequest = new SimulationRequest();
        simulationRequest.setParameters(new String[]{"tag1"});
        simulationRequest.setOperation("removetag");
        simulationRequest.setPath("/test/2017/10/18");

        CommonUtil.simulateRegistryOperation(registry, simulationRequest);
        assertNotNull(CommonUtil.getSimulationResponse().getStatus());
        assertEquals(3, CommonUtil.getSimulationResponse().getStatus().length);
    }

    @Test
    public void testSimulateRegistryForgetTagsOperation() throws Exception {
        final SimulationRequest simulationRequest = new SimulationRequest();
        simulationRequest.setOperation("gettags");
        simulationRequest.setPath("/test/2017/10/18");

        CommonUtil.simulateRegistryOperation(registry, simulationRequest);
        assertNotNull(CommonUtil.getSimulationResponse().getStatus());
        assertEquals(3, CommonUtil.getSimulationResponse().getStatus().length);
    }

    @Test
    public void testSimulateRegistryForgetResourcePathsWithTagsOperation() throws Exception {
        final SimulationRequest simulationRequest = new SimulationRequest();
        simulationRequest.setParameters(new String[]{"tag1"});
        simulationRequest.setOperation("getresourcepathswithtag");

        CommonUtil.simulateRegistryOperation(registry, simulationRequest);
        assertNotNull(CommonUtil.getSimulationResponse().getStatus());
        assertEquals(1, CommonUtil.getSimulationResponse().getStatus().length);
    }

    @Test
    public void testSimulateRegistryForRateResourceOperation() throws Exception {
        final SimulationRequest simulationRequest = new SimulationRequest();
        simulationRequest.setParameters(new String[]{"12"});
        simulationRequest.setOperation("rateresource");
        simulationRequest.setPath("/test/2017/10/18");

        CommonUtil.simulateRegistryOperation(registry, simulationRequest);
        assertNotNull(CommonUtil.getSimulationResponse().getStatus());
        assertEquals(3, CommonUtil.getSimulationResponse().getStatus().length);
    }

    @Test
    public void testSimulateRegistryForGetRatesOperation() throws Exception {
        final SimulationRequest simulationRequest = new SimulationRequest();
        simulationRequest.setParameters(new String[]{"admin"});
        simulationRequest.setOperation("getrating");
        simulationRequest.setPath("/test/2017/10/18");

        CommonUtil.simulateRegistryOperation(registry, simulationRequest);
        assertNotNull(CommonUtil.getSimulationResponse().getStatus());
        assertEquals(3, CommonUtil.getSimulationResponse().getStatus().length);
    }

    @Test
    public void testSimulateRegistryForGetAverageRatesOperation() throws Exception {
        final SimulationRequest simulationRequest = new SimulationRequest();
        simulationRequest.setOperation("getaveragerating");
        simulationRequest.setPath("/test/2017/10/18");

        CommonUtil.simulateRegistryOperation(registry, simulationRequest);
        assertNotNull(CommonUtil.getSimulationResponse().getStatus());
        assertEquals(3, CommonUtil.getSimulationResponse().getStatus().length);
    }

    @Test
    public void testSimulateRegistryForAddCommentOperation() throws Exception {
        final SimulationRequest simulationRequest = new SimulationRequest();
        simulationRequest.setParameters(new String[]{"Testing"});
        simulationRequest.setOperation("addcomment");
        simulationRequest.setPath("/test/2017/10/18");

        CommonUtil.simulateRegistryOperation(registry, simulationRequest);
        assertNotNull(CommonUtil.getSimulationResponse().getStatus());
        assertEquals(3, CommonUtil.getSimulationResponse().getStatus().length);
    }

    @Test
    public void testSimulateRegistryForEditCommentOperation() throws Exception {
        final SimulationRequest simulationRequest = new SimulationRequest();
        simulationRequest.setParameters(new String[]{"Testing"});
        simulationRequest.setOperation("editcomment");
        simulationRequest.setPath("/test/2017/10/18");

        CommonUtil.simulateRegistryOperation(registry, simulationRequest);
        assertNotNull(CommonUtil.getSimulationResponse().getStatus());
        assertEquals(3, CommonUtil.getSimulationResponse().getStatus().length);
    }

    @Test
    public void testSimulateRegistryForRemoveCommentOperation() throws Exception {
        final SimulationRequest simulationRequest = new SimulationRequest();
        simulationRequest.setOperation("removeComment");
        simulationRequest.setPath("/test/2017/10/18");

        try {
            CommonUtil.simulateRegistryOperation(registry, simulationRequest);
        } catch (RegistryException e) {
            assertEquals("The transaction is already rollbacked, you can not commit a transaction already " +
                    "rollbacked, nested depth: 1.", e.getMessage());
        }
    }

    @Test
    public void testSimulateRegistryForGetCommentOperation() throws Exception {
        final SimulationRequest simulationRequest = new SimulationRequest();
        simulationRequest.setOperation("getcomments");
        simulationRequest.setPath("/test/2017/10/18");

        CommonUtil.simulateRegistryOperation(registry, simulationRequest);
        assertNotNull(CommonUtil.getSimulationResponse().getStatus());
        assertEquals(3, CommonUtil.getSimulationResponse().getStatus().length);
    }

    @Test
    public void testSimulateRegistryForSearchContentOperation() throws Exception {
        final SimulationRequest simulationRequest = new SimulationRequest();
        simulationRequest.setParameters(new String[]{"Testing"});
        simulationRequest.setOperation("searchcontent");
        new Thread() {
            public void run() {
                try {
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin");
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234, true);
                    CommonUtil.simulateRegistryOperation(registry, simulationRequest);
                    assertNotNull(CommonUtil.getSimulationResponse().getStatus());
                    assertEquals(0, CommonUtil.getSimulationResponse().getStatus().length);
                } catch (Exception e) {
                    fail("Error while simulating the handler operations");
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }
        }.start();
    }

    @Test
    public void testSimulateRegistryForExecuteQueryOperation() throws Exception {
        final SimulationRequest simulationRequest = new SimulationRequest();
        simulationRequest.setParameters(new String[]{"name:WSO2,version:1.0.0"});
        simulationRequest.setOperation("executequery");
        simulationRequest.setPath("/test/2017/10/18");

        CommonUtil.simulateRegistryOperation(registry, simulationRequest);
        assertNotNull(CommonUtil.getSimulationResponse().getStatus());
        assertEquals(3, CommonUtil.getSimulationResponse().getStatus().length);
    }

    @Test
    public void testSimulateRegistryForInvalidOperation() throws Exception {
        final SimulationRequest simulationRequest = new SimulationRequest();
        simulationRequest.setOperation("invalid");

        try {
            CommonUtil.simulateRegistryOperation(registry, simulationRequest);
        } catch (Exception e) {
            assertEquals("Unsupported Registry Operation: invalid", e.getMessage());
        }
    }

    @Test
    public void testAddHandler() throws Exception {
        String handlerName = "org.wso2.carbon.registry.extensions.handlers.ServiceMediaTypeHandler";
        assertTrue(CommonUtil.addHandler(registry, handlerConfiguration));
        String[] handlerList = CommonUtil.getHandlerList(registry);
        assertArrayEquals(new String[]{handlerName}, handlerList);
        String handlerConfig = CommonUtil.getHandlerConfiguration(registry, handlerName);
        assertEquals(handlerConfiguration, handlerConfig);

        assertTrue(CommonUtil.updateHandler(registry, handlerName, updateHandlerConfig));
        String updatedHandlerName = "org.wso2.carbon.registry.extensions.handlers.DeleteSubscriptionHandler";
        handlerList = CommonUtil.getHandlerList(registry);
        assertArrayEquals(new String[]{updatedHandlerName}, handlerList);
        String updatedHandlerConfig = CommonUtil.getHandlerConfiguration(registry, updatedHandlerName);
        assertEquals(updateHandlerConfig, updatedHandlerConfig);

        assertTrue(CommonUtil.deleteHandler(registry, updatedHandlerName));
        handlerList = CommonUtil.getHandlerList(registry);
        assertArrayEquals(null, handlerList);
    }

    @Test
    public void testUpdateHandler() throws Exception {
        String handlerName = "org.wso2.carbon.registry.extensions.handlers.ServiceMediaTypeHandler";
        assertTrue(CommonUtil.updateHandler(registry, null, handlerConfiguration));
        String[] handlerList = CommonUtil.getHandlerList(registry);
        assertArrayEquals(new String[]{handlerName}, handlerList);
        String handlerConfig = CommonUtil.getHandlerConfiguration(registry, handlerName);
        assertEquals(handlerConfiguration, handlerConfig);

        assertTrue(CommonUtil.deleteHandler(registry, handlerName));
        handlerList = CommonUtil.getHandlerList(registry);
        assertArrayEquals(null, handlerList);
    }

    @Test
    public void testUpdateHandlerWithSameName() throws Exception {
        String testHandlerConfig = "<handler class=\"org.wso2.carbon.registry.extensions.handlers" +
                ".ServiceMediaTypeHandler1\">\n" +
                "\t<filter class=\"org.wso2.carbon.registry.core.jdbc.handlers.filters.MediaTypeMatcher\">\n" +
                "\t\t<property name=\"mediaType\">application/vnd.wso2-service+xml</property>\n" +
                "\t</filter>\n" +
                "</handler>";
        String handlerName = "org.wso2.carbon.registry.extensions.handlers.ServiceMediaTypeHandler1";
        assertTrue(CommonUtil.updateHandler(registry, handlerName, testHandlerConfig));
        String[] handlerList = CommonUtil.getHandlerList(registry);
        assertArrayEquals(new String[]{handlerName}, handlerList);
        String handlerConfig = CommonUtil.getHandlerConfiguration(registry, handlerName);
        assertEquals(testHandlerConfig, handlerConfig);

        assertTrue(CommonUtil.deleteHandler(registry, handlerName));
        handlerList = CommonUtil.getHandlerList(registry);
        assertArrayEquals(null, handlerList);
    }

    @Test
    public void testAddDefaultHandlersIfNotAvailable() throws Exception {
        assertTrue(CommonUtil.addDefaultHandlersIfNotAvailable(registry));
    }

    @Test
    public void testDeserializeHandlerConfiguration() throws Exception {
        String HandlerConfigs = "<handler class=\"org.wso2.carbon.registry.extensions.handlers" +
                ".ServiceMediaTypeHandler\" tenant=\"-1234\" methods=\"get,put\"><property name=\"mediaType\">application/vnd.wso2-service+xml</property><filter class=\"org.wso2.carbon.registry.core.jdbc.handlers.filters.MediaTypeMatcher\"><property name=\"mediaType\">application/vnd.wso2-service+xml</property></filter></handler>";
        OMElement element = AXIOMUtil.stringToOM(HandlerConfigs);
        HandlerConfigurationBean configurationBean = CommonUtil.deserializeHandlerConfiguration(element);
        assertEquals("org.wso2.carbon.registry.extensions.handlers.ServiceMediaTypeHandler",
                configurationBean.getHandlerClass());
        assertEquals("org.wso2.carbon.registry.core.jdbc.handlers.filters.MediaTypeMatcher",
                configurationBean.getFilter().getFilterClass());
        assertEquals("application/vnd.wso2-service+xml", configurationBean.getFilter().getNonXmlProperties()
                .get("mediaType"));
    }

    @Test
    public void testSerializeHandlerConfiguration() throws Exception {
        String expectedHandlerConfigs = "<handler class=\"org.wso2.carbon.registry.extensions.handlers" +
                ".ServiceMediaTypeHandler\" tenant=\"-1234\" methods=\"get,put\"><property name=\"mediaType\">application/vnd.wso2-service+xml</property><filter class=\"org.wso2.carbon.registry.core.jdbc.handlers.filters.MediaTypeMatcher\"><property name=\"mediaType\">application/vnd.wso2-service+xml</property></filter></handler>";
        OMElement element = AXIOMUtil.stringToOM(expectedHandlerConfigs);
        HandlerConfigurationBean configurationBean = new HandlerConfigurationBean();
        configurationBean.setHandlerClass("org.wso2.carbon.registry.extensions.handlers.ServiceMediaTypeHandler");
        configurationBean.setMethods(new String[]{"get", "put"});
        configurationBean.setTenant("-1234");
        configurationBean.getPropertyList().add("mediaType");
        configurationBean.getNonXmlProperties().put("mediaType", "application/vnd.wso2-service+xml");
        FilterConfigurationBean filterConfigurationBean = new FilterConfigurationBean();
        filterConfigurationBean.setFilterClass("org.wso2.carbon.registry.core.jdbc.handlers.filters.MediaTypeMatcher");
        filterConfigurationBean.getNonXmlProperties().put("mediaType", "application/vnd.wso2-service+xml");
        filterConfigurationBean.getPropertyList().add("mediaType");
        configurationBean.setFilter(filterConfigurationBean);
        assertEquals(element.toString(), CommonUtil.serializeHandlerConfiguration(configurationBean).toString());
    }

    @Test
    public void testGetContextRoot() throws Exception {
        String handlerContextRoot = "/repository/components/org.wso2.carbon.governance/handlers1/";
        CommonUtil.setContextRoot(handlerContextRoot);
        assertEquals(handlerContextRoot, CommonUtil.getContextRoot());
        CommonUtil.setContextRoot(RegistryConstants.HANDLER_CONFIGURATION_PATH);
    }
}
