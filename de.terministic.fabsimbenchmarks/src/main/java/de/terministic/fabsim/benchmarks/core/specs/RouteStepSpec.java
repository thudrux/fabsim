package de.terministic.fabsim.benchmarks.core.specs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.terministic.fabsim.core.duration.IValue;
import de.terministic.fabsim.metamodel.components.ProcessStep.ProcessType;

public final class RouteStepSpec {
	private final String toolGroupName;
	private final ProcessType processingUnit;
	private final IValue processingTimeDistribution;
	private final Integer batchMinimum;
	private final Integer batchMaximum;
	private final String setupStateName;
	private final List<SetupTimeSpec> setupTimeSpecs;

	public RouteStepSpec(final String toolGroupName, final ProcessType processingUnit,
			final IValue processingTimeDistribution) {
		this(toolGroupName, processingUnit, processingTimeDistribution, (Integer) null, (Integer) null);
	}

	public RouteStepSpec(final String toolGroupName, final ProcessType processingUnit,
			final IValue processingTimeDistribution, final Integer batchMinimum, final Integer batchMaximum) {
		this(toolGroupName, processingUnit, processingTimeDistribution, batchMinimum, batchMaximum, null,
				Collections.<SetupTimeSpec>emptyList());
	}

	public RouteStepSpec(final String toolGroupName, final ProcessType processingUnit,
			final IValue processingTimeDistribution, final String setupStateName,
			final List<SetupTimeSpec> setupTimeSpecs) {
		this(toolGroupName, processingUnit, processingTimeDistribution, null, null, setupStateName, setupTimeSpecs);
	}

	public RouteStepSpec(final String toolGroupName, final ProcessType processingUnit,
			final IValue processingTimeDistribution, final Integer batchMinimum, final Integer batchMaximum,
			final String setupStateName, final List<SetupTimeSpec> setupTimeSpecs) {
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
		this.setupStateName = normalizeSetupStateName(setupStateName);
		this.setupTimeSpecs = copySetupTimeSpecs(setupTimeSpecs);
	}

	private static String normalizeSetupStateName(final String setupStateName) {
		if (setupStateName == null || setupStateName.trim().isEmpty()) {
			return null;
		}
		return setupStateName.trim();
	}

	private static List<SetupTimeSpec> copySetupTimeSpecs(
			final List<SetupTimeSpec> setupTimeSpecs) {
		if (setupTimeSpecs == null || setupTimeSpecs.isEmpty()) {
			return Collections.emptyList();
		}
		final ArrayList<SetupTimeSpec> copy = new ArrayList<>(setupTimeSpecs.size());
		for (final SetupTimeSpec setupTimeSpec : setupTimeSpecs) {
			if (setupTimeSpec == null) {
				throw new IllegalArgumentException("setupTimeSpecs must not contain null entries");
			}
			copy.add(setupTimeSpec);
		}
		return Collections.unmodifiableList(copy);
	}

	private static void validateBatchConfiguration(final ProcessType processingUnit, final Integer batchMinimum,
			final Integer batchMaximum) {
		if (processingUnit == ProcessType.BATCH) {
			if (batchMinimum == null || batchMaximum == null) {
				throw new IllegalArgumentException("batchMinimum and batchMaximum are required for BATCH steps");
			}
			if (batchMinimum <= 0) {
				throw new IllegalArgumentException("batchMinimum must be greater than 0");
			}
			if (batchMinimum > batchMaximum) {
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

	public String getSetupStateName() {
		return this.setupStateName;
	}

	public List<SetupTimeSpec> getSetupTimeSpecs() {
		return this.setupTimeSpecs;
	}
}
