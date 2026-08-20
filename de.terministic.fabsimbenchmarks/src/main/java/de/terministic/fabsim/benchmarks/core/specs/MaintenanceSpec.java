package de.terministic.fabsim.benchmarks.core.specs;

import de.terministic.fabsim.core.duration.IValue;

public final class MaintenanceSpec {
	private final long interval;
	private final IValue durationDistribution;

	public MaintenanceSpec(final long interval, final IValue durationDistribution) {
		if (interval <= 0L) {
			throw new IllegalArgumentException("interval must be greater than 0");
		}
		if (durationDistribution == null) {
			throw new IllegalArgumentException("durationDistribution must not be null");
		}
		this.interval = interval;
		this.durationDistribution = durationDistribution;
	}

	public long getInterval() {
		return this.interval;
	}

	public IValue getDurationDistribution() {
		return this.durationDistribution;
	}
}
