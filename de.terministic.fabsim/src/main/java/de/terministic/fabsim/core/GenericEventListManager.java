package de.terministic.fabsim.core;

import java.util.Iterator;
import java.util.PriorityQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericEventListManager<T extends Comparable<T>> implements IEventListManager {

	private final PriorityQueue<EventsTypeList<T>> events = new PriorityQueue<>();
	private final Logger logger = LoggerFactory.getILoggerFactory().getLogger(this.getClass().getName());

	private long simulationEndTime = Long.MAX_VALUE;
	private long spentTime = 0L;
	private final Class<T> type;

	public GenericEventListManager(Class<T> type) {
		this.type = type;
	}

	public AbstractSimEvent getNextEvent() {
		EventsTypeList<T> head = this.events.poll();
		AbstractSimEvent event = head.poll();
		if (head.size() > 0)
			this.events.add(head);
		return event;
	}

	public void scheduleEvent(final AbstractSimEvent event) {
		Iterator<EventsTypeList<T>> value = events.iterator();
		while (value.hasNext()) {
			if (type.equals(AbstractComponent.class)) {
				if (value.next().getType().equals(event.component)) {
					value.next().add(event);
					return;
				}
			}
			if (type.equals(AbstractSimEvent.class)) {
				if (value.next().getType().equals(event)) {
					value.next().add(event);
					return;
				}
			}
		}
		EventsTypeList<T> eventsTypeList;
		try {
			if (type.equals(AbstractComponent.class)) {
				eventsTypeList = new EventsTypeList<T>((T) event.getComponent());
				eventsTypeList.add(event);
				events.add(eventsTypeList);
				return;
			}
			if (type.equals(AbstractSimEvent.class)) {
				eventsTypeList = new EventsTypeList<T>((T) event);
				eventsTypeList.add(event);
				events.add(eventsTypeList);
				return;
			}
		} catch (ClassCastException e) {
		}
	}

	public void unscheduleEvent(final AbstractSimEvent event) {
		if (event != null & event.getEventTime() <= simulationEndTime) {
			Iterator<EventsTypeList<T>> value = events.iterator();
			while (value.hasNext()) {
				if (type.equals(AbstractComponent.class)) {
					if (value.next().getType().equals(event.component)) {
						value.next().remove(event);
						return;
					}
				}
				if (type.equals(AbstractSimEvent.class)) {
					if (value.next().getType().equals(event)) {
						value.next().remove(event);
						return;
					}
				}
			}
		}
	}

	public int size() {
		int count = 0;
		Iterator<EventsTypeList<T>> value = events.iterator();
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
