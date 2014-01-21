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

import org.wso2.carbon.registry.jcr.util.RegistryJCRItemOperationUtil;

import javax.jcr.*;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;


public class RegistryValue implements Value {


    private Object value;
    private int type = 0;
    private static final Object hashObject = new Object();


    public RegistryValue(Object key) {
        validateNameSpaceURIS(key);
    }

    public RegistryValue(Object key, int i) {
        validateNameSpaceURIS(key);
        this.type = i;   //this is to handle value types such as Name ,....
    }

    private void validateNameSpaceURIS(Object value) {
        String val = "";
        if ((value instanceof String) && (value.toString().contains("{"))) {
            val = RegistryJCRItemOperationUtil.replaceNameSpacePrefixURIS(value.toString());
            this.value = val;
        } else {
            this.value = value;
        }
    }

    public int hashCode() {
        if (value != null) {
            return hashObject.hashCode() & type & value.hashCode();
        } else {
            return hashObject.hashCode() & type;
        }
    }

    public boolean equals(Object obj) {

        boolean eql = false;

        if (obj instanceof Value) {

            try {

                if ((value instanceof String) && (((Value) obj).getString().equals(this.getString()))) {

                    eql = true;
                } else if ((value instanceof Calendar) && (((Value) obj).getDate().getTimeInMillis() == (this.getDate().getTimeInMillis()))) {

                    eql = true;

                } else if ((value instanceof InputStream) && (((Value) obj).getStream().equals(this.getStream()))) {

                    eql = true;

                }

            } catch (RepositoryException e) {
                e.printStackTrace();
            }
        }

        return eql;
    }


    public Object getKey() {

        return value;
    }


    public String getString() throws ValueFormatException, IllegalStateException, RepositoryException {

        return value.toString();
    }

    public InputStream getStream() throws RepositoryException {

        if (value != null)
            return (InputStream) value;
        else
            return null;
    }

    public Binary getBinary() throws RepositoryException {

        RegistryBinary aBinary = new RegistryBinary(value);
        return aBinary;
    }

    public long getLong() throws ValueFormatException, RepositoryException {

        long aLong = 0;
        if (value instanceof Long) {

            aLong = ((Long) value).longValue();

        }
        return aLong;
    }

    public double getDouble() throws ValueFormatException, RepositoryException {
        double aDouble = 0;
        if (value instanceof Double) {
            aDouble = ((Double) value).doubleValue();
        }
        return aDouble;
    }

    public BigDecimal getDecimal() throws ValueFormatException, RepositoryException {

        BigDecimal aBigDecimal = new BigDecimal("0");
        if (value instanceof BigDecimal) {
            aBigDecimal = new BigDecimal(value.toString());
        }
        return aBigDecimal;
    }

    public Calendar getDate() throws ValueFormatException, RepositoryException {

        Calendar aCalendar = null;
        if ((value != null) && (value instanceof Long)) {
            aCalendar = Calendar.getInstance();
            aCalendar.setTimeInMillis(((Long) value).longValue());

        } else if (value instanceof Calendar) {
            aCalendar = Calendar.getInstance();
            aCalendar.setTimeInMillis(((Calendar) value).getTimeInMillis());

        }
        return aCalendar;
    }

    public boolean getBoolean() throws ValueFormatException, RepositoryException {

        return Boolean.getBoolean(value.toString());
    }

    public int getType() {

        int type = 0;
        if (this.type == 0) {

            if (value instanceof String) type = 1;
            else if (value instanceof Binary) type = 2;
            else if (value instanceof Long) type = 3;
            else if (value instanceof Double) type = 4;
            else if (value instanceof Calendar) type = 5;
            else if (value instanceof Boolean) type = 6;
            else if (value instanceof Node) type = 9;
            else if (value instanceof BigDecimal) type = 12;

            return type;
        } else {
            return this.type;
        }


    }


    public Node getNode() { //TODO


        return null;
    }


}
