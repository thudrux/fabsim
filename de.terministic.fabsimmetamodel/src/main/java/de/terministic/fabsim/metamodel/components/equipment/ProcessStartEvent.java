package de.terministic.fabsim.metamodel.components.equipment;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.metamodel.FabModel;

public class ProcessStartEvent extends AbstractSimEvent {

	private final AbstractFlowItem flowItem;
	private final AbstractResource tool;

	public ProcessStartEvent(FabModel model, final long time, final AbstractResource tool, final AbstractResourceGroup toolgroup,
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
	}

}
