package com.patrikdufresne.managers;

import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;

import com.patrikdufresne.managers.ManagedObject;

@Entity
public class MockEntity extends ManagedObject {

	private List<String> items;
	private String string;

	@ElementCollection
	public List<String> getItems() {
		return items;
	}

	public void setItems(List<String> items) {
		this.items = items;
	}

	public void setString(String value) {
		changeSupport.firePropertyChange("array", this.string,
				this.string = value);
	}

	public String getString() {
		return this.string;
	}

}
