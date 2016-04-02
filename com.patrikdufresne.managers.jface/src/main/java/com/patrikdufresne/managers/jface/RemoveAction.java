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

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.IShellProvider;

import com.patrikdufresne.jface.dialogs.DetailMessageDialog;
import com.patrikdufresne.managers.ManagedObject;
import com.patrikdufresne.managers.ManagerException;
import com.patrikdufresne.managers.Managers;
import com.patrikdufresne.util.Localized;

/**
 * Action to remove object using managers.
 * <p>
 * The confirmation message displayed by this action can be personalized using the
 * {@link #setConfirmationMessage(String)} and {@link #setConfirmationShortDetail(String)}.
 * 
 * @author Patrik Dufresne
 * 
 */
public class RemoveAction extends Action {
    /**
     * Image id.
     */
    public static final String ICON_LIST_REMOVE_16 = "RemoveAction.iconListRemove16";

    /**
     * Used for localization.
     */
    private static final Localized L = Localized.load(RemoveAction.class);

    /**
     * Property name of this action object (value <code>"object"</code>). The object property define the entity to be
     * delete.
     */
    public static final String OBJECT = "object"; //$NON-NLS-1$

    /**
     * Property name of this action objects (value <code>"objects"</code>). The objects property define the entities to
     * be deleted.
     */
    public static final String OBJECTS = "objects"; //$NON-NLS-1$

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
     * The confirmation message to be displayed.
     */
    private String message;

    /**
     * The list of object to be removed.
     */
    private List<ManagedObject> objects;

    /**
     * Shell provider.
     */
    private IShellProvider shellProvider;

    /**
     * A short detail message.
     */
    private String shortDetail;

    /**
     * Create the action
     * 
     * @param provider
     *            the selection provider
     * @param shellProvider
     *            the shell provider
     */
    public RemoveAction(Managers managers, IShellProvider shellProvider) {
        if (managers == null || shellProvider == null) {
            throw new NullPointerException();
        }
        this.managers = managers;
        this.shellProvider = shellProvider;
        setText(L.get("RemoveAction.text")); //$NON-NLS-1$
        setToolTipText(L.get("RemoveAction.toolTipText")); //$NON-NLS-1$
        setImageDescriptor(JFaceResources.getImageRegistry().getDescriptor(ICON_LIST_REMOVE_16));
        setEnabled(canRun());
    }

    /**
     * Check if this action can be run.
     * 
     * @return
     */
    protected boolean canRun() {
        return this.objects != null && this.objects.size() > 0;
    }

    /**
     * Return the confirmation message value or null if unset.
     * 
     * @return the message
     */
    public String getConfirmationMessage() {
        return this.message;
    }

    /**
     * Return the short detail confirmation message.
     * 
     * @return
     */
    public String getConfirmationShortDetail() {
        return this.shortDetail;
    }

    public ManagedObject getObject() {
        if (this.objects == null || this.objects.size() == 0) {
            return null;
        }
        return this.objects.get(0);
    }

    public List<ManagedObject> getObjects() {
        return this.objects;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {

        if (this.objects == null || this.objects.size() == 0) {
            return;
        }

        // Confirmation
        DetailMessageDialog dlg = new DetailMessageDialog(this.shellProvider.getShell(), L.get("RemoveAction.text"), //$NON-NLS-1$
                null,
                this.message != null ? this.message : L.get("RemoveAction.confirmRemove.message"), //$NON-NLS-1$
                this.shortDetail != null ? this.shortDetail : L.get("RemoveAction.confirmRemove.shortDetail"), null, //$NON-NLS-1$
                MessageDialog.QUESTION,
                new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL },
                1, // no is the default
                null,
                false);
        if (dlg.open() != IDialogConstants.YES_ID) {
            return;
        }

        // Proceed with removal
        try {
            this.managers.removeAll(this.objects);
        } catch (ClassCastException e) {
            RemoveArchiveAction.handleException(shellProvider.getShell(), RemoveArchiveAction.REMOVE_ID, e);
        } catch (ManagerException e) {
            RemoveArchiveAction.handleException(shellProvider.getShell(), RemoveArchiveAction.REMOVE_ID, e);
        }

    }

    /**
     * Sets the confirmation to be displayed when the user run this action.
     * 
     * @param message
     *            the messagevalue or null to use the default message.
     */
    public void setConfirmationMessage(String message) {
        this.message = message;
    }

    /**
     * Sets the short detail confirmation message.
     * 
     * @param message
     */
    public void setConfirmationShortDetail(String message) {
        this.shortDetail = message;
    }

    /**
     * Sets the object to be deleted.
     * 
     * @param object
     *            the object
     */
    public void setObject(ManagedObject object) {
        firePropertyChange(OBJECT, getObject(), this.objects = (object == null ? null : Arrays.asList(object)));
        setEnabled(canRun());
    }

    public void setObjects(List<ManagedObject> objects) {
        firePropertyChange(OBJECTS, this.objects, this.objects = objects);
        setEnabled(canRun());
    }

}
