package de.terministic.fabsim.core;

import java.util.Comparator;

public class EventComparator implements Comparator<ISimEvent> {

	@Override
	public int compare(final ISimEvent e1, final ISimEvent e2) {
		int result = Long.compare(e1.getEventTime(), e2.getEventTime());
		if (result == 0) {
			result = Integer.compare(e1.getPriority(), e2.getPriority());
		}
		return result;
	}

}
