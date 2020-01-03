package de.terministic.fabsim.dispatchRules;

import java.util.ArrayList;

import de.terministic.fabsim.core.AbstractFlowItem;

public abstract class AbstractDispatchRule {

	private String name;

	public AbstractDispatchRule(final String name) {
		this.name = name;
	}

	public abstract AbstractFlowItem getBestItem(ArrayList<AbstractFlowItem> items);

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public abstract ArrayList<AbstractFlowItem> sortWithDispatchRule(ArrayList<AbstractFlowItem> items);

	public abstract ArrayList<AbstractFlowItem> addItemToList(AbstractFlowItem item, ArrayList<AbstractFlowItem> items);

}