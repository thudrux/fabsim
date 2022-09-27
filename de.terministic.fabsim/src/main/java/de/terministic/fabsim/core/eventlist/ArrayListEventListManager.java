package de.terministic.fabsim.core.eventlist;

import java.util.ArrayList;

import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.core.IEventListManager;

public class ArrayListEventListManager extends ArrayList<AbstractSimEvent> implements IEventListManager {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1061048431973521816L;
	long simEndTime;
	private long spentTime;

	private EventComparator comp = new EventComparator();

	@Override
	public AbstractSimEvent getNextEvent() {
		return this.remove(0);
	}

	@Override
	public void scheduleEvent(AbstractSimEvent event) {
		this.add(event);
		this.sort(comp);
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
