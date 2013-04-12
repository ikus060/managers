/*
 * Copyright (c) 2011, Patrik Dufresne. All rights reserved.
 * Patrik Dufresne PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.patrikdufresne.managers;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.hibernate.criterion.Restrictions;

/**
 * This implementation of {@link IManager} as specialized functions to manage
 * the archivable objects. Notably the #archive() function
 * 
 * @author Patrik Dufresne
 * 
 * @param <T>
 */
public abstract class AbstractArchivableManager<T extends ArchivableObject>
		extends AbstractManager<T> implements IArchivableManager<T> {

	/**
	 * Create a new manager.
	 */
	public AbstractArchivableManager(Managers managers) {
		super(managers);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Archive the given objects.
	 * 
	 * @param s
	 *            object to be archived
	 * 
	 * @throws ManagerException
	 */
	public void archive(final Collection<? extends T> s)
			throws ManagerException {
		checkObject(s);
		getManagers().exec(new Exec() {
			@Override
			public void run() throws ManagerException {
				preArchiveObjects(s);
				Date date = new Date();
				Iterator<? extends T> iter = s.iterator();
				while (iter.hasNext()) {
					T t = iter.next();
					preArchiveObject(t);
					t.setModificationDate(date);
					t.setArchivedDate(date);
					ManagerContext.getDefaultSession().update(t);
					postArchiveObject(t);
				}
				postArchiveObjects(s);
			}
		});

	}

	/**
	 * This implementation return the un-archived object. class.
	 * 
	 * @see com.patrikdufresne.managers.IManager#list()
	 */
	@Override
	public List<T> list() throws ManagerException {
		return getManagers().query(new Query<List<T>>() {
			@SuppressWarnings("unchecked")
			@Override
			public List<T> run() throws ManagerException {
				return (List<T>) ManagerContext
						.getDefaultSession()
						.createCriteria(objectClass())
						.add(Restrictions
								.isNull(ArchivableObject.ARCHIVED_DATE)).list();
			}
		});
	}

	/**
	 * This function is used to list all the archived object managed by this
	 * class.
	 * 
	 * @return list of archived object
	 * @throws ManagerException
	 */
	public List<T> listArchived() throws ManagerException {
		return getManagers().query(new Query<List<T>>() {
			@SuppressWarnings("unchecked")
			@Override
			public List<T> run() throws ManagerException {
				return (List<T>) ManagerContext
						.getDefaultSession()
						.createCriteria(objectClass())
						.add(Restrictions
								.isNotNull(ArchivableObject.ARCHIVED_DATE))
						.list();
			}
		});
	}

	/**
	 * Subclasses may implement this function to execute code after archiving an
	 * object. This function is called for each object passed to
	 * {@link #archive(Collection)}.
	 * 
	 * @param t
	 *            the object
	 * @throws ManagerException
	 */
	protected void postArchiveObject(T t) {
		// Nothing to do
	}

	/**
	 * Subclasses may implement this function to execute code after archiving
	 * objects. This function is call only once by {@link #archive(Collection)}.
	 * 
	 * @param t
	 *            the archived objects
	 * @throws ManagerException
	 */
	protected void postArchiveObjects(Collection<? extends T> s) {
		// Nothing to do
	}

	/**
	 * This implementation raise an exception if the record is archived.
	 */
	@Override
	protected void preAddObject(T t) throws ManagerException {
		super.preAddObject(t);
		if (t.getArchivedDate() != null) {
			throw new ManagerException("can't add an archived record");
		}
	}

	/**
	 * Subclasses may implement this function to execute code before archiving
	 * an object. This function is called for each object passed to
	 * {@link #archive(Collection)}.
	 * 
	 * @param t
	 *            the object
	 * @throws ManagerException
	 */
	protected void preArchiveObject(T t) {
		// Nothing to do
	}

	/**
	 * Subclasses may implement this function to execute code before archiving
	 * objects. This function is call only once by {@link #archive(Collection)}.
	 * 
	 * @param t
	 *            the objects
	 * @throws ManagerException
	 */
	protected void preArchiveObjects(Collection<? extends T> s) {
		// Nothing to do
	}

	/**
	 * This implementation raise an exception if the record is archived.
	 */
	@Override
	protected void preRemoveObject(T t) throws ManagerException {
		super.preRemoveObject(t);
		if (t.getArchivedDate() != null) {
			throw new ManagerException("can't remove an archived record");
		}
	}

	/**
	 * This implementation raise an exception if the record is archived.
	 */
	@Override
	protected void preUpdateObject(T t) throws ManagerException {
		super.preUpdateObject(t);
		if (t.getArchivedDate() != null) {
			throw new ManagerException("can't update an archived record");
		}
	}

}
