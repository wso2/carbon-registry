/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.extensions.handlers.utils;

import com.ibm.wsdl.Constants;
import com.ibm.wsdl.util.xml.DOMUtils;
import com.ibm.wsdl.xml.WSDLReaderImpl;
import org.w3c.dom.Element;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;

import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;
import java.io.File;
import java.util.*;

public class ExWSDLReaderImpl extends WSDLReaderImpl {

    WSDLReaderImpl reader = null;

    public ExWSDLReaderImpl(WSDLReaderImpl reader) {
        this.reader = reader;
    }

    protected Definition parseDefinitions(String documentBaseURI,
                                          Element defEl,
                                          Map importedDefs)
            throws WSDLException {
        Definition def = super.parseDefinitions(documentBaseURI, defEl, importedDefs);
        List<Import> toRemove = new LinkedList<Import>();
        Map imports = def.getImports();
        if (imports.size() > 0) {
            Iterator iterator = imports.values().iterator();
            for (; iterator.hasNext();) {
                Vector values = (Vector) iterator.next();
                for (Object value : values) {
                    Import wsdlImport = (Import) value;
                    if (wsdlImport.getDefinition() == null) {
                        toRemove.add(wsdlImport);
                    }
                }
            }
        }
        for (Import wsdlImport : toRemove) {
            def.removeImport(wsdlImport);
        }
        return def;
    }

    protected Import parseImport(Element importEl,
                                 Definition def,
                                 Map importedDefs)
            throws WSDLException {
        if (/*CommonUtil.isPathMapExisting()*/true) {
            String location = new File(def.getDocumentBaseURI(),
                    DOMUtils.getAttribute(importEl, Constants.ATTR_LOCATION)).toString();
            if (CommonUtil.isImportedArtifactExisting(location)) {
                return new Import() {
                    public void setNamespaceURI(String s) {

                    }

                    public String getNamespaceURI() {
                        return null;
                    }

                    public void setLocationURI(String s) {

                    }

                    public String getLocationURI() {
                        return null;
                    }

                    public void setDefinition(Definition definition) {

                    }

                    public Definition getDefinition() {
                        return null;
                    }

                    public void setDocumentationElement(Element element) {

                    }

                    public Element getDocumentationElement() {
                        return null;
                    }

                    public void setExtensionAttribute(QName qName, Object o) {

                    }

                    public Object getExtensionAttribute(QName qName) {
                        return null;
                    }

                    public Map getExtensionAttributes() {
                        return null;
                    }

                    public List getNativeAttributeNames() {
                        return null;
                    }

                    public void addExtensibilityElement(ExtensibilityElement extensibilityElement) {

                    }

                    public ExtensibilityElement removeExtensibilityElement(
                            ExtensibilityElement extensibilityElement) {
                        return null;
                    }

                    public List getExtensibilityElements() {
                        return null;
                    }
                };
            }
            Import anImport = super.parseImport(importEl, def, importedDefs);
            CommonUtil.addImportedArtifact(location);
            return anImport;
        }
        return super.parseImport(importEl, def, importedDefs);
    }

    public int hashCode() {
        return reader.hashCode();
    }

    public boolean equals(Object obj) {
        return obj instanceof ExWSDLReaderImpl && reader.equals(obj);
    }

    public String toString() {
        return reader.toString();
    }
}
