package de.terministic.fabsim.metamodel.dispatchRules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.NotYetImplementedException;
import de.terministic.fabsim.metamodel.components.equipment.BatchDetails;
import de.terministic.fabsim.metamodel.components.equipment.queuecentriccontroller.IFlowItemQueue;

public class FIFOwithMaxWait extends AbstractDispatchRule {

	ArrayList<AbstractFlowItem> listMaxWaitExceeded = new ArrayList<>();
	ArrayList<AbstractFlowItem> listMaxWaitNotExceeded = new ArrayList<>();

	public FIFOwithMaxWait() {
		super("FIFOwithMaxWait");
	}

	private int compareWaitingTimes(final long time1, final long time2) {
		int ret;
		if (time1 < time2) {
			ret = -1;
		} else if (time1 == time2) {
			ret = 0;
		} else {
			ret = 1;
		}
		return ret;
	}

	private void createListsForMaxWait(final ArrayList<AbstractFlowItem> items) {
		for (final AbstractFlowItem item : items) {
			if (item.getCurrentStep().getBatchDetails() != null) {
				final long waitingTime = item.getTime()
						- item.getTimeStamps(item.getCurrentStepNumber()).getArrivalTime();
				;
				final long maxWaitingTime = item.getCurrentStep().getBatchDetails().getMaxWait();
				if (waitingTime > maxWaitingTime) {
					this.listMaxWaitExceeded.add(item);
				} else {
					this.listMaxWaitNotExceeded.add(item);
				}
			} else {
				this.listMaxWaitNotExceeded.add(item);
			}
		}
	}

	@Override
	public AbstractFlowItem getBestItem(final ArrayList<AbstractFlowItem> items) {
		final ArrayList<AbstractFlowItem> l = sortWithDispatchRule(items);
		if (l.size() > 0)
			return l.get(0);
		else
			return null;
	}

	private void sortListMaxWaitExceed() {
		Collections.sort(this.listMaxWaitExceeded, new Comparator<AbstractFlowItem>() {
			@Override
			public int compare(final AbstractFlowItem o1, final AbstractFlowItem o2) {
				final long time1 = -(o1.getTimeStamps(o1.getCurrentStepNumber()).getArrivalTime()
						+ o1.getCurrentStep().getBatchDetails().getMaxWait());
				final long time2 = -(o2.getTimeStamps(o2.getCurrentStepNumber()).getArrivalTime()
						+ o2.getCurrentStep().getBatchDetails().getMaxWait());
				return compareWaitingTimes(time1, time2);
			}
		});
	}

	private void sortListMaxWaitNotExceed() {
		Collections.sort(this.listMaxWaitNotExceeded, new Comparator<AbstractFlowItem>() {
			@Override
			public int compare(final AbstractFlowItem o1, final AbstractFlowItem o2) {
				final long time1 = -o1.getTimeStamps(o1.getCurrentStepNumber()).getArrivalTime();
				final long time2 = -o2.getTimeStamps(o2.getCurrentStepNumber()).getArrivalTime();
				return compareWaitingTimes(time1, time2);
			}
		});
	}

	@Override
	public ArrayList<AbstractFlowItem> sortWithDispatchRule(ArrayList<AbstractFlowItem> items) {
		createListsForMaxWait(items);
		sortListMaxWaitExceed();
		sortListMaxWaitNotExceed();
		this.listMaxWaitExceeded.addAll(this.listMaxWaitNotExceeded);
		return this.listMaxWaitExceeded;
	}

	@Override
	public ArrayList<AbstractFlowItem> addItemToList(AbstractFlowItem item, ArrayList<AbstractFlowItem> items) {
		items.add(item);
		return items;
	}

	@Override
	public IFlowItemQueue createBatchQueue(BatchDetails details) {
		// TODO Auto-generated method stub
//		return null;
		throw new NotYetImplementedException();
	}

	@Override
	public IFlowItemQueue createQueue() {
		// TODO Auto-generated method stub
//		return null;
		throw new NotYetImplementedException();
	}
}
