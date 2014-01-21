/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.jcr;

import javax.jcr.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RegistryNamespace implements NamespaceRegistry {

    List<String> prefix = new ArrayList();
    List<String> uris = new ArrayList();
    Map uri_nameSpace = new HashMap();
    Map prefix_nameSpace = new HashMap();


    public void registerNamespace(String s, String s1) throws NamespaceException, UnsupportedRepositoryOperationException, AccessDeniedException, RepositoryException {


        if ((s == null) || ((s.equals("jcr")) || (s.equals("nt")) || (s.contains("xml")) || (s.equals("mix")) || (s.equals("sv")))) {

            throw new NamespaceException();
        } else if ((s != null)) {
            prefix.add(s);
            uris.add(s1);
            uri_nameSpace.put(s1, s);
            prefix_nameSpace.put(s, s1);

        }


    }


    public void unregisterNamespace(String s) throws NamespaceException, UnsupportedRepositoryOperationException, AccessDeniedException, RepositoryException {

        if ((s == null) || ((s.equals("jcr")) || (s.equals("nt")) || (s.equals("xml")) || (s.equals("mix")) || (s.equals("sv")))) {

            throw new NamespaceException();
        } else if (uri_nameSpace.containsKey(s)) {
            uri_nameSpace.remove(s);
            prefix_nameSpace.remove(s);
        } else if(!uri_nameSpace.containsKey(s)) {
          throw  new NamespaceException("Cannot remove unregistered ");
        }

    }

    public String[] getPrefixes() throws RepositoryException {
        String temp = "";
        for (String s : prefix) {
            temp = ":" + s;

        }

        return temp.split(":");
    }

    public String[] getURIs() throws RepositoryException {
        String temp = "";
        for (String s : uris) {
            temp = ":" + s;

        }

        return temp.split(":");
    }

    public String getURI(String s) throws NamespaceException, RepositoryException {


        return prefix_nameSpace.get(s).toString();
    }

    public String getPrefix(String s) throws NamespaceException, RepositoryException {

        String pref = "";
        if (uri_nameSpace == null || uri_nameSpace.size() == 0) {
            throw new NamespaceException();

        } else if (uri_nameSpace.get(s) != null) {
            pref = uri_nameSpace.get(s).toString();

        }
        return pref;
    }
}