package de.terministic.fabsim.components;

import de.terministic.fabsim.core.AbstractComponent;
import de.terministic.fabsim.core.AbstractFlowItem;
import de.terministic.fabsim.core.FabModel;

public class FlowItemDestructionEvent extends AbstractReportingEvent {

	public FlowItemDestructionEvent(FabModel model, final long time, final AbstractComponent component,
			final AbstractFlowItem flowItem) {
		super(model, time, component, flowItem);
	}

}
