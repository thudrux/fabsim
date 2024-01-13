package de.terministic.fabsim.metamodel.components.equipment;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.metamodel.FabModel;

public class UnloadingFinishedEvent extends AbstractSimEvent {

	public UnloadingFinishedEvent(FabModel model, final long time, final AbstractResource abstractResource,
			final AbstractResourceGroup parent, final AbstractFlowItem flowItem) {
		super(model, time, abstractResource, flowItem);
	}

	@Override
	public int getPriority() {
		return 6;
	}

	@Override
	public void resolveEvent() {
		super.resolveEvent();
		((AbstractTool) getComponent()).onUnloadingFinished(this);
	}

}
