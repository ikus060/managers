package com.patrikdufresne.managers;

import com.patrikdufresne.managers.AbstractManager;
import com.patrikdufresne.managers.Managers;

public class MockEntityManager extends AbstractManager<MockEntity> {

	public MockEntityManager(Managers managers) {
		super(managers);
	}

	@Override
	public Class<MockEntity> objectClass() {
		return MockEntity.class;
	}

}
