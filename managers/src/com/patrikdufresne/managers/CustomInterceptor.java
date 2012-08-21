/*
 * Copyright (c) 2011, Patrik Dufresne. All rights reserved.
 * Patrik Dufresne PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.patrikdufresne.managers;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import org.hibernate.EmptyInterceptor;
import org.hibernate.event.PostDeleteEvent;
import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostLoadEvent;
import org.hibernate.event.PostLoadEventListener;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostUpdateEventListener;
import org.hibernate.event.PreLoadEvent;
import org.hibernate.event.PreLoadEventListener;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This interceptor implement two functionalities.
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
public class CustomInterceptor extends EmptyInterceptor implements
		PostInsertEventListener, PostUpdateEventListener,
		PostDeleteEventListener, PostLoadEventListener, PreLoadEventListener {

	/**
	 * version id
	 */
	private static final long serialVersionUID = -1772112377871583530L;

	/**
	 * The logger.
	 */
	protected final transient Logger logger = LoggerFactory
			.getLogger(getClass());

	/**
	 * The object identity tracker used by this interceptor.
	 */
	private ObjectIdentityTracker tracker;

	/**
	 * Create a new interceptor.
	 */
	public CustomInterceptor() {
		this.tracker = new ObjectIdentityTracker();
	}

	/**
	 * Get a fully loaded entity instance that is cached externally
	 * 
	 * @param entityName
	 *            the name of the entity
	 * @param id
	 *            the instance identifier
	 * @return a fully initialized entity
	 * @throws CallbackException
	 */
	// @Override
	// public Object getEntity(String entityName, Serializable id)
	// throws CallbackException {
	// return this.tracker.find(entityName, id);
	// }

	/**
	 * Called just before an object is initialized. The interceptor may change
	 * the <tt>state</tt>, which will be propagated to the persistent object.
	 * Note that when this method is called, <tt>entity</tt> will be an empty
	 * uninitialized instance of the class.
	 * <p>
	 * This implementation add the entity to the {@link ObjectIdentityTracker}.
	 * 
	 * @return <tt>true</tt> if the user modified the <tt>state</tt> in any way.
	 */
	@Override
	public boolean onLoad(Object entity, Serializable id, Object[] state,
			String[] propertyNames, Type[] types) {
		String entityName = entity.getClass().getCanonicalName();
		this.tracker.register(entityName, id, entity);
		return false;
	}

	/**
	 * This implementation will remove the entity from the
	 * object-identity-track.
	 */
	@Override
	public void onPostDelete(PostDeleteEvent event) {
		this.tracker.unregister(event.getPersister().getEntityName(),
				event.getId());
		ManagerContext.getDefault().getEventTable()
				.add(ManagerEvent.REMOVE, event.getEntity());
	}

	@Override
	public void onPostInsert(PostInsertEvent event) {
		this.tracker.register(event.getPersister().getEntityName(),
				event.getId(), event.getEntity());
		ManagerContext.getDefault().getEventTable()
				.add(ManagerEvent.ADD, event.getEntity());
	}

	@Override
	public void onPostUpdate(PostUpdateEvent event) {
		ManagerContext.getDefault().getEventTable()
				.add(ManagerEvent.UPDATE, event.getEntity());

		// TODO on post update, any instance of this object required to be
		// updated
		for (Object obj : this.tracker.find(event.getPersister()
				.getEntityName(), event.getId())) {
			try {
				ManagedObjectUtils.copyProperties(event.getEntity(), obj);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * This implementation suspend the change support for the managed object.
	 */
	@Override
	public void onPreLoad(PreLoadEvent event) {

	}

	@Override
	public void onPostLoad(PostLoadEvent event) {
		// TODO Auto-generated method stub

	}

}