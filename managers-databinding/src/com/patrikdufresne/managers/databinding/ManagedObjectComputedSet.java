/*
 * Copyright (c) 2011, Patrik Dufresne. All rights reserved.
 * Patrik Dufresne PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.patrikdufresne.managers.databinding;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.databinding.observable.set.AbstractObservableSet;
import org.eclipse.core.databinding.observable.set.ComputedSet;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.patrikdufresne.managers.IManagerObserver;
import com.patrikdufresne.managers.ManagedObject;
import com.patrikdufresne.managers.ManagerEvent;
import com.patrikdufresne.managers.ManagerException;
import com.patrikdufresne.managers.Managers;

/**
 * This class may be used to observe the a specific manager.
 * <p>
 * Notice : event is this class implement the {@link IManagerObserver}
 * interface, the client should not attach the object to a manager.
 * <p>
 * Subclasses may override the {@link #doList()} function to query the database
 * differently. When doing so, it's also recommended to implement doSelect to
 * filter the elements received within the events.
 * <p>
 * This implementation is an adaptation of the {@link ComputedSet} class.
 * 
 * @author Patrik Dufresne
 * 
 */
@SuppressWarnings("rawtypes")
public class ManagedObjectComputedSet extends AbstractObservableSet {

	/**
	 * Inner class that implements interfaces that we don't want to expose as
	 * public API. Each interface could have been implemented using a separate
	 * anonymous class, but we combine them here to reduce the memory overhead
	 * and number of classes.
	 * 
	 * <p>
	 * The IChangeListener is attached to every dependency.
	 * </p>
	 * 
	 * <p>
	 * IManagerObserver is attache to managers.
	 * </p>
	 * 
	 */
	private class PrivateInterface implements IChangeListener, IStaleListener,
			IManagerObserver {

		@Override
		public void handleChange(ChangeEvent event) {
			makeDirty();
		}

		@Override
		public void handleManagerEvent(final ManagerEvent event) {
			if (!isDisposed()) {
				getRealm().exec(new Runnable() {
					@Override
					public void run() {
						notifyIfChanged(event);
					}
				});
			}
		}

		@Override
		public void handleStale(StaleEvent event) {
			if (!ManagedObjectComputedSet.this.dirty)
				makeStale();
		}

	}

	/**
	 * Return a map with the default events to listen to.
	 * 
	 * @param elementType
	 *            the element type
	 * @return the map
	 */
	private static Map<Class, Integer> defaultEvents(
			Class<? extends ManagedObject> elementType) {
		Map<Class, Integer> map = new HashMap<Class, Integer>();
		map.put(elementType, Integer.valueOf(ManagerEvent.ALL));
		return map;
	}

	/** Cached set */
	Set cachedSet = new HashSet();

	/** List of observable dependencies. */
	private IObservable[] dependencies = new IObservable[0];

	/** True if dirty */
	boolean dirty = true;

	/** The element type of this observable */
	private Class<? extends ManagedObject> elementType;

	/** List of manager event to listen to */
	private Map<Class, Integer> events;

	private Managers managers;

	private PrivateInterface privateInterface = new PrivateInterface();

	private boolean stale = false;

	/**
	 * Default constructor to create an observable computed set for the managers
	 * and element type specified.
	 * <p>
	 * This computed set will listen to default manager events and has no
	 * dependencies.
	 * 
	 * @param managers
	 *            the managers
	 * @param elementType
	 *            the element type
	 */
	public ManagedObjectComputedSet(Managers managers,
			Class<? extends ManagedObject> elementType) {
		this(managers, elementType, defaultEvents(elementType), null);
	}

	/**
	 * Create an observable set for the managers and element type specified.
	 * This computed set will listen to default manager events.
	 * 
	 * @param managers
	 *            the managers
	 * @param elementType
	 *            the element type
	 * @param dependencies
	 *            list of observable dependencies or null
	 * 
	 */
	public ManagedObjectComputedSet(Managers managers,
			Class<? extends ManagedObject> elementType,
			IObservable[] dependencies) {
		this(managers, elementType, defaultEvents(elementType), dependencies);
	}

	/**
	 * Create an observable set for the managers and element type specified.
	 * This computed set will listen to the <code>events</code> specified.
	 * 
	 * 
	 * @param managers
	 *            the managers
	 * @param elementType
	 *            the element type
	 * @param events
	 *            the list of manager event to listen to
	 * 
	 */
	public ManagedObjectComputedSet(Managers managers,
			Class<? extends ManagedObject> elementType,
			Map<Class, Integer> events) {
		this(managers, elementType, events, null);
	}

