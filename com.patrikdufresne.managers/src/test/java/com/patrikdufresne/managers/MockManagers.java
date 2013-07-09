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

import org.hibernate.cfg.Configuration;

/**
 * Managers for unit test
 * 
 * @author Patrik Dufresne
 * 
 */
public class MockManagers extends Managers {

    /**
     * The entity manager
     */
    private MockEntityManager mockEntityManager;

    /**
     * Default constructor.
     * 
     * @param url
     *            the database url
     * @throws ManagerException
     */
    public MockManagers(Configuration config) throws ManagerException {
        super(config);
    }

    /**
     * This implementation add only one annotated class.
     */
    @Override
    protected void configure(Configuration config) {
        super.configure(config);
        config.addAnnotatedClass(MockEntity.class);
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
