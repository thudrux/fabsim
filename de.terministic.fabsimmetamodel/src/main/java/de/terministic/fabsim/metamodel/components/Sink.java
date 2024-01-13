package de.terministic.fabsim.metamodel.components;

import de.terministic.fabsim.core.IFlowItem;
import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.AbstractSink;
import de.terministic.fabsim.metamodel.NotYetImplementedException;
import de.terministic.fabsim.metamodel.components.equipment.breakdown.IBreakdown;

public class Sink extends AbstractSink {
	public Sink(FabModel model, final String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void addBreakdown(final IBreakdown breakdown) {
		// TODO Auto-generated method stub
		throw new NotYetImplementedException();
	}

	@Override
	public boolean canProcessItem() {
		return true;
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFlowItemArrival(FlowItemArrivalEvent event) {
		event.getSender().onAcceptedFlowItemTransfer(event);
		getSimulationEngine().getEventList().scheduleEvent(
				new FlowItemDestructionEvent(getModel(), getSimulationEngine().getTime(), this, event.getFlowItem()));
	}

	@Override
	public void announceFlowItemArrival(IFlowItem item) {
		// Do nothing as this resource has no capa limit
	}

}
