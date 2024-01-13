package de.terministic.fabsim.metamodel.components.equipment.queuecentriccontroller;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.PriorityQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.terministic.fabsim.core.IFlowItem;
import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.components.Batch;
import de.terministic.fabsim.metamodel.components.Lot;
import de.terministic.fabsim.metamodel.components.Recipe;
import de.terministic.fabsim.metamodel.components.equipment.BatchDetails;

public class FifoBatchFlowItemQueue extends PriorityQueue<IFlowItem> implements IFlowItemQueue {

	/**
	 * 
	 */
	protected Logger logger = LoggerFactory.getILoggerFactory().getLogger(this.getClass().getName());
	private static final long serialVersionUID = 2004682036381685856L;

	private int waferCount = 0;
	private BatchDetails details;

	public FifoBatchFlowItemQueue(BatchDetails details) {
		super(new Comparator<IFlowItem>() {
			@Override
			public int compare(final IFlowItem o1, final IFlowItem o2) {
				final Long time1 = Long.valueOf(((AbstractFlowItem)o1).getTimeStamps(((AbstractFlowItem)o1).getCurrentStepNumber()).getArrivalTime());
				final Long time2 = Long.valueOf(((AbstractFlowItem)o2).getTimeStamps(((AbstractFlowItem)o2).getCurrentStepNumber()).getArrivalTime());
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
	public IFlowItem takeHighestPriorityFlowItem() {
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
			AbstractFlowItem firstInQueue = (AbstractFlowItem)peek();
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
				result = createNewBatch(firstInQueue);
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
	public boolean addFlowItem(IFlowItem item) {
//		logger.info("[{}] START addFlowItem for {} with queue looking like {}", item.getTime(), item, this);
		waferCount += item.getSize();
		boolean result = this.add(item);
//		logger.info("[{}] END addFlowItem for {} with queue looking like {}", item.getTime(), item, this);
		return result;
	}

	LinkedHashMap<IFlowItem, IFlowItemQueue> candidateMap;

	@Override
	public boolean addFlowItemCandidate(IFlowItem item, IFlowItemQueue queue) {
		if (candidateMap == null) {
			candidateMap = new LinkedHashMap<>();
		}
		candidateMap.put(item, queue);
		return addFlowItem(item);

	}

	@Override
	public IFlowItem takeCandidate(IFlowItem item) { // TODO take candidate
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
	public IFlowItem takeBestCandidate() {
		return this.takeCandidate(this.lookAtHighestPriorityFlowItem());
	}
}
