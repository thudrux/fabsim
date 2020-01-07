package de.terministic.fabsim.components.equipment;

import de.terministic.fabsim.core.AbstractComponent;
import de.terministic.fabsim.core.AbstractFlowItem;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.core.FabModel;

public class FinishedHandlingEvent extends AbstractSimEvent {

	public FinishedHandlingEvent(FabModel model, final long time, final AbstractComponent component,
			final AbstractFlowItem flowItem) {
		super(model, time, component, flowItem);
	}

	@Override
	public int getPriority() {
		return 1000;
	}

	@Override
	public void resolveEvent() {
		super.resolveEvent();
	}

}
