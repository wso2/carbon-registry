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

package org.wso2.carbon.registry.common.ui.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.server.admin.common.IServerAdmin;
import org.wso2.carbon.server.admin.ui.ServerAdminClient;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.ui.clients.RegistryAdminServiceClient;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.StringReader;

public class UIUtil {

    private static final String[] hexamap = {
        "%00", "%01", "%02", "%03", "%04", "%05", "%06", "%07", "%08", "%09", "%0A", "%0B", "%0C", "%0D", "%0E", "%0F",
        "%10", "%11", "%12", "%13", "%14", "%15", "%16", "%17", "%18", "%19", "%1A", "%1B", "%1C", "%1D", "%1E", "%1F",
        "%20", "%21", "%22", "%23", "%24", "%25", "%26", "%27", "%28", "%29", "%2A", "%2B", "%2C", "%2D", "%2E", "%2F",
        "%30", "%31", "%32", "%33", "%34", "%35", "%36", "%37", "%38", "%39", "%3A", "%3B", "%3C", "%3D", "%3E", "%3F",
        "%40", "%41", "%42", "%43", "%44", "%45", "%46", "%47", "%48", "%49", "%4A", "%4B", "%4C", "%4D", "%4E", "%4F",
        "%50", "%51", "%52", "%53", "%54", "%55", "%56", "%57", "%58", "%59", "%5A", "%5B", "%5C", "%5D", "%5E", "%5F",
        "%60", "%61", "%62", "%63", "%64", "%65", "%66", "%67", "%68", "%69", "%6A", "%6B", "%6C", "%6D", "%6E", "%6F",
        "%70", "%71", "%72", "%73", "%74", "%75", "%76", "%77", "%78", "%79", "%7A", "%7B", "%7C", "%7D", "%7E", "%7F",
        "%80", "%81", "%82", "%83", "%84", "%85", "%86", "%87", "%88", "%89", "%8A", "%8B", "%8C", "%8D", "%8E", "%8F",
        "%90", "%91", "%92", "%93", "%94", "%95", "%96", "%97", "%98", "%99", "%9A", "%9B", "%9C", "%9D", "%9E", "%9F",
        "%A0", "%A1", "%A2", "%A3", "%A4", "%A5", "%A6", "%A7", "%A8", "%A9", "%AA", "%AB", "%AC", "%AD", "%AE", "%AF",
        "%B0", "%B1", "%B2", "%B3", "%B4", "%B5", "%B6", "%B7", "%B8", "%B9", "%BA", "%BB", "%BC", "%BD", "%BE", "%BF",
        "%C0", "%C1", "%C2", "%C3", "%C4", "%C5", "%C6", "%C7", "%C8", "%C9", "%CA", "%CB", "%CC", "%CD", "%CE", "%CF",
        "%D0", "%D1", "%D2", "%D3", "%D4", "%D5", "%D6", "%D7", "%D8", "%D9", "%DA", "%DB", "%DC", "%DD", "%DE", "%DF",
        "%E0", "%E1", "%E2", "%E3", "%E4", "%E5", "%E6", "%E7", "%E8", "%E9", "%EA", "%EB", "%EC", "%ED", "%EE", "%EF",
        "%F0", "%F1", "%F2", "%F3", "%F4", "%F5", "%F6", "%F7", "%F8", "%F9", "%FA", "%FB", "%FC", "%FD", "%FE", "%FF"};

