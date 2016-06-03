/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.extensions.test.jdbc;

import junit.framework.Assert;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.aspects.DefaultLifecycle;
import org.wso2.carbon.registry.extensions.test.utils.BaseTestCase;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.*;

import java.util.ArrayList;
import java.util.List;

public class DefaultLifecycleTest extends BaseTestCase {

    // todo: aspect
    /**
     * Registry instance for use in tests. Note that there should be only one Registry instance in a
     * JVM.
     */
    protected static Registry registry = null;
    public final String LIFECYCLE_NAME = "DefaultLifecycle";

    public void setUp() throws RegistryException {
        super.setUp();
/*        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain("foo.com");
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(1);*/
        ctx.addAspect(LIFECYCLE_NAME, new DefaultLifecycle(), MultitenantConstants.SUPER_TENANT_ID);
        EmbeddedRegistryService embeddedRegistry = ctx.getEmbeddedRegistryService();
        new RegistryCoreServiceComponent().registerBuiltInHandlers(embeddedRegistry);
        registry = embeddedRegistry.getRegistry("admin", "admin");
    }

    public void testLifecycle() throws RegistryException {

        Resource r1 = registry.newResource();
        byte[] r1content = RegistryUtils.encodeString("R1 content");
        r1.setContent(r1content);
        registry.put("/d12/r1", r1);

        String text1 = "this can be used as a test resource.";
        String text2 = "I like this";
        final Comment comment1 = new Comment(text1);
        comment1.setUser("someone");
        registry.addComment("/d12/r1", comment1);
        final Comment comment2 = new Comment(text2);
        comment2.setUser("someone");
        registry.addComment("/d12/r1", comment2);

        Comment[] comments = registry.getComments("/d12/r1");
        Assert.assertNotNull(registry.get("/d12/r1").getContent());

        boolean commentFound = false;
        for (Comment comment : comments) {
            if (comment.getText().equals(text1)) {
                commentFound = true;
                break;
            }
        }
        Assert.assertTrue("comment '" + text1 +
                "' is not associated with the artifact /d12/r1", commentFound);

        Resource commentsResource = registry.get("/d12/r1;comments");
        Assert.assertTrue("Comment collection resource should be a directory.",
                commentsResource instanceof Collection);
        comments = (Comment[])commentsResource.getContent();

        List commentTexts = new ArrayList();
        for (Comment comment : comments) {
            Resource commentResource = registry.get(comment.getPath());
            commentTexts.add(commentResource.getContent());
        }

        Assert.assertTrue(text1 + " is not associated for resource /d12/r1.",
                commentTexts.contains(text1));
        Assert.assertTrue(text2 + " is not associated for resource /d12/r1.",
                commentTexts.contains(text2));

        registry.associateAspect("/d12/r1", LIFECYCLE_NAME);

        registry.invokeAspect("/d12/r1", LIFECYCLE_NAME, "promote");

        Resource resource = registry.get("/developed/d12/r1");
        Assert.assertNotNull(resource);
        Assert.assertNotNull(resource.getContent());

        comments = registry.getComments("/developed/d12/r1");

        commentFound = false;
        for (Comment comment : comments) {
            if (comment.getText().equals(text1)) {
                commentFound = true;
                break;
            }
        }
        Assert.assertTrue("comment '" + text1 +
                "' is not associated with the artifact /developed/d12/r1", commentFound);

        commentsResource = registry.get("/developed/d12/r1;comments");
        Assert.assertTrue("Comment collection resource should be a directory.",
                commentsResource instanceof Collection);
        comments = (Comment[])commentsResource.getContent();

        commentTexts = new ArrayList();
        for (Comment comment : comments) {
            Resource commentResource = registry.get(comment.getPath());
            commentTexts.add(commentResource.getContent());
        }

        Assert.assertTrue(text1 + " is not associated for resource /developed/d12/r1.",
                commentTexts.contains(text1));
        Assert.assertTrue(text2 + " is not associated for resource /developed/d12/r1.",
                commentTexts.contains(text2));

    }
}
