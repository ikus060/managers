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

import java.util.Collection;

/**
 * Manager implementing this interface allow the object to be archived.
 * 
 * @author Patrik Dufresne
 * 
 * @param <T>
 */
public interface IArchivableManager<T extends ArchivableObject> extends IManager<T> {

    /**
     * Archive the given objects.
     * 
     * @param s
     *            object to be archived
     * 
     * @throws ManagerException
     */
    void archive(Collection<? extends T> t) throws ManagerException;

}