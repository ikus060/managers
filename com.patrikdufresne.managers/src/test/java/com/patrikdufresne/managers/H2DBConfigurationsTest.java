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
import static org.hamcrest.CoreMatchers.*;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.hibernate.cfg.Configuration;
import org.junit.Test;

/**
 * Check behavior of {@link H2DBConfigurations}
 * 
 * @author Patrik Dufresne
 * 
 */
public class H2DBConfigurationsTest {

    @Test
    public void testGetServerUrl_WithRunningServer() throws ManagerException, IOException {
        String url = "test-" + System.nanoTime();
        Configuration config = H2DBConfigurations.create(url, true, true);
        MockManagers managers = new MockManagers(config);
        try {
            assertThat(H2DBConfigurations.getServerUrl(url)[0], containsString(url));
            assertThat(H2DBConfigurations.getServerUrl(url)[0], startsWith("jdbc:h2:tcp://"));
        } finally {
            managers.dispose();
        }
    }

    @Test(expected = IOException.class)
    public void testGetServerUrl_WithoutRunningServer() throws ManagerException, IOException {
        String url = "test-" + System.nanoTime();
        Configuration config = H2DBConfigurations.create(url, true, false);
        MockManagers managers = new MockManagers(config);
        try {
            H2DBConfigurations.getServerUrl(url);
        } finally {
            managers.dispose();
        }
    }

    @Test(expected = FileNotFoundException.class)
    public void testGetServerUrl_WithInvalidFile() throws ManagerException, IOException {
        String url = "test-" + System.nanoTime();
        H2DBConfigurations.getServerUrl(url);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetServerUrl_WithNull() throws ManagerException, IOException {
        H2DBConfigurations.getServerUrl(null);
    }

}
