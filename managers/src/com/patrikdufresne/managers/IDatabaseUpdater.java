/*
 * Copyright (c) 2011, Patrik Dufresne. All rights reserved.
 * Patrik Dufresne PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.patrikdufresne.managers;

import org.hibernate.SessionFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;

public interface IDatabaseUpdater {

	public void runUpdate(SessionFactory factory, final Dialect dialect,
			DatabaseMetadata metadata);

	public boolean needUpdate(DatabaseMetadata metadata);

}
