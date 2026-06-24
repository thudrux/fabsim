package de.terministic.fabsim.metamodel.statistics;

import java.util.HashMap;
import java.util.Map;

import de.terministic.fabsim.core.ISimEvent;
import de.terministic.fabsim.core.SimEventListener;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.components.FlowItemDestructionEvent;
import de.terministic.fabsim.metamodel.components.Lot;

public class FinishedLotStatisticsCollector extends SimEventListener {

	private final FabModel fabModel;
	private final Map<Integer, Integer> priorityWeights;
	private long finishedLots;
	private long tardyLots;
	private long weightedTardiness;

	public FinishedLotStatisticsCollector(final FabModel fabModel, final Map<Integer, Integer> priorityWeights) {
		if (fabModel == null) {
			throw new IllegalArgumentException("fabModel must not be null");
		}
		if (priorityWeights == null) {
			throw new IllegalArgumentException("priorityWeights must not be null");
		}
		this.fabModel = fabModel;
		this.priorityWeights = new HashMap<>(priorityWeights);
	}

	@Override
	public void processEvent(final ISimEvent event) {
		if (!(event instanceof FlowItemDestructionEvent)) {
			return;
		}
		if (!(event.getFlowItem() instanceof Lot)) {
			return;
		}

		final Lot lot = (Lot) event.getFlowItem();
		final long tardiness = Math.max(0L, event.getEventTime() - lot.getDueDate());
		final int weight = getPriorityWeight(lot.getPrio());
		this.finishedLots++;
		if (tardiness > 0L) {
			this.tardyLots++;
		}
		this.weightedTardiness += tardiness * weight;
	}

	public long getFinishedWafers() {
		return this.finishedLots * (long) this.fabModel.getLotSize();
	}

	public long getTotalWeightedTardiness() {
		return this.weightedTardiness;
	}

	public long getTardyWafers() {
		return this.tardyLots * (long) this.fabModel.getLotSize();
	}

	private int getPriorityWeight(final int priority) {
		final Integer weight = this.priorityWeights.get(priority);
		if (weight != null) {
			return weight;
		}
		return 1;
	}
}
