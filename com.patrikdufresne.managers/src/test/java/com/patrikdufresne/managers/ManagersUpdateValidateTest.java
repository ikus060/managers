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

import java.net.MalformedURLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.dialect.Dialect;
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

    private MockManagers managers;

    protected MockManagers getManagers() {
        return this.managers;
    }

    @Test
    public void testUpdate() throws MalformedURLException, ManagerException {

        // Create the first database and close it
        DatabaseUrl url = new DatabaseUrl("./unittestUpdate");
        url.localfile().delete();
        managers = new MockManagers(url);
        managers.dispose();

        // Open the same database with a new Managers version.
        managers = new MockManagers(url) {

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

}
