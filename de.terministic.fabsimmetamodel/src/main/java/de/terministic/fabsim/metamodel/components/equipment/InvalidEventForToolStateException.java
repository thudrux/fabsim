package de.terministic.fabsim.metamodel.components.equipment;

public class InvalidEventForToolStateException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = -962011282402270663L;

	public InvalidEventForToolStateException() {
		super();
	}

	public InvalidEventForToolStateException(final String msg) {
		super(msg);
	}
}
