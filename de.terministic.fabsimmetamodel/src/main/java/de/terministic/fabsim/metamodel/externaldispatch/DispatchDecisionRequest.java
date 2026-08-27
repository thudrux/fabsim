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
	private final double leadTimeFactor;

	private DispatchDecisionRequest(final FabStateSnapshot fabState,
			final List<FlowItemQueuedWithIDSnapshot> candidates, final double leadTimeFactor) {
		this.fabState = fabState;
		this.candidates = candidates;
		this.leadTimeFactor = leadTimeFactor;
	}

	public static DispatchDecisionRequest capture(final FabModel model, final AbstractToolGroup toolGroup,
			final AbstractTool tool, final Collection<AbstractFlowItem> candidates,
			final double leadTimeFactor) {
		validateLeadTimeFactor(leadTimeFactor);
		final long simulationTime = model == null || model.getSimulationEngine() == null ? 0L
				: model.getSimulationEngine().getTime();
		final FabStateSnapshot fabState = FabStateSnapshot.capture(model, toolGroup, simulationTime,
				leadTimeFactor);
		final List<FlowItemQueuedWithIDSnapshot> candidateSnapshots = new ArrayList<>();
		if (candidates != null) {
			for (final AbstractFlowItem item : candidates) {
				candidateSnapshots.add(FlowItemQueuedWithIDSnapshot.capture(item, tool, simulationTime,
						leadTimeFactor));
			}
		}
		return new DispatchDecisionRequest(fabState, Collections.unmodifiableList(candidateSnapshots),
				leadTimeFactor);
	}

	public static void validateLeadTimeFactor(final double leadTimeFactor) {
		if (Double.isNaN(leadTimeFactor) || Double.isInfinite(leadTimeFactor)
				|| leadTimeFactor <= 0.0d) {
			throw new IllegalArgumentException("leadTimeFactor must be a positive finite value");
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

	public double getLeadTimeFactor() {
		return this.leadTimeFactor;
	}
}
