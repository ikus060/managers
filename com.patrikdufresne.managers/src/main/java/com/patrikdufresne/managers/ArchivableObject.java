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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.patrikdufresne.managers.ManagedObject;

/**
 * This abstract class is used to represent all implementation of
 * {@link ManagedObject} that are archivable. Meaning, the record may be soft
 * deleted: still in database, but not used. To represent archived record, a
 * date is used. If the date is not null, the record is archived. Otherwise the
 * record is not archived / active.
 * 
 * @author Patrik Dufresne
 * 
 */
@MappedSuperclass
public abstract class ArchivableObject extends ManagedObject {

    /**
     * Property value for archived date.
     */
    public static final String ARCHIVED_DATE = "archivedDate";

    private Date archivedDate;

    /**
     * Return the archived date or null is the record is not archived.
     * 
     * @return
     */

    @Column(nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    public Date getArchivedDate() {
        return archivedDate;
    }

    /**
     * Set the archived date. Their is no restriction to un-archive a record.
     * 
     * @param archivedDate
     *            the new archived date.
     */
    public void setArchivedDate(Date archivedDate) {
        this.archivedDate = archivedDate;
    }

}
