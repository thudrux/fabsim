package de.terministic.fabsim.metamodel.components.equipment;

public class InvalidEventForToolGroupStateException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 659100220172121737L;

	public InvalidEventForToolGroupStateException() {
		super();
	}

	public InvalidEventForToolGroupStateException(String msg) {
		super(msg);
	}
}
