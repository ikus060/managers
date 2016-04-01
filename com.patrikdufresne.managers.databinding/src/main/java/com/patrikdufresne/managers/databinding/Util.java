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
package com.patrikdufresne.managers.databinding;

/*
 * Copyright (c) 2011, Patrik Dufresne. All rights reserved.
 * Patrik Dufresne PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
import java.lang.reflect.Field;

import org.eclipse.core.databinding.beans.IBeanObservable;
import org.eclipse.core.databinding.observable.IDecoratingObservable;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObserving;
import org.eclipse.core.internal.databinding.observable.masterdetail.DetailObservableValue;

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
            IObservable decorated = ((IDecoratingObservable) target).getDecorated();
            ManagedObject object = findManagedObject(decorated);
            if (object != null) return object;
        }

        if (target instanceof IObserving) {
            Object observed = ((IObserving) target).getObserved();
            if (observed instanceof ManagedObject) return (ManagedObject) observed;
            if (observed instanceof IObservable) return findManagedObject((IObservable) observed);
        }

        if (target instanceof DetailObservableValue) {
            try {
                Field f = DetailObservableValue.class.getDeclaredField("outerObservableValue");
                f.setAccessible(true);
                IObservable observable = (IObservable) f.get(target);
                return findManagedObject(observable);
            } catch (Exception e) {
                // Swallow exception.
            }
        }

        return null;
    }

}
