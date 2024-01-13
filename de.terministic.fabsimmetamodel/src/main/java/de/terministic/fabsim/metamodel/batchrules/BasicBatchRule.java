package de.terministic.fabsim.metamodel.batchrules;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.NotYetImplementedException;
import de.terministic.fabsim.metamodel.components.Batch;
import de.terministic.fabsim.metamodel.components.Lot;
import de.terministic.fabsim.metamodel.components.Recipe;
import de.terministic.fabsim.metamodel.dispatchRules.AbstractDispatchRule;

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
		final Batch b = new Batch(item.getModel(), r);
		b.setupForSimulation(getSimulationEngine());
		b.getTimeStampMap().put(0, item.getCurrentTimeStemp());
		return b;
	}

	@Override
	public QueueChangeAndBatches selectFirstPossibleBatch(ArrayList<IFlowItem> list) {
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
			if (roomInBatch >= list.get(i).getSize()) {
				logger.trace("Adding {} to {}", list.get(i), batch);
				batch.addItem(list.get(i));
			} else {
				AbstractFlowItem child = ((Lot) list.get(i)).splitOfChild(roomInBatch);
				list.add(i++, child);
				logger.trace("Adding {} to {}", child, batch);
				batch.addItem(child);
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

	@Deprecated
	private QueueChangeAndBatches selectPossibleBatches(ArrayList<AbstractFlowItem> list) {
		int wafersInList = 0;
		for (AbstractFlowItem item : list) {
			wafersInList += item.getSize();
		}
		logger.trace("before select FlowItemList({} Wafer): {}", wafersInList, list);
		final int maxBatch = list.get(0).getCurrentStep().getBatchDetails().getMaxBatch();
		final int minBatch = list.get(0).getCurrentStep().getBatchDetails().getMinBatch();
		final long maxWait = list.get(0).getCurrentStep().getBatchDetails().getMaxWait();
		final ArrayList<AbstractFlowItem> batches = new ArrayList<>();
		ArrayList<AbstractFlowItem> children = new ArrayList<>();
		LinkedHashMap<AbstractFlowItem, Integer> childPosition = new LinkedHashMap<>();
		boolean itemIsFinished = false;
		this.currentBatch = null;
		for (int i = 0; i < list.size(); i++) {
			final AbstractFlowItem item = list.get(i);
			itemIsFinished = false;
			if (this.currentBatch == null) {
				this.currentBatch = createNewBatch(item);
			}

			while (!itemIsFinished) {
				// check if current item fits remaining space
				if (this.currentBatch.getSize() < maxBatch) {
					final int remainingSpace = maxBatch - this.currentBatch.getSize();
					if (item.getSize() <= remainingSpace) {
						// true, put it into batch
						this.currentBatch.addItem(item);
						// item is batched completly, go to next item
						itemIsFinished = true;
						if (this.currentBatch.getSize() == maxBatch) {
							// batch is full, put it in the list, delete batch,
							// to create a new one
							// TODO overwrite batch in list?
							batches.add(this.currentBatch);
							this.logger.trace("batch is full:{}", this.currentBatch);
							this.currentBatch = null;
						}
					} else {
						// false, split it
						if (item instanceof Lot) {
							// TODO are in item only remaining items after this
							// step?

							AbstractFlowItem child = ((Lot) item).splitOfChild(remainingSpace);
							children.add(child);
							childPosition.put(child, i);
							this.currentBatch.addItem(child);
							// batch is full, put it in the list, create a new
							// batch
							batches.add(this.currentBatch);
							this.currentBatch = createNewBatch(item);
						} else
							// TODO currently it can't unbatch an existing batch
							// to split this batch and fill the remaining space.
							// this can cause errors
							throw new NotYetImplementedException();
					}
				}
			}
			// check if items remaining, no items left check for waiting time if
			// below minbatch size
			if (i == list.size() - 1) {
				if (this.currentBatch != null) {
					if (this.currentBatch.getSize() >= minBatch) {
						batches.add(this.currentBatch);
					} else if (getTime() - this.currentBatch.getTimeStamps(this.currentBatch.getCurrentStepNumber())
							.getArrivalTime() >= maxWait) {
						batches.add(this.currentBatch);
					}
				}
			}
		}
		int i = 0;
		for (AbstractFlowItem child : children) {
			list.add(childPosition.get(child) + i++, child);
		}
		// list.addAll(children);
		int wafersInList2 = 0;
		for (AbstractFlowItem item : list) {
			wafersInList2 += item.getSize();
		}
		if (wafersInList != wafersInList2) {
			throw new RuntimeException("Lost wafers while batching");
		}
		// if (!batches.isEmpty()) {
		// logger.debug("after select FlowItemList({} Wafer): {}",
		// wafersInList2, list);
		// for (AbstractFlowItem batch : batches) {
		// logger.debug("Valid Batch found: {}", batch);
		// }
		// }
		QueueChangeAndBatches result = new QueueChangeAndBatches(list, batches);
		return result;
	}

	@Deprecated
	private QueueChangeAndBatches selectPossibleBatches(ArrayList<AbstractFlowItem> possibleItems,
			final AbstractDispatchRule drule) {
		possibleItems = drule.sortWithDispatchRule(possibleItems);
		return selectPossibleBatches(possibleItems);
	}

	@Override
	public QueueChangeAndBatches selectFirstPossibleBatch(ArrayList<AbstractFlowItem> possibleItems,
			AbstractDispatchRule drule) {
		possibleItems = drule.sortWithDispatchRule(possibleItems);
		return selectFirstPossibleBatch(possibleItems);
	}

}
