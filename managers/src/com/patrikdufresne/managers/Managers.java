/*
 * Copyright (c) 2011, Patrik Dufresne. All rights reserved.
 * Patrik Dufresne PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.patrikdufresne.managers;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;

import org.h2.Driver;
import org.h2.tools.Server;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.SessionFactoryObserver;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.context.internal.ThreadLocalSessionContext;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.BootstrapServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

/**
 * This class is used to keep track of the Hibernate context. It's intended to
 * provide a single object to execute any database operation.
 * 
 * @author Patrik Dufresne
 * 
 */
public abstract class Managers {

	/**
	 * Open the database in creation mode.
	 */
	private static final String CREATE = "create";

	/**
	 * Open the database in update mode
	 */
	private static final String UPDATE = "update";

	/**
	 * Open the database in validate mode.
	 */
	private static final String VALIDATE = "validate";

	/**
	 * Check the url object. Generate an exception in case it's invalid.
	 * 
	 * @param url
	 */
	private static void checkUrl(DatabaseUrl url) {
		if (url == null) {
			throw new NullPointerException();
		}
		try {
			url.test();
		} catch (IOException e) {
			throw new HibernateException(e);
		}
	}

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
		public void disintegrate(SessionFactoryImplementor sessionFactory,
				SessionFactoryServiceRegistry serviceRegistry) {
			// Nothing to do
		}

		/**
		 * This implementation add an in
		 */
		@Override
		public void integrate(Configuration config,
				SessionFactoryImplementor sessionFactory,
				SessionFactoryServiceRegistry serviceRegistry) {

			CustomListener listener = new CustomListener();

			EventListenerRegistry eventListenerRegistry = serviceRegistry
					.getService(EventListenerRegistry.class);
			eventListenerRegistry.appendListeners(EventType.POST_INSERT,
					listener);
			eventListenerRegistry.appendListeners(EventType.POST_UPDATE,
					listener);
			eventListenerRegistry.appendListeners(EventType.POST_DELETE,
					listener);
			eventListenerRegistry
					.appendListeners(EventType.POST_LOAD, listener);

		}

