package com.patrikdufresne.managers.databinding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.patrikdufresne.managers.AbstractManagerTestCase;
import com.patrikdufresne.managers.ManagerException;
import com.patrikdufresne.managers.MockEntity;

@RunWith(DatabindingClassRunner.class)
public class ManagedObjectComputedSetTest extends AbstractManagerTestCase {

	@Test
	public void AddObjectUpdatesSet() {

		IObservableSet set = new ManagedObjectComputedSet(getManagers()
				.getMockEntityManager());
		assertTrue(set.isEmpty());

		MockEntity entity = addMockEntity(getManagers());

		assertEquals(1, set.size());
		assertEquals(entity, set.iterator().next());

	}

	@Test
	public void RemoveObjectUpdatesSet() {

		IObservableSet set = new ManagedObjectComputedSet(getManagers()
				.getMockEntityManager());
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

}
