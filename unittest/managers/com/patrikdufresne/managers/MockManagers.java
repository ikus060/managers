package com.patrikdufresne.managers;

import org.hibernate.cfg.Configuration;

import com.patrikdufresne.managers.DatabaseUrl;
import com.patrikdufresne.managers.IManager;
import com.patrikdufresne.managers.Managers;

/**
 * Managers for unit test
 * 
 * @author Patrik Dufresne
 * 
 */
public class MockManagers extends Managers {

	/**
	 * The entity manager
	 */
	private MockEntityManager mockEntityManager;

	/**
	 * Default constructor.
	 * 
	 * @param url
	 *            the database url
	 * @throws ManagerException
	 */
	public MockManagers(DatabaseUrl url) throws ManagerException {
		super(url);
	}

	/**
	 * This implementation add only one annotated class.
	 */
	@Override
	protected void configure(Configuration config) {
		super.configure(config);
		config.addAnnotatedClass(MockEntity.class);
	}

	public MockEntityManager getMockEntityManager() {
		if (mockEntityManager == null) {
			mockEntityManager = new MockEntityManager(this);
		}
		return mockEntityManager;
	}

	@Override
	public IManager getManagerForClass(Class clazz) {
		if (clazz.equals(MockEntity.class)) {
			return getMockEntityManager();
		}
		return null;

	}

}
