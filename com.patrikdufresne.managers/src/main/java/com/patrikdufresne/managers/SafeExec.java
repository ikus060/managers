package com.patrikdufresne.managers;

/**
 * This interface extends the runnable {@link Exec} to handle exceptions.
 * 
 * @author Patrik Dufresne
 * 
 */
public interface SafeExec extends Exec {

	/**
	 * Handles an exception thrown by this exec's <code>run</code> method. The
	 * processing done here should be specific to the particular usecase for
	 * this runnable. Generalized exception processing (e.g., logging in the
	 * platform's log) is done by the {@link Managers}.
	 * 
	 * 
	 * @param exception
	 *            an exception which occurred during processing the body of this
	 *            query (i.e., in <code>run()</code>)
	 */
	public void handleException(Throwable exception);

}
