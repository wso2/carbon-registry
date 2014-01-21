/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.resource.servlets;

import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.resource.services.utils.CommonUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

public class ResourceServlet extends HttpServlet {

    private static final Log log = LogFactory.getLog(ResourceServlet.class);

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            UserRegistry registry = (UserRegistry) request.getSession().getAttribute(
                            RegistryConstants.ROOT_REGISTRY_INSTANCE);
            String path = request.getParameter("path");
            if (path == null) {
                String msg = "Could not get the resource content. Path is not specified.";
                log.error(msg);
                response.setStatus(400);
                return;
            }

            Resource resource = null;
            try {
                resource = registry.get(path);
            } catch (RegistryException e) {
                String msg = "Error retrieving the resource " + path + ". " + e.getMessage();
                log.error(msg, e);
                throw e;
            }

            if (resource instanceof Collection) {
                String msg = "Could not get the resource content. Path " + path + " refers to a collection.";
                log.error(msg);
                response.setStatus(501);
                return;
            }

            response.setDateHeader("Last-Modified", resource.getLastModified().getTime());

            if (resource.getMediaType() != null && resource.getMediaType().length() > 0) {
                response.setContentType(resource.getMediaType());
            } else {
                response.setHeader(
                        "Content-Disposition", "attachment; filename=" + RegistryUtils.getResourceName(path));
                response.setContentType("application/download");
            }

            InputStream contentStream = resource.getContentStream();
            if (contentStream != null) {

                try {
                    ServletOutputStream servletOutputStream = response.getOutputStream();
                    byte[] contentChunk = new byte[1024];
                    int byteCount;
                    while ((byteCount = contentStream.read(contentChunk)) != -1) {
                        servletOutputStream.write(contentChunk, 0, byteCount);
                    }

                    response.flushBuffer();
                    servletOutputStream.flush();

                } finally {
                    contentStream.close();
                }

            } else {
                Object content = resource.getContent();
                if (content != null) {

                    if (content instanceof byte[]) {
                        ServletOutputStream servletOutputStream = response.getOutputStream();
                        servletOutputStream.write((byte[])content);
                        response.flushBuffer();
                        servletOutputStream.flush();
                    } else {
                        PrintWriter writer = response.getWriter();
                        writer.write(content.toString());
                        writer.flush();
                    }
                }
            }

            resource.discard();

        } catch (RegistryException e) {
            String msg = "Failed to get resource content. " + e.getMessage();
            log.error(msg, e);
            response.setStatus(500);
            return;
        }
    }
}
