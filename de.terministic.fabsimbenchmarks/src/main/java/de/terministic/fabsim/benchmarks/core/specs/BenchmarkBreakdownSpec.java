package de.terministic.fabsim.benchmarks.core.specs;

import de.terministic.fabsim.core.duration.IValue;

public final class BenchmarkBreakdownSpec {
	private final IValue timeToFailureDistribution;
	private final IValue timeToRepairDistribution;

	public BenchmarkBreakdownSpec(final IValue timeToFailureDistribution, final IValue timeToRepairDistribution) {
		if (timeToFailureDistribution == null) {
			throw new IllegalArgumentException("timeToFailureDistribution must not be null");
		}
		if (timeToRepairDistribution == null) {
			throw new IllegalArgumentException("timeToRepairDistribution must not be null");
		}
		this.timeToFailureDistribution = timeToFailureDistribution;
		this.timeToRepairDistribution = timeToRepairDistribution;
	}

	public IValue getTimeToFailureDistribution() {
		return this.timeToFailureDistribution;
	}

	public IValue getTimeToRepairDistribution() {
		return this.timeToRepairDistribution;
	}
}
