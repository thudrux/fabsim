package de.terministic.fabsim.core;

import java.util.ArrayList;
import java.util.Collections;

public class EventsTypeList extends ArrayList<AbstractSimEvent> implements Comparable<EventsTypeList> {

	private static final long serialVersionUID = -3684156624934255743L;

	public EventsTypeList() {
	}

	@Override
	public int compareTo(EventsTypeList that) {
		return this.get(0).compareTo(that.get(0));
	}

	@Override
	public boolean add(AbstractSimEvent event) {
		if (super.add(event)) {
			Collections.sort(this);
			return true;
		}
		return false;
	}

	public AbstractSimEvent pop() {
		if (this.size() > 0) {
			return this.remove(0);
		}
		return null;
	}
}
