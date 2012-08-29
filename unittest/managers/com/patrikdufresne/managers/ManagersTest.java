package com.patrikdufresne.managers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;

public class ManagersTest extends AbstractManagerTest {

	/**
	 * Create a new entity object and add it to the database using the managers.
	 * 
	 * @return
	 * @throws ManagerException
	 */
	public MockEntity addEntity() throws ManagerException {
		MockEntity entity = new MockEntity();
		entity.setName("a");
		getManagers().getMockEntityManager().add(Arrays.asList(entity));
		return entity;
	}

	/**
	 * Test if the manager add the entity to the database.
	 * 
	 * @throws ManagerException
	 */
	@Test
	public void testAdd_withNewEntity_ExpectEvent() throws ManagerException {

		ManagerEventCounter counter = new ManagerEventCounter();
		getManagers().getMockEntityManager().addObserver(ManagerEvent.ADD,
				counter);

		MockEntity entity = new MockEntity();
		entity.setName("a");
		assertNull(entity.getCreationDate());
		assertNull(entity.getModificationDate());
		getManagers().getMockEntityManager().add(Arrays.asList(entity));

		assertEquals("Wrong number of entities", 1, getManagers()
				.getMockEntityManager().list().size());

		assertEquals("Wrong number of event", 1, counter.size());

		// Check value of creation date and modification date
		assertNotNull(entity.getCreationDate());
		assertNotNull(entity.getModificationDate());
		assertEquals((new Date()).getTime(),
				entity.getCreationDate().getTime(), 60000);
		assertEquals((new Date()).getTime(), entity.getModificationDate()
				.getTime(), 60000);

	}

	/**
	 * Test if the manager generate an event when an entity is updated. Also
	 * check if other reference of the same object is updated.
	 * 
	 * @throws ManagerException
	 */
	@Test
	public void testUpdate_withEntity_ExpectEventAndReferenceUpdate()
			throws ManagerException {

		// Add entity
		MockEntity entity = new MockEntity();
		entity.setName("a");
		getManagers().getMockEntityManager().add(Arrays.asList(entity));
		assertEquals("Wrong number of entities", 1, getManagers()
				.getMockEntityManager().list().size());

		// Attach update listener
		ManagerEventCounter counter = new ManagerEventCounter();
		getManagers().getMockEntityManager().addObserver(ManagerEvent.UPDATE,
				counter);

		// Get new reference to the same entity
		MockEntity entity2 = getManagers().getMockEntityManager().list().get(0);
		assertNotNull(entity2);

		// Update first reference
		entity.setName("b");
		getManagers().updateAll(Arrays.asList(entity));

		// Check number of events
		assertEquals("Wrong number of event.", 1, counter.size());

		// Check entities value
		assertEquals("b", entity.getName());
		assertEquals("b", entity2.getName());

		// Check value of creation date and modification date
		assertNotNull(entity.getModificationDate());
		assertEquals((new Date()).getTime(), entity.getModificationDate()
				.getTime(), 60000);
		assertEquals(entity.getModificationDate(), entity.getModificationDate());

	}

	/**
	 * Test if the manager generate an event when an entity is removed.
	 * 
	 * @throws ManagerException
	 */
	@Test
	public void testRemove_withEntity_ExpectEvent() throws ManagerException {

		// Add entity
		MockEntity entity = new MockEntity();
		entity.setName("a");
		getManagers().getMockEntityManager().add(Arrays.asList(entity));
		assertEquals("Wrong number of entities", 1, getManagers()
				.getMockEntityManager().list().size());

		// Attach remove listener
		ManagerEventCounter counter = new ManagerEventCounter();
		getManagers().getMockEntityManager().addObserver(ManagerEvent.REMOVE,
				counter);

		// Remove entity
		getManagers().removeAll(Arrays.asList(entity));

		assertEquals("Wrong number of entities", 0, getManagers()
				.getMockEntityManager().list().size());

		assertEquals("Wrong number of event.", 1, counter.size());

	}

	@Test
	public void testObjectIdentity() throws ManagerException {

		// Add entity
		MockEntity entity = addEntity();

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
		entity.setName("1");
		try {
			getManagers().getMockEntityManager().update(Arrays.asList(entity));
		} catch (ManagerException e) {
			fail("Fail to update the entity.", e);
			return;
		}

		assertEquals("Properties not copied.", entity.getName(),
				entity2.getName());

	}

}
