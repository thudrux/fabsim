package de.terministic.fabsim.metamodel.externaldispatch;

public final class ExternalDispatchConfiguration {

	private final String host;
	private final int port;
	private final long timeoutMillis;

	public ExternalDispatchConfiguration(final String host, final int port, final long timeoutMillis) {
		if (host == null || host.trim().isEmpty()) {
			throw new IllegalArgumentException("host must not be blank");
		}
		if (port <= 0) {
			throw new IllegalArgumentException("port must be positive");
		}
		if (timeoutMillis <= 0L) {
			throw new IllegalArgumentException("timeoutMillis must be positive");
		}
		this.host = host;
		this.port = port;
		this.timeoutMillis = timeoutMillis;
	}

	public static ExternalDispatchConfiguration localDefault() {
		return new ExternalDispatchConfiguration("127.0.0.1", 50051, 5000L);
	}

	public String getHost() {
		return this.host;
	}

	public int getPort() {
		return this.port;
	}

	public long getTimeoutMillis() {
		return this.timeoutMillis;
	}
}
