/**
 * Copyright(C) 2013 Patrik Dufresne Service Logiciel <info@patrikdufresne.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.patrikdufresne.managers;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to store a map of object and flags. The flags used to be
 * an INSERT, UPDATE and/or DELETE. This class is typically used by the internal
 * function to store which object need to be include in the event notification.
 * 
 * @author patapouf
 * 
 */
public class EventTable implements Cloneable {

    public List<Integer> types;

    public List<Object> entities;

    /**
     * Add a new entry.
     * 
     * @param eventType
     *            the event flag
     * @param entity
     *            the entity
     */
    public void add(int eventType, Object entity) {
        if (this.types == null || this.entities == null) {
            this.types = new ArrayList<Integer>();
            this.entities = new ArrayList<Object>();
        }
        this.types.add(Integer.valueOf(eventType));
        this.entities.add(entity);
    }

    /**
     * Clear all the events.
     */
    public void clear() {
        if (this.types != null) {
            this.types.clear();
        }
        if (this.entities != null) {
            this.entities.clear();
        }
    }

    /**
     * Return the event table size.
     * 
     * @return number of event in this table event.
     */
    public int size() {
        if (this.types == null) {
            return 0;
        }
        return this.types.size();
    }

    @Override
    public EventTable clone() {
        EventTable cloned = new EventTable();
        if (this.types != null) {
            cloned.types = new ArrayList<Integer>(this.types);
        }
        if (this.entities != null) {
            cloned.entities = new ArrayList<Object>(this.entities);
        }
        return cloned;
    }

    /**
     * Combine all the stored data into a list of ManagerEvent.
     * 
     * @return list of ManagerEvent
     */
    // public Collection<ManagerEvent> listEvent() {
    //
    // Map<ManagerEvent, ManagerEvent> eventMap = new
    // LinkedHashMap<ManagerEvent, ManagerEvent>();
    //
    // Iterator<Entry<Object, Integer>> iter = this.events.entrySet()
    // .iterator();
    // while (iter.hasNext()) {
    // Entry<Object, Integer> entry = iter.next();
    // Object object = entry.getKey();
    // int type = entry.getValue().intValue();
    //
    // ManagerEvent eventKey = new ManagerEvent();
    // eventKey.clazz = object.getClass();
    // eventKey.type = type;
    //
    // ManagerEvent eventObj = eventMap.get(eventKey);
    // if (eventObj == null) {
    // eventObj = new ManagerEvent();
    // eventObj.clazz = object.getClass();
    // eventObj.type = type;
    // eventObj.objects = new LinkedList<Object>();
    // eventMap.put(eventKey, eventObj);
    // }
    //
    // eventObj.objects.add(object);
    //
    // }
    // return eventMap.values();
    //
    // }

}
