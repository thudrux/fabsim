package de.terministic.fabsim.components.equipment.queuecentriccontroller;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.PriorityQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.terministic.fabsim.components.Batch;
import de.terministic.fabsim.components.Lot;
import de.terministic.fabsim.components.Recipe;
import de.terministic.fabsim.components.equipment.BatchDetails;
import de.terministic.fabsim.core.AbstractFlowItem;

public class FifoBatchFlowItemQueue extends PriorityQueue<AbstractFlowItem> implements IFlowItemQueue {

	/**
	 * 
	 */
	protected Logger logger = LoggerFactory.getILoggerFactory().getLogger(this.getClass().getName());
	private static final long serialVersionUID = 2004682036381685856L;

	private int waferCount = 0;
	private BatchDetails details;

	public FifoBatchFlowItemQueue(BatchDetails details) {
		super(new Comparator<AbstractFlowItem>() {
			@Override
			public int compare(final AbstractFlowItem o1, final AbstractFlowItem o2) {
				final Long time1 = Long.valueOf(o1.getTimeStamps(o1.getCurrentStepNumber()).getArrivalTime());
				final long time2 = (o2.getTimeStamps(o2.getCurrentStepNumber()).getArrivalTime());
				return time1.compareTo(time2);
			}
		});
		this.details = details;
	}

	@Override
	public int sizeInProcessUnits() {
		int result = waferCount / details.getMaxBatch();
		if (waferCount % details.getMaxBatch() > details.getMinBatch()) {
			result++;
		}
		return result;
	}

	@Override
	public int sizeInWafers() {
		return this.waferCount;
	}

	@Override
	public AbstractFlowItem takeHighestPriorityFlowItem() {
		return this.takeCandidate(this.lookAtHighestPriorityFlowItem());
	}

	private Batch createNewBatch(final AbstractFlowItem item) {
		final Recipe r = new Recipe(item.getCurrentStep().getBatchDetails().getBatchId());
		r.add(item.getCurrentStep());
		final Batch b = new Batch(item.getModel(), r);
		b.setupForSimulation(item.getModel().getSimulationEngine());
		b.getTimeStampMap().put(0, item.getCurrentTimeStemp());
		return b;
	}

	private AbstractFlowItem createCandidate() {
//		logger.info("START createCandidate with queue looking like {}", this);
		AbstractFlowItem result;
		if (this.isEmpty()) {
			result = null;
		} else {
			AbstractFlowItem firstInQueue = peek();
			long currentTime = firstInQueue.getTime();
			long arrival = firstInQueue.getTimeStamps(firstInQueue.getCurrentStepNumber()).getArrivalTime();
			long waitedTime = currentTime - arrival;
//			logger.info("Wafer count: {}, first elements arrival {}, max wait:{}, total waited: {}", waferCount,
//					arrival, details.getMaxWait(), waitedTime);
			if ((waferCount < details.getMinBatch()) && (waitedTime < details.getMaxWait())) {// TODO
																								// time
																								// elapse
																								// =>
																								// create
																								// candidate
				result = null;
			} else {
				result = createNewBatch(this.peek());
			}
		}
//		logger.info("END createCandidate with {} and queue looking like {}", result, this);
		return result;
	}

	@Override
	public AbstractFlowItem lookAtHighestPriorityFlowItem() { // TODO create shallow candidate, done?
		return createCandidate();
	}

	@Override
	public boolean addFlowItem(AbstractFlowItem item) {
//		logger.info("[{}] START addFlowItem for {} with queue looking like {}", item.getTime(), item, this);
		waferCount += item.getSize();
		boolean result = this.add(item);
//		logger.info("[{}] END addFlowItem for {} with queue looking like {}", item.getTime(), item, this);
		return result;
	}

	LinkedHashMap<AbstractFlowItem, IFlowItemQueue> candidateMap;

	@Override
	public boolean addFlowItemCandidate(AbstractFlowItem item, IFlowItemQueue queue) {
		if (candidateMap == null) {
			candidateMap = new LinkedHashMap<>();
		}
		candidateMap.put(item, queue);
		return addFlowItem(item);

	}

	@Override
	public AbstractFlowItem takeCandidate(AbstractFlowItem item) { // TODO take candidate
//		logger.info("[{}] START takeCandidate for {} with queue looking like {}", item.getTime(), item, this);
		if (candidateMap != null) {
			candidateMap.get(item).takeCandidate(item);
		} else {
			Batch batch = (Batch) item;
			int remainingSize = details.getMaxBatch();
			while ((remainingSize > 0) && (this.size() > 0)) {
				Lot lot = (Lot) this.peek();
				if (lot.getCurrentLotSize() <= remainingSize) {
					waferCount -= lot.getCurrentLotSize();
					batch.addItem(this.poll());
					remainingSize -= lot.getCurrentLotSize();
				} else {
					batch.addItem(lot.splitOfChild(remainingSize));
					waferCount -= remainingSize;
					remainingSize = 0;
				}
			}
		}
//		logger.info("[{}] END takeCandidate for {} with queue looking like {}", item.getTime(), item, this);
		return item;
	}

	@Override
	public AbstractFlowItem takeBestCandidate() {
		return this.takeCandidate(this.lookAtHighestPriorityFlowItem());
	}
}
