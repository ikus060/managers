package com.patrikdufresne.managers.databinding;

/*
 * Copyright (c) 2011, Patrik Dufresne. All rights reserved.
 * Patrik Dufresne PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
import org.eclipse.core.databinding.beans.IBeanObservable;
import org.eclipse.core.databinding.observable.IDecoratingObservable;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObserving;
import org.eclipse.core.databinding.observable.value.IObservableValue;

import com.patrikdufresne.managers.ManagedObject;

/**
 * Utility class to provide common functions to data binding managers.
 * 
 * @author Patrik Dufresne
 * 
 */
public class Util {

	/**
	 * Return the an instance of {@link ManagedObject} embedded in an
	 * observable.
	 * 
	 * @param target
	 *            the observable
	 * @return the managed object or null if not found
	 */
	public static ManagedObject findManagedObject(IObservable target) {
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

}
