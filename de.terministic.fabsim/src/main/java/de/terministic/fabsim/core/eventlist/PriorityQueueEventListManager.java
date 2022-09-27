package de.terministic.fabsim.core.eventlist;

import java.util.PriorityQueue;

import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.core.IEventListManager;

public class PriorityQueueEventListManager extends PriorityQueue<AbstractSimEvent> implements IEventListManager {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1061048431973521816L;
	long simEndTime;
	private long spentTime;

	@Override
	public AbstractSimEvent getNextEvent() {
		// TODO Auto-generated method stub
		return this.poll();
	}

	@Override
	public void scheduleEvent(AbstractSimEvent event) {
		this.add(event);
	}

	@Override
	public void unscheduleEvent(AbstractSimEvent event) {
		this.remove(event);
	}

	@Override
	public void setSimulationEndTime(long endTime) {
		this.simEndTime = endTime;
	}

	@Override
	public long getSpentTime() {
		return spentTime;
	}

	@Override
	public void setSpentTime(final long spentTime) {
		this.spentTime = spentTime;
	}

}
