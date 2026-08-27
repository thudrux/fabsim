package de.terministic.fabsim.metamodel.externaldispatch.snapshots;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;
import de.terministic.fabsim.metamodel.components.equipment.AbstractToolGroup;
import de.terministic.fabsim.metamodel.components.equipment.ToolGroup;

public final class ToolGroupSnapshot {
	private final String name;
	private final boolean waitingForDispatch;
	private final List<ToolSnapshot> tools;
	private final List<FlowItemQueuedSnapshot> queuedItems;
	private final long totalProjectedTardiness;
	private final long workInProgress;

	ToolGroupSnapshot(final String name, final boolean waitingForDispatch, final List<ToolSnapshot> tools,
			final List<FlowItemQueuedSnapshot> queuedItems, final long totalProjectedTardiness,
			final long workInProgress) {
		this.name = name;
		this.waitingForDispatch = waitingForDispatch;
		this.tools = tools;
		this.queuedItems = queuedItems;
		this.totalProjectedTardiness = totalProjectedTardiness;
		this.workInProgress = workInProgress;
	}

	public static ToolGroupSnapshot capture(final AbstractToolGroup toolGroupBase,
			final boolean waitingForDispatch, final long currentTime, final double leadTimeFactor) {
		SnapshotCalculations.validateLeadTimeFactor(leadTimeFactor);
		final ToolGroup toolGroup = (ToolGroup) toolGroupBase;
		final List<FlowItemQueuedSnapshot> queuedItems = new ArrayList<>();
		for (final AbstractFlowItem item : toolGroup.getQueue()) {
			queuedItems.add(FlowItemQueuedSnapshot.capture(item, toolGroup, currentTime,
					leadTimeFactor));
		}
		final Map<AbstractTool, AbstractFlowItem> itemByTool = new LinkedHashMap<>();
		for (final Map.Entry<AbstractFlowItem, AbstractTool> entry : toolGroup.getInProcessMap().entrySet()) {
			itemByTool.putIfAbsent(entry.getValue(), entry.getKey());
		}
		final List<ToolSnapshot> tools = new ArrayList<>();
		for (final AbstractTool tool : toolGroup.getTools().values()) {
			final AbstractFlowItem item = itemByTool.get(tool);
			final FlowItemInProcessSnapshot inProcessItem = item == null ? null
					: FlowItemInProcessSnapshot.capture(item, tool, currentTime, leadTimeFactor);
			tools.add(ToolSnapshot.capture(tool, inProcessItem));
		}
		long totalProjectedTardiness = 0L;
		for (final AbstractFlowItem item : toolGroup.getQueue()) {
			totalProjectedTardiness += SnapshotCalculations.calculateWaferLevelProjectedTardiness(item,
					currentTime, leadTimeFactor);
		}
		for (final AbstractFlowItem item : itemByTool.values()) {
			totalProjectedTardiness += SnapshotCalculations.calculateWaferLevelProjectedTardiness(item,
					currentTime, leadTimeFactor);
		}
		long workInProgress = 0L;
		for (final AbstractFlowItem item : toolGroup.getQueue()) {
			workInProgress += SnapshotCalculations.calculateWaferLevelWorkInProgress(item);
		}
		for (final AbstractFlowItem item : itemByTool.values()) {
			workInProgress += SnapshotCalculations.calculateWaferLevelWorkInProgress(item);
		}
		return new ToolGroupSnapshot(toolGroup.getName(), waitingForDispatch,
				Collections.unmodifiableList(tools), Collections.unmodifiableList(queuedItems),
				totalProjectedTardiness, workInProgress);
	}

	public String getName() {
		return this.name;
	}

	public boolean getWaitingForDispatch() {
		return this.waitingForDispatch;
	}

	public List<ToolSnapshot> getTools() {
		return this.tools;
	}

	public List<FlowItemQueuedSnapshot> getQueuedItems() {
		return this.queuedItems;
	}

	long getTotalProjectedTardiness() {
		return this.totalProjectedTardiness;
	}

	long getWorkInProgress() {
		return this.workInProgress;
	}
}
