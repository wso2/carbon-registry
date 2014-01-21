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

package org.wso2.carbon.registry.cmis.util;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.*;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.wso2.carbon.registry.cmis.RegistryFolder;

import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Miscellaneous utility functions
 */
public final class CommonUtil {

    /**
     * Convert from <code>Calendar</code> to a <code>GregorianCalendar</code>.
     * 
     * @param date
     * @return  <code>date</code> if it is an instance of <code>GregorianCalendar</code>.
     *   Otherwise a new <code>GregorianCalendar</code> instance for <code>date</code>.
     */
    public static GregorianCalendar toCalendar(Calendar date) {
        if (date instanceof GregorianCalendar) {
            return (GregorianCalendar) date;
        } else {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTimeZone(date.getTimeZone());
            calendar.setTimeInMillis(date.getTimeInMillis());
            return calendar;
        }
    }

    /**
     * Replace every occurrence of <code>target</code> in <code>string</code> with
     * <code>replacement</code>.
     *
     * @param string  string to do replacement on
     * @param target  string to search for
     * @param replacement  string to replace with
     * @return  string with replacing done
     */
    public static String replace(String string, String target, String replacement) {
        if ("".equals(target)) {
            throw new IllegalArgumentException("target string must not be empty");
        }
        if ("".equals(replacement) || target.equals(replacement)) {
            return string;
        }

        StringBuilder result = new StringBuilder();
        int d = target.length();
        int k = 0;
        int j;
        do {
            j = string.indexOf(target, k);
            if (j < 0) {
                result.append(string.substring(k));
            } else {
                result.append(string.substring(k, j)).append(replacement);
            }
            k = j + d;
        } while (j >= 0);

        return result.toString();
    }

    /**
     * Escapes a GREG path such that it can be used in a XPath query
     * @param path
     * @return  escaped path
     */
    public static String escape(String path) {
        return replace(path, " ", "_x0020_"); // fixme do more thorough escaping of path
    }


    /*
          Common method to use to get target path
     */
    public static String getTargetPathOfNode(RegistryFolder parentFolder, String name) {

        String parentPath = parentFolder.getNode().getPath();
        if (parentPath.endsWith("/")){
            return parentPath+name;
        } else {
            return parentPath + "/" + name;
        }
    }


