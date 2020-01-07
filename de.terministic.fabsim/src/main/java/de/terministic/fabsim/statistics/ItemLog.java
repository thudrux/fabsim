package de.terministic.fabsim.statistics;

import java.util.ArrayList;
import java.util.List;

import de.terministic.fabsim.components.CreationEvent;
import de.terministic.fabsim.components.FlowItemDestructionEvent;
import de.terministic.fabsim.core.AbstractFlowItem;
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
			this.items.add(event.getFlowItem());
			event.getFlowItem().getStatistics().put(CREATIONTIMEMARKER, event.getEventTime());
		}
		if (event instanceof FlowItemDestructionEvent) {
			event.getFlowItem().getStatistics().put(DESTRUCTIONTIMEMARKER, event.getEventTime());
		}
	}

}
