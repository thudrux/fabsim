package de.terministic.fabsim.components.equipment.setup;

import de.terministic.fabsim.components.equipment.SetupFinishedEvent;
import de.terministic.fabsim.components.equipment.StartHandlingEvent;
import de.terministic.fabsim.core.ISimEvent;
import de.terministic.fabsim.core.SimEventListener;

public class MinProductiveTimeListener extends SimEventListener {
	private MinProductiveTimeSinceSetupChangeCondition condition;

	public MinProductiveTimeSinceSetupChangeCondition getCondition() {
		return this.condition;
	}

	@Override
	public void processEvent(final ISimEvent event) {
		if (event instanceof SetupFinishedEvent) {
			this.condition.notifyOfSetupFinishedAt(event.getComponent());
		}
		if (event instanceof StartHandlingEvent) {
			this.condition.notifyOfProcessStartAt(event.getComponent(), event.getFlowItem());
		}

	}

	public void setCondition(final MinProductiveTimeSinceSetupChangeCondition condition) {
		this.condition = condition;
	}

}
