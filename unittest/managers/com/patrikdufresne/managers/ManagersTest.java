package com.patrikdufresne.managers;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.patrikdufresne.managers.ManagerException;

public class ManagersTest extends AbstractManagerTestCase {

	@Test
	public void ObjectIdentity() {

		// Add entity
		MockEntity entity = new MockEntity();
		entity.setString("a");
		try {
			getManagers().getMockEntityManager().add(Arrays.asList(entity));
		} catch (ManagerException e) {
			fail("Fail to add entity.", e);
			return;
		}

		// Get another instance of the entity
		List<MockEntity> list;
		try {
			list = getManagers().getMockEntityManager().list();
		} catch (ManagerException e) {
			fail("Fail to list the entities", e);
			return;
		}
		assertEquals("Wrong number of entity.", 1, list.size());
		MockEntity entity2 = list.get(0);
		assertFalse(entity == entity2);

		// Update the original entity.
		entity.setString("1");
		try {
			getManagers().getMockEntityManager().update(Arrays.asList(entity));
		} catch (ManagerException e) {
			fail("Fail to update the entity.", e);
			return;
		}

		assertEquals("Properties not copied.", entity.getString(),
				entity2.getString());

	}

}
