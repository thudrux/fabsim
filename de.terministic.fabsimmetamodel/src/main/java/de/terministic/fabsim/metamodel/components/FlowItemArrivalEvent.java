package de.terministic.fabsim.metamodel.components;

import de.terministic.fabsim.metamodel.AbstractComponent;
import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.metamodel.FabModel;

public class FlowItemArrivalEvent extends AbstractSimEvent {

	private AbstractComponent sender;

	public FlowItemArrivalEvent(FabModel model, long time, AbstractComponent component, AbstractFlowItem flowItem) {
		super(model, time, component, flowItem);
		// TODO Auto-generated constructor stub
	}

	public FlowItemArrivalEvent(FabModel model, long time, AbstractComponent receiver, AbstractFlowItem flowItem,
			AbstractComponent sender) {
		super(model, time, receiver, flowItem);
		this.sender = sender;
	}

	@Override
	public int getPriority() {
		return 1;
	}

	public AbstractComponent getSender() {
		return sender;
	}

	@Override
	public void resolveEvent() {
		super.resolveEvent();
		((AbstractComponent) component).onFlowItemArrival(this);
		/*
		 * if (component.take(getFlowItem())) {
		 * sender.onAcceptedFlowItemTransfer(this); } else {
		 * sender.onRejectedFlowItemTransfer(this); }
		 */
	}

}
