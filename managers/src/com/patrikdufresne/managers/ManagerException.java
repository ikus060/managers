package com.patrikdufresne.managers;

/**
 * This exception is throw when any type of error occurred within the execution
 * of a Manager.
 * 
 * @author patapouf
 * 
 */
public class ManagerException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ManagerException() {

	}

	public ManagerException(String message) {
		super(message);
	}

	public ManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public ManagerException(Throwable cause) {
		super(cause);
	}
}
