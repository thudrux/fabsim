package de.terministic.fabsim.components;

import de.terministic.fabsim.components.equipment.AbstractResource;
import de.terministic.fabsim.components.equipment.breakdown.IBreakdown;
import de.terministic.fabsim.core.AbstractFlowItem;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.NotYetImplementedException;

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
		event.getFlowItem().markCurrentStepAsFinished();
		sendFlowItemToResource(event.getFlowItem(), getSimulationEngine().getModel().getRouting());

	}

	@Override
	public void onFlowItemArrival(FlowItemArrivalEvent event) {
		AbstractFlowItem flowItem = event.getFlowItem();
		this.logger.debug("{} item {} arrived", getTime(), flowItem);
		this.coolDownTime = flowItem.getCurrentStep().getDuration(flowItem);
		final BufferFinishedEvent endEvent = new BufferFinishedEvent(getModel(), getTime() + this.coolDownTime, this,
				flowItem);
		getSimulationEngine().getEventList().scheduleEvent(endEvent);

	}

	@Override
	public void announceFlowItemArrival(AbstractFlowItem item) {
		// Do nothing resource has no capa limit

	}

	@Override
	public void addBreakdown(IBreakdown breakdown) {
		// TODO Auto-generated method stub
		throw new NotYetImplementedException();

	}

	@Override
	public boolean canProcessItem(AbstractFlowItem item) {
		return true;
	}

}
