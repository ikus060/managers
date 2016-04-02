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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.exception.GenericJDBCException;
import org.hibernate.tool.hbm2ddl.ColumnMetadata;
import org.hibernate.tool.hbm2ddl.TableMetadata;
import org.junit.Test;

/**
 * Tests the update and validate function of the Managers
 * 
 * @author Patrik Dufresne
 * 
 */
public class ManagersUpdateValidateTest {

    /**
     * Check if the same database can be shared by multiple managers.
     * 
     * @throws MalformedURLException
     * @throws ManagerException
     */
    @Test
    public void testWithInMemory() throws MalformedURLException, ManagerException {
        // Create a database with auto-server mode
        MockManagers m1 = new MockManagers(H2DBConfigurations.create("jdbc:h2:mem:InMemory", true, false));
        m1.addAll(Arrays.asList(new MockEntity()));

        // Open connection to the same database
        MockManagers m2 = new MockManagers(H2DBConfigurations.create("jdbc:h2:mem:InMemory", true, false));
        assertEquals(0, m2.getMockEntityManager().list().size());

        m2.dispose();
        m1.dispose();
    }

    /**
     * Check if the same database can be shared by multiple managers.
     * 
     * @throws MalformedURLException
     * @throws ManagerException
     */
    @Test
    public void testWithAutoServerMode() throws MalformedURLException, ManagerException {
        // Create a database with auto-server mode
        MockManagers serverManagers = new MockManagers(H2DBConfigurations.create("unittestAutoServerMode", true, true));
        serverManagers.addAll(Arrays.asList(new MockEntity()));

        // Open connection to the same database
        MockManagers managers1 = new MockManagers(H2DBConfigurations.create("unittestAutoServerMode", false, false));
        assertEquals(1, managers1.getMockEntityManager().list().size());
        MockManagers managers2 = new MockManagers(H2DBConfigurations.create("unittestAutoServerMode", false, false));
        assertEquals(1, managers2.getMockEntityManager().list().size());
        MockManagers managers3 = new MockManagers(H2DBConfigurations.create("unittestAutoServerMode", false, false));
        assertEquals(1, managers3.getMockEntityManager().list().size());

        managers1.dispose();
        managers2.dispose();
        managers3.dispose();
        serverManagers.dispose();
    }

    /**
     * Check if an exception is raised when trying to open a non-existing database.
     * 
     * @throws MalformedURLException
     * @throws ManagerException
     */
    @Test(expected = GenericJDBCException.class)
    public void testWithCreateFalse() throws MalformedURLException, ManagerException {
        new MockManagers(H2DBConfigurations.create("unittestCreateFalse" + System.nanoTime(), false, false));
        // Expect an exception
    }

    /**
     * Check if the updateDatabase function is called properly.
     * 
     * @throws MalformedURLException
     * @throws ManagerException
     */
    @Test
    public void testWithUpdate() throws MalformedURLException, ManagerException {
        // Create the first database and close it
        MockManagers managers = new MockManagers(H2DBConfigurations.create("./unittest", true, false));
        managers.dispose();
        // Open the same database with a new Managers version.
        managers = new MockManagers(H2DBConfigurations.create("./unittest", true, false)) {
            @Override
            protected void updateDatabase(SessionFactory factory) {
                Dialect dialect = DatabaseUpdateHelper.getDialect(factory);
                List<String> script = new ArrayList<String>();
                script.add(DatabaseUpdateHelper.alertTableAlterColumn(dialect, "MockEntity", "name", Types.VARCHAR, 50, 0, 0));
                DatabaseUpdateHelper.execute(factory, script);
            }
        };
        // Check to ensure the database was updated.
        SessionFactory factory = managers.getSessionFactory();
        String catalog = DatabaseUpdateHelper.getCatalog(factory);
        String schema = DatabaseUpdateHelper.getSchema(factory);
        TableMetadata tableMeta = DatabaseUpdateHelper.getTableMetadata(factory, "MockEntity", schema, catalog);
        assertNotNull(tableMeta);
        ColumnMetadata columnMeta = tableMeta.getColumnMetadata("name");
        assertNotNull(columnMeta);
        assertEquals(50, columnMeta.getColumnSize());
        managers.dispose();
    }

    /**
     * Check if the validateDatabase function is called ans generate exception.
     * 
     * @throws MalformedURLException
     * @throws ManagerException
     */
    @Test
    public void testWithValidate() throws MalformedURLException, ManagerException {
        // Create the first database and close it
        MockManagers managers = new MockManagers(H2DBConfigurations.create("./unittest", true, false));
        managers.dispose();
        // Open the same database with a new Managers version.
        try {
            managers = new MockManagers(H2DBConfigurations.create("./unittest", false, false)) {
                @Override
                protected void validateDatabase(SessionFactory factory) {
                    throw new IllegalStateException("db not valide");
                }
            };
        } catch (ManagerException e) {
            assertTrue(e.getCause() instanceof IllegalStateException);
        }
    }

}
