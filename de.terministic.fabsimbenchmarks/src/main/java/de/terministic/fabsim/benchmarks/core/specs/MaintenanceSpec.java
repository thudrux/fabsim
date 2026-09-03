package de.terministic.fabsim.benchmarks.core.specs;

import de.terministic.fabsim.core.duration.IValue;

public final class MaintenanceSpec {
	private final IValue intervalDistribution;
	private final IValue durationDistribution;

	public MaintenanceSpec(final IValue intervalDistribution, final IValue durationDistribution) {
		if (intervalDistribution == null) {
			throw new IllegalArgumentException("intervalDistribution must not be null");
		}
		if (durationDistribution == null) {
			throw new IllegalArgumentException("durationDistribution must not be null");
		}
		this.intervalDistribution = intervalDistribution;
		this.durationDistribution = durationDistribution;
	}

	public IValue getIntervalDistribution() {
		return this.intervalDistribution;
	}

	public IValue getDurationDistribution() {
		return this.durationDistribution;
	}
}
