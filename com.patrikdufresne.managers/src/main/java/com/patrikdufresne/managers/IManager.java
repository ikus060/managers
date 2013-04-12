/*
 * Copyright (c) 2011, Patrik Dufresne. All rights reserved.
 * Patrik Dufresne PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.patrikdufresne.managers;

import java.util.Collection;
import java.util.List;

/**
 * This interface represent a manager used to manipulate object within the
 * persistent layer. Each implementation of this interface should be
 * specialized to manage one class type.
 * 
 * @author Patrik Dufresne
 * 
 * @param <T>
 */
public interface IManager<T extends ManagedObject> {

	/**
	 * Add a set of objects.
	 * 
	 * @param t
	 *            the array of object
	 */
	public void add(Collection<? extends T> t) throws ManagerException;

	/**
	 * Add an observer to this manager that will be notify when object are added
	 * updated or removed.
	 * 
	 * @param eventType
	 *            the event type
	 * @param observer
	 *            the observer to be added
	 * @param cls
	 *            the type of class
	 */
	public void addObserver(int eventType, Class<?> cls,
			IManagerObserver observer);

	/**
	 * Add an observer to this manager that will notify when object are added,
	 * updated or removed.
	 * 
	 * @param observer
	 *            the observer to add.
	 */
	public void addObserver(int eventType, IManagerObserver observer);

	public T get(int id) throws ManagerException;

	/**
	 * Return the managers.
	 * 
	 * @return
	 */
	public Managers getManagers();

	/**
	 * List all records.
	 * 
	 * @return list of records
	 */
	public List<T> list() throws ManagerException;

	/**
	 * Return the object class manage by class implementing this interface.
	 * 
	 * @return the class type
	 */
	public Class<T> objectClass();

	/**
	 * Remove the given list of object.
	 * 
	 * @param t
	 *            the list
	 */
	public void remove(Collection<? extends T> t) throws ManagerException;

	/**
	 * Remove an observer.
	 * 
	 * @param eventType
	 * @param cls
	 * @param observer
	 */
	public void removeObserver(int eventType, Class<?> cls,
			IManagerObserver observer);

	/**
	 * Remove an observer from this manager.
	 * 
	 * @param observer
	 *            the observer to remove.
	 */
	public void removeObserver(int eventType, IManagerObserver observer);

	/**
	 * Return the number of records.
	 * 
	 * @return the size
	 */
	public int size() throws ManagerException;

	/**
	 * Update the given object.
	 * 
	 * @param t
	 *            the objects to update
	 */
	public void update(Collection<? extends T> t) throws ManagerException;

}
