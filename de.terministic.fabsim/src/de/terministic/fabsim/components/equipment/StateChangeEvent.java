package de.terministic.fabsim.components.equipment;

import de.terministic.fabsim.core.AbstractComponent;
import de.terministic.fabsim.core.AbstractFlowItem;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.core.FabModel;

public class StateChangeEvent extends AbstractSimEvent {

	private SemiE10EquipmentState newState;
	private SetupState setupState = null;

	public StateChangeEvent(FabModel model, final long time, final AbstractComponent component,
			final AbstractFlowItem flowItem) {
		super(model, time, component, null);
	}

	public StateChangeEvent(FabModel model, final long time, final AbstractComponent component,
			final SemiE10EquipmentState newState) {
		super(model, time, component, null);
		this.newState = newState;
	}

	public SemiE10EquipmentState getNewState() {
		return this.newState;
	}

	@Override
	public int getPriority() {
		return 7;
	}

	public SetupState getSetupState() {
		return this.setupState;
	}

	public void setNewState(final SemiE10EquipmentState newState) {
		this.newState = newState;
	}

	public void setSetupState(final SetupState currentSetupState) {
		this.setupState = currentSetupState;
	}

	@Override
	public String toString() {
		return super.toString() + " => " + this.newState;
	}

}
