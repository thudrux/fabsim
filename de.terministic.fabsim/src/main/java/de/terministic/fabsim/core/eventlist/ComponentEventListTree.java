package de.terministic.fabsim.core.eventlist;

import java.util.TreeSet;

import de.terministic.fabsim.core.AbstractSimEvent;

public class ComponentEventListTree extends TreeSet<AbstractSimEvent> implements Comparable<ComponentEventListTree> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6077842494064722314L;

	@Override
	public int compareTo(final ComponentEventListTree other) {
		return this.first().compareTo(other.first());
	}

	public long getTime() {
		return this.first().getTime();
	}
}
