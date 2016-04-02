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

import org.eclipse.core.databinding.observable.Realm;

/**
 * Simple realm implementation that will set itself as default when constructed.
 * Invoke {@link #dispose()} to remove the realm from being the default. Does
 * not support asyncExec(...).
 */
public class DefaultRealm extends Realm {
    private Realm previousRealm;

    public DefaultRealm() {
        previousRealm = super.setDefault(this);
    }

    /**
     * @return always returns true
     */
    @Override
    public boolean isCurrent() {
        return true;
    }

    @Override
    protected void syncExec(Runnable runnable) {
        runnable.run();
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public void asyncExec(Runnable runnable) {
        throw new UnsupportedOperationException("asyncExec is unsupported");
    }

    /**
     * Removes the realm from being the current and sets the previous realm to
     * the default.
     */
    public void dispose() {
        if (getDefault() == this) {
            setDefault(previousRealm);
        }
    }
}