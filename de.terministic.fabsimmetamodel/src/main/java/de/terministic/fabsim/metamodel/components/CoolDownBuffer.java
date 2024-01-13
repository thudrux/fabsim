package de.terministic.fabsim.metamodel.components;

import de.terministic.fabsim.core.IFlowItem;
import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.NotYetImplementedException;
import de.terministic.fabsim.metamodel.components.equipment.AbstractResource;
import de.terministic.fabsim.metamodel.components.equipment.breakdown.IBreakdown;

public class CoolDownBuffer extends AbstractResource {
	private long coolDownTime;

	public CoolDownBuffer(FabModel model, final String name) {
		super(model, name);
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

	public void onProcessFinished(final BufferFinishedEvent event) {
		((AbstractFlowItem)event.getFlowItem()).markCurrentStepAsFinished();
		sendFlowItemToResource( event.getFlowItem(), getModel().getRouting());

	}

	@Override
	public void onFlowItemArrival(FlowItemArrivalEvent event) {
		AbstractFlowItem flowItem = (AbstractFlowItem) event.getFlowItem();
		this.logger.debug("{} item {} arrived", getTime(), flowItem);
		this.coolDownTime = flowItem.getCurrentStep().getDuration(flowItem);
		final BufferFinishedEvent endEvent = new BufferFinishedEvent(getModel(), getTime() + this.coolDownTime, this,
				flowItem);
		getSimulationEngine().getEventList().scheduleEvent(endEvent);

	}

	@Override
	public void announceFlowItemArrival(IFlowItem item) {
		// Do nothing resource has no capa limit

	}

	@Override
	public void addBreakdown(IBreakdown breakdown) {
		// TODO Auto-generated method stub
		throw new NotYetImplementedException();

	}

	@Override
	public boolean canProcessItem() {
		return true;
	}

}
