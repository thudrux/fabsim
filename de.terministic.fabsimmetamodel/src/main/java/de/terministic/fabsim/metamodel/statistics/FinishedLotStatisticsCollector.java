package de.terministic.fabsim.metamodel.statistics;

import de.terministic.fabsim.core.ISimEvent;
import de.terministic.fabsim.core.SimEventListener;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.components.FlowItemDestructionEvent;
import de.terministic.fabsim.metamodel.components.Lot;

public class FinishedLotStatisticsCollector extends SimEventListener {

	private final FabModel fabModel;
	private final long warmupTimeMillis;
	private long finishedLots;
	private long tardyLots;
	private long weightedTardiness;
	private long flowFactorSamples;
	private double flowFactorSum;

	public FinishedLotStatisticsCollector(final FabModel fabModel) {
		this(fabModel, 0L);
	}

	public FinishedLotStatisticsCollector(final FabModel fabModel, final long warmupTimeMillis) {
		if (fabModel == null) {
			throw new IllegalArgumentException("fabModel must not be null");
		}
		if (warmupTimeMillis < 0L) {
			throw new IllegalArgumentException("warmupTimeMillis must not be negative");
		}
		this.fabModel = fabModel;
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
		final int weight = getPriorityWeight(lot.getPrio());
		final long flowTime = Math.max(0L, event.getEventTime() - lot.getCreationTime());
		final long processingTime = lot.getRPT();
		this.finishedLots++;
		if (tardiness > 0L) {
			this.tardyLots++;
		}
		this.weightedTardiness += tardiness * weight;
		if (processingTime > 0L) {
			final double flowFactor = flowTime / (double) processingTime;
			this.flowFactorSamples++;
			this.flowFactorSum += flowFactor;
		}
	}

	public long getFinishedWafers() {
		return this.finishedLots * (long) this.fabModel.getLotSize();
	}

	public long getCompletedLots() {
		return this.finishedLots;
	}

	public long getTotalWeightedTardiness() {
		return this.weightedTardiness;
	}

	public long getTardyWafers() {
		return this.tardyLots * (long) this.fabModel.getLotSize();
	}

	public long getTardyLots() {
		return this.tardyLots;
	}

	public double getTardinessPerWaferMinutes() {
		if (this.finishedLots == 0L) {
			return 0.0d;
		}
		final double tardinessMinutes = this.weightedTardiness / (double) (60L * 1000L);
		return tardinessMinutes / (this.finishedLots * (double) this.fabModel.getLotSize());
	}

	public double getFlowFactorMean() {
		if (this.flowFactorSamples == 0L) {
			return 0.0d;
		}
		return this.flowFactorSum / this.flowFactorSamples;
	}

	private int getPriorityWeight(final int priority) {
		return priority;
	}
}
