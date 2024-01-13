package de.terministic.fabsim.metamodel.components.equipment;

import de.terministic.fabsim.metamodel.AbstractComponent;
import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.metamodel.FabModel;

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
		super.resolveEvent();
		((AbstractTool) this.component).onSetupFinished(this);
	}

}
