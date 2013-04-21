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

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.hibernate.criterion.Projections;

/**
 * This implementation of {@link IManager} provide basic feature to make it
 * easier to implement the interface.
 */
public abstract class AbstractManager<T extends ManagedObject> implements IManager<T> {
    /**
     * Define the managers.
     */
    private Managers managers;

    /**
     * Create a new manager.
     */
    public AbstractManager(Managers managers) {
        // Nothing to do
        this.managers = managers;
    }

    /**
     * This implementation add the given list of object to the database using
     * Hibernate.
     * 
     * @see com.patrikdufresne.managers.IManager#add(java.util.Collection)
     */
    @Override
    public void add(final Collection<? extends T> s) throws ManagerException {
        checkObject(s);
        getManagers().exec(new Exec() {
            @Override
            public void run() throws ManagerException {
                preAddObjects(s);
                Date date = new Date();
                Iterator<? extends T> iter = s.iterator();
                while (iter.hasNext()) {
                    T t = iter.next();
                    preAddObject(t);
                    t.setCreationDate(date);
                    t.setModificationDate(date);
                    ManagerContext.getDefaultSession().save(t);
                    postAddObject(t);
                }
                postAddObjects(s);
            }
        });
    }

    /**
     * This implementation addthe observer to the list of observer being notify.
     */
    @Override
    public void addObserver(int eventType, Class<?> cls, IManagerObserver observer) {
        if (!objectClass().isAssignableFrom(cls)) {
            throw new ClassCastException();
        }
        getManagers().addObserver(eventType, cls, observer);
    }

    /**
     * This implementation add the observer to the list of observers getting
     * notify.
     * 
     * @see com.patrikdufresne.managers.IManager#addObserver(int,
     *      com.patrikdufresne.managers.IManagerObserver)
     */
    @Override
    public void addObserver(int eventType, IManagerObserver observer) {
        addObserver(eventType, objectClass(), observer);
    }

    /**
     * Validate the object type. Not intend to be subclass.
     * 
     * @param s
     */
    protected void checkObject(Collection<? extends T> s) {
        if (s == null) throw new NullPointerException();
        Iterator<? extends T> iter = s.iterator();
        while (iter.hasNext()) {
            if (!objectClass().isInstance(iter.next())) throw new ClassCastException();
        }
    }

    /**
     * This implementation return the object with the given identifier.
     * 
     * @see com.patrikdufresne.managers.IManager#get()
     */
    @Override
    public T get(final int id) throws ManagerException {
        return getManagers().query(new Query<T>() {
            @SuppressWarnings("unchecked")
            @Override
            public T run() throws ManagerException {
                return (T) ManagerContext.getDefaultSession().get(objectClass(), Integer.valueOf(id));
            }
        });
    }

    /**
     * Return the managers
     * 
     * @return
     */
    public Managers getManagers() {
        return this.managers;
    }

    /**
     * This implementation return a complete list of all object managed by this
     * class.
     * 
     * @see com.patrikdufresne.managers.IManager#list()
     */
    @Override
    public List<T> list() throws ManagerException {
        return getManagers().query(new Query<List<T>>() {
            @SuppressWarnings("unchecked")
            @Override
            public List<T> run() throws ManagerException {
                return (List<T>) ManagerContext.getDefaultSession().createCriteria(objectClass()).list();
            }
        });
    }

    /**
     * Subclasses may implement this function to execute code after adding the
     * object to database.
     * 
     * @param t
     *            the object added
     * @throws ManagerException
     */
    protected void postAddObject(T t) throws ManagerException {
        // Implemented by subclasses
    }

    /**
     * Subclasses may implement this function to execute code after adding the
     * objects to database.
     * 
     * @param s
     *            the objects added
     * @throws ManagerException
     */
    protected void postAddObjects(Collection<? extends T> s) throws ManagerException {
        // Implemented by sub-classes.
    }

