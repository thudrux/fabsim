package de.terministic.fabsim.metamodel.dispatchRules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.components.Batch;
import de.terministic.fabsim.metamodel.components.Lot;
import de.terministic.fabsim.metamodel.components.ProcessStep;
import de.terministic.fabsim.metamodel.components.equipment.BatchDetails;
import de.terministic.fabsim.metamodel.components.equipment.queuecentriccontroller.FifoBatchFlowItemQueue;
import de.terministic.fabsim.metamodel.components.equipment.queuecentriccontroller.FifoFlowItemQueue;
import de.terministic.fabsim.metamodel.components.equipment.queuecentriccontroller.IFlowItemQueue;

public class CriticalRatio extends AbstractDispatchRule {

	public CriticalRatio() {
		super("Critical Ratio");
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
				final int ratioComparison = Double.compare(calculateCriticalRatio(first),
						calculateCriticalRatio(second));
				if (ratioComparison != 0) {
					return ratioComparison;
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
	public ArrayList<AbstractFlowItem> addItemToList(final AbstractFlowItem item,
			final ArrayList<AbstractFlowItem> items) {
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

	private double calculateCriticalRatio(final AbstractFlowItem item) {
		if (item instanceof Lot) {
			return calculateCriticalRatio((Lot) item);
		}
		if (item instanceof Batch) {
			return calculateCriticalRatio((Batch) item);
		}
		return Double.POSITIVE_INFINITY;
	}

	private double calculateCriticalRatio(final Lot lot) {
		if (lot == null) {
			return Double.POSITIVE_INFINITY;
		}

		final long remainingProcessingTime = calculateRemainingProcessingTime(lot);
		if (remainingProcessingTime <= 0L) {
			return Double.POSITIVE_INFINITY;
		}

		final long timeUntilDueDate = lot.getDueDate() - lot.getTime();
		return timeUntilDueDate / (double) remainingProcessingTime;
	}

	private double calculateCriticalRatio(final Batch batch) {
		if (batch == null || batch.getItems().isEmpty()) {
			return Double.POSITIVE_INFINITY;
		}

		double lowestCriticalRatio = Double.POSITIVE_INFINITY;
		for (final AbstractFlowItem child : batch.getItems()) {
			lowestCriticalRatio = Math.min(lowestCriticalRatio, calculateCriticalRatio(child));
		}
		return lowestCriticalRatio;
	}

	private long calculateRemainingProcessingTime(final AbstractFlowItem item) {
		if (item instanceof Lot) {
			return calculateRemainingProcessingTime((Lot) item);
		}
		if (item instanceof Batch) {
			return calculateRemainingProcessingTime((Batch) item);
		}
		return Long.MAX_VALUE;
	}

	private long calculateRemainingProcessingTime(final Lot lot) {
		if (lot == null || lot.getRecipe() == null) {
			return Long.MAX_VALUE;
		}

		final int currentStepNumber = lot.getCurrentStepNumber();
		if (currentStepNumber < 0 || currentStepNumber >= lot.getRecipe().size()) {
			return Long.MAX_VALUE;
		}

		long remainingProcessingTime = 0L;
		for (int i = currentStepNumber; i < lot.getRecipe().size(); i++) {
			final ProcessStep step = lot.getRecipe().get(i);
			remainingProcessingTime += Math.max(0L, step.getLoadTime());
			remainingProcessingTime += Math.max(0L, step.getDuration(lot));
			remainingProcessingTime += Math.max(0L, step.getUnloadTime());
		}
		return remainingProcessingTime;
	}

	private long calculateRemainingProcessingTime(final Batch batch) {
		if (batch == null || batch.getItems().isEmpty()) {
			return Long.MAX_VALUE;
		}

		long shortestRemainingProcessingTime = Long.MAX_VALUE;
		for (final AbstractFlowItem child : batch.getItems()) {
			shortestRemainingProcessingTime = Math.min(shortestRemainingProcessingTime,
					calculateRemainingProcessingTime(child));
		}
		return shortestRemainingProcessingTime;
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
