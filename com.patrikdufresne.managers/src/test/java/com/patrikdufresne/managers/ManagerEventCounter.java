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
import java.util.Collections;
import java.util.List;

/**
 * This implementation of {@link IManagerObserver} keep reference of every event
 * received.
 * 
 * @author ikus060
 * 
 */
public final class ManagerEventCounter implements IManagerObserver {
    /**
     * Array holding the events
     */
    private List<ManagerEvent> events;

    /**
     * Remove all the event
     */
    public void clear() {
        this.events = null;
    }

    /**
     * Return the number of event captured.
     * 
     * @return
     */
    public int size() {
        if (this.events == null) {
            return 0;
        }
        return this.events.size();
    }

    /**
     * Return the list of event captured by this observer
     * 
     * @return list of event.
     */
    public List<ManagerEvent> getEvents() {
        if (this.events == null) {
            return Collections.EMPTY_LIST;
        }
        return this.events;
    }

    /**
     * This implementation keep reference of every event received.
     */
    @Override
    public void handleManagerEvent(ManagerEvent event) {
        if (this.events == null) {
            this.events = new ArrayList<ManagerEvent>();
        }
        this.events.add(event);
    }
}