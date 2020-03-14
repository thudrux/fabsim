package de.terministic.fabsim.core;

import java.util.Iterator;
import java.util.PriorityQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventListTypeManager implements IEventListManager {

	private final PriorityQueue<EventsTypeList> events = new PriorityQueue<>();
	private final IEventListTypeComparator comparator;
	private final Logger logger = LoggerFactory.getILoggerFactory().getLogger(this.getClass().getName());

	private long simulationEndTime = Long.MAX_VALUE;
	private long spentTime = 0L;

	public EventListTypeManager(IEventListTypeComparator comparator) {
		this.comparator = comparator;
	}

	public AbstractSimEvent getNextEvent() {
		if (!events.isEmpty()) {
			EventsTypeList eventTypeList = this.events.poll();
			AbstractSimEvent event = eventTypeList.pop();
			if (eventTypeList.size() > 0)
				this.events.add(eventTypeList);
			return event;
		}
		return null;
	}

	public void scheduleEvent(final AbstractSimEvent event) {
		if (((AbstractSimEvent) event).getId() == 82) {
			this.logger.info("Scheduling event: {}", event);
		}
		for (EventsTypeList eventTypeList : events) {
			if (comparator.compare(eventTypeList.get(0), event)) {
				eventTypeList.add(event);
				return;
			}
		}
		EventsTypeList eventsTypeList = new EventsTypeList();
		eventsTypeList.add(event);
		events.add(eventsTypeList);
	}

	public void unscheduleEvent(final AbstractSimEvent event) {
		if (event != null && event.getEventTime() <= simulationEndTime) {
			for (EventsTypeList eventTypeList : events) {
				if (comparator.compare(eventTypeList.get(0), event))
					eventTypeList.remove(event);
			}

		}
	}

	public int size() {
		int count = 0;
		Iterator<EventsTypeList> value = events.iterator();
		while (value.hasNext())
			count += value.next().size();
		return count;
	}

	public long getSpentTime() {
		return this.spentTime;
	}

	public void setSpentTime(final long spentTime) {
		this.spentTime = spentTime;
	}

	public void setSimulationEndTime(long endTime) {
		simulationEndTime = endTime;
	}

	public long getSimulationEndTime(long endTime) {
		return simulationEndTime;
	}

	public void logEventListState() {
		logger.trace("EventTypeList: {}", events.size());
	}
}
