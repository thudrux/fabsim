package de.terministic.fabsim.core.eventlist;

import java.util.Comparator;

import de.terministic.fabsim.core.AbstractComponent;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.core.IEventListTypeComparator;

public class ComponentComparator implements Comparator<AbstractComponent>, IEventListTypeComparator {

	@Override
	public int compare(final AbstractComponent o1, final AbstractComponent o2) {
		return Long.compare(o1.getId(), o2.getId());
	}

	public boolean compare(final AbstractSimEvent o1, final AbstractSimEvent o2) {
		if (o1 != null && o1.getComponent() != null)
			return o1.getComponent().equals(o2.getComponent());
		else
			return false;
	}

}
