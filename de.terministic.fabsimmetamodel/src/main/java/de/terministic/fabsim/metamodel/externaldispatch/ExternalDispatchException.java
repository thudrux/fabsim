package de.terministic.fabsim.metamodel.externaldispatch;

public class ExternalDispatchException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ExternalDispatchException(final String message) {
		super(message);
	}

	public ExternalDispatchException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
