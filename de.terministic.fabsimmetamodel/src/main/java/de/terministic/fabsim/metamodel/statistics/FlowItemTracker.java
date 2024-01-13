package de.terministic.fabsim.metamodel.statistics;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.core.IFlowItem;
import de.terministic.fabsim.core.ISimEvent;
import de.terministic.fabsim.core.SimEventListener;

public class FlowItemTracker extends SimEventListener {

	long id;
	IFlowItem item;

	public FlowItemTracker(final long id) {
		this.id = id;
	}

	public IFlowItem getItem() {
		return this.item;
	}

	@Override
	public void processEvent(final ISimEvent event) {
		if (this.id == event.getFlowItem().getId()) {
			this.item = event.getFlowItem();
			this.logger.debug("{}\t{}\t{}\t{}", event.getEventTime(), event.getClass().getSimpleName(),
					event.getComponent().getName(), event.getFlowItem().getId());
		}

	}

}
