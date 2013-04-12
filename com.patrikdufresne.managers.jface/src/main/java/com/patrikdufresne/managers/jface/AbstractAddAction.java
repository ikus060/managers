/*
 * Copyright (c) 2011, Patrik Dufresne. All rights reserved.
 * Patrik Dufresne PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.patrikdufresne.managers.jface;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Display;

import com.patrikdufresne.jface.dialogs.DetailMessageDialog;
import com.patrikdufresne.managers.ManagedObject;
import com.patrikdufresne.managers.ManagerException;
import com.patrikdufresne.managers.Managers;
import com.patrikdufresne.util.Localized;

/**
 * Action to add object using managers.
 * 
 * @author Patrik Dufresne
 * 
 */
public abstract class AbstractAddAction extends Action {

	/**
	 * Image id.
	 */
	public static final String ICON_LIST_ADD_16 = "AbstractAddAction.iconListAdd16";

	static {
		ImageRegistry imageRegistry = JFaceResources.getImageRegistry();

		// Define the images used in the standard decorations.
		imageRegistry.put(ICON_LIST_ADD_16, ImageDescriptor.createFromFile(
				RemoveAction.class, "images/list-add-16.png"));//$NON-NLS-1$
	}

	/**
	 * Used for localization.
	 */
	private static final Localized L = Localized.load(AbstractAddAction.class);

	/**
	 * Managers to used to add the element.
	 */
	private Managers managers;

	/**
	 * Selection provider.
	 */
	private ISelectionProvider selectionProvider;
	/**
	 * Shell provider.
	 */
	private IShellProvider shellProvider;

	/**
	 * Create a new Add object action.
	 * 
	 * @param provider
	 *            the selection provider
	 * @param shellProvider
	 *            the shell provider use to display message box
	 * @param factory
	 *            the factory
	 */
	public AbstractAddAction(Managers managers, IShellProvider shellProvider) {
		this(managers, shellProvider, null);
	}

	/**
	 * Create a new Add object action.
	 * 
	 * @param provider
	 *            the selection provider
	 * @param shellProvider
	 *            the shell provider use to display message box
	 * @param factory
	 *            the factory
	 */
	public AbstractAddAction(Managers managers, IShellProvider shellProvider,
			ISelectionProvider selectionProvider) {
		super(null);
		if (managers == null || shellProvider == null) {
			throw new NullPointerException();
		}
		this.managers = managers;
		this.shellProvider = shellProvider;
		this.selectionProvider = selectionProvider;
		setText(L.get("AbstractAddAction.text"));
		setImageDescriptor(JFaceResources.getImageRegistry().getDescriptor(
				ICON_LIST_ADD_16));
		setEnabled(canCreateObject());
	}

	/**
	 * Check if it's possible to create the object using the given selection
	 * context.
	 * 
	 * @param selection
	 *            the selection context
	 * @return True if it's possible to create an object
	 */
	protected abstract boolean canCreateObject();

	/**
	 * Create the object.
	 * 
	 * @param selection
	 *            the selection context.
	 * @return the new object to add using a manager.
	 */
	protected abstract List<? extends ManagedObject> createObjects()
			throws ManagerException;

	/**
	 * Returns the managers used by this action to create the object.
	 * 
	 * @return
	 */
	public Managers getManagers() {
		return this.managers;
	}

	/**
	 * Return the selection provider or null if not set.
	 * 
	 * @return
	 */
	public ISelectionProvider getSelectionProvider() {
		return this.selectionProvider;
	}

	/**
	 * Return the shell provider given in the constructor.
	 * 
	 * @return
	 */
	public IShellProvider getShellProvider() {
		return this.shellProvider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {

		if (!canCreateObject()) {
			DetailMessageDialog.openWarning(this.shellProvider.getShell(),
					getText(), L.get("AbstractAddAction.cantCreateObject"));
			return;
		}

		// An error occurred
		final List<? extends ManagedObject> list;
		try {
			list = createObjects();
		} catch (ManagerException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			DetailMessageDialog.openDetailWarning(
					this.shellProvider.getShell(), getText(),
					L.get("AbstractAddAction.cantCreateObject"),
					L.get("AbstractAddAction.errorOccurred"), sw.toString());
			return;
		}
		if (list == null || list.isEmpty()) {
			DetailMessageDialog.openWarning(this.shellProvider.getShell(),
					getText(), L.get("AbstractAddAction.cantCreateObject"));
			return;
		}

		// Add objects
		try {
			this.managers.addAll(list);
		} catch (ManagerException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			DetailMessageDialog.openDetailWarning(
					this.shellProvider.getShell(), getText(),
					L.get("AbstractAddAction.cantCreateObject"),
					L.get("AbstractAddAction.errorOccurred"), sw.toString());
		}

		// Select object in the viewer
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				selectObjects(list);
			}
		});

	}

	/**
	 * Called to select into the selection provider if define.
	 * 
	 * @param list
	 */
	protected void selectObjects(Collection<? extends ManagedObject> list) {
		if (getSelectionProvider() instanceof StructuredViewer) {
			((StructuredViewer) getSelectionProvider()).setSelection(
					new StructuredSelection(list.toArray()), true);
		} else {
			getSelectionProvider().setSelection(
					new StructuredSelection(list.toArray()));
		}
	}

}
