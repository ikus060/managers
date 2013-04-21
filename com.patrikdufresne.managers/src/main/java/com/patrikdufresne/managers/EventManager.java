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
 * This class is used by the managers to register and unregister observers. It's
 * also used to dispatch events to those observers.
 */
public class EventManager {

    /**
     * Growth factor.
     */
    private static final int GROW_SIZE = 4;

    /**
     * List of class type.
     */
    private Class<?>[] clazzs;
    /**
     * Level indicator used to decrease or increase size of this table.
     */
    private int level;
    /**
     * List of listeners.
     */
    private IManagerObserver[] listeners;
    /**
     * List of types.
     */
    private int[] types;

    /**
     * Private constructor to avoid creating a singleton class.
     */
    public EventManager() {
        // Nothing to do
    }

    /**
     * Add a listener to be notify.
     * 
     * @param eventType
     *            the vent type
     * @param clazz
     *            the class type
     * @param listener
     *            the listener
     */
    public void hook(int eventType, Class<?> clazz, IManagerObserver listener) {
        // Check arguments
        if ((eventType & ManagerEvent.ALL) == 0) {
            throw new IllegalArgumentException("eventType"); //$NON-NLS-1$
        }
        if (clazz == null || listener == null) {
            throw new NullPointerException();
        }
        // Initialize arrays
        if (this.types == null) this.types = new int[GROW_SIZE];
        if (this.clazzs == null) this.clazzs = new Class<?>[GROW_SIZE];
        if (this.listeners == null) this.listeners = new IManagerObserver[GROW_SIZE];

        // Find an empty index
        int length = this.types.length;
        int index = length - 1;
        while (index >= 0) {
            if (this.types[index] != 0) {
                break;
            }
            --index;
        }
        index++;
        if (index == length) {
            // Increase arrays size
            int[] newTypes = new int[length + GROW_SIZE];
            Class<?>[] newClazzs = new Class<?>[length + GROW_SIZE];
            IManagerObserver[] newListeners = new IManagerObserver[length + GROW_SIZE];
            System.arraycopy(this.types, 0, newTypes, 0, length);
            System.arraycopy(this.clazzs, 0, newClazzs, 0, length);
            System.arraycopy(this.listeners, 0, newListeners, 0, length);
            this.types = newTypes;
            this.clazzs = newClazzs;
            this.listeners = newListeners;
        }
        this.types[index] = eventType;
        this.clazzs[index] = clazz;
        this.listeners[index] = listener;
    }

    /**
     * Check if there is a listener for a given event type and class.
     * 
     * @param eventType
     *            the event type
     * @param clazz
     *            the object class
     * @return number of listener
     */
    public boolean hooks(int eventType, Class<?> clazz) {
        // Check arrays
        if (this.types == null || this.clazzs == null || this.listeners == null) return false;
        // Check for any occurrence
        for (int i = 0; i < this.types.length; i++) {
            if ((this.types[i] & eventType) != 0 && this.clazzs[i] == clazz) return true;
        }
        return false;
    }

    /**
     * Remove a listener from this notify table.
     * 
     * @param index
     *            the index of the listener to be remove.
     */
    private void remove(int index) {
        if (this.level == 0) {
            // Move the data within the arrays
            int end = this.types.length - 1;
            System.arraycopy(this.types, index + 1, this.types, index, end - index);
            System.arraycopy(this.clazzs, index + 1, this.clazzs, index, end - index);
            System.arraycopy(this.listeners, index + 1, this.listeners, index, end - index);
            index = end;
        } else {
            if (this.level > 0) this.level = -this.level;
        }
        this.types[index] = 0;
        this.clazzs[index] = null;
        this.listeners[index] = null;
    }

    /**
     * Send the event to the interested listeners.
     * 
     * @param event
     *            the managed event
     */
    public void sendEvent(ManagerEvent event) {
        // Check the arrays
        if (this.types == null || this.clazzs == null || this.listeners == null) {
            return;
        }
        // Check the event type
        if ((event.type & ManagerEvent.ALL) == 0) {
            return;
        }
        this.level += this.level >= 0 ? 1 : -1;
        try {
            // Send the vent to any listener matching the event type and class
            // type
            for (int i = 0; i < this.types.length; i++) {
                if ((this.types[i] & event.type) != 0 && this.clazzs[i].isAssignableFrom(event.clazz) && this.listeners[i] != null) {
                    this.listeners[i].handleManagerEvent(event);
                }
            }
        } finally {
            boolean compact = this.level < 0;
            this.level -= this.level >= 0 ? 1 : -1;
            if (compact && this.level == 0) {
                int index = 0;
                for (int i = 0; i < this.types.length; i++) {
                    if (this.types[i] != 0) {
                        this.types[index] = this.types[i];
                        this.listeners[index] = this.listeners[i];
                        this.clazzs[index] = this.clazzs[i];
                        index++;
                    }
                }
                for (int i = index; i < this.types.length; i++) {
                    this.types[i] = 0;
                    this.listeners[i] = null;
                    this.clazzs[i] = null;
                }
            }
        }
    }

    /**
     * Sends events to observers.
     * 
     * @param events
     */
    public void sendEvents(EventTable table) {
        /*
         * Build a list of manager event.
         */
        List<ManagerEvent> events = new ArrayList<ManagerEvent>();
        for (int i = 0; i < table.types.size(); i++) {
            int eventType = table.types.get(i).intValue();
            Object entity = table.entities.get(i);
            Class<?> cls = entity.getClass();

            int index = 0;
            while (index < events.size() && (!events.get(index).clazz.equals(cls) || events.get(index).type != eventType)) {
                index++;
            }
            if (index < events.size()) {
                events.get(index).objects.add(entity);
            } else {
                ManagerEvent event = new ManagerEvent();
                event.clazz = cls;
                event.type = eventType;
                event.objects = new ArrayList<Object>();
                event.objects.add(entity);
                events.add(event);
            }
        }
        /*
         * Send those event
         */
        for (ManagerEvent event : events) {
            sendEvent(event);
        }

    }

    /**
     * Returns the number of listener in the notify table.
     * 
     * @return number of listener
     */
    public int size() {
        // Check if the arrays are initialized
        if (this.types == null || this.clazzs == null || this.listeners == null) return 0;
        int count = 0;
        for (int i = 0; i < this.types.length; i++) {
            if (this.types[i] != 0) count++;
        }
        return count;
    }

    /**
     * Remove the listener from the notify table.
     * 
     * @param eventType
     *            the event type
     * @param clazz
     *            the managed object class
     * @param listener
     *            the listener
     */
    public void unhook(int eventType, Class<?> clazz, IManagerObserver listener) {
        if (clazz == null || listener == null) {
            throw new NullPointerException();
        }
        if (this.types == null || this.clazzs == null || this.listeners == null) return;
        for (int i = 0; i < this.types.length; i++) {
            if (this.types[i] == eventType && this.clazzs[i] == clazz && this.listeners[i] == listener) {
                remove(i);
                return;
            }
        }
    }
}
