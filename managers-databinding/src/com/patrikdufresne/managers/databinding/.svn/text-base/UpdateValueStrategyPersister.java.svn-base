/*
 * Copyright (c) 2011, Patrik Dufresne. All rights reserved.
 * Patrik Dufresne PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.patrikdufresne.managers.databinding;

import java.util.Arrays;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;

import com.patrikdufresne.managers.ManagedObject;
import com.patrikdufresne.managers.ManagerException;
import com.patrikdufresne.managers.Managers;

public class UpdateValueStrategyPersister extends UpdateValueStrategy {

	protected IStatus doSet(IObservableValue observableValue, Object value) {
		// Call the super method to sets the value
		IStatus status = super.doSet(observableValue, value);
		if (!status.isOK()) {
			return status;
		}
		// Persists the modification
		return persist();
	}

	private IObservableValue entity;

	private Managers managers;

	public UpdateValueStrategyPersister(Managers managers,
			IObservableValue entity) {
		super();
		this.managers = managers;
		this.entity = entity;
	}

	/**
	 * Return the managed object or null.
	 * 
	 * @return
	 */
	protected ManagedObject getEntity() {
		if (this.entity.getValue() instanceof ManagedObject) {
			return (ManagedObject) this.entity.getValue();
		}
		return null;
	}

	/**
	 * This function may be used by external class to manually persist the data.
	 */
	protected IStatus persist() {
		// Check if there is an entity to be persist.
		if (getEntity() == null) {
			return ValidationStatus.ok();
		}
		// Check current validation status. If the status is OK, save the
		// entity.
		// Iterator iter = this.dbc.getValidationStatusProviders().iterator();
		// while (iter.hasNext()) {
		// ValidationStatusProvider statusProvider = (ValidationStatusProvider)
		// iter
		// .next();
		// IStatus status = (IStatus) statusProvider.getValidationStatus()
		// .getValue();
		// if (!status.isOK()) {
		// return ValidationStatus.ok();
		// }
		// }

		try {
			this.managers.updateAll(Arrays.asList(getEntity()));
		} catch (ManagerException e) {
			// TODO The data binding context should be updated with a ERROR
			// status.
			e.printStackTrace();
			return ValidationStatus.error("FAIL TO PERSISTS", e);
		}
		return ValidationStatus.ok();
	}

}
