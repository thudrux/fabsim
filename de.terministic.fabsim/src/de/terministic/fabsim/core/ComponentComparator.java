package de.terministic.fabsim.core;

import java.util.Comparator;

public class ComponentComparator implements Comparator<AbstractComponent> {

	@Override
	public int compare(final AbstractComponent o1, final AbstractComponent o2) {
		return Long.compare(o1.getId(), o2.getId());
	}

}
