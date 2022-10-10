package de.terministic.fabsim.core.eventlist.comparison;

import de.terministic.fabsim.core.AbstractComponent;
import de.terministic.fabsim.core.AbstractFlowItem;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.core.FabModel;

public class HoldModelEvent extends AbstractSimEvent {

	public HoldModelEvent(FabModel model, long time, AbstractComponent component, AbstractFlowItem flowItem) {
		super(model, time, component, flowItem);
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public void resolveEvent() {
		((HoldModelBlock) getComponent()).resolveHoldEvent();
	}

}
