package de.terministic.fabsim.metamodel.externaldispatch.snapshots;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;
import de.terministic.fabsim.metamodel.components.equipment.ToolGroup;

public class FlowItemQueuedSnapshot extends FlowItemSnapshotBase {
	private final long remainingCycleTime;
	private final long processingTime;
	private final long expectedSetupTime;
	private final long timeSinceArrival;

	FlowItemQueuedSnapshot(final long remainingCycleTime, final long processingTime,
			final long expectedSetupTime, final long timeSinceArrival, final int priority,
			final long lateness, final String recipe) {
		super(priority, lateness, recipe);
		this.remainingCycleTime = remainingCycleTime;
		this.processingTime = processingTime;
		this.expectedSetupTime = expectedSetupTime;
		this.timeSinceArrival = timeSinceArrival;
	}

	public static FlowItemQueuedSnapshot capture(final AbstractFlowItem item, final ToolGroup toolGroup,
			final long currentTime, final double projectedCycleTimeFactor) {
		SnapshotCalculations.validateProjectedCycleTimeFactor(projectedCycleTimeFactor);
		return new FlowItemQueuedSnapshot(SnapshotCalculations.calculateRemainingCycleTime(item,
				projectedCycleTimeFactor),
				SnapshotCalculations.calculateProcessingTime(item),
				SnapshotCalculations.calculateExpectedSetupTime(toolGroup, item),
				SnapshotCalculations.calculateTimeSinceArrival(item, currentTime),
				SnapshotCalculations.calculatePriority(item),
				SnapshotCalculations.calculateLateness(item, currentTime),
				SnapshotCalculations.calculateRecipe(item));
	}

	public static FlowItemQueuedSnapshot capture(final AbstractFlowItem item, final AbstractTool tool,
			final long currentTime, final double projectedCycleTimeFactor) {
		SnapshotCalculations.validateProjectedCycleTimeFactor(projectedCycleTimeFactor);
		return new FlowItemQueuedSnapshot(SnapshotCalculations.calculateRemainingCycleTime(item,
				projectedCycleTimeFactor),
				SnapshotCalculations.calculateProcessingTime(item),
				SnapshotCalculations.calculateExpectedSetupTime(tool, item),
				SnapshotCalculations.calculateTimeSinceArrival(item, currentTime),
				SnapshotCalculations.calculatePriority(item),
				SnapshotCalculations.calculateLateness(item, currentTime),
				SnapshotCalculations.calculateRecipe(item));
	}

	public static FlowItemQueuedSnapshot capture(final AbstractFlowItem item, final long currentTime,
			final double projectedCycleTimeFactor) {
		SnapshotCalculations.validateProjectedCycleTimeFactor(projectedCycleTimeFactor);
		return new FlowItemQueuedSnapshot(SnapshotCalculations.calculateRemainingCycleTime(item,
				projectedCycleTimeFactor),
				SnapshotCalculations.calculateProcessingTime(item), 0L,
				SnapshotCalculations.calculateTimeSinceArrival(item, currentTime),
				SnapshotCalculations.calculatePriority(item),
				SnapshotCalculations.calculateLateness(item, currentTime),
				SnapshotCalculations.calculateRecipe(item));
	}

	public long getRemainingCycleTime() {
		return this.remainingCycleTime;
	}

	public long getProcessingTime() {
		return this.processingTime;
	}

	public long getExpectedSetupTime() {
		return this.expectedSetupTime;
	}

	public long getTimeSinceArrival() {
		return this.timeSinceArrival;
	}
}
