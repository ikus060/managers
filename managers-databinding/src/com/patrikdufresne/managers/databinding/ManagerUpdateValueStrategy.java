/*
 * Copyright (c) 2011, Patrik Dufresne. All rights reserved.
 * Patrik Dufresne PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.patrikdufresne.managers.databinding;

import java.util.Arrays;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.IBeanObservable;
import org.eclipse.core.databinding.observable.IDecoratingObservable;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObserving;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;

import com.patrikdufresne.managers.ManagedObject;
import com.patrikdufresne.managers.ManagerException;
import com.patrikdufresne.managers.Managers;

/**
 * This implementation of the update strategy will persist the element in the
 * manager.
 * 
 * @author Patrik Dufresne
 * 
 */
public class ManagerUpdateValueStrategy extends UpdateValueStrategy {

	/**
	 * Create an update strategy to persists the managed object of an observable
	 * value.
	 * 
	 * @param managers
	 *            the managers instance to be used to persist the
	 *            {@link ManagedObject}.
	 * 
	 * @return a new update set strategy
	 */
	public static ManagerUpdateValueStrategy persistObservableValue(
			Managers managers, final IObservableValue observableValue) {
		return persistObservableValue(managers, true,
				UpdateValueStrategy.POLICY_UPDATE, observableValue);
	}

	/**
	 * Create an update strategy to persists the managed object of an observable
	 * value.
	 * 
	 * @param managers
	 *            the managers instance to be used to persist the
	 *            {@link ManagedObject}.
	 * 
	 * @param updatePolicy
	 *            one of {@link #POLICY_NEVER}, {@link #POLICY_ON_REQUEST},
	 *            {@link #POLICY_CONVERT}, or {@link #POLICY_UPDATE}
	 * 
	 * @return a new update set strategy
	 */
	public static ManagerUpdateValueStrategy persistObservableValue(
			Managers managers, int updatePolicy,
			final IObservableValue observableValue) {
		return persistObservableValue(managers, true, updatePolicy,
				observableValue);
	}

	/**
	 * Create an update strategy to persists the managed object of an observable
	 * value.
	 * 
	 * @param managers
	 *            the managers instance to be used to persist the
	 *            {@link ManagedObject}.
	 * @param provideDefaults
	 *            if <code>true</code>, default validators and a default
	 *            converter will be provided based on the observable value's
	 *            type.
	 * @param updatePolicy
	 *            one of {@link #POLICY_NEVER}, {@link #POLICY_ON_REQUEST},
	 *            {@link #POLICY_CONVERT}, or {@link #POLICY_UPDATE}
	 * 
	 * @return a new update set strategy
	 */
	public static ManagerUpdateValueStrategy persistObservableValue(
			Managers managers, boolean provideDefaults, int updatePolicy,
			final IObservableValue observableValue) {
		if (observableValue == null) {
			throw new IllegalArgumentException();
		}
		// Create an anonymous class overriding the findManagedObject method to
		// provide the value of the observable.
		return new ManagerUpdateValueStrategy(managers, provideDefaults,
				updatePolicy) {

			/**
			 * This implementation return the value of the observable if it
			 * contains a ManagedObject
			 */
			@Override
			protected ManagedObject findManagedObject(IObservable target) {
				if (observableValue != null && !observableValue.isDisposed()
						&& observableValue.getValue() instanceof ManagedObject) {
					return (ManagedObject) observableValue.getValue();
				}
				return null;
			}

		};

	}

	private Managers managers;

	/**
	 * Creates a new update value strategy for automatically updating the
	 * destination observable value whenever the source observable value
	 * changes. Default validators and a default converter will be provided. The
	 * defaults can be changed by calling one of the setter methods.
	 * 
	 * @param managers
	 *            the managers instance to be used to persist the
	 *            {@link ManagedObject}.
	 */
	public ManagerUpdateValueStrategy(Managers managers) {
		this(managers, true, UpdateValueStrategy.POLICY_UPDATE);
	}

