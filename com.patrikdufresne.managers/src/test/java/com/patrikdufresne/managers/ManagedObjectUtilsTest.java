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

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import org.junit.Test;

public class ManagedObjectUtilsTest {

    @Test
    public void testCopyProperties() throws IllegalAccessException, InvocationTargetException {
        MockEntity o1 = new MockEntity();
        o1.setId(1);
        o1.setArchivedDate(new Date());
        o1.setCreationDate(new Date());
        o1.setName("value");

        MockEntity o2 = new MockEntity();
        ManagedObjectUtils.copyProperties(o1, o2);

        assertEquals(o1.getId(), o2.getId());
        assertEquals(o1.getArchivedDate(), o2.getArchivedDate());
        assertEquals(o1.getCreationDate(), o2.getCreationDate());
        assertEquals(o1.getName(), o2.getName());
    }

    @Test
    public void testWriteProperty() {
        MockEntity o1 = new MockEntity();
        ManagedObjectUtils.writeProperty(o1, "id", 1);
        assertEquals(Integer.valueOf(1), o1.getId());
    }

    @Test
    public void testReadProperty() {
        MockEntity o1 = new MockEntity();
        o1.setId(1);
        assertEquals(Integer.valueOf(1), ManagedObjectUtils.readProperty(o1, "id"));
    }

}
