package de.terministic.fabsim.metamodel.components.equipment;

import de.terministic.fabsim.metamodel.AbstractComponent;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.core.IFlowItem;
import de.terministic.fabsim.metamodel.FabModel;

public class OperatorFinishedEvent extends AbstractSimEvent {

	public OperatorFinishedEvent(FabModel model, final long time, final AbstractComponent component,
			final AbstractResourceGroup toolGroup, final IFlowItem flowItem) {
		super(model, time, component, flowItem);
	}

	@Override
	public int getPriority() {
		return 4;
	}

	@Override
	public void resolveEvent() {
		super.resolveEvent();
		((AbstractTool) this.component).onOperatorFinishedProcessing(this);
	}

}
