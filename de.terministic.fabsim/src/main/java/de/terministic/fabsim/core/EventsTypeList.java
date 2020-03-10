package de.terministic.fabsim.core;

import java.util.PriorityQueue;

public class EventsTypeList<T extends Comparable<T>> extends PriorityQueue<AbstractSimEvent>
		implements Comparable<EventsTypeList<T>> {

	private static final long serialVersionUID = -3684156624934255743L;

	private final T type;

	public EventsTypeList(T type) {
		this.type = type;
	}

	public T getType() {
		return type;
	}

	@Override
	public int compareTo(EventsTypeList<T> that) {
		int result = Long.compare(this.peek().getEventTime(), that.peek().getEventTime());
		if (result == 0)
			result = this.getType().compareTo(that.getType());
		return result;
	}
}
