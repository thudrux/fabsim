package de.terministic.fabsim.metamodel.batchrules;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.components.Batch;
import de.terministic.fabsim.metamodel.components.Lot;
import de.terministic.fabsim.metamodel.components.Recipe;
import de.terministic.fabsim.metamodel.dispatchrules.AbstractDispatchRule;

public class BasicBatchRule extends AbstractBatchRule {
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	Batch currentBatch;

	public BasicBatchRule(FabModel model) {
		super(model, "BasicBatchRule");
	}

	@Override
	public Batch choseBestBatch(final ArrayList<Batch> items) {
		return items.get(0);
	}

	private Batch createNewBatch(final AbstractFlowItem item) {
		final Recipe r = new Recipe(item.getCurrentStep().getBatchDetails().getBatchId());
		r.add(item.getCurrentStep());
		final Batch b = new Batch((FabModel) item.getModel(), r);
		b.setupForSimulation(getSimulationEngine());
		b.getTimeStampMap().put(0, item.getCurrentTimeStemp());
		return b;
	}

	@Override
	public QueueChangeAndBatches selectFirstPossibleBatch(ArrayList<AbstractFlowItem> list) {
		logger.trace("selectFirstPossibleBatch({})", list);
		final int maxBatch = list.get(0).getCurrentStep().getBatchDetails().getMaxBatch();
		final int minBatch = list.get(0).getCurrentStep().getBatchDetails().getMinBatch();
		final long maxWait = list.get(0).getCurrentStep().getBatchDetails().getMaxWait();

		Batch batch = createNewBatch(list.get(0));
		int i = 0;
		while ((batch.getSize() < maxBatch) && (list.size() > i)) {
			logger.trace("i={}", i);
			int roomInBatch = maxBatch - batch.getSize();
			logger.trace("room in batch is: {}", roomInBatch);
			final AbstractFlowItem currentItem = list.get(i);
			if (roomInBatch >= currentItem.getSize()) {
				logger.trace("Adding {} to {}", currentItem, batch);
				batch.addItem(currentItem);
			} else if (currentItem instanceof Lot) {
				AbstractFlowItem child = ((Lot) currentItem).splitOfChild(roomInBatch);
				list.add(i++, child);
				logger.trace("Adding {} to {}", child, batch);
				batch.addItem(child);
			} else {
				logger.trace("Skipping {} because it does not fit into the current batch", currentItem);
			}
			i++;
			logger.trace("end of while");
		}
		QueueChangeAndBatches result;
		result = new QueueChangeAndBatches(list, batch);
		if (batch.getSize() < minBatch) {
			if (getTime() - batch.getTimeStamps(batch.getCurrentStepNumber()).getArrivalTime() < maxWait) {
				result.setBatch(null);
				logger.trace("No valid Batch found");
			}
		}
		logger.trace("Finished, result is {}", result);
		return result;

	}

	@Override
	public QueueChangeAndBatches selectFirstPossibleBatch(ArrayList<AbstractFlowItem> possibleItems,
			AbstractDispatchRule drule) {
		possibleItems = drule.sortWithDispatchRule(possibleItems);
		return selectFirstPossibleBatch(possibleItems);
	}

}
