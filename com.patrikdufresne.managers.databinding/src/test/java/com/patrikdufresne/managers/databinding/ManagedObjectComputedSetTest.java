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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.patrikdufresne.managers.AbstractManagerTest;
import com.patrikdufresne.managers.ManagerException;
import com.patrikdufresne.managers.MockEntity;

@RunWith(DatabindingClassRunner.class)
public class ManagedObjectComputedSetTest extends AbstractManagerTest {

    /**
     * Check if adding a listener without calling the get function will generate
     * events.
     */
    @Test
    public void addListener_ToFilteredSetWithoutCallingGet_ExpectEvents() {

        // Add entities
        MockEntity entity1 = addMockEntity(getManagers(), "a");
        MockEntity entity2 = addMockEntity(getManagers(), "b");
        MockEntity entity3 = addMockEntity(getManagers(), "");
        MockEntity entity4 = addMockEntity(getManagers(), "c");

        // Create the observable set
        IObservableSet set = new ManagedObjectComputedSet(getManagers(), MockEntity.class) {
            @Override
            protected Collection doList() throws ManagerException {
                List list = new ArrayList(super.doList());
                Iterator iter = list.iterator();
                while (iter.hasNext()) {
                    MockEntity obj = (MockEntity) iter.next();
                    if (obj.getName() == null || obj.getName().isEmpty()) {
                        iter.remove();
                    }
                }
                return list;
            }

            @Override
            protected boolean doSelect(Object element) {
                return ((MockEntity) element).getName() != null && !((MockEntity) element).getName().isEmpty();
            }
        };
        ChangeListenerCounter listener = new ChangeListenerCounter();
        set.addChangeListener(listener);
        set.addSetChangeListener(listener);

        // Add another entity should fire event
        MockEntity entity5 = addMockEntity(getManagers(), "");
        MockEntity entity6 = addMockEntity(getManagers(), "d");
        MockEntity entity7 = addMockEntity(getManagers(), "");
        assertEquals(1, listener.getSetChangeEvents().size());
        SetDiff diff = listener.getSetChangeEvents().get(0).diff;
        assertEquals(1, diff.getAdditions().size());
        assertEquals(0, diff.getRemovals().size());
        assertTrue(diff.getAdditions().contains(entity6));

    }

    /**
     * Check if adding a listener without calling the get function will generate
     * events.
     */
    @Test
    public void addListener_WithoutCallingGet_ExpectEvents() {

        // Add entities
        MockEntity entity1 = addMockEntity(getManagers());
        MockEntity entity2 = addMockEntity(getManagers());

        // Create the observable set
        IObservableSet set = new ManagedObjectComputedSet(getManagers(), MockEntity.class);
        ChangeListenerCounter listener = new ChangeListenerCounter();
        set.addChangeListener(listener);
        set.addSetChangeListener(listener);

        // Add another entity should fire event
        MockEntity entity3 = addMockEntity(getManagers());
        assertEquals(1, listener.getSetChangeEvents().size());
        SetDiff diff = listener.getSetChangeEvents().get(0).diff;
        assertEquals(1, diff.getAdditions().size());
        assertEquals(0, diff.getRemovals().size());
        assertTrue(diff.getAdditions().contains(entity3));

    }

    @Test
    public void addObject_UpdatesSet() {

        // Create the observable set
        ChangeListenerCounter listener = new ChangeListenerCounter();
        IObservableSet set = new ManagedObjectComputedSet(getManagers(), MockEntity.class);
        set.addChangeListener(listener);
        set.addSetChangeListener(listener);
        assertTrue(set.isEmpty());

        // Add entity
        MockEntity entity = addMockEntity(getManagers());

        // Check observable set content
        assertEquals(1, set.size());
        assertEquals(entity, set.iterator().next());

        // Check listener
        assertEquals(1, listener.getChangeEvents().size());
        assertEquals(1, listener.getSetChangeEvents().size());
        SetDiff diff = listener.getSetChangeEvents().get(0).diff;
        assertEquals(1, diff.getAdditions().size());
        assertEquals(0, diff.getRemovals().size());
        assertTrue(diff.getAdditions().contains(entity));

    }

