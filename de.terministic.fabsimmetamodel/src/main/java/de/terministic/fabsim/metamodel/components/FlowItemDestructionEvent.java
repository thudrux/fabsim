package de.terministic.fabsim.metamodel.components;

import de.terministic.fabsim.metamodel.AbstractComponent;
import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.metamodel.FabModel;

public class FlowItemDestructionEvent extends AbstractSimEvent {

	public FlowItemDestructionEvent(FabModel model, final long time, final AbstractComponent component,
			final AbstractFlowItem flowItem) {
		super(model, time, component, flowItem);
	}

	@Override
	public int getPriority() {
		return 100;
	}

	@Override
	public void resolveEvent() {
		super.resolveEvent();
	}

}
