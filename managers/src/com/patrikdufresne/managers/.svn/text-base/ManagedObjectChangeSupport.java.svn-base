package com.patrikdufresne.managers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeListenerProxy;
import java.io.Serializable;
import java.util.Arrays;
import java.util.EventListener;

/**
 * This is a utility class that can be used by beans that support bound
 * properties. It manages a list of listeners and dispatches
 * {@link PropertyChangeEvent}s to them. You can use an instance of this class
 * as a member field of your bean and delegate these types of work to it. The
 * {@link PropertyChangeListener} can be registered for all properties or for a
 * property specified by name.
 * <p>
 * Here is an example of {@code PropertyChangeSupport} usage that follows the
 * rules and recommendations laid out in the JavaBeans&trade; specification:
 * 
 * <pre>
 * public class MyBean {
 *     private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
 * 
 *     public void addPropertyChangeListener(PropertyChangeListener listener) {
 *         this.pcs.addPropertyChangeListener(listener);
 *     }
 * 
 *     public void removePropertyChangeListener(PropertyChangeListener listener) {
 *         this.pcs.removePropertyChangeListener(listener);
 *     }
 * 
 *     private String value;
 * 
 *     public String getValue() {
 *         return this.value;
 *     }
 * 
 *     public void setValue(String newValue) {
 *         String oldValue = this.value;
 *         this.value = newValue;
 *         this.pcs.firePropertyChange("value", oldValue, newValue);
 *     }
 * 
 *     [...]
 * }
 * </pre>
 * <p>
 * A {@code PropertyChangeSupport} instance is thread-safe.
 * <p>
 * This class is serializable. When it is serialized it will save (and restore)
 * any listeners that are themselves serializable. Any non-serializable
 * listeners will be skipped during serialization.
 * 
 */
