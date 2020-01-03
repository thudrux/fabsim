package de.terministic.fabsim.statistics;

import de.terministic.fabsim.components.CreationEvent;
import de.terministic.fabsim.components.FlowItemDestructionEvent;
import de.terministic.fabsim.core.ISimEvent;
import de.terministic.fabsim.core.SimEventListener;

public class FirstCycleTimeTracker extends SimEventListener {

	long arrival = -5L;
	long destruction = -10L;

	public long getCycleTime() {
		this.logger.debug("First Item times are Arrival: {} and Destruction: {}", this.arrival, this.destruction);
		return this.destruction - this.arrival;
	}

	@Override
	public void processEvent(final ISimEvent event) {
		if (event instanceof CreationEvent) {
			if (1L == event.getFlowItem().getId()) {
				this.arrival = event.getEventTime();
			}
		}
		if (event instanceof FlowItemDestructionEvent) {
			if (1L == event.getFlowItem().getId()) {
				this.destruction = event.getEventTime();
			}
		}

	}
}
