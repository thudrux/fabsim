package de.terministic.fabsim.benchmarks.core.specs;

import de.terministic.fabsim.core.duration.IValue;
import de.terministic.fabsim.metamodel.components.ProcessStep.ProcessType;

public final class BenchmarkRouteStepSpec {
	private final String toolGroupName;
	private final ProcessType processingUnit;
	private final IValue processingTimeDistribution;
	private final Integer batchMinimum;
	private final Integer batchMaximum;

	public BenchmarkRouteStepSpec(final String toolGroupName, final ProcessType processingUnit,
			final IValue processingTimeDistribution) {
		this(toolGroupName, processingUnit, processingTimeDistribution, null, null);
	}

	public BenchmarkRouteStepSpec(final String toolGroupName, final ProcessType processingUnit,
			final IValue processingTimeDistribution, final Integer batchMinimum, final Integer batchMaximum) {
		if (toolGroupName == null || toolGroupName.trim().isEmpty()) {
			throw new IllegalArgumentException("toolGroupName must not be blank");
		}
		if (processingUnit == null) {
			throw new IllegalArgumentException("processingUnit must not be null");
		}
		if (processingTimeDistribution == null) {
			throw new IllegalArgumentException("processingTimeDistribution must not be null");
		}
		validateBatchConfiguration(processingUnit, batchMinimum, batchMaximum);
		this.toolGroupName = toolGroupName;
		this.processingUnit = processingUnit;
		this.processingTimeDistribution = processingTimeDistribution;
		this.batchMinimum = batchMinimum;
		this.batchMaximum = batchMaximum;
	}

	private static void validateBatchConfiguration(final ProcessType processingUnit, final Integer batchMinimum,
			final Integer batchMaximum) {
		if (processingUnit == ProcessType.BATCH) {
			if (batchMinimum == null || batchMaximum == null) {
				throw new IllegalArgumentException("batchMinimum and batchMaximum are required for BATCH steps");
			}
			if (batchMinimum.intValue() <= 0) {
				throw new IllegalArgumentException("batchMinimum must be greater than 0");
			}
			if (batchMinimum.intValue() > batchMaximum.intValue()) {
				throw new IllegalArgumentException("batchMinimum must be smaller or equal to batchMaximum");
			}
			return;
		}
		if (batchMinimum != null || batchMaximum != null) {
			throw new IllegalArgumentException("batchMinimum and batchMaximum are only supported for BATCH steps");
		}
	}

	public String getToolGroupName() {
		return this.toolGroupName;
	}

	public ProcessType getProcessingUnit() {
		return this.processingUnit;
	}

	public IValue getProcessingTimeDistribution() {
		return this.processingTimeDistribution;
	}

	public Integer getBatchMinimum() {
		return this.batchMinimum;
	}

	public Integer getBatchMaximum() {
		return this.batchMaximum;
	}
}
