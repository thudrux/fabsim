package de.terministic.fabsim.benchmarks.core;

import de.terministic.fabsim.core.ISimEvent;
import de.terministic.fabsim.core.SimEventListener;
import de.terministic.fabsim.metamodel.components.FlowItemDestructionEvent;
import de.terministic.fabsim.metamodel.components.Lot;

public final class BenchmarkFabStatistics extends SimEventListener {

	private static final double HOURS_PER_DAY = 24.0d;
	private static final double MILLIS_PER_MINUTE = 60.0d * 1000.0d;

	private final long simulationTimeHours;
	private final long measurementTimeHours;
	private final long warmupTimeMillis;
	private long completedWafers;
	private double tardinessMillis;
	private long flowFactorSamples;
	private double flowFactorSum;

	public BenchmarkFabStatistics(final long simulationTimeHours, final long measurementTimeHours,
			final long warmupTimeMillis) {
		if (simulationTimeHours <= 0L) {
			throw new IllegalArgumentException("simulationTimeHours must be a positive number");
		}
		if (measurementTimeHours <= 0L) {
			throw new IllegalArgumentException("measurementTimeHours must be a positive number");
		}
		if (warmupTimeMillis < 0L) {
			throw new IllegalArgumentException("warmupTimeMillis must not be negative");
		}
		this.simulationTimeHours = simulationTimeHours;
		this.measurementTimeHours = measurementTimeHours;
		this.warmupTimeMillis = warmupTimeMillis;
	}

	@Override
	public void processEvent(final ISimEvent event) {
		if (!(event instanceof FlowItemDestructionEvent)) {
			return;
		}
		if (!(event.getFlowItem() instanceof Lot)) {
			return;
		}
		if (event.getEventTime() < this.warmupTimeMillis) {
			return;
		}

		final Lot lot = (Lot) event.getFlowItem();
		final long tardiness = Math.max(0L, event.getEventTime() - lot.getDueDate());
		final int waferCount = lot.getSize();
		final long flowTime = Math.max(0L, event.getEventTime() - lot.getCreationTime());
		final long processingTime = lot.getRPT();
		this.completedWafers += waferCount;
		this.tardinessMillis += tardiness * (double) lot.getPrio();
		if (processingTime > 0L) {
			final double flowFactor = flowTime / (double) processingTime;
			this.flowFactorSamples++;
			this.flowFactorSum += flowFactor;
		}
	}

	public long getSimulationTimeHours() {
		return this.simulationTimeHours;
	}

	public double getCompletedWafersPerDay() {
		return this.completedWafers / (this.measurementTimeHours / HOURS_PER_DAY);
	}

	public double getTardinessPerWaferMinutes() {
		if (this.completedWafers == 0L) {
			return 0.0d;
		}
		return (this.tardinessMillis / MILLIS_PER_MINUTE) / this.completedWafers;
	}

	public double getFlowFactor() {
		if (this.flowFactorSamples == 0L) {
			return 0.0d;
		}
		return this.flowFactorSum / this.flowFactorSamples;
	}
}
