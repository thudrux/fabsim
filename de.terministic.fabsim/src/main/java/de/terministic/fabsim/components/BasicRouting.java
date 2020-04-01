/*
 *
 * @author    Falk Pappert
 */
package de.terministic.fabsim.components;

import de.terministic.fabsim.components.equipment.AbstractResource;
import de.terministic.fabsim.components.equipment.breakdown.IBreakdown;
import de.terministic.fabsim.core.AbstractFlowItem;
import de.terministic.fabsim.core.AbstractRouting;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.NotYetImplementedException;
import de.terministic.fabsim.core.SimulatorEngineException;

/**
 * The Class BasicRouting.
 */
public class BasicRouting extends AbstractRouting {

	public BasicRouting(FabModel model, String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	public BasicRouting(FabModel model) {
		super(model, "BasicRouting");
	}

	@Override
	public void initialize() {
	}

	@Override
	public void onFlowItemArrival(FlowItemArrivalEvent event) {
		event.getSender().onAcceptedFlowItemTransfer(event);

		AbstractFlowItem flowItem = event.getFlowItem();
		if (flowItem.getRecipe().size() > flowItem.getCurrentStepNumber()) {
			this.logger.trace("Routing {} to {}", flowItem, flowItem.getCurrentStep().getComponent());
			sendFlowItemToResource(flowItem, (AbstractResource) flowItem.getCurrentStep().getComponent());
		} else {
			this.logger.error("FlowItem({}) can not be routed, recipe({}, {}) is smaller than current step number({})",
					flowItem, flowItem.getRecipe(), flowItem.getRecipe().size(), flowItem.getCurrentStepNumber());
			throw new SimulatorEngineException(
					"FlowItem can not be routed, recipe is smaller than current step number");
		}
	}

	@Override
	public void announceFlowItemArrival(AbstractFlowItem item) {
		// Do nothing as this resource has no capa limit

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