    public static String getContextRoot(HttpServletRequest request) {

        /*ServletContext context = request.getSession().getServletContext();
        HttpSession session = request.getSession();

        String serverURL = CarbonUIUtil.getServerURL(context, session);
        String serverRoot = serverURL.substring(0, serverURL.length() - "services/".length());
        serverRoot = serverRoot.substring(serverRoot.indexOf("//") + "//".length(), serverRoot.length());
        serverRoot = serverRoot.substring(serverRoot.indexOf("/") + "/".length(), serverRoot.length());
        return "/" + serverRoot;*/
        String contextRoot = request.getContextPath();
        if (contextRoot.startsWith("//")) {
            contextRoot = contextRoot.substring(1);
        }

        // We need a context root in the format '/foo/', for this logic to work.
        if (!contextRoot.startsWith(RegistryConstants.PATH_SEPARATOR)) {
            contextRoot = RegistryConstants.PATH_SEPARATOR + contextRoot;
        }
        if (!contextRoot.endsWith(RegistryConstants.PATH_SEPARATOR)) {
            contextRoot = contextRoot + RegistryConstants.PATH_SEPARATOR;
        }

        // Hack to obtain the correct context root.
        // The particular request that we get in here, due to some reason has a wrong context path.
        // TODO: Identify the true cause of this problem and fix this.
        if (contextRoot.endsWith("carbon/") && !request.getServletPath().startsWith("/carbon")) {
            contextRoot = contextRoot.substring(0, contextRoot.length() - "carbon/".length());
        }

        String tenantDomain = (String)request.getSession().getAttribute(MultitenantConstants.TENANT_DOMAIN);
        if (tenantDomain != null &&
                !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            contextRoot = "/"+ MultitenantConstants.TENANT_AWARE_URL_PREFIX + "/" + tenantDomain + contextRoot;
        }

        return contextRoot;
    }

    public static String getAtomURL(ServletConfig config, HttpServletRequest request, String resourcePath) {

        ServletContext context = request.getSession().getServletContext();
        HttpSession session = request.getSession();
        String remoteRegistryURL;
        String chroot = "";
        String cookie = (String)session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        try {
            String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext = (ConfigurationContext) config.
                    getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            IServerAdmin client =
                    (IServerAdmin) CarbonUIUtil.
                            getServerProxy(new ServerAdminClient(configContext,
                                    serverURL, cookie, session), IServerAdmin.class, session);

                RegistryAdminServiceClient registryAdminServiceClient =
                        new RegistryAdminServiceClient(cookie, config, session);
                remoteRegistryURL = registryAdminServiceClient.getRegistryHTTPSURL();
                if (remoteRegistryURL.equals("#")) {
                    remoteRegistryURL = null;
                } else {
                    remoteRegistryURL = remoteRegistryURL.substring(0, remoteRegistryURL.indexOf("/resource"));
                }


        } catch (Exception e) {
            remoteRegistryURL = null;
        }
        if (remoteRegistryURL == null) {
            String serverURL = CarbonUIUtil.getServerURL(context, session);
            String serverRoot = serverURL.substring(0, serverURL.length() - "services/".length());
            String tenantDomain = null;
            if(request.getSession().getAttribute(MultitenantConstants.TENANT_DOMAIN) != null) {
                tenantDomain = 	(String)request.getSession().getAttribute(MultitenantConstants.TENANT_DOMAIN);
            }
            else {
                tenantDomain = (String)request.getAttribute(MultitenantConstants.TENANT_DOMAIN);
            }
            if (tenantDomain != null &&
                    !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                return serverRoot + MultitenantConstants.TENANT_AWARE_URL_PREFIX + "/" + tenantDomain +
                        "/registry/atom" + encodeRegistryPath(resourcePath).replaceAll(" ", "+");
            } else {
                return serverRoot + "registry/atom" + encodeRegistryPath(resourcePath).replaceAll(" ", "+");
            }
        } else {
            if (remoteRegistryURL.endsWith("/")) {
                remoteRegistryURL = remoteRegistryURL.substring(0, remoteRegistryURL.length() - "/".length());
            }
            String serverRoot = remoteRegistryURL.substring(0, remoteRegistryURL.length() - "registry".length());
            return serverRoot + "registry/atom" + encodeRegistryPath(chroot + resourcePath).replaceAll(" ", "+");
        }
    }

