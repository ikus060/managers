/**
 * Copyright(C) 2013 Patrik Dufresne Service Logiciel <info@patrikdufresne.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.patrikdufresne.managers;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.SessionFactoryObserver;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.jdbc.Work;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.BootstrapServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

/**
 * @author Patrik Dufresne
 * 
 */
public abstract class Managers {

    private Configuration config;

    /**
     * The event table.
     */
    private EventManager eventManager;

    private SessionFactory factory;

    /**
     * Hibernate integrator
     */
    private Integrator integrator = new Integrator() {

        @Override
        public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
            // Nothing to do
        }

        /**
         * This implementation add an in
         */
        @Override
        public void integrate(Configuration config, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {

            CustomListener listener = new CustomListener();

            EventListenerRegistry eventListenerRegistry = serviceRegistry.getService(EventListenerRegistry.class);
            eventListenerRegistry.appendListeners(EventType.POST_INSERT, listener);
            eventListenerRegistry.appendListeners(EventType.POST_UPDATE, listener);
            eventListenerRegistry.appendListeners(EventType.POST_DELETE, listener);
            eventListenerRegistry.appendListeners(EventType.POST_LOAD, listener);

        }

        @Override
        public void integrate(MetadataImplementor metadata, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
            // Nothing to do
        }

    };

    /**
     * Create a new instance of managers
     * 
     * @param config
     *            the database configuration.
     * @throws ManagerException
     */
    public Managers(final Configuration config) throws ManagerException {
        this.config = config;

        /*
         * Set a session listener
         */
        config.setSessionFactoryObserver(new SessionFactoryObserver() {

            @Override
            public void sessionFactoryClosed(SessionFactory factory) {
                // Nothing to do

            }

            @Override
            public void sessionFactoryCreated(SessionFactory factory) {
                // Update the database
                if ("create".equals(config.getProperty(Environment.HBM2DDL_AUTO))) {
                    updateDatabase(factory);
                } else if ("custom-update".equals(config.getProperty(Environment.HBM2DDL_AUTO))) {
                    updateDatabase(factory);
                    validateDatabase(factory);
                }
            }

        });

        /*
         * Configure the manager
         */
        configure(config);

        ServiceRegistry serviceRegistry = new ServiceRegistryBuilder(new BootstrapServiceRegistryBuilder().with(this.integrator).build()).applySettings(
                config.getProperties()).buildServiceRegistry();

        try {
            this.factory = config.buildSessionFactory(serviceRegistry);
        } catch (Exception e) {
            throw new ManagerException(e);
        }

        // Test the database
        // TODO: If org.h2.jdbc.JdbcSQLException: The database is read only [90097-176] is
        // raise at this point. The database was not properly closed.
        Session session = this.factory.withOptions().openSession();
        Transaction t = session.beginTransaction();
        t.rollback();
        session.close();

        // Create the event manager
        this.eventManager = new EventManager();
    }

    /**
     * Add objects with different manager implementation.
     * 
     * @param list
     *            the list of objects
     */
    public void addAll(final Collection<? extends ManagedObject> list) throws ManagerException {
        // Open one transaction
        exec(new Exec() {
            @Override
            public void run() throws ManagerException {
                // Add each entities
                for (ManagedObject o : list) {
                    IManager<ManagedObject> manager = (IManager<ManagedObject>) getManagerForClass(o.getClass());
                    manager.add(Arrays.asList(o));
                }
            }
        });
    }

    /**
     * Add the given observer to the list of observer being notify when an object of the given class type is added,
     * updated or deleted.
     */
    public void addObserver(int eventType, Class<?> cls, IManagerObserver observer) {
        this.eventManager.hook(eventType, cls, observer);
    }

