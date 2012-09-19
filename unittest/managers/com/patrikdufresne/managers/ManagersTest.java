package com.patrikdufresne.managers;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.TransactionException;
import org.junit.Assert;
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

	/**
	 * Test the behavior of the managers when an exception occurred within the
	 * transaction. The expected behavior is a session rollback and close.
	 * 
	 * @throws ManagerException
	 */
	@Test
	public void testUpdate_WithRuntimeException_SessionClosed()
			throws ManagerException {

		// Add entity
		MockEntity entity = new MockEntity();
		entity.setName("a");
		getManagers().getMockEntityManager().add(Arrays.asList(entity));
		List<MockEntity> list = getManagers().getMockEntityManager().list();
		assertEquals("Wrong number of entities", 1, list.size());
		MockEntity sameEntity = list.get(0);

		// Update with exception
		entity.setName("b");
		try {
			getManagers().getMockEntityManager().updateWithRuntimeException(
					Arrays.asList(entity));
			Assert.fail("An exception should be trown");
		} catch (ManagerException e) {
			// Nothing to do, this is expected
			Assert.assertTrue(e.getCause() instanceof RuntimeException);
		} catch (Throwable e) {
			Assert.fail("Any exception, should be rethrown into a ManagerException.");
		}

		// Make sure other instance of the same entity is not updated
		assertEquals("a", sameEntity.getName());

		// Then run the update again without exception.
		getManagers().getMockEntityManager().update(Arrays.asList(entity));

		// Make sure other instance are updated
		assertEquals("b", sameEntity.getName());

	}

	/**
	 * Test the behavior when a ManagerException is raised within a transaction.
	 * The session should be rollback and close.
	 * 
	 * @throws ManagerException
	 */
	@Test
	public void testUpdate_WithManagerException_SessionClosed()
			throws ManagerException {

		// Add entity
		MockEntity entity = new MockEntity();
		entity.setName("a");
		getManagers().getMockEntityManager().add(Arrays.asList(entity));
		List<MockEntity> list = getManagers().getMockEntityManager().list();
		assertEquals("Wrong number of entities", 1, list.size());
		MockEntity sameEntity = list.get(0);

		// Update with exception
		entity.setName("b");
		try {
			getManagers().getMockEntityManager().updateWithManagerException(
					Arrays.asList(entity));
			Assert.fail("An exception should be trown");
		} catch (ManagerException e) {
			// Nothing to do, this is expected
			Assert.assertTrue(e.getCause() instanceof ManagerException);
		} catch (Throwable e) {
			Assert.fail("Any exception, should be re-thrown into a ManagerException.");
		}

		// Make sure other instance of the same entity is not updated
		assertEquals("a", sameEntity.getName());

		// Then run the update again without exception.
		getManagers().getMockEntityManager().update(Arrays.asList(entity));

	}

	/**
	 * Check if an exception thrown within a different thread cause any problem
	 * in the main thread.
	 * <p>
	 * In this test, an insert statement is run in a thread so the table is
	 * locked. When the exception is thrown, the lock should be release so the
	 * main thread is able to update the table.
	 * 
	 * @throws ManagerException
	 * @throws InterruptedException
	 */
	@Test
	public void testUpdate_WithManagerExceptionInThread_SessionClosed()
			throws ManagerException, InterruptedException {

		// Add entity
		final MockEntity entity1 = new MockEntity();
		entity1.setName("a");
		final MockEntity entity2 = new MockEntity();
		entity2.setName("b");
		getManagers().getMockEntityManager().add(
				Arrays.asList(entity1, entity2));
		assertEquals("Wrong number of entities", 2, getManagers()
				.getMockEntityManager().list().size());

		Thread thread;
		(thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					getManagers().getMockEntityManager()
							.addWithManagerException(Arrays.asList(entity2));
					Assert.fail("An exception should be trown");
				} catch (ManagerException e) {
					// Nothing to do, this is expected
				} catch (Throwable e) {
					// Assert.fail("Any exception, should be rethrown into a ManagerException.");
				}
			}
		})).start();

		thread.join();

		// Then run the update again without exception.
		getManagers().getMockEntityManager().update(Arrays.asList(entity1));

	}

	@Test
	public void testFailToBeginTransaction_SessionClosed()
			throws ManagerException, InterruptedException {

		// Add entity
		final MockEntity entity1 = new MockEntity();
		entity1.setName("a");

		// Open a transaction, so an exception will be raised when opening a
		// second transaction.
		Session session = getManagers().getSessionFactory().getCurrentSession();
		session.beginTransaction();

		try {
			getManagers().getMockEntityManager().add(Arrays.asList(entity1));
			Assert.fail("Exception expected");
		} catch (ManagerException e) {
			Assert.assertTrue(e.getCause() instanceof TransactionException);
		}

		getManagers().getMockEntityManager().add(Arrays.asList(entity1));

	}

}
