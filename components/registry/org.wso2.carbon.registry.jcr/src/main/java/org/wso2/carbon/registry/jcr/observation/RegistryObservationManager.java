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

package org.wso2.carbon.registry.jcr.observation;

import javax.jcr.RepositoryException;
import javax.jcr.observation.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class RegistryObservationManager implements ObservationManager {

    private Map<EventListener, Set> eventListenersMap = new HashMap<EventListener, Set>();
    private Set<EventListener> eventlistenerList = new HashSet<EventListener>();

    public void addEventListener(EventListener eventListener, int i, String s, boolean b, String[] strings, String[] strings1, boolean b1) throws RepositoryException {

        eventListenersMap.put(eventListener, getEventsFromTypes(i));
        eventlistenerList.add(eventListener);

    }


    public void removeEventListener(EventListener eventListener) throws RepositoryException {

        eventListenersMap.remove(eventListener);
        eventlistenerList.remove(eventListener);
    }

    public EventListenerIterator getRegisteredEventListeners() throws RepositoryException {

        return new RegistryEventListenerIterator(eventlistenerList);
    }

    public void setUserData(String s) throws RepositoryException {

    }

    public EventJournal getEventJournal() throws RepositoryException {

        return null;
    }

    public EventJournal getEventJournal(int i, String s, boolean b, String[] strings, String[] strings1) throws RepositoryException {

        return null;
    }

    private Set getEventsFromTypes(int types) {

        Set<Integer> eventTypes = new HashSet<Integer>();

        if ((types & Event.NODE_ADDED) == Event.NODE_ADDED) {

            eventTypes.add(Event.NODE_ADDED);
        }
        if ((types & Event.NODE_MOVED) == Event.NODE_MOVED) {

            eventTypes.add(Event.NODE_MOVED);
        }
        if ((types & Event.NODE_REMOVED) == Event.NODE_REMOVED) {

            eventTypes.add(Event.NODE_REMOVED);
        }
        if ((types & Event.PROPERTY_ADDED) == Event.PROPERTY_ADDED) {

            eventTypes.add(Event.PROPERTY_ADDED);
        }
        if ((types & Event.PROPERTY_CHANGED) == Event.PROPERTY_CHANGED) {

            eventTypes.add(Event.PROPERTY_CHANGED);
        }
        if ((types & Event.PROPERTY_REMOVED) == Event.PROPERTY_REMOVED) {

            eventTypes.add(Event.PROPERTY_REMOVED);
        }
        if ((types & Event.PERSIST) == Event.PERSIST) {

            eventTypes.add(Event.PERSIST);
        }
        return eventTypes;
    }

}
