package de.terministic.fabsim.metamodel.dispatchRules;

import de.terministic.fabsim.metamodel.components.equipment.ToolGroup;
import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.metamodel.FabModel;

public class MaxWaitingTimeInQueueEvent extends AbstractSimEvent {

	public MaxWaitingTimeInQueueEvent(FabModel model, final long time, final ToolGroup component,
			final AbstractFlowItem flowItem) {
		super(model, time, component, flowItem);
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public void resolveEvent() {
		super.resolveEvent();
		((AbstractFlowItem) item).setMaxWaitEvent(null);
		((ToolGroup) this.component).tryToStartAfterMaxWaitingTime((AbstractFlowItem) this.item);

	}

}
