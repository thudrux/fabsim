package de.terministic.fabsim.metamodel;

import de.terministic.fabsim.core.IComponent;
import de.terministic.fabsim.core.IFlowItem;
import de.terministic.fabsim.core.IModel;
import de.terministic.fabsim.core.SimEventListener;
import de.terministic.fabsim.metamodel.components.FlowItemArrivalEvent;
import de.terministic.fabsim.metamodel.components.equipment.InvalidEventForResourceException;

public abstract class AbstractComponent extends AbstractFixedModelElement implements IComponent {

	public AbstractComponent(FabModel model, final String name) {
		super(model, name);
	}

	@Override
	public void addListener(final SimEventListener listener) {
		listener.addComponentFilter(this);
		getSimulationEngine().addListener(listener);
	}

	@Override
	public abstract void initialize();

	public void onFlowItemArrival(final FlowItemArrivalEvent event) {
		throw new InvalidEventForResourceException("Trigger is not supported by " + this.getClass().getSimpleName());
	}

	public abstract void announceFlowItemArrival(IFlowItem item);

	public void onAcceptedFlowItemTransfer(FlowItemArrivalEvent event) {

	}

	public void onRejectedFlowItemTransfer(FlowItemArrivalEvent event) {
		throw new MaterialFlowException("Flow item was unable to leave component");
	}

}