    @Test
    public void addRemoveUpdateObject_WithFilteredSet_UpdateSet() throws ManagerException {

        // Create entities
        MockEntity entity1 = addMockEntity(getManagers(), "1");
        MockEntity entity2 = addMockEntity(getManagers(), "2");
        MockEntity entity3 = addMockEntity(getManagers(), "3");
        MockEntity entity4 = addMockEntity(getManagers(), "");

        // Create the observable set
        ChangeListenerCounter listener = new ChangeListenerCounter();
        IObservableSet set = new ManagedObjectComputedSet(getManagers(), MockEntity.class) {
            @Override
            protected Collection doList() throws ManagerException {
                List list = new ArrayList(super.doList());
                Iterator iter = list.iterator();
                while (iter.hasNext()) {
                    MockEntity obj = (MockEntity) iter.next();
                    if (obj.getName() == null || obj.getName().isEmpty()) {
                        iter.remove();
                    }
                }
                return list;
            }

            @Override
            protected boolean doSelect(Object element) {
                return ((MockEntity) element).getName() != null && !((MockEntity) element).getName().isEmpty();
            }
        };
        set.addChangeListener(listener);
        set.addSetChangeListener(listener);
        assertEquals(3, set.size());
        assertTrue(set.contains(entity1));
        assertTrue(set.contains(entity2));
        assertTrue(set.contains(entity3));

        // Add an entity
        MockEntity entity5 = addMockEntity(getManagers(), "");
        MockEntity entity6 = addMockEntity(getManagers(), "6");
        assertEquals(4, set.size());
        assertTrue(set.contains(entity1));
        assertTrue(set.contains(entity2));
        assertTrue(set.contains(entity3));
        assertTrue(set.contains(entity6));
        assertEquals(1, listener.getChangeEvents().size());
        assertEquals(1, listener.getSetChangeEvents().size());
        SetDiff diff1 = listener.getSetChangeEvents().get(0).diff;
        assertEquals(1, diff1.getAdditions().size());
        assertEquals(0, diff1.getRemovals().size());
        assertTrue(diff1.getAdditions().contains(entity6));
        listener.clear();

        // Update entity
        entity1.setName("");
        entity4.setName("4");
        entity5.setName("5");
        getManagers().updateAll(Arrays.asList(entity1, entity2, entity4, entity5));
        assertEquals(5, set.size());
        assertTrue(set.contains(entity2));
        assertTrue(set.contains(entity3));
        assertTrue(set.contains(entity4));
        assertTrue(set.contains(entity5));
        assertTrue(set.contains(entity6));
        assertEquals(1, listener.getChangeEvents().size());
        assertEquals(1, listener.getSetChangeEvents().size());
        SetDiff diff3 = listener.getSetChangeEvents().get(0).diff;
        assertEquals(2, diff3.getAdditions().size());
        assertEquals(1, diff3.getRemovals().size());
        assertTrue(diff3.getAdditions().contains(entity4));
        assertTrue(diff3.getAdditions().contains(entity5));
        assertTrue(diff3.getRemovals().contains(entity1));
        listener.clear();

        // Remove entities
        getManagers().removeAll(Arrays.asList(entity1, entity2, entity3));
        assertEquals(3, set.size());
        assertTrue(set.contains(entity4));
        assertTrue(set.contains(entity5));
        assertTrue(set.contains(entity6));
        assertEquals(1, listener.getChangeEvents().size());
        assertEquals(1, listener.getSetChangeEvents().size());
        SetDiff diff4 = listener.getSetChangeEvents().get(0).diff;
        assertEquals(0, diff4.getAdditions().size());
        assertEquals(2, diff4.getRemovals().size());
        assertTrue(diff4.getRemovals().contains(entity2));
        assertTrue(diff4.getRemovals().contains(entity3));

    }

