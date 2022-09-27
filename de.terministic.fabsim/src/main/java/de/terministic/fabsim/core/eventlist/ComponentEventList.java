package de.terministic.fabsim.core.eventlist;

import java.util.PriorityQueue;

import de.terministic.fabsim.core.AbstractSimEvent;

public class ComponentEventList extends PriorityQueue<AbstractSimEvent> implements Comparable<ComponentEventList> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6077842494064722314L;

	@Override
	public int compareTo(final ComponentEventList other) {
		return this.peek().compareTo(other.peek());
	}

	public long getTime() {
		return this.peek().getTime();
	}
}
