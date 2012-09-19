package com.patrikdufresne.managers;

import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;

import com.patrikdufresne.managers.ManagedObject;

/**
 * Entity for unit testing
 * 
 * @author Patrik Dufresne
 * 
 */
@Entity
public class MockEntity extends ManagedObject {

	@Override
	public String toString() {
		return "MockEntity [name=" + name + "]";
	}

	/**
	 * Name property key
	 */
	public static final String NAME = "name";

	private List<String> items;
	private String name;

	@ElementCollection
	public List<String> getItems() {
		return items;
	}

	public void setItems(List<String> items) {
		this.items = items;
	}

	public void setName(String value) {
		changeSupport.firePropertyChange(NAME, this.name, this.name = value);
	}

	public String getName() {
		return this.name;
	}

}
