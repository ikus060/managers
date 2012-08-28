package com.patrikdufresne.managers;

/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Ashley Cambrell - bug 198904
 ******************************************************************************/

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

public abstract class AbstractManagerTest {
	private MockManagers managers;

	protected MockManagers getManagers() {
		return this.managers;
	}

	@Before
	public void setupDatabase() throws Exception {
		DatabaseUrl url = new DatabaseUrl("./unittest");
		url.localfile().delete();
		managers = new MockManagers(url);
	}

	@After
	public void closeDatabase() throws Exception {
		if (managers != null) {
			managers.getDatabaseUrl().localfile().delete();
			managers.dispose();
		}
	}

	/**
	 * Fail with exception display
	 * 
	 * @param message
	 * @param e
	 */
	public static void fail(String message, Exception e) {
		StringWriter writer = new StringWriter();
		PrintWriter pw = new PrintWriter(writer);
		e.printStackTrace(pw);
		pw.flush();
		Assert.fail(message + "\r\n" + writer.toString());
	}

	public static MockEntity addMockEntity(MockManagers managers) {
		MockEntity entity = new MockEntity();
		try {
			managers.addAll(Arrays.asList(entity));
		} catch (ManagerException e) {
			fail("Fail to create the entity", e);
			return null;
		}
		return entity;
	}
}
