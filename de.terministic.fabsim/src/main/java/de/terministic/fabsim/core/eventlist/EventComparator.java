package de.terministic.fabsim.core.eventlist;

import java.util.Comparator;

import de.terministic.fabsim.core.ISimEvent;

public class EventComparator implements Comparator<ISimEvent> {

	@Override
	public int compare(final ISimEvent e1, final ISimEvent e2) {
		int result = Long.compare(e1.getEventTime(), e2.getEventTime());
		if (result == 0) {
			result = Integer.compare(e1.getPriority(), e2.getPriority());
		}
		if (result == 0) {
			result = Long.compare(e1.getId(), e2.getId());
		}
		return result;
	}

}
