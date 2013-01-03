/*
 * Copyright (c) 2011, Patrik Dufresne. All rights reserved.
 * Patrik Dufresne PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
