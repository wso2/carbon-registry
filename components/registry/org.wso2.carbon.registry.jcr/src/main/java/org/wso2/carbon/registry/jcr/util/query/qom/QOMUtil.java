package org.wso2.carbon.registry.jcr.util.query.qom;

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

import javax.jcr.*;
import javax.jcr.query.qom.QueryObjectModelFactory;

public class QOMUtil {

    public static boolean evalComparison(Value value1, Value value2, String operator) throws RepositoryException {
        int c = compareValues(value1, value2);
        if (operator.equals(QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO)) {
            return c == 0;
        } else if (operator == QueryObjectModelFactory.JCR_OPERATOR_NOT_EQUAL_TO) {
            return c != 0;
        } else if (operator == QueryObjectModelFactory.JCR_OPERATOR_LESS_THAN) {
            return c < 0;
        } else if (operator == QueryObjectModelFactory.JCR_OPERATOR_GREATER_THAN) {
            return c > 0;
        } else if (operator == QueryObjectModelFactory.JCR_OPERATOR_GREATER_THAN_OR_EQUAL_TO) {
            return c >= 0;
        } else if (operator == QueryObjectModelFactory.JCR_OPERATOR_LESS_THAN_OR_EQUAL_TO) {
            return c <= 0;
        } else {
            throw new UnsupportedOperationException(
                    "Unsupported comparison operator: " + operator);
        }
    }

    public static int compareValues(Value v1, Value v2) throws ValueFormatException,
            RepositoryException {
        Comparable c1 = getComparableValue(v1);
        Comparable c2;
        switch (v1.getType()) {

            case PropertyType.STRING:
                c2 = v2.getString();
                break;
            case PropertyType.DOUBLE:
                c2 = v2.getDouble();
                break;
            case PropertyType.LONG:
                c2 = v2.getLong();
                break;

            case PropertyType.BOOLEAN:
                c2 = v2.getBoolean();
                break;
            case PropertyType.DATE:
                c2 = v2.getDate() != null ? v2.getDate().getTimeInMillis():null;
                break;
            case PropertyType.DECIMAL:
                c2 = v2.getDecimal();
                break;
            case PropertyType.PATH:
            case PropertyType.URI:
//            case PropertyType.NAME:
//            case PropertyType.WEAKREFERENCE:
//            case PropertyType.REFERENCE:
//            TODO support rest of the types in future
                c2 = v2.getString();
                break;

            default:
                throw new RepositoryException("Property type not supported"
                        + PropertyType.nameFromValue(v2.getType()));
        }
        return comparisonEval(c1, c2);
    }


    public static int comparisonEval(Comparable c1, Comparable c2) {
        if (c1 == null) {
            return -5;
        } else if (c2 == null) {
            return 1;
        } else {
            try {
                return c1.compareTo(c2);
            } catch (ClassCastException e) {
                return -5;
            }
        }
    }

    public static Comparable getComparableValue(Value val)
            throws ValueFormatException, RepositoryException {
        switch (val.getType()) {
            case PropertyType.STRING:
                return val.getString();
            case PropertyType.DOUBLE:
                return val.getDouble();
            case PropertyType.LONG:
                return val.getLong();
            case PropertyType.BOOLEAN:
                return val.getBoolean();
            case PropertyType.DATE:
                return val.getDate().getTimeInMillis();
            case PropertyType.DECIMAL:
                return val.getDecimal();
            case PropertyType.PATH:
            case PropertyType.URI:
//            case PropertyType.NAME:
//            case PropertyType.WEAKREFERENCE:
//            case PropertyType.REFERENCE:
//                  TODO support rest of the types in future.
                return val.toString();
            default:
                throw new RepositoryException("Unsupported type: "
                        + PropertyType.nameFromValue(val.getType()));
        }
    }

    public static Value[] getPropertyValueFromName(String propName, Node n) {
        Value[] value = new Value[1];
        Value[] values;
        try {
            Property p = n.getProperty(propName);
            if (p != null && p.getValue() != null) {
                value[0] = p.getValue();
            } else if (p != null && p.isMultiple()) {
                values = p.getValues();
            }
        } catch (Exception e) {
            value = null;
            values = null;
        }

        return value;
    }

}
