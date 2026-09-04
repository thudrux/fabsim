package de.terministic.fabsim.metamodel.dispatchrules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.components.Batch;
import de.terministic.fabsim.metamodel.components.Lot;
import de.terministic.fabsim.metamodel.components.equipment.BatchDetails;
import de.terministic.fabsim.metamodel.components.equipment.queuecentriccontroller.FifoBatchFlowItemQueue;
import de.terministic.fabsim.metamodel.components.equipment.queuecentriccontroller.FifoFlowItemQueue;
import de.terministic.fabsim.metamodel.components.equipment.queuecentriccontroller.IFlowItemQueue;

public class FIFO extends AbstractDispatchRule {
	protected Logger logger = LoggerFactory.getILoggerFactory().getLogger(this.getClass().getName());

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
				return Long.compare(getArrivalTime(o1), getArrivalTime(o2));
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
//		logger.info("START createBatchQueue for {}", details);
		return new FifoBatchFlowItemQueue(details);
	}

	@Override
	public IFlowItemQueue createQueue() {
//		logger.info("START createQueue");
		return new FifoFlowItemQueue();
	}

	private long getArrivalTime(final AbstractFlowItem item) {
		if (item instanceof Lot) {
			return getArrivalTime((Lot) item);
		}
		if (item instanceof Batch) {
			return getArrivalTime((Batch) item);
		}
		if (item == null) {
			return Long.MAX_VALUE;
		}

		final int currentStepNumber = item.getCurrentStepNumber();
		if (currentStepNumber < 0) {
			return Long.MAX_VALUE;
		}
		return item.getTimeStamps(currentStepNumber).getArrivalTime();
	}

	private long getArrivalTime(final Lot lot) {
		if (lot == null) {
			return Long.MAX_VALUE;
		}

		final int currentStepNumber = lot.getCurrentStepNumber();
		if (currentStepNumber < 0) {
			return Long.MAX_VALUE;
		}
		return lot.getTimeStamps(currentStepNumber).getArrivalTime();
	}

	private long getArrivalTime(final Batch batch) {
		if (batch == null || batch.getItems().isEmpty()) {
			return Long.MAX_VALUE;
		}

		long earliestArrivalTime = Long.MAX_VALUE;
		for (final AbstractFlowItem item : batch.getItems()) {
			earliestArrivalTime = Math.min(earliestArrivalTime, getArrivalTime(item));
		}
		return earliestArrivalTime;
	}

}
