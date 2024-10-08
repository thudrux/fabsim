package de.terministic.fabsim.metamodel.components.equipment.queuecentriccontroller;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.PriorityQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.terministic.fabsim.metamodel.AbstractFlowItem;

public class FifoFlowItemQueue extends PriorityQueue<AbstractFlowItem> implements IFlowItemQueue {

	/**
	 * 
	 */
	protected Logger logger = LoggerFactory.getILoggerFactory().getLogger(this.getClass().getName());
	private static final long serialVersionUID = 2004682036381685856L;

	private int waferCount = 0;

	public FifoFlowItemQueue() {
		super(new Comparator<AbstractFlowItem>() {
			@Override
			public int compare(final AbstractFlowItem o1, final AbstractFlowItem o2) {
				final Long time1 = Long.valueOf(o1.getTimeStamps(o1.getCurrentStepNumber()).getArrivalTime());
				final long time2 = (o2.getTimeStamps(o2.getCurrentStepNumber()).getArrivalTime());
				return time1.compareTo(time2);
			}
		});
	}

	@Override
	public int sizeInProcessUnits() {
		return this.size();
	}

	@Override
	public int sizeInWafers() {
		return this.waferCount;
	}

	@Override
	public AbstractFlowItem takeHighestPriorityFlowItem() {
		AbstractFlowItem item = this.poll();
		waferCount -= item.getSize();
		return item;
	}

	@Override
	public AbstractFlowItem lookAtHighestPriorityFlowItem() {
		return this.peek();
	}

	@Override
	public boolean addFlowItem(AbstractFlowItem item) {
//		logger.info("[{}] START addFlowItem for {} with queue looking like {}", item.getTime(), item, this);
		waferCount += item.getSize();
		return this.add(item);
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
	public AbstractFlowItem takeCandidate(AbstractFlowItem item) {
		if (candidateMap != null) {
			candidateMap.get(item).takeCandidate(item);
		}
		{
			this.remove(item);
		}
		return item;
	}

	@Override
	public AbstractFlowItem takeBestCandidate() {
		return this.takeCandidate(this.lookAtHighestPriorityFlowItem());
	}
}
