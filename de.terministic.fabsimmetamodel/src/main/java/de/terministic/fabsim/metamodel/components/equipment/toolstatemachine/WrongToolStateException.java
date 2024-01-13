package de.terministic.fabsim.metamodel.components.equipment.toolstatemachine;

public class WrongToolStateException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = 2384286170291549456L;

	public WrongToolStateException(final String msg) {
		super(msg);
	}

}
