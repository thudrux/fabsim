package de.terministic.fabsim.metamodel.statistics;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.components.FlowItemDestructionEvent;
import de.terministic.fabsim.core.ISimEvent;
import de.terministic.fabsim.core.SimEventListener;

public class CycleTimeTracker extends SimEventListener {

	long combinedCycleTime;
	int counter;
	long averageCycleTime;

	public long getAverageCycleTime() {
		return this.averageCycleTime;
	}

	@Override
	public void processEvent(final ISimEvent event) {
		if (event instanceof FlowItemDestructionEvent) {
			this.counter += 1;
			final long cycleTime = event.getEventTime() - ((AbstractFlowItem) event.getFlowItem()).getCreationTime();
			this.combinedCycleTime += cycleTime;
			this.averageCycleTime = this.combinedCycleTime / this.counter;
		}
	}
}
