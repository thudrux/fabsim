package de.terministic.fabsim.benchmarks.core.batchrules;

import java.util.ArrayList;
import java.util.List;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.batchrules.AbstractBatchRule;
import de.terministic.fabsim.metamodel.batchrules.QueueChangeAndBatches;
import de.terministic.fabsim.metamodel.components.Batch;
import de.terministic.fabsim.metamodel.components.Recipe;
import de.terministic.fabsim.metamodel.components.equipment.BatchDetails;
import de.terministic.fabsim.metamodel.dispatchRules.AbstractDispatchRule;

public final class SameProductAndStepBatchRule extends AbstractBatchRule {

	public SameProductAndStepBatchRule(final FabModel model) {
		super(model, "SameProductAndStepBatchRule");
	}

	@Override
	public Batch choseBestBatch(final ArrayList<Batch> items) {
		return items == null || items.isEmpty() ? null : items.get(0);
	}

	@Override
	public QueueChangeAndBatches selectFirstPossibleBatch(final ArrayList<AbstractFlowItem> list) {
		return new QueueChangeAndBatches(list, createFirstValidBatch(list));
	}

	@Override
	public QueueChangeAndBatches selectFirstPossibleBatch(final ArrayList<AbstractFlowItem> possibleItems,
			final AbstractDispatchRule drule) {
		final ArrayList<AbstractFlowItem> sortedItems = new ArrayList<>(possibleItems);
		if (drule != null) {
			drule.sortWithDispatchRule(sortedItems);
		}
		return new QueueChangeAndBatches(possibleItems, createFirstValidBatch(sortedItems));
	}

	private Batch createFirstValidBatch(final List<AbstractFlowItem> items) {
		if (items == null || items.isEmpty()) {
			return null;
		}
		for (int first = 0; first < items.size(); first++) {
			final AbstractFlowItem firstItem = items.get(first);
			final BatchDetails details = firstItem.getCurrentStep().getBatchDetails();
			if (details == null) {
				continue;
			}
			final ArrayList<AbstractFlowItem> selectedItems = new ArrayList<>();
			int selectedSize = 0;
			for (int index = first; index < items.size(); index++) {
				final AbstractFlowItem item = items.get(index);
				if (!isSameProductAndStep(firstItem, item)) {
					continue;
				}
				if (selectedSize + item.getSize() > details.getMaxBatch()) {
					continue;
				}
				selectedItems.add(item);
				selectedSize += item.getSize();
				if (selectedSize == details.getMaxBatch()) {
					break;
				}
			}
			if (selectedSize >= details.getMinBatch() || isMaxWaitReached(firstItem, details)) {
				return createBatch(selectedItems);
			}
		}
		return null;
	}

	private boolean isSameProductAndStep(final AbstractFlowItem firstItem, final AbstractFlowItem item) {
		if (firstItem == null || item == null || firstItem.getProduct() == null || item.getProduct() == null
				|| firstItem.getCurrentStep() == null || item.getCurrentStep() == null) {
			return false;
		}
		return firstItem.getProduct().getName().equals(item.getProduct().getName())
				&& firstItem.getCurrentStep().getName().equals(item.getCurrentStep().getName());
	}

	private boolean isMaxWaitReached(final AbstractFlowItem firstItem, final BatchDetails details) {
		if (details.getMaxWait() == Long.MAX_VALUE) {
			return false;
		}
		return firstItem.getModel().getSimulationEngine().getTime()
				- firstItem.getCurrentTimeStemp().getArrivalTime() >= details.getMaxWait();
	}

	private Batch createBatch(final List<AbstractFlowItem> items) {
		if (items.isEmpty()) {
			return null;
		}
		final AbstractFlowItem firstItem = items.get(0);
		final Recipe recipe = new Recipe(firstItem.getCurrentStep().getBatchDetails().getBatchId());
		recipe.add(firstItem.getCurrentStep());
		final Batch batch = new Batch((FabModel) firstItem.getModel(), recipe);
		batch.setupForSimulation(firstItem.getModel().getSimulationEngine());
		batch.getTimeStampMap().put(0, firstItem.getCurrentTimeStemp());
		for (final AbstractFlowItem item : items) {
			batch.addItem(item);
		}
		return batch;
	}
}
