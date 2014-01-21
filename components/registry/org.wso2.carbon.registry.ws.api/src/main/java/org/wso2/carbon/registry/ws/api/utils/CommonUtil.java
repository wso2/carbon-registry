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
/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.com) All Rights Reserved.
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


package org.wso2.carbon.registry.ws.api.utils;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.*;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.servlet.http.HttpServletRequest;

import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.ws.api.*;

public class CommonUtil {
    private static final Log log = LogFactory.getLog(CommonUtil.class);

    private static RegistryService registryService;

    public static synchronized void setRegistryService(RegistryService service) {
        registryService = service;
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static Map createMap(String[] key,String[] value)throws RegistryException{
        Map map = new HashMap();
        try{
            if(key.length == value.length){
                for(int i=0;i<value.length;i++){
                    map.put(key[i],value[i]);
                }
                return map;
            }
        }catch(Exception e){
            throw new RegistryException("Wrong key value pair");
        }
        return null;
    }
    
    public static Resource transformWSResourcetoResource(WSResource wsResource, Object content)throws RegistryException{
        ResourceImpl resource = null;
        if (wsResource.isCollection()) {
            resource = new CollectionImpl();
        }
        else {
            resource = new ResourceImpl();
            resource.setContent(content);
        }
        
        if (wsResource.getDescription() != null) resource.setDescription(wsResource.getDescription());
        if (wsResource.getMediaType() != null) resource.setMediaType(wsResource.getMediaType());
        if (wsResource.getProperties() != null) resource.setProperties(getPropertiesForResource(wsResource.getProperties(), resource.getProperties()));
        if (wsResource.getAuthorUserName() != null) resource.setAuthorUserName(wsResource.getAuthorUserName());
        resource.setCreatedTime(new Date(wsResource.getCreatedTime()));
        if (wsResource.getId() != null) resource.setId(wsResource.getId());
        resource.setLastModified(new Date(wsResource.getLastModified()));
        if (wsResource.getLastUpdaterUserName() != null) resource.setLastUpdaterUserName(wsResource.getLastUpdaterUserName());
        if (wsResource.getParentPath() != null) resource.setParentPath(wsResource.getParentPath());
        if (wsResource.getPath() != null) resource.setPath(wsResource.getPath());
        if (wsResource.getUUID() != null) resource.setUUID(wsResource.getUUID());
        return resource;
    }
    
    public static Collection transformWSCollectiontoCollection(WSResource wsCollection, Object content) throws RegistryException {
        Collection collection = (Collection) transformWSResourcetoResource(wsCollection, content);
        
        try {
			if (content != null) {
				InputStream in = new ByteArrayInputStream((byte[]) content);
				ObjectInputStream ois = new ObjectInputStream(in);
				Object object = ois.readObject();
				if (object instanceof String[]) {
					String[] strArray = (String[]) object;
					collection.setContent(strArray);
				}
			}
		} catch (IOException e) {
			throw new RegistryException("Error forming String array from dataHandler for Collection", e);
		} catch (ClassNotFoundException e) {
			throw new RegistryException("Error forming String array from dataHandler for Collection", e);
		}
        return collection;
		
    }

    public static WSCollection transformCollectiontoWSCollection(Collection collection, DataHandler dataHandler) {
        WSCollection wsCollection = (WSCollection) transformResourceToWSResource(collection, dataHandler);
        wsCollection.setCollection(true);
        //         wsCollection.setChildCount(collection.getChildCount());
        return wsCollection;

    }

    public static WSResource transformResourceToWSResource(Resource resource, DataHandler dataHandler) {
        WSResource wsResource = null;
        if (resource instanceof Collection) {
            wsResource = new WSCollection();
        }
        else {
            wsResource = new WSResource();             
        }
        wsResource.setContentFile(dataHandler);
        wsResource.setAuthorUserName(resource.getAuthorUserName());

        if (resource.getCreatedTime() != null) wsResource.setCreatedTime(resource.getCreatedTime().getTime());
        //         wsResource.setDbBasedContentID(resource)
        wsResource.setDescription(resource.getDescription());

        wsResource.setId(resource.getId());
        if (resource.getLastModified() != null) wsResource.setLastModified(resource.getLastModified().getTime());
        wsResource.setLastUpdaterUserName(resource.getLastUpdaterUserName());
        if (resource instanceof ResourceImpl) {
            wsResource.setMatchingSnapshotID(((ResourceImpl)resource).getMatchingSnapshotID());
        }
        wsResource.setMediaType(resource.getMediaType());
        //         wsResource.setName(resource.)
        wsResource.setParentPath(resource.getParentPath());
        wsResource.setPath(resource.getPath());
        //         wsResource.setPathID();
        wsResource.setPermanentPath(resource.getPermanentPath());
        if (resource.getProperties() != null) wsResource.setProperties(getPropertiesForWSResource(resource.getProperties()));
        wsResource.setState(resource.getState());
        wsResource.setUUID(resource.getUUID());
        //         resource.get
        return wsResource;
    }

    public static byte[] makeBytesFromDataHandler(WSResource wsResource) throws IOException{
        DataHandler dataHandler = wsResource.getContentFile();
        if (dataHandler == null) return null;

        ByteArrayOutputStream output = null;
        //             OutputStream output = new FileOutputStream(tempFile);
        output = new ByteArrayOutputStream();
        try {
            dataHandler.writeTo(output);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            throw e;
        }

        return output.toByteArray();
    }

    public static DataHandler makeDataHandler(Resource resource, File tempFile) throws IOException, RegistryException{
        if (resource.getContent() == null) {
            return null;
        }
        InputStream is = null;
        OutputStream os = null;
        try {
            os = new FileOutputStream(tempFile);
            if (resource.getContent() instanceof String[]) {
                String[] strArray = (String[]) resource.getContent();

                ObjectOutputStream oos = new ObjectOutputStream(os);
                oos.writeObject(strArray);
            } else {
                try {
                    is = resource.getContentStream();
//                    os = new FileOutputStream(tempFile);

                    byte[] buffer = new byte[4096];
                    for (int n; (n = is.read(buffer)) != -1; )
                        os.write(buffer, 0, n);
                    os.flush();
                } finally {
                    if (is != null) {
                        is.close();
                    }
                }
            }
        } finally {
            if (os != null) {
                os.close();
            }
        }
        //         Base64Binary base64Binary = new Base64Binary();
        return new DataHandler(new FileDataSource(tempFile));
    }


    public static WSResource newResourcetoWSResource(Resource resource) {
        WSResource wsResource = new WSResource();
        //         wsResource.setAuthorUserName(resource.getAuthorUserName());
        //         wsResource.setId(resource.getId());
        //         wsResource.setResource(resource);
        return wsResource;
    }

    public static WSCollection newCollectiontoWSCollection(Collection collection) {
        WSCollection wsCollection = new WSCollection();
        //         wsCollection.setCollection(collection);
        return wsCollection;

    }
    public static WSTaggedResourcePath[] exchangeTaggedResourcepath(TaggedResourcePath[] registry){

        if(registry == null){
            return null;
        }
        WSTaggedResourcePath[] wsTRP = new WSTaggedResourcePath[registry.length];
        for(int i=0;i<registry.length;i++) {
            wsTRP[i] = new WSTaggedResourcePath();
            wsTRP[i].setResourcePath(registry[i].getResourcePath());
            wsTRP[i].setTagCount(registry[i].getTagCount());
            int size = registry[i].getTagCounts().keySet().size();
            String[] key = registry[i].getTagCounts().keySet().toArray(new String[size]);
            WSMap[] map = new WSMap[size];
            for(int j=0;j<size;j++){
                map[j] = new WSMap();
                map[j].setKey(key[j]);
                map[j].setValue(registry[i].getTagCounts().get(key[j]));
            }
            wsTRP[i].setTagCounts(map);
            
        }
        return wsTRP;
    }
    public static TaggedResourcePath[] exchangeWSResourcePath(WSTaggedResourcePath[] wsrpath){
        if(wsrpath == null){
            return null;
        }
        TaggedResourcePath[] trPath = new TaggedResourcePath[wsrpath.length];
        for(int i=0;i<wsrpath.length;i++){
            trPath[i] = new TaggedResourcePath();
            Map pathmap = new HashMap();
            trPath[i].setResourcePath(wsrpath[i].getResourcePath());
            trPath[i].setTagCount(wsrpath[i].getTagCount());
            WSMap[] map = wsrpath[i].getTagCounts();
            for(int j=0;j<map.length;j++){
                pathmap.put(map[j].getKey(),Long.parseLong(map[j].getValue()));
            }
            trPath[i].setTagCounts(pathmap);
        }
        return trPath;

    }
    public static WSComment RegistryCommenttoWSComment(Comment comment){
        WSComment wsComment = new WSComment();
        wsComment.setCommentPath(comment.getCommentPath());
        wsComment.setCommentID(comment.getCommentID());
        wsComment.setResourcePath(comment.getResourcePath());
        if (comment.getPath() != null) wsComment.setPath(comment.getPath());
        wsComment.setText(comment.getText());
        if (comment.getCreatedTime() != null) wsComment.setTime(comment.getCreatedTime().getTime());
        if (comment.getUser() != null) wsComment.setUser(comment.getUser());
        return wsComment;
    }
    public static Comment WSCommenttoRegistryComment(WSComment wsComment){
        Comment rcomment = new Comment();
        if (wsComment.getCommentPath() != null) rcomment.setCommentPath(wsComment.getCommentPath());
        if (wsComment.getPath() != null) rcomment.setPath(wsComment.getPath());
        rcomment.setCommentID(wsComment.getCommentID());
        rcomment.setResourcePath(wsComment.getResourcePath());
        rcomment.setText(wsComment.getText());
        rcomment.setTime(new Date(wsComment.getTime()));
        if (wsComment.getUser() != null) rcomment.setUser(wsComment.getUser());
        return rcomment;
    }
    

    private static WSProperty[] getPropertiesForWSResource(Properties props){
        Enumeration keys = props.propertyNames();
        Enumeration keys2 = props.propertyNames();
        int size =0,i=0;
        while(keys.hasMoreElements()){
            keys.nextElement();
            size++;
        }

        String[] keyarray = new String[size];
        while(keys2.hasMoreElements()){
            keyarray[i] = (String)keys2.nextElement();
            i++;
        }
        // To avoid property element not being created in soap message
        if (keyarray.length == 0) {
            WSProperty[] properties = new WSProperty[1];
            properties[0] = new WSProperty();
            return properties;
        }
        WSProperty[] properties = new WSProperty[keyarray.length];

        for(i = 0; i < keyarray.length; i++){
            properties[i] = new WSProperty();
            properties[i].setKey(keyarray[i]);
            List<String> list = (List<String>)props.get(keyarray[i]);
            String[] values = new String[list.size()];
            
            int j = 0;
            for (String str : list ) {
                values[j] = str;
                j++;
            }
            properties[i].setValues(values);
        }
        return properties;
    }
    
    private static Properties getPropertiesForResource(WSProperty[] wsprops,Properties properties){
        for(int i=0;i<wsprops.length;i++){
            if (wsprops[i].getValues() == null) {
                properties.put(wsprops[i].getKey(), new LinkedList<String>());
            }
            else {
                properties.put(wsprops[i].getKey(),new LinkedList<String>(
                        Arrays.asList(wsprops[i].getValues())));
            }
        }
        return properties;
    }

    public static WSAssociation transformAssociationToWSAssociation(Association asso) {
        return new WSAssociation(asso.getSourcePath(),
                asso.getDestinationPath(), asso.getAssociationType());
    }

    public static WSLogEntry transformLogEntryToWSLogEntry(LogEntry logEntry) {
        WSLogEntry wsLogEntry = new WSLogEntry();
        wsLogEntry.setResourcePath(logEntry.getResourcePath());
        wsLogEntry.setUserName(logEntry.getUserName());
        wsLogEntry.setDate(logEntry.getDate().getTime());
        wsLogEntry.setAction(logEntry.getAction());
        wsLogEntry.setActionData(logEntry.getActionData());

        return wsLogEntry;
    }

    public static WSTag transformTagToWSTag(Tag tag) {
        WSTag wsTag = new WSTag();
        wsTag.setCategory(tag.getCategory());
        wsTag.setTagName(tag.getTagName());
        wsTag.setTagCount(tag.getTagCount());
        return wsTag;
    }

}
