package com.patrikdufresne.managers;

import java.util.Collection;

/**
 * Manager implementing this interface allow the object to be archived.
 * 
 * @author Patrik Dufresne
 * 
 * @param <T>
 */
public interface IArchivableManager<T extends ArchivableObject> extends
		IManager<T> {

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
