/*
 * Copyright (c) 2007, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.extensions.aspects;

import org.wso2.carbon.registry.core.Aspect;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.apache.axiom.om.OMElement;

import javax.xml.namespace.QName;
import java.util.*;

public class Lifecycle extends Aspect {
    public static final String PROMOTE = "promote";
    public static final String DEMOTE = "demote";
    public enum ConditionEnum { isNull, equals, contains, lessThan, greaterThan }


    static class Condition {
        public String property;
        public ConditionEnum condition;
        public String value;

        Condition(String property, String condition, String value) {
            this.property = property;
            this.condition = ConditionEnum.valueOf(condition);
            this.value = value;
        }

        public boolean isTrue(Resource resource) {
            String propVal = resource.getProperty(property);
            if (propVal == null) {
                return condition == ConditionEnum.isNull;
            }

            switch (condition) {
                case equals:
                    return propVal.equals(value);
                case contains:
                    return propVal.indexOf(value) > -1;
                case lessThan:
                    return Integer.parseInt(propVal) < Integer.parseInt(value);
                case greaterThan:
                    return Integer.parseInt(propVal) > Integer.parseInt(value);
                default:
                    return false;
            }
        }

        public String getDescription() {
            StringBuffer ret = new StringBuffer();
            ret.append("Property '");
            ret.append(property);
            ret.append("' ");
            switch (condition) {
                case isNull:
                    ret.append("must be null");
                    break;
                case equals:
                    ret.append("must equal '");
                    ret.append(value);
                    ret.append("'");
                    break;
                case contains:
                    ret.append("must contain '");
                    ret.append(value);
                    ret.append("'");
                    break;
                case lessThan:
                    ret.append("must be less than ");
                    ret.append(value);
                    break;
                case greaterThan:
                    ret.append("must be greater than ");
                    ret.append(value);
                    break;
            }
            return ret.toString();
        }
    }

    private List<String> states = new ArrayList<String>();
    private Map<String, List<Condition>> transitions = new HashMap<String, List<Condition>>();
    private String stateProperty = "registry.lifecycle.Lifecycle.state";
    private String lifecyleProperty = "registry.LC.name";
    private String aspectName = "Lifecycle";

    public Lifecycle() {
        // Lifecycle with no configuration gets the default set of states, with no conditions.
        states.add("Created");
        states.add("Tested");
        states.add("Deployed");
        states.add("Deprecated");
    }

    public Lifecycle(OMElement config) throws RegistryException {
        String myName = config.getAttributeValue(new QName("name"));
        aspectName = myName;
        myName = myName.replaceAll("\\s", "");
        stateProperty = "registry.lifecycle." + myName + ".state";

        Iterator stateElements = config.getChildElements();
        while (stateElements.hasNext()) {
            OMElement stateEl = (OMElement)stateElements.next();
            String name = stateEl.getAttributeValue(new QName("name"));
            if (name == null) {
                throw new IllegalArgumentException("Must have a name attribute for each state");
            }
            states.add(name);
            List<Condition> conditions = null;
            Iterator conditionIterator = stateEl.getChildElements();
            while (conditionIterator.hasNext()) {
                OMElement conditionEl = (OMElement)conditionIterator.next();
                if (conditionEl.getQName().equals(new QName("condition"))) {
                    String property = conditionEl.getAttributeValue(new QName("property"));
                    String condition = conditionEl.getAttributeValue(new QName("condition"));
                    String value = conditionEl.getAttributeValue(new QName("value"));
                    Condition c = new Condition(property, condition, value);
                    if (conditions == null) conditions = new ArrayList<Condition>();
                    conditions.add(c);
                }
            }
            if (conditions != null) {
                transitions.put(name, conditions);
            }
        }
    }

    public void associate(Resource resource, Registry registry) throws RegistryException {
        resource.setProperty(stateProperty, states.get(0));
        resource.setProperty(lifecyleProperty, aspectName);
    }

    public void invoke(RequestContext context, String action) throws RegistryException {
        Resource resource = context.getResource();
        String currentState = resource.getProperty(stateProperty);
        int stateIndex = states.indexOf(currentState);
        if (stateIndex == -1) {
            throw new RegistryException("State '" + currentState + "' is not valid!");
        }

        String newState;
        if (PROMOTE.equals(action)) {
            if (stateIndex == states.size() - 1) {
                throw new RegistryException("Can't promote beyond end of configured lifecycle!");
            }

            // Make sure all conditions are met
            List<Condition> conditions = transitions.get(currentState);
            if (conditions != null) {
                for (Condition condition : conditions) {
                    if (!condition.isTrue(resource)) {
                        throw new RegistryException(
                                "Condition failed - " + condition.getDescription());
                    }
                }
            }
            newState = states.get(stateIndex + 1);
        } else if (DEMOTE.equals(action)) {
            if (stateIndex == 0) {
                throw new RegistryException("Can't demote beyond start of configured lifecycle!");
            }
            newState = states.get(stateIndex - 1);
        } else {
            throw new RegistryException("Invalid action '" + action + "'");
        }

        resource.setProperty(stateProperty, newState);
        context.getRepository().put(resource.getPath(), resource);
    }

    public String[] getAvailableActions(RequestContext context) {
        ArrayList<String> actions = new ArrayList<String>();
        Resource resource = context.getResource();
        String currentState = resource.getProperty(stateProperty);
        int stateIndex = states.indexOf(currentState);
        if (stateIndex > -1 && stateIndex < states.size() - 1) {
            actions.add(PROMOTE);
        }
        if (stateIndex > 0) {
            actions.add(DEMOTE);
        }
        return actions.toArray(new String[actions.size()]);
    }

    public void dissociate(RequestContext context) {
        Resource resource = context.getResource();
        if (resource != null) {
            resource.removeProperty(stateProperty);
            resource.removeProperty(lifecyleProperty);
        }
    }

    public String getCurrentState(Resource resource) {
        return resource.getProperty(stateProperty);
    }
}
