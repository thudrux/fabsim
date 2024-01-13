package de.terministic.fabsim.metamodel.components.equipment.queuecentriccontroller;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.PriorityQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.terministic.fabsim.core.IFlowItem;
import de.terministic.fabsim.metamodel.AbstractFlowItem;

public class FifoFlowItemQueue extends PriorityQueue<IFlowItem> implements IFlowItemQueue {

	/**
	 * 
	 */
	protected Logger logger = LoggerFactory.getILoggerFactory().getLogger(this.getClass().getName());
	private static final long serialVersionUID = 2004682036381685856L;

	private int waferCount = 0;

	public FifoFlowItemQueue() {
		super(new Comparator<IFlowItem>() {
			@Override
			public int compare(final IFlowItem o1, final IFlowItem o2) {
				final Long time1 = Long.valueOf(((AbstractFlowItem)o1).getTimeStamps(((AbstractFlowItem)o1).getCurrentStepNumber()).getArrivalTime());
				final Long time2 = Long.valueOf(((AbstractFlowItem)o2).getTimeStamps(((AbstractFlowItem)o2).getCurrentStepNumber()).getArrivalTime());
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
	public IFlowItem takeHighestPriorityFlowItem() {
		IFlowItem item = this.poll();
		waferCount -= item.getSize();
		return item;
	}

	@Override
	public IFlowItem lookAtHighestPriorityFlowItem() {
		return this.peek();
	}

	@Override
	public boolean addFlowItem(IFlowItem item) {
//		logger.info("[{}] START addFlowItem for {} with queue looking like {}", item.getTime(), item, this);
		waferCount += item.getSize();
		return this.add(item);
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
	public IFlowItem takeCandidate(IFlowItem item) {
		if (candidateMap != null) {
			candidateMap.get(item).takeCandidate(item);
		}
		{
			this.remove(item);
		}
		return item;
	}

	@Override
	public IFlowItem takeBestCandidate() {
		return this.takeCandidate(this.lookAtHighestPriorityFlowItem());
	}
}
