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
package com.patrikdufresne.managers.jface;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Test;

import com.patrikdufresne.util.Localized;

public class MessagesTest {

    /**
     * Check if the creation of the action is working.
     */
    @Test
    public void testLoadCanadaFrench() {
        Locale.setDefault(Locale.CANADA_FRENCH);
        assertEquals("Ajouter", Localized.get(RemoveAction.class, "AbstractAddAction.text"));
    }

    /**
     * Check if the creation of the action is working.
     */
    @Test
    public void testLoadCanada() {
        Locale.setDefault(Locale.CANADA);
        assertEquals("Add", Localized.get(RemoveAction.class, "AbstractAddAction.text"));
    }

}
