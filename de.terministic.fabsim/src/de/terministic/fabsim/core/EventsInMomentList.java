package de.terministic.fabsim.core;

import java.util.PriorityQueue;

public class EventsInMomentList extends PriorityQueue<AbstractSimEvent> implements Comparable<EventsInMomentList> {
	/**
	 *
	 */
	private static final long serialVersionUID = -3684156624934255743L;
	private final long time;

	EventsInMomentList(final long time) {
		this.time = time;
	}

	@Override
	public int compareTo(final EventsInMomentList other) {
		return Long.compare(this.getTime(), other.getTime());
	}

	public long getTime() {
		return this.time;
	}
}
