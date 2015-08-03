/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.registry.security.vault.util;

public interface SecureVaultConstants {
	public static final String SYSTEM_CONFIG_CONNECTOR_SECURE_VAULT_CONFIG =
	                                                                         "/_system/config/repository/components/secure-vault";
	public static final String CONNECTOR_SECURE_VAULT_CONFIG_REPOSITORY =
	                                                                      "/repository/components/secure-vault";
	public static final String CARBON_HOME = "carbon.home";
	public static final String SECRET_CONF = "secret-conf.properties";
	public static final String CONF_DIR = "conf";
	public static final String REPOSITORY_DIR = "repository";
	public static final String SECURITY_DIR = "security";
	/* Default configuration file path for secret manager */
	public final static String PROP_DEFAULT_CONF_LOCATION = "secret-manager.properties";
	/*
	 * If the location of the secret manager configuration is provided as a
	 * property- it's name
	 */
	public final static String PROP_SECRET_MANAGER_CONF = "secret.manager.conf";
	/* Property key for secretRepositories */
	public final static String PROP_SECRET_REPOSITORIES = "secretRepositories";
	/* Type of the secret repository */
	public final static String PROP_PROVIDER = "provider";
	/* Dot string */
	public final static String DOT = ".";

	// property key for global secret provider
	public final static String PROP_SECRET_PROVIDER = "carbon.secretProvider";

	public final static String SERVELT_SESSION = "comp.mgt.servlet.session";

	public static final String CONF_CONNECTOR_SECURE_VAULT_CONFIG_PROP_LOOK =
	                                                                          "conf:/repository/components/secure-vault";

}
