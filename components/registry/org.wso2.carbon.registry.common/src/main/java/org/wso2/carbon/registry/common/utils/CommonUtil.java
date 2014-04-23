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

package org.wso2.carbon.registry.common.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.common.CommonConstants;
import org.wso2.carbon.registry.common.ResourceData;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.securevault.SecretResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CommonUtil {

    private static final Log log = LogFactory.getLog(CommonUtil.class);

    private static final String ERROR_TITILE =
            "<strong>Error while processing the request</strong><br/>";

    private static String LOGGED_USER = "logged-user";

    /**
     * Constants used for formatting dates.
     */
    private static final long ONEMINUTE = 60 * 1000;
    private static final long ONEHOUR = 60 * ONEMINUTE;
    private static final long ONEDAY = 24 * ONEHOUR;

    public static UserRegistry getUserRegistry(HttpServletRequest request)
            throws RegistryException {

        UserRegistry userRegistry =
                (UserRegistry)request.getSession().getAttribute(RegistryConstants.REGISTRY_USER);

        if (userRegistry == null) {
            String msg = "User's Registry instance is not found. " +
                    "Users have to login to retrieve a registry instance. ";
            log.error(msg);
            throw new RegistryException(msg);
        }

        return userRegistry;
    }

    public static String getServiceVersion(OMElement element) {
        OMElement overview = element.getFirstChildWithName(new QName("Overview"));
        if (overview != null) {
            if (overview.getFirstChildWithName(new QName("Version")) != null) {
                return overview.getFirstChildWithName(new QName("Version")).getText();
            }
        }
        overview = element.getFirstChildWithName(new QName(CommonConstants.SERVICE_ELEMENT_NAMESPACE, "overview"));
        if (overview != null) {
            if (overview.getFirstChildWithName(new QName(CommonConstants.SERVICE_ELEMENT_NAMESPACE, "version")) != null) {
                return overview.getFirstChildWithName(new QName(CommonConstants.SERVICE_ELEMENT_NAMESPACE, "version")).getText();
            }
        }
        return "";
    }


    public static UserRegistry getUserRegistry(RegistryService registryService)
            throws RegistryException {
        MessageContext messageContext = MessageContext.getCurrentMessageContext();
        if (messageContext == null) {
            String msg = "Could not get the user's Registry session. Message context not found.";
            log.error(msg);
            throw new RegistryException(msg);
        }

        HttpServletRequest request =
                (HttpServletRequest) messageContext.getProperty("transport.http.servletRequest");

        return getUserRegistry(registryService, request);
    }

    public static UserRegistry getUserRegistry(
            RegistryService registryService,
            HttpServletRequest request) throws RegistryException {

        UserRegistry registry =
                (UserRegistry) request.getSession().getAttribute(RegistryConstants.USER_REGISTRY);

        if (registry == null) {
            String msg = "User's Registry instance is not found. " +
                    "Users have to login to retrieve a registry instance. ";
            log.error(msg);
            throw new RegistryException(msg);
       }

        return registry;
    }

    public static UserRegistry getUserRegistry(
            RegistryService registryService,
            HttpSession session) throws RegistryException {

        UserRegistry registry =
                (UserRegistry) session.getAttribute(RegistryConstants.USER_REGISTRY);

        if (registry == null) {
            // TODO: change the AuthenticationAdmin use the client session when backend-frontent not seperated
            String loggedUser = (String)session.getAttribute("logged-user");
            if (loggedUser != null) {
                registry = registryService.getUserRegistry(loggedUser);
                session.setAttribute(RegistryConstants.USER_REGISTRY, registry);
            }
        }  
        if (registry == null) {
            String msg = "User's Registry instance is not found. " +
                    "Users have to login to retrieve a registry instance. ";
            log.error(msg);
            throw new RegistryException(msg);
        }

        return registry;
    }

    public static void invalidateAllSessions(HttpServletRequest request) {
        request.getSession().invalidate();
    }

    public static void sendContent(HttpServletResponse response, String msg) {

        try {
            PrintWriter out = response.getWriter();

            out.println(msg);

            out.flush();
            out.close();

        } catch (IOException e) {

            String sendError = "Failed to send message. Caused by " + e.getMessage() +
                    "\nFollowing message was not send to the UI\n" + msg;
            log.error(sendError, e);
        }
    }

    public static void sendErrorContent(HttpServletResponse response, String msg) {

        try {
            PrintWriter out = response.getWriter();

            out.println(ERROR_TITILE);
            out.println(msg);

            out.flush();
            out.close();

        } catch (IOException e) {

            String sendError = "Failed to send error content. Caused by " + e.getMessage() +
                    "\nFollowing error was not send to the UI\n" + msg;
            log.error(sendError, e);
        }
    }

    public static void addErrorMessage(HttpServletRequest request, String msg) {

        String errorMessage = (String) request.getSession().getAttribute(CommonConstants.ERROR_MESSAGE);

        if (errorMessage == null) {
            errorMessage = "<li>" + msg + "</li>";
        } else {
            errorMessage = errorMessage + "<li>" + msg + "</li>";
        }

        request.getSession().setAttribute(CommonConstants.ERROR_MESSAGE, errorMessage);
    }

    public static void forwardToPage (
            HttpServletRequest request, HttpServletResponse response, String url) {

        String errorMsg = (String) request.getSession().getAttribute(CommonConstants.ERROR_MESSAGE);
        if (errorMsg != null) {
            errorMsg = "<strong>Errors have occured while processing the request.</strong><br/><ul>" + errorMsg + "</ul>";
            request.getSession().setAttribute(CommonConstants.ERROR_MESSAGE, errorMsg);
        }

        try {
            request.getRequestDispatcher(url).forward(request, response);

        } catch (Exception e) {

            String msg = "Failed to generate the page " + url + ". " + e.getMessage();
            log.error(msg, e);
        }
    }

    public static void redirect(HttpServletResponse response, String url) {

        try {
            response.sendRedirect(url);
        } catch (IOException e) {
            String msg = "Failed to redirect to the URL " + url + ". \nCaused by " +
                    e.getMessage();
            log.error(msg, e);
        }
    }

    public static String formatDate(Date dateToParse) {

        if (dateToParse == null) {
            return null;
        }

        Calendar now = Calendar.getInstance();
        Calendar date = Calendar.getInstance();
        date.setTime(dateToParse);

        String value = "";
        final long timeDifference = now.getTimeInMillis() - date.getTimeInMillis();
        if (timeDifference > 0 && timeDifference < ONEDAY) {
            long hours = 0;
            if (now.getTimeInMillis() - date.getTimeInMillis() > ONEHOUR) {
                hours = ((now.getTimeInMillis() - date.getTimeInMillis()) / ONEHOUR);
                value += hours + "h ";
            }
            if (hours < 6) {
                long minutes = ((now.getTimeInMillis() - date.getTimeInMillis()) / ONEMINUTE);
                value += (minutes - hours * 60) + "m ";
            }
            value += "ago";
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat("dd MMM HH:MM:ss");
            value = formatter.format(dateToParse);

            if (date.get(Calendar.YEAR) != now.get(Calendar.YEAR)) {
                formatter = new SimpleDateFormat("yyyy");
                value += " " + formatter.format(dateToParse);
            }
            value = "on " + value;
        }

        return new StringBuilder(value).append(" (on ").append(dateToParse.toString()).append(")").toString();
    }

    /**
     * Converts given strings to Dates
     *
     * @param dateString Allowed format mm/dd/yyyy
     *
     * @return Date corresponding to the given string date
     */
    public static Date computeDate(String dateString) throws RegistryException {

        if (dateString == null || dateString.length() == 0) {
            return null;
        }
		DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
		
        //DateFormat formatter = DateFormat.getDateInstance(DateFormat.SHORT);
        try {
            return formatter.parse(dateString);
        } catch (ParseException e) {
            String msg = "Date format is invalid: " + dateString;
            throw new RegistryException(msg, e);
        }
    }

    public static void populateAverageStars(ResourceData resourceData) {

        float tempRating = resourceData.getAverageRating() * 1000;
        tempRating = Math.round(tempRating);
        tempRating = tempRating / 1000;
        resourceData.setAverageRating(tempRating);

        float averageRating = resourceData.getAverageRating();
        String[] averageStars = new String[5];

        for (int i = 0; i < 5; i++) {

            if (averageRating >= i + 1) {
                averageStars[i] = "04";

            } else if (averageRating <= i) {
                averageStars[i] = "00";

            } else {

                float fraction = averageRating - i;

                if (fraction <= 0.125) {
                    averageStars[i] = "00";

                } else if (fraction > 0.125 && fraction <= 0.375) {
                    averageStars[i] = "01";

                } else if (fraction > 0.375 && fraction <= 0.625) {
                    averageStars[i] = "02";

                } else if (fraction > 0.625 && fraction <= 0.875) {
                    averageStars[i] = "03";

                } else {
                    averageStars[i] = "04";

                }
            }
        }

        resourceData.setAverageStars(averageStars);
    }

    public static String getServerBaseURL(HttpServletRequest request) {
        String reqURL = request.getRequestURL().toString();
        return reqURL.substring(0, reqURL.indexOf("/wso2registry"));
    }

    public static String generateOptionsFor(String value, String [] options) {
        StringBuffer ret = new StringBuffer();
        for (String option : options) {
            ret.append("<option value=\"");
            ret.append(option);
            ret.append("\"");
            if (option.equalsIgnoreCase(value)) {
                ret.append(" selected");
            }
            ret.append(">");
            ret.append(option);
            ret.append("</option>\n");
        }
        return ret.toString();
    }


    public static boolean isLatestVersion(String currentVersion, String lastUpdatedVersion) {

        if (Long.parseLong(lastUpdatedVersion) == Long.parseLong(currentVersion)) {
            return true;
        }
        return false;
    }

    public static Map<String, String> attributeArrayToMap(String[] array) {
        Map<String, String> map = new LinkedHashMap<String, String>();
        if (array != null) {
            for (String item : array) {
                if (item != null) {
                    String[] pair = item.split("\\|");
                    map.put(pair[0], pair[1]);
                }
            }
        }
        return map;
    }

    public static String[] mapToAttributeArray(Map<String, String> map) {
        List<String> list = new LinkedList<String>();
        for (Map.Entry<String, String> e : map.entrySet()) {
            list.add(e.getKey() + "|" + e.getValue());
        }
        return list.toArray(new String[list.size()]);
    }

    public static String getResolvedPassword(SecretResolver secretResolver,
                                             String configName, String password) {

        if (secretResolver != null && secretResolver.isInitialized()) {
            if (secretResolver.isTokenProtected("wso2registry." + configName + ".password")) {
                return secretResolver.resolve("wso2registry." + configName + ".password");
            } else {
                return password;
            }
        } else {
            return password;
        }
    }
}
