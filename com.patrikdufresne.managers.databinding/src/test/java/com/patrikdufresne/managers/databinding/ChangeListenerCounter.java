package com.patrikdufresne.managers.databinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.SetChangeEvent;

public class ChangeListenerCounter implements IChangeListener,
		ISetChangeListener {

	private List<ChangeEvent> changeEvents;

	private List<SetChangeEvent> setChangeEvents;

	public void clear() {
		this.changeEvents = null;
		this.setChangeEvents = null;
	}

	public List<ChangeEvent> getChangeEvents() {
		if (this.changeEvents == null) {
			return Collections.EMPTY_LIST;
		}
		return new ArrayList<ChangeEvent>(this.changeEvents);
	}

	public List<SetChangeEvent> getSetChangeEvents() {
		if (this.setChangeEvents == null) {
			return Collections.EMPTY_LIST;
		}
		return new ArrayList<SetChangeEvent>(this.setChangeEvents);
	}

	@Override
	public void handleChange(ChangeEvent event) {
		if (changeEvents == null) {
			changeEvents = new ArrayList<ChangeEvent>();
		}
		changeEvents.add(event);
	}

	@Override
	public void handleSetChange(SetChangeEvent event) {
		if (setChangeEvents == null) {
			setChangeEvents = new ArrayList<SetChangeEvent>();
		}
		setChangeEvents.add(event);
	}

}
