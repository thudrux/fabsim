package de.terministic.fabsim.metamodel.statistics;

import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;
import de.terministic.fabsim.metamodel.components.equipment.SemiE10EquipmentState;
import de.terministic.fabsim.metamodel.components.equipment.SetupState;

public class ToolStateLogEntry {
	private long time;
	private AbstractTool tool;
	private SemiE10EquipmentState newState;
	private SetupState setupState;

	public ToolStateLogEntry(final long time, final AbstractTool tool, final SemiE10EquipmentState newState,
			final SetupState setupState) {
		this.time = time;
		this.tool = tool;
		this.newState = newState;
		this.setSetupState(setupState);
	}

	public SemiE10EquipmentState getNewState() {
		return this.newState;
	}

	public SetupState getSetupState() {
		return this.setupState;
	}

	public long getTime() {
		return this.time;
	}

	public AbstractTool getTool() {
		return this.tool;
	}

	public void setNewState(final SemiE10EquipmentState newState) {
		this.newState = newState;
	}

	public void setSetupState(final SetupState setupState) {
		this.setupState = setupState;
	}

	public void setTime(final long time) {
		this.time = time;
	}

	public void setTool(final AbstractTool tool) {
		this.tool = tool;
	}

	@Override
	public String toString() {
		return "[" + this.time + "]_" + this.tool.getName() + "=>" + this.newState;
	}
}
