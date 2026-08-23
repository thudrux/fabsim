package de.terministic.fabsim.metamodel.externaldispatch.snapshots;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;

public final class FlowItemQueuedWithIDSnapshot extends FlowItemQueuedSnapshot {
	private final long id;

	FlowItemQueuedWithIDSnapshot(final long id, final long remainingCycleTime,
			final long processingTime, final long expectedSetupTime, final long timeSinceArrival,
			final int priority, final long lateness, final String recipe) {
		super(remainingCycleTime, processingTime, expectedSetupTime, timeSinceArrival, priority, lateness,
				recipe);
		this.id = id;
	}

	public static FlowItemQueuedWithIDSnapshot capture(final AbstractFlowItem item, final AbstractTool tool,
			final long currentTime, final double projectedCycleTimeFactor) {
		SnapshotCalculations.validateProjectedCycleTimeFactor(projectedCycleTimeFactor);
		return new FlowItemQueuedWithIDSnapshot(item.getId(),
				SnapshotCalculations.calculateRemainingCycleTime(item, projectedCycleTimeFactor),
				SnapshotCalculations.calculateProcessingTime(item),
				SnapshotCalculations.calculateExpectedSetupTime(tool, item),
				SnapshotCalculations.calculateTimeSinceArrival(item, currentTime),
				SnapshotCalculations.calculatePriority(item),
				SnapshotCalculations.calculateLateness(item, currentTime),
				SnapshotCalculations.calculateRecipe(item));
	}

	public long getId() {
		return this.id;
	}
}
