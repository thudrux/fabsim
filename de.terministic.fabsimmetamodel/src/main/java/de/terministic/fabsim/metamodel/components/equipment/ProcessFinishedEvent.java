package de.terministic.fabsim.metamodel.components.equipment;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.metamodel.FabModel;

public class ProcessFinishedEvent extends AbstractSimEvent {

	public ProcessFinishedEvent(FabModel model, final long time, final AbstractResource abstractTool,
			final AbstractResourceGroup parent, final AbstractFlowItem flowItem) {
		super(model, time, abstractTool, flowItem);
	}

	@Override
	public int getPriority() {
		return 8;
	}

	@Override
	public void resolveEvent() {
		super.resolveEvent();
		((AbstractTool) getComponent()).onProcessFinished(this);
	}

}
