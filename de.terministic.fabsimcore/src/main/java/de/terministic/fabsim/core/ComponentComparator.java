package de.terministic.fabsim.core;

import java.util.Comparator;

public class ComponentComparator implements Comparator<IComponent>, IEventListTypeComparator {

	@Override
	public int compare(final IComponent o1, final IComponent o2) {
		return Long.compare(o1.getId(), o2.getId());
	}

	@Override
	public boolean compare(ISimEvent a1, ISimEvent a2) {
		if (a1 != null && a1.getComponent() != null)
			return a1.getComponent().equals(a2.getComponent());
		else
			return false;

	}

}
