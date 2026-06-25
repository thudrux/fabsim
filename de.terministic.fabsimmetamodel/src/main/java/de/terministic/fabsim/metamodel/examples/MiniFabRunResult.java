package de.terministic.fabsim.metamodel.examples;

public final class MiniFabRunResult {

	private final long simulationTimeHours;
	private final long simulationTimeMillis;
	private final long throughput;
	private final long tardyWafers;
	private final long totalWeightedTardiness;

	public MiniFabRunResult(final long simulationTimeHours, final long simulationTimeMillis, final long throughput,
			final long tardyWafers, final long totalWeightedTardiness) {
		this.simulationTimeHours = simulationTimeHours;
		this.simulationTimeMillis = simulationTimeMillis;
		this.throughput = throughput;
		this.tardyWafers = tardyWafers;
		this.totalWeightedTardiness = totalWeightedTardiness;
	}

	public long getSimulationTimeHours() {
		return this.simulationTimeHours;
	}

	public long getSimulationTimeMillis() {
		return this.simulationTimeMillis;
	}

	public long getThroughput() {
		return this.throughput;
	}

	public long getFinishedWafers() {
		return this.throughput;
	}

	public long getTardyWafers() {
		return this.tardyWafers;
	}

	public long getTotalWeightedTardiness() {
		return this.totalWeightedTardiness;
	}
}
