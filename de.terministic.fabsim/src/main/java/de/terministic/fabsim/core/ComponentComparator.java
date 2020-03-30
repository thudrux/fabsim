package de.terministic.fabsim.core;

import java.util.Comparator;

public class ComponentComparator implements Comparator<AbstractComponent>, IEventListTypeComparator {

	@Override
	public int compare(final AbstractComponent o1, final AbstractComponent o2) {
		return Long.compare(o1.getId(), o2.getId());
	}

	public boolean compare(final AbstractSimEvent o1, final AbstractSimEvent o2) {
		if (o1 != null && o1.component != null)
			return o1.component.equals(o2.component);
		else
			return false;
	}

}
