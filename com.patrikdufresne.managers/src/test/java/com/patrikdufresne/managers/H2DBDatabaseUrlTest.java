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

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class H2DBDatabaseUrlTest {

    @Test
    public void testWithFileExtention() throws IOException {
        H2DBDatabaseUrl url = new H2DBDatabaseUrl("./my_database.h2.db");
        assertEquals(new File("./my_database").getCanonicalPath(), url.getAbsolutePath());
        assertEquals("my_database", url.getName());
        assertEquals("file:./my_database", url.toString());
        assertTrue(url.isLocal());
        assertFalse(url.isRemote());
        assertFalse(url.isInMemory());
    }

    @Test
    public void testWithRelativeFileURL() throws IOException {
        H2DBDatabaseUrl url = new H2DBDatabaseUrl("./my_database");
        assertEquals(new File("./my_database").getCanonicalPath(), url.getAbsolutePath());
        assertEquals("my_database", url.getName());
        assertEquals("file:./my_database", url.toString());
        assertTrue(url.isLocal());
        assertFalse(url.isRemote());
        assertFalse(url.isInMemory());
    }

    @Test
    public void testWithFileURL() throws IOException {
        H2DBDatabaseUrl url = new H2DBDatabaseUrl("file:./my_database");
        assertEquals(new File("./my_database").getCanonicalPath(), url.getAbsolutePath());
        assertEquals("my_database", url.getName());
        assertEquals("file:./my_database", url.toString());
        assertTrue(url.isLocal());
        assertFalse(url.isRemote());
        assertFalse(url.isInMemory());
    }

    @Test
    public void testWithJdbcH2Local() throws IOException {
        H2DBDatabaseUrl url = new H2DBDatabaseUrl("jdbc:h2:./my_database");
        assertEquals(new File("./my_database").getCanonicalPath(), url.getAbsolutePath());
        assertEquals("my_database", url.getName());
        assertEquals("jdbc:h2:./my_database", url.toString());
        assertTrue(url.isLocal());
        assertFalse(url.isRemote());
        assertFalse(url.isInMemory());
    }

    @Test
    public void testWithJdbcH2Remote() throws IOException {
        H2DBDatabaseUrl url = new H2DBDatabaseUrl("jdbc:h2:tcp://localhost/~/test");
        assertEquals(null, url.getAbsolutePath());
        assertEquals("test", url.getName());
        assertEquals("jdbc:h2:tcp://localhost/~/test", url.toString());
        assertFalse(url.isLocal());
        assertTrue(url.isRemote());
        assertFalse(url.isInMemory());
    }

    @Test
    public void testWithJdbcH2RemoteWithPort() throws IOException {
        H2DBDatabaseUrl url = new H2DBDatabaseUrl("jdbc:h2:tcp://dbserv:8084/~/sample");
        assertEquals(null, url.getAbsolutePath());
        assertEquals("sample", url.getName());
        assertEquals("jdbc:h2:tcp://dbserv:8084/~/sample", url.toString());
        assertFalse(url.isLocal());
        assertTrue(url.isRemote());
        assertFalse(url.isInMemory());
    }

    @Test
    public void testWithJdbcH2InMemory() throws IOException {
        H2DBDatabaseUrl url = new H2DBDatabaseUrl("jdbc:h2:mem:test_mem");
        assertEquals(null, url.getAbsolutePath());
        assertEquals("test_mem", url.getName());
        assertEquals("jdbc:h2:mem:test_mem", url.toString());
        assertFalse(url.isLocal());
        assertFalse(url.isRemote());
        assertTrue(url.isInMemory());
    }

}
