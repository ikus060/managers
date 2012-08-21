/*
 * Copyright (c) 2011, Patrik Dufresne. All rights reserved.
 * Patrik Dufresne PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.patrikdufresne.managers;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ManagedObjectUtils {

	public static void copyProperties(Object orig, Object dest)
			throws IllegalAccessException, InvocationTargetException {

		// Validate existence of the specified beans
		if (dest == null) {
			throw new IllegalArgumentException("No destination specified");
		}
		if (orig == null) {
			throw new IllegalArgumentException("No origin specified");
		}

		BeanInfo beanInfo;
		try {
			beanInfo = Introspector.getBeanInfo(orig.getClass());
		} catch (IntrospectionException e) {
			// cannot introspect, give up
			return;
		}

		PropertyDescriptor[] origDescriptors = beanInfo
				.getPropertyDescriptors();
		for (int i = 0; i < origDescriptors.length; i++) {
			String name = origDescriptors[i].getName();
			if ("class".equals(name)) {
				continue; // No point in trying to set an object's class
			}
			if (origDescriptors[i].getReadMethod() != null
					&& origDescriptors[i].getWriteMethod() != null) {
				// Copy the property value.
				Object value = readProperty(orig, origDescriptors[i]);
				writeProperty(dest, origDescriptors[i], value);
			}
		}

	}

	public static void writeProperty(Object source,
			PropertyDescriptor propertyDescriptor, Object value) {
		try {
			Method writeMethod = propertyDescriptor.getWriteMethod();
			if (null == writeMethod) {
				throw new IllegalArgumentException(
						"Missing public setter method for " //$NON-NLS-1$
								+ propertyDescriptor.getName() + " property"); //$NON-NLS-1$
			}
			if (!writeMethod.isAccessible()) {
				writeMethod.setAccessible(true);
			}
			writeMethod.invoke(source, new Object[] { value });
		} catch (InvocationTargetException e) {
			/*
			 * InvocationTargetException wraps any exception thrown by the
			 * invoked method.
			 */
			throw new RuntimeException(e.getCause());
		} catch (Exception e) {

		}
	}

	public static Object readProperty(Object source,
			PropertyDescriptor propertyDescriptor) {
		try {
			Method readMethod = propertyDescriptor.getReadMethod();
			if (readMethod == null) {
				throw new IllegalArgumentException(propertyDescriptor.getName()
						+ " property does not have a read method."); //$NON-NLS-1$
			}
			if (!readMethod.isAccessible()) {
				readMethod.setAccessible(true);
			}
			return readMethod.invoke(source, (Object[]) null);
		} catch (InvocationTargetException e) {
			/*
			 * InvocationTargetException wraps any exception thrown by the
			 * invoked method.
			 */
			throw new RuntimeException(e.getCause());
		} catch (Exception e) {
			return null;
		}
	}

}
