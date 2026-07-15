package de.terministic.fabsim.metamodel.examples;

public final class MiniFabRunResult {

	private static final double HOURS_PER_DAY = 24.0d;

	private final long runs;
	private final double simulationTimeHoursMean;
	private final double completedWafersPerDayMean;
	private final double completedWafersPerDayStdDev;
	private final double tardinessPerWaferHoursMean;
	private final double tardinessPerWaferHoursStdDev;
	private final double completedWafersMean;
	private final double completedWafersStdDev;
	private final double tardyWafersMean;
	private final double tardyWafersStdDev;
	private final double flowFactorMean;
	private final double flowFactorStdDev;

	public MiniFabRunResult(final long simulationTimeHours,
			final long completedWafers, final long tardyWafers,
			final double tardinessPerWaferHours, final double flowFactorMean) {
		this(simulationTimeHours, simulationTimeHours, completedWafers, tardyWafers, tardinessPerWaferHours,
				flowFactorMean);
	}

	public MiniFabRunResult(final long simulationTimeHours, final long measurementTimeHours,
			final long completedWafers, final long tardyWafers,
			final double tardinessPerWaferHours, final double flowFactorMean) {
		this(1L, simulationTimeHours,
				completedWafers / (measurementTimeHours / HOURS_PER_DAY), 0.0d,
				tardinessPerWaferHours, 0.0d, completedWafers, 0.0d, tardyWafers, 0.0d,
				flowFactorMean, 0.0d);
	}

	public MiniFabRunResult(final long runs, final double simulationTimeHoursMean,
			final double completedWafersPerDayMean,
			final double completedWafersPerDayStdDev, final double tardinessPerWaferHoursMean,
			final double tardinessPerWaferHoursStdDev, final double completedWafersMean,
			final double completedWafersStdDev, final double tardyWafersMean, final double tardyWafersStdDev,
			final double flowFactorMean, final double flowFactorStdDev) {
		this.runs = runs;
		this.simulationTimeHoursMean = simulationTimeHoursMean;
		this.completedWafersPerDayMean = completedWafersPerDayMean;
		this.completedWafersPerDayStdDev = completedWafersPerDayStdDev;
		this.tardinessPerWaferHoursMean = tardinessPerWaferHoursMean;
		this.tardinessPerWaferHoursStdDev = tardinessPerWaferHoursStdDev;
		this.completedWafersMean = completedWafersMean;
		this.completedWafersStdDev = completedWafersStdDev;
		this.tardyWafersMean = tardyWafersMean;
		this.tardyWafersStdDev = tardyWafersStdDev;
		this.flowFactorMean = flowFactorMean;
		this.flowFactorStdDev = flowFactorStdDev;
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

	public double getCompletedWafersPerDay() {
		return this.completedWafersPerDayMean;
	}

	public double getCompletedWafersPerDayMean() {
		return this.completedWafersPerDayMean;
	}

	public double getCompletedWafersPerDayStdDev() {
		return this.completedWafersPerDayStdDev;
	}

	public double getTardinessPerWaferHours() {
		return this.tardinessPerWaferHoursMean;
	}

	public double getTardinessPerWaferHoursMean() {
		return this.tardinessPerWaferHoursMean;
	}

	public double getTardinessPerWaferHoursStdDev() {
		return this.tardinessPerWaferHoursStdDev;
	}

	public long getCompletedWafers() {
		return Math.round(this.completedWafersMean);
	}

	public double getCompletedWafersMean() {
		return this.completedWafersMean;
	}

	public double getCompletedWafersStdDev() {
		return this.completedWafersStdDev;
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

	public double getFlowFactor() {
		return this.flowFactorMean;
	}

	public double getFlowFactorMean() {
		return this.flowFactorMean;
	}

	public double getFlowFactorStdDev() {
		return this.flowFactorStdDev;
	}
}
