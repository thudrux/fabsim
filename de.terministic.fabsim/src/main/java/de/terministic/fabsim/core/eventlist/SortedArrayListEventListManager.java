package de.terministic.fabsim.core.eventlist;

import java.util.ArrayList;

import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.core.IEventListManager;

public class SortedArrayListEventListManager extends ArrayList<AbstractSimEvent> implements IEventListManager {

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
		long eventTime = event.getEventTime();
		if ((this.size() < 1) || comp.compare(this.get(0), event) > 0) {
			this.add(0, event);
		} else if (comp.compare(this.get(this.size() - 1), event) <= 0) {
			this.add(this.size(), event);
		} else {
			boolean found = false;
			int lowerBound = 0;
			int upperBound = this.size() - 1;
			while (!found) {
				if (lowerBound + 1 >= upperBound) {
					found = true;
					this.add(upperBound, event);
				} else {
					int testPoint = lowerBound + (upperBound - lowerBound) / 2;

					int res = comp.compare(this.get(testPoint), event);
					if (res > 0) {
						upperBound = testPoint;
					} else {
						lowerBound = testPoint;
					}
				}
			}

		}
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
