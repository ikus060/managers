package com.patrikdufresne.managers;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

public class MockEntityManager extends AbstractArchivableManager<MockEntity> {

	public MockEntityManager(Managers managers) {
		super(managers);
	}

	@Override
	public Class<MockEntity> objectClass() {
		return MockEntity.class;
	}

	/**
	 * This function is used to simulate an exception within a transaction.
	 */
	public void updateWithRuntimeException(
			final Collection<? extends MockEntity> s) throws ManagerException {
		checkObject(s);
		getManagers().exec(new Exec() {
			@Override
			public void run() throws ManagerException {

				// Run the update as expected
				preUpdateObjects(s);
				Iterator<? extends MockEntity> iter = s.iterator();
				while (iter.hasNext()) {
					MockEntity t = iter.next();
					preUpdateObject(t);
					t.setModificationDate(new Date());
					ManagerContext.getDefaultSession().update(t);
					postUpdateObject(t);
				}
				postUpdateObjects(s);

				// Before the transaction end, throw the exception
				throw new RuntimeException("the simulated exception");
			}
		});
	}

	/**
	 * This function is used to simulate an exception within a transaction.
	 */
	public void updateWithManagerException(
			final Collection<? extends MockEntity> s) throws ManagerException {
		checkObject(s);
		getManagers().exec(new Exec() {
			@Override
			public void run() throws ManagerException {

				// Run the update as expected
				preUpdateObjects(s);
				Iterator<? extends MockEntity> iter = s.iterator();
				while (iter.hasNext()) {
					MockEntity t = iter.next();
					preUpdateObject(t);
					t.setModificationDate(new Date());
					ManagerContext.getDefaultSession().update(t);
					postUpdateObject(t);
				}
				postUpdateObjects(s);

				// Before the transaction end, throw the exception
				throw new ManagerException("the simulated exception");
			}
		});
	}
	
	
	public void addWithManagerException(final Collection<? extends MockEntity> s) throws ManagerException {
		checkObject(s);
		getManagers().exec(new Exec() {
			@Override
			public void run() throws ManagerException {
				preAddObjects(s);
				Iterator<? extends MockEntity> iter = s.iterator();
				while (iter.hasNext()) {
					MockEntity t = iter.next();
					preAddObject(t);
					t.setCreationDate(new Date());
					t.setModificationDate(new Date());
					ManagerContext.getDefaultSession().save(t);
					postAddObject(t);
				}
				postAddObjects(s);
				
				// Before the transaction end, throw the exception
				throw new ManagerException("the simulated exception");
			}
		});
	}

}
