package de.terministic.fabsim.metamodel.externaldispatch.snapshots;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.components.equipment.AbstractToolGroup;

public final class FabStateSnapshot {
	private final long simulationTime;
	private final List<ToolGroupSnapshot> toolGroups;
	private final CostSnapshot costSnapshot;

	FabStateSnapshot(final long simulationTime, final List<ToolGroupSnapshot> toolGroups,
			final CostSnapshot costSnapshot) {
		this.simulationTime = simulationTime;
		this.toolGroups = toolGroups;
		this.costSnapshot = costSnapshot;
	}

	public static FabStateSnapshot capture(final FabModel model, final AbstractToolGroup selectedToolGroup,
			final long currentTime, final double projectedCycleTimeFactor) {
		SnapshotCalculations.validateProjectedCycleTimeFactor(projectedCycleTimeFactor);
		final List<ToolGroupSnapshot> toolGroups = new ArrayList<>();
		long totalProjectedTardiness = 0L;
		long workInProgress = 0L;
		if (model != null) {
			for (final AbstractToolGroup groupBase : model.getToolGroups().values()) {
				final boolean waitingForDispatch = selectedToolGroup != null
						&& groupBase.getId() == selectedToolGroup.getId();
				final ToolGroupSnapshot toolGroup = ToolGroupSnapshot.capture(groupBase, waitingForDispatch,
						currentTime, projectedCycleTimeFactor);
				toolGroups.add(toolGroup);
				totalProjectedTardiness += toolGroup.getTotalProjectedTardiness();
				workInProgress += toolGroup.getWorkInProgress();
			}
		}
		return new FabStateSnapshot(currentTime, Collections.unmodifiableList(toolGroups),
				new CostSnapshot(totalProjectedTardiness, workInProgress));
	}

	public long getSimulationTime() {
		return this.simulationTime;
	}

	public List<ToolGroupSnapshot> getToolGroups() {
		return this.toolGroups;
	}

	public CostSnapshot getCostSnapshot() {
		return this.costSnapshot;
	}
}
