/**
 * Copyright(C) 2013 Patrik Dufresne Service Logiciel <info@patrikdufresne.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.patrikdufresne.managers.jface;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.IShellProvider;

import com.patrikdufresne.jface.dialogs.DetailMessageDialog;
import com.patrikdufresne.managers.ArchivableObject;
import com.patrikdufresne.managers.ManagedObject;
import com.patrikdufresne.managers.ManagerException;
import com.patrikdufresne.managers.Managers;
import com.patrikdufresne.util.Localized;

/**
 * Action to remove and/or archive Managed object.
 * 
 * @author Patrik Dufresne
 * 
 */
public class RemoveArchiveAction extends Action {
    /**
     * The archive button id.
     * 
     * @See MessageDialogWithToggle
     */
    private static final int ARCHIVE_ID = IDialogConstants.INTERNAL_ID + 1;

    /**
     * Image id.
     */
    public static final String ICON_LIST_REMOVE_16 = "RemoveArchiveAction.iconListRemove16";

    /**
     * Used for localized.
     */
    private static final Localized L = Localized.load(RemoveArchiveAction.class);

    /**
     * Property name of this action objects (value <code>"objects"</code>). The
     * objects property define the entities to be deleted.
     */
    public static final String OBJECTS = "objects"; //$NON-NLS-1$

    /**
     * The remove button id.
     * 
     * @See MessageDialogWithToggle
     */
    private static final int REMOVE_ID = IDialogConstants.INTERNAL_ID;

    static {
        ImageRegistry imageRegistry = JFaceResources.getImageRegistry();

        // Define the images used in the standard decorations.
        imageRegistry.put(ICON_LIST_REMOVE_16, ImageDescriptor.createFromFile(RemoveAction.class, "images/list-remove-16.png"));//$NON-NLS-1$
    }

    /**
     * Managers used to remove the objects.
     */
    private Managers managers;

    /**
     * The list of object to be removed.
     */
    private List<ManagedObject> objects;

    /**
     * Shell provider.
     */
    private IShellProvider shellProvider;

    /**
     * Create the action
     * 
     * @param provider
     *            the selection provider
     * @param shellProvider
     *            the shell provider
     */
    public RemoveArchiveAction(Managers managers, IShellProvider shellProvider) {
        if (managers == null || shellProvider == null) {
            throw new NullPointerException();
        }
        this.managers = managers;
        this.shellProvider = shellProvider;
        setText(L.get("RemoveArchiveAction.text")); //$NON-NLS-1$
        setToolTipText(L.get("RemoveArchiveAction.toolTipText")); //$NON-NLS-1$
        setImageDescriptor(JFaceResources.getImageRegistry().getDescriptor(ICON_LIST_REMOVE_16));
        setEnabled(canRun());
    }

    /**
     * Check if this action can be run.
     * <p>
     * This implementation check if the objects list is set and if it contains
     * archivable objects.
     * 
     * @return True if the action can be run.
     */
    protected boolean canRun() {
        if (this.objects == null || this.objects.size() == 0) {
            return false;
        }
        Iterator<ManagedObject> iter = this.objects.iterator();
        while (iter.hasNext()) {
            ManagedObject obj = iter.next();
            if (!(obj instanceof ArchivableObject)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return an unmodifiable collection of object to be removed by this action.
     * 
     * @return
     */
    public List<ManagedObject> getObjects() {
        return this.objects;
    }

    /**
     * This function is used to display an error message to the user.
     * 
     * @param e
     *            the exception.
     */
    private void handleException(int operation, ManagerException e) {

        String message = operation == REMOVE_ID ? L.get("RemoveArchiveAction.cantRemoveObject") //$NON-NLS-1$
                : L.get("RemoveArchiveAction.cantArchiveObject"); //$NON-NLS-1$
        String shortDetail = L.get("RemoveArchiveAction.unknownException"); //$NON-NLS-1$

        // Check the cause to provide a better error message
        if (e.isConstraintViolationException()) {
            shortDetail = L.get("RemoveArchiveAction.constraintViolationException"); //$NON-NLS-1$
        }

        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        DetailMessageDialog.openDetailWarning(this.shellProvider.getShell(), L.get("RemoveArchiveAction.exception.title"), //$NON-NLS-1$
                message,
                shortDetail,
                sw.toString());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {

        if (!canRun()) {
            return;
        }

        String[] buttons = new String[] { L.get("RemoveArchiveAction.removeLabel"), //$NON-NLS-1$
                L.get("RemoveArchiveAction.archiveLabel"), //$NON-NLS-1$
                IDialogConstants.CANCEL_LABEL };

        // Prompt the user to confirm the remove or the archive
        DetailMessageDialog dlg = new DetailMessageDialog(this.shellProvider.getShell(), L.get("RemoveArchiveAction.confirmMessage.title"), //$NON-NLS-1$
                null,
                L.get("RemoveArchiveAction.confirmMessage.message"), //$NON-NLS-1$
                L.get("RemoveArchiveAction.confirmMessage.shortDetail"), //$NON-NLS-1$
                null,
                MessageDialog.QUESTION,
                buttons,
                2,
                null,
                false);

        int operation = dlg.open();
        if (operation != REMOVE_ID && operation != ARCHIVE_ID) {
            return;
        }

        try {
            switch (operation) {
            case REMOVE_ID:
                this.managers.removeAll(this.objects);
                break;
            case ARCHIVE_ID:
                this.managers.archiveAll(this.objects);
                break;
            }
        } catch (ManagerException e) {

            handleException(operation, e);

        }
        // TODO release the object array
    }

    /**
     * Sets the objects to be remove or archived.
     * 
     * @param objects
     *            the object collection or null to unset.
     */
    public void setObjects(List<ManagedObject> objects) {
        firePropertyChange(OBJECTS, getObjects(), this.objects = (objects == null || objects.size() == 0 ? null : Collections
                .unmodifiableList(new ArrayList<ManagedObject>(objects))));
        setEnabled(canRun());
    }

}
