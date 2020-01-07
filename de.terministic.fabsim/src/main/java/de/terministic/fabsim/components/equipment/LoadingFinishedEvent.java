package de.terministic.fabsim.components.equipment;

import de.terministic.fabsim.core.AbstractFlowItem;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.core.FabModel;

public class LoadingFinishedEvent extends AbstractSimEvent {

	public LoadingFinishedEvent(FabModel model, final long time, final AbstractResource abstractResource,
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
		((AbstractTool) getComponent()).onLoadingFinished(this);
	}

}
