# ----------------------------------------------------------------------------
#  Copyright 2017 WSO2, Inc. http://www.wso2.org
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


This directory contains all OSGi specific stuff in Carbon. Usages of each and
every directory are as follows.

1. plugins
   This contains all OSGi bundles that are used to run the server.

2. p2
   Contains Carbon provisioning (p2) related configuration files.

3. lib
   If you want any third part libraries to be used as OSGi bundles in the
   system, copy those libraries into this directory.

4. dropins
   If you have OSGi bundles that should be added to Carbon, copy
   those into this directory.

5. configuration
   OSGi specific configuration files.

6. extensions
   Directory to drop non-OSGi libraries which should become system extensions
   in the OSGi environment.

7. patches
   Directory to drop OSGi level patches.
