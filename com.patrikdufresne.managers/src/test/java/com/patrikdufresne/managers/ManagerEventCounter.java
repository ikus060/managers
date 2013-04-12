package com.patrikdufresne.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This implementation of {@link IManagerObserver} keep reference of every event
 * received.
 * 
 * @author ikus060
 * 
 */
public final class ManagerEventCounter implements IManagerObserver {
	/**
	 * Array holding the events
	 */
	private List<ManagerEvent> events;

	/**
	 * Remove all the event
	 */
	public void clear() {
		this.events = null;
	}

	/**
	 * Return the number of event captured.
	 * 
	 * @return
	 */
	public int size() {
		if (this.events == null) {
			return 0;
		}
		return this.events.size();
	}

	/**
	 * Return the list of event captured by this observer
	 * 
	 * @return list of event.
	 */
	public List<ManagerEvent> getEvents() {
		if (this.events == null) {
			return Collections.EMPTY_LIST;
		}
		return this.events;
	}

	/**
	 * This implementation keep reference of every event received.
	 */
	@Override
	public void handleManagerEvent(ManagerEvent event) {
		if (this.events == null) {
			this.events = new ArrayList<ManagerEvent>();
		}
		this.events.add(event);
	}
}