/*
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.apache.openjpa.osgi;

import org.apache.openjpa.lib.conf.ConfigurationProvider;
import org.apache.openjpa.persistence.PersistenceProductDerivation;
import org.apache.openjpa.persistence.PersistenceUnitInfoImpl;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.Map;

public class OSGiPersistenceProductDerivation extends PersistenceProductDerivation {

    /**
     * Load configuration from the given resource and unit names, which may
     * be null.
     */
    public ConfigurationProvider load(String rsrc, String name, Map m, ClassLoader loader)
        throws IOException {
        boolean explicit = !StringUtils.isEmpty(rsrc);
        if (!explicit)
            rsrc = RSRC_DEFAULT;

        ConfigurationProviderImpl cp = new ConfigurationProviderImpl();
        Boolean ret = load(cp, rsrc, name, m, loader, explicit);
        if (ret != null)
            return (ret.booleanValue()) ? cp : null;
        if (explicit)
            return null;

        // persistence.xml does not exist; just load map
        PersistenceUnitInfoImpl pinfo = new PersistenceUnitInfoImpl();
        pinfo.fromUserProperties(m);
        if (!isOpenJPAPersistenceProvider(pinfo, null)) {
            warnUnknownProvider(pinfo);
            return null;
        }
        cp.addProperties(pinfo.toOpenJPAProperties());
        return cp;
    }
}
