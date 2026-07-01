package de.terministic.fabsim.metamodel.examples;

public final class MiniFabRunResult {

	private final long runs;
	private final double simulationTimeHoursMean;
	private final double simulationTimeMillisMean;
	private final double throughputMean;
	private final double throughputStdDev;
	private final double tardyWafersMean;
	private final double tardyWafersStdDev;
	private final double totalWeightedTardinessMean;
	private final double totalWeightedTardinessStdDev;

	public MiniFabRunResult(final long simulationTimeHours, final long simulationTimeMillis, final long throughput,
			final long tardyWafers, final long totalWeightedTardiness) {
		this(1L, simulationTimeHours, simulationTimeMillis, throughput, 0.0d, tardyWafers, 0.0d,
				totalWeightedTardiness, 0.0d);
	}

	public MiniFabRunResult(final long runs, final double simulationTimeHoursMean,
			final double simulationTimeMillisMean, final double throughputMean, final double throughputStdDev,
			final double tardyWafersMean, final double tardyWafersStdDev, final double totalWeightedTardinessMean,
			final double totalWeightedTardinessStdDev) {
		this.runs = runs;
		this.simulationTimeHoursMean = simulationTimeHoursMean;
		this.simulationTimeMillisMean = simulationTimeMillisMean;
		this.throughputMean = throughputMean;
		this.throughputStdDev = throughputStdDev;
		this.tardyWafersMean = tardyWafersMean;
		this.tardyWafersStdDev = tardyWafersStdDev;
		this.totalWeightedTardinessMean = totalWeightedTardinessMean;
		this.totalWeightedTardinessStdDev = totalWeightedTardinessStdDev;
	}

	public long getRuns() {
		return this.runs;
	}

	public long getSimulationTimeHours() {
		return Math.round(this.simulationTimeHoursMean);
	}

	public double getSimulationTimeHoursMean() {
		return this.simulationTimeHoursMean;
	}

	public long getSimulationTimeMillis() {
		return Math.round(this.simulationTimeMillisMean);
	}

	public double getSimulationTimeMillisMean() {
		return this.simulationTimeMillisMean;
	}

	public long getThroughput() {
		return Math.round(this.throughputMean);
	}

	public double getThroughputMean() {
		return this.throughputMean;
	}

	public double getThroughputStdDev() {
		return this.throughputStdDev;
	}

	public long getFinishedWafers() {
		return getThroughput();
	}

	public double getFinishedWafersMean() {
		return getThroughputMean();
	}

	public double getFinishedWafersStdDev() {
		return getThroughputStdDev();
	}

	public long getTardyWafers() {
		return Math.round(this.tardyWafersMean);
	}

	public double getTardyWafersMean() {
		return this.tardyWafersMean;
	}

	public double getTardyWafersStdDev() {
		return this.tardyWafersStdDev;
	}

	public long getTotalWeightedTardiness() {
		return Math.round(this.totalWeightedTardinessMean);
	}

	public double getTotalWeightedTardinessMean() {
		return this.totalWeightedTardinessMean;
	}

	public double getTotalWeightedTardinessStdDev() {
		return this.totalWeightedTardinessStdDev;
	}
}
