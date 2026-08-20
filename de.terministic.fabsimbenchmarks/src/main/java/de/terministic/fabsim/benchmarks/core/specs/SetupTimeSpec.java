package de.terministic.fabsim.benchmarks.core.specs;

public final class SetupTimeSpec {
	private final String currentProductName;
	private final Integer currentRouteStepIndex;
	private final String currentSetupStateName;
	private final long setupTime;

	public SetupTimeSpec(final String currentProductName, final Integer currentRouteStepIndex,
			final String currentSetupStateName, final long setupTime) {
		if (currentRouteStepIndex != null && currentRouteStepIndex <= 0) {
			throw new IllegalArgumentException("currentRouteStepIndex must be greater than 0");
		}
		if (setupTime < 0L) {
			throw new IllegalArgumentException("setupTime must not be negative");
		}
		this.currentProductName = normalizeOptional(currentProductName);
		this.currentRouteStepIndex = currentRouteStepIndex;
		this.currentSetupStateName = normalizeOptional(currentSetupStateName);
		this.setupTime = setupTime;
	}

	public static SetupTimeSpec from(final String productName, final int stepIndex,
			final String setupStateName, final long setupTime) {
		if (productName == null || productName.trim().isEmpty()) {
			throw new IllegalArgumentException("productName must not be blank");
		}
		if (setupStateName == null || setupStateName.trim().isEmpty()) {
			throw new IllegalArgumentException("setupStateName must not be blank");
		}
		return new SetupTimeSpec(productName, stepIndex, setupStateName, setupTime);
	}

	public static SetupTimeSpec fromAnyProductAndStep(final String setupStateName, final long setupTime) {
		if (setupStateName == null || setupStateName.trim().isEmpty()) {
			throw new IllegalArgumentException("setupStateName must not be blank");
		}
		return new SetupTimeSpec(null, null, setupStateName, setupTime);
	}

	public static SetupTimeSpec fromAnyCurrentState(final long setupTime) {
		return new SetupTimeSpec(null, null, null, setupTime);
	}

	private static String normalizeOptional(final String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		return value.trim();
	}

	public String getCurrentProductName() {
		return this.currentProductName;
	}

	public Integer getCurrentRouteStepIndex() {
		return this.currentRouteStepIndex;
	}

	public String getCurrentSetupStateName() {
		return this.currentSetupStateName;
	}

	public long getSetupTime() {
		return this.setupTime;
	}
}
