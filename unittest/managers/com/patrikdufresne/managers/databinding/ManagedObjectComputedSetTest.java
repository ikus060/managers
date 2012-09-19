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
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.patrikdufresne.managers.AbstractManagerTest;
import com.patrikdufresne.managers.ManagerException;
import com.patrikdufresne.managers.MockEntity;

@RunWith(DatabindingClassRunner.class)
public class ManagedObjectComputedSetTest extends AbstractManagerTest {

	@Test
	public void AddObjectUpdatesSet() {

		IObservableSet set = new ManagedObjectComputedSet(getManagers(),
				MockEntity.class);
		assertTrue(set.isEmpty());

		MockEntity entity = addMockEntity(getManagers());

		assertEquals(1, set.size());
		assertEquals(entity, set.iterator().next());

	}

	@Test
	public void RemoveObjectUpdatesSet() {

		IObservableSet set = new ManagedObjectComputedSet(getManagers(),
				MockEntity.class);
		assertTrue(set.isEmpty());

		MockEntity entity1 = addMockEntity(getManagers());
		MockEntity entity2 = addMockEntity(getManagers());
		MockEntity entity3 = addMockEntity(getManagers());

		assertEquals(3, set.size());

		assertTrue(set.contains(entity1));
		assertTrue(set.contains(entity2));
		assertTrue(set.contains(entity3));

		try {
			getManagers().getMockEntityManager().remove(Arrays.asList(entity1));
		} catch (ManagerException e) {
			fail("Fail to remove the object", e);
			return;
		}

		assertEquals(2, set.size());

		assertTrue(set.contains(entity2));
		assertTrue(set.contains(entity3));

	}

	@Test
	public void AddRemoveUpdateObject_UpdatesFilteredSet()
			throws ManagerException {

		// Create entities
		MockEntity entity1 = addMockEntity(getManagers(), "1");
		MockEntity entity2 = addMockEntity(getManagers(), "2");
		MockEntity entity3 = addMockEntity(getManagers(), "3");
		MockEntity entity4 = addMockEntity(getManagers(), "");

		// Create the observable
		IObservableSet set = new ManagedObjectComputedSet(getManagers(),
				MockEntity.class) {

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
				return ((MockEntity) element).getName() != null
						&& !((MockEntity) element).getName().isEmpty();
			}

		};
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

		// Update entity
		entity1.setName("");
		entity4.setName("4");
		entity5.setName("5");
		getManagers().updateAll(Arrays.asList(entity1, entity4, entity5));
		assertEquals(5, set.size());
		assertTrue(set.contains(entity2));
		assertTrue(set.contains(entity3));
		assertTrue(set.contains(entity4));
		assertTrue(set.contains(entity5));
		assertTrue(set.contains(entity6));

		// Remove entity
		getManagers().removeAll(Arrays.asList(entity2, entity3));
		assertEquals(3, set.size());
		assertTrue(set.contains(entity4));
		assertTrue(set.contains(entity5));
		assertTrue(set.contains(entity6));

	}

	@Test
	public void UpdateDependencies_UpdatesFilteredSet() throws ManagerException {

		final WritableValue pattern = new WritableValue("[0-9]", String.class);

		// Create entities
		MockEntity entity1 = addMockEntity(getManagers(), "1");
		MockEntity entity2 = addMockEntity(getManagers(), "2");
		MockEntity entity3 = addMockEntity(getManagers(), "3");
		MockEntity entity4 = addMockEntity(getManagers(), "");
		MockEntity entity5 = addMockEntity(getManagers(), "a");
		MockEntity entity6 = addMockEntity(getManagers(), "b");

		// Create the observable
		IObservableSet set = new ManagedObjectComputedSet(getManagers(),
				MockEntity.class, new IObservable[] { pattern }) {

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

			protected String getPattern() {
				if (pattern.getValue() instanceof String) {
					return (String) pattern.getValue();
				}
				return null;
			}

			@Override
			protected boolean doSelect(Object element) {
				if (getPattern() == null)
					return false;
				return Pattern.compile(getPattern())
						.matcher(((MockEntity) element).getName()).find();
			}

		};
		assertEquals(3, set.size());
		assertTrue(set.contains(entity1));
		assertTrue(set.contains(entity2));
		assertTrue(set.contains(entity3));

		// Update dependency
		pattern.setValue("[a-z0-9]");
		assertEquals(5, set.size());
		assertTrue(set.contains(entity1));
		assertTrue(set.contains(entity2));
		assertTrue(set.contains(entity3));
		assertTrue(set.contains(entity5));
		assertTrue(set.contains(entity6));

	}

}
