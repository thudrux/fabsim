package de.terministic.fabsim.metamodel.components;

import java.util.ArrayList;

public class Recipe extends ArrayList<ProcessStep> implements ProcessDetails {
	/**
	 *
	 */
	private static final long serialVersionUID = 7910245675682361715L;
	private final String name;

	public Recipe(final String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return this.name;
	}

}