	/**
	 * Create a new observable list from a manager.
	 * 
	 * @param managers
	 *            the managers to use
	 * @param elementType
	 *            The element type of this observable
	 * @param dependencies
	 *            the list of observable dependencies.
	 * 
	 * @throws NullPointerException
	 *             is the argument is null
	 */
	public ManagedObjectComputedSet(Managers managers,
			Class<? extends ManagedObject> elementType,
			Map<Class, Integer> events, IObservable[] dependencies) {
		this(Realm.getDefault(), managers, elementType, events, dependencies);
	}

	/**
	 * Create a new observable list from a manager.
	 * 
	 * @param realm
	 *            the realm
	 * @param managers
	 *            the managers to use
	 * @param dependencies
	 *            the list of observable dependencies.
	 * @param cache
	 */
	public ManagedObjectComputedSet(Realm realm, Managers managers,
			Class<? extends ManagedObject> elementType,
			Map<Class, Integer> events, IObservable[] dependencies) {
		super(realm);
		if (managers == null || elementType == null) {
			throw new NullPointerException();
		}
		ObservableTracker.observableCreated(this);

		this.managers = managers;
		this.elementType = elementType;
		this.events = events;
		this.dependencies = dependencies;
	}

	/**
	 * This implementation throw an exception.
	 */
	@Override
	public boolean add(Object o) {
		throw new UnsupportedOperationException();
	}

	/**
	 * This implementation throw an exception.
	 */
	@Override
	public boolean addAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized void addChangeListener(IChangeListener listener) {
		super.addChangeListener(listener);
	}

	@Override
	public synchronized void addSetChangeListener(ISetChangeListener listener) {
		super.addSetChangeListener(listener);
	}

	/**
	 * This implementation throw an exception.
	 */
	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	/**
	 * This implementation remove listener.
	 */
	@Override
	public synchronized void dispose() {
		try {
			stopListening();
			lastListenerRemoved();
			this.managers = null;
			this.dependencies = null;
			this.privateInterface = null;
		} finally {
			super.dispose();
		}
	}

	final Set doGetSet() {

		if (this.dirty) {

			checkListening();

			try {
				Collection rawData = doList();
				if (rawData instanceof Set) {
					this.cachedSet = (Set) rawData;
				} else {
					this.cachedSet = new HashSet(rawData);
				}
			} catch (ManagerException e) {
				Policy.getLog()
						.log(new Status(IStatus.ERROR,
								Policy.JFACE_DATABINDING, 0,
								"Error querying the list from the manager.", e)); //$NON-NLS-1$
				this.cachedSet = Collections.EMPTY_SET;
			}

			this.dirty = false;
		}

		return this.cachedSet;
	}

	/**
	 * Query the database.
	 * <p>
	 * Sub classes may override this function to query the database using
	 * something else then list().
	 * 
	 * @return a collection
	 */
	protected Collection doList() throws ManagerException {
		return getManagers().getManagerForClass(this.elementType).list();
	}

	/**
	 * Check if the element should be selected. Default implementation always
	 * return true.
	 * <p>
	 * Subclasses should override this function if it's override
	 * {@link #doList()}.
	 * 
	 * @param element
	 *            the element to check
	 * @return True if the element should be selected
	 */
	protected boolean doSelect(Object element) {
		return true;
	}

	/**
	 * This implementation return True if the object is a
	 * ManagerObservableCollection with the same manager.
	 */
	@Override
	public boolean equals(Object obj) {
		getterCalled();
		if (this == obj)
			return true;
		// Skip the equals of the super class.
		// if (!super.equals(obj))
		// return false;
		if (getClass() != obj.getClass())
			return false;
		ManagedObjectComputedSet other = (ManagedObjectComputedSet) obj;
		if (this.managers == null) {
			if (other.managers != null)
				return false;
		} else if (!this.managers.equals(other.managers))
			return false;

		if (this.elementType == null) {
			if (other.elementType != null)
				return false;
		} else if (!this.elementType.equals(other.elementType))
			return false;

		return true;
	}

	/**
	 * This implementation returns the manager object class.
	 */
	@Override
	public Object getElementType() {
		return this.elementType;
	}

	/**
	 * Return the managers
	 * 
	 * @return
	 */
	public Managers getManagers() {
		return this.managers;
	}

