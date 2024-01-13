package de.terministic.fabsim.metamodel.components.equipment.setup;

import java.util.List;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;
import de.terministic.fabsim.metamodel.components.equipment.AbstractToolGroup;
import de.terministic.fabsim.metamodel.components.equipment.SetupState;

public interface ISetupChangeCondition {

	public boolean checkCondition(AbstractTool tool, AbstractFlowItem item, SetupState newState);

	public boolean checkCondition(AbstractToolGroup toolGroup, List<AbstractFlowItem> flowItems, AbstractTool tool,
			SetupState newState);

	public SetupState getState();

	public void initialize(AbstractToolGroup toolGroup);

}