    @Test
    public void removeObject_UpdatesSet() throws ManagerException {

        // Create observable set
        ChangeListenerCounter listener = new ChangeListenerCounter();
        IObservableSet set = new ManagedObjectComputedSet(getManagers(), MockEntity.class);
        set.addChangeListener(listener);
        set.addSetChangeListener(listener);
        assertTrue(set.isEmpty());

        // Add entities
        MockEntity entity1 = addMockEntity(getManagers());
        MockEntity entity2 = addMockEntity(getManagers());
        MockEntity entity3 = addMockEntity(getManagers());
        assertEquals(3, set.size());
        assertTrue(set.contains(entity1));
        assertTrue(set.contains(entity2));
        assertTrue(set.contains(entity3));
        listener.clear();

        // Remove entity
        getManagers().getMockEntityManager().remove(Arrays.asList(entity1));

        // Check observable set content
        assertEquals(2, set.size());
        assertTrue(set.contains(entity2));
        assertTrue(set.contains(entity3));

        // Check listener
        assertEquals(1, listener.getChangeEvents().size());
        assertEquals(1, listener.getSetChangeEvents().size());
        SetDiff diff = listener.getSetChangeEvents().get(0).diff;
        assertEquals(0, diff.getAdditions().size());
        assertEquals(1, diff.getRemovals().size());
        assertTrue(diff.getRemovals().contains(entity1));
    }

    @Test
    public void updateDependencies_WithFilteredSet_UpdatesSet() throws ManagerException {

        final WritableValue pattern = new WritableValue("[0-9]", String.class);

        // Create entities
        MockEntity entity1 = addMockEntity(getManagers(), "1");
        MockEntity entity2 = addMockEntity(getManagers(), "2");
        MockEntity entity3 = addMockEntity(getManagers(), "3");
        MockEntity entity4 = addMockEntity(getManagers(), "");
        MockEntity entity5 = addMockEntity(getManagers(), "a");
        MockEntity entity6 = addMockEntity(getManagers(), "b");

        // Create the observable
        ChangeListenerCounter listener = new ChangeListenerCounter();
        IObservableSet set = new ManagedObjectComputedSet(getManagers(), MockEntity.class, new IObservable[] { pattern }) {

            @Override
            protected Collection doList() throws ManagerException {
                List list = new ArrayList(super.doList());
                Iterator iter = list.iterator();
                while (iter.hasNext()) {
                    Object obj = iter.next();
                    if (!doSelect(obj)) {
                        iter.remove();
                    }
                }
                return list;
            }

            @Override
            protected boolean doSelect(Object element) {
                if (getPattern() == null) return false;
                return Pattern.compile(getPattern()).matcher(((MockEntity) element).getName()).find();
            }

            protected String getPattern() {
                if (pattern.getValue() instanceof String) {
                    return (String) pattern.getValue();
                }
                return null;
            }

        };
        set.addChangeListener(listener);
        set.addSetChangeListener(listener);
        assertEquals(3, set.size());
        assertTrue(set.contains(entity1));
        assertTrue(set.contains(entity2));
        assertTrue(set.contains(entity3));

        // Update dependency
        pattern.setValue("[a-z2-9]");
        assertEquals(4, set.size());
        assertTrue(set.contains(entity2));
        assertTrue(set.contains(entity3));
        assertTrue(set.contains(entity5));
        assertTrue(set.contains(entity6));
        assertEquals(1, listener.getChangeEvents().size());
        assertEquals(1, listener.getSetChangeEvents().size());
        SetDiff diff4 = listener.getSetChangeEvents().get(0).diff;
        assertEquals(2, diff4.getAdditions().size());
        assertEquals(1, diff4.getRemovals().size());
        assertTrue(diff4.getAdditions().contains(entity5));
        assertTrue(diff4.getAdditions().contains(entity6));
        assertTrue(diff4.getRemovals().contains(entity1));

    }

