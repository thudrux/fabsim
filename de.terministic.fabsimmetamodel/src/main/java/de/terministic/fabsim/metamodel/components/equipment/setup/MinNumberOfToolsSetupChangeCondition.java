package de.terministic.fabsim.metamodel.components.equipment.setup;

import java.util.List;

import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;
import de.terministic.fabsim.metamodel.components.equipment.AbstractToolGroup;
import de.terministic.fabsim.metamodel.components.equipment.SetupState;
import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.NotYetImplementedException;

public class MinNumberOfToolsSetupChangeCondition implements ISetupChangeCondition {
	private final SetupState state;
	private final int number;

	public MinNumberOfToolsSetupChangeCondition(final SetupState state, final int number) {
		this.state = state;
		this.number = number;
	}

	@Override
	public boolean checkCondition(final AbstractTool tool, final AbstractFlowItem item, final SetupState newState) {
		// TODO Auto-generated method stub
		throw new NotYetImplementedException();
	}

	@Override
	public boolean checkCondition(final AbstractToolGroup toolGroup, final List<AbstractFlowItem> flowItems,
			final AbstractTool tool, final SetupState newState) {
		// TODO Auto-generated method stub
		throw new NotYetImplementedException();
	}

	public int getNumber() {
		return this.number;
	}

	@Override
	public SetupState getState() {
		return this.state;
	}

	@Override
	public void initialize(final AbstractToolGroup toolGroup) {
		// TODO Auto-generated method stub
		throw new NotYetImplementedException();

	}

}
