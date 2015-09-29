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

Running WSO2 Carbon Server as a Windows Service
------------------------------------------------

1. Download the latest stable version of YAJSW from the project home page. (http://yajsw.sourceforge.net/)
2. Unzip the YAJSW archive and place the provided wrappe.conf file (this directory) inside <YAJSW.Home.Dir>/conf.
3. Set JAVA_HOME and CARBON_HOME system properties
4. Start the product as a windows service. (batch scripts are found under <YAJSW.Home.Dir>/bat)

For more detailed info please refer to Carbon wiki documentation (http://docs.wso2.org/wiki/dashboard.action) and YAJSW project documentation.
