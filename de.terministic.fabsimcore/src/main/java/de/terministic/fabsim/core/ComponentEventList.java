package de.terministic.fabsim.core;

import java.util.PriorityQueue;

public class ComponentEventList extends PriorityQueue<ISimEvent> implements Comparable<ComponentEventList> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6077842494064722314L;

	@Override
	public int compareTo(final ComponentEventList other) {
		return this.peek().compareTo(other.peek());
	}

	public long getTime() {
		return this.peek().getEventTime();
	}
}
