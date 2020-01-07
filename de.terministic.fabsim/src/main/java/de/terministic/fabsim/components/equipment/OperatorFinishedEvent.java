package de.terministic.fabsim.components.equipment;

import de.terministic.fabsim.core.AbstractComponent;
import de.terministic.fabsim.core.AbstractFlowItem;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.core.FabModel;

public class OperatorFinishedEvent extends AbstractSimEvent {

	public OperatorFinishedEvent(FabModel model, final long time, final AbstractComponent component,
			final AbstractResourceGroup toolGroup, final AbstractFlowItem flowItem) {
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
