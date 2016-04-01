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
package com.patrikdufresne.managers.databinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.SetChangeEvent;

public class ChangeListenerCounter implements IChangeListener, ISetChangeListener {

    private List<ChangeEvent> changeEvents;

    private List<SetChangeEvent> setChangeEvents;

    public void clear() {
        this.changeEvents = null;
        this.setChangeEvents = null;
    }

    public List<ChangeEvent> getChangeEvents() {
        if (this.changeEvents == null) {
            return Collections.EMPTY_LIST;
        }
        return new ArrayList<ChangeEvent>(this.changeEvents);
    }

    public List<SetChangeEvent> getSetChangeEvents() {
        if (this.setChangeEvents == null) {
            return Collections.EMPTY_LIST;
        }
        return new ArrayList<SetChangeEvent>(this.setChangeEvents);
    }

    @Override
    public void handleChange(ChangeEvent event) {
        if (changeEvents == null) {
            changeEvents = new ArrayList<ChangeEvent>();
        }
        changeEvents.add(event);
    }

    @Override
    public void handleSetChange(SetChangeEvent event) {
        if (setChangeEvents == null) {
            setChangeEvents = new ArrayList<SetChangeEvent>();
        }
        setChangeEvents.add(event);
    }

}
