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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Transient;

public class ManagedObjectUtils {

    public static void copyProperties(Object orig, Object dest) throws IllegalAccessException, InvocationTargetException {

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

        PropertyDescriptor[] origDescriptors = beanInfo.getPropertyDescriptors();
        for (int i = 0; i < origDescriptors.length; i++) {
            String name = origDescriptors[i].getName();
            if ("class".equals(name)) {
                continue; // No point in trying to set an object's class
            }
            if (origDescriptors[i].getReadMethod().getAnnotation(Transient.class) != null
                    || origDescriptors[i].getWriteMethod().getAnnotation(Transient.class) != null) {
                // Don't copy transient value.
                continue;
            }
            if (origDescriptors[i].getReadMethod() != null && origDescriptors[i].getWriteMethod() != null) {
                // Copy the property value if it changed
                Object newValue = readProperty(orig, origDescriptors[i]);
                Object oldValue = readProperty(dest, origDescriptors[i]);
                if (newValue != oldValue) {
                    writeProperty(dest, origDescriptors[i], newValue);
                }
            }
        }

    }

    /**
     * Returns the element type of the given collection-typed property for the given bean.
     * 
     * @param descriptor
     *            the property being inspected
     * @return the element type of the given collection-typed property if it is an array property, or Object.class
     *         otherwise.
     */
    public static Class getCollectionPropertyElementType(PropertyDescriptor descriptor) {
        Class propertyType = descriptor.getPropertyType();
        return propertyType.isArray() ? propertyType.getComponentType() : Object.class;
    }

    /**
     * Goes recursively into the interface and gets all defined propertyDescriptors
     * 
     * @param propertyDescriptors
     *            The result list of all PropertyDescriptors the given interface defines (hierarchical)
     * @param iface
     *            The interface to fetch the PropertyDescriptors
     * @throws IntrospectionException
     */
    private static void getInterfacePropertyDescriptors(List propertyDescriptors, Class iface) throws IntrospectionException {
        BeanInfo beanInfo = Introspector.getBeanInfo(iface);
        PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
        for (int i = 0; i < pds.length; i++) {
            PropertyDescriptor pd = pds[i];
            propertyDescriptors.add(pd);
        }
        Class[] subIntfs = iface.getInterfaces();
        for (int j = 0; j < subIntfs.length; j++) {
            getInterfacePropertyDescriptors(propertyDescriptors, subIntfs[j]);
        }
    }

    /**
     * @param beanClass
     * @param propertyName
     * @return the PropertyDescriptor for the named property on the given bean class
     */
    public static PropertyDescriptor getPropertyDescriptor(Class beanClass, String propertyName) {
        if (!beanClass.isInterface()) {
            BeanInfo beanInfo;
            try {
                beanInfo = Introspector.getBeanInfo(beanClass);
            } catch (IntrospectionException e) {
                // cannot introspect, give up
                return null;
            }
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (int i = 0; i < propertyDescriptors.length; i++) {
                PropertyDescriptor descriptor = propertyDescriptors[i];
                if (descriptor.getName().equals(propertyName)) {
                    return descriptor;
                }
            }
        } else {
            try {
                PropertyDescriptor propertyDescriptors[];
                List pds = new ArrayList();
                getInterfacePropertyDescriptors(pds, beanClass);
                if (pds.size() > 0) {
                    propertyDescriptors = (PropertyDescriptor[]) pds.toArray(new PropertyDescriptor[pds.size()]);
                    PropertyDescriptor descriptor;
                    for (int i = 0; i < propertyDescriptors.length; i++) {
                        descriptor = propertyDescriptors[i];
                        if (descriptor.getName().equals(propertyName)) return descriptor;
                    }
                }
            } catch (IntrospectionException e) {
                // cannot introspect, give up
                return null;
            }
        }
        throw new IllegalArgumentException("Could not find property with name " + propertyName + " in class " + beanClass); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Sets the contents of the given property on the given source object to the given value.
     * 
     * @param source
     *            the source object which has the property being updated
     * @param propertyName
     *            the property being changed
     * @param value
     *            the new value of the property
     */
    public static void writeProperty(Object source, String propertyName, Object value) {
        writeProperty(source, getPropertyDescriptor(source.getClass(), propertyName), value);
    }

    /**
     * Sets the contents of the given property on the given source object to the given value.
     * 
     * @param source
     *            the source object which has the property being updated
     * @param propertyDescriptor
     *            the property being changed
     * @param value
     *            the new value of the property
     */
    public static void writeProperty(Object source, PropertyDescriptor propertyDescriptor, Object value) {
        try {
            Method writeMethod = propertyDescriptor.getWriteMethod();
            if (null == writeMethod) {
                throw new IllegalArgumentException("Missing public setter method for " //$NON-NLS-1$
                        + propertyDescriptor.getName()
                        + " property"); //$NON-NLS-1$
            }
            if (!writeMethod.isAccessible()) {
                writeMethod.setAccessible(true);
            }
            writeMethod.invoke(source, new Object[] { value });
        } catch (InvocationTargetException e) {
            /*
             * InvocationTargetException wraps any exception thrown by the invoked method.
             */
            throw new RuntimeException(e.getCause());
        } catch (Exception e) {

        }
    }

    /**
     * Returns the contents of the given property for the given bean.
     * 
     * @param source
     *            the source bean
     * @param propertyDescriptor
     *            the property to retrieve
     * @return the contents of the given property for the given bean.
     */
    public static Object readProperty(Object source, String propertyName) {
        return readProperty(source, getPropertyDescriptor(source.getClass(), propertyName));
    }

    /**
     * Returns the contents of the given property for the given bean.
     * 
     * @param source
     *            the source bean
     * @param propertyDescriptor
     *            the property to retrieve
     * @return the contents of the given property for the given bean.
     */
    public static Object readProperty(Object source, PropertyDescriptor propertyDescriptor) {
        try {
            Method readMethod = propertyDescriptor.getReadMethod();
            if (readMethod == null) {
                throw new IllegalArgumentException(propertyDescriptor.getName() + " property does not have a read method."); //$NON-NLS-1$
            }
            if (!readMethod.isAccessible()) {
                readMethod.setAccessible(true);
            }
            return readMethod.invoke(source, (Object[]) null);
        } catch (InvocationTargetException e) {
            /*
             * InvocationTargetException wraps any exception thrown by the invoked method.
             */
            throw new RuntimeException(e.getCause());
        } catch (Exception e) {
            return null;
        }
    }

}
