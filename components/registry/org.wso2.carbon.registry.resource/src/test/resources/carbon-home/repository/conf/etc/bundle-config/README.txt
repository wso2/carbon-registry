# ----------------------------------------------------------------------------
#  Copyright 2015 WSO2, Inc. http://www.wso2.org
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

# -----------------------------------------------------------------------------

This directory supports adding third-pary config files to specific bundles during runtime.

Explanation: Each OSGi bundle has its own classLoader. Some thirdpary libs read configs from classPath. This scenario fails in OSGi runtime, since OSGi runtime does not share a common classPath for individual bundles. Bundling config files during the bundle creation process itself will solve the issue. However it limits the ability to edit the configs during restarts.

Here we are providing a workaround for such scenarios. The given config file will get resolved to a fragment bundle and will get attached to the specified host bundle. The host bundle name(symbolic name) is resolved by looking at the directory structure. Hence host bundle name should be directory name of the config file directory.


Example: The bundle with symbolic name, 'org.foo.bar' expects a config file named 'foobar.properties' from its classPath.

create a directory named 'org.foo.bar' inside 'repository/conf/etc/bundle-config' - (this directory) and place the foobar.properties file.


