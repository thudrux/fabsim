package de.terministic.fabsim.metamodel.components;

import de.terministic.fabsim.metamodel.AbstractComponent;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.core.IFlowItem;
import de.terministic.fabsim.core.IModel;

public class FlowItemDestructionEvent extends AbstractSimEvent {

	public FlowItemDestructionEvent(IModel model, final long time, final AbstractComponent component,
			final IFlowItem flowItem) {
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