public class ManagedObjectChangeSupport implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3469976102973763335L;

	private static final String ALL_PROPERTIES = "ManagedObjectChangeSupport.allProperties"; //$NON-NLS-1$

	/**
	 * Growth factor.
	 */
	private static final int GROW_SIZE = 4;

	/**
	 * Level indicator used to decrease or increase size of this table.
	 */
	private int level;

	private PropertyChangeListener[] listeners;

	private String[] properties;

	/**
	 * The object to be provided as the "source" for any generated events.
	 */
	private Object source;

	private PropertyChangeEvent delayedEvents[];

	/**
	 * After calling this function, the change supported will delay the fire
	 * events until resume is called.
	 */
	public void suspend() {
		if (this.delayedEvents == null) {
			this.delayedEvents = new PropertyChangeEvent[GROW_SIZE];
		}
	}

	/**
	 * Resume the fire events. Every delayed event are fired. Does nothing if
	 * suspend is not called.
	 */
	public void resume() {
		if (this.delayedEvents == null) {
			return;
		}
		PropertyChangeEvent[] events = this.delayedEvents;
		this.delayedEvents = null;

		for (PropertyChangeEvent e : events) {
			sendEvent(e);
		}

	}

	/**
	 * Constructs a <code>PropertyChangeSupport</code> object.
	 * 
	 * @param sourceBean
	 *            The bean to be given as the source for any events.
	 */
	public ManagedObjectChangeSupport(Object sourceBean) {
		if (sourceBean == null) {
			throw new NullPointerException();
		}
		this.source = sourceBean;
	}

	/**
	 * Add a PropertyChangeListener to the listener list. The listener is
	 * registered for all properties. The same listener object may be added more
	 * than once, and will be called as many times as it is added. If
	 * <code>listener</code> is null, no exception is thrown and no action is
	 * taken.
	 * 
	 * @param listener
	 *            The PropertyChangeListener to be added
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		if (listener == null) {
			return;
		}
		if (listener instanceof PropertyChangeListenerProxy) {
			PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy) listener;
			// Call two argument add method.
			addPropertyChangeListener(proxy.getPropertyName(),
					(PropertyChangeListener) proxy.getListener());
		} else {
			addPropertyChangeListener(ALL_PROPERTIES, listener);
		}
	}

	/**
	 * Add a PropertyChangeListener for a specific property. The listener will
	 * be invoked only when a call on firePropertyChange names that specific
	 * property. The same listener object may be added more than once. For each
	 * property, the listener will be invoked the number of times it was added
	 * for that property. If <code>propertyName</code> or <code>listener</code>
	 * is null, no exception is thrown and no action is taken.
	 * 
	 * @param propertyName
	 *            The name of the property to listen on.
	 * @param eventListener
	 *            The PropertyChangeListener to be added
	 */
	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener eventListener) {
		if (propertyName == null || eventListener == null) {
			return;
		}
		hook(propertyName, eventListener);
	}

	/**
	 * Fires a property change event to listeners that have been registered to
	 * track updates of all properties or a property with the specified name.
	 * <p>
	 * No event is fired if the given event's old and new values are equal and
	 * non-null.
	 * 
	 * @param event
	 *            the {@code PropertyChangeEvent} to be fired
	 */
	public void firePropertyChange(PropertyChangeEvent event) {
		// Check if the change support is currently in suspend mode
		if (this.delayedEvents != null) {
			delayFireEvent(event);
			return;
		}

		if (this.properties == null || this.listeners == null) {
			return;
		}
		Object oldValue = event.getOldValue();
		Object newValue = event.getNewValue();
		if (oldValue == null || newValue == null || !oldValue.equals(newValue)) {
			sendEvent(event);
		}
	}

	/**
	 * Reports a boolean bound property update to listeners that have been
	 * registered to track updates of all properties or a property with the
	 * specified name.
	 * <p>
	 * No event is fired if old and new values are equal.
	 * <p>
	 * This is merely a convenience wrapper around the more general
	 * {@link #firePropertyChange(String, Object, Object)} method.
	 * 
	 * @param propertyName
	 *            the programmatic name of the property that was changed
	 * @param oldValue
	 *            the old value of the property
	 * @param newValue
	 *            the new value of the property
	 */
	public void firePropertyChange(String propertyName, boolean oldValue,
			boolean newValue) {
		if (oldValue != newValue) {
			firePropertyChange(propertyName, Boolean.valueOf(oldValue),
					Boolean.valueOf(newValue));
		}
	}

	/**
	 * Reports an integer bound property update to listeners that have been
	 * registered to track updates of all properties or a property with the
	 * specified name.
	 * <p>
	 * No event is fired if old and new values are equal.
	 * <p>
	 * This is merely a convenience wrapper around the more general
	 * {@link #firePropertyChange(String, Object, Object)} method.
	 * 
	 * @param propertyName
	 *            the programmatic name of the property that was changed
	 * @param oldValue
	 *            the old value of the property
	 * @param newValue
	 *            the new value of the property
	 */
	public void firePropertyChange(String propertyName, int oldValue,
			int newValue) {
		if (oldValue != newValue) {
			firePropertyChange(propertyName, Integer.valueOf(oldValue),
					Integer.valueOf(newValue));
		}
	}

	/**
	 * Reports a bound property update to listeners that have been registered to
	 * track updates of all properties or a property with the specified name.
	 * <p>
	 * No event is fired if old and new values are equal and non-null.
	 * <p>
	 * This is merely a convenience wrapper around the more general
	 * {@link #firePropertyChange(PropertyChangeEvent)} method.
	 * 
	 * @param propertyName
	 *            the programmatic name of the property that was changed
	 * @param oldValue
	 *            the old value of the property
	 * @param newValue
	 *            the new value of the property
	 */
	public void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
		if (oldValue == null || newValue == null || !oldValue.equals(newValue)) {
			firePropertyChange(new PropertyChangeEvent(this.source,
					propertyName, oldValue, newValue));
		}
	}

	/**
	 * Returns an array of all the listeners that were added to the
	 * PropertyChangeSupport object with addPropertyChangeListener().
	 * <p>
	 * If some listeners have been added with a named property, then the
	 * returned array will be a mixture of PropertyChangeListeners and
	 * <code>PropertyChangeListenerProxy</code>s. If the calling method is
	 * interested in distinguishing the listeners then it must test each element
	 * to see if it's a <code>PropertyChangeListenerProxy</code>, perform the
	 * cast, and examine the parameter.
	 * 
	 * <pre>
	 * PropertyChangeListener[] listeners = bean.getPropertyChangeListeners();
	 * for (int i = 0; i &lt; listeners.length; i++) {
	 * 	if (listeners[i] instanceof PropertyChangeListenerProxy) {
	 * 		PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy) listeners[i];
	 * 		if (proxy.getPropertyName().equals(&quot;foo&quot;)) {
	 * 			// proxy is a PropertyChangeListener which was associated
	 * 			// with the property named &quot;foo&quot;
	 * 		}
	 * 	}
	 * }
	 * </pre>
	 * 
	 * @see PropertyChangeListenerProxy
	 * @return all of the <code>PropertyChangeListeners</code> added or an empty
	 *         array if no listeners have been added
	 * @since 1.4
	 */
	public PropertyChangeListener[] getPropertyChangeListeners() {
		PropertyChangeListener[] list = new PropertyChangeListener[this.properties.length];
		int j = 0;
		for (int i = 0; i < this.properties.length; i++) {
			if (this.properties[i].equals(ALL_PROPERTIES)) {
				list[j] = this.listeners[i];
				j++;
			}
		}
		return Arrays.copyOf(list, j);
	}

	/**
	 * Returns an array of all the listeners which have been associated with the
	 * named property.
	 * 
	 * @param propertyName
	 *            The name of the property being listened to
	 * @return all of the <code>PropertyChangeListeners</code> associated with
	 *         the named property. If no such listeners have been added, or if
	 *         <code>propertyName</code> is null, an empty array is returned.
	 * @since 1.4
	 */
	public PropertyChangeListener[] getPropertyChangeListeners(
			String propertyName) {
		PropertyChangeListener[] list = new PropertyChangeListener[this.properties.length];
		int j = 0;
		for (int i = 0; i < this.properties.length; i++) {
			if (this.properties[i].equals(propertyName)) {
				list[j] = this.listeners[i];
				j++;
			}
		}
		return Arrays.copyOf(list, j);
	}

	/**
	 * Check if there are any listeners for a specific property, including those
	 * registered on all properties. If <code>propertyName</code> is null, only
	 * check for listeners registered on all properties.
	 * 
	 * @param propertyName
	 *            the property name.
	 * @return true if there are one or more listeners for the given property
	 */
	public boolean hasListeners(String propertyName) {
		int i = 0;
		while (i < this.properties.length
				&& (this.properties[i] == null || !this.properties[i]
						.equals(propertyName))) {
			i++;
		}
		return i < this.properties.length;
	}

	private void hook(String propertyName, PropertyChangeListener listener) {
		// Initialize arrays
		if (this.properties == null)
			this.properties = new String[GROW_SIZE];
		if (this.listeners == null)
			this.listeners = new PropertyChangeListener[GROW_SIZE];

		// Find an empty index
		int length = this.properties.length;
		int index = length - 1;
		while (index >= 0) {
			if (this.properties[index] != null) {
				break;
			}
			--index;
		}
		index++;
		if (index == length) {
			// Increase arrays size
			String[] newProperties = new String[length + GROW_SIZE];
			PropertyChangeListener[] newListeners = new PropertyChangeListener[length
					+ GROW_SIZE];
			System.arraycopy(this.properties, 0, newProperties, 0, length);
			System.arraycopy(this.listeners, 0, newListeners, 0, length);
			this.properties = newProperties;
			this.listeners = newListeners;
		}
		this.properties[index] = propertyName;
		this.listeners[index] = listener;
	}

	private void delayFireEvent(PropertyChangeEvent event) {
		if (this.delayedEvents == null)
			return;
		// Find an empty index
		int length = this.delayedEvents.length;
		int index = length - 1;
		while (index >= 0) {
			if (this.delayedEvents[index] != null) {
				break;
			}
			--index;
		}
		index++;
		if (index == length) {
			// Increase arrays size
			PropertyChangeEvent[] newDelayedEvents = new PropertyChangeEvent[length
					+ GROW_SIZE];
			System.arraycopy(this.listeners, 0, newDelayedEvents, 0, length);
			this.delayedEvents = newDelayedEvents;
		}
		this.delayedEvents[index] = event;
	}

	/**
	 * Remove a listener from this notify table.
	 * 
	 * @param index
	 *            the index of the listener to be remove.
	 */
	private void remove(int index) {
		int i = index;
		if (this.level == 0) {
			// Move the data within the arrays
			int end = this.properties.length - 1;
			System.arraycopy(this.properties, i + 1, this.properties, i, end
					- i);
			System.arraycopy(this.listeners, i + 1, this.listeners, i, end - i);
			i = end;
		} else {
			if (this.level > 0)
				this.level = -this.level;
		}
		this.properties[i] = null;
		this.listeners[i] = null;
	}

	/**
	 * Remove a PropertyChangeListener from the listener list. This removes a
	 * PropertyChangeListener that was registered for all properties. If
	 * <code>listener</code> was added more than once to the same event source,
	 * it will be notified one less time after being removed. If
	 * <code>listener</code> is null, or was never added, no exception is thrown
	 * and no action is taken.
	 * 
	 * @param listener
	 *            The PropertyChangeListener to be removed
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		if (listener == null) {
			return;
		}
		if (listener instanceof PropertyChangeListenerProxy) {
			PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy) listener;
			// Call two argument remove method.
			removePropertyChangeListener(proxy.getPropertyName(),
					proxy.getListener());
		} else {
			removePropertyChangeListener(ALL_PROPERTIES, listener);
		}
	}

	/**
	 * Remove a PropertyChangeListener for a specific property. If
	 * <code>listener</code> was added more than once to the same event source
	 * for the specified property, it will be notified one less time after being
	 * removed. If <code>propertyName</code> is null, no exception is thrown and
	 * no action is taken. If <code>listener</code> is null, or was never added
	 * for the specified property, no exception is thrown and no action is
	 * taken.
	 * 
	 * @param propertyName
	 *            The name of the property that was listened on.
	 * @param listener
	 *            The PropertyChangeListener to be removed
	 */
	public void removePropertyChangeListener(String propertyName,
			EventListener listener) {
		if (listener == null || propertyName == null) {
			return;
		}
		unhook(propertyName, listener);
	}

	/**
	 * Send the event to the interested listeners.
	 * 
	 * @param event
	 *            the managed event
	 */
	private void sendEvent(PropertyChangeEvent event) {
		if (this.properties == null || this.listeners == null || event == null
				|| event.getPropertyName() == null) {
			return;
		}
		this.level += this.level >= 0 ? 1 : -1;
		try {
			// Send the vent to any listener matching the event type and class
			// type
			for (int i = 0; i < this.properties.length; i++) {
				if ((this.properties[i].equals(ALL_PROPERTIES) || this.properties[i]
						.equals(event.getPropertyName()))
						&& this.listeners[i] != null) {
					this.listeners[i].propertyChange(event);
				}
			}
		} finally {
			boolean compact = this.level < 0;
			this.level -= this.level >= 0 ? 1 : -1;
			if (compact && this.level == 0) {
				int index = 0;
				for (int i = 0; i < this.properties.length; i++) {
					if (this.properties[i] != null) {
						this.properties[index] = this.properties[i];
						this.listeners[index] = this.listeners[i];
						index++;
					}
				}
				for (int i = index; i < this.properties.length; i++) {
					this.properties[i] = null;
					this.listeners[i] = null;
				}
			}
		}
	}

	private void unhook(String propertyName, EventListener listener) {
		if (propertyName == null || listener == null) {
			throw new NullPointerException();
		}
		if (this.properties == null || this.listeners == null)
			return;
		for (int i = 0; i < this.properties.length; i++) {
			if (this.properties[i].equals(propertyName)
					&& this.listeners[i] == listener) {
				remove(i);
				return;
			}
		}
	}
}