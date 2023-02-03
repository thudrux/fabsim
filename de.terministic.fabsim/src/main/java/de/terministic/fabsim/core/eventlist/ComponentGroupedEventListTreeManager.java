package de.terministic.fabsim.core.eventlist;

import java.util.LinkedHashMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.terministic.fabsim.core.AbstractComponent;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.core.IEventListManager;
import de.terministic.fabsim.core.ISimEvent;

public class ComponentGroupedEventListTreeManager implements IEventListManager {
	private final TreeSet<ComponentEventList> events = new TreeSet<>();
	private final LinkedHashMap<AbstractComponent, ComponentEventList> componentMap = new LinkedHashMap<>();
	private final Logger logger = LoggerFactory.getILoggerFactory().getLogger(this.getClass().getName());
	private long spentTime = 0L;

	public ComponentGroupedEventListTreeManager() {

	}

	public AbstractSimEvent getNextEvent() {
		ComponentEventList compEvents = this.events.first();
		this.events.remove(compEvents);
		final AbstractSimEvent result = compEvents.poll();
		if (compEvents.size() > 0) {
			events.add(compEvents);
		} else {
			componentMap.remove(result.getComponent());
		}
		return result;
	}

	public long getSpentTime() {
		return this.spentTime;
	}

	public void scheduleEvent(final AbstractSimEvent event) {
		if (event.getEventTime() <= simulationEndTime) {
			if (componentMap.containsKey(event.getComponent())) {
				ComponentEventList compEvents = componentMap.get(event.getComponent());
				// if (compEvents.peek().compareTo(event) >= 0) {
				if (event.compareTo(compEvents.peek()) >= 0) {
					compEvents.add(event);
				} else {
					events.remove(compEvents);
					compEvents.add(event);
					events.add(compEvents);
				}
			} else {
				ComponentEventList compEvents = new ComponentEventList();
				compEvents.add(event);
				componentMap.put(event.getComponent(), compEvents);
				events.add(compEvents);
			}

		}

	}

	public void setSpentTime(final long spentTime) {
		this.spentTime = spentTime;
	}

	public int size() {
		int count = 0;
		for (ComponentEventList compEvents : events) {
			count += compEvents.size();
		}
		return count;
	}

	public void unscheduleEvent(final AbstractSimEvent event) {
		if (event != null) {
			if (event.getEventTime() <= simulationEndTime) {
				final ComponentEventList compEvents = componentMap.get(event.getComponent());
				if (compEvents.peek() == event) {
					events.remove(compEvents);
					if (compEvents.size() > 1) {
						compEvents.poll();
						events.add(compEvents);
					} else {
						componentMap.remove(event.getComponent());
					}

				} else {
					compEvents.remove(event);
				}
			}
		}
	}

	public void logEventListState() {
//		logger.trace("Component events: {}", events.size());
		for (ComponentEventList componentList : events) {
			for (ISimEvent event : componentList) {
//				logger.trace("Event left: {}", event);
			}
		}
	}

	private long simulationEndTime = Long.MAX_VALUE;

	public void setSimulationEndTime(long endTime) {
		simulationEndTime = endTime;
	}
}
