package de.terministic.fabsim.metamodel.externaldispatch.snapshots;

import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;

public final class ToolSnapshot {
	private final long id;
	private final String currentToolState;

	ToolSnapshot(final long id, final String currentToolState) {
		this.id = id;
		this.currentToolState = currentToolState;
	}

	public static ToolSnapshot capture(final AbstractTool tool) {
		return new ToolSnapshot(tool.getId(), tool.getCurrentToolState() == null ? ""
				: tool.getCurrentToolState().name());
	}

	public long getId() {
		return this.id;
	}

	public String getCurrentToolState() {
		return this.currentToolState;
	}
}