    protected void postRemoveObject(T t) throws ManagerException {
        // Implemented by subclasses
    }

    protected void postRemoveObjects(Collection<? extends T> s) throws ManagerException {
        // Implemented by subclasses
    }

    protected void postUpdateObject(T t) throws ManagerException {
        // Implemented by subclasses
    }

    protected void postUpdateObjects(Collection<? extends T> s) throws ManagerException {
        // Implemented by subclasses
    }

    /**
     * Sub-class may implement this function to validate the insertion. Notice :
     * the transaction is already open.
     * 
     * @param t
     */
    protected void preAddObject(T t) throws ManagerException {
        // Implemented by sub-class
    }

    protected void preAddObjects(Collection<? extends T> s) throws ManagerException {
        // Implemented by sub-class
    }

    /**
     * Sub-class may implement this function to validate the removal. Notice :
     * the transaction is already open.
     * 
     * @param t
     */
    protected void preRemoveObject(T t) throws ManagerException {
        // Implemented by sub-class
    }

    protected void preRemoveObjects(Collection<? extends T> s) throws ManagerException {
        // Implemented by sub-class
    }

    /**
     * Sub-class may implement this function to validate the update. Notice :
     * the transaction is already open.
     * 
     * @param t
     */
    protected void preUpdateObject(T t) throws ManagerException {
        // Implemented by sub-class
    }

    protected void preUpdateObjects(Collection<? extends T> t) throws ManagerException {
        // Implemented by sub-class
    }

    /**
     * This implementation remove the given object using Hibernate.
     * 
     * @see com.patrikdufresne.managers.IManager#remove(java.util.Collection)
     */
    @Override
    public void remove(final Collection<? extends T> s) throws ManagerException {
        checkObject(s);
        getManagers().exec(new Exec() {
            @Override
            public void run() throws ManagerException {
                preRemoveObjects(s);
                Iterator<? extends T> iter = s.iterator();
                while (iter.hasNext()) {
                    T t = iter.next();
                    preRemoveObject(t);
                    ManagerContext.getDefaultSession().delete(t);
                    postRemoveObject(t);
                }
                postRemoveObjects(s);
            }
        });
    }

    @Override
    public void removeObserver(int eventType, Class<?> cls, IManagerObserver observer) {
        if (!objectClass().isAssignableFrom(cls)) {
            throw new ClassCastException();
        }
        getManagers().removeObserver(eventType, cls, observer);
    }

    /**
     * This implementation remote the observer.
     * 
     * @see com.patrikdufresne.managers.IManager#removeObserver(int,
     *      com.patrikdufresne.managers.IManagerObserver)
     */
    @Override
    public void removeObserver(int eventType, IManagerObserver observer) {
        removeObserver(eventType, objectClass(), observer);
    }

    /**
     * This implementation return the number of record in the table.
     */
    @Override
    public int size() throws ManagerException {
        return getManagers().query(new Query<Integer>() {
            @Override
            public Integer run() throws ManagerException {
                return (Integer) ManagerContext.getDefaultSession().createCriteria(objectClass()).setProjection(Projections.count("id")).uniqueResult(); //$NON-NLS-1$
            }
        }).intValue();
    }

    /**
     * This implementation update the given list of object using Hibernate.
     * 
     * @see com.patrikdufresne.managers.IManager#update(java.util.Collection)
     */
    @Override
    public void update(final Collection<? extends T> s) throws ManagerException {
        checkObject(s);
        getManagers().exec(new Exec() {
            @Override
            public void run() throws ManagerException {
                preUpdateObjects(s);
                Date date = new Date();
                Iterator<? extends T> iter = s.iterator();
                while (iter.hasNext()) {
                    T t = iter.next();
                    preUpdateObject(t);
                    t.setModificationDate(date);
                    ManagerContext.getDefaultSession().update(t);
                    postUpdateObject(t);
                }
                postUpdateObjects(s);
            }
        });
    }
}
