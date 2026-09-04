package de.terministic.fabsim.metamodel.externaldispatch.snapshots;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;
import de.terministic.fabsim.metamodel.components.equipment.ToolGroup;

public class FlowItemQueuedSnapshot extends FlowItemSnapshotBase {
	private final long remainingCycleTime;
	private final long expectedProcessingTime;
	private final long expectedSetupTime;
	private final long timeSinceArrival;

	FlowItemQueuedSnapshot(final long remainingCycleTime, final long expectedProcessingTime,
			final long expectedSetupTime, final long timeSinceArrival, final int priority,
			final long lateness, final String recipe) {
		super(priority, lateness, recipe);
		this.remainingCycleTime = remainingCycleTime;
		this.expectedProcessingTime = expectedProcessingTime;
		this.expectedSetupTime = expectedSetupTime;
		this.timeSinceArrival = timeSinceArrival;
	}

	public static FlowItemQueuedSnapshot capture(final AbstractFlowItem item, final ToolGroup toolGroup,
			final long currentTime) {
		return new FlowItemQueuedSnapshot(SnapshotCalculations.calculateRemainingCycleTime(item),
				SnapshotCalculations.calculateExpectedProcessingTime(item),
				SnapshotCalculations.calculateExpectedSetupTime(toolGroup, item),
				SnapshotCalculations.calculateTimeSinceArrival(item, currentTime),
				SnapshotCalculations.calculatePriority(item),
				SnapshotCalculations.calculateLateness(item, currentTime),
				SnapshotCalculations.calculateRecipe(item));
	}

	public static FlowItemQueuedSnapshot capture(final AbstractFlowItem item, final AbstractTool tool,
			final long currentTime) {
		return new FlowItemQueuedSnapshot(SnapshotCalculations.calculateRemainingCycleTime(item),
				SnapshotCalculations.calculateExpectedProcessingTime(item),
				SnapshotCalculations.calculateExpectedSetupTime(tool, item),
				SnapshotCalculations.calculateTimeSinceArrival(item, currentTime),
				SnapshotCalculations.calculatePriority(item),
				SnapshotCalculations.calculateLateness(item, currentTime),
				SnapshotCalculations.calculateRecipe(item));
	}

	public static FlowItemQueuedSnapshot capture(final AbstractFlowItem item, final long currentTime) {
		return new FlowItemQueuedSnapshot(SnapshotCalculations.calculateRemainingCycleTime(item),
				SnapshotCalculations.calculateExpectedProcessingTime(item), 0L,
				SnapshotCalculations.calculateTimeSinceArrival(item, currentTime),
				SnapshotCalculations.calculatePriority(item),
				SnapshotCalculations.calculateLateness(item, currentTime),
				SnapshotCalculations.calculateRecipe(item));
	}

	public long getRemainingCycleTime() {
		return this.remainingCycleTime;
	}

	public long getExpectedProcessingTime() {
		return this.expectedProcessingTime;
	}

	public long getExpectedSetupTime() {
		return this.expectedSetupTime;
	}

	public long getTimeSinceArrival() {
		return this.timeSinceArrival;
	}
}
