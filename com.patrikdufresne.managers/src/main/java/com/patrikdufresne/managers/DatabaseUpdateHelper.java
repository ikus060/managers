/**
 * Copyright(C) 2013 Patrik Dufresne Service Logiciel <info@patrikdufresne.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.patrikdufresne.managers;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.jdbc.ReturningWork;
import org.hibernate.jdbc.Work;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;
import org.hibernate.tool.hbm2ddl.TableMetadata;

/**
 * Utility class to update the database
 * 
 * @author Patrik Dufresne
 * 
 */
public class DatabaseUpdateHelper {

    public static String alertTableAddColumn(Dialect dialect, String table, String column, int code, int length, int precision, int scale, String defaultValue) {
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
     *            The database {@link Dialect} to use.
     * @param table
     *            The table to alter
     * @param column
     *            The column to alter
     * @param code
     *            The {@link java.sql.Types} typecode
     * @param length
     *            The new datatype length value
     * @param precision
     *            The new datatype precision value
     * @param scale
     *            The new datatype scale value
     * @return The SQL command to alter the table's column
     */
    public static String alertTableAlterColumn(Dialect dialect, String table, String column, int code, int length, int precision, int scale) {
        if (table == null || table.isEmpty()) {
            throw new IllegalArgumentException("table should be define");
        }
        if (column == null || column.isEmpty()) {
            throw new IllegalArgumentException("column should be define");
        }
        StringBuffer buf = new StringBuffer();
        buf.append("ALTER TABLE "); //$NON-NLS-1$
        buf.append(table);
        buf.append(" ALTER COLUMN "); //$NON-NLS-1$
        buf.append(dialect.quote(column));
        buf.append(' ');
        buf.append(dialect.getTypeName(code, length, precision, scale));
        return buf.toString();
    }

    public static String alterTableAddConstraint(
            Dialect dialect,
            String table,
            String constraintName,
            String columnName,
            String refTableName,
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

    public static String alterTableDropConstraint(Dialect dialect, String table, String constraintName) {
        StringBuffer buf = new StringBuffer();
        buf.append("ALTER TABLE "); //$NON-NLS-1$
        buf.append(table);
        buf.append(" DROP CONSTRAINT "); //$NON-NLS-1$
        buf.append(constraintName);
        return buf.toString();
    }

    /**
     * Create the SQL to drop a column.
     * 
     * @param dialect
     *            the dialect
     * @param table
     *            the table name
     * @param columnName
     *            the column name to be drop
     * @return the resulting SQL
     */
    public static String alterTableDropColumn(Dialect dialect, String table, String columnName) {
        StringBuffer buf = new StringBuffer();
        buf.append("ALTER TABLE "); //$NON-NLS-1$
        buf.append(table);
        buf.append(" DROP COLUMN "); //$NON-NLS-1$
        buf.append(columnName);
        return buf.toString();
    }

    /**
     * Execute the given SQL command against the session factory
     * 
     * @param factory
     *            the session factory
     * @param script
     *            the SQL script.
     */
    public static void execute(SessionFactory factory, final List<String> script) {
        // Run the update script
        Session session = factory.openSession();
        try {
            session.doWork(new Work() {
                @Override
                public void execute(Connection connection) throws SQLException {
                    for (String sql : script) {
                        Statement stmt = connection.createStatement();
                        stmt.executeUpdate(sql);
                    }
                }
            });
        } finally {
            session.close();
        }
    }

    /**
     * Return the database catalog used by the sesion factory.
     * 
     * @param factory
     * @return the database catalog.
     */
    public static String getCatalog(SessionFactory factory) {
        return (String) ((SessionFactoryImpl) factory).getProperties().get(Environment.DEFAULT_CATALOG);

    }

    /**
     * Create a JDBC database metadata
     * 
     * @param factory
     *            The session factory
     * @param dialect
     *            the database dialect
     * @return the metadata
     */
    public static DatabaseMetadata getDatabaseMetadata(SessionFactory factory) {
        final Dialect dialect = getDialect(factory);
        // Run the update script
        return factory.openSession().doReturningWork(new ReturningWork<DatabaseMetadata>() {
            @Override
            public DatabaseMetadata execute(Connection connection) throws SQLException {
                return new DatabaseMetadata(connection, dialect);
            }
        });
    }

    /**
     * Small hack to get the dialect from the session factory
     * 
     * @param factory
     *            the factory
     * @return the dialect used by the session factory.
     */
    public static Dialect getDialect(SessionFactory factory) {

        ServiceRegistry serviceRegistry = ((SessionFactoryImpl) factory).getServiceRegistry();
        final JdbcServices jdbcServices = serviceRegistry.getService(JdbcServices.class);
        return jdbcServices.getDialect();

    }

    /**
     * Return the database schema used by the session factory.
     * 
     * @param factory
     * @return the schema
     */
    public static String getSchema(SessionFactory factory) {
        return (String) ((SessionFactoryImpl) factory).getProperties().get(Environment.DEFAULT_SCHEMA);
    }

    public static TableMetadata getTableMetadata(SessionFactory factory, final String table, final String schema, final String catalog) {

        final Dialect dialect = getDialect(factory);

        // Run the update script
        return factory.openSession().doReturningWork(new ReturningWork<TableMetadata>() {
            @Override
            public TableMetadata execute(Connection connection) throws SQLException {
                DatabaseMetadata meta = new DatabaseMetadata(connection, dialect);
                return meta.getTableMetadata(table, schema, catalog, false);
            }
        });
    }

    /**
     * Private constructor for utility class
     */
    private DatabaseUpdateHelper() {
        // Nothing to do
    }

}