		@Override
		public void integrate(MetadataImplementor metadata,
				SessionFactoryImplementor sessionFactory,
				SessionFactoryServiceRegistry serviceRegistry) {
			// Nothing to do
		}

	};

	private String mode;

	private Server server;

	/**
	 * This session factory observer is used to stop server.
	 */
	private SessionFactoryObserver sessionFactoryObserver = new SessionFactoryObserver() {

		private static final long serialVersionUID = 1L;

		@Override
		public void sessionFactoryClosed(SessionFactory factory) {
			stopServer();
		}

		@Override
		public void sessionFactoryCreated(SessionFactory factory) {
			// Validate or update the database
			if (mode.equals(VALIDATE)) {
				validateDatabase(factory);
			} else if (mode.equals(UPDATE)) {
				updateDatabase(factory);
			}
		}

	};

	private DatabaseUrl url;

	/**
	 * Create a new managers instance using the given database URL.
	 * 
	 * @param url
	 *            the database URL
	 * @throws ManagerException
	 */
	public Managers(DatabaseUrl url) throws ManagerException {

		// Test the url
		checkUrl(url);
		this.url = url;

		// Allow sub classes to add classes
		Configuration config = new Configuration();
		configure(config);

		ServiceRegistry serviceRegistry = new ServiceRegistryBuilder(
				new BootstrapServiceRegistryBuilder().with(integrator).build())
				.applySettings(config.getProperties()).buildServiceRegistry();

		try {
			this.factory = config.buildSessionFactory(serviceRegistry);
		} catch (Exception e) {
			// If the server got started, let stop it
			stopServer();
			throw new ManagerException(e);
		}

		// Test the database
		Session session = this.factory.withOptions().openSession();
		Transaction t = session.beginTransaction();
		t.rollback();

		// Create the event manager
		this.eventManager = new EventManager();

	}

	/**
	 * Add objects with different manager implementation.
	 * 
	 * @param list
	 *            the list of objects
	 */
	public void addAll(final Collection<? extends ManagedObject> list)
			throws ManagerException {
		// Open one transaction
		exec(new Exec() {
			@Override
			public void run() throws ManagerException {
				// Add each entities
				for (ManagedObject o : list) {
					IManager<ManagedObject> manager = (IManager<ManagedObject>) getManagerForClass(o
							.getClass());
					manager.add(Arrays.asList(o));
				}
			}
		});
	}

	/**
	 * Archive the given objects.
	 * <p>
	 * This function shall be used to avoid calling the specific implementation
	 * of the manager.
	 * 
	 * @param list
	 *            the objects to be archived.
	 * @throws ManagerException
	 *             If the object is not archivable or if the associated manager
	 *             doesn't implement the IArchivableManager.
	 */
	public void archiveAll(final Collection<? extends ManagedObject> list)
			throws ManagerException {
		// Open one transaction
		exec(new Exec() {
			@Override
			public void run() throws ManagerException {
				// Add each entities
				for (ManagedObject o : list) {
					if (!(o instanceof ArchivableObject)) {
						throw new ManagerException("object not archivable");
					}
					IManager<ManagedObject> manager = (IManager<ManagedObject>) getManagerForClass(o
							.getClass());
					if (!(manager instanceof IArchivableManager)) {
						throw new ManagerException(
								"manager not supporting archiving");
					}
					((IArchivableManager) manager).archive(Arrays.asList(o));
				}
			}
		});
	}

	/**
	 * Add the given observer to the list of observer being notify when an
	 * object of the given class type is added, updated or deleted.
	 */
	public void addObserver(int eventType, Class<?> cls,
			IManagerObserver observer) {
		this.eventManager.hook(eventType, cls, observer);
	}

	/**
	 * Set configuration properties. Sub classes may access the database URL
	 * using {@link #getDatabaseUrl()}.
	 * 
	 * @param config
	 *            the Configuration
	 */
	protected void configure(Configuration config) {

		// Use H2 DB dialect
		config.setProperty(Environment.DIALECT,
				H2Dialect.class.getCanonicalName());

		// Use H2 DB driver
		config.setProperty(Environment.DRIVER, Driver.class.getCanonicalName());

		// Set default username
		config.setProperty(Environment.USER, "sa");//$NON-NLS-1$

		// Set default password
		config.setProperty(Environment.PASS, "");//$NON-NLS-1$

		// Set default shema
		config.setProperty(Environment.DEFAULT_SCHEMA, "PUBLIC");//$NON-NLS-1$

		// Set a driver managed connection pool (to avoid usgin connection pool)
		config.setProperty(Environment.CONNECTION_PROVIDER,
				"org.hibernate.connection.DriverManagerConnectionProvider");

		// Enable Hibernate's automatic session context management
		config.setProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS,
				ThreadLocalSessionContext.class.getCanonicalName());//$NON-NLS-1$ 

		// Display SQL statement
		config.setProperty(Environment.SHOW_SQL, "true");//$NON-NLS-1$ //$NON-NLS-2$

		// Set a session listener
		config.setSessionFactoryObserver(this.sessionFactoryObserver);

		// Set URL according to database url data
		if (url.isLocal()) {
			// Get the file location
			File file = url.localfile();
			config.setProperty(Environment.URL,
					"jdbc:h2:tcp://localhost/" + url.getName()); //$NON-NLS-1$

			// Drop and re-create the database schema on startup
			if (!file.exists()) {
				this.mode = CREATE;
				config.setProperty(Environment.HBM2DDL_AUTO, "create");//$NON-NLS-1$
			} else {
				this.mode = UPDATE;
			}

			// Need to open a H2DB server locallysFs
			startServer(file.getParent());
		} else {
			// Set the connection string
			config.setProperty(Environment.URL, url.toString());
			this.mode = VALIDATE;
		}
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
	public <T> T get(final Class<T> cls, final Serializable id)
			throws ManagerException {
		return query(new Query<T>() {
			@Override
			public T run() throws ManagerException {
				return (T) ManagerContext.getDefaultSession().get(cls, id);
			}
		});
	}

	/**
	 * Return the database url previously provided in the constructor.
	 * 
	 * @return
	 */
	public DatabaseUrl getDatabaseUrl() {
		return this.url;
	}

	/**
	 * Return a list of network interface. Used to retrieve a list of valid ip
	 * address.
	 * 
	 * @return
	 */
	private String[] getInterfaces() {
		try {
			LinkedList<String> adresses = new LinkedList<String>();
			Enumeration<NetworkInterface> e = NetworkInterface
					.getNetworkInterfaces();

			while (e.hasMoreElements()) {
				NetworkInterface ni = e.nextElement();
				Enumeration<InetAddress> e2 = ni.getInetAddresses();

				while (e2.hasMoreElements()) {
					// Convert to dot representation
					InetAddress ip = e2.nextElement();
					if (!(ip instanceof Inet4Address)) {
						continue;
					}
					byte[] ipAddr = ip.getAddress();
					String ipAddrStr = ""; //$NON-NLS-1$
					for (int i = 0; i < ipAddr.length; i++) {
						if (i > 0) {
							ipAddrStr += "."; //$NON-NLS-1$
						}
						ipAddrStr += ipAddr[i] & 0xFF;
					}
					adresses.add(ipAddrStr);
				}
			}
			String[] array = new String[adresses.size()];
			return adresses.toArray(array);
		} catch (Exception e) {
			return new String[0];
		}
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
	public abstract IManager<ManagedObject> getManagerForClass(
			Class<? extends ManagedObject> clazz);

	/**
	 * Returns a list of possible url to connect remotely to the h2db server.
	 * 
	 * @return
	 */
	public String[] getServerUrl() {
		if (this.server != null && this.url != null
				&& this.url.getName() != null) {
			String[] ip = getInterfaces();
			if (ip != null) {
				ArrayList<String> urls = new ArrayList<String>(ip.length);
				for (int i = 0; i < ip.length; i++) {
					if (!ip[i].matches("127.0.(0|1).1")) { //$NON-NLS-1$
						urls.add(String
								.format("jdbc:h2:tcp://%s/%s", ip[i], this.url.getName())); //$NON-NLS-1$
					}
				}
				String a[] = new String[urls.size()];
				return urls.toArray(a);
			}
		}
		return new String[0];
	}

	/**
	 * Returns the session factory.
	 * 
	 * @return
	 */
	public SessionFactory getSessionFactory() {
		return this.factory;
	}

	/**
	 * Check if the internal database server is running.
	 * 
	 * @return True if the server is running.
	 */
	public boolean isServerRunning() {
		return this.server != null && this.server.isRunning(true);
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
	public void removeAll(final Collection<? extends ManagedObject> list)
			throws ManagerException {
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
	public void removeObserver(int eventType, Class<?> cls,
			IManagerObserver observer) {
		this.eventManager.unhook(eventType, cls, observer);
	}

	/**
	 * This function is used to run a runnable within a safe context for
	 * hibernate session.
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
				ManagerContext.getDefault().getSession().getTransaction()
						.commit();
			} catch (Throwable e) {
				// Error occurred within the transaction/runnable. Rollback any
				// modification and close the session so next run will create a
				// new session.
				ManagerContext.getDefault().getSession().getTransaction()
						.rollback();
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
	private void startServer(String baseDir) {
		if (isServerRunning()) {
			throw new RuntimeException("Already started"); //$NON-NLS-1$
		}

		try {
			// List available arguments : java -cp h2*.jar org.h2.tools.Server
			// -?
			// -tcpAllowOthers : to allow other computer to connect
			this.server = Server.createTcpServer(
					new String[] { "-tcpAllowOthers", "-baseDir", baseDir }) //$NON-NLS-1$ //$NON-NLS-2$
					.start();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * Stop the h2db server
	 */
	private void stopServer() {
		if (!isServerRunning()) {
			throw new RuntimeException("Server not running"); //$NON-NLS-1$
		}
		this.server.stop();
	}

	/**
	 * Used to update multiple entities
	 * 
	 * @param list
	 *            the collection of entity
	 * @throws ManagerException
	 */
	public void updateAll(final Collection<? extends ManagedObject> list)
			throws ManagerException {
		// Open a transaction
		exec(new Exec() {
			@Override
			public void run() throws ManagerException {
				// Update the entities
				for (ManagedObject o : list) {
					IManager<ManagedObject> manager = (IManager<ManagedObject>) getManagerForClass(o
							.getClass());
					manager.update(Arrays.asList(o));
				}
			}
		});

	}

	/**
	 * This function is called by the managers when the database shema may
	 * required to be updated. Sub-classes implementing this function should
	 * detect if an update is required and update the shema.
	 * <p>
	 * Sub-classes should use {@link DatabaseUpdateHelper} to get the metadata
	 * and to alter it.
	 * 
	 * @param factory
	 */
	protected void updateDatabase(SessionFactory factory) {
		// Nothing to do
	}

	/**
	 * This function is called by the manager when the database schema is open
	 * in validation mode.
	 * <p>
	 * Sub-classes should use the {@link DatabaseUpdateHelper} to get the
	 * metadata to be validated
	 * 
	 * @param factory
	 */
	protected void validateDatabase(SessionFactory factory) {

	}

}
