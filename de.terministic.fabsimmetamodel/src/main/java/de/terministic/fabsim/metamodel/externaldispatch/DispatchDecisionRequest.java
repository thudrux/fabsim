package de.terministic.fabsim.metamodel.externaldispatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;
import de.terministic.fabsim.metamodel.components.equipment.AbstractToolGroup;
import de.terministic.fabsim.metamodel.externaldispatch.snapshots.FabStateSnapshot;
import de.terministic.fabsim.metamodel.externaldispatch.snapshots.FlowItemQueuedWithIDSnapshot;

public final class DispatchDecisionRequest {

	private final FabStateSnapshot fabState;
	private final List<FlowItemQueuedWithIDSnapshot> candidates;
	private final double projectedCycleTimeFactor;

	private DispatchDecisionRequest(final FabStateSnapshot fabState,
			final List<FlowItemQueuedWithIDSnapshot> candidates, final double projectedCycleTimeFactor) {
		this.fabState = fabState;
		this.candidates = candidates;
		this.projectedCycleTimeFactor = projectedCycleTimeFactor;
	}

	public static DispatchDecisionRequest capture(final FabModel model, final AbstractToolGroup toolGroup,
			final AbstractTool tool, final Collection<AbstractFlowItem> candidates,
			final double projectedCycleTimeFactor) {
		validateProjectedCycleTimeFactor(projectedCycleTimeFactor);
		final long simulationTime = model == null || model.getSimulationEngine() == null ? 0L
				: model.getSimulationEngine().getTime();
		final FabStateSnapshot fabState = FabStateSnapshot.capture(model, toolGroup, simulationTime,
				projectedCycleTimeFactor);
		final List<FlowItemQueuedWithIDSnapshot> candidateSnapshots = new ArrayList<>();
		if (candidates != null) {
			for (final AbstractFlowItem item : candidates) {
				candidateSnapshots.add(FlowItemQueuedWithIDSnapshot.capture(item, tool, simulationTime,
						projectedCycleTimeFactor));
			}
		}
		return new DispatchDecisionRequest(fabState, Collections.unmodifiableList(candidateSnapshots),
				projectedCycleTimeFactor);
	}

	public static void validateProjectedCycleTimeFactor(final double projectedCycleTimeFactor) {
		if (Double.isNaN(projectedCycleTimeFactor) || Double.isInfinite(projectedCycleTimeFactor)
				|| projectedCycleTimeFactor <= 0.0d) {
			throw new IllegalArgumentException("projectedCycleTimeFactor must be a positive finite value");
		}
	}

	public FabStateSnapshot getFabState() {
		return this.fabState;
	}

	public List<FlowItemQueuedWithIDSnapshot> getCandidates() {
		return this.candidates;
	}

	public long getSimulationTime() {
		return this.fabState == null ? 0L : this.fabState.getSimulationTime();
	}

	public double getProjectedCycleTimeFactor() {
		return this.projectedCycleTimeFactor;
	}
}
