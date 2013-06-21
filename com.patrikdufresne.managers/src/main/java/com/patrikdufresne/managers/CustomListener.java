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

import java.lang.reflect.InvocationTargetException;

import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostLoadEvent;
import org.hibernate.event.spi.PostLoadEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;

/**
 * This listener implement two functionalities.
 * <p>
 * 1. It's allow to persist the object identity over multiple Hibernate session.
 * Something Hibernate doesn't provide by default. To implement this
 * functionality, this object use an instance of {@link ObjectIdentityTracker}
 * to keep track of every entity created by Hibernate.
 * <p>
 * 2. During an hibernate session, this class keep track of every entity added,
 * updated or removed from the persistence layer. At the commit time, this class
 * send a notification to dispatch the event related to an entity being changed.
 * 
 * @author Patrik Dufresne
 * 
 */
public class CustomListener implements PostInsertEventListener, PostUpdateEventListener, PostDeleteEventListener, PostLoadEventListener {

    /**
     * version id
     */
    private static final long serialVersionUID = -1772112377871583530L;

    /**
     * The object identity tracker used by this interceptor.
     */
    private ObjectIdentityTracker tracker;

    /**
     * Create a new interceptor.
     */
    public CustomListener() {
        this.tracker = new ObjectIdentityTracker();
    }

    /**
     * This implementation will remove the entity from the object-identity-track
     * and add a new remove event to the event table.
     */
    @Override
    public void onPostDelete(PostDeleteEvent event) {
        this.tracker.unregister(event.getPersister().getEntityName(), event.getId());
        ManagerContext.getDefault().getEventTable().add(ManagerEvent.REMOVE, event.getEntity());
    }

    /**
     * This implementation register the entity and add a new event to the event
     * table.
     */
    @Override
    public void onPostInsert(PostInsertEvent event) {
        this.tracker.register(event.getPersister().getEntityName(), event.getId(), event.getEntity());
        ManagerContext.getDefault().getEventTable().add(ManagerEvent.ADD, event.getEntity());
    }

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        ManagerContext.getDefault().getEventTable().add(ManagerEvent.UPDATE, event.getEntity());

        // On post update, any instance of this object required to be
        // updated
        for (Object obj : this.tracker.find(event.getPersister().getEntityName(), event.getId())) {
            try {
                ManagedObjectUtils.copyProperties(event.getEntity(), obj);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * This implementation register the loaded entity.
     */
    @Override
    public void onPostLoad(PostLoadEvent event) {
        String entityName = event.getEntity().getClass().getCanonicalName();
        this.tracker.register(entityName, event.getId(), event.getEntity());
    }

}
