package de.terministic.fabsim.metamodel.externaldispatch.snapshots;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.components.equipment.AbstractToolGroup;

public final class FabStateSnapshot {
	private final long simulationTime;
	private final List<ToolGroupSnapshot> toolGroups;

	FabStateSnapshot(final long simulationTime, final List<ToolGroupSnapshot> toolGroups) {
		this.simulationTime = simulationTime;
		this.toolGroups = toolGroups;
	}

	public static FabStateSnapshot capture(final FabModel model, final AbstractToolGroup selectedToolGroup,
			final long currentTime) {
		final List<ToolGroupSnapshot> toolGroups = new ArrayList<>();
		if (model != null) {
			for (final AbstractToolGroup groupBase : model.getToolGroups().values()) {
				final boolean waitingForDispatch = selectedToolGroup != null
						&& groupBase.getId() == selectedToolGroup.getId();
				final ToolGroupSnapshot toolGroup = ToolGroupSnapshot.capture(groupBase, waitingForDispatch,
						currentTime);
				toolGroups.add(toolGroup);
			}
		}
		return new FabStateSnapshot(currentTime, Collections.unmodifiableList(toolGroups));
	}

	public long getSimulationTime() {
		return this.simulationTime;
	}

	public List<ToolGroupSnapshot> getToolGroups() {
		return this.toolGroups;
	}
}
