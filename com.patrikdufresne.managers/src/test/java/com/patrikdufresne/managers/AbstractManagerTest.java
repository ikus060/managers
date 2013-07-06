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
            managers.dispose();
            managers.getDatabaseUrl().localfile().delete();
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
        return addMockEntity(managers, "");
    }

    public static MockEntity addMockEntity(MockManagers managers, String name) {
        MockEntity entity = new MockEntity();
        entity.setName(name);
        try {
            managers.addAll(Arrays.asList(entity));
        } catch (ManagerException e) {
            fail("Fail to create the entity", e);
            return null;
        }
        return entity;
    }
}
