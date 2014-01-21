/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.extensions.handlers.utils;

import org.apache.xerces.parsers.XMLGrammarPreparser;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLParseException;
import org.apache.xerces.xni.XNIException;
import org.wso2.carbon.registry.extensions.utils.WSDLValidationInfo;

public class SchemaValidator {

    /** Namespaces feature id (http://xml.org/sax/features/namespaces). */
    private static final String NAMESPACES_FEATURE_ID = "http://xml.org/sax/features/namespaces";

    /** Validation feature id (http://xml.org/sax/features/validation). */
    private static final String VALIDATION_FEATURE_ID = "http://xml.org/sax/features/validation";

    /** Schema validation feature id (http://apache.org/xml/features/validation/schema). */
    private static final String SCHEMA_VALIDATION_FEATURE_ID = "http://apache.org/xml/features/validation/schema";

    /** Schema full checking feature id (http://apache.org/xml/features/validation/schema-full-checking). */
    private static final String SCHEMA_FULL_CHECKING_FEATURE_ID = "http://apache.org/xml/features/validation/schema-full-checking";

    /** Honour all schema locations feature id (http://apache.org/xml/features/honour-all-schemaLocations). */
    private static final String HONOUR_ALL_SCHEMA_LOCATIONS_ID = "http://apache.org/xml/features/honour-all-schemaLocations";


	public static WSDLValidationInfo validate(XMLInputSource xmlInputSource) throws Exception {
		XMLGrammarPreparser preparser = new XMLGrammarPreparser();
		preparser.registerPreparser(XMLGrammarDescription.XML_SCHEMA, null);
        preparser.setFeature(NAMESPACES_FEATURE_ID, true);
        preparser.setFeature(VALIDATION_FEATURE_ID, true);
        preparser.setFeature(SCHEMA_VALIDATION_FEATURE_ID, true);
        WSDLValidationInfo validationInfo = new WSDLValidationInfo();
        preparser.setErrorHandler(new ErrorHandler(validationInfo));
        preparser.preparseGrammar(XMLGrammarDescription.XML_SCHEMA, xmlInputSource);
        return validationInfo;
	}

	private static class ErrorHandler implements XMLErrorHandler {

        WSDLValidationInfo validationInfo;
        private ErrorHandler(WSDLValidationInfo validationInfo) {
            this.validationInfo = validationInfo;
        }

        public void error(String domain, String key, XMLParseException exception)
				throws XNIException {
			validationInfo.addValidationMessage("Error: " + exception.getMessage());
		}

		public void fatalError(String domain, String key,
				XMLParseException exception) throws XNIException {
            validationInfo.addValidationMessage("Fatal Error: " + exception.getMessage());
		}

		public void warning(String domain, String key,
				XMLParseException exception) throws XNIException {
			validationInfo.addValidationMessage("Warning: " + exception.getMessage());
		}

	}
}