    public static Resource getResourceById(Registry repository, String resourceId){

        Resource resource = null;
        try {
            resource = repository.get(getPathById(resourceId));
        } catch (RegistryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return resource;
    }

    public static String getPathById(String resourceId) {
        return resourceId;
    }

    //public static Resource getResourceByPath(String path){

    //}

    public static void setProperty(Registry repository,Resource resource, PropertyData<?> propertyData) throws RegistryException {
        List<String> values;
        String propertyName = propertyData.getId();

        if (propertyData instanceof PropertyBoolean) {
            values = toValue((PropertyBoolean) propertyData);

        }
        else if (propertyData instanceof PropertyDateTime) {
            values = toValue((PropertyDateTime) propertyData);

        }
        else if (propertyData instanceof PropertyDecimal) {
            values = toValue((PropertyDecimal) propertyData);

        }
        else if (propertyData instanceof PropertyHtml) {
            values = toValue((PropertyHtml) propertyData);

        }
        else if (propertyData instanceof PropertyId) {
            values = toValue((PropertyId) propertyData);

        }
        else if (propertyData instanceof PropertyInteger) {
            values = toValue((PropertyInteger) propertyData);

        }
        else if (propertyData instanceof PropertyString) {
            values = toValue((PropertyString) propertyData);

        }
        else if (propertyData instanceof PropertyUri) {
            values = toValue((PropertyUri) propertyData);

        }
        else {
            throw new CmisInvalidArgumentException("Invalid property type: " + propertyData);
        }

        //Add the property list to the resource

        if (PropertyIds.NAME.equals(propertyName)) {
            resource.setProperty(PropertyIds.NAME, values);
            /*Have to rename since when compileObjectProperties is called
              it sets the PropertyIds.NAME from the node name. Not from the property value.
             */
            //repository.rename(resource.getPath(), resource.getParentPath()+values.get(0));
        }
        else if (PropertyIds.CONTENT_STREAM_MIME_TYPE.equals(propertyName)) {
            resource.setProperty(CMISConstants.GREG_MIMETYPE, values);
            resource.setMediaType(values.get(0));
        } else {
            resource.setProperty(propertyName, values);
        }

        //Commit the changes
        repository.put(resource.getPath(), resource);


    }

    /**
     * Remove a property from a GREG node
     */
    public static void removeProperty(Registry repository,Resource resource, PropertyData<?> propertyData) throws RegistryException {
        String id = propertyData.getId();

        if(resource.getPropertyValues(id) != null ){ //has property
            resource.removeProperty(id);
            repository.put(resource.getPath(), resource);
        }

    }

    /**
     * Convert an array of <code>Value</code>s to a list of <code>String</code>s.
     */
    public static List<String> toStrings(String[] values) throws RegistryException {
        ArrayList<String> strings = new ArrayList<String>(values.length);

        for (String v : values) {
            strings.add(v);
        }

        return strings;
    }

    /**
     * Convert an array of <code>Value</code>s to a list of <code>BigInteger</code>s.
     */
    public static List<BigInteger> toInts(String[] values) throws RegistryException {
        ArrayList<BigInteger> ints = new ArrayList<BigInteger>(values.length);

        for (String v : values) {
            ints.add(BigInteger.valueOf(Long.parseLong(v)));
        }

        return ints;
    }

    /**
     * Convert an array of <code>Value</code>s to a list of <code>BigDecimal</code>s.
     */
    public static List<BigDecimal> toDecs(String[] values) throws RegistryException {
        ArrayList<BigDecimal> decs = new ArrayList<BigDecimal>(values.length);

        for (String v : values) {
            decs.add(new BigDecimal(v));
        }

        return decs;
    }

    /**
     * Convert an array of double <code>Value</code>s to a list of <code>BigInteger</code>s.
     */
    public static List<BigDecimal> doublesToDecs(String[] values) throws RegistryException {
        ArrayList<BigDecimal> decs = new ArrayList<BigDecimal>(values.length);

        for (String v : values) {
            decs.add(BigDecimal.valueOf(Double.parseDouble(v)));
        }

        return decs;
    }

    /**
     * Convert an array of <code>Value</code>s to a list of <code>Booleans</code>s.
     */
    public static List<Boolean> toBools(String[] values) throws RegistryException {
        ArrayList<Boolean> bools = new ArrayList<Boolean>(values.length);

        for (String v : values) {
            bools.add(Boolean.parseBoolean(v));
        }

        return bools;
    }

    /**
     * Convert an array of <code>Value</code>s to a list of <code>GregorianCalendar</code>s.
     */
    public static List<GregorianCalendar> toDates(String[] values) throws RegistryException {
        ArrayList<GregorianCalendar> dates = new ArrayList<GregorianCalendar>(values.length);

        for (String v : values) {
            //TODO check
            //Parses ISO 8601 compliant date string
            dates.add(CommonUtil.toCalendar(javax.xml.bind.DatatypeConverter.parseDateTime(v)));
        }

        return dates;
    }

    /**
     * Convert a <code>PropertyBoolean</code> to an array of GREG <code>Values</code>.
     */
    public static List<String> toValue(PropertyBoolean propertyData) {
        List<Boolean> values = propertyData.getValues();
        List<String> result = null;

        if (values == null) {
            return result;
        }

        for (Boolean v : values) {
            result.add(v.toString());
        }

        return result;
    }

    /**
     * Convert a <code>PropertyDateTime</code> to an array of GREG <code>Values</code>.
     */
    public static List<String> toValue(PropertyDateTime propertyData) {
        List<GregorianCalendar> values = propertyData.getValues();
        List<String> result = null;
        if (values == null) {
            return result;
        }

        for (GregorianCalendar v : values) {
            //   yyyy/mm/dd
            StringBuffer date = new StringBuffer();
            try{
                date.append(v.get(Calendar.YEAR));
                date.append("/");
                date.append(v.get(Calendar.MONTH));
                date.append("/");
                date.append(v.get(Calendar.DATE));
            }catch (Exception e){
                e.printStackTrace();
            }
            result.add(date.toString());
        }
        return result;
    }

    /**
     * Convert a <code>PropertyDecimal</code> to an array of GREG <code>Values</code>.
     */
    public static List<String> toValue(PropertyDecimal propertyData) {
        List<BigDecimal> values = propertyData.getValues();
        List<String> result = null;
        if (values == null) {
            return result;
        }

        for (BigDecimal v : values) {
            result.add(v.toString());
        }

        return result;
    }

    /**
     * Convert a <code>PropertyHtml</code> to an array of GREG <code>Values</code>.
     */
    public static List<String> toValue(PropertyHtml propertyData) {
        List<String> values = propertyData.getValues();
        List<String> result = null;
        if (values == null) {
            return result;
        }

        for (String v : values) {
            result.add(v);
        }

        return result;
    }

    /**
     * Convert a <code>PropertyId</code> to an array of GREG <code>Values</code>.
     */
    public static List<String> toValue(PropertyId propertyData) {
        List<String> values = propertyData.getValues();
        List<String> result = null;
        if (values == null) {
            return result;
        }

        for (String v : values) {
            result.add(v);
        }

        return result;
    }

    /**
     * Convert a <code>PropertyInteger</code> to an array of GREG <code>Values</code>.
     */
    public static List<String> toValue(PropertyInteger propertyData) {
        List<BigInteger> values = propertyData.getValues();
        List<String> result = null;
        if (values == null) {
            return result;
        }

        for (BigInteger v : values) {
            result.add(v.toString());
        }

        return result;
    }

    /**
     * Convert a <code>PropertyString</code> to an array of GREG <code>Values</code>.
     */
    public static List<String> toValue(PropertyString propertyData) {
        List<String> values = propertyData.getValues();
        List<String> result = new ArrayList<String>();
        if (values == null) {
            return result;
        }

        for (String v : values) {
            result.add(v);
        }

        return result;
    }

    /**
     * Convert a <code>PropertyUri</code> to an array of GREG <code>Values</code>.
     */
    public static List<String> toValue(PropertyUri propertyData) {
        List<String> values = propertyData.getValues();
        List<String> result = null;
        if (values == null) {
            return result;
        }

        for (String v : values) {
            result.add(v);
        }

        return result;
    }

    public static String getDestPathOfNode(String parentPath, String name) {

        if (parentPath.endsWith("/")){
            return parentPath+name;
        } else{
            return parentPath + "/" + name;
        }
    }

    public static boolean isNonEmptyProperties(Properties props) {
        return props != null && props.getProperties() != null;
    }

    public static boolean hasObjectId(Holder<String> objectId) {
        return objectId != null && objectId.getValue() != null;
    }



}
