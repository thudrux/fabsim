package de.terministic.fabsim.metamodel.statistics;

import de.terministic.fabsim.metamodel.components.FlowItemDestructionEvent;
import de.terministic.fabsim.core.ISimEvent;
import de.terministic.fabsim.core.SimEventListener;

public class FinishedFlowItemCounter extends SimEventListener {
	private long itemCount = 0L;

	public long getItemCount() {
		return this.itemCount;
	}

	@Override
	public void processEvent(final ISimEvent event) {
		if (event instanceof FlowItemDestructionEvent) {
			setItemCount(getItemCount() + 1);
			// this.logger.trace("Counted a finished flowItem");
		}

	}

	public void setItemCount(final long itemCounter) {
		this.itemCount = itemCounter;
	}

}
