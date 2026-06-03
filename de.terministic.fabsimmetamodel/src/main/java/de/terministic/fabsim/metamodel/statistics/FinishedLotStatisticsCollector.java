package de.terministic.fabsim.metamodel.statistics;

import java.util.HashMap;
import java.util.Map;

import de.terministic.fabsim.core.ISimEvent;
import de.terministic.fabsim.core.SimEventListener;
import de.terministic.fabsim.metamodel.components.FlowItemDestructionEvent;
import de.terministic.fabsim.metamodel.components.Lot;

public class FinishedLotStatisticsCollector extends SimEventListener {

	private final Map<Integer, Integer> priorityWeights;
	private long finishedLots;
	private long weightedTardinessNumerator;
	private long totalPriorityWeight;

	public FinishedLotStatisticsCollector(final Map<Integer, Integer> priorityWeights) {
		if (priorityWeights == null) {
			throw new IllegalArgumentException("priorityWeights must not be null");
		}
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
		this.totalPriorityWeight += weight;
		this.weightedTardinessNumerator += tardiness * weight;
	}

	public long getFinishedLots() {
		return this.finishedLots;
	}

	public double getWeightedTardiness() {
		if (this.totalPriorityWeight == 0L) {
			return 0.0d;
		}
		return this.weightedTardinessNumerator / (double) this.totalPriorityWeight;
	}

	private int getPriorityWeight(final int priority) {
		final Integer weight = this.priorityWeights.get(Integer.valueOf(priority));
		if (weight != null) {
			return weight.intValue();
		}
		return 1;
	}
}
