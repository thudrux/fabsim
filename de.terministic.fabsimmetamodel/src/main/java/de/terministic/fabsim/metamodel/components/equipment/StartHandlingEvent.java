package de.terministic.fabsim.metamodel.components.equipment;

import de.terministic.fabsim.metamodel.AbstractComponent;
import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.metamodel.FabModel;

public class StartHandlingEvent extends AbstractSimEvent {

	public StartHandlingEvent(FabModel model, final long time, final AbstractComponent component,
			final AbstractFlowItem flowItem) {
		super(model, time, component, flowItem);
	}

	@Override
	public int getPriority() {
		// TODO Auto-generated method stub
		return 1000;
	}

	@Override
	public void resolveEvent() {
		super.resolveEvent();
	}

}
