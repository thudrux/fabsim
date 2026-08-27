package de.terministic.fabsim.metamodel.externaldispatch.snapshots;

import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;

public final class ToolSnapshot {
	private final long id;
	private final String currentToolState;
	private final FlowItemInProcessSnapshot inProcessItem;

	ToolSnapshot(final long id, final String currentToolState, final FlowItemInProcessSnapshot inProcessItem) {
		this.id = id;
		this.currentToolState = currentToolState;
		this.inProcessItem = inProcessItem;
	}

	public static ToolSnapshot capture(final AbstractTool tool) {
		return capture(tool, null);
	}

	public static ToolSnapshot capture(final AbstractTool tool, final FlowItemInProcessSnapshot inProcessItem) {
		return new ToolSnapshot(tool.getId(), tool.getCurrentToolState() == null ? ""
				: tool.getCurrentToolState().name(), inProcessItem);
	}

	public long getId() {
		return this.id;
	}

	public String getCurrentToolState() {
		return this.currentToolState;
	}

	public FlowItemInProcessSnapshot getInProcessItem() {
		return this.inProcessItem;
	}
}
