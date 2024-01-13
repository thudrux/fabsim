package de.terministic.fabsim.core;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimulationEngine {

	protected final Logger logger = LoggerFactory.getILoggerFactory().getLogger(this.getClass().getName());
	protected final IEventListManager eventList;
	protected long currentSimTime = 0L;
	protected IModel model;
	private final ArrayList<SimEventListener> listenerList = new ArrayList<>();
	protected ISimEventFactory eventFactory;
	long eventCounter = 0L;
	
	public SimulationEngine(ISimEventFactory eventFactory) {
		this.eventList = new PriorityQueueEventListManager();
		this.eventFactory = eventFactory;
		this.eventFactory.setSimulationEngine(this);

	}

	public SimulationEngine(IEventListManager eventList, ISimEventFactory eventFactory) {
		this.eventList = eventList;
		this.eventFactory = eventFactory;
		this.eventFactory.setSimulationEngine(this);

	}

	public void addListener(final SimEventListener listener) {
		this.listenerList.add(listener);
	}

	public List<SimEventListener> getListener() {
		return listenerList;
	}

	public void addListeners(List<SimEventListener> listeners) {
		for (SimEventListener listener : listeners) {
			addListener(listener);
		}
	}

	public ISimEventFactory getEventFactory() {
		return this.eventFactory;
	}

	public IEventListManager getEventList() {
		return this.eventList;
	}

	public IModel getModel() {
		return this.model;
	}

	public long getTime() {
		return this.currentSimTime;
	}

	protected void setTime(long time) {
		this.currentSimTime = time;
	}

	public void init(final IModel model) {
		this.model = model;
		if (model == null) {
			this.logger.error("FabModel can not be null");
		}
		eventFactory.setModel(model);
		model.setupForSimulation(this);
		model.initialize();
	}

	public void notifyListener(final ISimEvent event) {
		for (final SimEventListener listener : this.listenerList) {
			listener.notify(event);
		}
	}

	public void runSimulation(final long endTime) {
			eventList.scheduleEvent(new DummyEvent(model, Long.MAX_VALUE, null, null));
			eventList.setSimulationEndTime(endTime);
			boolean logStart = false;
	//		logger.debug("Simulation run starting (Number of resolved events was: {} events left in event list: {})",
	//				eventCounter, eventList.size());
	//		System.out.println("Event list size is " + this.eventList.size());
			while (this.eventList.size() > 0 && this.currentSimTime <= endTime) {
	//			if (eventCounter > 17138) {
	//			System.out.println(eventList.toString());
	//			}
				final ISimEvent event = this.eventList.getNextEvent();
				eventCounter++;
	//			System.out.println("[" + event.getEventTime() + "] Resolving event: " + event);
				if (event.getEventTime() < this.currentSimTime) {
					System.out.println("EventTime: " + event.getEventTime() + " EventCouter= " + eventCounter);
					System.out.println("Event (" + event.getClass() + ") was scheduled before(" + event.getEventTime()
							+ ") current simulation time(" + getTime() + ")");
					throw new SimulatorEngineException("Event (" + event.getClass() + ") was scheduled before("
							+ event.getEventTime() + ") current simulation time(" + getTime() + ")");
				}
				this.currentSimTime = event.getEventTime();
				if (this.currentSimTime > endTime) {
					break;
				} else {
	//				if ((event.getComponent() != null)) {// &&
	//														// (event.getComponent().getName().equals("ToolGroup_0")))
	//														// {
	//					this.logger.info("[{}] Resolving event: {}", getTime(), event);
	//				}
					event.resolveEvent();
					notifyListener(event);
					// if (eventCounter % 1000000 == 0) {
					// logger.trace("At {} there are {} events left in the event
					// list", event.getTime(), eventList.size());
					// }
				}
			}
			this.currentSimTime = endTime;
	//		logger.debug("Simulation run completed(Number of resolved events was: {} events left in event list: {})",
	//				eventCounter, eventList.size());
			// eventList.logEventListState();
		}

	public void setEventFactory(final ISimEventFactory eventFactory) {
		this.eventFactory = eventFactory;
	}

	public long getEventCounter() {
		return eventCounter;
	}

	public void setEventCounter(long eventCounter) {
		this.eventCounter = eventCounter;
	}

}