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

import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * Entity for unit testing
 * 
 * @author Patrik Dufresne
 * 
 */
@Entity
public class MockEntity extends ArchivableObject {

    /**
     * Name property key
     */
    public static final String NAME = "name";

    private List<String> items;

    private String name;
    @ElementCollection
    public List<String> getItems() {
        return items;
    }

    public String getName() {
        return this.name;
    }

    @Transient
    public Integer getTransientId() {
        if (getId() != null) {
            return getId() * 3;
        }
        return null;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

    public void setName(String value) {
        changeSupport.firePropertyChange(NAME, this.name, this.name = value);
    }

    @Transient
    public void setTransientId(Integer id) {
        if (id != null) {
            id = id / 3;
        }
        setId(id);
    }

    @Override
    public String toString() {
        return "MockEntity [name=" + name + "]";
    }

}
