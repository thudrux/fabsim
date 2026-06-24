package de.terministic.fabsim.metamodel.dispatchRules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.components.equipment.BatchDetails;
import de.terministic.fabsim.metamodel.components.equipment.queuecentriccontroller.FifoBatchFlowItemQueue;
import de.terministic.fabsim.metamodel.components.equipment.queuecentriccontroller.FifoFlowItemQueue;
import de.terministic.fabsim.metamodel.components.equipment.queuecentriccontroller.IFlowItemQueue;
import de.terministic.fabsim.metamodel.components.ProcessStep;
import de.terministic.fabsim.metamodel.components.ProcessStep.ProcessType;

public class SRPT extends AbstractDispatchRule {

	public SRPT() {
		super("SRPT");
	}

	@Override
	public AbstractFlowItem getBestItem(final ArrayList<AbstractFlowItem> items) {
		final ArrayList<AbstractFlowItem> sortedItems = sortWithDispatchRule(items);
		if (sortedItems.isEmpty()) {
			return null;
		}
		return sortedItems.get(0);
	}

	@Override
	public ArrayList<AbstractFlowItem> sortWithDispatchRule(final ArrayList<AbstractFlowItem> items) {
		Collections.sort(items, new Comparator<AbstractFlowItem>() {
			@Override
			public int compare(final AbstractFlowItem first, final AbstractFlowItem second) {
				final int remainingComparison = Long.compare(
						calculateRemainingProcessingTime(first),
						calculateRemainingProcessingTime(second));
				if (remainingComparison != 0) {
					return remainingComparison;
				}

				final long arrivalTime1 = getArrivalTime(first);
				final long arrivalTime2 = getArrivalTime(second);
				final int arrivalComparison = Long.compare(arrivalTime1, arrivalTime2);
				if (arrivalComparison != 0) {
					return arrivalComparison;
				}

				return Long.compare(first.getId(), second.getId());
			}
		});

		return items;
	}

	@Override
	public ArrayList<AbstractFlowItem> addItemToList(final AbstractFlowItem item, final ArrayList<AbstractFlowItem> items) {
		items.add(item);
		return items;
	}

	@Override
	public IFlowItemQueue createBatchQueue(final BatchDetails details) {
		return new FifoBatchFlowItemQueue(details);
	}

	@Override
	public IFlowItemQueue createQueue() {
		return new FifoFlowItemQueue();
	}

	private long calculateRemainingProcessingTime(final AbstractFlowItem item) {
		if (item == null || item.getRecipe() == null) {
			return Long.MAX_VALUE;
		}

		final int currentStepNumber = item.getCurrentStepNumber();
		if (currentStepNumber < 0 || currentStepNumber >= item.getRecipe().size()) {
			return Long.MAX_VALUE;
		}

		long remainingProcessingTime = 0L;
		for (int i = currentStepNumber; i < item.getRecipe().size(); i++) {
			final ProcessStep step = item.getRecipe().get(i);
			remainingProcessingTime += Math.max(0L, step.getLoadTime());
			remainingProcessingTime += Math.max(0L, step.getDuration(item));
			remainingProcessingTime += Math.max(0L, step.getUnloadTime());
		}
		return remainingProcessingTime;
	}

	private long getArrivalTime(final AbstractFlowItem item) {
		if (item == null) {
			return Long.MAX_VALUE;
		}

		final int currentStepNumber = item.getCurrentStepNumber();
		if (currentStepNumber < 0) {
			return Long.MAX_VALUE;
		}
		return item.getTimeStamps(currentStepNumber).getArrivalTime();
	}
}