	final Set getSet() {
		getterCalled();
		return doGetSet();
	}

	/**
	 * This implementation always return null since all the primary function are
	 * overrided.
	 * 
	 * @return
	 */
	@Override
	protected Set getWrappedSet() {
		return doGetSet();
	}

	@Override
	public int hashCode() {
		getterCalled();
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + this.managers.hashCode();
		return result;
	}

	@Override
	public boolean isStale() {
		// recalculate set if dirty, to ensure staleness is correct.
		getSet();
		return this.stale;
	}

	protected void makeDirty() {
		if (!this.dirty) {
			this.dirty = true;

			makeStale();

			stopListening();

			// copy the old set
			final Set oldSet = new HashSet(this.cachedSet);
			// Fire the "dirty" event. This implementation recomputes the new
			// set lazily.
			fireSetChange(new SetDiff() {
				SetDiff delegate;

				@Override
				public Set getAdditions() {
					return getDelegate().getAdditions();
				}

				private SetDiff getDelegate() {
					if (this.delegate == null)
						this.delegate = Diffs.computeSetDiff(oldSet, getSet());
					return this.delegate;
				}

				@Override
				public Set getRemovals() {
					return getDelegate().getRemovals();
				}
			});
		}
	}

	void makeStale() {
		if (!this.stale) {
			this.stale = true;
			fireStale();
		}
	}

	protected void notifyIfChanged(ManagerEvent event) {
		Set<Object> additions = new HashSet<Object>();
		Set<Object> removals = new HashSet<Object>();
		if ((event.type & ManagerEvent.UPDATE) != 0) {
			for (Object element : event.objects) {
				if (doSelect(element)) {
					if (this.cachedSet.add(element)) {
						additions.add(element);
					}
				} else {
					if (this.cachedSet.remove(element)) {
						removals.add(element);
					}
				}
			}
		} else if ((event.type & ManagerEvent.ADD) != 0) {
			for (Object element : event.objects) {
				if (doSelect(element)) {
					if (this.cachedSet.add(element)) {
						additions.add(element);
					}
				}
			}
		} else if ((event.type & ManagerEvent.REMOVE) != 0) {
			for (Object element : event.objects) {
				if (this.cachedSet.remove(element)) {
					removals.add(element);
				}
			}
		}
		// Fire change
		if (additions.size() != 0 || removals.size() != 0) {
			fireSetChange(Diffs.createSetDiff(additions, removals));
		}
	}

	/**
	 * This implementation throw an exception.
	 */
	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	/**
	 * This implementation throw an exception.
	 */
	@Override
	public boolean removeAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	/**
	 * This implementation throw an exception.
	 */
	@Override
	public boolean retainAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * Add listener to dependencies
	 */
	protected void startListening() {
		if (this.dependencies != null) {
			for (int i = 0; i < this.dependencies.length; i++) {
				IObservable observable = this.dependencies[i];
				observable.addChangeListener(this.privateInterface);
				observable.addStaleListener(this.privateInterface);
			}
			this.dependencies = null;
		}

		if (this.events != null) {
			for (Entry<Class, Integer> e : this.events.entrySet()) {
				getManagers().addObserver(e.getValue().intValue(),
						(Class) e.getKey(), this.privateInterface);
			}
		}
	}

	/**
	 * Remove listener from dependencies.
	 */
	protected void stopListening() {
		if (this.dependencies != null) {
			for (int i = 0; i < this.dependencies.length; i++) {
				IObservable observable = this.dependencies[i];
				observable.removeChangeListener(this.privateInterface);
				observable.removeStaleListener(this.privateInterface);
			}
			this.dependencies = null;
		}

		if (this.events != null) {
			for (Entry<Class, Integer> e : this.events.entrySet()) {
				getManagers().removeObserver(e.getValue().intValue(),
						(Class) e.getKey(), this.privateInterface);
			}
		}
	}

	@Override
	public String toString() {
		getterCalled();
		return "ManagerObservableCollection [" + getWrappedSet().toString() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/** True if the listener is added */
	private boolean listenerAdded;

	/**
	 * 
	 */
	protected void firstListenerAdded() {
		super.firstListenerAdded();
		checkListening();
	}

	protected void checkListening() {
		// A listener is added, let add our listener too.
		if (!this.listenerAdded) {
			startListening();
			this.listenerAdded = true;
		}
	}

}
