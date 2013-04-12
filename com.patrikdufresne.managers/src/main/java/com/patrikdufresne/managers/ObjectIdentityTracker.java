/*
 * Copyright (c) 2011, Patrik Dufresne. All rights reserved.
 * Patrik Dufresne PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.patrikdufresne.managers;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.map.AbstractReferenceMap;
import org.apache.commons.collections.map.ReferenceIdentityMap;

/**
 * This class is used to keep track of any entity instance during the time life
 * of the managers. It keep a weak reference to the object to the garbage
 * collector may un-allocate the object. Before the object is freed, this class
 * is a cache for entities.
 * 
 * @author Patrik Dufresne
 * 
 */
public class ObjectIdentityTracker {

	/**
	 * The cache list.
	 */
	private Map<String, Map<Serializable, ReferenceIdentityMap>> cache;

	/**
	 * Used internally to create a reference-map instance.
	 * 
	 * @return the map.
	 */
	private ReferenceIdentityMap createReferenceMap() {
		return new ReferenceIdentityMap(AbstractReferenceMap.WEAK,
				AbstractReferenceMap.HARD);
	}

	/**
	 * This function is used to find an entity corresponding to the given
	 * parameters.
	 * 
	 * @param entityName
	 *            the entity name (the entity class name)
	 * @param id
	 *            the entity id
	 * @return the entity object or null if the entity is not found.
	 */
	public Collection<?> find(String entityName, Serializable id) {
		purge();
		if (this.cache == null) {
			return null;
		}
		Map<Serializable, ReferenceIdentityMap> map = this.cache
				.get(entityName);
		if (map == null) {
			return null;
		}
		ReferenceIdentityMap weakMap = map.get(id);
		if (weakMap == null) {
			return null;
		}
		return weakMap.keySet();
	}

	private void purge() {

	}

	/**
	 * This function is used to add a new entity object to this tracker.
	 * 
	 * @param entityName
	 *            the entity name (the entity class name)
	 * @param id
	 *            the entity id
	 * @param entity
	 *            the entity object
	 */
	public void register(String entityName, Serializable id, Object entity) {
		if (this.cache == null) {
			this.cache = new HashMap<String, Map<Serializable, ReferenceIdentityMap>>();
		}
		Map<Serializable, ReferenceIdentityMap> map = this.cache
				.get(entityName);
		if (map == null) {
			map = new HashMap<Serializable, ReferenceIdentityMap>();
			this.cache.put(entityName, map);
		}
		ReferenceIdentityMap weakMap = map.get(id);
		if (weakMap == null) {
			weakMap = createReferenceMap();
			map.put(id, weakMap);
		}
		weakMap.put(entity, EXISTS);
	}

	private Object EXISTS = new Object();

	/**
	 * This function is used to unregister an entity object. This function may
	 * be called when the object it removed from the persistent layer.
	 * 
	 * @param entityName
	 *            the entity class name
	 * @param id
	 *            the entity id
	 */
	public void unregister(String entityName, Serializable id) {
		if (this.cache == null) {
			return;
		}
		Map<Serializable, ReferenceIdentityMap> map = this.cache
				.get(entityName);
		if (map == null) {
			return;
		}
		// Remove weak reference.
		map.remove(id);
		// Check if the map may be remove.
		if (map.size() == 0) {
			this.cache.remove(entityName);
		}
	}

}