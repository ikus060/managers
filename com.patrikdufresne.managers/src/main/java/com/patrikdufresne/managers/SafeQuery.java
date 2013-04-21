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

/**
 * This interface extends the runnable {@link Query} to handle exceptions.
 * 
 * @author Patrik Dufresne
 * 
 */
public interface SafeQuery<E> extends Query<E> {

    /**
     * Handles an exception thrown by this query's <code>run</code> method. The
     * processing done here should be specific to the particular usecase for
     * this runnable. Generalized exception processing (e.g., logging in the
     * platform's log) is done by the {@link Managers}.
     * 
     * 
     * @param exception
     *            an exception which occurred during processing the body of this
     *            query (i.e., in <code>run()</code>)
     */
    public void handleException(Throwable exception);

}
