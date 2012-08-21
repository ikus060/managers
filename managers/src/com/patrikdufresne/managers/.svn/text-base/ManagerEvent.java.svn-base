package com.patrikdufresne.managers;

import java.util.Arrays;
import java.util.Collection;

/**
 * This class represent an event. It mainly used to notify a IManagerObserver
 * about a modification happening in the Manager: add, update, remove event.
 * <p>
 * This event hold only one type of event but it may applied to multiple
 * objects.
 * <p>
 * The type value is either one of the type constant defined in this class (ADD,
 * REMOVE, UPDATE and UPDATE_PARENT) or must be build be bitwise OR'ing together
 * (that is using the <code>int</code> "|" operator) two or more of those
 * constants.
 * 
 */
public class ManagerEvent {

	/**
	 * Object added.
	 */
	public static final int ADD = 1;

	/**
	 * Object removed.
	 */
	public static final int REMOVE = (1 << 1);
	/**
	 * One or more object's property has changed.
	 */
	public static final int UPDATE = (1 << 2);
	/**
	 * Object's parent has changed.
	 */
	public static final int UPDATE_PARENT = (1 << 3);
	/**
	 * For internal used - reload of the object is required.
	 */
	public static final int RELOAD = (1 << 4);
	/**
	 * All flags.
	 */
	public static final int ALL = ADD | UPDATE | REMOVE | UPDATE_PARENT;
	/**
	 * The event type.
	 */
	public int type;
	/**
	 * The class type.
	 */
	public Class<?> clazz;
	/**
	 * List of elements.
	 */
	public Collection<Object> objects;
	/**
	 * Not used.
	 */
	public String[] properties;

	/**
	 * Return true if equal
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ManagerEvent other = (ManagerEvent) obj;
		if (clazz == null) {
			if (other.clazz != null)
				return false;
		} else if (!clazz.equals(other.clazz))
			return false;
		if (objects == null) {
			if (other.objects != null)
				return false;
		} else if (!objects.equals(other.objects))
			return false;
		if (!Arrays.equals(properties, other.properties))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	/**
	 * Return a hash code.
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
		result = prime * result + ((objects == null) ? 0 : objects.hashCode());
		result = prime * result + Arrays.hashCode(properties);
		result = prime * result + type;
		return result;
	}
}
