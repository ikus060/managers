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

import org.hibernate.Session;

public class ManagerContext {
    /**
     * The current hibernate session.
     */
    private Session session;

    /**
     * Create a new manager context.
     * 
     * @param session
     *            the hibernate session.
     */
    public ManagerContext() {

    }

    /**
     * Sets the hibernate session.
     * 
     * @param session
     */
    public void setSession(Session session) {
        this.session = session;
    }

    private static ThreadLocal<ManagerContext> defaultContext = new ThreadLocal<ManagerContext>();

    /**
     * Return the current hibernate session.
     * 
     * @return
     */
    public Session getSession() {
        return this.session;
    }

    /**
     * Returns the default context for the calling thread, or <code>null</code>
     * if no default context has been set.
     * 
     * @return the default realm, or <code>null</code>
     */
    public static ManagerContext getDefault() {
        return defaultContext.get();
    }

    /**
     * Sets the default realm for the calling thread, returning the current
     * default thread. This method is inherently unsafe, it is recommended to
     * use {@link #runWithDefault(Realm, Runnable)} instead. This method is
     * exposed to subclasses to facilitate testing.
     * 
     * @param realm
     *            the new default realm, or <code>null</code>
     * @return the previous default realm, or <code>null</code>
     */
    public static void setDefault(ManagerContext context) {
        defaultContext.set(context);
    }

    private EventTable events;

    /**
     * Return the current event table.
     * 
     * @return the event table.
     */
    public EventTable getEventTable() {
        if (this.events == null) {
            this.events = new EventTable();
        }
        return this.events;
    }

    /**
     * return the default session if there is a default context.
     * 
     * @return
     */
    public static Session getDefaultSession() {
        return getDefault() != null ? getDefault().getSession() : null;
    }

    /**
     * Sets a default session for the default manager context.
     * 
     * @param session
     *            the hibernate session.
     */
    public static void setDefaultSession(Session session) {
        if (getDefault() == null) {
            setDefault(new ManagerContext());
        }
        getDefault().setSession(session);
    }

}
