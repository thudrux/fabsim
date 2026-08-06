package de.terministic.fabsim.metamodel.examples.minifab;

import java.util.ArrayList;
import java.util.List;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.batchrules.AbstractBatchRule;
import de.terministic.fabsim.metamodel.batchrules.QueueChangeAndBatches;
import de.terministic.fabsim.metamodel.components.Batch;
import de.terministic.fabsim.metamodel.components.Recipe;
import de.terministic.fabsim.metamodel.dispatchRules.AbstractDispatchRule;

public class MiniFabBatchRule extends AbstractBatchRule {

	public MiniFabBatchRule(final FabModel model) {
		super(model, "MiniFabBatchRule");
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
		if (items == null || items.size() < MiniFab.LOTS_PER_STATION1_BATCH) {
			return null;
		}
		for (int first = 0; first < items.size() - 2; first++) {
			for (int second = first + 1; second < items.size() - 1; second++) {
				for (int third = second + 1; third < items.size(); third++) {
					final ArrayList<AbstractFlowItem> selectedItems = new ArrayList<>();
					selectedItems.add(items.get(first));
					selectedItems.add(items.get(second));
					selectedItems.add(items.get(third));
					if (isValidMiniFabBatch(selectedItems)) {
						return createBatch(selectedItems);
					}
				}
			}
		}
		return null;
	}

	private boolean isValidMiniFabBatch(final List<AbstractFlowItem> items) {
		if (items.size() != MiniFab.LOTS_PER_STATION1_BATCH) {
			return false;
		}
		final String stepName = getStepName(items.get(0));
		if (!MiniFab.STEP_S1.equals(stepName) && !MiniFab.STEP_S5.equals(stepName)) {
			return false;
		}
		int twCount = 0;
		boolean containsPa = false;
		boolean containsPb = false;
		for (final AbstractFlowItem item : items) {
			if (!stepName.equals(getStepName(item))) {
				return false;
			}
			final String productName = getProductName(item);
			switch (productName) {
			case MiniFab.PRODUCT_TW:
				twCount++;
				break;
			case MiniFab.PRODUCT_PA:
				containsPa = true;
				break;
			case MiniFab.PRODUCT_PB:
				containsPb = true;
				break;
			default:
				break;
			}
		}
		if (MiniFab.STEP_S1.equals(stepName)) {
			return twCount <= 1;
		}
		return !(containsPa && containsPb);
	}

	private Batch createBatch(final List<AbstractFlowItem> items) {
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

	private String getStepName(final AbstractFlowItem item) {
		if (item == null || item.getCurrentStep() == null) {
			return "";
		}
		return item.getCurrentStep().getName();
	}

	private String getProductName(final AbstractFlowItem item) {
		if (item == null || item.getProduct() == null) {
			return "";
		}
		return item.getProduct().getName();
	}
}