    @Test
    public void addRemoveUpdateObject_WithDependenciesSet_UpdateSet() throws ManagerException {

        final WritableValue pattern = new WritableValue("[0-9]", String.class);

        // Create entities
        MockEntity entity1 = addMockEntity(getManagers(), "1");
        MockEntity entity2 = addMockEntity(getManagers(), "2");
        MockEntity entity3 = addMockEntity(getManagers(), "3");
        MockEntity entity4 = addMockEntity(getManagers(), "");

        // Create the observable set
        ChangeListenerCounter listener = new ChangeListenerCounter();
        IObservableSet set = new ManagedObjectComputedSet(getManagers(), MockEntity.class, new IObservable[] { pattern }) {
            @Override
            protected Collection doList() throws ManagerException {
                List list = new ArrayList(super.doList());
                Iterator iter = list.iterator();
                while (iter.hasNext()) {
                    Object obj = iter.next();
                    if (!doSelect(obj)) {
                        iter.remove();
                    }
                }
                return list;
            }

            @Override
            protected boolean doSelect(Object element) {
                if (getPattern() == null) return false;
                return Pattern.compile(getPattern()).matcher(((MockEntity) element).getName()).find();
            }

            protected String getPattern() {
                if (pattern.getValue() instanceof String) {
                    return (String) pattern.getValue();
                }
                return null;
            }
        };
        set.addChangeListener(listener);
        set.addSetChangeListener(listener);
        assertEquals(3, set.size());
        assertTrue(set.contains(entity1));
        assertTrue(set.contains(entity2));
        assertTrue(set.contains(entity3));

        // Add an entity
        MockEntity entity5 = addMockEntity(getManagers(), "");
        MockEntity entity6 = addMockEntity(getManagers(), "6");
        assertEquals(4, set.size());
        assertTrue(set.contains(entity1));
        assertTrue(set.contains(entity2));
        assertTrue(set.contains(entity3));
        assertTrue(set.contains(entity6));
        assertEquals(1, listener.getChangeEvents().size());
        assertEquals(1, listener.getSetChangeEvents().size());
        SetDiff diff1 = listener.getSetChangeEvents().get(0).diff;
        assertEquals(1, diff1.getAdditions().size());
        assertEquals(0, diff1.getRemovals().size());
        assertTrue(diff1.getAdditions().contains(entity6));
        listener.clear();

        // Update entity
        entity1.setName("");
        entity4.setName("4");
        entity5.setName("5");
        getManagers().updateAll(Arrays.asList(entity1, entity2, entity4, entity5));
        assertEquals(5, set.size());
        assertTrue(set.contains(entity2));
        assertTrue(set.contains(entity3));
        assertTrue(set.contains(entity4));
        assertTrue(set.contains(entity5));
        assertTrue(set.contains(entity6));
        assertEquals(1, listener.getChangeEvents().size());
        assertEquals(1, listener.getSetChangeEvents().size());
        SetDiff diff3 = listener.getSetChangeEvents().get(0).diff;
        assertEquals(2, diff3.getAdditions().size());
        assertEquals(1, diff3.getRemovals().size());
        assertTrue(diff3.getAdditions().contains(entity4));
        assertTrue(diff3.getAdditions().contains(entity5));
        assertTrue(diff3.getRemovals().contains(entity1));
        listener.clear();

        // Remove entities
        getManagers().removeAll(Arrays.asList(entity1, entity2, entity3));
        assertEquals(3, set.size());
        assertTrue(set.contains(entity4));
        assertTrue(set.contains(entity5));
        assertTrue(set.contains(entity6));
        assertEquals(1, listener.getChangeEvents().size());
        assertEquals(1, listener.getSetChangeEvents().size());
        SetDiff diff4 = listener.getSetChangeEvents().get(0).diff;
        assertEquals(0, diff4.getAdditions().size());
        assertEquals(2, diff4.getRemovals().size());
        assertTrue(diff4.getRemovals().contains(entity2));
        assertTrue(diff4.getRemovals().contains(entity3));

    }

}
