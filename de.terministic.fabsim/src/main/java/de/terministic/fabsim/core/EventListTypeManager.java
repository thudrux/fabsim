package de.terministic.fabsim.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventListTypeManager implements IEventListManager {

	private final ArrayList<EventsTypeList> events = new ArrayList<>();
	private final IEventListTypeComparator comparator;
	private final Logger logger = LoggerFactory.getILoggerFactory().getLogger(this.getClass().getName());
	private long simulationEndTime = Long.MAX_VALUE;
	private long spentTime = 0L;

	public EventListTypeManager(IEventListTypeComparator comparator) {
		this.comparator = comparator;
	}

	public AbstractSimEvent getNextEvent() {
		if (events.size() > 0) {
			EventsTypeList eventTypeList = events.get(0);
			AbstractSimEvent event = eventTypeList.poll();
			if (eventTypeList.size() > 0) {
				if (events.get(1) != null) {
					if (eventTypeList.get(0).getEventTime() >= events.get(1).get(0).getEventTime()) {
						Collections.sort(events);
					}
				}
			} else {
				events.remove(0);
			}
			return event;
		}
		return null;
	}

	public void scheduleEvent(AbstractSimEvent event) {
		if (!events.isEmpty()) {
			for (EventsTypeList eventTypeList : events) {
				if (comparator.compare(eventTypeList.get(0), event)) {
					eventTypeList.add(event);
					if (eventTypeList.get(0).equals(event) && eventTypeList.size() > 1)
						Collections.sort(events);
					return;
				}
			}
		}
		EventsTypeList eventsTypeList = new EventsTypeList();
		eventsTypeList.add(event);
		events.add(eventsTypeList);
		Collections.sort(events);
	}

	public void unscheduleEvent(AbstractSimEvent event) {
		for (int i = 0; i < events.size(); i++) {
			EventsTypeList eventsTypeList = events.get(i);
			if (comparator.compare(eventsTypeList.get(0), event)) {
				if (eventsTypeList.get(0).equals(event)) {
					eventsTypeList.remove(0);
					if (eventsTypeList.size() > 0) {
						if (events.get(i + 1) != null) {
							if (eventsTypeList.get(0).getEventTime() >= events.get(i + 1).get(0).getEventTime()) {
								Collections.sort(events);
							}
						}
					} else {
						events.remove(i);
					}
					return;

				} else {
					eventsTypeList.remove(event);
					return;
				}
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
