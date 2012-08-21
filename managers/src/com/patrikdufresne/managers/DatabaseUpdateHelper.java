/*
 * Copyright (c) 2011, Patrik Dufresne. All rights reserved.
 * Patrik Dufresne PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.patrikdufresne.managers;

import org.hibernate.dialect.Dialect;

public class DatabaseUpdateHelper {

	public static String alertTableAddColumn(Dialect dialect, String table,
			String column, int code, int length, int precision, int scale,
			String defaultValue) {
		StringBuffer buf = new StringBuffer();
		buf.append("ALTER TABLE "); //$NON-NLS-1$
		buf.append(table);
		buf.append(" ADD "); //$NON-NLS-1$
		buf.append(dialect.quote(column));
		buf.append(' ');
		buf.append(dialect.getTypeName(code, length, precision, scale));
		// Add default if set
		if (defaultValue != null) {
			buf.append(" DEFAULT "); //$NON-NLS-1$
			buf.append(defaultValue);
		}
		return buf.toString();
	}

	/**
	 * Create an alter table statement to change the column type.
	 * 
	 * @param dialect
	 * @param table
	 * @param column
	 * @param code
	 * @param length
	 * @param precision
	 * @param scale
	 * @return
	 */
	public static String alertTableAlterColumn(Dialect dialect, String table,
			String column, int code, int length, int precision, int scale) {
		StringBuffer buf = new StringBuffer();
		buf.append("ALTER TABLE "); //$NON-NLS-1$
		buf.append(table);
		buf.append(" ALTER COLUMN "); //$NON-NLS-1$
		buf.append(dialect.quote(column));
		buf.append(' ');
		buf.append(dialect.getTypeName(code, length, precision, scale));
		return buf.toString();
	}

	public static String alterTableDropConstraint(Dialect dialect,
			String table, String constraintName) {
		StringBuffer buf = new StringBuffer();
		buf.append("ALTER TABLE "); //$NON-NLS-1$
		buf.append(table);
		buf.append(" DROP CONSTRAINT "); //$NON-NLS-1$
		buf.append(constraintName);
		return buf.toString();
	}

	public static String alterTableAddConstraint(Dialect dialect, String table,
			String constraintName, String columnName, String refTableName,
			String refColumnName) {
		StringBuffer buf = new StringBuffer();
		buf.append("ALTER TABLE "); //$NON-NLS-1$
		buf.append(table);
		buf.append(" ADD CONSTRAINT "); //$NON-NLS-1$
		buf.append(constraintName);
		buf.append(" FOREIGN KEY ("); //$NON-NLS-1$
		buf.append(columnName);
		buf.append(") REFERENCES "); //$NON-NLS-1$
		buf.append(refTableName);
		buf.append("("); //$NON-NLS-1$
		buf.append(refColumnName);
		buf.append(") NOCHECK"); //$NON-NLS-1$
		return buf.toString();
	
		// ALTER TABLE PUBLIC.PRODUCTIONEVENT ADD CONSTRAINT
		// PUBLIC.FK145FD541BD74184A FOREIGN KEY(PLANIF_ID) INDEX
		// PUBLIC.FK145FD541BD74184A_INDEX_9 REFERENCES PUBLIC.PLANIF(ID)
		// NOCHECK
	}

}
