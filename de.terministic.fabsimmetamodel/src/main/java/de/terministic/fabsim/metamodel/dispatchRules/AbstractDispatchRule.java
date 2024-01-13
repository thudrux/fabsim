package de.terministic.fabsim.metamodel.dispatchRules;

import java.util.ArrayList;

import de.terministic.fabsim.core.IFlowItem;
import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.components.equipment.BatchDetails;
import de.terministic.fabsim.metamodel.components.equipment.queuecentriccontroller.IFlowItemQueue;

public abstract class AbstractDispatchRule {

	private String name;

	public AbstractDispatchRule(final String name) {
		this.name = name;
	}

	public abstract AbstractFlowItem getBestItem(ArrayList<IFlowItem> possibleItems);

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public abstract ArrayList<AbstractFlowItem> sortWithDispatchRule(ArrayList<AbstractFlowItem> items);

	public abstract ArrayList<AbstractFlowItem> addItemToList(AbstractFlowItem item, ArrayList<AbstractFlowItem> items);

	public abstract IFlowItemQueue createBatchQueue(BatchDetails details);

	public abstract IFlowItemQueue createQueue();

}