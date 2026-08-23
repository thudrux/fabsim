package de.terministic.fabsim.metamodel.externaldispatch.snapshots;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;

public final class FlowItemInProcessSnapshot extends FlowItemSnapshotBase {
	private final long remainingCycleTime;
	private final long processingTimeLeft;

	FlowItemInProcessSnapshot(final long remainingCycleTime, final long processingTimeLeft,
			final int priority, final long lateness) {
		super(priority, lateness, "");
		this.remainingCycleTime = remainingCycleTime;
		this.processingTimeLeft = processingTimeLeft;
	}

	public static FlowItemInProcessSnapshot capture(final AbstractFlowItem item, final AbstractTool tool,
			final long currentTime, final double projectedCycleTimeFactor) {
		SnapshotCalculations.validateProjectedCycleTimeFactor(projectedCycleTimeFactor);
		final long remainingProcessTime = tool.getToolStateMachine().getRemainingProcessTime(tool);
		final long processingTimeLeft = Math.max(0L, remainingProcessTime);
		return new FlowItemInProcessSnapshot(SnapshotCalculations.calculateRemainingCycleTime(item,
				processingTimeLeft, projectedCycleTimeFactor),
				processingTimeLeft,
				SnapshotCalculations.calculatePriority(item),
				SnapshotCalculations.calculateLateness(item, currentTime));
	}

	public long getRemainingCycleTime() {
		return this.remainingCycleTime;
	}

	public long getProcessingTimeLeft() {
		return this.processingTimeLeft;
	}
}