    private static String encodeRegistryPath(String path) {
        if (path == null) {
            return null;
        }
        StringBuffer sbuf = new StringBuffer();
        int len = path.length();
        for (int i = 0; i < len; i++) {
          int ch = path.charAt(i);
          if (('A' <= ch && ch <= 'Z')          // alpha numeric characters
           || ('a' <= ch && ch <= 'z')
           || ('0' <= ch && ch <= '9')) {
               sbuf.append((char)ch);
          } else if (ch == '-' || ch == '_'		// allowed characters
                  || ch == '.' || ch == '/'
                  || ch == ' ' || ch == ';') {
            sbuf.append((char)ch);
          } else if (ch <= 0x007F) {		    // other ASCII
            sbuf.append(hexamap[ch]);
          } else if (ch <= 0x07FF) {		    // non-ASCII <= 0x7FF
            sbuf.append(hexamap[0xC0 | (ch >> 6)]);
            sbuf.append(hexamap[0x80 | (ch & 0x3F)]);
          } else {					            // 0x7FF < ch <= 0xFFFF
            sbuf.append(hexamap[0xE0 | (ch >> 12)]);
            sbuf.append(hexamap[0x80 | ((ch >> 6) & 0x3F)]);
            sbuf.append(hexamap[0x80 | (ch & 0x3F)]);
          }
        }
        return sbuf.toString();
    }

    public static String getFirstPage(int pageNumber, int pageLength, String[] allNodes) {
        if (allNodes == null || allNodes.length == 0) {
            return "";
        } else {
            int start = (pageNumber - 1) * pageLength;
            if (start < 0 || allNodes.length <= start) {
                return "";
            }
            String startName = allNodes[start];
            if (startName.indexOf("/") != -1) {
                startName = startName.substring(startName.lastIndexOf("/") + 1);
            }
            return startName;
        }
    }

    public static String getLastPage(int pageNumber, int pageLength, String[] allNodes) {
        if (allNodes == null || allNodes.length == 0) {
            return "";
        } else {
            int end = (pageNumber - 1) * pageLength + pageLength - 1;
            if (end >= allNodes.length) {
                end = allNodes.length - 1;
            }
            if (end < 0) {
                return "";
            }
            String endName = allNodes[end];
            if (endName.indexOf("/") != -1) {
                endName = endName.substring(endName.lastIndexOf("/") + 1);
            }
            return endName;
        }
    }

    public static String[] getChildren(int start, int pageLength, String[] childPaths) {
        int availableLength = 0;
        if (childPaths != null && childPaths.length > 0) {
            availableLength = childPaths.length - start;
        }
        if (availableLength < pageLength) {
            pageLength = availableLength;
        }

        String[] resultChildPaths = new String[pageLength];
        System.arraycopy(childPaths, start, resultChildPaths, 0, pageLength);
        return resultChildPaths;
    }

    public static OMElement buildOMElement(String content) throws Exception {
        XMLStreamReader parser;
        try {
            parser = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(content));
        } catch (XMLStreamException e) {
            String msg = "Error in initializing the parser to build the OMElement.";
            throw new Exception(msg, e);
        }

        //create the builder
        StAXOMBuilder builder = new StAXOMBuilder(parser);
        //get the root element (in this case the envelope)

        return builder.getDocumentElement();
    }

    public static void printNodesOfTree(TreeNode parentNode, String parentNodeName, int count, JspWriter out) throws IOException {
        try {
            TreeNode[] children = parentNode.getChildNodes();
            String displayName = parentNode.getKey();
            String thisNodeName = "tempNode" + count;
            boolean isNodeValue = false;
            if (children != null && children.length == 1 &&
                    children[0].getChildNodes() == null && children[0].getKey() != null) {
                // the key represent the value.
                displayName += ": <strong>" + children[0].getKey() + "</strong>";
                isNodeValue = true;
            } else if (children == null) {
                displayName = "<strong>" + displayName + "</strong>";
            }
            out.write("var " + thisNodeName + " = new YAHOO.widget.TextNode(\""
                    + displayName + "\", " + parentNodeName + ", true);");
            if (children != null && !isNodeValue) {
                for (TreeNode child : children) {
                    count++;
                    printNodesOfTree(child, thisNodeName, count, out);
                }
            }
        }catch(IOException e){
            e.printStackTrace();
            throw e;
        }
    }
}
