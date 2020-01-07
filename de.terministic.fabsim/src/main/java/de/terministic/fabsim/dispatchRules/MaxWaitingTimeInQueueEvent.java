package de.terministic.fabsim.dispatchRules;

import de.terministic.fabsim.components.equipment.ToolGroup;
import de.terministic.fabsim.core.AbstractFlowItem;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.core.FabModel;

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
		item.setMaxWaitEvent(null);
		((ToolGroup) this.component).tryToStartAfterMaxWaitingTime(this.item);

	}

}
