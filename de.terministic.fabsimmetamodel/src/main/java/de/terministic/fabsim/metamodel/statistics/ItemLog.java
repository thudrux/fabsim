package de.terministic.fabsim.metamodel.statistics;

import java.util.ArrayList;
import java.util.List;

import de.terministic.fabsim.metamodel.components.CreationEvent;
import de.terministic.fabsim.metamodel.components.FlowItemDestructionEvent;
import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.core.ISimEvent;
import de.terministic.fabsim.core.SimEventListener;

public class ItemLog extends SimEventListener {
	public static final String CREATIONTIMEMARKER = "ITEMLOG_CREATION_TIME";
	public static final String DESTRUCTIONTIMEMARKER = "ITEMLOG_DESTRUCTION_TIME";
	private final List<AbstractFlowItem> items = new ArrayList<>();

	public List<AbstractFlowItem> getItems() {
		return this.items;
	}

	@Override
	public void processEvent(final ISimEvent event) {
		if (event instanceof CreationEvent) {
			this.items.add((AbstractFlowItem) event.getFlowItem());
			((AbstractFlowItem) event.getFlowItem()).getStatistics().put(CREATIONTIMEMARKER, event.getEventTime());
		}
		if (event instanceof FlowItemDestructionEvent) {
			((AbstractFlowItem) event.getFlowItem()).getStatistics().put(DESTRUCTIONTIMEMARKER, event.getEventTime());
		}
	}

}