    /**
     * Archive the given objects.
     * <p>
     * This function shall be used to avoid calling the specific implementation of the manager.
     * 
     * @param list
     *            the objects to be archived.
     * @throws ManagerException
     *             If the object is not archivable or if the associated manager doesn't implement the
     *             IArchivableManager.
     */
    public void archiveAll(final Collection<? extends ManagedObject> list) throws ManagerException {
        // Open one transaction
        exec(new Exec() {
            @Override
            public void run() throws ManagerException {
                // Add each entities
                for (ManagedObject o : list) {
                    if (!(o instanceof ArchivableObject)) {
                        throw new ManagerException("object not archivable");
                    }
                    IManager<ManagedObject> manager = (IManager<ManagedObject>) getManagerForClass(o.getClass());
                    if (!(manager instanceof IArchivableManager)) {
                        throw new ManagerException("manager not supporting archiving");
                    }
                    ((IArchivableManager) manager).archive(Arrays.asList(o));
                }
            }
        });
    }

    /**
     * Set configuration properties. Sub classes may access the database URL using {@link #getDatabaseUrl()}.
     * 
     * @param config
     *            the Configuration
     */
    protected void configure(Configuration config) {

        // Display SQL statement
        config.setProperty(Environment.SHOW_SQL, "true");//$NON-NLS-1$ //$NON-NLS-2$

    }

    /**
     * Disposed this managers and close sessions.
     */
    public void dispose() {
        if (this.factory != null) {
            this.factory.close();
        }
        this.factory = null;
    }

    /**
     * Use to execute an operation withing one transaction.
     * 
     * @param runnable
     *            the runnable to execute
     * @throws ManagerException
     */
    public void exec(Exec runnable) throws ManagerException {
        run(runnable);
    }

    /**
     * Query a single object from the database
     * 
     * @param cls
     *            the object type
     * @param id
     *            the object id
     * @return the object
     * @throws ManagerException
     */
    public <T> T get(final Class<T> cls, final Serializable id) throws ManagerException {
        return query(new Query<T>() {
            @Override
            public T run() throws ManagerException {
                return (T) ManagerContext.getDefaultSession().get(cls, id);
            }
        });
    }

    /**
     * Return the manager for the given object class.
     * 
     * @param managers
     *            the managers
     * @param clazz
     *            the class
     * @return the manager or null if not found.
     */
    public abstract IManager<ManagedObject> getManagerForClass(Class<? extends ManagedObject> clazz);

    /**
     * Returns the session factory.
     * 
     * @return
     */
    public SessionFactory getSessionFactory() {
        return this.factory;
    }

    /**
     * Return the URL used to open this managers.
     * @return
     */
    public String getUrl() {
        return this.config.getProperty(Environment.URL);
    }

    /**
     * Check if database is read-only.
     * 
     * @return True if database is read-only.
     */
    public boolean isReadOnly() {
        // Check if readonly
        final AtomicBoolean readonly = new AtomicBoolean();
        Session session = getSessionFactory().withOptions().openSession();
        Transaction t = session.beginTransaction();
        session.doWork(new Work() {
            @Override
            public void execute(Connection connection) throws SQLException {
                readonly.set(connection.isReadOnly());
            }
        });
        t.commit();
        session.close();
        return readonly.get();
    }

    @SuppressWarnings("unchecked")
    public <E> E query(Query<E> runnable) throws ManagerException {
        return (E) run(runnable);
    }

    /**
     * This function is used to remove all the given objects.
     * 
     * @param managers
     *            the managers to used
     * @param list
     *            the objects to remove
     * @throws ManagerException
     */
    public void removeAll(final Collection<? extends ManagedObject> list) throws ManagerException {
        // Open one transaction
        exec(new Exec() {
            @Override
            public void run() throws ManagerException {
                // Remove the entities
                for (ManagedObject o : list) {
                    getManagerForClass(o.getClass()).remove(Arrays.asList(o));
                }
            }
        });

    }

    /**
     * Remove the given observer from the list of observer to be notify.
     * 
     * @param eventType
     *            the event type
     * @param cls
     *            the class type
     * @param observer
     *            the observer to be remove
     */
    public void removeObserver(int eventType, Class<?> cls, IManagerObserver observer) {
        this.eventManager.unhook(eventType, cls, observer);
    }

