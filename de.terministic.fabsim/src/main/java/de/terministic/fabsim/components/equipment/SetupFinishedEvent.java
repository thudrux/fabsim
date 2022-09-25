package de.terministic.fabsim.components.equipment;

import de.terministic.fabsim.core.AbstractComponent;
import de.terministic.fabsim.core.AbstractFlowItem;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.core.FabModel;

public class SetupFinishedEvent extends AbstractSimEvent {

	public SetupFinishedEvent(FabModel model, final long time, final AbstractComponent tool,
			final AbstractResourceGroup parent, final AbstractFlowItem flowItem) {
		super(model, time, tool, flowItem);

	}

	@Override
	public int getPriority() {
		return 14;
	}

	@Override
	public void resolveEvent() {
		((AbstractTool) this.component).onSetupFinished(this);
	}

}
