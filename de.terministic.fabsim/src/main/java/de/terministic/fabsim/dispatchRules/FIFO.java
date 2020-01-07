package de.terministic.fabsim.dispatchRules;

import java.util.ArrayList;

import de.terministic.fabsim.core.AbstractFlowItem;

public class FIFO extends AbstractDispatchRule {

	public FIFO() {
		super("FIFO");
	}

	@Override
	public AbstractFlowItem getBestItem(final ArrayList<AbstractFlowItem> items) {
		final ArrayList<AbstractFlowItem> l = sortWithDispatchRule(items);
		if (l.size() > 0)
			return l.get(0);
		else
			return null;
	}

	@Override
	public ArrayList<AbstractFlowItem> sortWithDispatchRule(ArrayList<AbstractFlowItem> items) {
		// final ArrayList<AbstractFlowItem> newList = new ArrayList<>();
		// final ArrayList<AbstractFlowItem> oldList = new ArrayList<>(items);
		// while (oldList.size() > 0) {
		// int index = 0;
		// long lastTime =
		// oldList.get(0).getTimeStamps(oldList.get(0).getCurrentStepNumber()).getArrivalTime();
		// for (int i = 0; i < oldList.size(); i++) {
		// if (lastTime >
		// oldList.get(i).getTimeStamps(oldList.get(i).getCurrentStepNumber()).getArrivalTime())
		// {
		// index = i;
		// lastTime =
		// oldList.get(i).getTimeStamps(oldList.get(i).getCurrentStepNumber()).getArrivalTime();
		// }
		// }
		// newList.add(oldList.get(index));
		// oldList.remove(index);
		// }
		return items;
	}

	@Override
	public ArrayList<AbstractFlowItem> addItemToList(AbstractFlowItem item, ArrayList<AbstractFlowItem> items) {
		items.add(item);
		return items;
	}

}
