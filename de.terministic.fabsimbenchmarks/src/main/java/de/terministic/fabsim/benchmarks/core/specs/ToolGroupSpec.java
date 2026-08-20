package de.terministic.fabsim.benchmarks.core.specs;

import de.terministic.fabsim.metamodel.components.equipment.AbstractHomogeneousResourceGroup.ProcessingType;

public final class ToolGroupSpec {
	private final String name;
	private final int numberOfTools;
	private final ProcessingType processingType;
	private final long loadTime;
	private final long unloadTime;
	private final MaintenanceSpec maintenance;
	private final BreakdownSpec breakdown;

	public ToolGroupSpec(final String name, final int numberOfTools, final ProcessingType processingType,
			final long loadTime, final long unloadTime, final MaintenanceSpec maintenance) {
		this(name, numberOfTools, processingType, loadTime, unloadTime, maintenance, null);
	}

	public ToolGroupSpec(final String name, final int numberOfTools, final ProcessingType processingType,
			final long loadTime, final long unloadTime, final MaintenanceSpec maintenance,
			final BreakdownSpec breakdown) {
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("name must not be blank");
		}
		if (numberOfTools <= 0) {
			throw new IllegalArgumentException("numberOfTools must be greater than 0");
		}
		if (processingType == null) {
			throw new IllegalArgumentException("processingType must not be null");
		}
		if (loadTime < 0L) {
			throw new IllegalArgumentException("loadTime must not be negative");
		}
		if (unloadTime < 0L) {
			throw new IllegalArgumentException("unloadTime must not be negative");
		}
		if (maintenance == null) {
			throw new IllegalArgumentException("maintenance must not be null");
		}
		this.name = name;
		this.numberOfTools = numberOfTools;
		this.processingType = processingType;
		this.loadTime = loadTime;
		this.unloadTime = unloadTime;
		this.maintenance = maintenance;
		this.breakdown = breakdown;
	}

	public String getName() {
		return this.name;
	}

	public int getNumberOfTools() {
		return this.numberOfTools;
	}

	public ProcessingType getProcessingType() {
		return this.processingType;
	}

	public long getLoadTime() {
		return this.loadTime;
	}

	public long getUnloadTime() {
		return this.unloadTime;
	}

	public MaintenanceSpec getMaintenance() {
		return this.maintenance;
	}

	public BreakdownSpec getBreakdown() {
		return this.breakdown;
	}
}
