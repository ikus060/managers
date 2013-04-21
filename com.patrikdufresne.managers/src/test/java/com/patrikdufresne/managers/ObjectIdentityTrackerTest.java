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
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

/**
 * Test the {@link ObjectIdentityTracker} class.
 * 
 * @author Patrik Dufresne
 * 
 */
public class ObjectIdentityTrackerTest {

    private ObjectIdentityTracker tracker;

    @Before
    public void createObjectIdentityTracker() {
        tracker = new ObjectIdentityTracker();
    }

    @Test
    public void testFind_WithMockEntity_ReturnEntity() {

        MockEntity entity = new MockEntity();
        entity.setId(new Integer(1));

        tracker.register("MockEntity", entity.getId(), entity);

        Collection<?> finds = tracker.find("MockEntity", entity.getId());
        assertEquals("Wrong number of object found.", 1, finds.size());
        assertTrue(entity == finds.iterator().next());
    }

    @Test
    public void testFind_WithDisposedMockEntity_ReturnNull() {

        MockEntity entity = new MockEntity();
        entity.setId(new Integer(1));

        tracker.register("MockEntity", Integer.valueOf(2), entity);
        entity = null;

        System.gc();

        Collection<?> finds = tracker.find("MockEntity", Integer.valueOf(1));
        assertTrue("Wrong number of object found.", finds == null || finds.size() == 0);

    }

    @Test
    public void testFind_WithTwoEntities_ReturnTwoEntities() {
        MockEntity entity1 = new MockEntity();
        entity1.setId(new Integer(1));

        MockEntity entity2 = new MockEntity();
        entity2.setId(new Integer(1));

        tracker.register("MockEntity", entity1.getId(), entity1);
        tracker.register("MockEntity", entity2.getId(), entity2);

        Collection<?> finds = tracker.find("MockEntity", entity1.getId());
        assertEquals("Wrong number of object found.", 2, finds.size());
    }

    @Test
    public void testUnregister_WithRegisteredEntity_RemoveAllEntities() {
        MockEntity entity1 = new MockEntity();
        entity1.setId(new Integer(1));
        MockEntity entity2 = new MockEntity();
        entity2.setId(new Integer(1));

        tracker.register("MockEntity", entity1.getId(), entity1);
        tracker.register("MockEntity", entity2.getId(), entity2);
        Collection<?> finds = tracker.find("MockEntity", entity1.getId());
        assertEquals("Wrong number of object found.", 2, finds.size());

        tracker.unregister("MockEntity", entity1.getId());

        finds = tracker.find("MockEntity", entity1.getId());
        assertTrue("Wrong number of object found.", finds == null || finds.size() == 0);
    }
}
