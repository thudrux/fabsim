package de.terministic.fabsim.components.equipment;

import de.terministic.fabsim.core.AbstractFlowItem;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.core.FabModel;

public class ProcessStartEvent extends AbstractSimEvent {

	private final AbstractFlowItem flowItem;
	private final Tool tool;

	public ProcessStartEvent(FabModel model, final long time, final Tool tool, final AbstractToolGroup toolgroup,
			final AbstractFlowItem flowItem) {
		super(model, time, tool, flowItem);
		this.tool = tool;
		this.flowItem = flowItem;
	}

	@Override
	public int getPriority() {
		return 13;
	}

	@Override
	public void resolveEvent() {
		super.resolveEvent();
		this.tool.finishProcessingOfFlowItem(this.flowItem);
	}

}
