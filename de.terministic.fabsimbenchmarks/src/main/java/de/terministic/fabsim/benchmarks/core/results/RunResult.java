package de.terministic.fabsim.benchmarks.core.results;

public final class RunResult {

	private static final double HOURS_PER_DAY = 24.0d;

	private final long simulationTimeHours;
	private final double completedWafersPerDay;
	private final double tardinessPerWaferMinutes;
	private final long completedWafers;
	private final long tardyWafers;
	private final double flowFactor;

	public RunResult(final long simulationTimeHours,
			final long completedWafers, final long tardyWafers,
			final double tardinessPerWaferMinutes, final double flowFactor) {
		this(simulationTimeHours, simulationTimeHours, completedWafers, tardyWafers, tardinessPerWaferMinutes,
				flowFactor);
	}

	public RunResult(final long simulationTimeHours, final long measurementTimeHours,
			final long completedWafers, final long tardyWafers,
			final double tardinessPerWaferMinutes, final double flowFactor) {
		this.simulationTimeHours = simulationTimeHours;
		this.completedWafersPerDay = completedWafers / (measurementTimeHours / HOURS_PER_DAY);
		this.tardinessPerWaferMinutes = tardinessPerWaferMinutes;
		this.completedWafers = completedWafers;
		this.tardyWafers = tardyWafers;
		this.flowFactor = flowFactor;
	}

	public long getSimulationTimeHours() {
		return this.simulationTimeHours;
	}

	public double getCompletedWafersPerDay() {
		return this.completedWafersPerDay;
	}

	public double getTardinessPerWaferMinutes() {
		return this.tardinessPerWaferMinutes;
	}

	public long getCompletedWafers() {
		return this.completedWafers;
	}

	public long getTardyWafers() {
		return this.tardyWafers;
	}

	public double getFlowFactor() {
		return this.flowFactor;
	}
}
