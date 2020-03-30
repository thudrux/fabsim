package de.terministic.fabsim.core;

import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeGroupedEventListManager implements IEventListManager {
	private final TreeMap<Long, EventsInMomentList> events = new TreeMap<>();
	private final Logger logger = LoggerFactory.getILoggerFactory().getLogger(this.getClass().getName());
	private long spentTime = 0L;

	public TimeGroupedEventListManager() {

	}

	public AbstractSimEvent getNextEvent() {
		EventsInMomentList currentEvents = this.events.firstEntry().getValue();
		while (currentEvents.isEmpty()) {
			this.events.remove(this.events.firstKey());
			currentEvents = this.events.firstEntry().getValue();
		}
		final AbstractSimEvent result = currentEvents.poll();
		return result;
	}

	public long getSpentTime() {
		return this.spentTime;
	}

	public void scheduleEvent(final AbstractSimEvent event) {
		if (event.getEventTime() <= simulationEndTime) {
			EventsInMomentList moments = this.events.get(event.getEventTime());
			if (moments == null) {
				moments = new EventsInMomentList(event.getEventTime());
				this.events.put(event.getEventTime(), moments);
			}
			moments.add(event);
		}
	}

	public void setSpentTime(final long spentTime) {
		this.spentTime = spentTime;
	}

	public int size() {
		int count = 0;
		for (EventsInMomentList moment : events.values()) {
			count += moment.size();
		}
		return count;
	}

	public void unscheduleEvent(final AbstractSimEvent event) {
		if ((event.getComponent().getName().equals("ToolGroup_2"))) {
			logger.trace("trying to unschedule event: {}", event);
		}
		if (event != null) {
			if (event.getEventTime() <= simulationEndTime) {
				final EventsInMomentList moment = this.events.get(event.getEventTime());
				moment.remove(event);
				if (moment.size() == 0) {
					events.remove(event.getEventTime());
				}
			}
		}
	}

	public void logEventListState() {
		logger.trace("EventList moments: {}", events.size());
		for (EventsInMomentList moment : events.values()) {
			for (ISimEvent event : moment) {
				logger.trace("Event left: {}", event);
			}
		}
	}

	private long simulationEndTime = Long.MAX_VALUE;

	public void setSimulationEndTime(long endTime) {
		simulationEndTime = endTime;
	}
}
