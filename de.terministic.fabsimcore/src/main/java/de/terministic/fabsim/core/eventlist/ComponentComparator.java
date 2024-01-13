package de.terministic.fabsim.core.eventlist;

import java.util.Comparator;

import de.terministic.fabsim.core.IComponent;
import de.terministic.fabsim.core.IEventListTypeComparator;
import de.terministic.fabsim.core.ISimEvent;

public class ComponentComparator implements Comparator<IComponent>, IEventListTypeComparator {

	@Override
	public int compare(final IComponent o1, final IComponent o2) {
		return Long.compare(o1.getId(), o2.getId());
	}

	public boolean compare(final ISimEvent o1, final ISimEvent o2) {
		if (o1 != null && o1.getComponent() != null)
			return o1.getComponent().equals(o2.getComponent());
		else
			return false;
	}

}
