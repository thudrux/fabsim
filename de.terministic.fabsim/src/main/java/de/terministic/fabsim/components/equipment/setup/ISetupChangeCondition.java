package de.terministic.fabsim.components.equipment.setup;

import java.util.List;

import de.terministic.fabsim.components.equipment.AbstractTool;
import de.terministic.fabsim.components.equipment.AbstractToolGroup;
import de.terministic.fabsim.components.equipment.SetupState;
import de.terministic.fabsim.core.AbstractFlowItem;

public interface ISetupChangeCondition {

	public boolean checkCondition(AbstractTool tool, AbstractFlowItem item, SetupState newState);

	public boolean checkCondition(AbstractToolGroup toolGroup, List<AbstractFlowItem> flowItems, AbstractTool tool,
			SetupState newState);

	public SetupState getState();

	public void initialize(AbstractToolGroup toolGroup);

}
