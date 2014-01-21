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

package org.wso2.carbon.registry.resource.ui.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.ContentDownloadBean;
import org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient;

import javax.activation.DataHandler;
import javax.servlet.ServletConfig;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class GetResourceContentProcessor {

    private static final Log log = LogFactory.getLog(GetResourceContentProcessor.class);

    public static void getContent(HttpServletRequest request, HttpServletResponse response, ServletConfig config)
            throws Exception {

        try {
            ResourceServiceClient client = new ResourceServiceClient(config, request.getSession());
            String path = request.getParameter("path");
            if (path == null) {
                String msg = "Could not get the resource content. Path is not specified.";
                log.error(msg);
                response.setStatus(400);
                return;
            }

            ContentDownloadBean bean = client.getContentDownloadBean(path);

            InputStream contentStream = null;
            if (bean.getContent() != null) {
                contentStream = bean.getContent().getInputStream();
            } else {
                String msg = "The resource content was empty.";
                log.error(msg);
                response.setStatus(204);
                return;
            }

            response.setDateHeader("Last-Modified", bean.getLastUpdatedTime().getTime().getTime());

            if (bean.getMediatype() != null && bean.getMediatype().length() > 0) {
                response.setContentType(bean.getMediatype());
            } else {
                response.setContentType("application/download");
            }

            if (bean.getResourceName() != null) {
                response.setHeader(
                        "Content-Disposition", "attachment; filename=\"" + bean.getResourceName() + "\"");
            }

            if (contentStream != null) {

                ServletOutputStream servletOutputStream = null;
                try {
                    servletOutputStream = response.getOutputStream();

                    byte[] contentChunk = new byte[1024];
                    int byteCount;
                    while ((byteCount = contentStream.read(contentChunk)) != -1) {
                        servletOutputStream.write(contentChunk, 0, byteCount);
                    }

                    response.flushBuffer();
                    servletOutputStream.flush();

                } finally {
                    contentStream.close();

                    if (servletOutputStream != null) {
                        servletOutputStream.close();
                    }
                }
            }

        } catch (RegistryException e) {
            String msg = "Failed to get resource content. " + e.getMessage();
            log.error(msg, e);
            response.setStatus(500);
            return;
        }
    }

    public static void getContentWithDependencies(HttpServletRequest request, HttpServletResponse response, ServletConfig config)
            throws Exception {

        try {
            String path = request.getParameter("path");
            if (path == null) {
                String msg = "Could not get the resource content. Path is not specified.";
                log.error(msg);
                response.setStatus(400);
                return;
            }

            ResourceServiceClient client = new ResourceServiceClient(config, request.getSession());
            ContentDownloadBean bean = client.getContentDownloadBean(path);
            String downloadName = bean.getResourceName() + ".zip";

            response.setDateHeader("Last-Modified", bean.getLastUpdatedTime().getTime().getTime());
            InputStream zipContentStream = null;

            ContentDownloadBean zipContentBean = client.getZipWithDependencies(path);
            if (zipContentBean == null || (zipContentBean != null && zipContentBean.getContent() == null)) {
                String msg = "Error occurred while streaming the resource with its dependencies : path =" + path;
                log.error(msg);
                response.setStatus(204);
                return;
            }

            zipContentStream = zipContentBean.getContent().getInputStream();

            if (zipContentStream == null) {
                String msg = "Error occurred while streaming the resource with its dependencies : path =" + path;
                log.error(msg);
                response.setStatus(204);
                return;
            }

            if (bean.getMediatype() != null && bean.getMediatype().length() > 0) {
                response.setContentType(bean.getMediatype());
            } else {
                response.setContentType("application/download");
            }

            if (bean.getResourceName() != null) {
                response.setHeader(
                        "Content-Disposition", "attachment; filename=\"" + downloadName + "\"");
            }

            if (zipContentStream != null) {

                ServletOutputStream servletOutputStream = null;
                try {
                    servletOutputStream = response.getOutputStream();

                    byte[] contentChunk = new byte[1024];
                    int byteCount;
                    while ((byteCount = zipContentStream.read(contentChunk)) != -1) {
                        servletOutputStream.write(contentChunk, 0, byteCount);
                    }

                    response.flushBuffer();
                    servletOutputStream.flush();

                } finally {
                    zipContentStream.close();

                    if (servletOutputStream != null) {
                        servletOutputStream.close();
                    }
                }
            }

        } catch (Exception e) {
            String msg = "Failed to get resource content. " + e.getMessage();
            log.error(msg, e);
            response.setStatus(500);
            return;
        }
    }
}
