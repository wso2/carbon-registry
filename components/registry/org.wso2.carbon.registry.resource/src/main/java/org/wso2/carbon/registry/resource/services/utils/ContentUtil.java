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

package org.wso2.carbon.registry.resource.services.utils;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.wso2.carbon.registry.common.CommonConstants;
import org.wso2.carbon.registry.common.ResourceData;
import org.wso2.carbon.registry.common.utils.UserUtil;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.config.RemoteConfiguration;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.pagination.PaginationUtils;
import org.wso2.carbon.registry.core.secure.AuthorizationFailedException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.beans.CollectionContentBean;
import org.wso2.carbon.registry.resource.beans.ContentBean;
import org.wso2.carbon.registry.resource.beans.ContentDownloadBean;
import org.wso2.carbon.registry.resource.download.DownloadManagerService;
import org.wso2.carbon.utils.CarbonUtils;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.namespace.QName;
import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ContentUtil {

    private static final Log log = LogFactory.getLog(ContentUtil.class);
    private static DownloadManagerService downloadManagerService;

    public static void setDownloadManagerService(DownloadManagerService downloadManagerService) {
        ContentUtil.downloadManagerService = downloadManagerService;
    }

    public static CollectionContentBean getCollectionContent(String path,
                                       UserRegistry registry) throws Exception {

        try {
            Resource resource = registry.get(path);
            if (!(resource instanceof Collection)) {
                String msg = "Attempted to get collection content from " +
                        "a non-collection resource " + path;
                log.error(msg);
                throw new RegistryException(msg);
            }

            Collection collection = (Collection) resource;
            String[] childPaths = collection.getChildren();
            MessageContext messageContext = MessageContext.getCurrentMessageContext();
            String[] paginatedResult;
            if (messageContext != null && PaginationUtils.isPaginationHeadersExist(messageContext)) {

                int rowCount = childPaths.length;
                try {
                    PaginationUtils.setRowCount(messageContext, Integer.toString(rowCount));
                    PaginationContext paginationContext = PaginationUtils.initPaginationContext(messageContext);

                    int start = paginationContext.getStart();
                    int count = paginationContext.getCount();

                    int startIndex;
                    if (start == 1) {
                        startIndex = 0;
                    } else {
                        startIndex = start;
                    }
                    if (rowCount < start + count) {
                        paginatedResult = new String[rowCount - startIndex];
                        System.arraycopy(childPaths, startIndex, paginatedResult, 0, (rowCount - startIndex));
                    } else {
                        paginatedResult = new String[count];
                        System.arraycopy(childPaths, startIndex, paginatedResult, 0,count);
                    }
                } finally {
                    PaginationContext.destroy();
                }
                childPaths = paginatedResult;
            }
            CollectionContentBean bean = new CollectionContentBean();
            bean.setChildPaths(childPaths);
            bean.setChildCount(childPaths.length);
            bean.setCollectionTypes(getCollectionTypes());
            if (registry.getRegistryContext() != null) {
                List remoteInstances =  registry.getRegistryContext().
                        getRemoteInstances();
                String[] instances = new String[remoteInstances.size()];
                for(int i=0; i<instances.length; i++) {
                    instances[i] = ((RemoteConfiguration)remoteInstances.get(i)).getId();
                }
                bean.setRemoteInstances(instances);
            }
            ResourcePath resourcePath = new ResourcePath(path);
            bean.setPathWithVersion(resourcePath.getPathWithVersion());
            bean.setVersionView(!resourcePath.isCurrentVersion());

            return bean;

        } catch (Exception e) {
            String msg = "Failed to get content details of the resource " + path +
                    ". Caused by: " + ((e.getCause() instanceof SQLException) ?
                    "" : e.getMessage());
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }

    public static ResourceData[] getResourceData(String[] childPaths,
                                                 UserRegistry registry) throws Exception {

        List <ResourceData> resourceDataList = new ArrayList <ResourceData> ();

        for (String childPath : childPaths) {

            try {
                if (childPath == null || childPath.length() == 0) {
                    continue;
                }
                Resource child = registry.get(childPath);

                ResourceData resourceData = new ResourceData();
                resourceData.setResourcePath(childPath); // + RegistryConstants.VIEW_ACTION);

                String[] parts = childPath.split(RegistryConstants.PATH_SEPARATOR);
                if (parts.length > 0) {
                    resourceData.setName(parts[parts.length - 1]);
                }

                resourceData.setResourceType(child instanceof Collection ?
                        CommonConstants.COLLECTION : CommonConstants.RESOURCE);
                resourceData.setAuthorUserName(child.getAuthorUserName());
                resourceData.setDescription(child.getDescription());
                resourceData.setAverageRating(registry.getAverageRating(child.getPath()));
                Calendar createDateTime = Calendar.getInstance();
                createDateTime.setTime(child.getCreatedTime());
                resourceData.setCreatedOn(createDateTime);
                List mountPoints = child.getPropertyValues("registry.mountpoint");
                List targetPoints = child.getPropertyValues("registry.targetpoint");
//                List paths = child.getPropertyValues("registry.path");
                List actualPaths = child.getPropertyValues("registry.actualpath");
                String user = child.getProperty("registry.user");
                if (child.getProperty("registry.mount") != null) {
                    resourceData.setMounted(true);
                }
                if (child.getProperty("registry.link") != null) {
                    resourceData.setLink(true);

                    if(mountPoints != null && targetPoints != null) {
//                        String mountPoint = (String)mountPoints.get(0);
//                        String targetPoint = (String)targetPoints.get(0);
//                        String tempPath;
//                        if (targetPoint.equals(RegistryConstants.PATH_SEPARATOR) && !childPath.equals(mountPoint)) {
//                            tempPath = ((String)paths.get(0)).substring(mountPoint.length());
//                        } else {
//                            tempPath = targetPoint + ((String)paths.get(0)).substring(mountPoint.length());
//                        }
                        String tempPath = (String)actualPaths.get(0);
                        resourceData.setPutAllowed(
                        UserUtil.isPutAllowed(registry.getUserName(), tempPath, registry));
                        resourceData.setDeleteAllowed(UserUtil.isDeleteAllowed(registry.getUserName(),
                                 tempPath, registry));
                        resourceData.setGetAllowed(UserUtil.isGetAllowed(registry.getUserName(), tempPath, registry));
                        resourceData.setRealPath(tempPath);
                    } else if (user != null) {
                        if (registry.getUserName().equals(user)) {
                            resourceData.setPutAllowed(true);
                            resourceData.setDeleteAllowed(true);
                            resourceData.setGetAllowed(true);
                        } else {
                            resourceData.setPutAllowed(
                        UserUtil.isPutAllowed(registry.getUserName(), childPath, registry));
                            resourceData.setDeleteAllowed(
                        UserUtil.isDeleteAllowed(registry.getUserName(), childPath, registry));
                            resourceData.setGetAllowed(
                        UserUtil.isGetAllowed(registry.getUserName(), childPath, registry));
                        }
                        // Mounted resources should be accessed via the link, and we need not set
                        // the real path.
                    }
                } else {
                    resourceData.setPutAllowed(
                        UserUtil.isPutAllowed(registry.getUserName(), childPath, registry));
                    resourceData.setDeleteAllowed(
                        UserUtil.isDeleteAllowed(registry.getUserName(), childPath, registry));
                    resourceData.setGetAllowed(
                        UserUtil.isGetAllowed(registry.getUserName(), childPath, registry));
                }

                calculateAverageStars(resourceData);

                if(child.getProperty("registry.externalLink") != null) {
                    resourceData.setExternalLink(true);
                }
                if(child.getProperty("registry.absent") != null){
                    resourceData.setAbsent(child.getProperty("registry.absent"));
                }
                resourceDataList.add(resourceData);

            } catch (AuthorizationFailedException ignore) {
                // if we get an auth failed exception while accessing a child, we simply skip it.
                // we are not showing unauthorized resources.
            }
        }

        return resourceDataList.toArray(new ResourceData[resourceDataList.size()]);
    }

    public static ContentBean getContent(String path, UserRegistry registry) throws Exception {

        ResourcePath resourcePath = new ResourcePath(path);
        ContentBean bean = new ContentBean();

        Resource resource = registry.get(path);
        bean.setMediaType(resource.getMediaType());
        bean.setCollection(resource instanceof Collection);
        bean.setLoggedIn(!RegistryConstants.ANONYMOUS_USER.equals(registry.getUserName()));
        bean.setPathWithVersion(resourcePath.getPathWithVersion());
        bean.setAbsent(resource.getProperty("registry.absent"));
        List mountPoints = resource.getPropertyValues("registry.mountpoint");
        List targetPoints = resource.getPropertyValues("registry.targetpoint");
//        List paths = resource.getPropertyValues("registry.path");
        List actualPaths = resource.getPropertyValues("registry.actualpath"); 
        String user = resource.getProperty("registry.user");

        if (resource.getProperty("registry.link") != null) {

            if (mountPoints != null && targetPoints != null) {
//                String mountPoint = (String)mountPoints.get(0);
//                String targetPoint = (String)targetPoints.get(0);
//                String tempPath;
//                if (targetPoint.equals(RegistryConstants.PATH_SEPARATOR) && !childPath.equals(mountPoint)) {
//                    tempPath = ((String)paths.get(0)).substring(mountPoint.length());
//                } else {
//                    tempPath = targetPoint + ((String)paths.get(0)).substring(mountPoint.length());
//                }
                String tempPath = (String)actualPaths.get(0);
                bean.setPutAllowed(
                        UserUtil.isPutAllowed(registry.getUserName(), tempPath, registry));
                bean.setRealPath(tempPath);
            } else if (user != null) {
                if (registry.getUserName().equals(user)) {
                    bean.setPutAllowed(true);
                } else {
                    bean.setPutAllowed(
                        UserUtil.isPutAllowed(registry.getUserName(), path, registry));
                }
                // Mounted resources should be accessed via the link, and we need not set
                // the real path.
            }
        } else {
            boolean putAllowed = UserUtil.isPutAllowed(registry.getUserName(), path, registry);
            bean.setPutAllowed(putAllowed);
        }

        bean.setVersionView(!resourcePath.isCurrentVersion());
        bean.setContentPath(resourcePath.getCompletePath());
        resource.discard();
        
        return bean;
    }

    private static String[] getCollectionTypes() {
        return new String[] {"default", "Axis2 repository", "Synapse repository"};
    }

    private static void calculateAverageStars(ResourceData resourceData) {

        float tempRating = resourceData.getAverageRating() * 1000;
        tempRating = Math.round(tempRating);
        tempRating = tempRating / 1000;
        resourceData.setAverageRating(tempRating);

        float averageRating = resourceData.getAverageRating();
        String[] averageStars = new String[5];

        for (int i = 0; i < 5; i++) {

            if (averageRating >= i + 1) {
                averageStars[i] = "04";

            } else if (averageRating <= i) {
                averageStars[i] = "00";

            } else {

                float fraction = averageRating - i;

                if (fraction <= 0.125) {
                    averageStars[i] = "00";

                } else if (fraction > 0.125 && fraction <= 0.375) {
                    averageStars[i] = "01";

                } else if (fraction > 0.375 && fraction <= 0.625) {
                    averageStars[i] = "02";

                } else if (fraction > 0.625 && fraction <= 0.875) {
                    averageStars[i] = "03";

                } else {
                    averageStars[i] = "04";

                }
            }
        }

        resourceData.setAverageStars(averageStars);
    }

    public static boolean hasAssociations(String path,String type,UserRegistry registry) throws Exception {

        if(type == null || path == null) {
         return false;
        }
        Association [] associations = registry.getAssociations(path,type);
        for(Association association:associations){
            if (association.getAssociationType() != null && association.getAssociationType().equals(type)
                    && association.getSourcePath().equals(path)) {
                return true;
            }
        }
      return false;
    }

    public static ContentDownloadBean getContentWithDependencies(String path,UserRegistry registry) throws Exception {
        if(downloadManagerService != null) {
            return downloadManagerService.getDownloadContent(path,registry);
        } else {
           return getDownloadContent(path,registry);
        }
    }


    private static ContentDownloadBean getDownloadContent(String path, Registry _registry) throws Exception {
        UserRegistry registry = (UserRegistry)_registry;
        InputStream zipContentStream = null;
        File srcDir = null;
        File zipFile = null;
        String COLLECTION = "collection";
        ByteArrayOutputStream outputStream = null;
        ContentDownloadBean zipContent;
        try {

            if (path == null) {
                String msg = "Could not get the resource content. Path is not specified.";
                log.error(msg);
                return null;
            }

            ContentDownloadBean bean = GetDownloadContentUtil.getContentDownloadBean(path, registry);
            String zipDirPath = CarbonUtils.getCarbonHome() + File.separator + "tmp" + File.separator + bean.toString();
            String zipDependencyPath = zipDirPath +File.separator + "dependencies";
            String zipPath = zipDirPath + "-zip.zip";

            Association[] associations = registry.getAssociations(path,"depends");

            if (associations.length != 0) {
                srcDir = new File(zipDirPath);
                srcDir.mkdir();
                new File(zipDependencyPath).mkdir();
                zipFile = new File(zipPath);

     //                Creating artifact file itself
                File _tmp = new File(zipDirPath + File.separator + bean.getResourceName());
                DataOutputStream _fos = new DataOutputStream(new FileOutputStream(_tmp));
                byte[] _bytes = IOUtils.toByteArray(bean.getContent().getInputStream());

                createDependencies(associations,registry,zipDependencyPath,path,COLLECTION, new String(_bytes),_fos,true);

                ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
                zipDir(zipDirPath, zos);
                zos.close();
                zipContentStream = new FileInputStream(new File(zipPath));

            } else if (bean.getContent() != null) {
                zipContentStream = bean.getContent().getInputStream();
            } else {
                String msg = "The resource content was empty.";
                log.error(msg);
                return null;
            }

            if (zipContentStream != null) {

                try {
                    outputStream = new ByteArrayOutputStream();

                    byte[] contentChunk = new byte[1024];
                    int byteCount;
                    while ((byteCount = zipContentStream.read(contentChunk)) != -1) {
                        outputStream.write(contentChunk, 0, byteCount);
                    }
                    outputStream.flush();

                } finally {
                    zipContentStream.close();

                    if (outputStream != null) {
                        outputStream.close();
                    }
                }
            }
        zipContent = new ContentDownloadBean();
        DataSource contentSource = new InputStreamBasedDataSource(new ByteArrayInputStream(outputStream.toByteArray()));
        DataHandler content = new DataHandler(contentSource);
        zipContent.setContent(content);

        } catch (RegistryException e) {
            String msg = "Failed to get resource content. " + e.getMessage();
            log.error(msg, e);
            return null;
        } finally {
            FileUtils.deleteQuietly(srcDir);
            FileUtils.deleteQuietly(zipFile);
        }

        return zipContent;
    }

        private static void createDependencies(Association[] associations, UserRegistry registry, String zipDependencyPath,
                                           String scrPath, String COLLECTION,String content,DataOutputStream srcOutputStream,boolean isMasterArtifact) throws Exception {
        for (Association associationBean : associations) {
            if (isADependency(associationBean, registry, scrPath, COLLECTION)) {
                ContentDownloadBean dependencyBean = GetDownloadContentUtil.getContentDownloadBean(associationBean.getDestinationPath(), registry);
                InputStream dependencyContentStream = dependencyBean.getContent().getInputStream();
                File tmp = new File(zipDependencyPath + File.separator + dependencyBean.getResourceName());
                DataOutputStream fos = new DataOutputStream(new FileOutputStream(tmp));
                byte[] bytes = IOUtils.toByteArray(dependencyContentStream);
                createDependencies(registry.getAssociations(associationBean.getDestinationPath(),"depends"),
                        registry,zipDependencyPath,associationBean.getDestinationPath(),COLLECTION,new String(bytes),fos,false);
            }
        }

        if(scrPath.endsWith(".wsdl") || scrPath.endsWith(".xsd")) {
            OMElement srcOMElement = AXIOMUtil.stringToOM(content);
            updateSchemaImports(srcOMElement,isMasterArtifact);
            updateWSDLImports(srcOMElement,isMasterArtifact);
            IOUtils.write(srcOMElement.toString().getBytes(), srcOutputStream);
        } else {
            IOUtils.write(content.getBytes(), srcOutputStream);
        }

    }

    private static boolean isADependency(Association associationBean,UserRegistry registry,String scrPath,String COLLECTION) throws Exception {
        if(associationBean.getDestinationPath() == null ||
                (!registry.resourceExists(associationBean.getDestinationPath()))){
            return false;
        }
        ResourceData resourceData = ContentUtil.getResourceData(new String[]{associationBean.getDestinationPath()}, registry)[0];
          boolean isCollection = resourceData.getResourceType().equals(COLLECTION);
          return (associationBean.getAssociationType() != null && associationBean.getAssociationType().equals("depends")
                  && associationBean.getSourcePath().equals(scrPath) && !isCollection);
    }


    private static OMElement updateSchemaImports(OMElement omElement,boolean isMasterArtifact) throws JaxenException {

        AXIOMXPath xPath = new AXIOMXPath("//xs:schema/xs:import[@schemaLocation]");
//        "http://schemas.xmlsoap.org/wsdl/"
        xPath.addNamespace("xs", "http://www.w3.org/2001/XMLSchema");
        Object result = xPath.evaluate(omElement);
        if(!(result instanceof ArrayList)){
         return omElement;
        }
        List list = (ArrayList)result;
        for (Object obj : list) {
            OMElement _import = (OMElement) obj;
            OMAttribute attribute = _import.getAttribute(new QName("schemaLocation"));
            String newValue = isMasterArtifact ? "dependencies" + attribute.getAttributeValue().substring(attribute.getAttributeValue().lastIndexOf("/"))
                    :attribute.getAttributeValue().substring(attribute.getAttributeValue().lastIndexOf("/")+1);
            attribute.setAttributeValue(newValue);
        }
      return omElement;
    }

    private static OMElement updateWSDLImports(OMElement omElement,boolean isMasterArtifact) throws JaxenException {

        AXIOMXPath xPath = new AXIOMXPath("//wsd:import[@location]");
        xPath.addNamespace("wsd", "http://schemas.xmlsoap.org/wsdl/");

        Object result = xPath.evaluate(omElement);
        if(!(result instanceof ArrayList)){
         return omElement;
        }
        List list = (ArrayList) result;
        for (Object obj : list) {
            OMElement _import = (OMElement) obj;
            OMAttribute attribute = _import.getAttribute(new QName("location"));
            String newValue = isMasterArtifact ? "dependencies" + attribute.getAttributeValue().substring(attribute.getAttributeValue().lastIndexOf("/"))
                    :attribute.getAttributeValue().substring(attribute.getAttributeValue().lastIndexOf("/")+1);
            attribute.setAttributeValue(newValue);
        }
      return omElement;
    }


    private static void zipDir(String dirToZip, ZipOutputStream zos) throws org.wso2.carbon.registry.api.RegistryException {
        try {

            File zipDir = new File(dirToZip);
            String[] dirList = zipDir.list();
            byte[] readBuffer = new byte[1024];
            int bytesIn = 0;
            for (int i = 0; i < dirList.length; i++) {
                File f = new File(zipDir,dirList[i]);
                if(f.isDirectory()) {
                  zipDir(f.getPath(),zos);
                  continue;
                } else {
                FileInputStream fis = new FileInputStream(f.getPath());
                ZipEntry anEntry = new ZipEntry(f.getPath().contains("dependencies") ? "dependencies" + File.separator + f.getName():f.getName());
                zos.putNextEntry(anEntry);
                while ((bytesIn = fis.read(readBuffer)) != -1) {
                    zos.write(readBuffer, 0, bytesIn);
                }
                zos.flush();
                fis.close();
            }
            }

        } catch (Exception e) {
            throw new org.wso2.carbon.registry.api.RegistryException("Error occurred while zipping the file");
        }
    }

}

