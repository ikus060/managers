package com.patrikdufresne.managers;

import com.patrikdufresne.managers.DatabaseConfiguration;
import com.patrikdufresne.managers.DatabaseUrl;
import com.patrikdufresne.managers.IManager;
import com.patrikdufresne.managers.Managers;

public class MockManagers extends Managers {

	private MockEntityManager mockEntityManager;

	public MockManagers(DatabaseUrl url) {
		super(url);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void configure(DatabaseConfiguration cfg) {
		cfg.addAnnotatedClass(MockEntity.class);
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
