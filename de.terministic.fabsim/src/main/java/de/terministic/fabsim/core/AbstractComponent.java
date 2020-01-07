package de.terministic.fabsim.core;

import de.terministic.fabsim.components.FlowItemArrivalEvent;
import de.terministic.fabsim.components.equipment.InvalidEventForResourceException;

public abstract class AbstractComponent extends AbstractFixedModelElement {

	public AbstractComponent(FabModel model, final String name) {
		super(model, name);
	}

	public void addListener(final SimEventListener listener) {
		listener.addComponentFilter(this);
		getSimulationEngine().addListener(listener);
	}

	public abstract void initialize();

	public void onFlowItemArrival(final FlowItemArrivalEvent event) {
		throw new InvalidEventForResourceException("Trigger is not supported by " + this.getClass().getSimpleName());
	}

	public abstract void announceFlowItemArrival(AbstractFlowItem item);

	public void onAcceptedFlowItemTransfer(FlowItemArrivalEvent event) {

	}

	public void onRejectedFlowItemTransfer(FlowItemArrivalEvent event) {
		throw new MaterialFlowException("Flow item was unable to leave component");
	}

}
