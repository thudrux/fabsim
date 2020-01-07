package de.terministic.fabsim.statistics;

import de.terministic.fabsim.core.ISimEvent;
import de.terministic.fabsim.core.SimEventListener;

public class SimulationLog extends SimEventListener {

	@Override
	public void processEvent(final ISimEvent event) {
		this.logger.debug("{}\t{}\t{}\t{}", event.getEventTime(), event.getClass().getSimpleName(),
				event.getComponent().getName(), event.getFlowItem());

	}

}
