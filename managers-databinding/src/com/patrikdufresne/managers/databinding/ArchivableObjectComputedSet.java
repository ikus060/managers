/*
 * Copyright (c) 2011, Patrik Dufresne. All rights reserved.
 * Patrik Dufresne PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.patrikdufresne.managers.databinding;

import java.util.Map;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;

import com.patrikdufresne.managers.ArchivableObject;
import com.patrikdufresne.managers.Managers;

/**
 * This computed set is specialized to handle the archivable objects.
 * 
 * @author Patrik Dufresne
 * 
 */
public class ArchivableObjectComputedSet extends ManagedObjectComputedSet {

	/**
	 * Mode to compute only archived elements.
	 */
	public static final int MODE_ARCHIVED = 1;

	/**
	 * Mode to compute only un-archied elements.
	 */
	public static final int MODE_NOT_ARCHIVED = 2;

	/**
	 * This function check the value of the mode arguments.
	 * <p>
	 * If the value is not one of the MODE_* constant and exception is thrown.
	 * 
	 * @param mode
	 *            the mode value
	 */
	private static void checkMode(int mode) {
		if (mode != MODE_ARCHIVED && mode != MODE_NOT_ARCHIVED) {
			throw new IllegalArgumentException("unsupported mode value");
		}
	}

	private int mode;

	/**
	 * Create an observable computed set for the managers and element type
	 * specified.
	 * <p>
	 * This computed set will listen to default manager events and has no
	 * dependencies.
	 * 
	 * @param managers
	 *            the managers
	 * @param elementType
	 *            the element type
	 */
	public ArchivableObjectComputedSet(Managers managers,
			Class<? extends ArchivableObject> elementType, int mode) {
		super(managers, elementType);
		checkMode(mode);
		this.mode = mode;
	}

	/**
	 * Create an observable set for the managers and element type specified.
	 * <p>
	 * This computed set will listen to default manager events.
	 * 
	 * @param managers
	 *            the managers
	 * @param elementType
	 *            the element type
	 * @param dependencies
	 *            list of observable dependencies or null
	 */
	public ArchivableObjectComputedSet(Managers managers,
			Class<? extends ArchivableObject> elementType,
			IObservable[] dependencies, int mode) {
		super(managers, elementType, dependencies);
		checkMode(mode);
		this.mode = mode;
	}

	/**
	 * Create an observable set for the managers and element type specified.
	 * This computed set will listen to the <code>events</code> specified.
	 * 
	 * @param managers
	 *            the managers
	 * @param elementType
	 *            the element type
	 * @param events
	 *            the list of manager event to listen to
	 */
	public ArchivableObjectComputedSet(Managers managers,
			Class<? extends ArchivableObject> elementType,
			Map<Class, Integer> events, int mode) {
		super(managers, elementType, events);
		checkMode(mode);
		this.mode = mode;
	}

	/**
	 * Create a new observable list from a manager.
	 * 
	 * @param managers
	 *            the managers to use
	 * @param elementType
	 *            The element type of this observable
	 * @param events
	 *            the list of manager event to listen to
	 * @param dependencies
	 *            the list of observable dependencies.
	 */
	public ArchivableObjectComputedSet(Managers managers,
			Class<? extends ArchivableObject> elementType,
			Map<Class, Integer> events, IObservable[] dependencies, int mode) {
		super(managers, elementType, events, dependencies);
		checkMode(mode);
		this.mode = mode;
	}

	/**
	 * Create a new observable list from a manager.
	 * 
	 * @param realm
	 *            the realm
	 * @param managers
	 *            the managers to use
	 * @param elementType
	 *            The element type of this observable
	 * @param events
	 *            the list of manager event to listen to
	 * @param dependencies
	 *            the list of observable dependencies.
	 * @param mode
	 *            the working mode of this computed set. Should be one of the
	 *            MODE_* constant.
	 */
	public ArchivableObjectComputedSet(Realm realm, Managers managers,
			Class<? extends ArchivableObject> elementType,
			Map<Class, Integer> events, IObservable[] dependencies, int mode) {
		super(realm, managers, elementType, events, dependencies);
		checkMode(mode);
		this.mode = mode;
	}

	/**
	 * This implementation is used to handle the archive mode to either select
	 * or unselect archived elements.
	 */
	@Override
	protected boolean doSelect(Object element) {
		return element instanceof ArchivableObject
				&& ((ArchivableObject) element).getArchivedDate() == null ? this.mode == MODE_NOT_ARCHIVED
				: this.mode == MODE_ARCHIVED;
	}

}
