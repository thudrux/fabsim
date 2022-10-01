package de.terministic.fabsim.components;

import de.terministic.fabsim.core.AbstractComponent;
import de.terministic.fabsim.core.AbstractFlowItem;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.core.FabModel;

public abstract class AbstractReportingEvent extends AbstractSimEvent {

	public AbstractReportingEvent(FabModel model, long time, AbstractComponent component, AbstractFlowItem flowItem) {
		super(model, time, component, flowItem);
	}

	@Override
	public int getPriority() {
		return 100;
	}

	@Override
	public final void resolveEvent() {
		// do nothing
	}

}