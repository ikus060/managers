/*
 * Copyright (c) 2011, Patrik Dufresne. All rights reserved.
 * Patrik Dufresne PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.patrikdufresne.managers.databinding;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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

import com.patrikdufresne.managers.IManager;
import com.patrikdufresne.managers.IManagerObserver;
import com.patrikdufresne.managers.ManagerEvent;
import com.patrikdufresne.managers.ManagerException;

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
public class ManagedObjectComputedSet extends AbstractObservableSet implements
		IManagerObserver {

	/**
	 * Inner class that implements interfaces that we don't want to expose as
	 * public API. Each interface could have been implemented using a separate
	 * anonymous class, but we combine them here to reduce the memory overhead
	 * and number of classes.
	 * 
	 * <p>
	 * The Runnable calls calculate and stores the result in cachedSet.
	 * </p>
	 * 
	 * <p>
	 * The IChangeListener stores each observable in the dependencies list. This
	 * is registered as the listener when calling ObservableTracker, to detect
	 * every observable that is used by computeValue.
	 * </p>
	 * 
	 * <p>
	 * The IChangeListener is attached to every dependency.
	 * </p>
	 * 
	 */
	private class PrivateInterface implements Runnable, IChangeListener,
			IStaleListener {
		public PrivateInterface() {
			// Nothing to do
		}

		@Override
		public void handleChange(ChangeEvent event) {
			makeDirty();
		}

		@Override
		public void handleStale(StaleEvent event) {
			if (!ManagedObjectComputedSet.this.dirty)
				makeStale();
		}

		@Override
		public void run() {
			ManagedObjectComputedSet.this.cachedSet = calculate();
			if (ManagedObjectComputedSet.this.cachedSet == null)
				ManagedObjectComputedSet.this.cachedSet = Collections.EMPTY_SET;
		}
	}

	Set<Object> cachedSet = new HashSet<Object>();

	private IObservable[] dependencies = new IObservable[0];

	boolean dirty = true;

	/**
	 * The adapted manager.
	 */
	private IManager manager;

	private PrivateInterface privateInterface = new PrivateInterface();

	private boolean stale = false;

	/**
	 * Create a new observable list from a manager.
	 * 
	 * @param manager
	 *            the object manager to adapt.
	 * 
	 * @throws NullPointerException
	 *             is the argument is null
	 */
	public ManagedObjectComputedSet(IManager manager) {
		this(Realm.getDefault(), manager);
	}

	/**
	 * Create a new observable list from a manager.
	 * 
	 * @param realm
	 *            the realm
	 * @param manager
	 * @param cache
	 */
	public ManagedObjectComputedSet(Realm realm, IManager manager) {
		super(realm);
		if (manager == null) {
			throw new NullPointerException();
		}
		ObservableTracker.observableCreated(this);
		this.manager = manager;
		// Attach listener
		attachManagerObserver();
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
		// If somebody is listening, we need to make sure we attach our own
		// listeners
		computeSetForListeners();
	}

	@Override
	public synchronized void addSetChangeListener(ISetChangeListener listener) {
		super.addSetChangeListener(listener);
		// If somebody is listening, we need to make sure we attach our own
		// listeners
		computeSetForListeners();
	}

	/**
	 * This function is called during the construction of this object to attach
	 * listener to the manager. By default, it attach it self on ADD and REMOVE
	 * events.
	 */
	protected void attachManagerObserver() {
		this.manager.addObserver(ManagerEvent.ADD, this);
		this.manager.addObserver(ManagerEvent.REMOVE, this);
	}

	/**
	 * Subclasses must override this method to calculate the set contents. Any
	 * dependencies used to calculate the set must be {@link IObservable}, and
	 * implementers must use one of the interface methods tagged TrackedGetter
	 * for ComputedSet to recognize it as a dependency.
	 * 
	 * @return the object's set.
	 */
	protected Set calculate() {
		try {
			Collection rawData = doList();
			if (rawData instanceof Set) {
				return (Set) rawData;
			}
			Set set = new HashSet(rawData);
			return set;
		} catch (ManagerException e) {
			Policy.getLog().log(
					new Status(IStatus.ERROR, Policy.JFACE_DATABINDING, 0,
							"Error querying the list from the manager.", e)); //$NON-NLS-1$
			return Collections.EMPTY_SET;
		}
	}

	/**
	 * This implementation throw an exception.
	 */
	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	private void computeSetForListeners() {
		// Some clients just add a listener and expect to get notified even if
		// they never called getValue(), so we have to call getValue() ourselves
		// here to be sure. Need to be careful about realms though, this method
		// can be called outside of our realm.
		// See also bug 198211. If a client calls this outside of our realm,
		// they may receive change notifications before the runnable below has
		// been executed. It is their job to figure out what to do with those
		// notifications.
		getRealm().exec(new Runnable() {
			@Override
			public void run() {
				if (ManagedObjectComputedSet.this.dependencies == null) {
					// We are not currently listening.
					// But someone is listening for changes. Call getValue()
					// to make sure we start listening to the observables we
					// depend on.
					getSet();
				}
			}
		});
	}

	/**
	 * This function is called during the disposale of this object to detach it
	 * self from the manager.
	 */
	protected void detachManagerObserver() {
		this.manager.removeObserver(ManagerEvent.ADD, this);
		this.manager.removeObserver(ManagerEvent.REMOVE, this);
	}

	@Override
	public synchronized void dispose() {
		try {
			stopListening();
			lastListenerRemoved();
			detachManagerObserver();
			this.manager = null;
		} finally {
			super.dispose();
		}
	}

	final Set doGetSet() {
		if (this.dirty) {
			// This line will do the following:
			// - Run the calculate method
			// - While doing so, add any observable that is touched to the
			// dependencies list
			IObservable[] newDependencies = ObservableTracker.runAndMonitor(
					this.privateInterface, this.privateInterface, null);

			// If any dependencies are stale, a stale event will be fired here
			// even if we were already stale before recomputing. This is in case
			// clients assume that a set change is indicative of non-staleness.
			this.stale = false;
			for (int i = 0; i < newDependencies.length; i++) {
				if (newDependencies[i].isStale()) {
					makeStale();
					break;
				}
			}

			if (!this.stale) {
				for (int i = 0; i < newDependencies.length; i++) {
					newDependencies[i].addStaleListener(this.privateInterface);
				}
			}

			this.dependencies = newDependencies;

			this.dirty = false;
		}

		return this.cachedSet;
	}

	/**
	 * Query the database.
	 * 
	 * @return listof object
	 */
	protected Collection doList() throws ManagerException {
		return this.manager.list();
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
		if (this.manager == null) {
			if (other.manager != null)
				return false;
		} else if (!this.manager.equals(other.manager))
			return false;
		return true;
	}

	/**
	 * This implementation returns the manager object class.
	 */
	@Override
	public Object getElementType() {
		return this.manager.objectClass();
	}

	/**
	 * Return the adapted manager.
	 * 
	 * @return the manager.
	 */
	public IManager getManager() {
		return this.manager;
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

	/**
	 * This impleentation handle the manager event to notify any listener of
	 * this observable list of the event.
	 */
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

	protected void notifyIfChanged(ManagerEvent event) {
		Set<Object> additions = new HashSet<Object>();
		Set<Object> removals = new HashSet<Object>();
		if ((event.type & ManagerEvent.UPDATE) != 0) {
			for (Object element : event.objects) {
				if (doSelect(element)) {
					additions.add(element);
					this.cachedSet.add(element);
				} else {
					removals.add(element);
					this.cachedSet.remove(element);
				}
			}
		}
		if ((event.type & ManagerEvent.ADD) != 0) {
			for (Object element : event.objects) {
				if (doSelect(element)) {
					additions.add(element);
					this.cachedSet.add(element);
				}
			}
		}
		if ((event.type & ManagerEvent.REMOVE) != 0) {
			removals.addAll(event.objects);
			this.cachedSet.removeAll(event.objects);
		}
		// Fire change
		fireSetChange(Diffs.createSetDiff(additions, removals));
	}

	@Override
	public int hashCode() {
		getterCalled();
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + this.manager.hashCode();
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

	private void stopListening() {
		if (this.dependencies != null) {
			for (int i = 0; i < this.dependencies.length; i++) {
				IObservable observable = this.dependencies[i];

				observable.removeChangeListener(this.privateInterface);
				observable.removeStaleListener(this.privateInterface);
			}
			this.dependencies = null;
		}
	}

	@Override
	public String toString() {
		getterCalled();
		return "ManagerObservableCollection [" + getWrappedSet().toString() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}

}
