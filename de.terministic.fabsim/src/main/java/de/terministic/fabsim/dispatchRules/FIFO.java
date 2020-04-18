package de.terministic.fabsim.dispatchRules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import de.terministic.fabsim.components.equipment.BatchDetails;
import de.terministic.fabsim.components.equipment.queuecentriccontroller.FifoFlowItemQueue;
import de.terministic.fabsim.components.equipment.queuecentriccontroller.IFlowItemQueue;
import de.terministic.fabsim.core.AbstractFlowItem;
import de.terministic.fabsim.core.NotYetImplementedException;

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
		Collections.sort(items, new Comparator<AbstractFlowItem>() {
			@Override
			public int compare(final AbstractFlowItem o1, final AbstractFlowItem o2) {
				final Long time1 = new Long(o1.getTimeStamps(o1.getCurrentStepNumber()).getArrivalTime());
				final long time2 = (o2.getTimeStamps(o2.getCurrentStepNumber()).getArrivalTime());
				return time1.compareTo(time2);
			}
		});

		return items;
	}

	@Override
	public ArrayList<AbstractFlowItem> addItemToList(AbstractFlowItem item, ArrayList<AbstractFlowItem> items) {
		items.add(item);
		return items;
	}

	@Override
	public IFlowItemQueue createBatchQueue(BatchDetails details) {
		// TODO Auto-generated method stub
		throw new NotYetImplementedException("Batch Queue not yet implemented");
//		return null;
	}

	@Override
	public IFlowItemQueue createQueue() {
		return new FifoFlowItemQueue();
	}

}
