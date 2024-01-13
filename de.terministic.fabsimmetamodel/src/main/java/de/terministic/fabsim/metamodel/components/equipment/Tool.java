package de.terministic.fabsim.metamodel.components.equipment;

import de.terministic.fabsim.metamodel.components.FlowItemArrivalEvent;
import de.terministic.fabsim.metamodel.components.equipment.breakdown.IBreakdown;
import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.NotYetImplementedException;
import de.terministic.fabsim.metamodel.components.equipment.toolstatemachine.AbstractToolStateMachine;

public class Tool extends AbstractTool {
	public Tool(FabModel model, final String name, final AbstractResourceGroup toolGroup,
			final AbstractToolStateMachine stateMachine) {
		super(model, name, toolGroup, stateMachine);
	}

	@Override
	public void addBreakdown(final IBreakdown breakdown) {
		// TODO Auto-generated method stub
		throw new NotYetImplementedException();
	}

	@Override
	public void becomesAvailable() {
		((ToolGroup) this.parent).onResourceBecomesAvailable(this);
	}

	@Override
	public void becomesUnavailable() {
		((ToolGroup) this.parent).onResourceBecomesUnavailable(this);
	}

	@Override
	public void initialize() {
		// do nothing
	}

	@Override
	public void onFlowItemArrival(FlowItemArrivalEvent event) {
		super.onFlowItemArrival(event);

		if (this.toolStateMachine.readyToProcess(this)) {
			this.toolStateMachine.handleFlowItemArrival(this, (AbstractFlowItem)event.getFlowItem());
			event.getSender().onAcceptedFlowItemTransfer(event);
		} else {
			logger.trace("Tool can not accept new flowItem, is in state: {}",
					toolStateMachine.getCurrentSemiE10StateOfTool(this));
			event.getSender().onRejectedFlowItemTransfer(event);
		}
	}

	@Override
	public void announceFlowItemArrival(AbstractFlowItem item) {
		// TODO Reject all further canProcess requests
	}
}
