package de.terministic.fabsim.metamodel.dispatchRules;

import java.util.ArrayList;

import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;
import de.terministic.fabsim.metamodel.components.equipment.AbstractToolGroup;
import de.terministic.fabsim.metamodel.components.equipment.BatchDetails;
import de.terministic.fabsim.metamodel.components.equipment.queuecentriccontroller.IFlowItemQueue;
import de.terministic.fabsim.metamodel.AbstractFlowItem;

public abstract class AbstractDispatchRule {

	private String name;

	public AbstractDispatchRule(final String name) {
		this.name = name;
	}

	public abstract AbstractFlowItem getBestItem(ArrayList<AbstractFlowItem> items);

	public AbstractFlowItem getBestItem(ArrayList<AbstractFlowItem> items, AbstractToolGroup tg, AbstractTool tool){
		return this.getBestItem(items);
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public abstract ArrayList<AbstractFlowItem> sortWithDispatchRule(ArrayList<AbstractFlowItem> items);
	public ArrayList<AbstractFlowItem> sortWithDispatchRule(ArrayList<AbstractFlowItem> items, AbstractToolGroup tg, AbstractTool tool){
		return this.sortWithDispatchRule(items);
	}

	public abstract ArrayList<AbstractFlowItem> addItemToList(AbstractFlowItem item, ArrayList<AbstractFlowItem> items);

	public abstract IFlowItemQueue createBatchQueue(BatchDetails details);

	public abstract IFlowItemQueue createQueue();

}