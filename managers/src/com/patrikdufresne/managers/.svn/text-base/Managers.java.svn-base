/*
 * Copyright (c) 2011, Patrik Dufresne. All rights reserved.
 * Patrik Dufresne PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.patrikdufresne.managers;

import java.util.Arrays;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostUpdateEventListener;

/**
 * This class is used to keep track of the hibernate context. It's intended to
 * provide a single object to execute any database opperation.
 * 
 * @author Patrik Dufresne
 * 
 */
public abstract class Managers {

	/**
	 * The database configuration used to connect to the database or to start
	 * the h2db server.
	 */
	protected DatabaseConfiguration config;

	/**
	 * The event table.
	 */
	protected EventManager eventManager;
	protected SessionFactory factory;

	public Managers(DatabaseUrl url) {

		// Create an hibernate configuration
		this.config = new DatabaseConfiguration();
		this.config.setDatabaseUrl(url);

		CustomInterceptor interceptor = new CustomInterceptor();
		// interceptor.setDispatcher(interceptor);
		this.config.setInterceptor(interceptor);
		this.config.getEventListeners().setPostInsertEventListeners(
				new PostInsertEventListener[] { interceptor });
		this.config.getEventListeners().setPostUpdateEventListeners(
				new PostUpdateEventListener[] { interceptor });
		this.config.getEventListeners().setPostDeleteEventListeners(
				new PostDeleteEventListener[] { interceptor });

		configure(this.config);

		// Create the factory
		this.factory = this.config.buildSessionFactory();

		// Test the database
		Session session = this.factory.openSession();
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
	public void addAll(List<? extends ManagedObject> list)
			throws ManagerException {
		// TODO do it in one transaction.
		for (ManagedObject o : list) {
			IManager<ManagedObject> manager = (IManager<ManagedObject>) getManagerForClass(o
					.getClass());
			manager.add(Arrays.asList(o));
		}
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
	 * Subclasses must implement this function to add further database
	 * configuration.
	 * 
	 * @param cfg
	 */
	abstract protected void configure(DatabaseConfiguration cfg);

	/**
	 * Disposed this managers.
	 */
	public void dispose() {
		if (this.factory != null) {
			this.factory.close();
		}
		this.factory = null;
	}

	public void exec(Exec runnable) throws ManagerException {
		run(runnable);
	}

	/**
	 * Return the database configuration.
	 * 
	 * @return
	 */
	public DatabaseConfiguration getConfig() {
		return this.config;
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
	 * Returns the session factory.
	 * 
	 * @return
	 */
	public SessionFactory getSessionFactory() {
		return this.factory;
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
	public void removeAll(final List<? extends ManagedObject> list)
			throws ManagerException {

		exec(new Exec() {
			@Override
			public void run() throws ManagerException {
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
			Session session = this.getSessionFactory().getCurrentSession();
			session.beginTransaction();
			ManagerContext.setDefaultSession(session);
			ManagerContext.getDefault().getEventTable().clear();
			try {
				if (runnable instanceof Query) {
					result = ((Query) runnable).run();
				} else {
					((Exec) runnable).run();
				}
				// Commit to database
				ManagerContext.getDefault().getSession().getTransaction()
						.commit();
			} catch (HibernateException e) {
				ManagerContext.getDefault().getSession().getTransaction()
						.rollback();
				throw new ManagerException(e);
			} finally {
				ManagerContext.setDefaultSession(null);
			}
			// Notify observers
			EventTable table = ManagerContext.getDefault().getEventTable();
			if (table.size() > 0) {
				table = table.clone();
				this.sendEvents(table);
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

	public void sendEvents(EventTable events) {
		this.eventManager.sendEvents(events);
	}

	public void updateAll(List<? extends ManagedObject> list)
			throws ManagerException {
		// TODO do it in one transaction.
		for (ManagedObject o : list) {
			IManager<ManagedObject> manager = (IManager<ManagedObject>) getManagerForClass(o
					.getClass());
			manager.update(Arrays.asList(o));
		}
	}

}