    /**
     * This function is used to run a runnable within a safe context for hibernate session.
     * 
     * @param runnable
     * @throws ManagerException
     */
    @SuppressWarnings("rawtypes")
    private Object run(Object runnable) throws ManagerException {

        Object result = null;
        if (ManagerContext.getDefaultSession() == null) {
            Session session;
            // Opening a new session and starting a transaction may throw
            // exceptions, make sure to run it inside a try catch to properly
            // handle the error.
            try {
                session = this.getSessionFactory().getCurrentSession();
            } catch (Throwable e) {
                // The session is not created, re-throw the exception
                throw new ManagerException("can't open a new session", e);
            }
            try {
                session.beginTransaction();
            } catch (Throwable e) {
                // Can't start a new transaction, release session so next time a
                // new session will be created.
                session.close();
                throw new ManagerException("can't begin a transaction", e);
            }
            // Sets the default session to use within this manager context.
            ManagerContext.setDefaultSession(session);
            ManagerContext.getDefault().getEventTable().clear();
            try {
                // Run the runnable
                if (runnable instanceof Query) {
                    result = ((Query) runnable).run();
                } else {
                    ((Exec) runnable).run();
                }
                // Commit to database & close session
                ManagerContext.getDefault().getSession().getTransaction().commit();
            } catch (Throwable e) {
                // Error occurred within the transaction/runnable. Rollback any
                // modification and close the session so next run will create a
                // new session.
                ManagerContext.getDefault().getSession().getTransaction().rollback();
                if (runnable instanceof SafeQuery) {
                    ((SafeQuery) runnable).handleException(e);
                } else if (runnable instanceof SafeExec) {
                    ((SafeExec) runnable).handleException(e);
                }
                throw new ManagerException(e);
            } finally {
                // Unset the default session
                ManagerContext.setDefaultSession(null);
            }
            // Notify observers
            EventTable table = ManagerContext.getDefault().getEventTable();
            if (table.size() > 0) {
                table = table.clone();
                this.eventManager.sendEvents(table);
                table.clear();
            }
        } else {
            // There is a current context, so let re-use it
            if (runnable instanceof Query) {
                result = ((Query) runnable).run();
            } else {
                ((Exec) runnable).run();
            }
        }
        return result;
    }

    /**
     * Start the h2db server.
     * 
     * @param baseDir
     */
    // private void startServer(String baseDir) {
    // if (isServerRunning()) {
    //			throw new RuntimeException("Already started"); //$NON-NLS-1$
    // }
    //
    // try {
    // // List available arguments : java -cp h2*.jar org.h2.tools.Server
    // // -?
    // // -tcpAllowOthers : to allow other computer to connect
    // this.server = Server.createTcpServer(
    //					new String[] { "-tcpAllowOthers", "-baseDir", baseDir }) //$NON-NLS-1$ //$NON-NLS-2$
    // .start();
    // } catch (SQLException e) {
    // e.printStackTrace();
    // throw new RuntimeException(e);
    // }
    // }

    /**
     * Stop the h2db server
     */
    // private void stopServer() {
    // if (!isServerRunning()) {
    //			throw new RuntimeException("Server not running"); //$NON-NLS-1$
    // }
    // this.server.stop();
    // }

    /**
     * Used to update multiple entities
     * 
     * @param list
     *            the collection of entity
     * @throws ManagerException
     */
    public void updateAll(final Collection<? extends ManagedObject> list) throws ManagerException {
        // Open a transaction
        exec(new Exec() {
            @Override
            public void run() throws ManagerException {
                // Update the entities
                for (ManagedObject o : list) {
                    IManager<ManagedObject> manager = (IManager<ManagedObject>) getManagerForClass(o.getClass());
                    manager.update(Arrays.asList(o));
                }
            }
        });

    }

    /**
     * This function is called by the managers when the database shema may required to be updated. Sub-classes
     * implementing this function should detect if an update is required and update the shema.
     * <p>
     * Sub-classes should use {@link DatabaseUpdateHelper} to get the metadata and to alter it.
     * 
     * @param factory
     */
    protected void updateDatabase(SessionFactory factory) {
        // Nothing to do
    }

    /**
     * This function is called by the manager when the database schema is open in validation mode.
     * <p>
     * Sub-classes should use the {@link DatabaseUpdateHelper} to get the metadata to be validated
     * 
     * @param factory
     */
    protected void validateDatabase(SessionFactory factory) {

    }

}