	/**
	 * Creates a new update value strategy with a configurable update policy.
	 * Default validators and a default converter will be provided if
	 * <code>provideDefaults</code> is <code>true</code>. The defaults can be
	 * changed by calling one of the setter methods.
	 * 
	 * @param managers
	 *            the managers instance to be used to persist the
	 *            {@link ManagedObject}.
	 * @param provideDefaults
	 *            if <code>true</code>, default validators and a default
	 *            converter will be provided based on the observable value's
	 *            type.
	 * @param updatePolicy
	 *            one of {@link #POLICY_NEVER}, {@link #POLICY_ON_REQUEST},
	 *            {@link #POLICY_CONVERT}, or {@link #POLICY_UPDATE}
	 */
	public ManagerUpdateValueStrategy(Managers managers,
			boolean provideDefaults, int updatePolicy) {
		super(provideDefaults, updatePolicy);
		if (managers == null) {
			throw new NullPointerException();
		}
		this.managers = managers;
	}

	/**
	 * Creates a new update value strategy with a configurable update policy.
	 * Default validators and a default converter will be provided. The defaults
	 * can be changed by calling one of the setter methods.
	 * 
	 * @param managers
	 *            the managers instance to be used to persist the
	 *            {@link ManagedObject}.
	 * @param updatePolicy
	 *            one of {@link #POLICY_NEVER}, {@link #POLICY_ON_REQUEST},
	 *            {@link #POLICY_CONVERT}, or {@link #POLICY_UPDATE}
	 */
	public ManagerUpdateValueStrategy(Managers managers, int updatePolicy) {
		this(managers, true, updatePolicy);
	}

	/**
	 * This implementation sets the observable value and if the observable is
	 * associated to a ManagedObject, this class will persiste the data into the
	 * manager.
	 */
	@Override
	protected IStatus doSet(IObservableValue observableValue, Object value) {
		// Call the super method to sets the value
		IStatus status = super.doSet(observableValue, value);
		if (!status.isOK()) {
			return status;
		}
		// Persists the modification
		return persist(observableValue);
	}

	/**
	 * Return an instance of {@link ManagedObject} embedded in the specified
	 * observable object. This function is used by
	 * {@link #persist(IObservableSet)} to retrieve an object to be persist.
	 * <p>
	 * Commonly, the IObservable refer to a bean property from which the manage
	 * object is get. This implementation is using the
	 * {@link Util#findManagedObject(IObservable)}. Subclasses may override this
	 * function to return the appropriate object to be persist.
	 * 
	 * 
	 * @param target
	 *            the observable object
	 * @return a manage object or null if not found
	 */
	protected ManagedObject findManagedObject(IObservable target) {
		if (target instanceof IBeanObservable) {
			Object observed = ((IBeanObservable) target).getObserved();
			if (observed instanceof ManagedObject) {
				return (ManagedObject) observed;
			}
		}

		if (target instanceof IDecoratingObservable) {
			IObservable decorated = ((IDecoratingObservable) target)
					.getDecorated();
			ManagedObject object = findManagedObject(decorated);
			if (object != null)
				return object;
		}

		if (target instanceof IObserving) {
			Object observed = ((IObserving) target).getObserved();
			if (observed instanceof ManagedObject)
				return (ManagedObject) observed;
			if (observed instanceof IObservable)
				return findManagedObject((IObservable) observed);
		}

		return null;
	}

	/**
	 * Persist the modification to the managers. This function is called after
	 * the Set modification and will try to persists the modification done to
	 * the embedded {@link ManagedObject}.
	 * 
	 * @param observable
	 *            the observable being modify (should embedded a
	 *            {@link ManagedObject}
	 */
	protected IStatus persist(IObservable observable) {

		ManagedObject obj = findManagedObject(observable);
		if (obj == null) {
			return ValidationStatus.ok();
		}

		try {
			this.managers.updateAll(Arrays.asList(obj));
		} catch (ManagerException e) {
			e.printStackTrace();
			return ValidationStatus.error(
					"Failure to persist the ManagedObject", e); //$NON-NLS-1$
		}
		return ValidationStatus.ok();
	}

}
